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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectTypes={
    @ProjectType(id="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM_APPLICATION),
    @ProjectType(id="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
})
public class NetBeansStartupArgs implements LateBoundPrerequisitesChecker {

    @Override public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        String actionName = config.getActionName();
        StartupExtender.StartMode mode;
        if (ActionProvider.COMMAND_RUN.equals(actionName)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else {
            return true;
        }
        List<String> args = new ArrayList<String>();
        InstanceContent ic = new InstanceContent();
        Project p = config.getProject();
        if (p != null) {
            ic.add(p);
            ActiveJ2SEPlatformProvider pp = p.getLookup().lookup(ActiveJ2SEPlatformProvider.class);
            if (pp != null) {
                ic.add(pp.getJavaPlatform());
            }
        }
        for (StartupExtender group : StartupExtender.getExtenders(new AbstractLookup(ic), mode)) {
            args.addAll(group.getArguments());
        }
        if (!args.isEmpty()) {
            StringBuilder b = new StringBuilder();
            for (String arg : args) {
                b.append("-J").append(arg).append(' ');
            }
            String other = config.getProperties().get(NetBeansRunParamsIDEChecker.OLD_PROPERTY);
            if (other != null) {
                config.setProperty(NetBeansRunParamsIDEChecker.OLD_PROPERTY, b.toString() + other);
            }
            String newProp = config.getProperties().get(NetBeansRunParamsIDEChecker.PROPERTY);
            if (newProp != null) {
                config.setProperty(NetBeansRunParamsIDEChecker.PROPERTY, b.toString() + newProp);
            } else {
                String prop = NetBeansRunParamsIDEChecker.usingNbmPlugin311(config.getMavenProject()) ? 
                                NetBeansRunParamsIDEChecker.PROPERTY : 
                                NetBeansRunParamsIDEChecker.OLD_PROPERTY;
                config.setProperty(prop, b.toString());                
            }
        }
        return true;
    }

}
