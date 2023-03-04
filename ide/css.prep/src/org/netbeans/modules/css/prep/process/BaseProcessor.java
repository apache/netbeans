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
package org.netbeans.modules.css.prep.process;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.prep.util.BaseCssPreprocessor;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Process file/folder changes.
 */
abstract class BaseProcessor {

    private static final Logger LOGGER = Logger.getLogger(BaseProcessor.class.getName());

    protected final BaseCssPreprocessor cssPreprocessor;
    private final Set<FileObject> processedFiles = new HashSet<>();


    BaseProcessor(BaseCssPreprocessor cssPreprocessor) {
        assert cssPreprocessor != null;
        this.cssPreprocessor = cssPreprocessor;
    }

    protected abstract boolean isEnabledInternal(@NonNull Project project);

    protected abstract boolean isSupportedFile(FileObject fileObject);

    protected abstract boolean isPartial(FileObject fileObject);

    protected abstract List<Pair<String, String>> getMappings(Project project);

    protected abstract String getCompilerOptions(Project project);

    protected abstract void compileInternal(Project project, File workDir, File source, File target, List<String> compilerOptions);

    public void process(Project project, FileObject fileObject, String originalName, String originalExtension) {
        if (!isEnabled(project)) {
            // not enabled in this project
            return;
        }
        if (fileObject.isData()) {
            processFile(project, fileObject, originalName, originalExtension);
        } else {
            assert fileObject.isFolder() : "Folder expected: " + fileObject;
            processFolder(project, fileObject);
        }
    }

    private void processFolder(Project project, FileObject fileObject) {
        assert fileObject.isFolder() : "Folder expected: " + fileObject;
        for (FileObject child : fileObject.getChildren()) {
            if (child.isData()) {
                processFile(project, child, null, null);
            } else {
                processFolder(project, child);
            }
        }
    }

    private void processFile(Project project, FileObject fileObject, String originalName, String originalExtension) {
        assert fileObject.isData() : "File expected: " + fileObject;
        if (!isSupportedFile(fileObject)) {
            // unsupported file
            return;
        }
        if (!processedFiles.add(fileObject)) {
            // already processed
            return;
        }
        if (fileObject.isValid()) {
            fileChanged(project, fileObject);
            if (originalName != null) {
                // file renamed
                fileRenamed(project, fileObject, originalName, originalExtension);
            }
        } else {
            // deleted file
            fileDeleted(project, fileObject);
        }
    }

    private boolean isEnabled(Project project) {
        if (project == null) {
            return true;
        }
        return isEnabledInternal(project);
    }

    protected void fileChanged(Project project, FileObject fileObject) {
        if (!isPartial(fileObject)) {
            compile(project, fileObject);
        }
        compileReferences(project, fileObject);
    }

    protected void compile(Project project, FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        if (file == null) {
            LOGGER.log(Level.WARNING, "Not compiling, file not found for fileobject {0}", FileUtil.getFileDisplayName(fileObject));
            return;
        }
        FileObject webRoot = getWebRoot(project, fileObject);
        File target = getTargetFile(project, webRoot, file);
        if (target == null) {
            // not found
            return;
        }
        compileInternal(project, FileUtil.toFile(webRoot), file, target,
                CssPreprocessorUtils.parseCompilerOptions(getCompilerOptions(project), webRoot));
    }

    protected void compileReferences(Project project, FileObject fileObject) {
        if (project == null) {
            // we need project for dependencies
            LOGGER.log(Level.INFO, "Cannot compile 'import' file {0}, no project", fileObject);
            return;
        }
        try {
            DependenciesGraph dependenciesGraph = CssIndex.get(project).getDependencies(fileObject);
            for (FileObject referring : dependenciesGraph.getAllReferingFiles()) {
                if (fileObject.equals(referring)) {
                    // ignore myself
                    continue;
                }
                processFile(project, referring, null, null);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private void fileRenamed(Project project, FileObject fileObject, String originalName, String originalExtension) {
        assert originalName != null : fileObject;
        assert originalExtension != null : fileObject;
        File originalFile = new File(FileUtil.toFile(fileObject).getParentFile(), originalName + "." + originalExtension); // NOI18N
        File targetFile = getTargetFile(project, getWebRoot(project, fileObject), originalFile);
        deleteFile(targetFile);
        deleteMapFile(targetFile);
    }

    private void fileDeleted(Project project, FileObject fileObject) {
        File targetFile = getTargetFile(project, getWebRoot(project, fileObject), FileUtil.toFile(fileObject));
        deleteFile(targetFile);
        deleteMapFile(targetFile);
    }

    // #239916
    private void deleteMapFile(File targetFile) {
        if (targetFile == null) {
            return;
        }
        deleteFile(getMapFile(targetFile));
    }

    protected File getMapFile(@NonNull File targetFile) {
        assert targetFile != null;
        return new File(targetFile.getParent(), targetFile.getName() + ".map"); // NOI18N
    }

    private void deleteFile(File file) {
        if (file == null
                || !file.isFile()) {
            return;
        }
        FileObject fo = FileUtil.toFileObject(file);
        assert fo != null;
        try {
            fo.delete();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot delete file", ex);
        }
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "BaseProcessor.error.mappings.empty=No mappings found for {0} preprocessor",
    })
    @CheckForNull
    protected File getTargetFile(Project project, FileObject webRoot, File file) {
        if (webRoot == null) {
            LOGGER.log(Level.INFO, "Not compiling, file {0} not underneath web root of project {1}",
                    new Object[] {file, FileUtil.getFileDisplayName(project.getProjectDirectory())});
            return null;
        }
        List<Pair<String, String>> mappings = getMappings(project);
        if (mappings.isEmpty()) {
            LOGGER.log(Level.INFO, "Not compiling, no mappings for project {0}", FileUtil.getFileDisplayName(project.getProjectDirectory()));
            cssPreprocessor.fireProcessingErrorOccured(project, Bundle.BaseProcessor_error_mappings_empty(cssPreprocessor.getDisplayName()));
            return null;
        }
        File target = CssPreprocessorUtils.resolveTarget(webRoot, mappings, file);
        if (target == null) {
            LOGGER.log(Level.INFO, "Not compiling, file {0} not matched within current mappings {1}",
                    new Object[] {file, mappings});
            return null;
        }
        return target;
    }

    // #237600
    @CheckForNull
    private FileObject getWebRoot(Project project, FileObject fileObject) {
        // try to resolve webroot even if input file is not underneath webroot
        FileObject webRoot = CssPreprocessorUtils.getWebRoot(project, fileObject);
        if (webRoot != null) {
            return webRoot;
        }
        // simply get some webroot
        return CssPreprocessorUtils.getWebRoot(project);
    }

}
