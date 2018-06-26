/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
