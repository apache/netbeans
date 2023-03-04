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
package org.netbeans.modules.cordova.project;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
//@NodeFactory.Registration(projectType = "org-netbeans-modules-web-clientproject", position = 800)
public class ClientProjectNodeFactory implements NodeFactory {

    
    @Override
    public NodeList<?> createNodes(Project p) {
        return new PlatformsSingleton(p);
    }

    private static class PlatformsSingleton implements NodeList, FileChangeListener {

        private static Object key = "platforms"; // NOI18N
        
        private ChangeSupport changeSupport = new ChangeSupport(this);        
        
        private FileObject root;
        
        public PlatformsSingleton(Project p) {
            root = p.getProjectDirectory();
            
            root.addFileChangeListener(this);
        }


        @Override
        public void fileFolderCreated(FileEvent fe) {
            changeSupport.fireChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            changeSupport.fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            changeSupport.fireChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public List keys() {
            FileObject platforms = root.getFileObject("platforms"); // NOI18N
            if (platforms != null) {
                    return Collections.singletonList(key);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(Object k) {
            if (key.equals(k)) {
                FileObject platforms = root.getFileObject("platforms"); // NOI18N
                if (platforms != null) {
                    try {
                        DataObject dob = DataObject.find(platforms);
                        return dob.getNodeDelegate();
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return null;
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }
    }

    
}
