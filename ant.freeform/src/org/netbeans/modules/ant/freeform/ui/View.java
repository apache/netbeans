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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
