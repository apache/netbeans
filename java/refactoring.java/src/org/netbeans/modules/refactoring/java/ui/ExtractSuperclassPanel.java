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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.ui.elements.FiltersDescription;
import org.netbeans.modules.refactoring.java.ui.elements.FiltersManager;
import org.netbeans.modules.refactoring.java.ui.elements.JCheckBoxIcon;
import org.netbeans.modules.refactoring.java.ui.elements.TapPanel;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public class ExtractSuperclassPanel extends JPanel implements CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_Selected", "LBL_ExtractSC_Member", "LBL_ExtractSC_MakeAbstract"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, MemberInfo.class, Boolean.class};
    
    // refactoring this panel provides parameters for
    private final ExtractSuperclassRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = Member (Java element), 2 = Make Abstract (true/false)
    private Object[][] members = new Object[0][0];
    
    private TapPanel filtersPanel;
    private FiltersManager filtersManager;
    private final TreePathHandle[] selected;
    
    /** Creates new form ExtractSuperclassPanel
     * @param refactoring The refactoring this panel provides parameters for.
     * @param selectedMembers Members that should be pre-selected in the panel
     *      (determined by which nodes the action was invoked on - e.g. if it was
     *      invoked on a method, the method will be pre-selected to be pulled up)
     */
    public ExtractSuperclassPanel(ExtractSuperclassRefactoring refactoring, TreePathHandle[] selected, final ChangeListener parent) {
        this.refactoring = refactoring;
        this.selected = selected;
        this.tableModel = new TableModel();
        initComponents();
        setPreferredSize(new Dimension(420, 380));
        String defaultName = "NewClass"; //NOI18N
        nameText.setText(defaultName); 
        nameText.setSelectionStart(0);
        nameText.setSelectionEnd(defaultName.length());
        
        nameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent event) {
                parent.stateChanged(null);
            }
            @Override
            public void insertUpdate(DocumentEvent event) {
                parent.stateChanged(null);
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                parent.stateChanged(null);
            }
        });
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        AbstractButton[] res = new AbstractButton[2];
        
        res[0] = new JButton(null, new JCheckBoxIcon(true, new Dimension(16, 16)));
        res[0].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(true);
            }
        });
        res[0].setToolTipText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "TIP_SelectAll"));
        
        res[1] = new JButton(null, new JCheckBoxIcon(false, new Dimension(16, 16)));
        res[1].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(false);
            }
        });
        res[1].setToolTipText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "TIP_DeselectAll"));
        
        FiltersDescription desc = new FiltersDescription();
        filtersManager = FiltersDescription.createManager(desc);
