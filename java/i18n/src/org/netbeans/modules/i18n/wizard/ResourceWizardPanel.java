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
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.ResourceHolder;
import org.netbeans.modules.i18n.SelectorUtils;

import org.openide.WizardValidationException;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.awt.Mnemonics;


/**
 * Second panel of I18N Wizard.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see Panel
 */
final class ResourceWizardPanel extends JPanel {

    /** Local copy of i18n wizard data. */
    private final Map<DataObject,SourceData> sourceMap = Util.createWizardSourceMap();

    /** Table model for resourcesTable. */
    private final ResourceTableModel tableModel = new ResourceTableModel();

    /** This component panel wizard descriptor.
     * @see org.openide.WizardDescriptor.Panel 
     * @see Panel */
    private final Panel descPanel;

    private final boolean testMode;
    
    /** Creates new form SourceChooserPanel. */
    private ResourceWizardPanel(Panel descPanel, boolean testMode) {
        this.descPanel = descPanel;
        this.testMode = testMode;
        
        initComponents();        
        
        initTable();
        
        initAccesibility();

        if(testMode) {
            HelpCtx.setHelpIDString(this, Util.HELP_ID_SELECTTESTRESOURCE);
        } else {
            HelpCtx.setHelpIDString(this, Util.HELP_ID_SELECTRESOURCE);
        }
    }

    
    /** Getter for <code>resources</code> property. */
    public Map<DataObject,SourceData> getSourceMap() {
        return sourceMap;
    }
    
    /** Setter for <code>resources</code> property. */
    public void setSourceMap(Map<DataObject,SourceData> sourceMap) {
            this.sourceMap.clear();
            this.sourceMap.putAll(sourceMap);
        
        tableModel.fireTableDataChanged();
       
        descPanel.fireStateChanged();
    }
    
    private String getPanelDescription() {
        if (testMode == false) {
            return Util.getString("MSG_ResourcePanel_desc");
        } else {
            return Util.getString("MSG_ResourcePanel_test_desc");
        }
    }
    
