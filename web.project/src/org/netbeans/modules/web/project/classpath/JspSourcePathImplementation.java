/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Implementation of ClassPathImplementation which represents the Web Pages folder.
 *
 * @author Andrei Badea
 */
final class JspSourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List <PathResourceImplementation> resources;
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private ProjectDirectoryListener projectDirListener;

    /**
     * Construct the implementation.
     */
    public JspSourcePathImplementation(AntProjectHelper helper, PropertyEvaluator eval) {
        assert helper != null;
        assert eval != null;
        this.helper = helper;
        this.evaluator = eval;
        eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        FileObject projectDir = helper.getProjectDirectory();
        projectDirListener = new ProjectDirectoryListener();
        projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(projectDirListener, projectDir));
    }

    public List <PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return resources;
            }
        }
        PathResourceImplementation webDocbaseDirRes = null;
        String webDocbaseDir = evaluator.getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
        if (webDocbaseDir != null) {
            FileObject webDocbaseDirFO = helper.resolveFileObject(webDocbaseDir);
            if (webDocbaseDirFO != null) {
                try {
                    webDocbaseDirRes = ClassPathSupport.createResource(webDocbaseDirFO.getURL());
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        synchronized (this) {
            if (this.resources == null) {
                if (webDocbaseDirRes != null) {
                    this.resources = Collections.singletonList(webDocbaseDirRes);
                } else {
                    this.resources = Collections.<PathResourceImplementation>emptyList();
                }
            }
            return this.resources;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (WebProjectProperties.WEB_DOCBASE_DIR.equals(evt.getPropertyName())) {
            fireChange();
        }
    }
    
    private void fireChange() {
        synchronized (this) {
            this.resources = null;
        }
        this.support.firePropertyChange (PROP_RESOURCES,null,null);
    }
    
    private final class ProjectDirectoryListener implements FileChangeListener {

        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileDeleted(FileEvent fe) {
            if (isWatchedFile(getFileName(fe))) {
                fireChange();
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            if (isWatchedFile(getFileName(fe))) {
                fireChange();
            }
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            if (isWatchedFile(getFileName(fe)) || isWatchedFile(getOldFileName(fe))) {
                fireChange();
            }
        }

        private boolean isWatchedFile(String fileName) {
            String webDir = evaluator.getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
            return fileName.equals(webDir);
        }

        private String getFileName(FileEvent fe) {
            return fe.getFile().getNameExt();
        }

        private String getOldFileName(FileRenameEvent fe) {
            String result = fe.getName();
            if (!(fe.getExt()).equals("")) { // NOI18N
                result = result + "." + fe.getExt(); // NOI18N
            }
            return result;
        }
    }
}
