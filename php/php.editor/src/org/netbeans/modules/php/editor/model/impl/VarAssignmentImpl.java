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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;

/**
 *
 * @author Radek Matous
 */
class VarAssignmentImpl extends AssignmentImpl<VariableNameImpl> {

    VarAssignmentImpl(
            VariableNameImpl var,
            Scope scope,
            boolean conditionalBlock,
            OffsetRange scopeRange,
            OffsetRange nameRange,
            Assignment assignment,
            Map<String, AssignmentImpl> allAssignments) {
        super(var, scope, scopeRange, nameRange, assignment, allAssignments, var.isDeprecated());
        setConditionalBlock(conditionalBlock);
    }

    VarAssignmentImpl(VariableNameImpl var, Scope scope, boolean conditionalBlock, OffsetRange scopeRange, OffsetRange nameRange, String typeName) {
        super(var, scope, scopeRange, nameRange, typeName, var.isDeprecated());
        setConditionalBlock(conditionalBlock);
    }

    @Override
    public String getNormalizedName() {
        return super.getNormalizedName();
    }

    @Override
    boolean canBeProcessed(String tName) {
        final String name = getName();
        return canBeProcessed(tName, VariousUtils.VAR_TYPE_PREFIX + name) && canBeProcessed(tName, VariousUtils.VAR_TYPE_PREFIX + name.substring(1));
    }

}
