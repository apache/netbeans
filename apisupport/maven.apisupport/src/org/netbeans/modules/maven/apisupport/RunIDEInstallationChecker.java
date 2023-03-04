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

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.xml.namespace.QName;
import org.apache.maven.cli.configuration.SettingsXmlConfigurationProcessor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.settings.Activation;
import org.netbeans.modules.maven.model.settings.Profile;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.SettingsQName;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * Ensures that {@code netbeans.installation} is defined for {@code nbm:run-ide}.
 */
@ProjectServiceProvider(service=PrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class RunIDEInstallationChecker implements PrerequisitesChecker {

    @Messages({
        "RunIDEInstallationChecker_title=Define netbeans.installation",
        "# {0} - NetBeans installation directory", "# {1} - settings.xml location", "RunIDEInstallationChecker_message="
            + "Running standalone modules or suites requires $'{'netbeans.installation} to be defined. "
            + "(Using the NetBeans Application project template avoids the need for this configuration.) "
            + "Define as {0} in {1} now?"
    })
    @Override public boolean checkRunConfig(RunConfig config) {
        if (config.getGoals().contains("nbm:run-ide")) {
            Project project = config.getProject();
            if (project != null) {
                NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
                if (nbmp != null && MavenNbModuleImpl.findIDEInstallation(project) == null) {
                        String netbeansInstallation = new File(System.getProperty("netbeans.home")).getParent();
                        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(RunIDEInstallationChecker_message(netbeansInstallation, SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE), RunIDEInstallationChecker_title(), NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
                            try {
                                defineIDE(netbeansInstallation);
                            } catch (IOException x) {
                                Exceptions.printStackTrace(x);
                            }
                        }
                        // config.setProperty(MavenNbModuleImpl.PROP_NETBEANS_INSTALL, netbeansInstallation);
                        return false;
                }
            }
        }
        return true;
    }
    
    static void setRunningIDEAsInstallation() {
        String netbeansInstallation = new File(System.getProperty("netbeans.home")).getParent();
        try {
            defineIDE(netbeansInstallation);
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }

    private static void defineIDE(final String netbeansInstallation) throws IOException {
        FileObject settingsXml = FileUtil.toFileObject(SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE);
        if (settingsXml == null) {
            settingsXml = FileUtil.copyFile(FileUtil.getConfigFile("Maven2Templates/settings.xml"), FileUtil.createFolder(SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE.getParentFile()), "settings");
        }
        Utilities.performSettingsModelOperations(settingsXml, Collections.<ModelOperation<SettingsModel>>singletonList(new ModelOperation<SettingsModel>() {
            public @Override void performOperation(SettingsModel model) {
                Profile netbeansIde = model.getSettings().findProfileById("netbeans-ide");
                if (netbeansIde != null) {
                    return;
                }
                netbeansIde = model.getFactory().createProfile();
                netbeansIde.setId("netbeans-ide");
                Activation activation = model.getFactory().createActivation();
                // XXX why does the model not have this property??
                QName ACTIVE_BY_DEFAULT = SettingsQName.createQName("activeByDefault", model.getSettingsQNames().getNamespaceVersion());
                activation.setChildElementText("activeByDefault", "true", ACTIVE_BY_DEFAULT);
                netbeansIde.setActivation(activation);
                org.netbeans.modules.maven.model.settings.Properties properties = model.getFactory().createProperties();
                properties.setProperty("netbeans.installation", netbeansInstallation);
                netbeansIde.setProperties(properties);
                model.getSettings().addProfile(netbeansIde);
            }
        }));
    }

}
