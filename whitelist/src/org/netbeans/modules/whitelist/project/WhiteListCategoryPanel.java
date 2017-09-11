/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * WhiteListCategoryPanel.java
 *
 * Created on 22/07/2011, 7:37:04 AM
 */
package org.netbeans.modules.whitelist.project;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation.UserSelectable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author david
 */
public class WhiteListCategoryPanel extends javax.swing.JPanel implements ActionListener {


    private Project p;

    /** Creates new form WhiteListCategoryPanel */
    public WhiteListCategoryPanel(Project p) {
        this.p = p;
        initComponents();
        WhiteListsModel model = new WhiteListsModel(getTableContent());
        jTable1.setModel(model);
        jTable1.getTableHeader().setVisible(false);
        jTable1.getTableHeader().setPreferredSize(new Dimension(0, 0));
        jTable1.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        jTable1.getColumnModel().getColumn(0).setMaxWidth(25);
        jTable1.getColumnModel().getColumn(0).setMinWidth(25);
    }

    private List<Desc> getTableContent() {
        List<Desc> l = new ArrayList<Desc>();
        for (WhiteListQueryImplementation.UserSelectable impl : WhiteListLookupProvider.getUserSelectableWhiteLists()) {
            l.add(new Desc(impl, WhiteListLookupProvider.isWhiteListEnabledInProject(p, impl.getId())));
        }
        return l;
    }
    
    public static ProjectCustomizer.CompositeCategoryProvider createWhiteListCategoryProvider(Map attrs) {
        return new Factory(Boolean.TRUE.equals((Boolean)attrs.get("show"))); //NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Desc d : ((WhiteListsModel)jTable1.getModel()).whitelists) {
            WhiteListLookupProvider.enableWhiteListInProject(p, d.w.getId(), d.active);
        }
    }

    public static class Factory implements ProjectCustomizer.CompositeCategoryProvider {
 
        private static final String CATEGORY_WHITELIST = "WhiteList"; // NOI18N

        private final boolean alwaysShowWhiteListPanel;
        
        public Factory(boolean showWhiteListPanel) {
            alwaysShowWhiteListPanel = showWhiteListPanel;
        }

        public ProjectCustomizer.Category createCategory(Lookup context) {
            Project p = context.lookup(Project.class);
            if (p == null) {
                return null;
            }
            if (WhiteListLookupProvider.getUserSelectableWhiteLists().isEmpty()) {
                return null;
            }
            if (!WhiteListLookupProvider.isWhiteListPanelEnabled(p) && !alwaysShowWhiteListPanel) {
                return null;
            }
            return ProjectCustomizer.Category.create(
                    CATEGORY_WHITELIST,
                    NbBundle.getMessage(WhiteListCategoryPanel.class, "LBL_CategoryWhitelist"), //NOI18N
                    null);
        }

        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            Project p = context.lookup(Project.class);
            assert p != null;
            WhiteListCategoryPanel customizerPanel = new WhiteListCategoryPanel(p);
            category.setStoreListener(customizerPanel);
            return customizerPanel;
        }
    } // End of Factory class

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(WhiteListCategoryPanel.class, "WhiteListCategoryPanel.jLabel1.text")); // NOI18N

        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(283, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    private static class Desc {
        private WhiteListQueryImplementation.UserSelectable w;
        private boolean active;

        public Desc(UserSelectable w, boolean active) {
            this.w = w;
            this.active = active;
        }
    }
    
    private static class WhiteListsModel implements TableModel {

        private List<Desc> whitelists;

        public WhiteListsModel(List<Desc> whitelists) {
            assert whitelists.size() > 0;
            this.whitelists = new ArrayList(whitelists);
        }
        
        @Override
        public int getRowCount() {
            return whitelists.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        private String[] header = new String[]{"Enabled", "Whitelist"};
        private Class[] headerClass = new Class[]{Boolean.class, String.class};
        
        @Override
        public String getColumnName(int columnIndex) {
            return header[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return headerClass[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Desc srd = whitelists.get(rowIndex);
            if (columnIndex == 0) {
                return Boolean.valueOf(srd.active);
            } else {
                return srd.w.getDisplayName();
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (aValue instanceof Boolean) {
                whitelists.get(rowIndex).active = ((Boolean)aValue).booleanValue();
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
        }
        
    }
}
