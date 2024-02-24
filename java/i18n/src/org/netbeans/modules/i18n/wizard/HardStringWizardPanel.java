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
import java.util.EventObject;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.PropertyPanel;

import org.netbeans.modules.i18n.java.JavaI18nFinder;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.WizardValidationException;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;


/**
 * <code>WizardDescriptor.Panel</code> used for to show found hard coded strings
 * for sepcified sources. It offers default key-value pairs and allows modify them.
 * These values will be used by actual i18n-zation of those sources.
 * It is the fourth and last panel of I18N Wizard.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see Panel
 */
final class HardStringWizardPanel extends JPanel implements ListSelectionListener {

    /** Column index of check box column. */
    private static final int COLUMN_INDEX_CHECK = 0;
    /** Column index of hard string column. */
    private static final int COLUMN_INDEX_HARDSTRING = 1;
    /** Column index of key column. */
    private static final int COLUMN_INDEX_KEY = 2;
    /** Column index of value column. */
    private static final int COLUMN_INDEX_VALUE = 3;
    /** Column index of custom column. */
    private static final int COLUMN_INDEX_CUSTOM = 4;
        
    /** Local copy of i18n wizard data. */
    private final Map<DataObject,SourceData> sourceMap = Util.createWizardSourceMap();

    /** Table model for <code>stringTable</code>. */
    private final AbstractTableModel tableModel = new HardCodedStringTableModel();
    
    
    /** Creates new form HardCodedStringsPanel */
    private HardStringWizardPanel() {
        initComponents();
        
        postInitComponents();
        
        initTable();

        initAccessibility();
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_FOUNDSTRINGS);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        jSplitPane1.setDividerLocation(0.5D);
    }

    
    /** Sets combo model only for source which were some found strings in. */
    private void setComboModel(Map<DataObject,SourceData> sourceMap) {
        List<DataObject> nonEmptySources = new ArrayList<DataObject>();
        String mimeType = null;
        for (Map.Entry<DataObject,SourceData> entry : sourceMap.entrySet()) {
            if (!entry.getValue().getStringMap().isEmpty()) {
                nonEmptySources.add(entry.getKey());
                mimeType = entry.getKey().getPrimaryFile().getMIMEType();
            }
        }
        
        sourceCombo.setModel(new DefaultComboBoxModel(nonEmptySources.toArray()));
        if (mimeType != null) {
            preview.setContentType(mimeType);
        }
        if (!nonEmptySources.isEmpty())
            sourceComboActionPerformed(null);
    }
    
    /** Adds additional init of components. */
    private void postInitComponents() {
        sourceLabel.setLabelFor(sourceCombo);
        hardStringLabel.setLabelFor(hardStringTable);
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
            // init removed string for the first time
            Set<HardCodedString> removed = new HashSet<HardCodedString>();
            
            // add all strings with empty keys
            Map<HardCodedString,I18nString> stringMap = sourceData.getStringMap();
            for (Map.Entry<HardCodedString,I18nString> entry : stringMap.entrySet()) {
                if (entry.getValue().getKey().equals("")) {
                    removed.add(entry.getKey());
                }
            }
            sourceData.setRemovedStrings(removed);
        }
        
        return sourceData.getRemovedStrings();                    
    }

    /** Inits table component. */
    private void initTable() {
        hardStringTable.setDefaultRenderer(HardCodedString.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                    
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                HardCodedString hcString = (HardCodedString)value;
                
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
        
        hardStringTable.setDefaultRenderer(I18nString.class, new DefaultTableCellRenderer() {
            private final JButton dotButton = new JButton("...");               // NOI18N
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

                I18nString i18nString = (I18nString) value;
                
                int modelColumn = hardStringTable.convertColumnIndexToModel(column);

                if (modelColumn == COLUMN_INDEX_CUSTOM) {
                    return dotButton;
                }
                    
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

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

        hardStringTable.setDefaultEditor(I18nString.class, new DefaultCellEditor(new JTextField()) {
            
            @Override
            public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected,
                int row, int column) {

                I18nString i18nString = (I18nString)value;
                
                int modelColumn = hardStringTable.convertColumnIndexToModel(column);
                
                if (modelColumn == COLUMN_INDEX_KEY) {
                    value = (i18nString == null) ? "" : i18nString.getKey(); // NOI18N
                } else if (modelColumn == COLUMN_INDEX_VALUE) {
                    value = (i18nString == null) ? "" : i18nString.getValue(); // NOI18N
                } else {
                    value = ""; // NOI18N
                }
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        });
        
        Component cellSample = new DefaultTableCellRenderer()
                               .getTableCellRendererComponent(
                                    hardStringTable,  //table
                                    "N/A",            //value           //NOI18N
                                    false,            //isSelected
                                    false,            //hasFocus
                                    0, 0);            //row, column
        int cellHeight = cellSample.getPreferredSize().height;
        int rowHeight = cellHeight + hardStringTable.getRowMargin();
        hardStringTable.setRowHeight(Math.max(16, rowHeight));
        
        hardStringTable.getColumnModel().getColumn(COLUMN_INDEX_CUSTOM).setCellEditor(new CustomizeCellEditor());

        // PENDING: Setting the size of columns with check box and  customize button editor.
        hardStringTable.getColumnModel().getColumn(COLUMN_INDEX_CHECK).setMaxWidth(30);
        hardStringTable.getColumnModel().getColumn(COLUMN_INDEX_CUSTOM).setMaxWidth(30);
        hardStringTable.getSelectionModel().addListSelectionListener(this);
    }
    
    private void initAccessibility() {        
        sourceCombo.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(HardStringWizardPanel.class,
                                    "ACSD_sourceCombo"));
        hardStringTable.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(HardStringWizardPanel.class,
                                    "ACSD_hardStringTable"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceLabel = new javax.swing.JLabel();
        sourceCombo = new javax.swing.JComboBox();
        hardStringLabel = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        scrollPane = new javax.swing.JScrollPane();
        hardStringTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        preview = new javax.swing.JEditorPane();

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

        org.openide.awt.Mnemonics.setLocalizedText(hardStringLabel, NbBundle.getBundle(HardStringWizardPanel.class).getString("LBL_FoundStrings")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(hardStringLabel, gridBagConstraints);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);

        scrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        hardStringTable.setModel(tableModel);
        scrollPane.setViewportView(hardStringTable);

        jSplitPane1.setLeftComponent(scrollPane);

        preview.setEditable(false);
        jScrollPane1.setViewportView(preview);

        jSplitPane1.setRightComponent(jScrollPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void sourceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceComboActionPerformed
        if((sourceMap.get(sourceCombo.getSelectedItem())).getStringMap().isEmpty()) {
            // There are no hardcoded strings found for this selected source.
            JLabel label = new JLabel(NbBundle.getBundle(HardStringWizardPanel.class).getString("TXT_NoHardstringsSource"));
            label.setHorizontalAlignment(JLabel.CENTER);
            scrollPane.setViewportView(label);
        } else {
            scrollPane.setViewportView(hardStringTable);
            tableModel.fireTableDataChanged();
        }
        SourceData data = sourceMap.get(sourceCombo.getSelectedItem());
        Document doc = data.getSupport().getDocument();
        preview.setDocument(doc);
    }//GEN-LAST:event_sourceComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hardStringLabel;
    private javax.swing.JTable hardStringTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JEditorPane preview;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JComboBox sourceCombo;
    private javax.swing.JLabel sourceLabel;
    // End of variables declaration//GEN-END:variables

    public void valueChanged(ListSelectionEvent e) {
        int ix = hardStringTable.getSelectedRow();
        if (ix >= 0) {
            Object o = hardStringTable.getModel().getValueAt (ix, COLUMN_INDEX_HARDSTRING);
            if (o instanceof HardCodedString) {
                HardCodedString hard = (HardCodedString) o;
                Position start = hard.getStartPosition();
                Position end = hard.getEndPosition();
                preview.setSelectionStart (start.getOffset());
                preview.setSelectionEnd(end.getOffset());
            }
        }
    }
    // End of variables declaration

    /** Table model for this class. */
    private class HardCodedStringTableModel extends AbstractTableModel {
        
        /** Constructor. */
        public HardCodedStringTableModel() {
        }
        
        
        
        /** Implements superclass abstract method. */
        public int getColumnCount() {
            return 5;
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
                if (getRemovedStrings().contains(stringMap.keySet().toArray()[rowIndex])) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else if (columnIndex == COLUMN_INDEX_HARDSTRING) {
                return stringMap.keySet().toArray()[rowIndex];
            } else {
                return stringMap.values().toArray()[rowIndex];
            }
        }
        
        /** Overrides superclass method.
         * @ return true for all columns but first */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex != COLUMN_INDEX_HARDSTRING);
        }
        
        /** Overrides superclass method. */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Map<HardCodedString,I18nString> stringMap = getStringMap();
            if (stringMap == null) {
                return;
            }

            switch (columnIndex) {
                case COLUMN_INDEX_HARDSTRING: return;
                case COLUMN_INDEX_CUSTOM:
                    I18nString otherValue = (I18nString) getValueAt(rowIndex, COLUMN_INDEX_KEY);
                    if (!((I18nString) value).getKey().equals("")) {
                        setValueAt(Boolean.TRUE, rowIndex, COLUMN_INDEX_CHECK);
                    } else {
                        setValueAt(Boolean.FALSE, rowIndex, COLUMN_INDEX_CHECK);
                    }
                    break;
                case COLUMN_INDEX_CHECK : 
                    if (value instanceof Boolean) {

                        // check that the key is not empty and thus it is allowed
                        // to change the value. Display a notification otherwise.
                        if ((((Boolean) value).booleanValue() == true) && 
                            ((I18nString) getValueAt(rowIndex, COLUMN_INDEX_KEY)).getKey().equals("")) 
                        { // empty,not allowed
                            String message = NbBundle.getMessage(HardStringWizardPanel.class, "MSG_CANNOT_INSERT_EMPTY_KEYS");
                            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
                        } else {               
                            Set<HardCodedString> hcStrings = stringMap.keySet();
                            HardCodedString[] hcStringsArr = hcStrings.toArray(new HardCodedString[0]);
                            HardCodedString hardString = hcStringsArr[rowIndex];

                            Set<HardCodedString> removedStrings = getRemovedStrings();

                            if (((Boolean) value).booleanValue()) {
                                removedStrings.remove(hardString);
                            } else {
                                removedStrings.add(hardString);
                            }
                        }
                    } 
                    break;
                case COLUMN_INDEX_KEY :  {
                    I18nString i18nString = (I18nString) stringMap.values().toArray()[rowIndex];
                    i18nString.setKey(value.toString());
                    if (!value.toString().equals("")) {
                        setValueAt(Boolean.TRUE, rowIndex, COLUMN_INDEX_CHECK);
                    } else {
                        setValueAt(Boolean.FALSE, rowIndex, COLUMN_INDEX_CHECK);
                    }
                    break;
                }

                case COLUMN_INDEX_VALUE: {
                    I18nString i18nString = (I18nString) stringMap.values().toArray()[rowIndex];
                    i18nString.setValue(value.toString());
                    if (!i18nString.getKey().equals("")) {
                        setValueAt(Boolean.TRUE, rowIndex, COLUMN_INDEX_CHECK);
                    }                    
                    break;
                }
            } // switch (columnIndex)
            
            fireTableRowsUpdated(rowIndex, rowIndex);
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
            } else if(column == COLUMN_INDEX_KEY) {
                return NbBundle.getMessage(HardStringWizardPanel.class, "LBL_Key");
            } else if(column == COLUMN_INDEX_VALUE) {
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
                    I18nString clone = (I18nString) i18nString.clone();
                    panel.setI18nString(i18nString);

                    String title = Util.getString("PROP_cust_dialog_name"); //NOI18N
                    DialogDescriptor dd = new DialogDescriptor(panel, title);
                    dd.setModal(true);
                    dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
                    
                    Object options[] =  new Object[] {
                        DialogDescriptor.OK_OPTION,
                        DialogDescriptor.CANCEL_OPTION,
                    };                    
                    dd.setOptions(options);
                    //dd.setAdditionalOptions(new Object[0]);
                    dd.setHelpCtx(new HelpCtx(I18nUtil.PE_I18N_STRING_HELP_ID));
                    dd.setButtonListener(CustomizeCellEditor.this);

                    Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                    dialog.setVisible(true);
                    if (dd.getValue() == DialogDescriptor.CANCEL_OPTION) {
                        i18nString.become(clone);
                    }
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
            if(anEvent instanceof MouseEvent) { 
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
        
        /** HardString panel component cache. */
        private transient HardStringWizardPanel hardStringPanel;
                
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
            
            panel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(HardStringWizardPanel.class).getString("ACS_HardStringWizardPanel"));//NOI18N
            
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(3));
            panel.setName(NbBundle.getBundle(HardStringWizardPanel.class).getString("TXT_ModifyStrings"));//NOI18N
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

        /** */
        public void prepareValidation() {
            assert EventQueue.isDispatchThread();
            // do this only if there's anything to do
            if (hasFoundStrings) {       
                if (progressPanel == null) {
                    progressPanel = new ProgressWizardPanel(true);
                }

                progressPanel.setMainText(
                        NbBundle.getMessage(
                                HardStringWizardPanel.class,
                                "LBL_Internationalizing"));             //NOI18N
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
            // do this only if there's anything to do
            if (hasFoundStrings) {       
                // Do replacement job here.
                Map<DataObject,SourceData> sourceMap = getUI().getSourceMap();

                // For each source perform the task.
                int outerCounter = 0;
                for (Map.Entry<DataObject,SourceData> srcMapEntry : sourceMap.entrySet()) {
                    outerCounter++;
                    DataObject source = srcMapEntry.getKey();
                    SourceData sourceData = srcMapEntry.getValue();

                    // Get i18n support for this source.
                    I18nSupport support = sourceData.getSupport();

                    // Get string map.
                    Map<HardCodedString,I18nString> stringMap = sourceData.getStringMap();

                    // Get removed strings.
                    Set removed = sourceData.getRemovedStrings();

                    // Do actual replacement.
                    ClassPath cp = ClassPath.getClassPath(source.getPrimaryFile(), ClassPath.SOURCE);
                    progressPanel.setSubText(
                            NbBundle.getMessage(
                                    HardStringWizardPanel.class, "LBL_Source")
                                    + " "
                                    + cp.getResourceName(source.getPrimaryFile(), '.', false));

                    int innerCounter = 0;
                    for (Map.Entry<HardCodedString,I18nString> entry : stringMap.entrySet()) {
                        innerCounter++;
                        HardCodedString hcString = entry.getKey();
                        I18nString i18nString = entry.getValue();

                        if ((removed != null) && removed.contains(hcString)) {
                            // Don't proceed.
                            continue;
                        }

                        // Put new property into bundle.
                        support.getResourceHolder().addProperty(
                                i18nString.getKey(),
                                i18nString.getValue(),
                                i18nString.getComment());

                        // Replace string in source.
                        support.getReplacer().replace(hcString, i18nString);

                        progressPanel.setSubProgress((int) (innerCounter / (float) stringMap.size() * 100));
                    } // End of inner for.

                    // Provide additional changes if there are some.
                    if (support.hasAdditionalCustomizer()) {
                        support.performAdditionalChanges();
                    }

                    progressPanel.setMainProgress((int) (outerCounter / (float) sourceMap.size() * 100));
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
            return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
        }

        private synchronized HardStringWizardPanel getUI() {
            if (hardStringPanel == null) {
                hardStringPanel = new HardStringWizardPanel();
            }
            return hardStringPanel;
        }
        
        private JComponent getMessageComp() {
            if (emptyLabel == null) {
                emptyLabel = new JLabel(NbBundle.getMessage(getClass(), "TXT_NoHardstrings"));
                emptyLabel.setHorizontalAlignment(JLabel.CENTER);
                emptyLabel.setVerticalAlignment(JLabel.CENTER);
            }
            return emptyLabel;
        }

    } // End of nested Panel class.
}
