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

package org.netbeans.modules.websvc.utilities.ui;

import javax.swing.JPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import java.awt.BorderLayout;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.FilterNode;

/**
 * @author  rico
 */
public class SelectHandlerPanel extends JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager;
    private Node[] selectedNodes;
    private JPanel panel;
    private Project project;
    
    /** Creates a new instance of SelectHandlerPanel */
    public SelectHandlerPanel(Project project) {
        this.project = project;
        initComponents();
        manager = new ExplorerManager();
        manager.addPropertyChangeListener(
                new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                selectedNodes = manager.getSelectedNodes();
            }
        });
        populateTree();
    }
    
    public Node[] getSelectedNodes(){
        return selectedNodes;
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void populateTree(){
        LogicalViewProvider lvp = project.getLookup().lookup(LogicalViewProvider.class);
        Node projectView = lvp.createLogicalView();
        Children.Array children = new Children.Array();
        FilterNode filter = new UnmodifiableFilterNode(projectView, new SourceListViewChildren());
        children.add(new FilterNode[] {filter});
        manager.setRootContext(filter);
        
    }
    
    private void initComponents() {
        panel = new JPanel();
        setLayout(new BorderLayout());
        BorderLayout bl = new BorderLayout();
        panel.setLayout(bl);
        bl.setVgap(10);
        add(panel, BorderLayout.CENTER);
        
        BeanTreeView btv = new BeanTreeView();
        btv.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btv.getAccessibleContext().
                setAccessibleName(NbBundle.getMessage(SelectHandlerPanel.class,"LBL_Class_Tree"));
        btv.getAccessibleContext().setAccessibleDescription
                (NbBundle.getMessage(SelectHandlerPanel.class,"ACSD_SelectHandler"));
        String projectName = project.getProjectDirectory().getName();
        String classesLabel = projectName + " " +
                NbBundle.getMessage(SelectHandlerPanel.class, "LBL_PROJECT_CLASSES") + ":";
        JLabel label = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(label, classesLabel);
        label.setLabelFor(btv.getViewport().getView());
        panel.add(label, BorderLayout.NORTH);
        panel.add(btv, BorderLayout.CENTER);   //NOI18N
        panel.validate();
        validate();
    }
    
    
    class SourceListViewChildren extends Children.Keys<String> {
        public static final String KEY_SOURCES = "sourcesKey"; //NOI18N
        
        protected Node[] createNodes(String key) {
            Node n = null;
            List<Node> sourceNodes = new LinkedList<Node>();
            if (key == KEY_SOURCES) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for(int i = 0; i < groups.length; i++){
                    sourceNodes.add(PackageView.createPackageView(groups[i]));
                }
            }
            return sourceNodes.<Node>toArray(new Node[sourceNodes.size()]);
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            createNodes();
        }
        
        private void createNodes() {
            List<String> l = new ArrayList<String>();
            l.add(KEY_SOURCES);
            setKeys(l);
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.<String>emptySet());
            super.removeNotify();
        }    
    }

    class UnmodifiableFilterNode extends org.openide.nodes.FilterNode {

        UnmodifiableFilterNode(Node original, org.openide.nodes.Children children) {
            super(original, children);
        }

        @Override
        public boolean canRename() {
            return false;
        }
    }

}
