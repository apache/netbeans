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


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.FilteredNode;
import org.netbeans.modules.i18n.I18nUtil;

import org.openide.awt.Mnemonics;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.openide.util.ImageUtilities;
import org.netbeans.modules.i18n.SelectorUtils;
import org.openide.WizardDescriptor;

/**
 * First panel used in I18N (test) Wizard.
 *
 * @author  Peter Zavadsky
 * @see Panel
 */
final class SourceWizardPanel extends JPanel {

    /** Sources selected by user. */
    private final Map<DataObject,SourceData> sourceMap
            = Util.createWizardSourceMap();
    
    /** This component panel wizard descriptor.
     * @see org.openide.WizardDescriptor.Panel 
     * @see Panel */
    private final Panel descPanel;

    /**
     * Panel role true (test wizard) false (i18n) wizard
     */
    private boolean testRole = false;
    
    /** Creates new form SourceChooserPanel.
     * @param it's panel wizard descriptor */
    private SourceWizardPanel(Panel descPanel, boolean testRole) {
        this.descPanel = descPanel;
        this.testRole = testRole;
        
        initComponents();        

        initAccessibility ();
        
        setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
        
        initList();
        
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(0));
        
        if (testRole) {
            setName(Util.getString("TXT_SelecTestSources"));            //NOI18N
            HelpCtx.setHelpIDString(this, Util.HELP_ID_SELECTTESTSOURCES);
        } else {
            setName(Util.getString("TXT_SelectSources"));               //NOI18N
            HelpCtx.setHelpIDString(this, Util.HELP_ID_SELECTSOURCES);
        }        
    }
    

    /** Getter for <code>sources</code> property. */
    public Map<DataObject,SourceData> getSourceMap() {
        return sourceMap;
    }
    
    /** Setter for <code>sources</code> property. */
    public void setSourceMap(Map<DataObject,SourceData> sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        sourcesList.setListData(sourceMap.keySet().toArray());
        
        descPanel.fireStateChanged();
    }

    /**
     * Panel description depend of its container test or i18n role
     */
    private String getPanelDescription() {
        if (testRole == false)   {
            return Util.getString("MSG_SourcesPanel_desc");             //NOI18N
        } else {
            return Util.getString("MSG_SourcesPanel_test_desc");        //NOI18N
        }        
    }

    /**
     * Accessible panel description depends of its container test or i18n role
     */
    private String getAccessibleListDescription() {
        if (testRole == false)   {
            return Util.getString("ACSD_sourcesList");                  //NOI18N
        } else {
            return Util.getString("ACSD_sourcesList_test");             //NOI18N
        }        
    }
    
    
    /** 
     * List content drives remove button enableness.
     */
    private void initList() {
        sourcesList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent evt) {
                    removeButton.setEnabled(!sourcesList.isSelectionEmpty());
                }
            }
        );
        
        removeButton.setEnabled(!sourcesList.isSelectionEmpty());
    }
    
    private void initAccessibility() {        
        getAccessibleContext().setAccessibleDescription(getPanelDescription());
        
        addButton.setToolTipText(Util.getString("CTL_AddSource_desc")); //NOI18N
        
        removeButton.setToolTipText(Util.getString("CTL_RemoveSource_desc"));//NOI18N
        
        sourcesList.getAccessibleContext().setAccessibleName(Util.getString("ACSN_sourcesList"));//NOI18N
        sourcesList.getAccessibleContext().setAccessibleDescription(getAccessibleListDescription());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI initialization code">
    private void initComponents() {

        sourcesList = new JList();
        sourcesList.setCellRenderer(new DataObjectListCellRenderer());

        addButton = new JButton();
        Mnemonics.setLocalizedText(addButton,
                                   Util.getString("CTL_AddSource"));    //NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton = new JButton();
        Mnemonics.setLocalizedText(removeButton,
                                   Util.getString("CTL_RemoveSource")); //NOI18N
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        Util.layoutSelectResourcePanel(
                this,
                getPanelDescription(),
                Util.getString("LBL_SelectedSourcesToI18N"),            //NOI18N
                sourcesList,
                addButton,
                removeButton);
    }// </editor-fold>

    private void removeButtonActionPerformed(ActionEvent evt) {
        Object[] selected = sourcesList.getSelectedValues();
        
        for(int i=0; i<selected.length; i++) {
            sourceMap.remove(selected[i]);
        }

        sourcesList.setListData(sourceMap.keySet().toArray());
        
        descPanel.fireStateChanged();
    }

    private void addButtonActionPerformed(ActionEvent evt) {

        // take actual project from first data object

        Project prj = descPanel.getProject();
  
        // Selects source data objects which could be i18n-ized.
        try {
            FilteredNode.NodeFilter HIDDEN_FOLDERS_FILTER = new FilteredNode.NodeFilter() {

                public boolean acceptNode(Node node) {
                    if (node.getName().startsWith("."))
                        return false;
                    return true;
                }
            };
            Node[] selectedNodes= NodeOperation.getDefault().select(
                Util.getString("LBL_SelectSources"),                    //NOI18N
                Util.getString("LBL_Filesystems"),                      //NOI18N
                SelectorUtils.sourcesNode(prj, HIDDEN_FOLDERS_FILTER),
                new NodeAcceptor() {
                    public boolean acceptNodes(Node[] nodes) {
                        if (nodes == null || nodes.length == 0) {
                            return false;
                        }

                        for (Node node : nodes) {
                            // Has to be data object.
                            Object dataObject = node.getCookie(DataObject.class);
                            if (dataObject == null) {
                                return false;
                            }
                            // if it is folder and constains some our data object.
                            if (dataObject instanceof DataFolder) {
                                if (I18nUtil.containsAcceptedDataObject((DataFolder) dataObject)) {
                                    return true;
                                }
                            } else if (FactoryRegistry.hasFactory(dataObject.getClass())) {
                                // Has to have registered i18n factory for that data object class name.
                                return true;
                            }
                        }
                        
                        return false;
                    }                    
                }
            );
            
            for(int i=0; i<selectedNodes.length; i++) {
                DataObject dataObject = selectedNodes[i].getCookie(DataObject.class);

                if (dataObject instanceof DataFolder) {
                    // recursively add folder content
                    Iterator<DataObject> it = I18nUtil.getAcceptedDataObjects((DataFolder) dataObject).iterator();
                    while (it.hasNext()) {
                        Util.addSource(sourceMap, it.next());
                    }
                } else {
                    Util.addSource(sourceMap, dataObject);
                }
            }
            
            sourcesList.setListData(sourceMap.keySet().toArray());
           
            descPanel.fireStateChanged();
        } catch (UserCancelException uce) {
           // ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, uce);
             // nobody is interested in the message
        }
    }

    private JButton addButton;
    private JLabel lblSelectedSources;
    private JButton removeButton;
    private JList sourcesList;


    /** List cell rendrerer which uses data object as values. */
    public static class DataObjectListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
        JList list,
        Object value,            // value to display
        int index,               // cell index
        boolean isSelected,      // is the cell selected
        boolean cellHasFocus)    // the list and the cell have the focus
        {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            DataObject dataObject = (DataObject)value;

            if (dataObject != null) {
                ClassPath cp = ClassPath.getClassPath(dataObject.getPrimaryFile(), ClassPath.SOURCE );
                                
                // Handle Bug 200268 (http://netbeans.org/bugzilla/show_bug.cgi?id=200268)
                if(cp == null) {
                    label.setText(""); // NOI18N
                } else {
                    label.setText(cp.getResourceName(dataObject.getPrimaryFile(), '.', false )); // NOI18N
                }
                label.setIcon(ImageUtilities.image2Icon(dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                label.setText(""); // NOI18N
                label.setIcon(null);
            }

            return label;
        }
    }
    
        
    
    /** <code>WizardDescriptor.Panel</code> used for <code>SourceChooserPanel</code>.
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel */
    public static class Panel extends I18nWizardDescriptor.Panel {

        /** Test wizard flag. */
        private final boolean testWizard;
        
        
        /** Constructor for i18n wizard. */
        public Panel() {
            this(false);
        }
        
        /** Constructor for specified i18n wizard. */
        public Panel(boolean testWizard) {
            this.testWizard = testWizard;
        }
        
        
        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {                                    
            Component component = new SourceWizardPanel(this, testWizard);            
            
            return component;
        }

        /** Gets if panel is valid. Overrides superclass method. */
        @Override
        public boolean isValid() {
            return !((SourceWizardPanel) getComponent()).getSourceMap().isEmpty();
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        @Override
        public void readSettings(I18nWizardDescriptor.Settings settings) {
	  super.readSettings(settings);
	  ((SourceWizardPanel) getComponent()).setSourceMap(getMap());
        }

        /** Stores settings at the end of panel show. Overrides superclass method. */
        @Override
        public void storeSettings(I18nWizardDescriptor.Settings settings) {
	    super.storeSettings(settings);
	    super.storeSettings(settings);
            // Update sources.
            getMap().clear();
            getMap().putAll(((SourceWizardPanel) getComponent()).getSourceMap());
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            return new HelpCtx(testWizard
                               ? I18nUtil.HELP_ID_TESTING
                               : I18nUtil.HELP_ID_WIZARD);
        }

    } // End of nested Panel class.
    
}
