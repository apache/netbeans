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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans suite project.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class SuiteLogicalView implements LogicalViewProvider {
    
    private final SuiteProject suite;
    
    public SuiteLogicalView(final SuiteProject suite) {
        this.suite = suite;
    }
    
    public Node createLogicalView() {
        return new SuiteRootNode(suite);
    }
    
    public Node findPath(Node root, Object target) {
        if (root.getLookup().lookup(SuiteProject.class) != suite) {
            // Not intended for this project. Should not normally happen anyway.
            return null;
        }
        
        DataObject file;
        if (target instanceof FileObject) {
            try {
                file = DataObject.find((FileObject) target);
            } catch (DataObjectNotFoundException e) {
                return null; // OK
            }
        } else if (target instanceof DataObject) {
            file = (DataObject) target;
        } else {
            // What is it?
            return null;
        }
        
        Node impFilesNode = root.getChildren().findChild("important.files"); // NOI18N
        if (impFilesNode != null) {
            Node[] impFiles = impFilesNode.getChildren().getNodes(true);
            for (int i = 0; i < impFiles.length; i++) {
                if (impFiles[i].getCookie(DataObject.class) == file) {
                    return impFiles[i];
                }
            }
        }
        
        return null;
    }
    
    /** Package private for unit test only. */
    static final class SuiteRootNode extends AbstractNode
            implements PropertyChangeListener {
        
        private static final Image ICON = ImageUtilities.loadImage(SuiteProject.SUITE_ICON_PATH, true);
        
        private final SuiteProject suite;
        private final ProjectInformation info;
        
        SuiteRootNode(final SuiteProject suite) {
            super(NodeFactorySupport.createCompositeChildren(suite, "Projects/org-netbeans-modules-apisupport-project-suite/Nodes"), 
                  Lookups.fixed(new Object[] {suite, DataFolder.findFolder( suite.getProjectDirectory() ),
                    suite.getProjectDirectory()}));
            this.suite = suite;
            info = ProjectUtils.getInformation(suite);
            info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
        }
        
        public String getName() {
            return info.getDisplayName();
        }
        
        public String getDisplayName() {
            return info.getDisplayName();
        }
        
        public String getShortDescription() {
            return NbBundle.getMessage(SuiteLogicalView.class, "HINT_suite_project_root_node",
                    FileUtil.getFileDisplayName(suite.getProjectDirectory()));
        }
        
        public Action[] getActions(boolean context) {
            return SuiteActions.getProjectActions(suite);
        }
        
        public Image getIcon(int type) {
            return ICON;
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type); // the same in the meantime
        }
        
        public void propertyChange(final PropertyChangeEvent evt) {
            ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {

                public void run() {
                    if (evt.getPropertyName().equals(ProjectInformation.PROP_NAME)) {
                        fireNameChange(null, getName());
                    } else if (evt.getPropertyName().equals(ProjectInformation.PROP_DISPLAY_NAME)) {
                        fireDisplayNameChange(null, getDisplayName());
                    }
                }
            });
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(suite, name);
        }
        
    }
}
