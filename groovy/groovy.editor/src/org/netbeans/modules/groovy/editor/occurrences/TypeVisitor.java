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

package org.netbeans.modules.groovy.editor.occurrences;

import java.util.Iterator;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.FindTypeUtils;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;

/**
 *
 * @author Martin Adamek
 */
public class TypeVisitor extends ClassCodeVisitorSupport {

    protected final SourceUnit sourceUnit;
    protected final AstPath path;
    protected final ASTNode leaf;
    protected final BaseDocument doc;
    protected final int cursorOffset;
    private final boolean visitOtherClasses;


    public TypeVisitor(SourceUnit sourceUnit, AstPath path, BaseDocument doc,
            int cursorOffset, boolean visitOtherClasses) {
        this.sourceUnit = sourceUnit;
        this.path = path;
        this.leaf = path.leaf();
        this.doc = doc;
        this.cursorOffset = cursorOffset;
        this.visitOtherClasses = visitOtherClasses;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public void collect() {
        // FIXME use snapshot TH
        doc.readLock();
        try {
            TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, cursorOffset);
            if (ts == null) {
                return;
            }
            Token<GroovyTokenId> token = ts.token();
            if (token == null) {
                return;
            }
            ts.movePrevious();
            if (!isValidToken(token, ts.token())) {
                return;
            }
        } finally {
            doc.readUnlock();
        }

        if (leaf instanceof Variable) {
            Variable variable = (Variable) leaf;
            for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
                ASTNode scope = it.next();
                if (scope instanceof ClosureExpression) {
                    VariableScope variableScope = ((ClosureExpression) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitClosureExpression((ClosureExpression) scope);
                        return;
                    }
                } else if (scope instanceof MethodNode) {
                    MethodNode method = (MethodNode) scope;
                    VariableScope variableScope = method.getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitParameters(method.getParameters(), variable);

                        // This might look awkward.  If we have caret location on the method parameter type, we want
                        // to walk through the whole file and look for the type occurrences. BUT if we have caret
                        // location on the method parameter itself (not the type) we don't want to go through the
                        // whole code, because it's out of the variable scope - and in that case we returns
                        boolean isParamType = false;
                        if (FindTypeUtils.isCaretOnClassNode(path, doc, cursorOffset)) {
                            isParamType = true;
                        }
                        if (!isParamType) {
                            super.visitMethod(method);
                            return;
                        }
                    }
                    super.visitMethod(method);
                } else if (scope instanceof ConstructorNode) {
                    ConstructorNode constructor = (ConstructorNode) scope;
                    VariableScope variableScope = (constructor).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitParameters(constructor.getParameters(), variable);
                    }
                    super.visitConstructor(constructor);
                } else if (scope instanceof ForStatement) {
                    VariableScope variableScope = ((ForStatement) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitForLoop((ForStatement) scope);
                        return;
                    }
                } else if (scope instanceof BlockStatement) {
                    VariableScope variableScope = ((BlockStatement) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitBlockStatement((BlockStatement) scope);
                        return;
                    }
                } else if (scope instanceof ClosureListExpression) {
                    VariableScope variableScope = ((ClosureListExpression) scope).getVariableScope();
                    if (variableScope != null && variableScope.getDeclaredVariable(variable.getName()) != null) {
                        visitClosureListExpression((ClosureListExpression) scope);
                        return;
                    }
                } else if (scope instanceof ClassNode) {
                    ClassNode classNode = (ClassNode) scope;
                    for (FieldNode fieldNode: classNode.getFields()) {
                        visitField(fieldNode);
                    }
                }
                else if (scope instanceof VariableExpression) {
                    visitVariableExpression((VariableExpression)scope);
                }
            }
        }

        if (visitOtherClasses) {
            ModuleNode moduleNode = (ModuleNode) path.root();
            for (ClassNode classNode : moduleNode.getClasses()) {
                visitClass(classNode);
            }
        }
    }

    /**
     * Children can override this if it has special requirement on selected token.
     */
    protected boolean isValidToken(Token<GroovyTokenId> currentToken, Token<GroovyTokenId> previousToken) {
        return true;
    }

    /**
     * Children can override this to do extra things with method/constructor parameters.
     */
    protected void visitParameters(Parameter[] parameters, Variable variable) {
    }
}
