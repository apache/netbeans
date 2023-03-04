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

package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;

/**
 * Holds single 'use' statement info as well as one part of group 'use' statement.
 */
public final class SingleUseStatementPartInfo extends ASTNodeInfo<SingleUseStatementPart> {

    @NullAllowed
    private final GroupUseStatementPartInfo groupUseStatementPartInfo;
    private final UseStatement.Type type;


    private SingleUseStatementPartInfo(SingleUseStatementPart node, GroupUseStatementPartInfo groupUseStatementPartInfo, UseStatement.Type type) {
        super(node);
        assert type != null;
        this.groupUseStatementPartInfo = groupUseStatementPartInfo;
        this.type = type;
    }

    public static SingleUseStatementPartInfo create(SingleUseStatementPart node, UseStatement.Type type) {
        return new SingleUseStatementPartInfo(node, null, type);
    }

    public static SingleUseStatementPartInfo create(SingleUseStatementPart node, GroupUseStatementPartInfo groupUseStatementPartInfo) {
        UseStatement.Type type = node.getType() != null ? node.getType() : groupUseStatementPartInfo.getType();
        return new SingleUseStatementPartInfo(node, groupUseStatementPartInfo, type);
    }

    public UseStatement.Type getType() {
        return type;
    }

    public boolean isPartOfGroupUse() {
        return groupUseStatementPartInfo != null;
    }

    @Override
    public String getName() {
        if (groupUseStatementPartInfo == null) {
            return super.getName();
        }
        return getName(false);
    }

    /**
     * Returns name of this node info.
     * @param baseOffsets {@code true} for offsets of base name (the common part, placed before '{');
     *                    {@code false} for offsets of the part itself
     * @return name of this node info
     */
    public String getName(boolean baseOffsets) {
        assert groupUseStatementPartInfo != null;
        return QualifiedName.create(CodeUtils.compoundName(groupUseStatementPartInfo.getOriginalNode(), getOriginalNode(), baseOffsets)).toString();
    }

}
