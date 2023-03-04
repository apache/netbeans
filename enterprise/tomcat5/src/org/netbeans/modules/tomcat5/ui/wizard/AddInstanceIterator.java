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

package org.netbeans.modules.tomcat5.ui.wizard;

import org.netbeans.modules.tomcat5.optional.StartTomcat;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.swing.JComponent;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.modules.tomcat5.util.TomcatUsers;

/**
 * Iterator for the add Tomcat server wizard.
 *
 * @author abadea
 */
public class AddInstanceIterator implements WizardDescriptor.InstantiatingIterator {
    
    public static final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE; // NOI18N    
    private static final String PROP_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA;  // NOI18N
    private static final String PROP_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; // NOI18N
    private static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N
    
    private static final String[] CONTENT_DATA = new String[] { 
        NbBundle.getMessage(AddInstanceIterator.class, "LBL_InstallationAndLoginDetails") };

    private WizardDescriptor wizard;
    private InstallPanel panel;
    
    public AddInstanceIterator() {
        super();
    }

    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }

    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void previousPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void nextPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Set instantiate() throws java.io.IOException {
        Set result = new HashSet();
        String displayName = getDisplayName();
        String url = panel.getVisual().getUrl();
        String username = panel.getVisual().getUsername();
        String password = panel.getVisual().getPassword();
        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, username, password, displayName);
            Properties prop = panel.getVisual().getProperties ();
            Enumeration en = prop.propertyNames ();
            while (en.hasMoreElements ()) {
                String key = (String) en.nextElement ();
                ip.setProperty (key, prop.getProperty (key));
            }
            ip.setProperty(TomcatProperties.PROP_RUNNING_CHECK_TIMEOUT,
                    Integer.toString(TomcatProperties.DEF_VALUE_RUNNING_CHECK_TIMEOUT));

            result.add(ip);
            checkStartupScript(panel.getVisual().getHomeDir());

            // TODO: refactor the ensureCatalinaBaseReady out of the TomcatManager class and let it throw exceptions 
            //       when something goes wrong, so that we can provide reasonable feedback about failures to the user
            TomcatManager manager = null;
            try {
                manager = (TomcatManager) TomcatFactory.getInstance().getDeploymentManager(url, username, password);
            } catch (DeploymentManagerCreationException e) {
                // this should never happen
                Logger.getLogger(AddInstanceIterator.class.getName()).log(Level.SEVERE, null, e);
                return result;
            }
            manager.ensureCatalinaBaseReady();
            if (panel.getVisual().createUserEnabled()) {
                File tomcatUsersXml = new File(manager.getTomcatProperties().getCatalinaDir(), "conf/tomcat-users.xml"); // NOI18N
                TomcatUsers.createUser(tomcatUsersXml, username, password, manager.getTomcatVersion());
            }
        } catch (IOException e) {
            // TODO: remove this catch as soon as the ensureCatalinaBaseReady method is refactored
            Logger.getLogger(AddInstanceIterator.class.getName()).log(Level.WARNING, null, e);
        }
        return result;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public WizardDescriptor.Panel current() {
        if (panel == null) {
            panel = new InstallPanel();
        }
        setContentData((JComponent)panel.getComponent());
        setContentSelectedIndex((JComponent)panel.getComponent());
        return panel;
    }

    private void setContentData(JComponent component) {
        if (component.getClientProperty(PROP_CONTENT_DATA) == null) {
            component.putClientProperty(PROP_CONTENT_DATA, CONTENT_DATA);
        }
    }

    private void setContentSelectedIndex(JComponent component) {
        if (component.getClientProperty(PROP_CONTENT_SELECTED_INDEX) == null) {
            component.putClientProperty(PROP_CONTENT_SELECTED_INDEX, 0);
        }
    }

    private String getDisplayName() {
        return (String)wizard.getProperty(PROP_DISPLAY_NAME);
    }
    
    /** check for missing startup script - workaround for Tomcat Windows installer distribution */
    private void checkStartupScript(File homeDir) {
        String CATALINA = Utilities.isWindows() ? StartTomcat.CATALINA_BAT 
                                                : StartTomcat.CATALINA_SH;
        boolean catalinaOK = new File(homeDir, "bin/" + CATALINA).exists(); // NOI18N

        String SETCLASSPATH = Utilities.isWindows() ? StartTomcat.SETCLASSPATH_BAT
                                                    : StartTomcat.SETCLASSPATH_SH;
        boolean setclasspathOK = new File(homeDir, "bin/" + SETCLASSPATH).exists(); // NOI18N

        if (!catalinaOK || !setclasspathOK) {
            File bundledHome = TomcatInstallUtil.getBundledHome();
            // INFO: DO NOT FORGET TO CHANGE THE BUNDLED TOMCAT VERSION (AND THE VERSION STRING BELLOW) WHEN UPGRADING
            //       5.5.17  - hopefully this string helps not to miss it;)
            TomcatVersion version = bundledHome == null ? null : TomcatFactory.getTomcatVersion(bundledHome);
            if (version != TomcatManager.TomcatVersion.TOMCAT_55 
                    || bundledHome == null || !bundledHome.exists()) {
                // If there is no bundled Tomcat or the being installed Tomcat is a different
                // version, there is no place where to get the startup scripts from. Lets inform 
                // the user about the problem at least.
                String msg;
                if (!catalinaOK && !setclasspathOK) {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_scripts_fix_by_hand", CATALINA, SETCLASSPATH);
                } else {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_script_fix_by_hand", !catalinaOK ? CATALINA : SETCLASSPATH);
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                // The IDE can copy the startup scripts for the user, lets ask
                // him whether to do it or not.
                String msg;
                if (!catalinaOK && !setclasspathOK) {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_scripts", CATALINA, SETCLASSPATH);
                } else {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_script", !catalinaOK ? CATALINA : SETCLASSPATH);
                }
                NotifyDescriptor nd =
                        new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.YES_OPTION)) {
                    try {
                        if (bundledHome != null) {
                            if (!catalinaOK) {
                                FileUtil.copyFile(
                                    FileUtil.toFileObject(new File(bundledHome, "bin/" + CATALINA)), // NOI18N
                                    FileUtil.toFileObject(new File(homeDir, "bin")),    // NOI18N
                                    CATALINA.substring(0, CATALINA.indexOf("."))    // NOI18N
                                );
                            }
                            if (!setclasspathOK) {
                                FileUtil.copyFile(
                                    FileUtil.toFileObject(new File(bundledHome, "bin/" + SETCLASSPATH)), // NOI18N
                                    FileUtil.toFileObject(new File(homeDir, "bin")),        // NOI18N
                                    SETCLASSPATH.substring(0, SETCLASSPATH.indexOf("."))    // NOI18N
                                );
                            }
                        }
                    } catch (IOException e) {
                        msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_startup_scripts_copy_failed");
                        nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }
            }
        }
    }
}
