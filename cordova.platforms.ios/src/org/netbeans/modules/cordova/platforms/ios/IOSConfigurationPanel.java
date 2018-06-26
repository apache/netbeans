/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.ios;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.cordova.platforms.spi.SDK;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class IOSConfigurationPanel extends javax.swing.JPanel {

    private PropertyProvider config;
    final RequestProcessor RP = new RequestProcessor(IOSConfigurationPanel.class);
    
    private void refreshDeviceCombo(Collection sdKs) {
        final IOSSDK[] sdks = (IOSSDK[]) sdKs.toArray(new IOSSDK[sdKs.size()]);
        sdkCombo.setEnabled(true);
        sdkCombo.setRenderer(new SDKRenderer());
        sdkCombo.setModel(new DefaultComboBoxModel(sdks));
        for (IOSSDK sdk : sdks) {
            final String sdkProp = config.getProperty(IOSSDK.IOS_BUILD_SDK_PROP);
            if (sdk.getName().equals(sdkProp)) {
                sdkCombo.setSelectedItem(sdk);
                break;
            }
        }
        final String deviceProp = config.getProperty(Device.VIRTUAL_DEVICE_PROP); //NOI18N
        if (deviceProp !=null)
            virtualDeviceCombo.setSelectedItem(IOSDevice.valueOf(deviceProp));
    }

    private void initControls(PropertyProvider config) {
        initComponents();
        virtualDeviceCombo.setModel(new DefaultComboBoxModel(new Object[]{IOSDevice.IPHONE, IOSDevice.IPHONE_RETINA, IOSDevice.IPAD, IOSDevice.IPAD_RETINA}));
        virtualDeviceCombo.setRenderer(new DeviceRenderer());
        String device = config.getProperty(Device.DEVICE_PROP); //NOI18N
        if (Device.DEVICE.equals(device)) { //NOI18N
            deviceCombo.setSelectedIndex(Device.DEVICE.equals(device)?1:0); //NOI18N
        }
        setCombosVisible(!Device.DEVICE.equals(device)); //NOI18N
        debuggerCheckBox.setVisible(false);
        deviceCombo.setVisible(false);
        deviceLabel.setVisible(false);
        validate();
    }

    public static class IOSConfigurationCustomizer implements ProjectConfigurationCustomizer {
        
        private final Project p;
        private PropertyProvider config;
        
        public IOSConfigurationCustomizer(Project p, PropertyProvider config) {
            this.p = p;
            this.config = config;
        }
        
        

        @Override
        public JPanel createPanel() {
            return new IOSConfigurationPanel(config);
        }

        @Override
        public EnumSet<HiddenProperties> getHiddenProperties() {
            return EnumSet.of(HiddenProperties.WEB_SERVER);
        }

    }
    
    private static class SDKRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof IOSSDK) {
                setText(((IOSSDK) value).getName());
            }
            return this;
        }
    }
    
    private static class DeviceRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof IOSDevice) {
                setText(((IOSDevice) value).getDisplayName());
            }
            return this;
        }
    }
    
    
    /**
     * Creates new form AndroidConfigurationCustomizer
     */
    @NbBundle.Messages({
        "LBL_NoMac=iOS Development is supported only on Mac.",
        "LBL_NoXcode=Please install Xcode and iOS SDK."})
    public IOSConfigurationPanel(PropertyProvider config) {
        this.config = config;
        if (!Utilities.isMac()) {
            setLayout(new BorderLayout());
            add(new JLabel(Bundle.LBL_NoMac()));
            validate();
        } else if (!org.netbeans.modules.cordova.platforms.api.PlatformManager.getPlatform(PlatformManager.IOS_TYPE).isReady()) {
            setLayout(new BorderLayout());
            add(new JLabel(Bundle.LBL_NoXcode()));
            validate();
        } else {
            initControls(config);
        }
    }

    @NbBundle.Messages("LBL_PleaseWait=Please Wait...")
    private void setCombosVisible(boolean visible) {
        if (visible) {
        sdkCombo.setModel(new DefaultComboBoxModel(new Object[]{Bundle.LBL_PleaseWait()}));
        sdkCombo.setEnabled(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final Collection<? extends SDK> sdKs = PlatformManager.getPlatform(PlatformManager.IOS_TYPE).getSDKs();
                    refreshDeviceCombo(sdKs);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        }
        virtualDeviceCombo.setVisible(visible);
        virtualDeviceLabel.setVisible(visible);
        sdkCombo.setVisible(visible);
        sdkLabel.setVisible(visible);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    @NbBundle.Messages({
        "LBL_Simulator=Simulator",
        "LBL_ConnectedDevice=Connected Device"
    })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sdkLabel = new javax.swing.JLabel();
        sdkCombo = new javax.swing.JComboBox();
        virtualDeviceLabel = new javax.swing.JLabel();
        virtualDeviceCombo = new javax.swing.JComboBox();
        deviceLabel = new javax.swing.JLabel();
        deviceCombo = new javax.swing.JComboBox();
        debuggerCheckBox = new javax.swing.JCheckBox();

        sdkLabel.setLabelFor(sdkCombo);
        org.openide.awt.Mnemonics.setLocalizedText(sdkLabel, org.openide.util.NbBundle.getMessage(IOSConfigurationPanel.class, "IOSConfigurationPanel.sdkLabel.text")); // NOI18N

        sdkCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Bundle.LBL_PleaseWait() }));
        sdkCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sdkComboActionPerformed(evt);
            }
        });

        virtualDeviceLabel.setLabelFor(virtualDeviceCombo);
        org.openide.awt.Mnemonics.setLocalizedText(virtualDeviceLabel, org.openide.util.NbBundle.getMessage(IOSConfigurationPanel.class, "IOSConfigurationPanel.virtualDeviceLabel.text")); // NOI18N

        virtualDeviceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                virtualDeviceComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deviceLabel, org.openide.util.NbBundle.getMessage(IOSConfigurationPanel.class, "IOSConfigurationPanel.deviceLabel.text")); // NOI18N

        deviceCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { Bundle.LBL_Simulator(), Bundle.LBL_ConnectedDevice() }));
        deviceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deviceComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(debuggerCheckBox, org.openide.util.NbBundle.getMessage(IOSConfigurationPanel.class, "IOSConfigurationPanel.debuggerCheckBox.text")); // NOI18N
        debuggerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debuggerCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(virtualDeviceLabel)
                    .addComponent(sdkLabel)
                    .addComponent(deviceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(virtualDeviceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sdkCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deviceCombo, 0, 372, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(debuggerCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deviceLabel)
                    .addComponent(deviceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sdkLabel)
                    .addComponent(sdkCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(virtualDeviceLabel)
                    .addComponent(virtualDeviceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debuggerCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sdkComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdkComboActionPerformed
        config.putProperty(IOSSDK.IOS_BUILD_SDK_PROP, ((IOSSDK)sdkCombo.getSelectedItem()).getIdentifier());
    }//GEN-LAST:event_sdkComboActionPerformed

    private void virtualDeviceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_virtualDeviceComboActionPerformed
        final IOSDevice val = (IOSDevice)virtualDeviceCombo.getSelectedItem();
        if (val != null)
            config.putProperty(Device.VIRTUAL_DEVICE_PROP, val.name()); //NOI18N
    }//GEN-LAST:event_virtualDeviceComboActionPerformed

    private void deviceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deviceComboActionPerformed
        if (deviceCombo.getSelectedIndex() == 0) {
            config.putProperty(Device.DEVICE_PROP, Device.EMULATOR); //NOI18N
            setCombosVisible(true);
        } else {
            config.putProperty(Device.DEVICE_PROP, Device.DEVICE); //NOI18N
            setCombosVisible(false);
        }
    }//GEN-LAST:event_deviceComboActionPerformed

    private void debuggerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debuggerCheckBoxActionPerformed
        config.putProperty("debug.enabled", Boolean.toString(debuggerCheckBox.isSelected())); //NOI18N
    }//GEN-LAST:event_debuggerCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debuggerCheckBox;
    private javax.swing.JComboBox deviceCombo;
    private javax.swing.JLabel deviceLabel;
    private javax.swing.JComboBox sdkCombo;
    private javax.swing.JLabel sdkLabel;
    private javax.swing.JComboBox virtualDeviceCombo;
    private javax.swing.JLabel virtualDeviceLabel;
    // End of variables declaration//GEN-END:variables
}
