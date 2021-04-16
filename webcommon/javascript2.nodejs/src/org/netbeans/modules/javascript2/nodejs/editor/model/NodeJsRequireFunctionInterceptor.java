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
                                    List<TypeUsage> modelTypes = new ArrayList<>();
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
