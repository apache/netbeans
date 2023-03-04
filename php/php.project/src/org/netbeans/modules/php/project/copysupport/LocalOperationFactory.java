/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.project.copysupport;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
@org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
final class LocalOperationFactory extends FileOperationFactory {

    private static final Logger LOGGER = Logger.getLogger(LocalOperationFactory.class.getName());

    LocalOperationFactory(PhpProject project) {
        super(project);
    }

    @Override
    protected boolean isEnabled() {
        return isEnabled(true);
    }

    private boolean isEnabled(boolean verbose) {
        boolean copySourcesEnabled = ProjectPropertiesSupport.isCopySourcesEnabled(project);
        if (verbose) {
            LOGGER.log(Level.FINE, "LOCAL copying enabled for project {0}: {1}", new Object[] {project.getName(), copySourcesEnabled});
        }
        return copySourcesEnabled;
    }

    private boolean isEnabledAndValidConfig() {
        if (!isEnabled(false)) {
            LOGGER.log(Level.FINE, "LOCAL copying not enabled for project {0}", project.getName());
            return false;
        }

        if (isInvalid()) {
            LOGGER.log(Level.FINE, "LOCAL copying invalid for project {0}", project.getName());
            return false;
        }

        if (getSources() == null) {
            LOGGER.log(Level.WARNING, "LOCAL copying disabled for project {0}. Reason: source root is null", project.getName());
            return false;
        }

        File targetRoot = getTargetRoot();
        if (targetRoot == null) {
            LOGGER.log(Level.INFO, "LOCAL copying disabled for project {0}. Reason: target folder is null", project.getName());

            if (askUser(NbBundle.getMessage(LocalOperationFactory.class, "MSG_NoTargetFolder", project.getName()))) {
                showCustomizer(CompositePanelProviderImpl.SOURCES);
            }
            invalidate();
            return false;
        }

        File writableFolder = targetRoot;
        while (writableFolder != null && !writableFolder.exists()) {
            writableFolder = writableFolder.getParentFile();
        }

        boolean isWritable = writableFolder != null && FileUtils.isDirectoryWritable(writableFolder);
        if (!isWritable) {
            LOGGER.log(Level.INFO, "LOCAL copying disabled for project {0}. Reason: target folder {1} is not writable", new Object[] {project.getName(), writableFolder});

            if (askUser(NbBundle.getMessage(LocalOperationFactory.class, "MSG_TargetFolderNotWritable", project.getName(), writableFolder))) {
                showCustomizer(CompositePanelProviderImpl.SOURCES);
            }
            invalidate();
            return false;
        }

        return true;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected Callable<Boolean> createInitHandlerInternal(final FileObject source) {
        LOGGER.log(Level.FINE, "Creating INIT handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        if (!ProjectPropertiesSupport.isCopySourcesOnOpen(project)) {
            LOGGER.log(Level.FINE, "Copying on open not enabled for project (project {0})", project.getName());
            return null;
        }
        return createCopyFolderHandler(source);
    }

    @Override
    protected Callable<Boolean> createReinitHandlerInternal(final FileObject source) {
        LOGGER.log(Level.FINE, "Creating REINIT handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return createCopyFolderHandler(source);
    }

    @Override
    protected Callable<Boolean> createCopyHandlerInternal(final FileObject source, FileEvent fileEvent) {
        LOGGER.log(Level.FINE, "Creating COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        // createCopyFolderRecursivelyHandler used because of external changes
        // fire just one FS event for top most folder. See #172139
        return source.isFolder() ? createCopyFolderHandler(source) : createCopyFileHandler(source);
    }

    private Callable<Boolean> createCopyFolderHandler(final FileObject source) {
        assert source.isFolder();
        LOGGER.log(Level.FINE, "Creating COPY FOLDER handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running COPY FOLDER handler for {0} (project {1})", new Object[]{getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }
                if (!target.exists()) {
                    FileUtil.createFolder(target);
                    if (!target.isDirectory()) {
                        LOGGER.log(Level.FINE, "Failed for {0}, cannot create directory {1}", new Object[]{getPath(source), target});
                        return false;
                    }
                    LOGGER.log(Level.FINE, "Directory {0} created", target);
                }
                Boolean work = null;
                String[] list = target.list();
                if (list != null) {
                    // if any children exist, they will be:
                    // - overwritten (if they exist in sources)
                    // - kept untouched (if they don't exist in sources)
                    Enumeration<? extends FileObject> children = source.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        if (!isSourceFileValid(child)) {
                            LOGGER.log(Level.FINE, "Ignored for {0} (not valid)", getPath(child));
                            continue;
                        }
                        target = getTarget(child, false);
                        if (target == null) {
                            LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(child));
                            continue;
                        }
                        if (!doCopy(child, target)) {
                            return false;
                        }
                        work = true;
                    }
                }
                return work;
            }
        };
    }

