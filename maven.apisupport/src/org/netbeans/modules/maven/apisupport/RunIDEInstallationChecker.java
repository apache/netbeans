/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.xml.namespace.QName;
import org.apache.maven.cli.MavenCli;
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
                        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(RunIDEInstallationChecker_message(netbeansInstallation, MavenCli.DEFAULT_USER_SETTINGS_FILE), RunIDEInstallationChecker_title(), NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
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
        FileObject settingsXml = FileUtil.toFileObject(MavenCli.DEFAULT_USER_SETTINGS_FILE);
        if (settingsXml == null) {
            settingsXml = FileUtil.copyFile(FileUtil.getConfigFile("Maven2Templates/settings.xml"), FileUtil.createFolder(MavenCli.DEFAULT_USER_SETTINGS_FILE.getParentFile()), "settings");
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
