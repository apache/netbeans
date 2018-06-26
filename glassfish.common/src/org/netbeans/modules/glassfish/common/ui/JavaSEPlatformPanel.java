/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.utils.JavaUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.OK_OPTION;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Warning panel showing an information about unsupported Java SE platform used
 * and allowing user to select another one from Java SE platforms registered
 * in NetBeans.
 * <p/>
 * @author Tomas Kraus
 */
public class JavaSEPlatformPanel extends JPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Action to invoke Java SE platforms customizer.
     */
    private class PlatformAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            PlatformsCustomizer.showCustomizer(javaPlatform());
            javaPlatforms = JavaUtils.findSupportedPlatforms(instance);
            ((JavaPlatformsComboBox)javaComboBox).updateModel(javaPlatforms);
            setDescriptorButtons(descriptor, javaPlatforms);
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(JavaSEPlatformPanel.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set buttons in user notification descriptor depending on Java SE
     * platforms.
     * <p/>
     * This method is used in constructor so it's better to be static.
     * <p/>
     * @param descriptor    User notification descriptor.
     * @param javaPlatforms Java SE JDK selection content.
     */
    private void setDescriptorButtons(
            NotifyDescriptor descriptor, JavaPlatform[] javaPlatforms) {
        if (javaPlatforms == null || javaPlatforms.length == 0) {
            descriptor.setOptions(new Object[] {CANCEL_OPTION});
        } else {
            descriptor.setOptions(new Object[] {OK_OPTION, CANCEL_OPTION});
        }
    }

    /**
     * Display GlassFish Java SE selector to allow switch Java SE used
     * to run GlassFish.
     * <p/>
     * Selected Java SE is stored in server instance properties and returned
     * by this method. Properties are persisted.
     * <p/>
     * @param instance GlassFish server instance to be started.
     * @param javaHome Java SE home currently selected.
     */
    public static FileObject selectServerSEPlatform(            
           final  GlassfishInstance instance, final File javaHome) {
        FileObject selectedJavaHome = null;
        // Matching Java SE home installed platform if exists.
        JavaPlatform platform = JavaUtils.findInstalledPlatform(javaHome);
        String platformName = platform != null
                ? platform.getDisplayName() : javaHome.getAbsolutePath();
        String message = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.warning", platformName);
        String title = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.title", platformName);
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                null, title, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
        JavaSEPlatformPanel panel = new JavaSEPlatformPanel(
                notifyDescriptor, instance, message);
        Object button = DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (button == CANCEL_OPTION) {
            return selectedJavaHome;
        }
        JavaPlatform selectedPlatform = panel.javaPlatform();
        if (selectedPlatform != null) {
            Iterator<FileObject> platformIterator
                    = selectedPlatform.getInstallFolders().iterator();
            if (platformIterator.hasNext()) {
                selectedJavaHome = (FileObject)platformIterator.next();
            }
        }
        if (selectedJavaHome != null && panel.updateProperties()) {
            instance.setJavaHome(panel.isJavaPlatformDefault() ? null
                    : FileUtil.toFile(selectedJavaHome).getAbsolutePath());
            try {
                GlassfishInstance.writeInstanceToFile(instance);
            } catch(IOException ex) {
                LOGGER.log(Level.INFO,
                        "Could not store GlassFish server attributes", ex);
            }
        }
        return selectedJavaHome;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server instance used to search for supported platforms. */
    private final GlassfishInstance instance;

    /** Warning message to be shown in the panel. */
    private final String message;

    /** Java SE JDK selection label. */
    private final String javaLabelText;

    /** Update properties check box label. */
    private final String propertiesLabelText;

    /** Platform customizer button label. */
    private final String platformButtonText;

    /** Platform customizer button action. */
    private final PlatformAction platformButtonAction;

    /** Java SE JDK selection content. */
    JavaPlatform[] javaPlatforms;

    /** User notification descriptor. */
    private final NotifyDescriptor descriptor;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new Java SE platform selection panel with message.
     * <p/>
     * @param descriptor User notification descriptor.
     * @param instance   GlassFish server instance used to search
     *                   for supported platforms.
     * @param message    Warning text.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public JavaSEPlatformPanel(final NotifyDescriptor descriptor,
            final GlassfishInstance instance, final String message) {
        this.descriptor = descriptor;
        this.instance = instance;
        this.message = message;
        this.javaLabelText = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.javaLabel");
        this.propertiesLabelText = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.propertiesLabel");
        this.platformButtonText = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.platformButton");
        this.platformButtonAction = new PlatformAction();
        javaPlatforms = JavaUtils.findSupportedPlatforms(instance);
        descriptor.setMessage(this);
        setDescriptorButtons(this.descriptor, this.javaPlatforms);
        initComponents();
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters and Setters                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve state of properties update check box.
     * <p/>
     * @return Returns <code>true</code> when properties update check box
     *         was selected or <code>false</code> otherwise.
     */
    boolean updateProperties() {
        return propertiesCheckBox.isSelected();
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

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        javaComboBox = new JavaPlatformsComboBox(javaPlatforms);
        javaLabel = new javax.swing.JLabel();
        propertiesLabel = new javax.swing.JLabel();
        propertiesCheckBox = new javax.swing.JCheckBox();
        platformButton = new javax.swing.JButton(platformButtonAction);

        setMaximumSize(new java.awt.Dimension(500, 200));
        setMinimumSize(new java.awt.Dimension(500, 150));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(500, 150));

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, this.message);

        org.openide.awt.Mnemonics.setLocalizedText(javaLabel, this.javaLabelText);

        org.openide.awt.Mnemonics.setLocalizedText(propertiesLabel, this.propertiesLabelText);

        propertiesCheckBox.setSelected(true);
        propertiesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        org.openide.awt.Mnemonics.setLocalizedText(platformButton, this.platformButtonText);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaLabel)
                            .addComponent(propertiesLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(javaComboBox, 0, 232, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(platformButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(propertiesCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(javaLabel)
                    .addComponent(javaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(platformButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(propertiesCheckBox)
                    .addComponent(propertiesLabel))
                .addContainerGap(13, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox javaComboBox;
    private javax.swing.JLabel javaLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton platformButton;
    private javax.swing.JCheckBox propertiesCheckBox;
    private javax.swing.JLabel propertiesLabel;
    // End of variables declaration//GEN-END:variables
}
