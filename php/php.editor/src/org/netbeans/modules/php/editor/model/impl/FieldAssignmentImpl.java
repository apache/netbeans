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

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.Scope;

/**
 * @author Radek Matous
 */
class FieldAssignmentImpl extends AssignmentImpl<FieldElementImpl> {
    private VariableNameImpl variableNameImpl;
    FieldAssignmentImpl(VariableNameImpl variableNameImpl, FieldElementImpl field, Scope scope, OffsetRange scopeRange, OffsetRange nameRange, String typeName) {
        super(field, scope, scopeRange, nameRange, typeName, field.isDeprecated());
        this.variableNameImpl = variableNameImpl;
    }

    @Override
    boolean canBeProcessed(String tName) {
        final String name = getName();
        if (!canBeProcessed(tName, VariousUtils.FIELD_TYPE_PREFIX + name) || !canBeProcessed(tName, VariousUtils.FIELD_TYPE_PREFIX + name.substring(1))) {
            return canBeProcessed(tName, VariousUtils.VAR_TYPE_PREFIX + variableNameImpl.getName())
                    && canBeProcessed(tName, VariousUtils.VAR_TYPE_PREFIX + variableNameImpl.getName().substring(1));
        }
        return true;
    }
}
