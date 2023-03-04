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

/*
 * AWTComponentBreakpointPanel.java
 *
 * Created on Aug 19, 2011, 8:10:58 AM
 */
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ActionsPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ConditionsPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ControllerProvider;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.ComponentBreakpoint.ComponentDescription;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
public class ComponentBreakpointPanel extends javax.swing.JPanel implements ControllerProvider {
    
    private static final String         HELP_ID = "NetbeansDebuggerBreakpointComponentJPDA"; // NOI18N
    private ComponentBreakpoint         breakpoint;
    private LineBreakpoint              fakeActionsBP;
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private CBController                controller = new CBController();
    private boolean                     createBreakpoint = false;
    
    private static ComponentBreakpoint createBreakpoint () {
        ComponentBreakpoint cb = null;
        ComponentBreakpoint.ComponentDescription componentDescription = null;
        ScreenshotUIManager activeScreenshotManager = ScreenshotUIManager.getActive();
        if (activeScreenshotManager != null) {
            ComponentInfo ci = activeScreenshotManager.getSelectedComponent();
            if (ci instanceof JavaComponentInfo) {
                componentDescription = new ComponentBreakpoint.ComponentDescription(
                        ci,
                        ((JavaComponentInfo) ci).getThread().getDebugger(),
                        ((JavaComponentInfo) ci).getComponent());
            }

            if (componentDescription == null) {
                componentDescription = new ComponentBreakpoint.ComponentDescription("");
            }
            
            cb = (ci instanceof RemoteAWTScreenshot.AWTComponentInfo) ? new AWTComponentBreakpoint(componentDescription) : new FXComponentBreakpoint(componentDescription);
        }
        /*cb.setPrintText (
            NbBundle.getBundle (LineBreakpointPanel.class).getString 
                ("CTL_Line_Breakpoint_Print_Text")
        );*/
        return cb;
    }
    

    public ComponentBreakpointPanel() {
        this (createBreakpoint (), true);
    }
    
    /** Creates new form AWTComponentBreakpointPanel */
    public ComponentBreakpointPanel(ComponentBreakpoint cb) {
        this(cb, false);
    }
    
