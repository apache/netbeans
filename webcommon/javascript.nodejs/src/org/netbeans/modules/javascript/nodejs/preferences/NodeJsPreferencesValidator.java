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

package org.netbeans.modules.javascript.nodejs.preferences;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.ValidationUtils;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

public final class NodeJsPreferencesValidator {

    private static final String START_FILE = "start.file"; // NOI18N
    private static final String DEBUG_PORT = "debug.port"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    public static String getCustomizerCategory(ValidationResult result) {
        assert !result.isFaultless() : result.getFirstErrorMessage() + " + " + result.getFirstWarningMessage();
        List<ValidationResult.Message> messages = new ArrayList<>();
        messages.addAll(result.getErrors());
        messages.addAll(result.getWarnings());
        for (ValidationResult.Message message : messages) {
            switch (message.getSource().toString()) {
                case ValidationUtils.NODE_PATH:
                case ValidationUtils.NODE_SOURCES_PATH:
                case DEBUG_PORT:
                    return NodeJsCustomizerProvider.CUSTOMIZER_IDENT;
                case START_FILE:
                    return WebClientProjectConstants.CUSTOMIZER_RUN_IDENT;
                default:
                    assert false : "Unknown validation source: " + message.getSource().toString();
            }
        }
        assert false;
        return NodeJsCustomizerProvider.CUSTOMIZER_IDENT;
    }

    public ValidationResult getResult() {
        return result;
    }

    public NodeJsPreferencesValidator validate(Project project, boolean validateNodeSources) {
        NodeJsPreferences preferences = NodeJsSupport.forProject(project).getPreferences();
        if (!preferences.isEnabled()) {
            return this;
        }
        validateNode(preferences.isDefaultNode(), preferences.getNode(), validateNodeSources ? preferences.getNodeSources() : null);
        return this;
    }

    public NodeJsPreferencesValidator validateNode(String node) {
        ValidationUtils.validateNode(result, node);
        return this;
    }

    public NodeJsPreferencesValidator validateCustomizer(boolean enabled, boolean defaultNode, String node, String nodeSources, int debugPort) {
        if (!enabled) {
            return this;
        }
        validateNode(defaultNode, node, nodeSources);
        validateDebugPort(debugPort);
        return this;
    }

    @NbBundle.Messages("NodeJsPreferencesValidator.startFile.name=Start file")
    public NodeJsPreferencesValidator validateRun(String startFile, String args) {
        String warning = FileUtils.validateFile(Bundle.NodeJsPreferencesValidator_startFile_name(), startFile, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(START_FILE, warning));
        }
        return this;
    }

    @NbBundle.Messages("NodeJsPreferencesValidator.debugPort.invalid=Debug port is invalid")
    public NodeJsPreferencesValidator validateDebugPort(int debugPort) {
        if (debugPort < 0
                || debugPort > 65535) {
            result.addWarning(new ValidationResult.Message(DEBUG_PORT, Bundle.NodeJsPreferencesValidator_debugPort_invalid()));
        }
        return this;
    }

    private void validateNode(boolean defaultNode, String node, String nodeSources) {
        if (defaultNode) {
            return;
        }
        ValidationUtils.validateNode(result, node);
        ValidationUtils.validateNodeSources(result, nodeSources);
    }

}