    /** Inits table component. */
    private void initTable() {
        resourcesTable.setDefaultRenderer(DataObject.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                    
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                DataObject dataObject = (DataObject)value;

                if(dataObject != null) {                    
                    String name = "N/A";
                    if (column == 0) {
                        // name for the first column, from sources
                        ClassPath cp = ClassPath.getClassPath(dataObject.getPrimaryFile(), ClassPath.SOURCE );                    
                        name = cp.getResourceName( dataObject.getPrimaryFile(), '.', false );
                    } else {
                        // name for resource bundle, from execution,
                        // but the reference file must be the
                        // corresponding source
                        DataObject dob = (DataObject)tableModel.getValueAt(row, 0);
                        name = Util.getResourceName(dob.getPrimaryFile(), dataObject.getPrimaryFile(), '.', false);
                    }

                    label.setText(name); // NOI18N
                    label.setIcon(ImageUtilities.image2Icon(dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
                } else {
                    label.setText(""); // NOI18N
                    label.setIcon(null);
                }
                
                return label;
            }
        });

        Component cellSample = new DefaultTableCellRenderer()
                               .getTableCellRendererComponent(
                                    resourcesTable, //table
                                    "N/A",          //value             //NOI18N
                                    false,          //isSelected
                                    false,          //hasFocus
                                    0, 0);          //row, column
        int cellHeight = cellSample.getPreferredSize().height;
        int rowHeight = cellHeight + resourcesTable.getRowMargin();
        resourcesTable.setRowHeight(Math.max(16, rowHeight));

        resourcesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                addButton.setEnabled(!resourcesTable.getSelectionModel().isSelectionEmpty());
            }
        });
        
        addButton.setEnabled(!resourcesTable.getSelectionModel().isSelectionEmpty());
    }
    
    
    private void initAccesibility() {        
        addButton.getAccessibleContext().setAccessibleDescription(Util.getString("ACS_CTL_SelectResource"));//NOI18N
        addAllButton.getAccessibleContext().setAccessibleDescription(Util.getString("ACS_CTL_SelectResourceAll"));//NOI18N
        resourcesTable.getAccessibleContext().setAccessibleDescription(Util.getString("ACSD_resourcesTable"));//NOI18N
        resourcesTable.getAccessibleContext().setAccessibleName(Util.getString("ACSN_resourcesTable"));//NOI18N
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI initialization code">
    private void initComponents() {
        resourcesTable = new JTable();
        resourcesTable.setModel(tableModel);

        addAllButton = new JButton();
        Mnemonics.setLocalizedText(
                addAllButton,
                Util.getString("CTL_SelectResourceAll"));               //NOI18N
        addAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addAllButtonActionPerformed(evt);
            }
        });

        addButton = new JButton();
        Mnemonics.setLocalizedText(
                addButton,
                Util.getString("CTL_SelectResource"));                  //NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        Util.layoutSelectResourcePanel(
                this,
                getPanelDescription(),
                Util.getString("LBL_SelectedResources"),            //NOI18N
                resourcesTable,
                addAllButton,
                addButton);
    }// </editor-fold>

    private void addButtonActionPerformed(ActionEvent evt) {
        DataObject resource = selectResource();
        
        if (resource == null) {
            return;
        }

        int[] selectedRows = resourcesTable.getSelectedRows();

        // Feed data.
        for(int i=0; i<selectedRows.length; i++) {
            DataObject dataObject = (DataObject) resourcesTable.getValueAt(selectedRows[i], 0);

            sourceMap.put(dataObject, new SourceData(resource));
            
            tableModel.fireTableCellUpdated(selectedRows[i], 1);
        }

        descPanel.fireStateChanged();
    }

    private void addAllButtonActionPerformed(ActionEvent evt) {
        DataObject resource = selectResource();
        
        if (resource == null) {
            return;
        }

        // Feed data.
        for (int i = 0; i < resourcesTable.getRowCount(); i++) {
            DataObject dataObject = (DataObject) resourcesTable.getValueAt(i, 0);

            sourceMap.put(dataObject, new SourceData(resource));
            
            tableModel.fireTableCellUpdated(i, 1);
        }

        descPanel.fireStateChanged();
    }

    /** Helper method. Gets user selected resource. */
    private DataObject selectResource() {        
        FileObject fo = null;
        DataObject source = null;
        for (DataObject dobj : sourceMap.keySet()) {
            fo = dobj.getPrimaryFile();            
            source = dobj;
        }

        // Get i18n support for this source.
        I18nSupport support = null;
        try {
            support = FactoryRegistry.getFactory(source.getClass()).create(source);
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);            
        }
    
        ResourceHolder rh = support != null ? support.getResourceHolder() : null;
        
        DataObject template = null;
        try {
            template = rh != null ? rh.getTemplate(rh.getResourceClasses()[0]) : null;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
            
        return SelectorUtils.selectOrCreateBundle(fo, template, null);
    }
    
    // Variables declaration
    private JButton addAllButton;
    private JButton addButton;
    private JTextArea descTextArea;
    private JScrollPane jScrollPane1;
    private JLabel lblSelectedResources;
    private JTable resourcesTable;
    // End of variables declaration

    /** Table model for this class. */
    private class ResourceTableModel extends AbstractTableModel {
        
        /** Constructor. */
        public ResourceTableModel() {
        }
        
        
        /** Implements superclass abstract method. */
        public int getColumnCount() {
            return 2;
        }
        
        /** Implemenst superclass abstract method. */
        public int getRowCount() {
            return sourceMap.size();
        }
        
        /** Implements superclass abstract method. */
        public Object getValueAt(int rowIndex, int columnIndex) {

            if (columnIndex == 0) {
                return sourceMap.keySet().toArray()[rowIndex];
            } else { 
                SourceData[] values = new SourceData[0];
                SourceData value = sourceMap.values().toArray(values)[rowIndex];
                return value == null ? null : value.getResource();
            }
            
        }
        
        /** Overrides superclass method. 
         * @return DataObject.class */
        @Override
        public Class getColumnClass(int columnIndex) {
            return DataObject.class;
        }

        /** Overrides superclass method. */
        @Override
        public String getColumnName(int column) {
            String msgKey = (column == 0) ? "CTL_Source"                //NOI18N
                                          : "CTL_Resource";             //NOI18N
            return NbBundle.getMessage(ResourceWizardPanel.class, msgKey);
        }
    } // End of ResourceTableModel inner class.
    
    
    /** <code>WizardDescriptor.Panel</code> used for <code>ResourceChooserPanel</code>. 
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel */
    public static class Panel extends I18nWizardDescriptor.Panel
            implements AsynchronousValidatingPanel<I18nWizardDescriptor.Settings> {

        private static final String CARD_GUI = "gui";                   //NOI18N
        private static final String CARD_PROGRESS = "progress";         //NOI18N

        /** Cached component. */
        private transient ResourceWizardPanel resourcePanel;
        
        /** Indicates whether this panel is used in i18n test wizard or not. */
        private final boolean testWizard;

        /** */
        private volatile ProgressWizardPanel progressPanel;

        /** Constructs Panel for i18n wizard. */
        public Panel() {
            this(false);
        }

        /** Constructs panel for i18n wizard or i18n test wizard. */
        public Panel(boolean testWizard) {
            this.testWizard = testWizard;
        }
        
        
        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {
            JPanel panel = new JPanel(new CardLayout());

            // Accessibility
            panel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ResourceWizardPanel.class).getString("ACS_ResourceWizardPanel"));                 
            
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(1));

            String msgKey = testWizard ? "TXT_SelectTestResource"       //NOI18N
                                       : "TXT_SelectResource";          //NOI18N
            panel.setName(NbBundle.getMessage(ResourceWizardPanel.class, msgKey));
            panel.setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
            
            panel.add(getUI(), CARD_GUI);

            return panel;
        }

        /** Indicates if panel is valid. Overrides superclass method. */
        @Override
        public boolean isValid() {
            return !getUI().getSourceMap().containsValue(null);
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        @Override
        public void readSettings(I18nWizardDescriptor.Settings settings) {
	    super.readSettings(settings);
            getUI().setSourceMap(getMap());

            Container container = (Container) getComponent();
            ((CardLayout) container.getLayout()).show(container, CARD_GUI);
        }

        /** Stores settings at the end of panel show. Overrides superclass abstract method. */
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
            if (progressPanel == null) {
                progressPanel = new ProgressWizardPanel(false);
            }

            showProgressPanel(progressPanel);

            progressPanel.setMainText(NbBundle.getMessage(ResourceWizardPanel.class,
                                                          "TXT_Loading"));//NOI18N
            progressPanel.setMainProgress(0);
        }

        public void validate() throws WizardValidationException {
            assert !EventQueue.isDispatchThread();

            // Do search.
            Map<DataObject,SourceData> sourceMap = getUI().getSourceMap();
            Iterator<Map.Entry<DataObject,SourceData>> sourceIterator
                    = sourceMap.entrySet().iterator();

            // For each source perform the task.
            final String prefixLoading
                    = NbBundle.getMessage(ResourceWizardPanel.class,
                                          "TXT_Loading")                //NOI18N
                      + ' ';
            final String prefixSearchingIn
                    = NbBundle.getMessage(ResourceWizardPanel.class,
                                          "TXT_SearchingIn")            //NOI18N
                      + ' ';

            for (int i = 0; sourceIterator.hasNext(); i++) {
                Map.Entry<DataObject,SourceData> entry = sourceIterator.next();
                DataObject source = entry.getKey();
                FileObject fileObj = source.getPrimaryFile();
                String fileName = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)
                                  .getResourceName(fileObj, '.', false);

                progressPanel.setMainText(prefixLoading + fileName);

                // retrieve existing sourcedata -- will provide the resource for the new instance
                SourceData sourceData = entry.getValue();
                
                // prepare new sourcedata
                // Get i18n support for this source.
                I18nSupport support = sourceData.getSupport();
                if (support == null) {
                    try {
                        support = FactoryRegistry.getFactory(source.getClass()).create(source);
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        // Remove source from settings.
                        sourceIterator.remove();
                        continue;
                    }

                    sourceData = new SourceData(sourceData.getResource(), support);
                    entry.setValue(sourceData);
                }
                
                progressPanel.setMainText(prefixSearchingIn + fileName);
                
                HardCodedString[] foundStrings;
                
                if (testWizard) {
                    // Find all i18n-zied hard coded strings in the source.
                    foundStrings = support.getFinder().findAllI18nStrings();
                } else {
                    // Find all non-i18-ized hard coded strings in the source.
                    foundStrings = support.getFinder().findAllHardCodedStrings();
                }

                if (foundStrings == null) {
                    // Set empty map.
                    sourceData.setStringMap(Collections.<HardCodedString,I18nString>emptyMap());
                    continue;
                }

                Map<HardCodedString,I18nString> map
                        = new HashMap<HardCodedString,I18nString>(foundStrings.length); 

                // Put <hard coded string, i18n-string> pairs into map.
                for (HardCodedString hcString : foundStrings) {
                    if (testWizard && support.getResourceHolder().getValueForKey(hcString.getText())
                                      != null) {
                        continue;
                    }
                        map.put(hcString, support.getDefaultI18nString(hcString));
                }

                progressPanel.setMainProgress((int)((i+1)/(float)sourceMap.size() * 100));

                sourceData.setStringMap(map);
            } // End of outer for.
        }

        /** Helper method. Places progress panel for monitoring search. */
        private void showProgressPanel(ProgressWizardPanel progressPanel) {
            Container container = (Container) getComponent();
            container.add(progressPanel, CARD_PROGRESS);
            ((CardLayout) container.getLayout()).show(container, CARD_PROGRESS);
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            return new HelpCtx(testWizard ? I18nUtil.HELP_ID_TESTING
                                          : I18nUtil.HELP_ID_WIZARD);
        }
        
        private synchronized ResourceWizardPanel getUI() {
            if (resourcePanel == null) {
                resourcePanel = new ResourceWizardPanel(this, testWizard);
            }
            return resourcePanel;
        }

    } // End of nested Panel class.
    
}
