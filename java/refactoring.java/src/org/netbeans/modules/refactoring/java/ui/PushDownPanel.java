/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Flaska
 */
public class PushDownPanel extends JPanel implements CustomRefactoringPanel {
    
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_PullUp_Selected", "LBL_PullUp_Member", "LBL_PushDown_KeepAbstract"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, MemberInfo.class, Boolean.class};
    
    // refactoring this panel provides parameters for
    private final PushDownRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // pre-selected members (comes from the refactoring action - the elements
    // that should be pre-selected in the table of members)
    private final Set selectedMembers;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = Member (Java element), 2 = Make Abstract (true/false)
    private Object[][] members = new Object[0][0];
    
    private ChangeListener parent;
    
    private boolean initialized = false;
    
    
    /** Creates new form PushDownPanel
     * @param refactoring The refactoring this panel provides parameters for.
     * @param selectedMembers Members that should be pre-selected in the panel
     *      (determined by which nodes the action was invoked on - e.g. if it was
     *      invoked on a method, the method will be pre-selected to be pulled up)
     */
    public PushDownPanel(PushDownRefactoring refactoring, Set selectedMembers, ChangeListener parent) {
        this.refactoring = refactoring;
        this.tableModel = new TableModel();
        this.selectedMembers = selectedMembers;
        initComponents();
        setPreferredSize(new Dimension(420, 380));
        this.parent = parent;
    }
    
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        final TreePathHandle handle = refactoring.getSourceType();
        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        try {
            source.runUserActionTask(new InitialisationTask(handle), true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        parent.stateChanged(null);
        initialized = true;
    }
    
    public MemberInfo[] getMembers() {
        List list = new ArrayList();
        // go through all rows of a table and collect selected members
        for (int i = 0; i < members.length; i++) {
            // if the current row is selected, create MemberInfo for it and
            // add it to the list of selected members
            if (members[i][0].equals(Boolean.TRUE)) {
                Object element = members[i][1];
                MemberInfo member;
                member = (MemberInfo) element;
                if (members[i][2]!=null) {
                    member.setMakeAbstract((Boolean)members[i][2]);
                }
                list.add(member);
            }
        }
        // return the array of selected members
        return (MemberInfo[]) list.toArray(new MemberInfo[0]);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        membersScrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();
        chooseLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        membersScrollPane.setToolTipText("");

        membersTable.setModel(tableModel);
        membersScrollPane.setViewportView(membersTable);
        membersTable.getAccessibleContext().setAccessibleName(null);
        membersTable.getAccessibleContext().setAccessibleDescription(null);

        add(membersScrollPane, java.awt.BorderLayout.CENTER);

        chooseLabel.setLabelFor(membersTable);
        org.openide.awt.Mnemonics.setLocalizedText(chooseLabel, org.openide.util.NbBundle.getMessage(PushDownPanel.class, "LBL_PushDownLabel")); // NOI18N
        add(chooseLabel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    // </editor-fold>                        
//GEN-FIRST:event_jComboBox1ActionPerformed
//GEN-LAST:event_jComboBox1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel chooseLabel;
    private javax.swing.JScrollPane membersScrollPane;
    private javax.swing.JTable membersTable;
    // End of variables declaration//GEN-END:variables
    private class TableModel extends AbstractTableModel {
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return UIUtilities.getColumnName(NbBundle.getMessage(PushDownPanel.class, COLUMN_NAMES[column]));
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return COLUMN_CLASSES[columnIndex];
        }

        @Override
        public int getRowCount() {
            return members.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return members[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            members[rowIndex][columnIndex] = value;
            parent.stateChanged(null);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                // column 2 is editable only in case of non-static methods
                if (members[rowIndex][2] == null) {
                    return false;
                }
                Object element = members[rowIndex][1];
               
                return !(((MemberInfo) element).getModifiers().contains(Modifier.STATIC));
            } else {
                // column 0 is always editable, column 1 is never editable
                return columnIndex == 0;
            }
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private class InitialisationTask implements CancellableTask<CompilationController> {

        private final TreePathHandle handle;

        public InitialisationTask(TreePathHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            List<MemberInfo<? extends ElementHandle<? extends Element>>> l = new ArrayList<>();
            TypeElement sourceTypeElement = (TypeElement) handle.resolveElement(controller);
            for (TypeMirror tm:sourceTypeElement.getInterfaces()) {
                l.add(MemberInfo.create(RefactoringUtils.typeToElement(tm, controller), controller, MemberInfo.Group.IMPLEMENTS));
            }
            for (Element m: sourceTypeElement.getEnclosedElements()) {
                if (m.getKind() == ElementKind.CONSTRUCTOR || m.getKind() == ElementKind.STATIC_INIT || m.getKind() == ElementKind.INSTANCE_INIT) {
                    continue;
                }
                if (m instanceof TypeElement && controller.getTypes().isSubtype(m.asType(), sourceTypeElement.asType())) {
                    continue;
                }
                l.add(MemberInfo.create(m,controller));
            }
            
            Object[][] allMembers = new Object[l.size()][3];
            int i = 0;
            for (Iterator<MemberInfo<? extends ElementHandle<? extends Element>>> it = l.iterator(); it.hasNext(); ) {
                MemberInfo<? extends ElementHandle<? extends Element>> o = it.next();
                allMembers[i][0] = selectedMembers.contains(o) ? Boolean.TRUE : Boolean.FALSE;
                allMembers[i][1] = o;
                allMembers[i][2] = o.getElementHandle().getKind()==ElementKind.METHOD? Boolean.FALSE : null;
                i++;
            }
            members = new Object[i][3];
            if (i > 0) {
                System.arraycopy(allMembers, 0, members, 0, i);
            }
            
            // set renderer for the second column ("Member") do display name of the feature
            membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new UIUtilities.JavaElementTableCellRenderer() {
                // override the extractText method to add "implements " prefix to the text
                // in case the value is instance of MultipartId (i.e. it represents an interface
                // name from implements clause)
                @Override
                protected String extractText(Object value) {
                    String displayValue = super.extractText(value);
                    
                    if (value instanceof MemberInfo && ((MemberInfo) value).getGroup()==MemberInfo.Group.IMPLEMENTS) {
                        displayValue = "implements " + displayValue; // NOI18N
                    }
                    return displayValue;
                }
            });
            // send renderer for the third column ("Make Abstract") to make the checkbox:
            // 1. hidden for elements that are not methods
            // 2. be disabled for static methods
            // 3. be disabled and checked for methods if the target type is an interface
            membersTable.getColumnModel().getColumn(2).setCellRenderer(new UIUtilities.BooleanTableCellRenderer(membersTable) {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    // make the checkbox checked (even if "Make Abstract" is not set)
                    // for non-static methods if the target type is an interface
                    MemberInfo<ElementHandle> object = (MemberInfo) table.getModel().getValueAt(row,
                            1);
                    // the super method automatically makes sure the checkbox is not visible if the
                    // "Make Abstract" value is null (which holds for non-methods)
                    // and that the checkbox is disabled if the cell is not editable (which holds for
                    // static methods all the time and for all methods in case the target type is an interface
                    // - see the table model)
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            });
            // set background color of the scroll pane to be the same as the background
            // of the table
            membersScrollPane.setBackground(membersTable.getBackground());
            membersScrollPane.getViewport().setBackground(membersTable.getBackground());
            // set default row height
            membersTable.setRowHeight(18);
            // set grid color to be consistent with other netbeans tables
            if (UIManager.getColor("control") != null) { // NOI18N
                membersTable.setGridColor(UIManager.getColor("control")); // NOI18N
            }
            // compute and set the preferred width for the first and the third column
            UIUtilities.initColumnWidth(membersTable, 0, Boolean.TRUE, 4);
            UIUtilities.initColumnWidth(membersTable, 2, Boolean.TRUE, 4);}
    }
}
