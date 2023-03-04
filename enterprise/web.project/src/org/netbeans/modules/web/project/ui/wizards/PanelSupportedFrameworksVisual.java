/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.ExtenderController.Properties;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;

public class PanelSupportedFrameworksVisual extends JPanel implements HelpCtx.Provider, TableModelListener, ListSelectionListener, ChangeListener {
    
    /** All available web extensions */
    public static final int ALL_FRAMEWORKS = 0;
    
    /** Web extensions used in the project */
    public static final int USED_FRAMEWORKS = 1;
    
    /** Web extensions which are not used in the project */
    public static final int UNUSED_FRAMEWORKS = 2;
    
    private List ignoredFrameworks;
    private Map<WebFrameworkProvider, WebModuleExtender> extenders = new IdentityHashMap<WebFrameworkProvider, WebModuleExtender>();
    private FrameworksTableModel model;
    private PanelSupportedFrameworks panel;
    private ExtenderController controller;
    
    /** Creates new form PanelInitProject
     * @param project the web project; if it is null, all available web extensions will be shown
     * @param filter one of the options <code>ALL_FRAMEWORKS</code>, <code>USED_FRAMEWORKS</code>, <code>UNUSED_FRAMEWORKS</code>
     * @param ignoredFrameworks the list of frameworks to be ignored when creating list; null is allowed
     */
    public PanelSupportedFrameworksVisual(PanelSupportedFrameworks panel, ExtenderController controller, WebProject project, int filter, List ignoredFrameworks) {
        this.panel = panel;
        this.controller = controller;
        this.ignoredFrameworks = ignoredFrameworks;
        initComponents();

        model = new FrameworksTableModel();
        jTableFrameworks.setModel(model);
        jTableFrameworks.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none"); //NOI18N
        createFrameworksList(project, filter);

        FrameworksTableCellRenderer renderer = new FrameworksTableCellRenderer();
        renderer.setBooleanRenderer(jTableFrameworks.getDefaultRenderer(Boolean.class));
        jTableFrameworks.setDefaultRenderer(WebFrameworkProvider.class, renderer);
        jTableFrameworks.setDefaultRenderer(Boolean.class, renderer);
        initTableVisualProperties(jTableFrameworks);

        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "ACS_NWP2_Frameworks_A11YDesc"));  // NOI18N        

        // Provide a name in the title bar.
        setName(NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "LBL_NWP2_Frameworks")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "TXT_NewWebApp")); //NOI18N
    }

    private void initTableVisualProperties(JTable table) {
        table.getModel().addTableModelListener(this);
        
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().addListSelectionListener(this);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.setTableHeader(null);
        
        table.setRowHeight(jTableFrameworks.getRowHeight() + 4);        
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));        
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        
        table.getColumnModel().getColumn(0).setMaxWidth(30);
    }

    private void createFrameworksList(WebProject project, int filter) {
        List frameworks = WebFrameworks.getFrameworks();
        
        if (project == null || filter == ALL_FRAMEWORKS) {
            for (int i = 0; i < frameworks.size(); i++) {
                addFrameworkToModel((WebFrameworkProvider) frameworks.get(i));
            }
        } else if (filter == USED_FRAMEWORKS) {
            for (int i = 0; i < frameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
                if (framework.isInWebModule(project.getAPIWebModule())) {
                    addFrameworkToModel(framework);
                }
            }
        } else if (filter == UNUSED_FRAMEWORKS) {
            for (int i = 0; i < frameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
                if (!framework.isInWebModule(project.getAPIWebModule())) {
                    addFrameworkToModel(framework);
                }
            }
        }
    }
    
    private void addFrameworkToModel(WebFrameworkProvider framework) {
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        if (ignoredFrameworks == null || !ignoredFrameworks.contains(framework))
            model.addItem(new FrameworkModelItem(framework));
    }
    
    private void createExtenders() {
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            WebFrameworkProvider framework = item.getFramework();
            WebModuleExtender extender = framework.createWebModuleExtender(null, controller);
            if (extender != null) {
                extender.addChangeListener(this);
                extenders.put(framework, extender);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFrameworks = new javax.swing.JTable();
        jLabelConfig = new javax.swing.JLabel();
        jPanelConfig = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(500, 340));
        setRequestFocusEnabled(false);

        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "MNE_Frameworks").charAt(0));
        jLabel1.setLabelFor(jTableFrameworks);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "LBL_NWP2_Select_Frameworks")); // NOI18N

        jScrollPane1.setMinimumSize(new java.awt.Dimension(22, 70));
        jScrollPane1.setOpaque(false);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(453, 70));

        jTableFrameworks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTableFrameworks.setOpaque(false);
        jScrollPane1.setViewportView(jTableFrameworks);

        jLabelConfig.setLabelFor(jPanelConfig);

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(jPanelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
            .addComponent(jLabelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "ACS_LBL_NWP2_FrameworksTable_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableFrameworks;
    // End of variables declaration//GEN-END:variables

    boolean valid(WizardDescriptor wizardDescriptor) {
        setErrorMessage(wizardDescriptor, null);
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getItem(i).isSelected()) {
                FrameworkModelItem item = model.getItem(i);
                WebModuleExtender extender = (WebModuleExtender) extenders.get(item.getFramework());
                if (extender != null && !extender.isValid()) {
                    String message = (String) controller.getProperties().getProperty(WizardDescriptor.PROP_INFO_MESSAGE);
                    if (controller.getErrorMessage()==null && message != null) {
                        setInfoMessage(wizardDescriptor, message);
                    } else {
                        setErrorMessage(wizardDescriptor, controller.getErrorMessage());
                    }
                    return false;
                } else if (extender != null && extender.isValid()) {
                    String message = (String) controller.getProperties().getProperty(WizardDescriptor.PROP_INFO_MESSAGE);
                    if (controller.getErrorMessage()==null && message != null) {
                        setInfoMessage(wizardDescriptor, message);
                    }
                }
            }
        }

        return true;
    }
    
    private void setErrorMessage(WizardDescriptor wizardDescriptor, String errorMessage) {
        if (errorMessage == null || errorMessage.length() == 0) {
            errorMessage = " "; // NOI18N
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N
    }

    private void setInfoMessage(WizardDescriptor wizardDescriptor, String infoMessage) {
        if (infoMessage == null || infoMessage.length() == 0) {
            infoMessage = " "; // NOI18N
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, infoMessage); // NOI18N
    }
    
    void read(WizardDescriptor settings) {
        Properties properties = controller.getProperties();
        properties.setProperty("name", (String) settings.getProperty("name")); // NOI18N

        // FIXME I left string here for compatibility reasons (frameworks)
        Profile profile = (Profile) settings.getProperty(ProjectServerWizardPanel.J2EE_LEVEL);
        if (profile != null) {
            properties.setProperty(ProjectServerWizardPanel.J2EE_LEVEL, profile.toPropertiesString());
        }
        properties.setProperty("serverInstanceID", (String) settings.getProperty("serverInstanceID")); // NOI18N
        properties.setProperty("setSourceLevel", (String) settings.getProperty("setSourceLevel")); // NOI18N

        if (extenders.size() == 0) {
            // Initializing the config panels lazily; should not be done in getComponent(),
            // as that is called too early, even before the wizard properties have been set,
            // thus breaking impls of getComponent() relying on those properties
            createExtenders();
        }
        
        // In the ideal case this should be called before createExtenders();
        // calling it afterwards causes ConfigurationPanel.getComponent() to be called
        // before ConfigurationPanel.update(), which does not make sense (it effectively
        // obtains an empty component first and updates it with data afterwards.
        // Unfortunately existing panels expect to be called that way, so we are stuck with this for now
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            WebModuleExtender extender = (WebModuleExtender) extenders.get(item.getFramework());
            if (extender != null) {
                extender.update();
            }
        }
    }

    void store(WizardDescriptor settings) {
//        if ( bottomPanel != null ) {
//            bottomPanel.storeSettings( settings );
//        }
        
        settings.putProperty(WizardProperties.EXTENDERS, getSelectedExtenders());    //NOI18N
        settings.putProperty(WizardProperties.FRAMEWORK_NAMES, getSelectedFrameworkNames());
    }

    public List getSelectedExtenders() {
        List<WebModuleExtender> selectedExtenders = new LinkedList<WebModuleExtender>();
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            if (item.isSelected()) {
                WebModuleExtender extender = (WebModuleExtender) extenders.get(item.getFramework());
                if (extender != null) {
                    selectedExtenders.add(extender);
                }
            }
        }
        
        return selectedExtenders;
    }
    
    private List<String> getSelectedFrameworkNames() {
        List<String> selectedFrameworkNames = new LinkedList<String>();
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            if (item.isSelected()) {
                String name = item.getFramework().getName();
                if (name != null) {
                    selectedFrameworkNames.add(name);
                }
            }
        }
        
        return selectedFrameworkNames;
    }
    
    public Component[] getConfigComponents() {
        return new Component[] {jLabelConfig, jPanelConfig};
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        if (jPanelConfig.getComponentCount()>0){
            for (int i = 0; i < jPanelConfig.getComponentCount(); i++)
                if (jPanelConfig.getComponent(i) instanceof  HelpCtx.Provider)
                    return ((HelpCtx.Provider)jPanelConfig.getComponent(i)).getHelpCtx();
        }
        return null;
    }
    
    public void tableChanged(TableModelEvent e) {
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        if (jTableFrameworks.getSelectedRow() == -1) {
            return;
        }
        FrameworkModelItem item = model.getItem(jTableFrameworks.getSelectedRow());
        WebFrameworkProvider framework = item.getFramework();
        setConfigPanel(framework, item);
    }
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        if (jTableFrameworks.getSelectedRow() == -1) {
            return;
        }
        FrameworkModelItem item = model.getItem(jTableFrameworks.getSelectedRow());
        WebFrameworkProvider framework = item.getFramework();
        setConfigPanel(framework, item);
    }
    
    private void setConfigPanel(WebFrameworkProvider framework, FrameworkModelItem item) {
        if (extenders.get(framework) != null) {
            String message = MessageFormat.format(NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "LBL_NWP2_ConfigureFramework"), new Object[] {framework.getName()}); //NOI18N
            jLabelConfig.setText(message);
//            jLabelConfig.setEnabled(item.isSelected().booleanValue());
            
            jPanelConfig.removeAll();

            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;

            JComponent panelComponent = ((WebModuleExtender) extenders.get(framework)).getComponent();
            jPanelConfig.add(panelComponent, gridBagConstraints);
            
            jLabelConfig.setEnabled(item.isSelected());
            enableComponents(panelComponent, item.isSelected());
            ((WebModuleExtender) extenders.get(framework)).update();
            jPanelConfig.revalidate();
            jPanelConfig.repaint();
        } else {
            jLabelConfig.setText(""); //NOI18N
            jPanelConfig.removeAll();
            jPanelConfig.repaint();
            jPanelConfig.revalidate();
        }
        
        if (panel != null)
            panel.fireChangeEvent();
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if (panel != null)
            panel.fireChangeEvent();
    }
    
    private void enableComponents(Container root, boolean enabled) {
        root.setEnabled(enabled);
        for (int i = 0; i < root.getComponentCount(); i++) {
            Component child = root.getComponent(i);
            if (child instanceof Container) {
                enableComponents((Container)child, enabled);
            } else {
                child.setEnabled(enabled);
            }
        }
    }
    
    public static class FrameworksTableCellRenderer extends DefaultTableCellRenderer {
        private TableCellRenderer booleanRenderer;
        
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            if (value instanceof WebFrameworkProvider) {
                WebFrameworkProvider item = (WebFrameworkProvider) value;
                Component comp = super.getTableCellRendererComponent(table, item.getName(), isSelected, false, row, column);
                if (comp instanceof JComponent) {
                    ((JComponent)comp).setOpaque(isSelected);
                }
                return comp;
            } else {
                if (value instanceof Boolean && booleanRenderer != null)
                    return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                else
                    return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
        }
        
        public void setBooleanRenderer(TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }
    }

    /** 
     * Implements a TableModel.
     */
    public static final class FrameworksTableModel extends AbstractTableModel {
        private DefaultListModel<FrameworkModelItem> model;
        
        public FrameworksTableModel() {
            model = new DefaultListModel<>();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public int getRowCount() {
            return model.size();
        }
        
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return Boolean.class;
            else
                return WebFrameworkProvider.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0);
        }
        
        public Object getValueAt(int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0: return item.isSelected();
                case 1: return item.getFramework();
            }
            return "";
        }
        
        public void setValueAt(Object value, int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0: item.setSelected((Boolean) value);break;
                case 1: item.setFramework((WebFrameworkProvider) value);break;
                default: assert false;
            }
            fireTableCellUpdated(row, column);
        }
        
        private FrameworkModelItem getItem(int index) {
            return (FrameworkModelItem) model.get(index);
        }
        
        public void addItem(FrameworkModelItem item){
            model.addElement(item);
        }
    }

    private static final class FrameworkModelItem {
        private WebFrameworkProvider framework;
        private Boolean selected;
        
        /** Creates a new instance of BeanFormProperty */
        public FrameworkModelItem(WebFrameworkProvider framework) {
            this.setFramework(framework);
            setSelected(Boolean.FALSE);
        }

        public WebFrameworkProvider getFramework() {
            return framework;
        }

        public void setFramework(WebFrameworkProvider framework) {
            this.framework = framework;
        }

        public Boolean isSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

    }
}
