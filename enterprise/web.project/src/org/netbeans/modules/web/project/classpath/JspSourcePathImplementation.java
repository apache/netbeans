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
                webDocbaseDirRes = ClassPathSupport.createResource(webDocbaseDirFO.toURL());
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
