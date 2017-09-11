/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithBuilderRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class ReplaceConstructorWithBuilderPanel extends javax.swing.JPanel implements CustomRefactoringPanel {

    private static final String[] columnNames = {
        getString("LBL_BuilderParameter"), // NOI18N
        getString("LBL_BuilderSetterName"), // NOI18N
        getString("LBL_BuilderDefaultValue"), // NOI18N
        getString("LBL_BuilderOptionalSetter") // NOI18N
    };
    private static final boolean[] columnCanEdit = new boolean[]{
        false, true, true, true
    };
    private static final Class[] columnTypes = new Class[]{
        String.class, String.class, String.class, Boolean.class
    };
    private List<String> parameterTypes;
    private List<Boolean> parameterTypeVars;

    public ReplaceConstructorWithBuilderPanel(final @NonNull ChangeListener parent, String initialFQN,
            List<String> paramaterNames, List<String> parameterTypes, List<Boolean> parameterTypeVars) {
        initComponents();
        this.parameterTypes = parameterTypes;
        nameField.setText(initialFQN);
        nameField.setSelectionStart(0);
        nameField.setSelectionEnd(nameField.getText().length());

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        DefaultTableModel model = (DefaultTableModel) paramTable.getModel();
        Iterator<String> typesIt = parameterTypes.iterator();
        for (String name : paramaterNames) {
            model.addRow(new Object[]{typesIt.next() + " " + name, "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1), null, false}); //NOI18N
        }
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }
        });
        this.parameterTypeVars = parameterTypeVars;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        builderName = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        paramScrollPane = new javax.swing.JScrollPane();
        paramTable = new JTable() {

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 2 || column == 3) {
                    return !parameterTypeVars.get(row);
                }
                return super.isCellEditable(row, column);
            }
        };

        org.openide.awt.Mnemonics.setLocalizedText(builderName, org.openide.util.NbBundle.getMessage(ReplaceConstructorWithBuilderPanel.class, "ReplaceConstructorWithBuilder.jLabel1.text")); // NOI18N

        nameField.setColumns(15);

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{}, columnNames) {
            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnCanEdit[columnIndex];
            }
        });
        paramScrollPane.setViewportView(paramTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(builderName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
            .addComponent(paramScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(paramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(builderName)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel builderName;
    private javax.swing.JTextField nameField;
    private javax.swing.JScrollPane paramScrollPane;
    private javax.swing.JTable paramTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void initialize() {
    }

    public String getBuilderName() {
        return nameField.getText();
    }

    @Override
    public boolean requestFocusInWindow() {
        nameField.requestFocusInWindow();
        return true;
    }

    public List<ReplaceConstructorWithBuilderRefactoring.Setter> getSetters() {
        List<ReplaceConstructorWithBuilderRefactoring.Setter> result = new ArrayList();
        int size = parameterTypes.size();
        for (int i = 0; i < size; i++) {
            final String name = (String) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 0);
            result.add(new ReplaceConstructorWithBuilderRefactoring.Setter(
                    (String) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 1),
                    parameterTypes.get(i),
                    (String) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 2),
                    name.substring(name.lastIndexOf(' ')).trim(),
                    (Boolean) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 3)));
        }
        return result;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ReplaceConstructorWithBuilderPanel.class, key);
    }
}
