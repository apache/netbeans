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

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
abstract class FileOperationFactory {
    protected final PhpProject project;

    private final PhpVisibilityQuery phpVisibilityQuery;

    private volatile boolean factoryError = false;

    public FileOperationFactory(PhpProject project) {
        assert project != null;
        this.project = project;
        phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
    }

    final Callable<Boolean> createInitHandler(FileObject source) {
        if (isInvalid()) {
            getLogger().log(Level.FINE, "No INIT handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        return createInitHandlerInternal(source);
    }

    final Callable<Boolean> createReinitHandler(FileObject source) {
        if (isInvalid()) {
            getLogger().log(Level.FINE, "No REINIT handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        return createReinitHandlerInternal(source);
    }

    final Callable<Boolean> createCopyHandler(FileObject source, FileEvent fileEvent) {
        if (isInvalid()) {
            getLogger().log(Level.FINE, "No CREATE handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        if (!isValid(fileEvent)) {
            getLogger().log(Level.FINE, "No CREATE handler, File Event invalid for project {0}", project.getName());
            return null;
        }
        return createCopyHandlerInternal(source, fileEvent);
    }

    final Callable<Boolean> createRenameHandler(FileObject source, String oldName, FileRenameEvent fileRenameEvent) {
        if (isInvalid()) {
            getLogger().log(Level.FINE, "No RENAME handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        if (!isValid(fileRenameEvent)) {
            getLogger().log(Level.FINE, "No RENAME handler, File Event invalid for project {0}", project.getName());
            return null;
        }
        return createRenameHandlerInternal(source, oldName, fileRenameEvent);
    }

    final Callable<Boolean> createDeleteHandler(FileObject source, FileEvent fileEvent) {
        if (isInvalid()) {
            getLogger().log(Level.FINE, "No DELETE handler, File Operation Factory invalid for project {0}", project.getName());
            return null;
        }
        if (!isValid(fileEvent)) {
            getLogger().log(Level.FINE, "No DELETE handler, File Event invalid for project {0}", project.getName());
            return null;
        }
        return createDeleteHandlerInternal(source, fileEvent);
    }

    abstract Logger getLogger();
    protected abstract boolean isEnabled();
    protected abstract boolean isValid(FileEvent fileEvent);
    protected abstract Callable<Boolean> createInitHandlerInternal(FileObject source);
    protected abstract Callable<Boolean> createReinitHandlerInternal(FileObject source);
    protected abstract Callable<Boolean> createCopyHandlerInternal(FileObject source, FileEvent fileEvent);
    protected abstract Callable<Boolean> createRenameHandlerInternal(FileObject source, String oldName, FileRenameEvent fileRenameEvent);
    protected abstract Callable<Boolean> createDeleteHandlerInternal(FileObject source, FileEvent fileEvent);

    final void reset() {
        factoryError = false;
        resetInternal();
    }

    protected void resetInternal() {
    }

    final void invalidate() {
        factoryError = true;
    }

    final boolean isInvalid() {
        return factoryError;
    }

    protected final boolean isSourceFileValid(FileObject source) {
        assert CommandUtils.isUnderSources(project, source) : String.format("File %s not underneath sources of project %s", getPath(source), project.getName());
        return !isNbProjectMetadata(source) && PhpProjectUtils.isVisible(phpVisibilityQuery, source);
    }

    boolean isNbProjectMetadata(FileObject fo) {
        // #193869
        FileObject nbprojectDir = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        return FileUtil.isParentOf(nbprojectDir, fo) || nbprojectDir.equals(fo);
    }

    protected FileObject getSources() {
        return ProjectPropertiesSupport.getSourcesDirectory(project);
    }

    protected static String getPath(FileObject fo) {
        return FileUtil.getFileDisplayName(fo);
    }

    protected boolean askUser(String message) {
        Object openProperties = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, project.getName(), JOptionPane.YES_NO_OPTION));
        return openProperties.equals(JOptionPane.YES_OPTION);
    }

    protected void showCustomizer(final String category) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PhpProjectUtils.openCustomizer(project, category);
            }
        });
    }
}
