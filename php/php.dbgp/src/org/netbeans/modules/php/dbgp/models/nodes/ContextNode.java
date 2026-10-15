/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.dbgp.models.nodes;

import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.dbgp.ModelNode;
import org.netbeans.modules.php.dbgp.models.VariablesModelFilter.FilterType;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;

/**
 * Represent context which contains varaibles ( VariableNodes ). Could be Local,
 * Superglobal,...
 *
 * @author ads
 *
 */
public abstract class ContextNode extends AbstractModelNode implements ModelNode {
    private static final String SUPER_GLOBAL = "Superglobals"; // NOI18N
    private static final String SUPER_ICON = "org/netbeans/modules/debugger/resources/watchesView/SuperVariable"; // NOI18N
    private final String myName;
    private final int myIndex;

    protected ContextNode(Context ctx, List<Property> properties) {
        super(null, properties);
        myName = ctx.getContext();
        myIndex = ctx.getId();
    }

    @Override
    public String getName() {
        return myName;
    }

    public int getIndex() {
        return myIndex;
    }

    public int getVaraibleSize() {
        return getVariables().size();
    }

    @Override
    public ModelNode[] getChildren(int from, int to) {
        List<AbstractVariableNode> subList = getVariables().subList(from, to);
        return subList.toArray(new ModelNode[0]);
    }

    @Override
    public int getChildrenSize() {
        return getVariables().size();
    }

    @Override
    public String getIconBase() {
        if (isGlobal()) {
            return SUPER_ICON;
        }
        return null;
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return getChildrenSize() == 0;
    }

    public boolean equalsTo(ContextNode node) {
        String name = node.myName;
        if (name == null) {
            return myName == null;
        } else {
            return name.equals(myName);
        }
    }

    public boolean isGlobal() {
        return SUPER_GLOBAL.equals(getDbgpName());
    }

    @Override
    protected boolean isTypeApplied(Set<FilterType> set) {
        if (!set.contains(FilterType.SUPERGLOBALS)) {
            return !isGlobal();
        }
        return true;
    }

    private String getDbgpName() {
        return myName;
    }

}
