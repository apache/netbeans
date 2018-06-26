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
package org.netbeans.modules.javascript.nodejs.options;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.javascript.nodejs.util.ValidationUtils;
import org.netbeans.modules.web.common.api.ValidationResult;


public class NodeJsOptionsValidator {

    private final ValidationResult result = new ValidationResult();


    // do not validate Express since it is really optional
    public NodeJsOptionsValidator validate(boolean validateNode, boolean includingNodeSources) {
        if (validateNode) {
            validateNode(includingNodeSources);
        }
        return validateNpm();
    }

    public NodeJsOptionsValidator validateNode(boolean includingNodeSources) {
        NodeJsOptions nodeJsOptions = NodeJsOptions.getInstance();
        return validateNode(nodeJsOptions.getNode(), includingNodeSources ? nodeJsOptions.getNodeSources() : null);
    }

    public NodeJsOptionsValidator validateNode(String node, @NullAllowed String nodeSources) {
        ValidationUtils.validateNode(result, node);
        ValidationUtils.validateNodeSources(result, nodeSources);
        return this;
    }

    public NodeJsOptionsValidator validateNpm() {
        return validateNpm(NodeJsOptions.getInstance().getNpm());
    }

    public NodeJsOptionsValidator validateNpm(String npm) {
        ValidationUtils.validateNpm(result, npm);
        return this;
    }

    public NodeJsOptionsValidator validateExpress() {
        return validateExpress(NodeJsOptions.getInstance().getExpress());
    }

    public NodeJsOptionsValidator validateExpress(String express) {
        ValidationUtils.validateExpress(result, express);
        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

}
