/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * Code completion handler for name parameters.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
public class NamedParamsCompletion extends BaseCompletion {

    private CompletionContext context;

    @Override
    public boolean complete(List<CompletionProposal> proposals, CompletionContext context, int anchor) {
        this.context = context;

        ASTNode leaf = context.path.leaf();

        if (leaf instanceof ConstructorCallExpression) {
            ConstructorCallExpression constructorCall = (ConstructorCallExpression) leaf;
            
            Expression constructorArgs = constructorCall.getArguments();
            if (constructorArgs instanceof TupleExpression) {
                List<Expression> arguments = ((TupleExpression) constructorArgs).getExpressions();

                if (arguments.isEmpty()) {
                    completeNamedParams(proposals, anchor, constructorCall, null);
                } else {
                    for (Expression argExpression : arguments) {
                        if (argExpression instanceof NamedArgumentListExpression) {
                            completeNamedParams(proposals, anchor, constructorCall, (NamedArgumentListExpression) argExpression);
                        }
                    }
                }
            }
        }

        ASTNode leafParent = context.path.leafParent();
        ASTNode leafGrandparent = context.path.leafGrandParent();
        if (leafParent instanceof NamedArgumentListExpression &&
            leafGrandparent instanceof ConstructorCallExpression) {

            completeNamedParams(proposals, anchor, (ConstructorCallExpression) leafGrandparent, (NamedArgumentListExpression) leafParent);
        }

        return false;
    }

    private void completeNamedParams(
            List<CompletionProposal> proposals,
            int anchor,
            ConstructorCallExpression constructorCall,
            NamedArgumentListExpression namedArguments) {

        ClassNode type = constructorCall.getType();
        String prefix = context.getPrefix();

        for (FieldNode fieldNode : type.getFields()) {
            if (fieldNode.getLineNumber() < 0 || fieldNode.getColumnNumber() < 0) {
                continue;
            }

            String typeName = fieldNode.getType().getNameWithoutPackage();
            String name = fieldNode.getName();

            // If the prefix is empty, complete only missing parameters
            if ("".equals(prefix)) {
                if (isAlreadyPresent(namedArguments, name)) {
                    continue;
                }
            // Otherwise check if the field is starting with (and not equal to) the prefix
            } else {
                if (name.equals(prefix) || !name.startsWith(prefix)) {
                    continue;
                }
            }

            proposals.add(new CompletionItem.NamedParameter(typeName, name, anchor));
        }
    }

    /**
     * Check if the given name is in the list of named parameters.
     *
     * @param namedArgsExpression named parameters
     * @param name name
     * @return {@code true} if the given name is in the list of named parameters, {@code false} otherwise
     */
    private boolean isAlreadyPresent(NamedArgumentListExpression namedArgsExpression, String name) {
        if (namedArgsExpression == null) {
            return false;
        }
        List<MapEntryExpression> namedArgs = namedArgsExpression.getMapEntryExpressions();

        for (MapEntryExpression namedEntry : namedArgs) {
            String namedArgument = namedEntry.getKeyExpression().getText();
            if (namedArgument != null && namedArgument.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isParameterPrefix(NamedArgumentListExpression namedArgsExpression, String name) {
        List<MapEntryExpression> namedArgs = namedArgsExpression.getMapEntryExpressions();

        for (MapEntryExpression namedEntry : namedArgs) {
            String namedArgument = namedEntry.getKeyExpression().getText();
            if (namedArgument != null && name.startsWith(namedArgument)) {
                return true;
            }
        }
        return false;
    }
}
