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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository.dependency.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Anuradha G (theanuradha-at-netbeans.org)
 */
public class AddDependencyUI extends javax.swing.JPanel implements ExplorerManager.Provider {

    private ExplorerManager explorerManager = new ExplorerManager();
    private final JButton addButton;

    public AddDependencyUI(String libDef) {
        initComponents();
        lblDescription.setText(NbBundle.getMessage(AddDependencyUI.class, "LBL_Description", libDef));//NOI18N
        addButton = new JButton(NbBundle.getMessage(AddDependencyUI.class, "BTN_Add"));//NOI18N
        addButton.setEnabled(false);
        Node openProjectsNode = new AbstractNode(Children.create(new ChildFactory<Project>() {
            protected @Override boolean createKeys(List<Project> toPopulate) {
                for (Project project : OpenProjects.getDefault().getOpenProjects()) {
                    if (project.getLookup().lookup(NbMavenProject.class) != null) {
                        toPopulate.add(project);
                    }
                }
                return true;
            }
            protected @Override Node createNodeForKey(Project key) {
                return new OpenProjectNode(key);
            }
        }, true));
        explorerManager.setRootContext(openProjectsNode);
        BeanTreeView beanTreeView = (BeanTreeView) jScrollPane1;
        beanTreeView.setPopupAllowed(false);
        beanTreeView.setRootVisible(false);
        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("selectedNodes")) {//NOI18N
                    Node[] selectedNodes = explorerManager.getSelectedNodes();
                    boolean enable=false;
                    for (Node node : selectedNodes) {
                        if (node instanceof OpenProjectNode) {
                          enable=true;
                          break;

                        }
                    }
                    addButton.setEnabled(enable);
                   
                }
            }
        });
    }

    public JButton getAddButton() {
        return addButton;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();
        lblDescription = new javax.swing.JLabel();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, javax.swing.UIManager.getDefaults().getColor("CheckBoxMenuItem.selectionBackground")));

        lblDescription.setText(org.openide.util.NbBundle.getMessage(AddDependencyUI.class, "LBL_Description")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(lblDescription, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDescription;
    // End of variables declaration//GEN-END:variables

    public @Override ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public List<Project> getSelectedMavenProjects() {
        List<Project> mavenProjects = new ArrayList<Project>();
        Node[] selectedNodes = explorerManager.getSelectedNodes();
        for (Node node : selectedNodes) {
            mavenProjects.add(node.getLookup().lookup(Project.class));
        }

        return mavenProjects;
    }

    private static class OpenProjectNode extends AbstractNode {

        private ProjectInformation pi;

        public OpenProjectNode(Project project) {
            super(Children.LEAF, Lookups.singleton(project));
            pi = ProjectUtils.getInformation(project);
        }

        @Override
        public Image getIcon(int arg0) {
            return ImageUtilities.icon2Image(pi.getIcon());
        }

        @Override
        public String getDisplayName() {
            return pi.getDisplayName();
        }
    }
}