//        filtersManager.hookChangeListener(this);

        JComponent buttons = filtersManager.getComponent(res);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) //NOI18N
        {
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        membersListPanel.add(filtersPanel, BorderLayout.SOUTH);
    }

    @Override
    public boolean requestFocusInWindow() {
        nameText.requestFocusInWindow();
        return true;
    }

    /** Initialization of the panel (called by the parent window).
     */
    @Override
    public void initialize() {
        // *** initialize table
        // set renderer for the second column ("Member") do display name of the feature
        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
                if (value instanceof MemberInfo) {
                    setIcon(((MemberInfo) value).getIcon());
                }
                return this;
            }
            protected String extractText(Object value) {
                String displayValue;
                if (value instanceof MemberInfo) {
                    displayValue = ((MemberInfo) value).getHtmlText();
                } else {
                    displayValue = String.valueOf(value);
                }
                return displayValue;
            }
        });
        // send renderer for the third column ("Make Abstract") to make the checkbox:
        // 1. hidden for elements that are not methods
        // 2. be disabled for static methods
        membersTable.getColumnModel().getColumn(2).setCellRenderer(new UIUtilities.BooleanTableCellRenderer(membersTable));
        // set background color of the scroll pane to be the same as the background
        // of the table
        scrollPane.setBackground(membersTable.getBackground());
        scrollPane.getViewport().setBackground(membersTable.getBackground());
        // set default row height
        membersTable.setRowHeight(18);
        // set grid color to be consistent with other netbeans tables
        if (UIManager.getColor("control") != null) { // NOI18N
            membersTable.setGridColor(UIManager.getColor("control")); // NOI18N
        }
        // compute and set the preferred width for the first and the third column
        UIUtilities.initColumnWidth(membersTable, 0, Boolean.TRUE, 4);
        UIUtilities.initColumnWidth(membersTable, 2, Boolean.TRUE, 4);
    }
    
        
    private void selectAll(boolean select) {
        for (Object[] row : members) {
            row[0] = select? Boolean.TRUE : Boolean.FALSE;
        }
        
        tableModel.fireTableDataChanged();
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    public String getSuperClassName() {
        return nameText.getText();
    }
    
    /** Getter used by the refactoring UI to get members to be pulled up.
     * @return Descriptors of members to be pulled up.
     */
    public MemberInfo[] getMembers() {
        List<MemberInfo> list = new ArrayList<MemberInfo>();
        // go through all rows of a table and collect selected members
        for (int i = 0; i < members.length; i++) {
            // if the current row is selected, create MemberInfo for it and
            // add it to the list of selected members
            if (members[i][0].equals(Boolean.TRUE)) {
                MemberInfo member = (MemberInfo) members[i][1];
                member.setMakeAbstract(members[i][2] != null && ((Boolean) members[i][2]));
                list.add(member);
            }
        }
        // return the array of selected members
        return (MemberInfo[]) list.toArray(new MemberInfo[0]);
    }
    
    // --- GENERATED CODE ------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        membersListPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.BorderLayout(0, 10));

        namePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        namePanel.setLayout(new java.awt.BorderLayout(12, 0));

        nameLabel.setLabelFor(nameText);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "LBL_ExtractSC_Name")); // NOI18N
        namePanel.add(nameLabel, java.awt.BorderLayout.WEST);
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ExtractSuperclassPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        namePanel.add(nameText, java.awt.BorderLayout.CENTER);
        nameText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ACSD_SupeclassName")); // NOI18N
        nameText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ACSD_SuperclassNameDescription")); // NOI18N

        add(namePanel, java.awt.BorderLayout.NORTH);

        membersListPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "LBL_ExtractSCLabel"))); // NOI18N
        membersListPanel.setLayout(new java.awt.BorderLayout());

        scrollPane.setBorder(null);

        membersTable.setModel(tableModel);
        membersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        scrollPane.setViewportView(membersTable);
        membersTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ACSD_MembersToExtract")); // NOI18N
        membersTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ACSD_MembersToExtractDescription")); // NOI18N

        membersListPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        add(membersListPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel membersListPanel;
    private javax.swing.JTable membersTable;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameText;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
    // --- MODELS --------------------------------------------------------------
    
    /** Model for the members table.
     */
    private class TableModel extends AbstractTableModel {
        TableModel() {
            initialize();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return UIUtilities.getColumnName(NbBundle.getMessage(ExtractSuperclassPanel.class, COLUMN_NAMES[column]));
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
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                // column 2 is editable only in case of non-static methods
                // if the target type is not an interface
                if (members[rowIndex][2] == null) {
                    return false;
                }
                MemberInfo element = (MemberInfo) members[rowIndex][1];
                return !(element.getModifiers().contains(Modifier.STATIC) || element.getModifiers().contains(Modifier.ABSTRACT));
            } else {
                // column 0 is always editable, column 1 is never editable
                return columnIndex == 0;
            }
        }

        private void initialize() {
            final TreePathHandle sourceType = refactoring.getSourceType();
            if (sourceType == null) {
                return;
            }
            
            FileObject fo = sourceType.getFileObject();
            JavaSource js = JavaSource.forFileObject(fo);
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                        public void cancel() {
                        }

                    @Override
                        public void run(CompilationController javac) throws Exception {
                            javac.toPhase(JavaSource.Phase.RESOLVED);
                            initializeInTransaction(javac, sourceType);
                        }

                    }, true);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        private void initializeInTransaction(CompilationController javac, TreePathHandle sourceType) {
            TreePath sourceTreePath = sourceType.resolve(javac);
            ClassTree sourceTree = (ClassTree) sourceTreePath.getLeaf();
            List<MemberInfo<?>> result = new ArrayList<MemberInfo<?>>();
            
            for (Tree implTree : sourceTree.getImplementsClause()) {
                TreePath implPath = javac.getTrees().getPath(javac.getCompilationUnit(), implTree);
                TypeMirror implMirror = javac.getTrees().getTypeMirror(implPath);
                result.add(MemberInfo.create(implMirror, implTree,javac));
            }
            
            for (Tree member : sourceTree.getMembers()) {
                TreePath memberTreePath = javac.getTrees().getPath(javac.getCompilationUnit(), member);
                if (javac.getTreeUtilities().isSynthetic(memberTreePath)) {
                    continue;
                }
                
                Element memberElm = javac.getTrees().getElement(memberTreePath);
                if (memberElm == null) {
                    continue;
                }
                
//                if (memberElm.getModifiers().contains(Modifier.PRIVATE)) {
//                    //ignore private members.
//                    continue;
//                }
                if (memberElm.getKind() == ElementKind.FIELD) {
                    result.add(MemberInfo.create(memberElm, javac));
                } else if (memberElm.getKind() == ElementKind.METHOD) {
                    result.add(MemberInfo.create(memberElm,javac));
                }
            }
            
            // the members are collected
            // now, create a tree map (to sort them) and create the table data
            result.sort(new Comparator<MemberInfo<?>>() {
                @Override
                public int compare(MemberInfo<?> mi1, MemberInfo<?> mi2) {
                    int result = mi1.getGroup().compareTo(mi2.getGroup());
                    
                    if (result == 0) {
                        result = mi1.getName().compareTo(mi2.getName());
                    }
                    
                    return result;
                }
            });
            members = new Object[result.size()][3];
            for (int i = 0; i < members.length; i++) {
                MemberInfo<?> member = result.get(i);
                members[i][0] = Boolean.FALSE;
                members[i][1] = member;
                
                for (TreePathHandle treePathHandle : selected) {
                    ElementHandle selectedElement = treePathHandle.getElementHandle();
                    if(selectedElement != null && member.getElementHandle() instanceof ElementHandle &&
                            selectedElement.signatureEquals((ElementHandle)member.getElementHandle())) {
                        members[i][0] = Boolean.TRUE;
                    }
                }
                
                if (member.getGroup() == MemberInfo.Group.METHOD) {
                    members[i][2] = member.isMakeAbstract();
                } else {
                    members[i][2] = null;
                }
            }
            // fire event to repaint the table
            this.fireTableDataChanged();
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
