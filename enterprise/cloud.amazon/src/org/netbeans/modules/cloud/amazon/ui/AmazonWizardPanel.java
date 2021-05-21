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
package org.netbeans.modules.cloud.amazon.ui;

import com.amazonaws.AmazonClientException;
import java.awt.Component;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.serverplugin.AmazonJ2EEInstance;
import org.netbeans.modules.cloud.common.spi.support.ui.CloudResourcesWizardPanel;
import org.netbeans.modules.cloud.common.spi.support.ui.ServerResourceDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class AmazonWizardPanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor> {

    public static final String KEY_ID = "access-key-id"; // String
    public static final String KEY = "secret-access-key"; // String
    public static final String REGION = "region"; // String
    public static final String CODE = "code"; // String
    
    private ChangeSupport listeners;
    private AmazonWizardComponent component;
    private List<ServerResourceDescriptor> servers;
    private WizardDescriptor wd = null;
    
    public AmazonWizardPanel() {
        listeners = new ChangeSupport(this);
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AmazonWizardComponent(this, null);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getPanelContentData());            
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(0));
        }
        return component;
    }

    static String[] getPanelContentData() {
        return new String[] {
                NbBundle.getMessage(AmazonWizardPanel.class, "LBL_ACIW_Amazon"),
                NbBundle.getMessage(AmazonWizardPanel.class, "LBL_ACIW_Resources")
            };
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wd = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        if (component != null) {
            settings.putProperty(KEY_ID, component.getKeyId());
            settings.putProperty(KEY, component.getKey());
            settings.putProperty(CloudResourcesWizardPanel.PROP_SERVER_RESOURCES, servers);
            settings.putProperty(REGION, component.getRegionUrl());
            settings.putProperty(CODE, component.getRegionCode());
        }
    }
    
    public void setErrorMessage(String message) {
        wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public boolean isValid() {
        if (component == null || wd == null) {
            // ignore this case
        } else if (component.getKeyId().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonWizardPanel.class, "AmazonWizardPanel.missingKeyID"));
            return false;
        } else if (component.getKey().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(AmazonWizardPanel.class, "AmazonWizardPanel.missingKey"));
            return false;
        }
        setErrorMessage("");
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
        getComponent().setCursor(Utilities.createProgressCursor(getComponent()));
    }

    @Override
    public void validate() throws WizardValidationException {
        try {
            servers = new ArrayList<ServerResourceDescriptor>();
            AmazonInstance ai = new AmazonInstance("temporary", component.getKeyId(), component.getKey(), component.getRegionUrl(), component.getRegionCode());
            try {
                ai.testConnection();
            } catch (AmazonClientException ex) {
                throw new WizardValidationException((JComponent)getComponent(), 
                        "connection failed", NbBundle.getMessage(AmazonWizardPanel.class, "AmazonWizardPanel.wrong.credentials"));
            }
            List<AmazonJ2EEInstance> list = ai.readJ2EEServerInstances();
            for (AmazonJ2EEInstance inst : list) {
                AmazonJ2EEInstanceNode n = new AmazonJ2EEInstanceNode(inst);
                n.showServerType();
                servers.add(new ServerResourceDescriptor("Server", n.getDisplayName(), "", ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16))));
            }
        } finally {
            getComponent().setCursor(null);
        }
    }
    
}
