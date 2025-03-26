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

package org.netbeans.modules.maven.j2ee.ui.customizer.impl;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import static org.netbeans.modules.maven.j2ee.execution.ExecutionChecker.CLIENTURLPART;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.netbeans.modules.maven.j2ee.ui.customizer.BaseRunCustomizer;
import org.netbeans.modules.maven.j2ee.ui.customizer.CheckBoxUpdater;
import org.netbeans.modules.maven.j2ee.ui.customizer.ComboBoxUpdater;
import static org.netbeans.modules.maven.j2ee.ui.customizer.impl.Bundle.*;
import org.netbeans.modules.maven.j2ee.ui.util.WarningPanel;
import org.netbeans.modules.maven.j2ee.ui.util.WarningPanelSupport;
import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.j2ee.utils.ServerUtils;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.BrowserUISupport.BrowserComboBoxModel;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 * @author Martin Janicek
 */
public class CustomizerRunWeb extends BaseRunCustomizer {

    public static final String PROP_SHOW_IN_BROWSER = "netbeans.deploy.showBrowser"; //NOI18N
    public static final String PROP_ALWAYS_BUILD_BEFORE_RUNNING = "netbeans.always.build"; // NOI18N
    private static final Set<Profile> WEB_PROFILES;
    private static final Set<Profile> FULL_PROFILES;

    private final CheckBoxUpdater copyStaticResourcesUpdater;
    private final CheckBoxUpdater showBrowserUpdater;
    private final CheckBoxUpdater alwaysBuildUpdater;
    private final ComboBoxUpdater versionUpdater;
    private final boolean noServer;

    private BrowserComboBoxModel browserModel;

    private WebModule module;

    private NetbeansActionMapping run;
    private NetbeansActionMapping debug;
    private NetbeansActionMapping profile;

    private boolean isRunCompatible;
    private boolean isDebugCompatible;
    private boolean isProfileCompatible;

    private String oldUrl;
    private String oldContextPath;

    static {
        WEB_PROFILES = new TreeSet<>(Profile.UI_COMPARATOR);
        WEB_PROFILES.add(Profile.JAVA_EE_5);
        WEB_PROFILES.add(Profile.JAVA_EE_6_WEB);
        WEB_PROFILES.add(Profile.JAVA_EE_7_WEB);
        WEB_PROFILES.add(Profile.JAVA_EE_8_WEB);
        WEB_PROFILES.add(Profile.JAKARTA_EE_8_WEB);
        WEB_PROFILES.add(Profile.JAKARTA_EE_9_WEB);
        WEB_PROFILES.add(Profile.JAKARTA_EE_9_1_WEB);
        WEB_PROFILES.add(Profile.JAKARTA_EE_10_WEB);
        WEB_PROFILES.add(Profile.JAKARTA_EE_11_WEB);

        FULL_PROFILES = new TreeSet<>(Profile.UI_COMPARATOR);
        FULL_PROFILES.add(Profile.JAVA_EE_5);
        FULL_PROFILES.add(Profile.JAVA_EE_6_FULL);
        FULL_PROFILES.add(Profile.JAVA_EE_7_FULL);
        FULL_PROFILES.add(Profile.JAVA_EE_8_FULL);
        FULL_PROFILES.add(Profile.JAKARTA_EE_8_FULL);
        FULL_PROFILES.add(Profile.JAKARTA_EE_9_FULL);
        FULL_PROFILES.add(Profile.JAKARTA_EE_9_1_FULL);
        FULL_PROFILES.add(Profile.JAKARTA_EE_10_FULL);
        FULL_PROFILES.add(Profile.JAKARTA_EE_11_FULL);
    }

