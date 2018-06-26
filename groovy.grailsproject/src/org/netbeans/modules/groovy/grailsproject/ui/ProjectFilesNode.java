/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
