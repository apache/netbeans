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

package org.netbeans.modules.php.editor.codegen;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.codegen.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.codegen.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.codegen.SemiAttribute.ClassElementAttribute;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Andrei Badea
 */
public final class ASTNodeUtilities {

    private ASTNodeUtilities() {
    }

    public static Set<String> getVariablesInScope(ParserResult info, int offset, final VariableAcceptor acceptor) {
        List<ASTNode> path = NavUtils.underCaret(info, offset);
        Collections.reverse(path);
        SemiAttribute attr = SemiAttribute.semiAttribute(info);
        boolean ignoreGlobalScope = false;
        final Set<String> result = new HashSet<>();
        for (ASTNode node : path) {
            if (node instanceof FunctionDeclaration) {
                ignoreGlobalScope = true;
                class Scanner extends DefaultVisitor {
                    @Override
                    public void visit(Variable node) {
                        String name = SemiAttribute.extractVariableName(node);
                        if (name != null && acceptor.acceptVariable(name)) {
                            result.add(name);
                        }
                        super.visit(node);
                    }
                }
                new Scanner().scan(node);
            } else if (node instanceof ClassDeclaration) {
                ignoreGlobalScope = true;
                AttributedElement element = attr.getElement(node);
                if (element instanceof ClassElementAttribute) {
                    ClassElementAttribute classEl = (ClassElementAttribute) element;
                    for (AttributedElement variableEl : classEl.getElements(Kind.VARIABLE)) {
                        String name = variableEl.getName();
                        if (acceptor.acceptVariable(name)) {
                            result.add(name);
                        }
                    }
                    break;
                }
            }
        }
        if (!ignoreGlobalScope) {
            for (AttributedElement variableEl : attr.getGlobalElements(Kind.VARIABLE)) {
                String name = variableEl.getName();
                if (acceptor.acceptVariable(name)) {
                    result.add(name);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public interface VariableAcceptor {

        boolean acceptVariable(String variableName);
    }
}