    public ComponentBreakpointPanel(ComponentBreakpoint cb, boolean createBreakpoint) {
        this.breakpoint = cb;
        this.createBreakpoint = createBreakpoint;
        initComponents();
        int type;
        int supportedTypes;
        if (cb != null) {
            type = cb.getType();
            supportedTypes = cb.supportedTypes();
        } else {
            type = 0;
            supportedTypes = 0;
        }
        String componentName;
        if (cb != null && cb.getComponent() != null && cb.getComponent().getComponentInfo() != null) {
            componentName = cb.getComponent().getComponentInfo().getDisplayName();
        } else {
            componentName = NbBundle.getMessage(ComponentBreakpointPanel.class, "NoComponentSelected");
        }
        componentTextField.setText(componentName);
        addRemoveCheckBox.setSelected((type & AWTComponentBreakpoint.TYPE_ADD) != 0 || (type & AWTComponentBreakpoint.TYPE_REMOVE) != 0);
        addRemoveCheckBox.setVisible(((ComponentBreakpoint.TYPE_ADD | ComponentBreakpoint.TYPE_REMOVE) & supportedTypes) > 0);
        showHideCheckBox.setSelected((type & AWTComponentBreakpoint.TYPE_SHOW) != 0 || (type & AWTComponentBreakpoint.TYPE_HIDE) != 0);
        showHideCheckBox.setVisible(((ComponentBreakpoint.TYPE_SHOW | ComponentBreakpoint.TYPE_HIDE) & supportedTypes) > 0);
        repaintCheckBox.setSelected((type & AWTComponentBreakpoint.TYPE_REPAINT) != 0);
        repaintCheckBox.setVisible(((ComponentBreakpoint.TYPE_REPAINT) & supportedTypes) > 0);
        conditionsPanel = new ConditionsPanel(HELP_ID);
        conditionsPanel.setupConditionPaneContext();
        conditionsPanel.showClassFilter(false);
        if (cb != null) {
            conditionsPanel.setCondition(cb.getCondition());
            conditionsPanel.setHitCountFilteringStyle(cb.getHitCountFilteringStyle());
            conditionsPanel.setHitCount(cb.getHitCountFilter());
        }
        cPanel.add(conditionsPanel, "Center");  // NOI18N
        
        fakeActionsBP = LineBreakpoint.create("", 0);
        fakeActionsBP.setPrintText (
            NbBundle.getBundle (ComponentBreakpointPanel.class).getString 
                ("CTL_Component_Breakpoint_Print_Text")
        );

        actionsPanel = new ActionsPanel (fakeActionsBP);
        aPanel.add (actionsPanel, "Center");  // NOI18N

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.checkValid();
            }
        });
    }

    @Override
    public Controller getController() {
        return controller;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sPanel = new javax.swing.JPanel();
        componentLabel = new javax.swing.JLabel();
        componentTextField = new javax.swing.JTextField();
        componentActionLabel = new javax.swing.JLabel();
        addRemoveCheckBox = new javax.swing.JCheckBox();
        showHideCheckBox = new javax.swing.JCheckBox();
        repaintCheckBox = new javax.swing.JCheckBox();
        cPanel = new javax.swing.JPanel();
        aPanel = new javax.swing.JPanel();
        pushPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        sPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "TTL_ComponentBreakpointSettings"))); // NOI18N

        componentLabel.setText(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "ComponentBreakpointPanel.componentLabel.text")); // NOI18N

        componentTextField.setEditable(false);
        componentTextField.setText(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "ComponentBreakpointPanel.componentTextField.text")); // NOI18N

        componentActionLabel.setText(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "ComponentBreakpointPanel.componentActionLabel.text")); // NOI18N

        addRemoveCheckBox.setText(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "ComponentBreakpointPanel.addRemoveCheckBox.text")); // NOI18N

        showHideCheckBox.setText(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "ComponentBreakpointPanel.showHideCheckBox.text")); // NOI18N

        repaintCheckBox.setText(org.openide.util.NbBundle.getMessage(ComponentBreakpointPanel.class, "ComponentBreakpointPanel.repaintCheckBox.text")); // NOI18N

        javax.swing.GroupLayout sPanelLayout = new javax.swing.GroupLayout(sPanel);
        sPanel.setLayout(sPanelLayout);
        sPanelLayout.setHorizontalGroup(
            sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sPanelLayout.createSequentialGroup()
                .addComponent(componentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(componentTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
            .addGroup(sPanelLayout.createSequentialGroup()
                .addComponent(componentActionLabel)
                .addContainerGap())
            .addGroup(sPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(showHideCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRemoveCheckBox, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(173, Short.MAX_VALUE))
            .addGroup(sPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(repaintCheckBox)
                .addContainerGap(289, Short.MAX_VALUE))
        );
        sPanelLayout.setVerticalGroup(
            sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sPanelLayout.createSequentialGroup()
                .addGroup(sPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(componentLabel)
                    .addComponent(componentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(componentActionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addRemoveCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showHideCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(repaintCheckBox))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(sPanel, gridBagConstraints);

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(cPanel, gridBagConstraints);

        aPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(aPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pushPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aPanel;
    private javax.swing.JCheckBox addRemoveCheckBox;
    private javax.swing.JPanel cPanel;
    private javax.swing.JLabel componentActionLabel;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JTextField componentTextField;
    private javax.swing.JPanel pushPanel;
    private javax.swing.JCheckBox repaintCheckBox;
    private javax.swing.JPanel sPanel;
    private javax.swing.JCheckBox showHideCheckBox;
    // End of variables declaration//GEN-END:variables

    private class CBController implements Controller {

        private boolean valid;
        private String errMsg = null;

        @Override
        public boolean ok() {
            if (!valid) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errMsg));
                return false;
            }
            actionsPanel.ok ();
            int type =
                    (addRemoveCheckBox.isSelected() ? (AWTComponentBreakpoint.TYPE_ADD | AWTComponentBreakpoint.TYPE_REMOVE) : 0) |
                    (showHideCheckBox.isSelected() ? (AWTComponentBreakpoint.TYPE_SHOW | AWTComponentBreakpoint.TYPE_HIDE) : 0) |
                    (repaintCheckBox.isSelected() ? AWTComponentBreakpoint.TYPE_REPAINT : 0);
            breakpoint.setType(type);
            breakpoint.setCondition (conditionsPanel.getCondition());
            breakpoint.setHitCountFilter(conditionsPanel.getHitCount(), conditionsPanel.getHitCountFilteringStyle());
            breakpoint.setSuspend(fakeActionsBP.getSuspend());
            breakpoint.setPrintText(fakeActionsBP.getPrintText());
            if (createBreakpoint)
                DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
            return true;
        }

        @Override
        public boolean cancel() {
            return true;
        }

        private void setValid(boolean valid) {
            this.valid = valid;
            firePropertyChange(PROP_VALID, !valid, valid);
        }

        private void checkValid() {
            if (breakpoint != null) {
                ComponentDescription component = breakpoint.getComponent();
                if (component != null && component.getComponentInfo() != null) {
                    setValid(true);
                    return ;
                }
            }
            setErrorMessage(NbBundle.getMessage(ComponentBreakpointPanel.class, "MSG_No_Component_Spec"));
            setValid(false);
        }
        
        private void setErrorMessage(String msg) {
            errMsg = msg;
            firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, msg);
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            ComponentBreakpointPanel.this.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            ComponentBreakpointPanel.this.removePropertyChangeListener(l);
        }


    }
}
