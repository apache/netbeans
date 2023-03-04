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

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Include;

/**
 * @author Radek Matous
 */
public class IncludeInfo extends ASTNodeInfo<Include> {

    IncludeInfo(Include node) {
        super(node);
    }

    public static IncludeInfo create(Include include) {
        return new IncludeInfo(include);
    }

    @Override
    public Kind getKind() {
        return Kind.INCLUDE;
    }

    @Override
    public String getName() {
        Include incl = getOriginalNode();
        return VariousUtils.resolveFileName(incl);
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(getName()).toName();
    }

    @Override
    public OffsetRange getRange() {
        Include incl = getOriginalNode();
        Expression expression = incl.getExpression();
        return new OffsetRange(expression.getStartOffset(), expression.getEndOffset());
    }

    public String getFileName() {
        return VariousUtils.resolveFileName(getOriginalNode());
    }

}
