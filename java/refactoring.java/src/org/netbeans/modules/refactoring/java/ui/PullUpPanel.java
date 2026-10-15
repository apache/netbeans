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
import java.awt.EventQueue;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;


/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula, Jan Becicka
 */
public class PullUpPanel extends JPanel implements CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_PullUp_Selected", "LBL_PullUp_Member", "LBL_PullUp_MakeAbstract"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, TreePathHandle.class, Boolean.class};
    
    // refactoring this panel provides parameters for
    private final PullUpRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // pre-selected members (comes from the refactoring action - the elements
    // that should be pre-selected in the table of members)
    private final Set<MemberInfo<ElementHandle>> selectedMembers;

    // target type to move the members to
    private MemberInfo<ElementHandle<TypeElement>> targetType;

    private final ChangeListener parent;

    private boolean initialized = false;
    
    /** Creates new form PullUpPanel
     * @param refactoring The refactoring this panel provides parameters for.
     * @param selectedMembers Members that should be pre-selected in the panel
     *      (determined by which nodes the action was invoked on - e.g. if it was
     *      invoked on a method, the method will be pre-selected to be pulled up)
     */
    public PullUpPanel(PullUpRefactoring refactoring, Set<MemberInfo<ElementHandle>> selectedMembers, ElementKind sourceKind, final ChangeListener parent) {
        this.parent = parent;
        this.refactoring = refactoring;
        this.tableModel = new TableModel(sourceKind);
        this.selectedMembers = selectedMembers;
        initComponents();
        
        setPreferredSize(new Dimension(420, 380));
        
        // *** initialize table
        // set renderer for the second column ("Member") do display name of the feature
        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new UIUtilities.JavaElementTableCellRenderer() {
            // override the extractText method to add "implements " prefix to the text
            // in case the value is instance of MultipartId (i.e. it represents an interface
            // name from implements clause)
            @Override
            protected String extractText(Object value) {
                String displayValue = super.extractText(value);
                if (value instanceof MemberInfo && (((MemberInfo)value).getGroup()==MemberInfo.Group.IMPLEMENTS)) {
                    displayValue = "implements " + displayValue; // NOI18N
                }
                return displayValue;
            }
        });
        // send renderer for the third column ("Make Abstract") to make the checkbox:
        // 1. hidden for elements that are not methods
        // 2. be disabled for static methods
        // 3. be disabled and checked for methods if the target type is an interface
        // 4. be disabled and check for abstract methods
        membersTable.getColumnModel().getColumn(2).setCellRenderer(new UIUtilities.BooleanTableCellRenderer(membersTable) {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // make the checkbox checked (even if "Make Abstract" is not set)
                // for non-static methods if the target type is an interface
                MemberInfo<ElementHandle> object = (MemberInfo<ElementHandle>) table.getModel().getValueAt(row, 1);
                if (object.getElementHandle().getKind()== ElementKind.METHOD) {
                    if ((targetType.getElementHandle().getKind().isInterface() &&
                            (!((MemberInfo) object).getModifiers().contains(Modifier.STATIC) &&
                            !((MemberInfo) object).getModifiers().contains(Modifier.ABSTRACT)) &&
                            !((MemberInfo) object).getModifiers().contains(Modifier.DEFAULT))) {
                        value = Boolean.TRUE;
                    }
                }
                //`the super method automatically makes sure the checkbox is not visible if the
                // "Make Abstract" value is null (which holds for non-methods)
                // and that the checkbox is disabled if the cell is not editable (which holds for
                // static methods all the time and for all methods in case the target type is an interface
                // - see the table model)
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
        UIUtilities.initColumnWidth(membersTable, 2, Boolean.TRUE, 4);

        // *** initialize combo
        // set renderer for the combo (to display name of the class)
        supertypeCombo.setRenderer(new UIUtilities.JavaElementListCellRenderer()); // set combo model
        
        membersTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                parent.stateChanged(null);
            }
        });
    }

    /** Initialization of the panel (called by the parent window).
     */
    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        final TreePathHandle handle = refactoring.getSourceType();
        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.RESOLVED);
                    // retrieve supertypes (will be used in the combo)
                    Collection<TypeElement> supertypes = JavaRefactoringUtils.getSuperTypes((TypeElement)handle.resolveElement(controller), controller, true);
                    final List<MemberInfo> minfo = new LinkedList<>();
                    for (TypeElement e: supertypes) {
                        MemberInfo<ElementHandle<TypeElement>> memberInfo = MemberInfo.create(e, controller);
                        if(memberInfo.getElementHandle().resolve(controller) != null) { // #200200 - Error in pulling up to a interface with cyclic inheritance error
                            minfo.add(memberInfo);
                        }
                    }
                    
                    TypeElement sourceTypeElement = (TypeElement) handle.resolveElement(controller);
                    final String name = ElementHeaders.getHeader(sourceTypeElement, controller, ElementHeaders.NAME);
                    
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                           supertypeCombo.setModel(new ComboModel(minfo.toArray(new MemberInfo[0])));
                           setName(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "LBL_PullUpHeader", new Object[] {name})); // NOI18N
                        }
                    });
                }
            }, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        initialized = true;
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    /** Getter used by the refactoring UI to get value
     * of target type.
     * @return Target type.
     */
    public MemberInfo<ElementHandle<TypeElement>> getTargetType() {
        return targetType;
    }
    
    /** Getter used by the refactoring UI to get members to be pulled up.
     * @return Descriptors of members to be pulled up.
     */
    public MemberInfo[] getMembers() {
        final List list = new ArrayList();
        // remeber if the target type is an interface (will be used in the loop)
        boolean targetIsInterface = targetType.getElementHandle().getKind().isInterface();
        // go through all rows of a table and collect selected members
        for (int i = 0; i < tableModel.members.length; i++) {
            // if the current row is selected, create MemberInfo for it and
            // add it to the list of selected members
            if (tableModel.members[i][0].equals(Boolean.TRUE)) {
                MemberInfo<ElementHandle> element = (MemberInfo<ElementHandle>) tableModel.members[i][1];
                // for methods the makeAbstract is always set to true if the
                // target type is an interface
                element.setMakeAbstract(((element.getElementHandle().getKind() == ElementKind.METHOD) &&
                    targetIsInterface &&
                    !((MemberInfo)tableModel.members[i][1]).getModifiers().contains(Modifier.DEFAULT)) ||
                    ((Boolean) tableModel.members[i][2]==null?Boolean.FALSE:(Boolean)tableModel.members[i][2]));
                list.add(element);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        supertypePanel = new javax.swing.JPanel();
        supertypeCombo = new javax.swing.JComboBox();
        supertypeLabel = new javax.swing.JLabel();
        chooseLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.BorderLayout());

        supertypePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        supertypePanel.setLayout(new java.awt.BorderLayout(12, 0));
        supertypePanel.add(supertypeCombo, java.awt.BorderLayout.CENTER);

        supertypeLabel.setLabelFor(supertypeCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(supertypeLabel, bundle.getString("LBL_PullUp_Supertype")); // NOI18N
        supertypePanel.add(supertypeLabel, java.awt.BorderLayout.WEST);
        supertypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_DestinationSupertypeName")); // NOI18N
        supertypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_DestinationSupertypeDescription")); // NOI18N

        chooseLabel.setLabelFor(membersTable);
        org.openide.awt.Mnemonics.setLocalizedText(chooseLabel, org.openide.util.NbBundle.getMessage(PullUpPanel.class, "LBL_PullUpLabel")); // NOI18N
        chooseLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        supertypePanel.add(chooseLabel, java.awt.BorderLayout.SOUTH);

        add(supertypePanel, java.awt.BorderLayout.NORTH);

        membersTable.setModel(tableModel);
        membersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        scrollPane.setViewportView(membersTable);
        membersTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_MembersToPullUp")); // NOI18N
        membersTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_MembersToPullUpDescription")); // NOI18N

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel chooseLabel;
    private javax.swing.JTable membersTable;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JComboBox supertypeCombo;
    private javax.swing.JLabel supertypeLabel;
    private javax.swing.JPanel supertypePanel;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public Component getComponent() {
        return this;
    }

    // --- MODELS --------------------------------------------------------------
    
    /** Model for the members table.
     */
    private class TableModel extends AbstractTableModel {
        // data for the members table (first dimension - rows, second dimension - columns)
        // the columns are: 0 = Selected (true/false), 1 = Member (Java element), 2 = Make Abstract (true/false)
        private Object[][] members = new Object[0][0];
        private final ElementKind sourceKind;

        public TableModel(ElementKind sourceKind) {
            this.sourceKind = sourceKind;
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return UIUtilities.getColumnName(NbBundle.getMessage(PullUpPanel.class, COLUMN_NAMES[column]));
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
                // if the target type is not an interface
                // if the method is abstract
                if (members[rowIndex][2] == null) {
                    return false;
                }
                Object element = members[rowIndex][1];
                MemberInfo member = (MemberInfo) element;                
                return member.getModifiers().contains(Modifier.DEFAULT) || 
                        (!sourceKind.isInterface() &&
                        !(member.getModifiers().contains(Modifier.STATIC)) &&
                        !(member.getModifiers().contains(Modifier.ABSTRACT)));
            } else {
                // column 0 is always editable, column 1 is never editable
                return columnIndex == 0;
            }
        }
        

        /** Method called by target type combo box model when the selection changes
         * (i.e. when the selected target type changes).
         * Updates table rows based on the change (all members from the source type
         * up to the direct subtypes of the target type need to be displayed).
         * @param classes Classes the members of which should be displayed (these are all classes
         *      that are supertypes of source type (including the source type) and at the same time subtypes
         *      of the target type (excluding the target type).
         */
        void update(final MemberInfo<ElementHandle<TypeElement>> selectedTarget) {
            final Map map = new HashMap();
            JavaSource source = JavaSource.forFileObject(refactoring.getSourceType().getFileObject());
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(CompilationController info) {
                        try {
                            info.toPhase(JavaSource.Phase.RESOLVED);
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                        
                        TypeElement targetElement = selectedTarget.getElementHandle().resolve(info);
                        TypeElement sourceElement = (TypeElement) refactoring.getSourceType().resolveElement(info);
                        TypeMirror sourceTM = sourceElement.asType();
                        TypeMirror targetTM = targetElement.asType();
                        
                        // compute the classes (they must be superclasses of source type - including it -
                        // and subtypes of target type)
                        List<TypeElement> classes = new ArrayList<>();
                        
                        // add source type (it is always included)
                        classes.add(sourceElement);
                        classes.addAll(JavaRefactoringUtils.getSuperTypes(sourceElement, info, true));
                        for (TypeElement superType : classes) {
                            // add Members from supertypes up to the selected target
                            if (!info.getTypes().isSubtype(sourceTM, targetTM) ||
                                    info.getTypes().isSameType(superType.asType(), targetTM)) {
                                continue;
                            }
                            // collect interface names
                            for (TypeMirror tm: superType.getInterfaces()) {
                                MemberInfo ifcName = MemberInfo.create(RefactoringUtils.typeToElement(tm, info), info, MemberInfo.Group.IMPLEMENTS);
                                map.put(ifcName, new Object[] {Boolean.FALSE, ifcName, null});
                            }
                            // collect fields, methods and inner classes
                            List<? extends Element> features = superType.getEnclosedElements();
                            for (Element e : features) {
                                switch (e.getKind()) {
                                    case CONSTRUCTOR:
                                    case STATIC_INIT:
                                    case INSTANCE_INIT:
                                        continue;
                                    default: {
                                        MemberInfo mi = MemberInfo.create(e, info);
                                        map.put(mi, new Object[]{Boolean.FALSE, mi, (e.getKind() == ElementKind.METHOD) ? Boolean.FALSE : null});
                                    }
                                }
                            }
                        }
                        // select some members if applicable
                        if (members.length == 0) {
                            // if the collection of pre-selected members is not null
                            // this is the first creation of the table data ->
                            // -> select the members from the selectedMembers collection
                            for (Iterator it = selectedMembers.iterator(); it.hasNext();) {
                                Object[] value = (Object[]) map.get(it.next());
                                if (value != null) {
                                    value[0] = Boolean.TRUE;
                                }
                            }
                        } else {
                            // this is not the first update of the table content ->
                            // -> select elements that were selected before the update
                            // (if they will still be present in the table)
                            for (int i = 0; i < members.length; i++) {
                                Object[] value = (Object[]) map.get(members[i][1]);
                                if (value != null) {
                                    map.put(value[1], members[i]);
                                }
                            }
                        }

                        // TODO: remove overrides, since they cannot be pulled up
                        // Did not work even in 5.5

                        // the members are collected
                        // now, create a tree map (to sort them) and create the table data
                        TreeMap treeMap = new TreeMap(new Comparator() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                return ((MemberInfo) o1).getHtmlText().compareTo(((MemberInfo) o2).getHtmlText());

                //TODO: sorting
                //                            NamedElement ne1 = (NamedElement) o1, ne2 = (NamedElement) o2;
                //                            // elements are sorted primarily by their class name
                //                            int result = ne1.getClass().getName().compareTo(ne2.getClass().getName());
                //                            if (result == 0) {
                //                                // then by their display text
                //                                result = UIUtilities.getDisplayText(ne1).compareTo(UIUtilities.getDisplayText(ne2));
                //                            }
                //                            if (result == 0) {
                //                                // then the mofid is compared (to not take two non-identical
                //                                // elements as equals)
                //                                result = ne1.refMofId().compareTo(ne2.refMofId());
                //                            }
                //                            return result;
                            }
                        });
                        treeMap.putAll(map);
                        members = new Object[treeMap.size()][];
                        int i = 0;
                        for (Iterator it = treeMap.values().iterator(); it.hasNext(); i++) {
                            members[i] = (Object[]) it.next();
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            // fire event to repaint the table
            this.fireTableDataChanged();
        }
    }

    /** Model for combo box for choosing target type.
     */
    private class ComboModel extends AbstractListModel implements ComboBoxModel {
        private final MemberInfo<ElementHandle>[] supertypes;
       
        /** Creates the combo model.
         * @param supertypes List of applicable supertypes that may be chosen to be
         *      target types.
         */
        ComboModel(MemberInfo[] supertypes) {
            this.supertypes = supertypes;
            if (supertypes.length > 0) {
                setSelectedItem(supertypes[0]);
            }
        }
        
        /** Gets invoked when the selection changes. Computes the classes the members
         * of which can be pulled up and calls table model's update() method to
         * update the table content with changed set of members.
         * @param anItem Class selected to be the target.
         */
        @Override
        public void setSelectedItem(final Object anItem) {
            if (targetType != anItem) {
                targetType = ((MemberInfo) anItem);
                // must fire this (according to the ComboBoxModel interface contract)
                fireContentsChanged(this, -1, -1);
                tableModel.update(targetType);
            }
        }

        @Override
        public Object getSelectedItem() {
            return targetType;
        }

        @Override
        public Object getElementAt(int index) {
            return supertypes[index];
        }

        @Override
        public int getSize() {
            return supertypes.length;
        }
    }
}
