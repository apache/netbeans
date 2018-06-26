/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.occurrences;

import java.util.Iterator;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
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
                }
            }
        }

        if (visitOtherClasses) {
            ModuleNode moduleNode = (ModuleNode) path.root();
            for (Object object : moduleNode.getClasses()) {
                visitClass((ClassNode) object);
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
