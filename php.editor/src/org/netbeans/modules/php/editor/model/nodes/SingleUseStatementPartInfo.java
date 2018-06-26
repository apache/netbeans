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
