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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.model.GroupUseScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.nodes.GroupUseStatementPartInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleUseStatementPartInfo;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;

public class GroupUseScopeImpl extends ScopeImpl implements GroupUseScope {

    private final UseScope.Type type;
    private final List<UseScope> useScopes;


    GroupUseScopeImpl(NamespaceScopeImpl inScope, GroupUseStatementPartInfo nodeInfo) {
        super(inScope, nodeInfo.getName(), inScope.getFile(), nodeInfo.getRange(), PhpElementKind.GROUP_USE_STATEMENT, false);
        type = CodeUtils.mapType(nodeInfo.getType());
        List<SingleUseStatementPart> items = nodeInfo.getOriginalNode().getItems();
        useScopes = new ArrayList<>(items.size());
        for (SingleUseStatementPart item : items) {
            useScopes.add(inScope.createUseStatementPart(SingleUseStatementPartInfo.create(item, nodeInfo)));
        }
    }

    @Override
    public List<UseScope> getUseScopes() {
        return Collections.unmodifiableList(useScopes);
    }

    @Override
    public UseScope.Type getType() {
        return type;
    }

}
