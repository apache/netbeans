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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.logicalview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.netbeans.spi.queries.VisibilityQueryChangeEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Radek Matous
 */
public class PhpSourcesFilter implements  ChangeListener, ChangeableDataFilter {
        private static final long serialVersionUID = -743974567467955L;

        private final PhpProject project;
        private final FileObject rootFolder;
        private final PhpVisibilityQuery phpVisibilityQuery;
        // can be null when e.g. deleting project
        private final FileObject nbProject;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public PhpSourcesFilter(PhpProject project) {
            this(project, null);
        }
        public PhpSourcesFilter(PhpProject project, FileObject rootFolder) {
            assert project != null;

            this.project = project;
            this.rootFolder = rootFolder;

            phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
            nbProject = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N

            ProjectPropertiesSupport.addWeakIgnoredFilesListener(project, this);
        }

        @Override
        public boolean acceptDataObject(DataObject object) {
            return acceptFileObject(object.getPrimaryFile());
        }

        public boolean acceptFileObject(FileObject file) {
            return !isNbProject(file)
                    && !isTestDirectory(file)
                    && !isSeleniumDirectory(file)
                    && phpVisibilityQuery.isVisible(file);
        }

        private boolean isNbProject(FileObject file) {
            return file.equals(nbProject);
        }

        private boolean isTestDirectory(FileObject file) {
            for (FileObject root : ProjectPropertiesSupport.getTestDirectories(project, false)) {
                if (isDirectory(file, root)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSeleniumDirectory(FileObject file) {
            return isDirectory(file, ProjectPropertiesSupport.getSeleniumDirectory(project, false));
        }

        private boolean isDirectory(FileObject file, FileObject directory) {
            if (rootFolder == null || directory == null) {
                return false;
            }
            if (!directory.equals(rootFolder)) {
                // in sources or similar (but not in 'directory' definitely)
                return directory.equals(file);
            }
            return false;
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            if (event instanceof VisibilityQueryChangeEvent) {
                FileObject[] fileObjects = ((VisibilityQueryChangeEvent) event).getFileObjects();
                for (FileObject fileObject : fileObjects) {
                    if (project.equals(ProjectConvertors.getNonConvertorOwner(fileObject))) {
                        changeSupport.fireChange();
                        return;
                    }
                }
                return;
            }
            changeSupport.fireChange();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
    }
