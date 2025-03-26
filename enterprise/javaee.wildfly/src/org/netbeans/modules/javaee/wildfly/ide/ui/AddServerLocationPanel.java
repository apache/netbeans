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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.WizardDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerLocationPanel implements WizardDescriptor.FinishablePanel, ChangeListener {

    private static final String J2SE_PLATFORM_VERSION_17 = "1.7"; // NOI18N

    private final WildflyInstantiatingIterator instantiatingIterator;

    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private final transient Set<ChangeListener> listeners = ConcurrentHashMap.newKeySet(2);

    public AddServerLocationPanel(WildflyInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    @Override
    public void stateChanged(ChangeEvent ev) {
        new ArrayList<>(listeners).forEach(l -> l.stateChanged(ev));
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_jboss_location"); //NOI18N
    }

    @Override
    public boolean isValid() {
        String locationStr = component.getInstallLocation();
        if (locationStr == null || locationStr.trim().length() < 1) {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_SpecifyServerLocation")); // NOI18N
            return false;
        }

        File path = new File(locationStr);
        if (!WildflyPluginUtils.isGoodJBServerLocation(path)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidServerLocation")); // NOI18N
            return false;
        }

        // test if IDE is run on correct JDK version
        if (!runningOnCorrectJdk(path)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidJDK"));
            return false;
        }

        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        WildflyPluginProperties.getInstance().setInstallLocation(component.getInstallLocation());
        WildflyPluginProperties.getInstance().setConfigLocation(component.getConfigurationLocation());
        WildflyPluginProperties.getInstance().setDomainLocation(component.getConfigurationLocation());
        WildflyPluginProperties.getInstance().saveProperties();
        instantiatingIterator.setInstallLocation(locationStr);
        instantiatingIterator.setAdminPort("" + WildflyPluginProperties.getInstance().getAdminPort());
        return true;
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }

    @Override
    public void storeSettings(Object settings) {
        String installLocation = ((AddServerLocationVisualPanel) getComponent()).getInstallLocation();
        if (installLocation == null) {
            return;
        }
        instantiatingIterator.setInstallLocation(installLocation);
        instantiatingIterator.setConfigFile(
                ((AddServerLocationVisualPanel) getComponent()).getConfigurationLocation());
        instantiatingIterator.setServer("standalone");
        String serverPath = ((AddServerLocationVisualPanel) getComponent()).getInstallLocation() + File.separatorChar + "standalone";
        instantiatingIterator.setServerPath(serverPath);
        instantiatingIterator.setDeployDir(WildflyPluginUtils.getDeployDir(serverPath));
        instantiatingIterator.setAdminPort("" + WildflyPluginProperties.getInstance().getAdminPort());
        instantiatingIterator.setHost("localhost");
        instantiatingIterator.setPort("8080");
        instantiatingIterator.setPortOffset("0");
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    private boolean runningOnCorrectJdk(File path) {
        SpecificationVersion defPlatVersion = JavaPlatformManager.getDefault()
                .getDefaultPlatform().getSpecification().getVersion();
        // WF10 requires JDK8+
        if (!J2SE_PLATFORM_VERSION_17.equals(defPlatVersion.toString())) {
            return true;
        }
        WildflyPluginUtils.Version version = WildflyPluginUtils.getServerVersion(path);
        return version != null && version.compareToIgnoreUpdate(WildflyPluginUtils.WILDFLY_10_0_0) < 0;
    }
}
