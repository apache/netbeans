/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.ide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class PatchContextChooser extends javax.swing.JPanel implements ExplorerManager.Provider {
    private ExplorerManager manager;

    public PatchContextChooser() {
        initComponents();
        projectsView.setPopupAllowed(false);
        projectsView.setRootVisible(false);
        projectsView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        manager = new ExplorerManager();

        List<Node> nodes = new LinkedList<Node>();
        for (Project project: OpenProjects.getDefault().getOpenProjects()) {
            LogicalViewProvider view = project.getLookup().lookup(LogicalViewProvider.class);
            if (view != null) {
                Node node = view.createLogicalView();
                nodes.add(node);
            }
        }
        Collections.sort(nodes, new Comparator<Node>() {
            @Override public int compare(Node n1, Node n2) {
                if(n1 == null && n2 == null) {
                    return 0;
                }
                if(n1 == null) {
                    return -1;
                }
                if(n2 == null) {
                    return 1;
                }
                return n1.getDisplayName().compareTo(n2.getDisplayName());
            }
        });
        Children.Array children = new Children.Array();
        children.add(nodes.toArray(new Node[nodes.size()]));
        AbstractNode root = new AbstractNode(children);
        manager.setRootContext(root);
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node[] nodes = manager.getSelectedNodes();
                    FileObject fob = null;
                    if (nodes.length != 0) {
                        Lookup lookup = nodes[0].getLookup();
                        DataObject dob = lookup.lookup(DataObject.class);
                        if (dob != null) {
                            fob = dob.getPrimaryFile();
                        } else {
                            fob = lookup.lookup(FileObject.class);
                        }
                        if (fob == null) {
                            Project project = lookup.lookup(Project.class);
                            if (project != null) {
                                fob = project.getProjectDirectory();
                            }
                        }
                    }
                    locationField.setText((fob==null) ? "" : FileUtil.getFileDisplayName(fob)); // NOI18N
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectsView = new org.openide.explorer.view.BeanTreeView();
        titleLabel = new javax.swing.JLabel();
        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        locationButton = new javax.swing.JButton();

        titleLabel.setText(org.openide.util.NbBundle.getMessage(PatchContextChooser.class, "PatchContextChooser.titleLabel.text")); // NOI18N

        locationLabel.setText(org.openide.util.NbBundle.getMessage(PatchContextChooser.class, "PatchContextChooser.locationLabel.text")); // NOI18N

        locationButton.setText(org.openide.util.NbBundle.getMessage(PatchContextChooser.class, "PatchContextChooser.locationButton.text")); // NOI18N
        locationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectsView, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addComponent(titleLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(locationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(locationField, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(locationButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectsView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void locationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationButtonActionPerformed
        FileChooserBuilder builder = new FileChooserBuilder(PatchContextChooser.class);
        File file = builder.setApproveText(NbBundle.getMessage(PatchContextChooser.class, "LBL_Select")).showOpenDialog();
        if (file != null) {
            FileObject fob = FileUtil.toFileObject(file);
            locationField.setText(FileUtil.getFileDisplayName(fob));
        }
    }//GEN-LAST:event_locationButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton locationButton;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private org.openide.explorer.view.BeanTreeView projectsView;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public File getSelectedFile() {
        File file = null;
        String fileName = locationField.getText().trim();
        if (!"".equals(fileName)) { // NOI18N
            file = new File(fileName);
            if (!file.exists()) {
                file = null;
            }
        }
        return file;
    }

}
