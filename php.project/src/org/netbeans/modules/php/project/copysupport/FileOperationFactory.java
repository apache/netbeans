/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