    private Callable<Boolean> createCopyFileHandler(final FileObject source) {
        assert source.isData();
        LOGGER.log(Level.FINE, "Creating COPY FILE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running COPY FILE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }
                return doCopy(source, target);
            }
        };
    }

    @Override
    protected Callable<Boolean> createRenameHandlerInternal(final FileObject source, final String oldName, FileRenameEvent fileRenameEvent) {
        LOGGER.log(Level.FINE, "Creating RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }

                if (source.isFolder()) {
                    Enumeration<? extends FileObject> children = source.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        if (!isSourceFileValid(child)) {
                            LOGGER.log(Level.FINE, "Ignored for {0} (not valid)", getPath(child));
                            continue;
                        }
                        final File childTarget = getTarget(child, false);
                        if (childTarget != null
                                && !doCopy(child, childTarget)) {
                            return false;
                        }
                    }
                } else {
                    if (!doCopy(source, target)) {
                        return false;
                    }
                }
                // delete the old file/directory
                File parent = target.getParentFile();
                if (parent != null) {
                    File oldTarget = new File(parent, oldName);
                    return doDelete(oldTarget);
                }
                return true;
            }
        };
    }

    @Override
    protected Callable<Boolean> createDeleteHandlerInternal(final FileObject source, FileEvent fileEvent) {
        LOGGER.log(Level.FINE, "Creating DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }
                return doDelete(target);
            }
        };
    }

    private File getTargetRoot() {
        return ProjectPropertiesSupport.getCopySourcesTarget(project);
    }

    private Pair<FileObject, File> getConfigPair() {
        return Pair.of(getSources(), getTargetRoot());
    }

    private File getTarget(FileObject source) {
        return getTarget(source, true);
    }

    private File getTarget(FileObject source, boolean deepCheck) {
        LOGGER.log(Level.FINE, "Getting target for {0} (project {1}, deep check: {2})", new Object[] {getPath(source), project.getName(), deepCheck});
        Pair<FileObject, File> cfgPair = getConfigPair();
        if (deepCheck) {
            if (!isEnabledAndValidConfig()) {
                LOGGER.fine("\t-> null (invalid config)");
                return null;
            }
            if (!isPairValid(cfgPair)) {
                LOGGER.fine("\t-> null (invalid config pair)");
                return null;
            }
        }
        if (!isSourceFileValid(source)) {
            LOGGER.fine("\t-> null (invalid source)");
            return null;
        }

        FileObject sourceRoot = cfgPair.first();
        File targetRoot = cfgPair.second();
        assert sourceRoot != null;
        assert targetRoot != null;

        String relativePath = FileUtil.getRelativePath(sourceRoot, source);
        assert relativePath != null : String.format("Relative path be found because isSourceFileValid() was already called for %s", getPath(source));
        LOGGER.fine("\t-> found");
        return FileUtil.normalizeFile(new File(targetRoot, relativePath));
    }

    private boolean doCopy(FileObject source, File target) throws IOException {
        LOGGER.log(Level.FINE, "Copying file {0} -> {1}", new Object[] {getPath(source), target});
        File targetParent = target.getParentFile();
        if (source.isData()) {
            doDelete(target);
            FileObject parent = FileUtil.createFolder(targetParent);
            FileUtil.copyFile(source, parent, source.getName(), source.getExt());
            LOGGER.log(Level.FINE, "File {0} copied to {1}", new Object[] {getPath(source), target});
        } else {
            String[] childs = target.list();
            if (childs == null || childs.length == 0) {
                doDelete(target);
            }
            FileUtil.createFolder(target);
            LOGGER.log(Level.FINE, "Folder {0} created", target);
        }
        return target.exists();
    }

    private boolean doDelete(File target) throws IOException {
        LOGGER.log(Level.FINE, "Deleting file {0}", target);
        if (!target.exists()) {
            // nothing to do, no error
            LOGGER.log(Level.FINE, "File {0} does not exists, nothing to delete", target);
            return true;
        }
        FileObject targetFo = FileUtil.toFileObject(FileUtil.normalizeFile(target));
        assert targetFo != null : "FileObject must be found for " + target;
        if (!targetFo.isValid()) {
            LOGGER.log(Level.FINE, "FileObject {0} is not valid, nothing to delete", getPath(targetFo));
        } else {
            targetFo.delete();
            LOGGER.log(Level.FINE, "File {0} deleted", getPath(targetFo));
        }
        return !target.exists();
    }

    private static boolean isPairValid(Pair<FileObject, File> pair) {
        return pair != null && pair.first() != null && pair.second() != null;
    }

    @Override
    protected boolean isValid(FileEvent fileEvent) {
        return true;
    }
}
