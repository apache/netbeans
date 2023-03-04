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

package org.netbeans.modules.web.webmodule;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * This class is a bridge between the deprecated {@link WebFrameworkProvider#getConfigurationPanel}
 * and {@link WebFrameworkProvider#extend} methods and the new {@link WebModuleExtender} class
 * which replaces them.
 *
 * @author Andrei Badea
 */
@SuppressWarnings("deprecation")
public class WebModuleExtenderBridge extends WebModuleExtender {

    private final WebFrameworkProvider framework;
    private final WebModule webModule;
    private final ExtenderController controller;
    private final FrameworkConfigurationPanel configPanel;
    private final WizardDescriptor wizard;

    private Map<String, Object> oldProps;

    public static WebModuleExtenderBridge create(WebFrameworkProvider framework, WebModule webModule, ExtenderController controller) {
        WebModuleExtenderBridge result = new WebModuleExtenderBridge(framework, webModule, controller);
        result.initialize();
        return result;
    }

    private WebModuleExtenderBridge(WebFrameworkProvider framework, WebModule webModule, final ExtenderController controller) {
        this.framework = framework;
        this.webModule = webModule;
        this.controller = controller;
        FrameworkConfigurationPanel tmpPanel = framework.getConfigurationPanel(webModule);
        configPanel = (tmpPanel != null) ? tmpPanel : new EmptyConfigPanel();
        // we don't want to send configPanel to the wizard descriptor in order to
        // keep full control over the methods called on configPanel,
        // so we create a special panel for the wizard
        @SuppressWarnings("unchecked") // NOI18N
        WizardDescriptor tmpWizard = new WizardDescriptor(new WizardDescriptor.Panel[] { new EmptyPanel() });
        wizard = tmpWizard;
    }

    /**
     * Not done in the constructor to avoid escaping "this" before the constructor
     * has finished executing.
     */
    private void initialize() {
        wizard.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String message = (String) wizard.getProperty(WizardDescriptor.PROP_ERROR_MESSAGE);
                if (message != null && message.trim().length() == 0) {
                    // many WizardDescriptor clients pass " " for no error message to ensure
                    // that it still takes up vertical space
                    message = null;
                }
                controller.setErrorMessage(message); // NOI18N
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        configPanel.addChangeListener(listener);
    }

    @SuppressWarnings("unchecked")
    public Set<FileObject> extend(WebModule webModule) {
        configPanel.storeSettings(wizard);
        return (Set<FileObject>) framework.extend(webModule);
    }

    public JComponent getComponent() {
        return (JComponent) configPanel.getComponent();
    }

    public HelpCtx getHelp() {
        return configPanel.getHelp();
    }

    public boolean isValid() {
        return configPanel.isValid();
    }

    public void removeChangeListener(ChangeListener listener) {
        configPanel.removeChangeListener(listener);
    }

    @SuppressWarnings("unchecked")
    public void update() {
        if (oldProps != null) {
            for (Map.Entry<String, Object> entry : oldProps.entrySet()) {
                wizard.putProperty(entry.getKey(), null);
            }
        }
        Map<String, Object> newProps = controller.getProperties().getProperties();
        for (Map.Entry<String, Object> entry : newProps.entrySet()) {
            wizard.putProperty(entry.getKey(), entry.getValue());
        }
        oldProps = newProps;
        configPanel.readSettings(wizard);
    }

    private static class EmptyPanel implements WizardDescriptor.Panel {

        private JPanel component;

        public void addChangeListener(ChangeListener l) {
        }

        public Component getComponent() {
            if (component == null) {
                component = new JPanel();
            }
            return component;
        }

        public HelpCtx getHelp() {
            return null;
        }

        public boolean isValid() {
            return true;
        }

        public void readSettings(Object settings) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public void storeSettings(Object settings) {
        }
    }

    /**
     * An empty framework configuration panel to which WebModuleExtenderBridge will delegate
     * when the framework doesn't have a framework configuration panel.
     */
    private static final class EmptyConfigPanel extends EmptyPanel implements FrameworkConfigurationPanel {

        @Override
        public Component getComponent() {
            return null;
        }

        public void enableComponents(boolean enable) {
        }
    }
}
