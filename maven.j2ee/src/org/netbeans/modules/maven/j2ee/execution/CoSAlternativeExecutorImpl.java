/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.execution;

import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.spi.cos.CoSAlternativeExecutorImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * Implementation of the {@link CoSAlternativeExecutorImplementation} enables to changes the default
 * run/debug/profile behavior and does not force rebuild of application when one of these action is invoked.
 *
 * <p>
 * In combination with CoS/DoS feature this save time that was earlier needed for rebuild
 * application started before actual redeployment.
 *
 * <p>
 * See issue 230565 for some details about why this was needed.
 *
 * <p>
 * This class is <i>immutable</i> and thus <i>thread safe</i>.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 * @since 2.99
 */
@ProjectServiceProvider(
    service = {
        CoSAlternativeExecutorImplementation.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI
    }
)
public class CoSAlternativeExecutorImpl implements CoSAlternativeExecutorImplementation {

    @Override
    public boolean execute(RunConfig config, ExecutionContext executionContext) {
        // In some cases we want to proceed standard execution (e.g. when running single main file)
        if (isSet(config, ExecutionConstants.STANDARD_EXECUTION)) {
            return false;
        }

        if (isSet(config, ExecutionConstants.SKIP_BUILD)) {
            DeploymentHelper.DeploymentResult result = DeploymentHelper.perform(config, executionContext);

            switch (result) {
                // If the build was successful or canceled, we don't want to proceed standard maven execution
                case SUCCESSFUL:
                case CANCELED:
                    return true;
                case FAILED:
                    return false;
            }
        }
        // If the skip.build property is not set, it means we do want to proceed standard execution
        return false;
    }

    private boolean isSet(RunConfig config, String key) {
        Object standardExecution = config.getInternalProperties().get(key);

        if (standardExecution instanceof Boolean) {
            return (Boolean) standardExecution;
        }
        return false;
    }
}
