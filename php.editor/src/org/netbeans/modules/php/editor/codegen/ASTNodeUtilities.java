/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
