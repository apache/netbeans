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
package org.netbeans.modules.cloud.amazon.ui.serverplugin;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEServerInstanceProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class AmazonJ2EEServerWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor> {

    public static final String KEY_ID = "access-key-id";
    public static final String KEY = "secret-access-key";
    
    private AmazonJ2EEServerWizardComponent component;
    private WizardDescriptor wd;
    private ChangeSupport listeners = new ChangeSupport(this);
    
    public AmazonJ2EEServerWizardPanel() {
    }
    
    @Override
    public Component getComponent() {
        return getComponentImpl();
    }
        
    AmazonJ2EEServerWizardComponent getComponentImpl() {
        if (component == null) {
            component = new AmazonJ2EEServerWizardComponent(this, (String)wd.getProperty("suggested-name"), null);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getPanelContentData());            
            component.init();
        }
        return component;
    }

    private static String[] getPanelContentData() {
        return new String[] {
                NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardDescriptor.name")
            };
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(AmazonJ2EEServerWizardPanel.class.getName());
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        this.wd = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }
    
    public void setErrorMessage(String message) {
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
    
    @Override
    public boolean isValid() {
        if (!getComponentImpl().hasAccount()) {
            setErrorMessage(NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.missingAccount"));
            return false;
        }
        if (getComponentImpl().getApplicationName().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.missingAppName"));
            return false;
        }
        if (getComponentImpl().getEnvironmentName().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.missingEnvName"));
            return false;
        }
        if (getComponentImpl().getURL().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.missingUrl"));
            return false;
        }
        if (getComponentImpl().getEnvironmentName().length() < 4 || getComponentImpl().getEnvironmentName().length() > 23) {
            setErrorMessage(NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.envNameLength"));
            return false;
        }
        setErrorMessage(null);
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }
    
    void fireChange() {
        listeners.fireChange();
    }

    @Override
    public void prepareValidation() {
    }

    @Override
    public void validate() throws WizardValidationException {
        String appName = getComponentImpl().getApplicationName();
        String envName = getComponentImpl().getEnvironmentName();
        String url = getComponentImpl().getURL();
        List<AmazonJ2EEInstance> list = getComponentImpl().getAmazonInstance().readJ2EEServerInstances();
        for (AmazonJ2EEInstance ins : list) {
            if (ins.getApplicationName().equals(appName) && 
                ins.getEnvironmentName().equals(envName)) {
                    throw new WizardValidationException(getComponentImpl(), "app/env already exist", 
                            NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.validation1", 
                            appName, envName));
            }
        }
        if (!getComponentImpl().getAmazonInstance().checkURLValidity(url)) {
            throw new WizardValidationException(getComponentImpl(), "url already exist", 
                    NbBundle.getMessage(AmazonJ2EEServerWizardPanel.class, "AmazonJ2EEServerWizardPanel.validation2", url));
        }
    }
    
    private boolean isNewApp(AmazonInstance ai, String appName) {
        return !ai.readApplicationNames().contains(appName);
    }

    public InstanceProperties createServer() {
        String envID = createAppEnv();
        try {
            AmazonJ2EEServerInstanceProvider.getProvider().refreshServers().get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return findServer(envID);
    }
    
    private String createAppEnv() {
        String appName = getComponentImpl().getApplicationName();
        String envName = getComponentImpl().getEnvironmentName();
        String url = getComponentImpl().getURL();
        String containerType = getComponentImpl().getContainerType();
        String template = getComponentImpl().getTemplateName();
        AmazonInstance ai = getComponentImpl().getAmazonInstance();
        
        boolean newApp = isNewApp(ai, appName);
        if (newApp) {
            ai.createApplication(appName);
            ai.createInitialEmptyApplication(appName);
        }
        return ai.createEnvironment(appName, envName, url, containerType, template);
    }
    
    private InstanceProperties findServer(String envID) {
        String appName = getComponentImpl().getApplicationName();
        String containerType = getComponentImpl().getContainerType();
        String uri = AmazonJ2EEInstance.createURL(appName, envID, containerType);
        InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
        assert ip != null : "cannot find instance for " + uri;
        return ip;
    }

}
