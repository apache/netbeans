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
package org.netbeans.modules.php.project.runconfigs.validation;

import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.netbeans.modules.php.project.runconfigs.RunConfigInternal;
import org.openide.util.NbBundle;

/**
 * Validator for {@link RunConfigInternal}.
 */
public final class RunConfigInternalValidator {

    private RunConfigInternalValidator() {
    }

    public static String validateNewProject(RunConfigInternal config) {
        return validate(config, false);
    }

    public static String validateCustomizer(RunConfigInternal config) {
        return validate(config, true);
    }

    public static String validateConfigAction(RunConfigInternal config) {
        return validate(config, true);
    }

    @NbBundle.Messages("RunConfigInternalValidator.router.label=Router")
    private static String validate(RunConfigInternal config, boolean validateRouter) {
        String error;
        error = RemoteValidator.validateHost(config.getHostname());
        if (error != null) {
            return error;
        }
        error = RemoteValidator.validatePort(config.getPort());
        if (error != null) {
            return error;
        }
        if (validateRouter) {
            String routerRelativePath = config.getRouterRelativePath();
            if (StringUtils.hasText(routerRelativePath)) {
                error = BaseRunConfigValidator.validateRelativeFile(config.getWorkDir(), routerRelativePath, Bundle.RunConfigInternalValidator_router_label());
                if (error != null) {
                    return error;
                }
            }
        }
        return null;
    }

}
