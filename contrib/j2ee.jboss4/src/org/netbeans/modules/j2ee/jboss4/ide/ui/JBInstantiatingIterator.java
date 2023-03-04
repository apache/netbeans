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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;


/**
 *
 * @author Ivan Sidorkin
 */
public class JBInstantiatingIterator implements WizardDescriptor.InstantiatingIterator, ChangeListener {
    
    private static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    private static final String JBOSS_5_JAVA_OPTS = "-Xms128m -Xmx512m"; // NOI18N
    
    private static final String JBOSS_6_JAVA_OPTS = "-Xms128m -Xmx512m -XX:MaxPermSize=256m"; // NOI18N

    /**
     * skipServerLocationStep allow to skip Select Location step in New Instance Wizard
     * if this step allready was passed
     */
    public final boolean skipServerLocationStep = false;
    
    private transient AddServerLocationPanel locationPanel = null;
    private transient AddServerPropertiesPanel propertiesPanel = null;
    
    private WizardDescriptor wizard;
    private transient int index = 0;
    private transient WizardDescriptor.Panel[] panels = null;
    
    
    // private InstallPanel panel;
    private transient Set listeners = new HashSet(1);
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    public void previousPanel() {
        index--;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public String name() {
        return "JBoss Server AddInstanceIterator";  // NOI18N
    }
    
    public static void showInformation(final String msg,  final String title) {
        Runnable info = new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                d.setTitle(title);
                DialogDisplayer.getDefault().notify(d); 
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            info.run();
        } else {
            SwingUtilities.invokeLater(info);
        }
    }
    
    public Set instantiate() throws IOException {
        Set result = new HashSet();
        
        String displayName =  (String)wizard.getProperty(PROP_DISPLAY_NAME);
        JBPluginUtils.Version version = JBPluginUtils.getServerVersion(new File(installLocation));
        String url = JBDeploymentFactory.URI_PREFIX;
        if(version != null && "7".equals(version.getMajorNumber())){
            url += "//"+host + ":" + port+"?targetType=as7";    // NOI18N
        } else {
            url += host + ":" + port;    // NOI18N
        }
        if (server != null && !server.equals(""))                           // NOI18N
            url += "#" + server;                                            // NOI18N
        url += "&"+ installLocation;                                        // NOI18N
      
        try {
            Map<String, String> initialProperties = new HashMap<String, String>();
            initialProperties.put(JBPluginProperties.PROPERTY_SERVER, server);
            initialProperties.put(JBPluginProperties.PROPERTY_DEPLOY_DIR, deployDir);
            initialProperties.put(JBPluginProperties.PROPERTY_SERVER_DIR, serverPath);
            initialProperties.put(JBPluginProperties.PROPERTY_ROOT_DIR, installLocation);
            initialProperties.put(JBPluginProperties.PROPERTY_HOST, host);
            initialProperties.put(JBPluginProperties.PROPERTY_PORT, port);
            initialProperties.put(JBPluginProperties.PROPERTY_JMX_PORT, jmxPort);

            if (version != null && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_6_0_0) >= 0) {
                initialProperties.put(JBPluginProperties.PROPERTY_JAVA_OPTS, JBOSS_6_JAVA_OPTS);
            } else if (version != null && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_5_0_0) >= 0) {
                initialProperties.put(JBPluginProperties.PROPERTY_JAVA_OPTS, JBOSS_5_JAVA_OPTS);
            }

            InstanceProperties ip = InstanceProperties.createInstanceProperties(url,
                    userName, password, displayName, initialProperties);

            result.add(ip);
        } catch (InstanceCreationException e){
            showInformation(e.getLocalizedMessage(), NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "MSG_INSTANCE_REGISTRATION_FAILED")); //NOI18N
            Logger.getLogger("global").log(Level.SEVERE, e.getMessage());
        }
        
        return result;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    protected String[] createSteps() {
        if(!skipServerLocationStep){
            return new String[] { NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_ServerLocation"),  // NOI18N
                    NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_Properties") };    // NOI18N
        }else{
            if (!JBPluginProperties.getInstance().isCurrentServerLocationValid()){
                return new String[] { NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_ServerLocation"),  // NOI18N
                        NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_Properties") };    // NOI18N
            } else {
                return new String[] { NbBundle.getMessage(JBInstantiatingIterator.class, "STEP_Properties") };    // NOI18N
            }
        }
    }
    
    protected final String[] getSteps() {
        if (steps == null) {
            steps = createSteps();
        }
        return steps;
    }
    
    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        if (locationPanel == null) {
            locationPanel = new AddServerLocationPanel(this);
            
            locationPanel.addChangeListener(this);
        }
        if (propertiesPanel == null) {
            propertiesPanel = new AddServerPropertiesPanel(this);
            
            propertiesPanel.addChangeListener(this);
        }
        
        if (skipServerLocationStep){
            if (!JBPluginProperties.getInstance().isCurrentServerLocationValid()){
                return new WizardDescriptor.Panel[] {
                    (WizardDescriptor.Panel)locationPanel,
                            (WizardDescriptor.Panel)propertiesPanel
                };
            } else {
                return new WizardDescriptor.Panel[] {
                    (WizardDescriptor.Panel)propertiesPanel
                };
            }
        }else{
            return new WizardDescriptor.Panel[] {
                (WizardDescriptor.Panel)locationPanel,
                        (WizardDescriptor.Panel)propertiesPanel
            };
        }
    }
    
    private transient String[] steps = null;
    
    protected final int getIndex() {
        return index;
    }
    
    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel result = getPanels()[index];
        JComponent component = (JComponent)result.getComponent();
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getSteps());  // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(getIndex()));// NOI18N
        return result;
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireChangeEvent();
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    private String host;
    private String port;
    private String jmxPort;
    private String userName="";
    private String password="";
    private String server;
    private String installLocation;
    private String deployDir;
    private String serverPath;
    
    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void setHost(String host){
        this.host = host.trim();
    }
    
    public void setPort(String port){
        this.port = port.trim();
    }

    public void setJmxPort(String jmxPort){
        this.jmxPort = jmxPort.trim();
    }
    
    public void setServer(String server){
        this.server = server;
    }
    
    public void setServerPath(String serverPath){
        this.serverPath = serverPath;
    }
    
    public void setDeployDir(String deployDir){
        this.deployDir = deployDir;
    }
    
    public void setInstallLocation(String installLocation){
        this.installLocation = installLocation;
        propertiesPanel.installLocationChanged();
    }
    
}
