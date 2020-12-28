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

package org.netbeans.modules.python.project2.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Source roots view
 * @author Tomas Zezula
 */
@NbBundle.Messages({"LBL_Properties_Action=Properties"})
public final class SourceNodeFactory implements NodeFactory {
    public SourceNodeFactory() {
    }
    
    @Override
    public NodeList createNodes(Project p) {
        PythonProject2 project = (PythonProject2)p.getLookup().lookup(PythonProject2.class);
        assert project != null;
        return new SourcesNodeList(project);
    }
    
    private static class SourcesNodeList implements NodeList<SourceGroupKey>, ChangeListener {
        
        private PythonProject2 project;
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        
        public SourcesNodeList(PythonProject2 proj) {
            project = proj;
        }
        
        @Override
        public List<SourceGroupKey> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.EMPTY_LIST;
            }
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups(PythonProject2.SOURCES_TYPE_PYTHON);
            
            List result =  new ArrayList(groups.length);
            for (SourceGroup group : groups) {
                result.add(new SourceGroupKey(group));
            }
            return result;
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
        public Node node(SourceGroupKey key) {
            return new PackageViewFilterNode(key.group, project);
        }
        
        @Override
        public void addNotify() {
            getSources().addChangeListener(this);
        }
        
        @Override
        public void removeNotify() {
            getSources().removeChangeListener(this);
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }
        
    }
    
    private static class SourceGroupKey {
        
        public final SourceGroup group;
        public final FileObject fileObject;
        
        SourceGroupKey(SourceGroup group) {
            this.group = group;
            this.fileObject = group.getRootFolder();
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            String disp = this.group.getDisplayName();
            hash = 79 * hash + (fileObject != null ? fileObject.hashCode() : 0);
            hash = 79 * hash + (disp != null ? disp.hashCode() : 0);
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;
                
                if (fileObject != otherKey.fileObject && (fileObject == null || !fileObject.equals(otherKey.fileObject))) {
                    return false;
                }
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                boolean oneNull = thisDisplayName == null;
                boolean twoNull = otherDisplayName == null;
                return !(oneNull != twoNull || !thisDisplayName.equals(otherDisplayName));
            }
        }

        
    }
    
    /** Yet another cool filter node just to add properties action
     */
    private static class PackageViewFilterNode extends FilterNode {
        
        private String nodeName;
        private Project project;
        
        Action[] actions;
        
        public PackageViewFilterNode(SourceGroup sourceGroup, Project project) {
            super(PackageView.createPackageView(sourceGroup));
            this.project = project;
            this.nodeName = "Sources";  //NOI18N
        }
        
        @Override
        public Action[] getActions(boolean context) {
            if (!context) {
                if (actions == null) {
                    Action superActions[] = super.getActions(context);
                    actions = new Action[superActions.length + 2];
                    System.arraycopy(superActions, 0, actions, 0, superActions.length);
                    actions[superActions.length] = null;
                    actions[superActions.length + 1] = new PreselectPropertiesAction(project, nodeName);
                }
                return actions;
            } else {
                return super.getActions(context);
            }
        }
        
    }
    
    
    /** The special properties action
     */
    static class PreselectPropertiesAction extends AbstractAction {
        
        private final Project project;
        private final String nodeName;
        private final String panelName;
        
        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }
        
        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(SourceNodeFactory.class, "LBL_Properties_Action"));
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
//todo: Add customizer            
//            CustomizerProviderImpl cp = (CustomizerProviderImpl) project.getLookup().lookup(CustomizerProviderImpl.class);
//            if (cp != null) {
//                cp.showCustomizer(nodeName, panelName);
//            }
            
        }
    }
    
}
