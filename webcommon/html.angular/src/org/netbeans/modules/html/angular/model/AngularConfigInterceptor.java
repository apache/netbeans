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
package org.netbeans.modules.html.angular.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.angular.index.AngularJsIndexer;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Roman Svitanic
 */
@FunctionInterceptor.Registration(priority = 17)
public class AngularConfigInterceptor implements FunctionInterceptor {

    private static final Pattern PATTERN = Pattern.compile("(.)*\\.config");  //NOI18N
    public static final String COMPONENT_PROP = "component"; //NOI18N
    public static final String COMPONENTS_PROP = "components"; //NOI18N
    public static final String CONTROLLER_SUFFIX = "Controller"; //NOI18N
    private static final String COMPONENTS_BASEDIR = "components"; //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject,
            DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (!AngularJsIndexer.isScannerThread()) {
            return Collections.emptyList();
        }
        for (FunctionArgument arg : args) {
            if (arg.getKind() == FunctionArgument.Kind.ARRAY) {
                FileObject fo = globalObject.getFileObject();
                TokenHierarchy<?> th = snapshot.getTokenHierarchy();
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, arg.getOffset());
                if (ts != null && fo != null) {
                    saveComponentsToIndex(fo, findComponents(ts, arg.getOffset()));
                }
            }
        }
        return Collections.emptyList();
    }

    public static void saveComponentsToIndex(FileObject fo, Set<String> components) {
        for (String component : components) {
            if (!component.isEmpty()) {
                // replace the uppercase chars in the component names with dashes and lowercase char
                // e.g. "myCompDemo" to "my-comp-demo"
                Matcher m = Pattern.compile("[A-Z]").matcher(component); //NOI18N
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    m.appendReplacement(sb, '-' + m.group().toLowerCase());
                }
                m.appendTail(sb);
                String componentDashName = sb.length() == 0 ? component : sb.toString();
                String templateName = COMPONENTS_BASEDIR + "/" + componentDashName + "/" + componentDashName + ".html"; //NOI18N
                String controllerName = String.valueOf(component.charAt(0)).toUpperCase()
                        .concat(component.substring(1)).concat(CONTROLLER_SUFFIX);
                // index components as "controller as" - for CC in partials
                AngularJsIndexer.addTemplateController(fo.toURI(), templateName, controllerName, component);
                // index the list of the components - for CC in ng-link directive
                AngularJsIndexer.addComponent(fo.toURI(), component);
            }
        }
    }

    public static Set<String> findComponents(TokenSequence<? extends JsTokenId> ts, int offset) {
        if (ts == null || offset == -1) {
            return Collections.emptySet();
        }
        ts.move(offset);
        if (!ts.moveNext()) {
            return Collections.emptySet();
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token.id() != JsTokenId.BRACKET_LEFT_BRACKET) {
            token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_BRACKET));
        }
        Set<String> result = new HashSet<>();
        while (token != null && token.id() != JsTokenId.BRACKET_RIGHT_BRACKET) {
            token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_BRACKET));
            if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                // we are in the anonymous object defining the component
                while (token != null && token.id() != JsTokenId.BRACKET_RIGHT_CURLY) {
                    // find the property identifier or end of an object
                    token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.BRACKET_RIGHT_CURLY));
                    if (token.text().toString().startsWith(COMPONENT_PROP)) {
                        // we have found "component" or "components" property identifier, now find the string value or an anonymous object
                        token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.STRING, JsTokenId.BRACKET_LEFT_CURLY));
                        if (token != null) {
                            if (token.id() == JsTokenId.STRING) {
                                // simple case "component: 'mycomponent'"
                                result.add(token.text().toString());
                            } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                                // case "components: { prop1: 'comp1', prop2 'comp2' }:
                                while (token != null && token.id() != JsTokenId.BRACKET_RIGHT_CURLY) {
                                    // try to find string values/names of components inside this object (value of components property)
                                    token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.STRING, JsTokenId.BRACKET_RIGHT_CURLY));
                                    if (token != null && token.id() == JsTokenId.STRING) {
                                        // component name has been found
                                        result.add(token.text().toString());
                                    }
                                    ts.moveNext();
                                }
                            }
                        }
                    } else {
                        ts.moveNext();
                    }
                }
            }
        }
        return result;
    }

}
