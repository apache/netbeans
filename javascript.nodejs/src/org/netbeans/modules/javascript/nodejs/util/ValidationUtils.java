/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
