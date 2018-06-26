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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
    private final static class EmptyConfigPanel extends EmptyPanel implements FrameworkConfigurationPanel {

        @Override
        public Component getComponent() {
            return null;
        }

        public void enableComponents(boolean enable) {
        }
    }
}
