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

package org.netbeans.modules.apisupport.project.ui.branding;

import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import org.openide.util.NbBundle;

/**
 * Represents <em>Window System parameters</em> panel in branding editor.
 *
 * @author Radek Matous, S. Aubrecht
 */
public class WindowSystemBrandingPanel extends AbstractBrandingPanel {
    
    public WindowSystemBrandingPanel(BrandingModel model) {
        super(NbBundle.getMessage(BasicBrandingPanel.class, "LBL_WindowSystemTab"), model); //NOI18N
        
        initComponents();
        refresh();
        enableDisableComponents();
        
        ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setModified();
            }
        };
        cbEnableDnd.addItemListener(listener);
        cbEnableEditorClosing.addItemListener(listener);
        cbEnableFloating.addItemListener(listener);
        cbEnableMaximization.addItemListener(listener);
        cbEnableMinimumSize.addItemListener(listener);
        cbEnableResizing.addItemListener(listener);
        cbEnableSliding.addItemListener(listener);
        cbEnableViewClosing.addItemListener(listener);
        cbEnableAutoSlideInMinimizedMode.addItemListener( listener );
        cbEnableEditorModeDnD.addItemListener( listener );
        cbEnableEditorModeUndocking.addItemListener( listener );
        cbEnableModeClosing.addItemListener( listener );
        cbEnableModeSliding.addItemListener( listener );
        cbEnableViewModeDnD.addItemListener( listener );
        cbEnableViewModeUndocking.addItemListener( listener );
    }
    
    
    @Override
    public void store() {
        BrandingModel branding = getBranding();
        
        SplashUISupport.setValue(branding.getWsEnableClosingEditors(), Boolean.toString(cbEnableEditorClosing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableClosingViews(), Boolean.toString(cbEnableViewClosing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableDragAndDrop(), Boolean.toString(cbEnableDnd.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableFloating(), Boolean.toString(cbEnableFloating.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableMaximization(), Boolean.toString(cbEnableMaximization.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableMinimumSize(), Boolean.toString(cbEnableMinimumSize.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableResizing(), Boolean.toString(cbEnableResizing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableSliding(), Boolean.toString(cbEnableSliding.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableAutoSlideInMinimizedMode(), Boolean.toString(cbEnableAutoSlideInMinimizedMode.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableEditorModeDnD(), Boolean.toString(cbEnableEditorModeDnD.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableEditorModeUndocking(), Boolean.toString(cbEnableEditorModeUndocking.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableModeClosing(), Boolean.toString(cbEnableModeClosing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableModeSliding(), Boolean.toString(cbEnableModeSliding.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableViewModeDnD(), Boolean.toString(cbEnableViewModeDnD.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableViewModeUndocking(), Boolean.toString(cbEnableViewModeUndocking.isSelected()));
    }
    
    
    void refresh() {
        BrandingModel branding = getBranding();
        
        cbEnableDnd.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableDragAndDrop()));
        cbEnableEditorClosing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableClosingEditors()));
        cbEnableFloating.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableFloating()));
        cbEnableMaximization.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableMaximization()));
        cbEnableMinimumSize.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableMinimumSize()));
        cbEnableResizing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableResizing()));
        cbEnableSliding.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableSliding()));
        cbEnableViewClosing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableClosingViews()));
        cbEnableAutoSlideInMinimizedMode.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableAutoSlideInMinimizedMode()));
        cbEnableEditorModeDnD.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableEditorModeDnD()));
        cbEnableEditorModeUndocking.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableEditorModeUndocking()));
        cbEnableModeClosing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableModeClosing()));
        cbEnableModeSliding.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableModeSliding()));
        cbEnableViewModeDnD.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableViewModeDnD()));
        cbEnableViewModeUndocking.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableViewModeUndocking()));
        
        enableDisableComponents();
        
    }
    
    private void enableDisableComponents() {
        final BrandingModel branding = getBranding();
        cbEnableDnd.setEnabled(branding.isBrandingEnabled());
        cbEnableEditorClosing.setEnabled(branding.isBrandingEnabled());
        cbEnableFloating.setEnabled(branding.isBrandingEnabled());
        cbEnableMinimumSize.setEnabled(branding.isBrandingEnabled());
        cbEnableResizing.setEnabled(branding.isBrandingEnabled());
        cbEnableSliding.setEnabled(branding.isBrandingEnabled());
        cbEnableViewClosing.setEnabled(branding.isBrandingEnabled());
        cbEnableMaximization.setEnabled(branding.isBrandingEnabled());
        cbEnableAutoSlideInMinimizedMode.setEnabled(branding.isBrandingEnabled());
        cbEnableEditorModeDnD.setEnabled(branding.isBrandingEnabled());
        cbEnableEditorModeUndocking.setEnabled(branding.isBrandingEnabled());
        cbEnableModeClosing.setEnabled(branding.isBrandingEnabled());
        cbEnableModeSliding.setEnabled(branding.isBrandingEnabled());
        cbEnableViewModeDnD.setEnabled(branding.isBrandingEnabled());
        cbEnableViewModeUndocking.setEnabled(branding.isBrandingEnabled());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbEnableDnd = new javax.swing.JCheckBox();
        cbEnableFloating = new javax.swing.JCheckBox();
        cbEnableSliding = new javax.swing.JCheckBox();
        cbEnableViewClosing = new javax.swing.JCheckBox();
        cbEnableEditorClosing = new javax.swing.JCheckBox();
        cbEnableResizing = new javax.swing.JCheckBox();
        cbEnableMinimumSize = new javax.swing.JCheckBox();
        cbEnableMaximization = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        cbEnableModeSliding = new javax.swing.JCheckBox();
        cbEnableViewModeUndocking = new javax.swing.JCheckBox();
        cbEnableEditorModeUndocking = new javax.swing.JCheckBox();
        cbEnableViewModeDnD = new javax.swing.JCheckBox();
        cbEnableEditorModeDnD = new javax.swing.JCheckBox();
        cbEnableModeClosing = new javax.swing.JCheckBox();
        cbEnableAutoSlideInMinimizedMode = new javax.swing.JCheckBox();

        cbEnableDnd.setMnemonic('D');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableDnd, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableDnD")); // NOI18N

        cbEnableFloating.setMnemonic('F');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableFloating, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableFloating")); // NOI18N

        cbEnableSliding.setMnemonic('S');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableSliding, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableSliding")); // NOI18N

        cbEnableViewClosing.setMnemonic('N');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableViewClosing, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableViewClosing")); // NOI18N

        cbEnableEditorClosing.setMnemonic('C');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableEditorClosing, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableEditorClosing")); // NOI18N

        cbEnableResizing.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableResizing, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableResizing")); // NOI18N

        cbEnableMinimumSize.setMnemonic('E');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMinimumSize, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableMinimumSize")); // NOI18N

        cbEnableMaximization.setMnemonic('M');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMaximization, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableMaximization")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableModeSliding, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableModeSliding.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableViewModeUndocking, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableViewModeUndocking.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableEditorModeUndocking, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableEditorModeUndocking.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableViewModeDnD, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableViewModeDnD.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableEditorModeDnD, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableEditorModeDnD.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableModeClosing, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableModeClosing.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbEnableAutoSlideInMinimizedMode, NbBundle.getMessage(WindowSystemBrandingPanel.class, "WindowSystemBrandingPanel.cbEnableAutoSlideInMinimizedMode.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbEnableAutoSlideInMinimizedMode)
                    .addComponent(cbEnableModeClosing)
                    .addComponent(cbEnableEditorModeDnD)
                    .addComponent(cbEnableViewModeDnD)
                    .addComponent(cbEnableEditorModeUndocking)
                    .addComponent(cbEnableViewModeUndocking)
                    .addComponent(cbEnableModeSliding)
                    .addComponent(cbEnableEditorClosing)
                    .addComponent(cbEnableMinimumSize)
                    .addComponent(cbEnableFloating)
                    .addComponent(cbEnableSliding)
                    .addComponent(cbEnableMaximization)
                    .addComponent(cbEnableViewClosing)
                    .addComponent(cbEnableResizing)
                    .addComponent(cbEnableDnd))
                .addContainerGap(127, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbEnableDnd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableViewModeDnD)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableEditorModeDnD)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableFloating)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableViewModeUndocking)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableEditorModeUndocking)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableSliding)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableModeSliding)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableAutoSlideInMinimizedMode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableMaximization)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableViewClosing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableEditorClosing, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableModeClosing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableResizing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableMinimumSize)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbEnableDnd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableDnD")); // NOI18N
        cbEnableFloating.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableFloating")); // NOI18N
        cbEnableSliding.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.cbEnableSliding.AccessibleContext.accessibleName")); // NOI18N
        cbEnableSliding.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableSliding")); // NOI18N
        cbEnableViewClosing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableViewClosing")); // NOI18N
        cbEnableEditorClosing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableEditorClosing")); // NOI18N
        cbEnableResizing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableResizing")); // NOI18N
        cbEnableMinimumSize.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableMinimumSize")); // NOI18N
        cbEnableMaximization.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.cbEnableMaximization.AccessibleContext.accessibleName")); // NOI18N
        cbEnableMaximization.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableMaximization")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbEnableAutoSlideInMinimizedMode;
    private javax.swing.JCheckBox cbEnableDnd;
    private javax.swing.JCheckBox cbEnableEditorClosing;
    private javax.swing.JCheckBox cbEnableEditorModeDnD;
    private javax.swing.JCheckBox cbEnableEditorModeUndocking;
    private javax.swing.JCheckBox cbEnableFloating;
    private javax.swing.JCheckBox cbEnableMaximization;
    private javax.swing.JCheckBox cbEnableMinimumSize;
    private javax.swing.JCheckBox cbEnableModeClosing;
    private javax.swing.JCheckBox cbEnableModeSliding;
    private javax.swing.JCheckBox cbEnableResizing;
    private javax.swing.JCheckBox cbEnableSliding;
    private javax.swing.JCheckBox cbEnableViewClosing;
    private javax.swing.JCheckBox cbEnableViewModeDnD;
    private javax.swing.JCheckBox cbEnableViewModeUndocking;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
