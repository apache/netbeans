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

/*
 * SelectProjectPanel.java
 *
 * Created on Sep 4, 2009, 10:21:42 AM
 */

package org.netbeans.modules.maven.j2ee.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * GUI for selecting a project where to store server settings.
 *
 * @author dafe
 */
public class SelectProjectPanel extends javax.swing.JPanel {
    private final Project project;
    private final OpenListPanel olp;

    /** Creates new form SelectProjectPanel */
    public SelectProjectPanel(Project project) {
        this.project = project;
        initComponents();
        olp = new OpenListPanel(project);
        listPanel.add(olp, BorderLayout.CENTER);
    }

    void attachDD(DialogDescriptor dd) {
        olp.attachDD(dd);
    }

    private static Border getNbScrollPaneBorder () {
        Border b = UIManager.getBorder("Nb.ScrollPane.border");
        if (b == null) {
            Color c = UIManager.getColor("controlShadow");
            b = new LineBorder(c != null ? c : Color.GRAY);
        }
        return b;
    }

    Project getSelectedProject() {
        return olp.getSelectedProject();
    }

    private static class OpenListPanel extends JPanel implements ExplorerManager.Provider,
            PropertyChangeListener, Runnable, ActionListener {

        private final ListView lv;
        private final ExplorerManager manager;
        private final Project curProject;
        private DialogDescriptor dd;
        private NotificationLineSupport projNls;
        private Project selProj;

        public OpenListPanel(Project curProject) {
            this.curProject = curProject;
            lv = new ListView();
            lv.setDefaultProcessor(this);
            lv.setPopupAllowed(false);
            lv.setTraversalAllowed(false);
            lv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
            setLayout(new BorderLayout());
            add(lv, BorderLayout.CENTER);

            RequestProcessor.getDefault().post(this);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Node[] selNodes = manager.getSelectedNodes();
            if (selNodes.length == 1) {
                Project prj = selNodes[0].getLookup().lookup(Project.class);
                if (prj != null) {
                    selProj = prj;
                    dd.setValid(true);
                    projNls.clearMessages();
                    return;
                }
            }
            dd.setValid(false);
            projNls.setErrorMessage(NbBundle.getMessage(
                            SelectAppServerPanel.class, "ERR_NoSelection"));
        }

        public Project getSelectedProject () {
            return selProj;
        }

        /** Loads dependencies outside EQ thread, updates tab state in EQ */
        @Override
        public void run() {
            Project[] prjs = OpenProjects.getDefault().getOpenProjects();
            final List<Node> toRet = new ArrayList<>();
            for (Project p : prjs) {
                if (p == curProject) {
                    continue;
                }
                NbMavenProject mav = p.getLookup().lookup(NbMavenProject.class);
                if (mav != null) {
                    LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    toRet.add(lvp.createLogicalView());
                }
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Children.Array ch = new Children.Array();
                    ch.add(toRet.toArray(new Node[0]));
                    Node root = new AbstractNode(ch);
                    getExplorerManager().setRootContext(root);
                    if (ch.getNodesCount() > 0) {
                        Node first = ch.getNodeAt(0);
                        try {
                            getExplorerManager().setSelectedNodes(new Node[]{first});
                        } catch (PropertyVetoException ex) {
                            // what to do?
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // empty impl, disables default action
        }

        private void attachDD(DialogDescriptor dd) {
            this.dd = dd;
            projNls = dd.createNotificationLineSupport();
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listLabel = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();

        listLabel.setText(org.openide.util.NbBundle.getMessage(SelectProjectPanel.class, "SelectProjectPanel.listLabel.text")); // NOI18N

        listPanel.setBorder(getNbScrollPaneBorder());
        listPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(listPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .addComponent(listLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel listLabel;
    private javax.swing.JPanel listPanel;
    // End of variables declaration//GEN-END:variables

}
