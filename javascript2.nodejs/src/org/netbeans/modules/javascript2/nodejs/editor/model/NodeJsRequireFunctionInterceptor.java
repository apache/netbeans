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
package org.netbeans.modules.javascript2.nodejs.editor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.nodejs.editor.NodeJsUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 370)
public class NodeJsRequireFunctionInterceptor implements FunctionInterceptor {

    private static final Pattern METHOD_NAME = Pattern.compile(NodeJsUtils.REQUIRE_METHOD_NAME); //NOI18N

    @Override
    public Pattern getNamePattern() {
        return METHOD_NAME;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject,
            DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        FileObject fo = globalObject.getFileObject();
        if (fo == null) {
            // no action
            return Collections.emptyList();
        }

        if (args.size() == 1) {
            FunctionArgument theFirst = args.iterator().next();
            if (theFirst.getKind() == FunctionArgument.Kind.STRING) {
                String module = (String)theFirst.getValue();
                Source source = snapshot.getSource();
                if (source == null) {
                    // file doesn't exists yet
                    return Collections.emptyList();
                }
                JsObject requireObject = globalObject.getProperty(NodeJsUtils.REQUIRE_METHOD_NAME);
                if (requireObject != null) {
                    if (!(requireObject instanceof JsFunction)) {
                        JsObject parent = requireObject.getParent();
                        requireObject = factory.newFunction(scope, requireObject.getParent(), requireObject.getName(), new ArrayList<String>());
                        parent.addProperty(requireObject.getName(), requireObject);
                    }
                    if (requireObject instanceof JsFunction) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX).append(NodeJsUtils.getModuleName(module)).append('.');
                        ((JsFunction)requireObject).addReturnType(factory.newType(sb.toString() + NodeJsUtils.EXPORTS, -1, true));
                        sb.append(NodeJsUtils.MODULE).append('.').append(NodeJsUtils.EXPORTS);
                        ((JsFunction)requireObject).addReturnType(factory.newType(sb.toString(), -1, true));
                    }
                }
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot.getTokenHierarchy(), theFirst.getOffset());
                if (ts == null) {
                    return Collections.emptyList();
                }
                ts.move(theFirst.getOffset());
                if (ts.moveNext()) {
                    Token<? extends JsTokenId> token = LexUtilities.findPreviousIncluding(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.OPERATOR_SEMICOLON));
                    if (token != null && token.id() == JsTokenId.IDENTIFIER && NodeJsUtils.REQUIRE_METHOD_NAME.equals(token.text().toString())) {
                        token = LexUtilities.findPreviousIncluding(ts, Arrays.asList(JsTokenId.OPERATOR_ASSIGNMENT, JsTokenId.OPERATOR_SEMICOLON));
                        if (token != null && token.id() == JsTokenId.OPERATOR_ASSIGNMENT) {
                            token = LexUtilities.findPreviousIncluding(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.OPERATOR_SEMICOLON,
                                    JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_LEFT_PAREN));
                            if (token != null && token.id() == JsTokenId.IDENTIFIER) {
                                String objectName = token.text().toString();
                                JsObject jsObject = ((JsObject)scope).getProperty(objectName);
                                if (jsObject != null) {
                                    int assignmentOffset =  ts.offset() + token.length();
                                    List<TypeUsage> modelTypes = new ArrayList();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(NodeJsUtils.FAKE_OBJECT_NAME_PREFIX).append(NodeJsUtils.getModuleName(module)).append('.');
                                    modelTypes.add(factory.newType(sb.toString() + NodeJsUtils.EXPORTS, assignmentOffset, true));
                                    sb.append(NodeJsUtils.MODULE).append('.').append(NodeJsUtils.EXPORTS);
                                    modelTypes.add(factory.newType(sb.toString(), assignmentOffset, true));
                                    ts.move(theFirst.getOffset());
                                    int balance = 1;
                                    while (ts.moveNext() && balance > 0) {
                                        token = ts.token();
                                        if (token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                                            balance++;
                                        } else if (token.id() == JsTokenId.BRACKET_RIGHT_PAREN) {
                                            balance--;
                                        }
                                    }
                                    ts.movePrevious();
                                    token = LexUtilities.findNextIncluding(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.OPERATOR_SEMICOLON, JsTokenId.OPERATOR_DOT));
                                    if (token != null && token.id() != JsTokenId.OPERATOR_DOT) {
                                        Collection<? extends TypeUsage> assignments = jsObject.getAssignments();
                                        if (assignments.size() == 1) {
                                            TypeUsage assignment = assignments.iterator().next();
                                            if (assignment.getType().endsWith(NodeJsUtils.REQUIRE_METHOD_NAME)) {
                                                jsObject.clearAssignments();
                                            }
                                        }
                                        jsObject.addAssignment( modelTypes.get(0), assignmentOffset);
                                        jsObject.addAssignment( modelTypes.get(1), assignmentOffset);
                                    }
                                    return modelTypes;
                                }
                            }
                        }
                    }    
                }
            }
        }
        return Collections.emptyList();
    }

}
