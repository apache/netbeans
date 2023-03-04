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

package org.netbeans.modules.apisupport.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import static org.netbeans.modules.apisupport.project.ui.Bundle.*;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans module project.
 * @author Jesse Glick
 */
public final class ModuleLogicalView implements LogicalViewProvider {
    
    private final @NonNull NbModuleProject project;
    
    public ModuleLogicalView(@NonNull NbModuleProject project) {
        this.project = project;
    }
    
    public @Override Node createLogicalView() {
        return new RootNode(project);
    }
    
    /** cf. #45952 */
    public @Override Node findPath(Node root, Object target) {
        if (root.getLookup().lookup(NbModuleProject.class) != project) {
            // Not intended for this project. Should not normally happen anyway.
            return null;
        }
        
        DataObject file;
        
        if (target instanceof FileObject) {
            try {
                file = DataObject.find((FileObject) target);
            } catch (DataObjectNotFoundException e) {
                // #158131: might have tried unsuccessfully to delete, etc.
                return null;
            }
        } else if (target instanceof DataObject) {
            file = (DataObject) target;
        } else {
            // What is it?
            return null;
        }

        for (Node rootChild : root.getChildren().getNodes(true)) {
            Node found = PackageView.findPath(rootChild, target);
            //System.err.println("found " + found + " for " + target + " in " + rootChildren[i]);
            if (found != null) {
                return found;
            }
            // For Important Files node:
            if (rootChild.getName().equals(ImportantFilesNodeFactory.IMPORTANT_FILES_NAME)) {
                for (Node ifChild : rootChild.getChildren().getNodes(true)) {
                    if (ifChild.getLookup().lookup(DataObject.class) == file) {
                        return ifChild;
                    }
                }
            }
        }
        
        return null;
    }
    
    private static final class RootNode extends AbstractNode implements PropertyChangeListener {
        
        private final @NonNull NbModuleProject project;
        private final @NonNull ProjectInformation pi;
        
        @Messages({"# {0} - folder", "HINT_project_root_node=Module project in {0}"})
        RootNode(@NonNull NbModuleProject project) {
            
            // XXX add a NodePathResolver impl to lookup
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-apisupport-project/Nodes"), 
                  Lookups.fixed(project, DataFolder.findFolder( project.getProjectDirectory() ),
                    project.getProjectDirectory()));
            this.project = project;
            boolean osgi = false;
                Manifest man = project.getManifest();
                if (man != null) {
                    Attributes attrs = man.getMainAttributes();
                    if (attrs != null) {
                        osgi = attrs.getValue(ManifestManager.BUNDLE_SYMBOLIC_NAME) != null;
                    }
                }
            setIconBaseWithExtension(
                osgi ? NbModuleProject.NB_PROJECT_OSGI_ICON_PATH :
                NbModuleProject.NB_PROJECT_ICON_PATH
            );
            pi = ProjectUtils.getInformation(project); // must keep reference to PI, it is a wrapper and would be immediately GCed
            setDisplayName(pi.getDisplayName());
            setShortDescription(HINT_project_root_node(FileUtil.getFileDisplayName(project.getProjectDirectory())));
            pi.addPropertyChangeListener(WeakListeners.propertyChange(this, pi));
        }
        
        public @Override Action[] getActions(boolean ignore) {
            return ModuleActions.getProjectActions(project);
        }
        
        public @Override boolean canRename() {
            return true;
        }
        
        public @Override String getName() {
            return pi.getDisplayName();
        }
        
        public @Override void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(project, name);
        }

        @Override
        public void propertyChange (final PropertyChangeEvent evt) {
            ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {
                public @Override void run() {
                    fireNameChange(null, null);
                    fireDisplayNameChange(null, null);
                }
            });
        }
        
    }
}
