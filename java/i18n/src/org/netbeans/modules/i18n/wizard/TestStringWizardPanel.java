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


package org.netbeans.modules.i18n.wizard;


import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.PropertyPanel;

import org.netbeans.modules.i18n.java.JavaI18nFinder;
import org.openide.DialogDescriptor;
import org.openide.WizardValidationException;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;


/**
 * <code>WizardDescriptor.Panel</code> used for to show found missing keys.
 * It is the fourth and last panel of I18N Test Wizard.
 *
 * @author  Peter Zavadsky
 * @see Panel
 */
final class TestStringWizardPanel extends JPanel {
    
    /** Column index of check box column. */
    private static final int COLUMN_INDEX_CHECK = 0;
    /** Column index of hard string column. */
    private static final int COLUMN_INDEX_HARDSTRING = 1;
    /** Column index of key column. */
    private static final int COLUMN_INDEX_KEY = 2;
    /** Column index of value column. */
    private static final int COLUMN_INDEX_VALUE = 3;

    /** Local copy of i18n wizard data. */
    private final Map<DataObject,SourceData> sourceMap = Util.createWizardSourceMap();

    /** Table model for <code>stringTable</code>. */
    private final AbstractTableModel tableModel = new TestStringTableModel();
    
    /** Creates new form HardCodedStringsPanel */
    private TestStringWizardPanel() {
        
        initComponents();        
        
        postInitComponents();
        
        initTable();

        setComboModel(sourceMap);
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_FOUNDMISSINGRESOURCES);
    }

        
    /** Sets combo model only for source which were some found strings in. */
    private void setComboModel(Map<DataObject,SourceData> sourceMap) {
        List<DataObject> nonEmptySources = new ArrayList<DataObject>();

        for (Map.Entry<DataObject,SourceData> entry : sourceMap.entrySet()) {
            if (!entry.getValue().getStringMap().isEmpty()) {
                nonEmptySources.add(entry.getKey());
            }
        }
        sourceCombo.setModel(new DefaultComboBoxModel(nonEmptySources.toArray()));
    }
    
    /** Adds additional init of components. */
    private void postInitComponents() {
        sourceLabel.setLabelFor(sourceCombo);
        testStringLabel.setLabelFor(testStringTable);
    }

    /** Getter for <code>resources</code> property. */
    public Map<DataObject,SourceData> getSourceMap() {
        return sourceMap;
    } 
    
    /** Setter for <code>resources</code> property. */
    public void setSourceMap(Map<DataObject,SourceData> sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        setComboModel(sourceMap);
    }
    
    /** Gets string map for specified source data object. Utility method. */
    private Map<HardCodedString,I18nString> getStringMap() {
        SourceData sourceData = sourceMap.get(sourceCombo.getSelectedItem());
        return sourceData == null ? null : sourceData.getStringMap();
    }

    /** Gets hard coded strings user wish to not proceed. */
    private Set<HardCodedString> getRemovedStrings() {
        SourceData sourceData = sourceMap.get(sourceCombo.getSelectedItem());
        if (sourceData == null) {
            return null;
        }
        
        if (sourceData.getRemovedStrings() == null) {
            sourceData.setRemovedStrings(new HashSet<HardCodedString>());
        }
        
        return sourceData.getRemovedStrings();                    
    }
    
    /** Inits table component. */
    private void initTable() {
        testStringTable.setDefaultRenderer(HardCodedString.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                    
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                HardCodedString hcString = (HardCodedString) value;

                label.setText((hcString != null)
                              ? hcString.getText()
                              : ""); // NOI18N
                
                // Handle Bug 33759 (http://netbeans.org/bugzilla/show_bug.cgi?id=33759)
                SourceData data = sourceMap.get(sourceCombo.getSelectedItem());
                I18nSupport support = data.getSupport();
                if (support != null) {
                    I18nSupport.I18nFinder finder = support.getFinder();
                    if (finder instanceof JavaI18nFinder) {
                        if(label != null) {
                            if (hcString != null) {
                                HardCodedString newHCstring = ((JavaI18nFinder) finder).modifyHCStringText(hcString);
                                label.setText((newHCstring != null)
                                        ? newHCstring.getText()
                                        : hcString.getText());
                            }
                        }
                    }
                }
                return label;
            }
        });
        
        testStringTable.setDefaultRenderer(I18nString.class, new DefaultTableCellRenderer() {
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

                I18nString i18nString = (I18nString) value;

                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int modelColumn = testStringTable.convertColumnIndexToModel(column);
                
                if (i18nString != null) {
                    label.setText((modelColumn == COLUMN_INDEX_KEY)
                                  ? i18nString.getKey()
                                  : i18nString.getValue());
                } else {
                    label.setText(""); // NOI18N
                }
                
                return label;
            }
        });

        testStringTable.setDefaultEditor(I18nString.class, new DefaultCellEditor(new JTextField()) {
            
            @Override
            public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected,
                int row, int column) {

                I18nString i18nString = (I18nString) value;
                
                int modelColumn = testStringTable.convertColumnIndexToModel(column);
                
                if (modelColumn == COLUMN_INDEX_KEY) {
                    value = i18nString == null ? "" : i18nString.getKey(); // NOI18N
                } else if (modelColumn == COLUMN_INDEX_VALUE) {
                    value = i18nString == null ? "" : i18nString.getValue(); // NOI18N
                } else {
                    value = ""; // NOI18N
                }
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        });
        
        // PENDING: Setting the size of columns with check box and  customize button editor.
        testStringTable.getColumnModel().getColumn(COLUMN_INDEX_CHECK).setMaxWidth(30);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceLabel = new javax.swing.JLabel();
        sourceCombo = new javax.swing.JComboBox();
        testStringLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        testStringTable = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, NbBundle.getBundle(HardStringWizardPanel.class).getString("LBL_Source")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceLabel, gridBagConstraints);

        sourceCombo.setRenderer(new SourceWizardPanel.DataObjectListCellRenderer());
        sourceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(sourceCombo, gridBagConstraints);
        sourceCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestStringWizardPanel.class, "LBL_Source_Accessible_Name")); // NOI18N
        sourceCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TestStringWizardPanel.class, "LBL_Source_Accessible_Description")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(testStringLabel, NbBundle.getBundle(HardStringWizardPanel.class).getString("LBL_missing_keys")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(testStringLabel, gridBagConstraints);

        scrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        testStringTable.setModel(tableModel);
        scrollPane.setViewportView(testStringTable);
        testStringTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TestStringWizardPanel.class, "LBL_FoundStrings_Accessible_Name")); // NOI18N
        testStringTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TestStringWizardPanel.class, "LBL_FoundStrings_Accessible_Description")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(scrollPane, gridBagConstraints);
        scrollPane.getAccessibleContext().setAccessibleName("Found Strings with Missing Keys");
        scrollPane.getAccessibleContext().setAccessibleDescription("Found Strings with Missing Keys");
    }// </editor-fold>//GEN-END:initComponents

    private void sourceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceComboActionPerformed
        if ((sourceMap.get(sourceCombo.getSelectedItem())).getStringMap().isEmpty()) {
            // There are no hardcoded strings found for this selected source.
            JLabel label = new JLabel(
                    NbBundle.getMessage(TestStringWizardPanel.class,
                                        "TXT_AllI18nStringsSource"));   //NOI18N
            label.setHorizontalAlignment(JLabel.CENTER);
            scrollPane.setViewportView(label);
        } else {
            scrollPane.setViewportView(testStringTable);
            tableModel.fireTableDataChanged();
        }
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_sourceComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JComboBox sourceCombo;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JLabel testStringLabel;
    private javax.swing.JTable testStringTable;
    // End of variables declaration//GEN-END:variables

    /** Table model for this class. */
    private class TestStringTableModel extends AbstractTableModel {
        
        /** Constructor. */
        public TestStringTableModel() {
        }
        
        
        /** Implements superclass abstract method. */
        public int getColumnCount() {
            return 4;
        }
        
        /** Implemenst superclass abstract method. */
        public int getRowCount() {
            Map stringMap = getStringMap();
            return stringMap == null ? 0 : stringMap.size();
        }
        
        /** Implements superclass abstract method. */
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map stringMap = getStringMap();
            
            if (stringMap == null) {
                return null;
            }
            
            if (columnIndex == COLUMN_INDEX_CHECK) {
                return !getRemovedStrings().contains(stringMap.keySet().toArray()[rowIndex]) ? Boolean.TRUE : Boolean.FALSE;
            } else if (columnIndex == COLUMN_INDEX_HARDSTRING) {
                return stringMap.keySet().toArray()[rowIndex];
            } else {
                return stringMap.values().toArray()[rowIndex];
            }
        }
        
        /** Overrides superclass method.
         * @return false for all columns but the value and check box column */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == COLUMN_INDEX_CHECK)
                   || (columnIndex == COLUMN_INDEX_VALUE);
        }
        
        /** Overrides superclass method. */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Map<HardCodedString,I18nString> stringMap = getStringMap();
            
            if (stringMap == null) {
                return;
            }
             
            if (columnIndex == COLUMN_INDEX_CHECK && value instanceof Boolean) {
                HardCodedString[] hardCodedStrings
                        = stringMap.keySet().toArray(new HardCodedString[stringMap.size()]);
                HardCodedString hardString = hardCodedStrings[rowIndex];
                
                Set<HardCodedString> removedStrings = getRemovedStrings();
                
                if (((Boolean) value).booleanValue()) {
                    removedStrings.remove(hardString);
                } else {
                    removedStrings.add(hardString);
                }
            }
            
            if (columnIndex == COLUMN_INDEX_VALUE) {
                Collection<I18nString> i18nStrings = getStringMap().values();
                I18nString i18nString = i18nStrings.toArray(new I18nString[0])[rowIndex];

                i18nString.setValue(value.toString());
            }
        }
        
        /** Overrides superclass method. 
         * @return DataObject.class */
        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == COLUMN_INDEX_CHECK) {
                return Boolean.class;
            } else if (columnIndex == COLUMN_INDEX_HARDSTRING) {
                return HardCodedString.class;
            } else {
                return I18nString.class;
            }
        }

        /** Overrides superclass method. */
        @Override
        public String getColumnName(int column) {          
            if (column == COLUMN_INDEX_HARDSTRING) {
                return NbBundle.getMessage(HardStringWizardPanel.class, "LBL_HardString");
            } else if (column == COLUMN_INDEX_KEY) {
                return NbBundle.getMessage(HardStringWizardPanel.class, "LBL_Key");
            } else if (column == COLUMN_INDEX_VALUE) {
                return NbBundle.getMessage(HardStringWizardPanel.class, "LBL_Value");
            } else {
                return " "; // NOI18N
            }
        }
    } // End of ResourceTableModel nested class.


    /** Cell editor for the right most 'customize' column. It shows dialog 
     * constructed from <code>PropertyPanel</code> which provides actual custmization of the 
     * <code>I18nString</code> instance.
     * @see org.netbeans.modules.i18n.PropertyPanel
     */
    public static class CustomizeCellEditor extends AbstractCellEditor 
    implements TableCellEditor, ActionListener {

        /** <code>I18nString</code> instance to be edited by this editor. */
        private I18nString i18nString;
        
        /** Editor component, in our case <code>JButton</code>. */
        private JButton editorComponent;

        
        /** Constructor. */
        public CustomizeCellEditor() {
            editorComponent = new JButton("..."); // NOI18N
            
            editorComponent.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    PropertyPanel panel = i18nString.getSupport().getPropertyPanel();
                    panel.setI18nString(i18nString);

                    DialogDescriptor dd = new DialogDescriptor(panel,"Customize Property");
                    dd.setModal(true);
                    dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
                    dd.setOptions(new Object[] {DialogDescriptor.OK_OPTION});
                    dd.setAdditionalOptions(new Object[0]);
                    dd.setButtonListener(CustomizeCellEditor.this);

                    Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                    dialog.setVisible(true);
                }
            });
        }

        /** Implements <code>TableCellEditor</code> interface. */
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            i18nString = (I18nString) value;
            
            return editorComponent;
        }
        
        /** Implements <code>TableCellEditor</code> interface. */
        public Object getCellEditorValue() {
            return i18nString;
        }

        /** Implements <code>TableCellEditor</code> interface. */
        @Override
        public boolean isCellEditable(EventObject anEvent) { 
            if (anEvent instanceof MouseEvent) { 
                // Counts needed to start editing.
                return ((MouseEvent) anEvent).getClickCount() >= 1;
            }
            return true;
        }

        /** Implements <code>TableCellEditor</code> interface. */
        @Override
        public boolean shouldSelectCell(EventObject anEvent) { 
            return true; 
        }

        /** Implements <code>TableCellEditor</code> interface. */
        @Override
        public boolean stopCellEditing() {
            fireEditingStopped(); 
            return true;
        }

        /** Implements <code>TableCellEditor</code> interface. */
        @Override
        public void cancelCellEditing() {
           fireEditingCanceled(); 
        }
        
        /** Implements <code>ActionListener</code> interface. */
        public void actionPerformed(ActionEvent evt) {
            stopCellEditing();
        }

    }
    
    
    /** <code>WizardDescriptor.Panel</code> used for <code>HardCodedStringPanel</code>. 
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel*/
    public static class Panel extends I18nWizardDescriptor.Panel
                              implements WizardDescriptor.FinishablePanel<I18nWizardDescriptor.Settings>,
                                         AsynchronousValidatingPanel<I18nWizardDescriptor.Settings> {

        private static final String CARD_GUI = "gui";                   //NOI18N
        private static final String CARD_MSG = "msg";                   //NOI18N
        private static final String CARD_REPLACING = "replacing";       //NOI18N

        /** Empty label component. */
        private JLabel emptyLabel;        

        /** Test wizard panel component. */
        private transient TestStringWizardPanel testStringPanel;
        
        /** Indicates whether this panel is used in i18n test wizard or not. */
        private volatile boolean hasFoundStrings;
        /** */
        private volatile ProgressWizardPanel progressPanel;

        public Panel() {            
        }
        

        
        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {
            JPanel panel = new JPanel(new CardLayout());
            panel.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(
                            TestStringWizardPanel.class,
                            "ACS_TestStringWizardPanel"));              //NOI18N
            
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                                    Integer.valueOf(2));
            panel.setName(
                    NbBundle.getMessage(TestStringWizardPanel.class,
                                        "TXT_FoundMissingResource"));   //NOI18N
            panel.setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);                    
            return panel;
        }

        /** Gets if panel is valid. Overrides superclass method. */
        @Override
        public boolean isValid() {
            return true;
        }
        
        /**
         */
        public boolean isFinishPanel() {
            return true;
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        @Override
        public void readSettings(I18nWizardDescriptor.Settings settings) {
	    super.readSettings(settings);
            getUI().setSourceMap(getMap());
            
            hasFoundStrings = foundStrings(getMap());

            JPanel panel = (JPanel)getComponent();
            if (hasFoundStrings) {
                panel.add(getUI(), CARD_GUI);
                ((CardLayout) panel.getLayout()).show(panel, CARD_GUI);
            } else {
                panel.add(getMessageComp(), CARD_MSG);
                ((CardLayout) panel.getLayout()).show(panel, CARD_MSG);
            }
        }
        
        /** Stores settings at the end of panel show. Overrides superclass method. */
        @Override
        public void storeSettings(I18nWizardDescriptor.Settings settings) {
	    super.storeSettings(settings);
            // Update sources.
            getMap().clear();
            getMap().putAll(getUI().getSourceMap());
        }

        public void prepareValidation() {
            assert EventQueue.isDispatchThread();
            if (hasFoundStrings) {
                progressPanel = new ProgressWizardPanel(true);
                progressPanel.setMainText(
                        NbBundle.getMessage(getClass(),
                                            "LBL_Internationalizing")); //NOI18N
                progressPanel.setMainProgress(0);

                Container container = (Container) getComponent();
                container.add(progressPanel, CARD_REPLACING);
                ((CardLayout) container.getLayout()).show(container, CARD_REPLACING);
            }
        }

        /** Searches hard coded strings in sources and puts found hard coded string - i18n string pairs
         * into settings. Implements <code>ProgressMonitor</code> interface method. */
        public void validate() throws WizardValidationException {
            assert !EventQueue.isDispatchThread();
            if (hasFoundStrings) {
                // Add missing key-value pairs into resource.
                Map<DataObject,SourceData> sourceMap = getUI().getSourceMap();

                // For each source perform the task.
                int counterOuter = 0;
                for (Map.Entry<DataObject,SourceData> srcMapEntry : sourceMap.entrySet()) {
                    counterOuter++;
                    DataObject source = srcMapEntry.getKey();
                    SourceData sourceData = srcMapEntry.getValue();

                    // Get i18n support for this source.
                    I18nSupport support = sourceData.getSupport();

                    // Get string map.
                    Map<HardCodedString,I18nString> stringMap = sourceData.getStringMap();

                    // Get removed strings.
                    Set<HardCodedString> removed = sourceData.getRemovedStrings();

                    ClassPath cp = ClassPath.getClassPath(source.getPrimaryFile(),
                                                          ClassPath.SOURCE);                
                    progressPanel.setSubText(
                            Util.getString("LBL_Source")        //NOI18N
                            + " "                               //NOI18N
                            + cp.getResourceName(source.getPrimaryFile(), '.', false));

                    // Do actual replacement.
                    int counterInner = 0;
                    for (Map.Entry<HardCodedString,I18nString> entry : stringMap.entrySet()) {
                        counterInner++;
                        HardCodedString hcString = entry.getKey();
                        I18nString i18nString = entry.getValue();

                        if ((removed != null) && removed.contains(hcString)) {
                            // Don't proceed.
                            continue;
                        }

                        // Actually put missing property into bundle with origin comment.
                        String comment = i18nString.getComment();
                        if ((comment == null) || "".equals(comment)) {
                            DataObject dobj = source;
                            cp = ClassPath.getClassPath( dobj.getPrimaryFile(), ClassPath.SOURCE );                
                            comment = cp.getResourceName( dobj.getPrimaryFile(), '.', false );
                        }

                        // we may have already added it in, it is the referenced from
                        // multiple sources, merge comments
                        String key = i18nString.getKey();
                        String prev = support.getResourceHolder().getCommentForKey(key);
                        comment += (prev == null ? "" : " " + prev);                // NOI18N
                        support.getResourceHolder().addProperty(
                                                        i18nString.getKey(),
                                                        i18nString.getValue(),
                                                        comment,
                                                        false);

                        progressPanel.setSubProgress((int) (counterInner / (float) stringMap.size() * 100));
                    } // End of inner for.

                    // Provide additional changes if there are some.
                    if (support.hasAdditionalCustomizer()) {
                        support.performAdditionalChanges();
                    }

                    progressPanel.setMainProgress((int) (counterOuter / (float) sourceMap.size() * 100));
                } // End of outer for.
            } // if (foundStrings(getMap()))
        }
        
        /** Indicates if there were found some hardcoded strings in any of selected sources. 
         * @return true if at least one hard coded string was found. */
        private static boolean foundStrings(Map<DataObject,SourceData> sourceMap) {
            for (Map.Entry<DataObject,SourceData> entry : sourceMap.entrySet()) {
                if (!entry.getValue().getStringMap().isEmpty()) {
                    return true;
                }
            }
            return false;
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            return new HelpCtx(I18nUtil.HELP_ID_TESTING);
        }
        
        private synchronized TestStringWizardPanel getUI() {
            if (testStringPanel == null) {
                testStringPanel = new TestStringWizardPanel();
            }
            return testStringPanel;
        }

        private JComponent getMessageComp() {
            if (emptyLabel == null) {
                emptyLabel = new JLabel(
                        NbBundle.getMessage(TestStringWizardPanel.class,
                                            "TXT_AllI18nStrings"));         //NOI18N
                emptyLabel.setHorizontalAlignment(JLabel.CENTER);
                emptyLabel.setVerticalAlignment(JLabel.CENTER);            
            }
            return emptyLabel;
        }

    } // End of nested PanelDescriptor class.
}
