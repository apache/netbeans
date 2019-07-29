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

import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Project;

/**
 *
 * @author mkleint
 */
class AddOSGiParamToNbmPluginConfiguration implements ModelOperation<POMModel> {
    private final boolean useOsgi;
    private final MavenProject mp;


    AddOSGiParamToNbmPluginConfiguration(boolean useOsgiDeps, MavenProject mavenProject) {
        mp = mavenProject;
        useOsgi = useOsgiDeps;
    }

    @Override
    public void performOperation(POMModel model) {
        Project p = model.getProject();
        Build bld = p.getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            p.setBuild(bld);
        }
        Plugin plg = PluginBackwardPropertyUtils.findPluginFromBuild(bld);
        if (plg == null) {
            //how come the plugin is not there? maybe using on wrong project?
            //check plugin management first.
            PluginManagement pm = bld.getPluginManagement();
            if (pm != null) {
                plg = PluginBackwardPropertyUtils.findPluginFromPluginManagement(pm);                
            }
            if (plg == null) { // should not happen to begin with
                plg = model.getFactory().createPlugin();
                bld.addPlugin(plg);
                plg.setGroupId(MavenNbModuleImpl.GROUPID_APACHE);
                plg.setArtifactId(MavenNbModuleImpl.NBM_PLUGIN);
                plg.setVersion(MavenNbModuleImpl.LATEST_NBM_PLUGIN_VERSION);
            }
        }
        Configuration cnf = plg.getConfiguration();
        if (cnf == null) {
            cnf = model.getFactory().createConfiguration();
            plg.setConfiguration(cnf);
        }
        cnf.setSimpleParameter("useOSGiDependencies", Boolean.toString(useOsgi));
    }

}
