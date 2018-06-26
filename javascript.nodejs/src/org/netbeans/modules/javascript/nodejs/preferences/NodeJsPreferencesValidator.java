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
