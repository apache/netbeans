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
package org.netbeans.modules.php.dbgp.models.nodes;

import java.util.Set;
import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
class ScalarTypeVariableNode extends org.netbeans.modules.php.dbgp.models.VariablesModel.AbstractVariableNode {
    private static final String TYPE_FLOAT = "TYPE_Float"; // NOI18N
    private static final String TYPE_INT = "TYPE_Int"; // NOI18N
    private static final String TYPE_STRING = "TYPE_String"; // NOI18N
    private static final String TYPE_NULL = "TYPE_Null"; // NOI18N
    public static final String INTEGER = "integer"; // NOI18N
    public static final String INT = "int"; // NOI18N
    public static final String FLOAT = "float"; // NOI18N
    public static final String STRING = "string"; // NOI18N

    ScalarTypeVariableNode(Property property, AbstractModelNode parent) {
        super(property, parent);
    }

    @Override
    public String getType() {
        String type = super.getType();
        String bundleKey;
        switch (type) {
            case INTEGER:
            case INT:
                bundleKey = TYPE_INT;
                break;
            case FLOAT:
                bundleKey = TYPE_FLOAT;
                break;
            case STRING:
                bundleKey = TYPE_STRING;
                break;
            default:
                bundleKey = TYPE_NULL;
                break;
        }
        return NbBundle.getMessage(ScalarTypeVariableNode.class, bundleKey);
    }

    @Override
    protected boolean isTypeApplied(Set<FilterType> filters) {
        return filters.contains(FilterType.SCALARS);
    }

}
