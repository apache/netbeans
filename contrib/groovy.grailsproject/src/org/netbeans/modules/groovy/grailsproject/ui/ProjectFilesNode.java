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
package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Bruno Flavio
 */
class ProjectFilesNode extends AbstractNode {
    
    private static final String PF_BADGE = "org/netbeans/modules/groovy/grailsproject/resources/projectfilesBadge.png"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/groovy/grailsproject/resources/defaultFolder.gif"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/groovy/grailsproject/resources/defaultFolderOpen.gif"; // NOI18N
    
    public ProjectFilesNode(GrailsProject project) {
        super( Children.create( new ProjectFilesChildren(project), true),
               Lookups.fixed( project.getProjectDirectory() )
        );
    }
    
    @Override
    public final Image getIcon(int param) {
        return getIcon(false);
    }

    @Override
    public final Image getOpenedIcon(int param) {
        return getIcon(true);
    }
    
    private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(PF_BADGE, true); //NOI18N
        Image base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
        Image img = ImageUtilities.mergeImages(base, badge, 8, 8);
        return img;
    }
    
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ProjectFilesNode.class, "LBL_ProjectFiles");
    }
        
    private static class ProjectFilesChildren extends ChildFactory.Detachable<FileObject> {

        private final GrailsProject project;
        private final FileChangeAdapter fileChangeListener;
        
        ProjectFilesChildren(GrailsProject proj) {
            project = proj;
            fileChangeListener = new FileChangeAdapter() {
                @Override public void fileDataCreated(FileEvent fe) {
                    refresh(false);
                }
                @Override public void fileDeleted(FileEvent fe) {
                    refresh(false);
                }
            };
        }

        @Override protected Node createNodeForKey(FileObject key) {
            try {
                return DataObject.find(key).getNodeDelegate().cloneNode();
            } catch (DataObjectNotFoundException e) {
                return null;
            }
        }

        @Override protected boolean createKeys(List<FileObject> keys) {
            FileObject root = project.getProjectDirectory();
            //Grails3:
            keys.add(root.getFileObject("build.gradle")); // NOI18N
            keys.add(root.getFileObject("gradle.properties")); // NOI18N
            
            //Grails2: 
            keys.add(root.getFileObject("application.properties")); // NOI18N
            
            keys.removeAll(Collections.singleton(null));
            return true;
        }

        @Override protected void addNotify() {
            project.getProjectDirectory().addFileChangeListener(fileChangeListener);
        }
        
        @Override protected void removeNotify() {
            project.getProjectDirectory().removeFileChangeListener(fileChangeListener);
        }
    }
}
