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
package org.netbeans.modules.javascript.nodejs.util;

import java.io.File;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSourceRoots;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.util.NbBundle;

public final class ValidationUtils {

    public static final String NODE_PATH = "node.path"; // NOI18N
    public static final String NODE_SOURCES_PATH = "node.sources.path"; // NOI18N
    public static final String NPM_PATH = "npm.path"; // NOI18N
    public static final String EXPRESS_PATH = "express.path"; // NOI18N


    private ValidationUtils() {
    }

    @NbBundle.Messages("ValidationUtils.node.name=Node")
    public static void validateNode(ValidationResult result, String node) {
        String warning = ExternalExecutableValidator.validateCommand(node, Bundle.ValidationUtils_node_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(NODE_PATH, warning));
        }
    }

    @NbBundle.Messages({
        "ValidationUtils.node.sources.invalid=Node sources must be a directory",
        "# {0} - lib subdirectory",
        "ValidationUtils.node.sources.lib.invalid=Node sources must contain \"{0}\" subdirectory.",
    })
    public static void validateNodeSources(ValidationResult result, @NullAllowed String nodeSources) {
        if (nodeSources == null) {
            return;
        }
        File sources = new File(nodeSources);
        if (!sources.isDirectory()) {
            result.addWarning(new ValidationResult.Message(NODE_SOURCES_PATH, Bundle.ValidationUtils_node_sources_invalid()));
        } else if (!new File(sources, NodeJsSourceRoots.LIB_DIRECTORY).isDirectory()) {
            result.addWarning(new ValidationResult.Message(NODE_SOURCES_PATH, Bundle.ValidationUtils_node_sources_lib_invalid(NodeJsSourceRoots.LIB_DIRECTORY)));
        }
    }

    @NbBundle.Messages("ValidationUtils.npm.name=npm")
    public static void validateNpm(ValidationResult result, String npm) {
        String warning = ExternalExecutableValidator.validateCommand(npm, Bundle.ValidationUtils_npm_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(NPM_PATH, warning));
        }
    }

    @NbBundle.Messages("ValidationUtils.express.name=Express")
    public static void validateExpress(ValidationResult result, String express) {
        String warning = ExternalExecutableValidator.validateCommand(express, Bundle.ValidationUtils_express_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(EXPRESS_PATH, warning));
        }
    }

}
