/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.util.Arrays;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
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
                Plugin plg = bld.findPluginById(MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
                if (plg == null) {
                    plg = factory.createPlugin();
                    plg.setGroupId(MavenNbModuleImpl.GROUPID_MOJO);
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
        String v = PluginPropertyUtils.getPluginVersion(prj, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("3.11.1")) >= 0;
    } 
    
}
