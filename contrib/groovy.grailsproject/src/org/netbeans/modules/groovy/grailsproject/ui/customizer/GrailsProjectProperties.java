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

package org.netbeans.modules.groovy.grailsproject.ui.customizer;

import java.io.IOException;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.groovy.grails.api.GrailsEnvironment;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class GrailsProjectProperties {

    private final GrailsProject project;

    private final GrailsProjectConfig config;

    private ComboBoxModel environmentModel;

    private ComboBoxModel javaPlatformModel;

    private ButtonModel displayBrowserModel;

    private ListCellRenderer javaPlatformRenderer;

    private String port;

    private String debugBrowser;

    private String vmOptions;

    public GrailsProjectProperties(Project project) {
        assert project instanceof GrailsProject;
        this.project = (GrailsProject) project;
        this.config = GrailsProjectConfig.forProject(project);
    }

    public GrailsProject getProject() {
        return project;
    }

    public ComboBoxModel getEnvironmentModel() {
        if (environmentModel == null) {
            GrailsEnvironment[] envs = GrailsEnvironment.standardValues();
            Object[] values = new Object[envs.length];
            for (int i = 0; i < envs.length; i++) {
                values[i] = new EnvironmentItem(envs[i]);
            }
            environmentModel = new DefaultComboBoxModel(values);

            GrailsEnvironment env = config.getEnvironment();
            if (env != null) {
                environmentModel.setSelectedItem(new EnvironmentItem(env));
            }
        }
        return environmentModel;
    }

    public ComboBoxModel getJavaPlatformModel() {
        if (javaPlatformModel == null) {
            javaPlatformModel = PlatformUiSupport.createPlatformComboBoxModel(
                config.getJavaPlatform().getProperties().get("platform.ant.name")); // NOI18N
        }
        return javaPlatformModel;
    }

    public ButtonModel getDisplayBrowserModel() {
        if (displayBrowserModel == null) {
            displayBrowserModel = new JToggleButton.ToggleButtonModel();
            displayBrowserModel.setSelected(config.getDisplayBrowser());
        }
        return displayBrowserModel;
    }

    public ListCellRenderer getJavaPlatformRenderer() {
        if (javaPlatformRenderer == null) {
            javaPlatformRenderer = PlatformUiSupport.createPlatformListCellRenderer();
        }
        return javaPlatformRenderer;
    }

    public String getPort() {
        if (port == null) {
            port = config.getPort();
        }
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getVmOptions() {
        if (vmOptions == null) {
            vmOptions = config.getVmOptions();
        }
        return vmOptions;
    }

    public void setVmOptions(String vmOptions) {
        this.vmOptions = vmOptions;
    }

    public String getDebugBrowser() {
        if (debugBrowser == null) {
            debugBrowser = config.getDebugBrowser();
        }
        return debugBrowser;
    }

    public void setDebugBrowser(String debugBrowser) {
        this.debugBrowser = debugBrowser;
    }

    public void save() {
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    saveProperties();
                    return null;
                }
            });
            ProjectManager.getDefault().saveProject(project);
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void saveProperties() throws IOException {
        if (debugBrowser != null) {
            config.setDebugBrowser(debugBrowser);
        }
        if (port != null) {
            config.setPort(port);
        }

        config.setVmOptions(vmOptions);

        EnvironmentItem item = (EnvironmentItem) getEnvironmentModel().getSelectedItem();
        if (item != null) {
            config.setEnvironment(item.getEnvironment());
        }

        Object platform = getJavaPlatformModel().getSelectedItem();
        if (platform != null) {
            config.setJavaPlatform(PlatformUiSupport.getPlatform(platform));
        }

        config.setDisplayBrowser(getDisplayBrowserModel().isSelected());
    }

    private static class EnvironmentItem {

        private final GrailsEnvironment environment;

        public EnvironmentItem(GrailsEnvironment environment) {
            this.environment = environment;
        }

        public GrailsEnvironment getEnvironment() {
            return environment;
        }

        @Override
        public String toString() {
            return NbBundle.getMessage(GeneralCustomizerPanel.class,
                    "GeneralCustomizerPanel." + environment.toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EnvironmentItem other = (EnvironmentItem) obj;
            if (this.environment != other.environment) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.environment != null ? this.environment.hashCode() : 0);
            return hash;
        }

    }
}
