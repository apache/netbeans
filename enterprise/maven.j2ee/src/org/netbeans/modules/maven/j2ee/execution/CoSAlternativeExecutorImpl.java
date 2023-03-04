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
