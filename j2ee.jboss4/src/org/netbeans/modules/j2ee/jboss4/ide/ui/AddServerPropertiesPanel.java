/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
    private JBInstantiatingIterator instantiatingIterator;
    
    /** Creates a new instance of AddServerPropertiesPanel */
    public AddServerPropertiesPanel(JBInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }
    
    public boolean isValid() {
        AddServerPropertiesVisualPanel panel = (AddServerPropertiesVisualPanel)getComponent();
        
        String host = panel.getHost();
        String port = panel.getPort();
        String jmxPort = panel.getJmxPort();
        
        if(panel.isLocalServer()){
            // wrong domain path
            String path = panel.getDomainPath();
            File serverDirectory = new File(JBPluginProperties.getInstance().getInstallLocation());
            
            if (path.length() < 1) {
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_SpecifyDomainPath"));  //NOI18N
                return false;
            }
            if (!JBPluginUtils.isGoodJBInstanceLocation(serverDirectory, new File(path))) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_WrongDomainPath"));  //NOI18N
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
                    property = props.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                } catch (IllegalStateException ex) {
                    // instance removed
                }

                if (property == null) {
                    continue;
                }

                try {
                    String root = new File(property).getCanonicalPath();

                    if (root.equals(new File(path).getCanonicalPath())) {
                        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InstanceExists"));  //NOI18N
                        return false;
                    }
                } catch (IOException ex) {
                    // It's normal behaviour when instance is something else then jboss instance
                    continue;
                }
            }
            
            try{
                Integer.parseInt(port);
            } catch(Exception e) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InvalidPort"));  //NOI18N
                return false;
            }
            try{
                Integer.parseInt(jmxPort);
            } catch(Exception e) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_InvalidJmxPort"));  //NOI18N
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
            if (jmxPort.length() < 1) {
                wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        NbBundle.getMessage(AddServerPropertiesPanel.class, "MSG_EnterJmxPort"));  //NOI18N
                return false;
            }
        }
        
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        
        instantiatingIterator.setHost(host);
        instantiatingIterator.setPort(port);
        instantiatingIterator.setJmxPort(jmxPort);
        instantiatingIterator.setServer(panel.getDomain());
        instantiatingIterator.setServerPath(panel.getDomainPath());
        instantiatingIterator.setDeployDir(JBPluginUtils.getDeployDir( panel.getDomainPath()));
        
        JBPluginProperties.getInstance().setDomainLocation(panel.getDomainPath());
        
        return true;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new AddServerPropertiesVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
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
    
    public void readSettings(Object settings) {
        if (wizard == null)
            wizard = (WizardDescriptor)settings;
    }
    
    public void storeSettings(Object settings) {
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_jboss_properties"); //NOI18N
    }
    
    void installLocationChanged() {
        if (component != null)
            component.installLocationChanged();
    }
}
