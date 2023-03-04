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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;

/**
 * @author Radek Matous
 */
public class ConstantDeclarationInfo extends ClassConstantDeclarationInfo {

    ConstantDeclarationInfo(final Identifier node, final String value, final ConstantDeclaration constantDeclaration) {
        super(node, value, constantDeclaration);
    }

    public static List<? extends ConstantDeclarationInfo> create(ConstantDeclaration constantDeclaration) {
        List<ConstantDeclarationInfo> retval = new ArrayList<>();
        List<Identifier> names = constantDeclaration.getNames();
        for (Identifier identifier : names) {
            String value = null;
            for (final Expression expression : constantDeclaration.getInitializers()) {
                value = getConstantValue(expression);
                if (value != null) {
                    break;
                }
            }
            retval.add(new ConstantDeclarationInfo(identifier, value, constantDeclaration));
        }
        return retval;
    }

    @Override
    public Kind getKind() {
        return Kind.CONSTANT;
    }

}
