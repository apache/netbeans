/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
