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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Logical view of a freeform project.
 * @author Jesse Glick
 */
public final class View implements LogicalViewProvider {

    private final FreeformProject project;
    
    public View(FreeformProject project) {
        this.project = project;
    }
    
    public Node createLogicalView() {
        return new RootNode(project);
    }
    
    public Node findPath(Node root, Object target) {
        // Check each child node in turn.
        Node[] kids = root.getChildren().getNodes(true);
        for (Node kid : kids) {
            // First ask natures.
            for (ProjectNature nature : Lookup.getDefault().lookupAll(ProjectNature.class)) {
                Node n = nature.findSourceFolderViewPath(project, kid, target);
                if (n != null) {
                    return n;
                }
            }
            // Otherwise, check children and look for <source-folder>/<source-file> matches.
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = kid.getLookup().lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = target instanceof DataObject ? ((DataObject) target).getPrimaryFile() : (FileObject) target;
                if (kidFO == targetFO) {
                    return kid;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);
                    List<String> path = Collections.list(NbCollections.checkedEnumerationByFilter(new StringTokenizer(relPath, "/"), String.class, true)); // NOI18N
                    // XXX see original code for justification
                    path.set(path.size() - 1, targetFO.getName());
                    try {
                        return NodeOp.findPath(kid, Collections.enumeration(path));
                    } catch (NodeNotFoundException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
    
    private static final class RootNode extends AbstractNode implements PropertyChangeListener {
        
        private final FreeformProject p;
        private final ProjectInformation info;
        private static final RequestProcessor RP = new RequestProcessor(RootNode.class);
        
        @SuppressWarnings("LeakingThisInConstructor")
        public RootNode(FreeformProject p) {
            super(NodeFactorySupport.createCompositeChildren(p, "Projects/org-netbeans-modules-ant-freeform/Nodes"), Lookups.singleton(p));
            this.p = p;
            info = ProjectUtils.getInformation(p);
            info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
        }
        
        @Override
        public String getName() {
            return info.getDisplayName();
        }
        
        @Override
        public String getShortDescription() {
            return NbBundle.getMessage(View.class, "View.RootNode.shortDescription", FileUtil.getFileDisplayName(p.getProjectDirectory()));
        }
        
        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(info.getIcon());
        }
        
        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        @Override
        public Action[] getActions(boolean context) {
            return CommonProjectActions.forType("org-netbeans-modules-ant-freeform"); // NOI18N
        }
        
        @Override
        public boolean canRename() {
            return true;
        }
        
        @Override
        public boolean canDestroy() {
            return false;
        }
        
        @Override
        public boolean canCut() {
            return false;
        }
        
        @Override
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(p, name);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("freeform.node." + org.netbeans.modules.ant.freeform.Util.getMergedHelpIDFragments(p)); // NOI18N
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            RP.post(new Runnable() {
                public @Override void run() {
                    fireNameChange(null, null);
                    fireDisplayNameChange(null, null);
                    fireIconChange();
                    fireOpenedIconChange();
                }
            });
        }
        
    }
    
}