    @Messages({
        "WARNING_ChangingJavaEEVersion=<html>You are changing Java EE version. <b>Please be aware about "
            + "possible consequences</b>. Your project might not be deployable anymore if the selected "
            + "server doesn't support choosen version.<br><br>Also note that changing this value doesn't "
            + "make any changes in your project configuration (pom.xml will still reffer to the original "
            + "Java EE jar file etc.)</html>."
    })
    public CustomizerRunWeb(final ModelHandle2 handle, final Project project) {
        super(handle, project, J2eeModule.Type.WAR);
        initComponents();

        btnLearnMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLearnMore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://wiki.netbeans.org/FaqDeployOnSave"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        module = WebModule.getWebModule(project.getProjectDirectory());
        if (module != null) {
            contextPathTField.setText(module.getContextPath());
        }

        noServer = ExecutionChecker.DEV_NULL.equals(ServerUtils.findServer(project).getServerID());

        initValues();
        initServerModel(jCBServer, serverLabel);
        initDeployOnSave(jCBDeployOnSave, dosDescription);

        copyStaticResourcesUpdater = CheckBoxUpdater.create(jCBCopyStaticResources, MavenProjectSupport.isCopyStaticResourcesOnSave(project), new CheckBoxUpdater.Store() {

            @Override
            public void storeValue(boolean value) {
                MavenProjectSupport.setCopyStaticResourcesOnSave(project, value);
            }
        });

        String browser = (String) project.getProjectDirectory().getAttribute(PROP_SHOW_IN_BROWSER);
        boolean showBrowser = browser != null ? Boolean.parseBoolean(browser) : true;
        showBrowserUpdater = CheckBoxUpdater.create(jCBshowBrowser, showBrowser, new CheckBoxUpdater.Store() {

            @Override
            public void storeValue(boolean value) {
                try {
                    if (value) {
                        project.getProjectDirectory().setAttribute(PROP_SHOW_IN_BROWSER, null);
                    } else {
                        project.getProjectDirectory().setAttribute(PROP_SHOW_IN_BROWSER, Boolean.FALSE.toString());
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        Boolean alwaysBuild = (Boolean) project.getProjectDirectory().getAttribute(PROP_ALWAYS_BUILD_BEFORE_RUNNING);
        if (alwaysBuild == null) {
            alwaysBuild = Boolean.FALSE;
        }
        alwaysBuildUpdater = CheckBoxUpdater.create(jCBAlwaysBuild, alwaysBuild, new CheckBoxUpdater.Store() {

            @Override
            public void storeValue(boolean value) {
                try {
                    if (value) {
                        project.getProjectDirectory().setAttribute(PROP_ALWAYS_BUILD_BEFORE_RUNNING, true);
                    } else {
                        project.getProjectDirectory().setAttribute(PROP_ALWAYS_BUILD_BEFORE_RUNNING, false);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        versionUpdater = createVersionUpdater(J2eeModule.Type.WAR);
    }

    @Override
    public void applyChangesInAWT() {
        assert SwingUtilities.isEventDispatchThread();
        showBrowserUpdater.storeValue();

        Object obj = jCBServer.getSelectedItem();
        if (obj != null) {
            LoggingUtils.logUsage(CustomizerRunWeb.class, "USG_PROJECT_CONFIG_MAVEN_SERVER", new Object[] {obj.toString() }, "maven"); //NOI18N
        }
    }

    @Override
    public void applyChanges() {
        changeContextPath();

        serverUpdater.storeValue();
        versionUpdater.storeValue();
        alwaysBuildUpdater.storeValue();
        deployOnSaveUpdater.storeValue();
        copyStaticResourcesUpdater.storeValue();

        JavaEEProjectSettings.setBrowserID(project, browserModel.getSelectedBrowserId());
    }

    private void initValues() {
        List<NetbeansActionMapping> actionMappings = handle.getActionMappings(handle.getActiveConfiguration()).getActions();

        if (actionMappings == null || actionMappings.isEmpty()) {
            run = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_RUN, project);
            debug = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_DEBUG, project);
            profile = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_PROFILE, project);
        } else {
            for (NetbeansActionMapping actionMapping : actionMappings) {
                String actionName = actionMapping.getActionName();

                if (ActionProvider.COMMAND_RUN.equals(actionName)) {
                    run = actionMapping;
                }
                if (ActionProvider.COMMAND_DEBUG.equals(actionName)) {
                    debug = actionMapping;
                }
                if (ActionProvider.COMMAND_PROFILE.equals(actionName)) { // NOI18N
                    profile = actionMapping;
                }
            }
        }

        isRunCompatible = checkMapping(run);
        isDebugCompatible = checkMapping(debug);
        isProfileCompatible = checkMapping(profile);

        if (isRunCompatible) {
            if (run != null) {
                oldUrl = run.getProperties().get(CLIENTURLPART);
            } else if (debug != null) {
                oldUrl = debug.getProperties().get(CLIENTURLPART);
            }
        }

        if (oldUrl != null) {
            txtRelativeUrl.setText(oldUrl);
        } else {
            oldUrl = ""; //NOI18N
        }
        txtRelativeUrl.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                applyRelUrl();
            }
        });

        updateContextPathEnablement();
    }

    private ComboBoxUpdater createVersionUpdater(J2eeModule.Type projectType) {
        if (J2eeModule.Type.WAR.equals(projectType)) {
            jCBJavaeeVersion.setModel(new DefaultComboBoxModel(WEB_PROFILES.toArray()));
        } else {
            jCBJavaeeVersion.setModel(new DefaultComboBoxModel(FULL_PROFILES.toArray()));
        }

        final ListCellRenderer delegate = jCBJavaeeVersion.getRenderer();
        jCBJavaeeVersion.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return delegate.getListCellRendererComponent(list, ((Profile) value).getDisplayName(), index, isSelected, cellHasFocus);
            }

        });

        ComboBoxUpdater.Store store = new ComboBoxUpdater.Store() {

            @Override
            public void storeValue(Object profile) {
                if (profile != null) {
                    JavaEEProjectSettings.setProfile(project, (Profile) profile);
                }
            }
        };

        ComboBoxUpdater.Verify verifier = new ComboBoxUpdater.Verify() {

            @Override
            public boolean verifyValue(Object value) {
                if (WarningPanelSupport.isJavaEEChangeWarningActivated()) {
                    WarningPanel panel = new WarningPanel(WARNING_ChangingJavaEEVersion());
                    NotifyDescriptor dd = new NotifyDescriptor.Confirmation(panel, NotifyDescriptor.OK_CANCEL_OPTION);
                    DialogDisplayer.getDefault().notify(dd);

                    if (dd.getValue() == NotifyDescriptor.CANCEL_OPTION) {
                        return false;
                    }

                    if (panel.disabledWarning()) {
                        WarningPanelSupport.dontShowJavaEEChangeWarning();
                    }
                }
                return true;
            }
        };

        Profile defaultProfile = JavaEEProjectSettings.getProfile(project);
        if (defaultProfile == null) {
            WebModuleProviderImpl webModuleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
            if (webModuleProvider != null) {
                WebModuleImpl webModule = webModuleProvider.getModuleImpl();
                if (webModule != null) {
                    defaultProfile = webModule.getJ2eeProfile();
                }
            }
        }

        return ComboBoxUpdater.create(jCBJavaeeVersion, javaeeVersionLabel, defaultProfile, store, verifier);
    }

    private boolean checkMapping(NetbeansActionMapping map) {
        if (map != null) {
            for (String goal : map.getGoals()) {
                if (goal.indexOf("netbeans-deploy-plugin") > -1) { //NOI18N
                    return true;
                }
            }
            if (map.getProperties().containsKey(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY)) {
                return true;
            }
        }
        return false;
    }

    private void applyRelUrl() {
        String newUrl = txtRelativeUrl.getText().trim();
        if (!newUrl.equals(oldUrl)) {
            if (isRunCompatible) {
                run.addProperty(CLIENTURLPART, newUrl);
                ModelHandle2.setUserActionMapping(run, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
            if (isDebugCompatible) {
                debug.addProperty(CLIENTURLPART, newUrl);
                ModelHandle2.setUserActionMapping(debug, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
            if (isProfileCompatible) {
                profile.addProperty(CLIENTURLPART, newUrl);
                ModelHandle2.setUserActionMapping(profile, handle.getActionMappings());
                handle.markAsModified(handle.getActionMappings());
            }
        }
    }

    private void changeContextPath() {
        final WebModuleProviderImpl moduleProvider = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (contextPathTField.isEnabled()) {
            WebModuleImpl impl = moduleProvider.getModuleImpl();
            impl.setContextPath(contextPathTField.getText().trim());
        }
    }

    private JComboBox<WebBrowser> createBrowserComboBox() {
        String selectedBrowser = JavaEEProjectSettings.getBrowserID(project);
        browserModel = BrowserUISupport.createBrowserModel(selectedBrowser, true);
        jCBBrowser = BrowserUISupport.createBrowserPickerComboBox(browserModel.getSelectedBrowserId(), true, false, browserModel);
        jCBBrowser.setModel(browserModel);
        jCBBrowser.setRenderer(BrowserUISupport.createBrowserRenderer());

        return jCBBrowser;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLabel = new javax.swing.JLabel();
        jCBServer = new javax.swing.JComboBox();
        javaeeVersionLabel = new javax.swing.JLabel();
        contextPathLabel = new javax.swing.JLabel();
        contextPathTField = new javax.swing.JTextField();
        jCBshowBrowser = new javax.swing.JCheckBox();
        lblRelativeUrl = new javax.swing.JLabel();
        txtRelativeUrl = new javax.swing.JTextField();
        lblHint2 = new javax.swing.JLabel();
        jCBDeployOnSave = new javax.swing.JCheckBox();
        dosDescription = new javax.swing.JLabel();
        jCBJavaeeVersion = new javax.swing.JComboBox();
        browserLabel = new javax.swing.JLabel();
        jCBBrowser = createBrowserComboBox();
        jCBCopyStaticResources = new javax.swing.JCheckBox();
        jCBAlwaysBuild = new javax.swing.JCheckBox();
        btnLearnMore = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Server")); // NOI18N

        jCBServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBServerActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaeeVersionLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_J2EE_Version")); // NOI18N

        contextPathLabel.setLabelFor(contextPathTField);
        org.openide.awt.Mnemonics.setLocalizedText(contextPathLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Context_Path")); // NOI18N

        contextPathTField.setMinimumSize(new java.awt.Dimension(4, 24));
        contextPathTField.setPreferredSize(new java.awt.Dimension(4, 24));

        org.openide.awt.Mnemonics.setLocalizedText(jCBshowBrowser, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Display_on_Run")); // NOI18N

        lblRelativeUrl.setLabelFor(txtRelativeUrl);
        org.openide.awt.Mnemonics.setLocalizedText(lblRelativeUrl, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Relative_URL")); // NOI18N

        txtRelativeUrl.setMinimumSize(new java.awt.Dimension(4, 24));
        txtRelativeUrl.setPreferredSize(new java.awt.Dimension(4, 24));

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "LBL_Hint2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCBDeployOnSave, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCBDeployOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dosDescription, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.dosDescription.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browserLabel, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.browserLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCBCopyStaticResources, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCBCopyStaticResources.text")); // NOI18N
        jCBCopyStaticResources.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCBCopyStaticResources.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCBAlwaysBuild, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCBAlwaysBuild.text")); // NOI18N
        jCBAlwaysBuild.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.jCBAlwaysBuild.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnLearnMore, org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "CustomizerRunWeb.btnLearnMore.text")); // NOI18N
        btnLearnMore.setBorderPainted(false);
        btnLearnMore.setContentAreaFilled(false);
        btnLearnMore.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(browserLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRelativeUrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(javaeeVersionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(serverLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(contextPathLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCBServer, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(contextPathTField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCBJavaeeVersion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblHint2, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                            .addComponent(txtRelativeUrl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCBBrowser, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCBDeployOnSave)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(dosDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(btnLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCBAlwaysBuild)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jCBshowBrowser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCBCopyStaticResources, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverLabel)
                    .addComponent(jCBServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCBJavaeeVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaeeVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextPathTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contextPathLabel))
                .addGap(8, 8, 8)
                .addComponent(lblHint2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRelativeUrl)
                    .addComponent(txtRelativeUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(browserLabel)
                    .addComponent(jCBBrowser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jCBshowBrowser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCBCopyStaticResources)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCBDeployOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dosDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCBAlwaysBuild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        contextPathTField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtContextPath.AccessibleContext.accessibleDescription")); // NOI18N
        jCBshowBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.cbBrowser.AccessibleContext.accessibleDescription")); // NOI18N
        txtRelativeUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.txtRelativeUrl.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jCBServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBServerActionPerformed
        updateContextPathEnablement();
    }//GEN-LAST:event_jCBServerActionPerformed

    private void updateContextPathEnablement() {
        Server wp = (Server) jCBServer.getSelectedItem();
        if (wp == null || ExecutionChecker.DEV_NULL.equals(wp.getServerID())) {
            if (contextPathTField.isEnabled()) {
                contextPathTField.setEnabled(false);
                oldContextPath = contextPathTField.getText();
                if (!noServer) {
                    contextPathTField.setText(NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.contextPathDisabled"));
                } else {
                    contextPathTField.setText(NbBundle.getMessage(CustomizerRunWeb.class, "WebRunCustomizerPanel.contextPathDisabledConfirm"));
                }
            }
        } else {
            if (!contextPathTField.isEnabled() && !noServer) {
                contextPathTField.setEnabled(true);
                if (oldContextPath != null) {
                    contextPathTField.setText(oldContextPath);
                } else {
                    contextPathTField.setText(module.getContextPath());
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel browserLabel;
    private javax.swing.JButton btnLearnMore;
    private javax.swing.JLabel contextPathLabel;
    private javax.swing.JTextField contextPathTField;
    private javax.swing.JLabel dosDescription;
    private javax.swing.JCheckBox jCBAlwaysBuild;
    private javax.swing.JComboBox jCBBrowser;
    private javax.swing.JCheckBox jCBCopyStaticResources;
    private javax.swing.JCheckBox jCBDeployOnSave;
    private javax.swing.JComboBox jCBJavaeeVersion;
    private javax.swing.JComboBox jCBServer;
    private javax.swing.JCheckBox jCBshowBrowser;
    private javax.swing.JLabel javaeeVersionLabel;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblRelativeUrl;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JTextField txtRelativeUrl;
    // End of variables declaration//GEN-END:variables

}
