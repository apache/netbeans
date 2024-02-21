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
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
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
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.ui.elements.FiltersDescription;
import org.netbeans.modules.refactoring.java.ui.elements.FiltersManager;
import org.netbeans.modules.refactoring.java.ui.elements.JCheckBoxIcon;
import org.netbeans.modules.refactoring.java.ui.elements.TapPanel;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula, Jan Becicka, Jan Pokorsky
 */
public final class ExtractInterfacePanel extends JPanel implements CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_Selected", "LBL_ExtractInterface_Member"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, TreePathHandle.class};
    
    // refactoring this panel provides parameters for
    private final ExtractInterfaceRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = ExtractInterfaceInfo (Java element)
    private Object[][] members = new Object[0][0];
    
    private TapPanel filtersPanel;
    private FiltersManager filtersManager;
    
    /** Creates new form ExtractInterfacePanel
     * @param refactoring The refactoring this panel provides parameters for.
     */
    public ExtractInterfacePanel(ExtractInterfaceRefactoring refactoring, final ChangeListener parent) {
        this.refactoring = refactoring;
        this.tableModel = new TableModel();
        initComponents();
        setPreferredSize(new Dimension(420, 380));
        String defaultName = "NewInterface"; //NOI18N
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
        memberspanel.add(filtersPanel, BorderLayout.SOUTH);
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
        // set renderer for the second column ("Member") to display name of the feature
        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
                if (value instanceof ExtractInterfaceInfo) {
                    setIcon(((ExtractInterfaceInfo) value).icon);
                }
                return this;
            }
            protected String extractText(Object value) {
                String displayValue;
                if (value instanceof ExtractInterfaceInfo) {
                    displayValue = ((ExtractInterfaceInfo) value).htmlText;
                } else {
                    displayValue = String.valueOf(value);
                }
                return displayValue;
            }
        });
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
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    /** stores data collected via the panel.
     */
    public void storeSettings() {
        List<ElementHandle<VariableElement>> fields = new ArrayList<ElementHandle<VariableElement>>();
        List<ElementHandle<ExecutableElement>> methods = new ArrayList<ElementHandle<ExecutableElement>>();
        List<TypeMirrorHandle<TypeMirror>> implementz = new ArrayList<TypeMirrorHandle<TypeMirror>>();
        
        // go through all rows of a table and collect selected members
        for (int i = 0; i < members.length; i++) {
            if (members[i][0].equals(Boolean.TRUE)) {
                ExtractInterfaceInfo info = (ExtractInterfaceInfo) members[i][1];
                switch(info.group) {
                case FIELD: fields.add((ElementHandle<VariableElement>) info.handle); break;
                case METHOD: methods.add((ElementHandle<ExecutableElement>) info.handle); break;
                case IMPLEMENTS: implementz.add((TypeMirrorHandle<TypeMirror>) info.handle); break;
                }
            }
        }
        
        refactoring.setFields(fields);
        refactoring.setImplements(implementz);
        refactoring.setMethods(methods);
        refactoring.setInterfaceName(nameText.getText());
    }
    
    
    private void selectAll(boolean select) {
        for (Object[] row : members) {
            row[0] = select? Boolean.TRUE : Boolean.FALSE;
        }
        
        tableModel.fireTableDataChanged();
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
        memberspanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.BorderLayout(0, 10));

        namePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        namePanel.setLayout(new java.awt.BorderLayout(12, 0));

        nameLabel.setLabelFor(nameText);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "LBL_ExtractInterface_Name")); // NOI18N
        namePanel.add(nameLabel, java.awt.BorderLayout.WEST);
        nameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ACSD_InterfaceName")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ACSD_InterfaceNameDescription")); // NOI18N

        namePanel.add(nameText, java.awt.BorderLayout.CENTER);

        add(namePanel, java.awt.BorderLayout.NORTH);

        memberspanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "LBL_ExtractInterfaceLabel"))); // NOI18N
        memberspanel.setLayout(new java.awt.BorderLayout());

        scrollPane.setBorder(null);

        membersTable.setModel(tableModel);
        membersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        scrollPane.setViewportView(membersTable);
        membersTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ACSD_MembersToExtract")); // NOI18N
        membersTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ACSD_MembersToExtractDescription")); // NOI18N

        memberspanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        add(memberspanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable membersTable;
    private javax.swing.JPanel memberspanel;
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
            return UIUtilities.getColumnName(NbBundle.getMessage(ExtractInterfacePanel.class, COLUMN_NAMES[column]));
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
            // column 0 is always editable, column 1 is never editable
            return columnIndex == 0;
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
            List result = new ArrayList();
            
            for (Tree implTree : sourceTree.getImplementsClause()) {
                TreePath implPath = javac.getTrees().getPath(javac.getCompilationUnit(), implTree);
                TypeMirror implMirror = javac.getTrees().getTypeMirror(implPath);
                result.add(new ExtractInterfaceInfo<TypeMirrorHandle>(
                        TypeMirrorHandle.create(implMirror),
                        "implements " + implTree.toString(), // NOI18N
                        ElementIcons.getElementIcon(ElementKind.INTERFACE, null),
                        implTree.toString(),
                        Group.IMPLEMENTS
                        ));
            }
            
            for (Tree member : sourceTree.getMembers()) {
                TreePath memberTreePath = javac.getTrees().getPath(javac.getCompilationUnit(), member);
                if (javac.getTreeUtilities().isSynthetic(memberTreePath)) {
                    continue;
                }
                
                Element memberElm = javac.getTrees().getElement(memberTreePath);
                Set<Modifier> mods;
                if (memberElm == null || !(mods = memberElm.getModifiers()).contains(Modifier.PUBLIC)) {
                    continue;
                }
                
                Group group;
                String format = ElementHeaders.NAME;
                if (memberElm.getKind() == ElementKind.FIELD) {
                    if (!mods.contains(Modifier.STATIC) || !mods.contains(Modifier.FINAL)
                            || ((VariableTree) member).getInitializer() == null) {
                        continue;
                    }
                    group = Group.FIELD;
                    format += " : " + ElementHeaders.TYPE; // NOI18N
// XXX see ExtractInterfaceRefactoringPlugin class description
//                } else if (member.getKind() == Tree.Kind.CLASS) {
//                    if (!mods.contains(Modifier.STATIC))
//                        continue;
//                    group = 3;
                } else if (memberElm.getKind() == ElementKind.METHOD) {
                    if (mods.contains(Modifier.STATIC)) {
                        continue;
                    }
                    group = Group.METHOD;
                    format += ElementHeaders.PARAMETERS + " : " + ElementHeaders.TYPE; // NOI18N
                } else {
                    continue;
                }
                result.add(new ExtractInterfaceInfo<ElementHandle>(
                        ElementHandle.create(memberElm),
                        ElementHeaders.getHeader(memberElm, javac, format),
                        ElementIcons.getElementIcon(memberElm.getKind(), mods),
                        memberElm.getSimpleName().toString(),
                        group
                        ));
            }

            // the members are collected
            // now, create a tree map (to sort them) and create the table data
            result.sort(new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    ExtractInterfaceInfo i1 = (ExtractInterfaceInfo) o1;
                    ExtractInterfaceInfo i2 = (ExtractInterfaceInfo) o2;
                    int result = i1.group.compareTo(i2.group);
                    
                    if (result == 0) {
                        result = i1.name.compareTo(i2.name);
                    }
                    
                    return result;
                }
            });
            members = new Object[result.size()][2];
            for (int i = 0; i < members.length; i++) {
                members[i][0] = Boolean.FALSE;
                members[i][1] = result.get(i);
            }
            // fire event to repaint the table
            this.fireTableDataChanged();
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    private static final class ExtractInterfaceInfo<H> {
        final H handle;
        final String htmlText;
        final Icon icon;
        final String name;
        final Group group;
        
        public ExtractInterfaceInfo(H handle, String htmlText, Icon icon, String name, Group group) {
            this.handle = handle;
            this.htmlText = htmlText;
            this.icon = icon;
            this.name = name;
            this.group = group;
        }
    }
    
    private enum Group {
        IMPLEMENTS, METHOD, FIELD;
    }
}
