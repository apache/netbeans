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
