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
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerPropertiesPanel implements WizardDescriptor.Panel, ChangeListener {

    private WizardDescriptor wizard;
    private AddServerPropertiesVisualPanel component;
    private WildflyInstantiatingIterator instantiatingIterator;

    /** Creates a new instance of AddServerPropertiesPanel */
    public AddServerPropertiesPanel(WildflyInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    @Override
    public boolean isValid() {
        AddServerPropertiesVisualPanel panel = (AddServerPropertiesVisualPanel)getComponent();

        String host = panel.getHost();
        String portoffSet = panel.getPortOffSet();
        String port = panel.getPort();
        String adminPort = panel.getManagementPort();

        if(panel.isLocalServer()){
            // wrong domain path
            String path = panel.getDomainPath();
            File serverDirectory = new File(WildflyPluginProperties.getInstance().getInstallLocation());

            if (path.length() < 1) {
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_SpecifyDomainPath"));  //NOI18N
                return false;
            }
            if (!WildflyPluginUtils.isGoodJBInstanceLocation(serverDirectory, new File(path))) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_WrongDomainPath"));  //NOI18N
                return false;
            }
            if (host.length() < 1) {
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_EnterHost"));  //NOI18N
                return false;
            }

            for (String url : InstanceProperties.getInstanceList()) {
                InstanceProperties props = InstanceProperties.getInstanceProperties(url);
                if (props == null) {
                    // probably removed
                    continue;
                }

                String property = null;
                try {
                    property = props.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR);
                } catch (IllegalStateException ex) {
                    // instance removed
                }

                if (property == null) {
                    continue;
                }

                /*try {
                    String root = new File(property).getCanonicalPath();

                    if (root.equals(new File(path).getCanonicalPath())) {
                        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InstanceExists"));  //NOI18N
                        return false;
                    }
                } catch (IOException ex) {
                    // It's normal behaviour when instance is something else then jboss instance
                    continue;
                }*/
            }

            try{
                Integer.parseInt(port);
            } catch(Exception e) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InvalidPort"));  //NOI18N
                return false;
            }
            try{
                Integer.parseInt(portoffSet);
            } catch(Exception e) {
                portoffSet = "0";
            }

            try{
                Integer.parseInt(adminPort);
            } catch(Exception e) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InvalidPort"));  //NOI18N
                return false;
            }


        } else { //remote
            if (host.length() < 1){
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_EnterHost"));  //NOI18N
                return false;
            }
            if (port.length() < 1) {
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_EnterPort"));  //NOI18N
                return false;
            }
        }

        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);

        instantiatingIterator.setHost(host);
        instantiatingIterator.setPort(port);
        instantiatingIterator.setAdminPort(adminPort);
        instantiatingIterator.setPortOffset(portoffSet);
        instantiatingIterator.setServer(panel.getDomain());
        instantiatingIterator.setServerPath(panel.getDomainPath());
        instantiatingIterator.setDeployDir(WildflyPluginUtils.getDeployDir( panel.getDomainPath()));

        return true;
    }


    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddServerPropertiesVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public void stateChanged(ChangeEvent ev) {
        new ArrayList<>(listeners).forEach(l -> l.stateChanged(ev));
    }

    private final transient Set<ChangeListener> listeners = ConcurrentHashMap.newKeySet(2);
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
        if (wizard == null)
            wizard = (WizardDescriptor)settings;
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_jboss_properties"); //NOI18N
    }

    void installLocationChanged() {
        if (component != null)
            component.installLocationChanged();
    }
}
