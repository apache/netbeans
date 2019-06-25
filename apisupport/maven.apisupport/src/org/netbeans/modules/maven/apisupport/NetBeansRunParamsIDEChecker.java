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

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.util.Arrays;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Plugin;

/**
 * Ensures that {@code netbeans.run.params.ide} will be interpolated into {@code netbeans.run.params}.
 */
@ProjectServiceProvider(service=PrerequisitesChecker.class, projectTypes={
    @ProjectType(id="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM_APPLICATION), // cf. platformActionMappings.xml
    @ProjectType(id="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM) // cf. ideActionMappings.xml
})
public class NetBeansRunParamsIDEChecker implements PrerequisitesChecker {

    private static final String MASTER_PROPERTY = "netbeans.run.params"; // NOI18N
    static final String OLD_PROPERTY = "netbeans.run.params.ide"; // NOI18N
    static final String PROPERTY = "netbeans.run.params.debug"; // NOI18N - since nbm-maven-plugin 3.11.1

    public @Override boolean checkRunConfig(RunConfig config) {
        String val = config.getProperties().get(OLD_PROPERTY);
        MavenProject prj = config.getMavenProject();
        boolean usingNew = usingNbmPlugin311(prj);
        if (usingNew) {
            if (val == null) {
                return true;
            }
            //modified nbactions.xml file? just adjust to new property
            config.setProperty(PROPERTY, val);
            config.setProperty(OLD_PROPERTY, null);
            return true;
        }
        if (val == null) { //!usingNew
            val = config.getProperties().get(PROPERTY);
            if (val == null) {
                //using old version of nbm plugin but also not using the old or new property, so it should be save to continue.
                return true;
            }
            config.setProperty(OLD_PROPERTY, val);
        }
        //offer upgrade to new version.
        return removeInterpolation(prj.getFile());
        
    }

    @Messages({
        "# {0} - property name", "# {1} - pom.xml file", 
        "NetBeansRunParamsIDEChecker.msg_confirm=<html>New version of nbm-maven-plugin is available that doesn''t require pom.xml modification to debug or profile your project.<br>Upgrade to the new version of nbm-maven-plugin and remove the netbeans.run.params.ide property?",
        "NetBeansRunParamsIDEChecker.title_confirm=Upgrade nbm-maven-plugin to newer version",
        "NetBeansRunParamsIDEChecker.upgradeButton=Upgrade nbm-maven-plugin"
    })
    private static boolean removeInterpolation(File pom) {
        Object upgrade = NetBeansRunParamsIDEChecker_upgradeButton();
        Confirmation dd = new Confirmation(NetBeansRunParamsIDEChecker_msg_confirm(OLD_PROPERTY, pom), NetBeansRunParamsIDEChecker_title_confirm());
        dd.setOptions(new Object[] {upgrade, NotifyDescriptor.CANCEL_OPTION} );
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret != upgrade) {
            return true;
        }
        if (ret == upgrade) {
            Utilities.performPOMModelOperations(FileUtil.toFileObject(pom), 
                    Arrays.<ModelOperation<POMModel>>asList(new ModelOperation[] { createUpgradePluginOperation(), createRemoveIdePropertyOperation()}));
            return false;
        }
        return false;
    }

    private static ModelOperation<POMModel> createUpgradePluginOperation() {
        return new ModelOperation<POMModel>() {
            public @Override
            void performOperation(POMModel model) {
                POMComponentFactory factory = model.getFactory();
                Project project = model.getProject();
                Build bld = project.getBuild();
                if (bld == null) {
                    bld = factory.createBuild();
                    project.setBuild(bld);
                }
                Plugin plg = PluginBackwardPropertyUtils.findPluginFromBuild(bld);
                if (plg == null) {
                    plg = factory.createPlugin();
                    plg.setGroupId(MavenNbModuleImpl.GROUPID_APACHE);
                    plg.setArtifactId(MavenNbModuleImpl.NBM_PLUGIN);
                    plg.setExtensions(Boolean.TRUE);
                    bld.addPlugin(plg);
                }
                plg.setVersion(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION); //
            }
        };
    }
    
    private static ModelOperation<POMModel> createRemoveIdePropertyOperation() {
      return new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                Project project = model.getProject();
                Properties properties = project.getProperties();
                if (properties != null) {
                    if (properties.getProperty(OLD_PROPERTY) != null) {
                        properties.setProperty(OLD_PROPERTY, null);
                    }
                    String args = properties.getProperty(MASTER_PROPERTY);
                    if (args != null) {
                        String ref = "${" + OLD_PROPERTY + "}"; // NOI18N
                        if (args.contains(ref)) {
                            args = args.replace(ref, "");
                            if (args.trim().length() == 0) {
                                args = null;
                            }
                            properties.setProperty(MASTER_PROPERTY, args);
                        }
                    }
                }
            }
      };
    }

    
    static boolean usingNbmPlugin311(MavenProject prj) {
        String v = PluginBackwardPropertyUtils.getPluginVersion(prj);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("3.11.1")) >= 0;
    } 
    
}
