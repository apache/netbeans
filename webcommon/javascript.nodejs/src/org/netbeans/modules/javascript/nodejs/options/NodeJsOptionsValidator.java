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
