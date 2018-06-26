/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.SpinnerNumberModel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.utils.JavaUtils;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class VmCustomizer extends javax.swing.JPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Action to invoke Java SE platforms customizer.
     */
    private class PlatformAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            PlatformsCustomizer.showCustomizer(javaPlatform());
            javaPlatforms = JavaUtils.findSupportedPlatforms(instance);
            ((JavaPlatformsComboBox)javaComboBox)
                    .updateModel(javaPlatforms);
        }
        
    }

    /**
     * Port limits: Minimal value. From {
     *
//     * @see java.net.ServerSocket} constructor source code.
     */
    private static int PORT_MIN = 0x00;
    /**
     * Port limits: Maximal value. From {
     *
     * @see java.net.ServerSocket} constructor source code.
     */
    private static int PORT_MAX = 0xFFFF;

    /** Platform customizer button label. */
    private final String platformButtonText;

    /** Platform customizer button action. */
    private final PlatformAction platformButtonAction;

    /** GlassFish server instance to be modified. */
    private final GlassfishInstance instance;

    /** Java SE JDK selection content. */
    JavaPlatform[] javaPlatforms;

    /**
     * Creates new form VmCustomizer
     */
    public VmCustomizer(final GlassfishInstance instance) {
        this.instance = instance;
        javaPlatforms = JavaUtils.findSupportedPlatforms(this.instance);
        this.platformButtonText = NbBundle.getMessage(
                VmCustomizer.class,
                "VmCustomizer.platformButton");
        this.platformButtonAction = new PlatformAction();
        initComponents();
    }

    private void initFields() {
        String address = instance.getProperty(GlassfishModule.DEBUG_PORT);
        SpinnerNumberModel addressModel = (SpinnerNumberModel) addressValue.getModel();
        javaPlatforms = JavaUtils.findSupportedPlatforms(this.instance);
        ((JavaPlatformsComboBox)javaComboBox).updateModel(javaPlatforms);
        javaComboBox.setSelectedItem(instance.getJavaPlatform());
        if (null == address || "0".equals(address) || "".equals(address)) {
            useUserDefinedAddress.setSelected(false);
        } else {
            useUserDefinedAddress.setSelected(true);
            setAddressValue(address);
        }
        if (Utilities.isWindows() && !instance.isRemote()) {
            useSharedMemRB.setSelected("true".equals(instance.getProperty(GlassfishModule.USE_SHARED_MEM_ATTR)));
            useSocketRB.setSelected(!("true".equals(instance.getProperty(GlassfishModule.USE_SHARED_MEM_ATTR))));
        } else {
            // not windows -- disable shared mem and correct it if it was set...
            // or remote instance....
            useSharedMemRB.setEnabled(false);
            useSharedMemRB.setSelected(false);
            useSocketRB.setSelected(true);
        }
        useIDEProxyInfo.setSelected("true".equals(instance.getProperty(GlassfishModule.USE_IDE_PROXY_FLAG)));
        boolean isLocalDomain = instance.getProperty(GlassfishModule.DOMAINS_FOLDER_ATTR) != null;
        this.javaComboBox.setEnabled(isLocalDomain);
        this.useIDEProxyInfo.setEnabled(isLocalDomain);
        this.useSharedMemRB.setEnabled(isLocalDomain);
    }

    /**
     * Get value of number stored in
     * <code>addressValue</code> field.
     * <p/>
     * @return Value of number stored in
     * <code>addressValue</code> field.
     */
    private Number getAddressValue() {
        return ((SpinnerNumberModel) addressValue.getModel()).getNumber();
    }

    /**
     * Set value of number stored in
     * <code>addressValue</code> field.
     * <p/>
     * Value will be set to <code>0</code> if there is any problem with
     * retrieving integer value from <code>String</code> argument.
     * <p/>
     * @param number Value of number to be stored in
     * <code>addressValue</code> field.
     */
    private void setAddressValue(String number) {
        try {
        addressValue.setValue(new Integer(number));
        } catch (NumberFormatException nfe) {
            addressValue.setValue(new Integer(0));
        }
    }

    /**
     * Set value of number stored in
     * <code>addressValue</code> field.
     * <p/>
     * Value of <code>null</code> is stored as <code>0</code>.
     * <p/>
     * @param number Value of number to be stored in
     * <code>addressValue</code> field.
     */
    private void setAddressValue(Integer number) {
        addressValue.setValue(number != null ? number : new Integer(0));
    }

    private void persistFields() {
        String selectedJavaHome = null;
        JavaPlatform selectedPlatform
                = isJavaPlatformDefault() ? null : javaPlatform();
        if (selectedPlatform != null) {
            Iterator<FileObject> platformIterator = selectedPlatform.getInstallFolders().iterator();
            if (platformIterator.hasNext()) {
                FileObject javaHomeFO = platformIterator.next();
                selectedJavaHome = javaHomeFO != null
                        ? FileUtil.toFile(javaHomeFO).getAbsolutePath() : null;
            }
        }
        instance.setJavaHome(selectedJavaHome);
        if (selectedJavaHome != null) {
            RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
            if (null != db) {
                File f = new File(selectedJavaHome);
                if (f.exists() && f.canRead() && f.isDirectory()) {
                    File dbdir = new File(f, "db"); // NOI18N
                    if (dbdir.exists() && dbdir.isDirectory() && dbdir.canRead()) {
                        db.initialize(dbdir.getAbsolutePath());
                    }
                }
            }
        }
        instance.putProperty(GlassfishModule.USE_SHARED_MEM_ATTR,
                Boolean.toString(useSharedMemRB.isSelected()));
        instance.putProperty(GlassfishModule.USE_IDE_PROXY_FLAG,
                Boolean.toString(useIDEProxyInfo.isSelected()));
        instance.putProperty(GlassfishModule.DEBUG_PORT,
                getAddressValue().toString());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initFields();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        persistFields();
    }

    /**
     * Retrieve selected Java SE platform from java combo box.
     * <p/>
     * @return Returns {@see JavaPlatform} object of selected Java SE platform.
     */
    JavaPlatform javaPlatform() {
        JavaPlatformsComboBox.Platform platform =
                (JavaPlatformsComboBox.Platform)javaComboBox.getSelectedItem();
        return platform != null ? platform.getPlatform() : null;
    }

    /**
     * Check if selected Java SE platform from java combo box
     * is the default platform.
     * <p/>
     * @return Value of <code>true</code> if this platform is the default
     *         platform or <code>false</code> otherwise.
     */
    boolean isJavaPlatformDefault() {
        JavaPlatformsComboBox.Platform platform =
                (JavaPlatformsComboBox.Platform)javaComboBox.getSelectedItem();
        return platform != null ? platform.isDefault() : false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        debugSettingsPanel = new javax.swing.JPanel();
        useSocketRB = new javax.swing.JRadioButton();
        useSharedMemRB = new javax.swing.JRadioButton();
        useUserDefinedAddress = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        addressValue = new javax.swing.JSpinner();
        pickerPanel = new javax.swing.JPanel();
        javaInstallLabel = new javax.swing.JLabel();
        javaComboBox = new JavaPlatformsComboBox(javaPlatforms);
        platformButton = new javax.swing.JButton(platformButtonAction);
        useIDEProxyInfo = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.name")); // NOI18N

        debugSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.debugSettingsPanel.border.title.text"))); // NOI18N

        buttonGroup1.add(useSocketRB);
        org.openide.awt.Mnemonics.setLocalizedText(useSocketRB, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useSocketRB.text")); // NOI18N

        buttonGroup1.add(useSharedMemRB);
        org.openide.awt.Mnemonics.setLocalizedText(useSharedMemRB, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useSharedMemRB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useUserDefinedAddress, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useUserDefinedAddress.text", new Object[] {})); // NOI18N
        useUserDefinedAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleAddressUsage(evt);
            }
        });

        jLabel1.setLabelFor(addressValue);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.jLabel1.text")); // NOI18N

        addressValue.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        addressValue.setEditor(new javax.swing.JSpinner.NumberEditor(addressValue, "#####"));

        javax.swing.GroupLayout debugSettingsPanelLayout = new javax.swing.GroupLayout(debugSettingsPanel);
        debugSettingsPanel.setLayout(debugSettingsPanelLayout);
        debugSettingsPanelLayout.setHorizontalGroup(
            debugSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(debugSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(debugSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(debugSettingsPanelLayout.createSequentialGroup()
                        .addComponent(useUserDefinedAddress)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addressValue, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 136, Short.MAX_VALUE))
                    .addGroup(debugSettingsPanelLayout.createSequentialGroup()
                        .addGroup(debugSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useSharedMemRB)
                            .addComponent(useSocketRB))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        debugSettingsPanelLayout.setVerticalGroup(
            debugSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(debugSettingsPanelLayout.createSequentialGroup()
                .addComponent(useSharedMemRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useSocketRB)
                .addGap(8, 8, 8)
                .addGroup(debugSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useUserDefinedAddress)
                    .addComponent(jLabel1)
                    .addComponent(addressValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        useSocketRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_UseSockets")); // NOI18N
        useSharedMemRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_SharedMem")); // NOI18N
        useUserDefinedAddress.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_UseSelectedAddress")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_AddressLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaInstallLabel, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.javaInstallLabel.text")); // NOI18N

        javaComboBox.setMaximumSize(new java.awt.Dimension(400, 32767));
        javaComboBox.setMinimumSize(new java.awt.Dimension(400, 24));
        javaComboBox.setPreferredSize(new java.awt.Dimension(400, 24));

        platformButton.setText(this.platformButtonText);

        javax.swing.GroupLayout pickerPanelLayout = new javax.swing.GroupLayout(pickerPanel);
        pickerPanel.setLayout(pickerPanelLayout);
        pickerPanelLayout.setHorizontalGroup(
            pickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pickerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(javaInstallLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(javaComboBox, 0, 1, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(platformButton)
                .addContainerGap())
        );
        pickerPanelLayout.setVerticalGroup(
            pickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pickerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pickerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaInstallLabel)
                    .addComponent(javaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(platformButton))
                .addContainerGap())
        );

        javaInstallLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_JavaLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useIDEProxyInfo, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useIDEProxyInfo.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pickerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(debugSettingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(useIDEProxyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pickerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(debugSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useIDEProxyInfo)
                .addGap(19, 19, 19))
        );

        useIDEProxyInfo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_UseIdeProxySettings")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "A11Y_DESC_JavaPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void toggleAddressUsage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleAddressUsage
        if (useUserDefinedAddress.isSelected()) {
            // enable the edit field and fill it in
            addressValue.setEnabled(true);
            int debugPort = 9009;
            try {
                ServerSocket t = new ServerSocket(0);
                debugPort = t.getLocalPort();
                t.close();
            } catch (IOException ioe) {
                // I will ignore this nor now.
            }
            setAddressValue(new Integer(debugPort));
        } else {
            // clear the field and disable it
            setAddressValue(new Integer(0));
            addressValue.setEnabled(false);
        }
    }//GEN-LAST:event_toggleAddressUsage
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner addressValue;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel debugSettingsPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox javaComboBox;
    private javax.swing.JLabel javaInstallLabel;
    private javax.swing.JPanel pickerPanel;
    private javax.swing.JButton platformButton;
    private javax.swing.JCheckBox useIDEProxyInfo;
    private javax.swing.JRadioButton useSharedMemRB;
    private javax.swing.JRadioButton useSocketRB;
    private javax.swing.JCheckBox useUserDefinedAddress;
    // End of variables declaration//GEN-END:variables
}
