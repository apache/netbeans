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
package org.netbeans.modules.javascript2.prototypejs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 360)
public class CreateInterceptor implements FunctionInterceptor {

    private static final Pattern PATTERN = Pattern.compile("Class\\.create");  //NOI18N
    private static final String CLASS = "Class";    //NOI18N
    private static final String INITIALIZE_METHOD_NAME = "initialize"; //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject, DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        FunctionArgument fArg = null;
        for (FunctionArgument farg : args) {
            if (farg.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                fArg = farg;
                break;
            }
        }
        if (fArg != null && fArg.getValue() instanceof JsObject) {
            JsObject configObject = (JsObject) fArg.getValue();
            String objectName = findTheName(configObject);
            if (objectName != null && !objectName.isEmpty()) {
                JsObject newObject = ((JsObject) scope).getProperty(objectName);
                if (newObject != null) {
                    JsObject constructor = configObject.getProperty(INITIALIZE_METHOD_NAME);
                    if (constructor instanceof JsFunction) {
                        // we need to replace the original object with this constructor
                        List<Identifier> paramNames = new ArrayList<>();
                        for (JsObject param : ((JsFunction) constructor).getParameters()) {
                            paramNames.add(param.getDeclarationName());
                        }
                        JsObject newConstructor = factory.newFunction(scope, newObject.getParent(), newObject.getDeclarationName(), paramNames, constructor.getOffsetRange());
                        for (JsObject param : ((JsFunction) constructor).getParameters()) {
                            JsObject newParam = ((JsFunction)newConstructor).getParameter(param.getName());
                            // add assignments to know the types from js doc
                            for (TypeUsage type : param.getAssignments()) {
                                newParam.addAssignment(type, type.getOffset());
                            }
                            // coppy occurrences
                            for (Occurrence occurrence : param.getOccurrences()) {
                                newParam.addOccurrence(occurrence.getOffsetRange());
                            }
                        }
                        for (JsObject property : constructor.getProperties().values()) {
                            String propertyName = property.getName();
                            JsObject reference = factory.newReference(newConstructor, propertyName, property.getOffsetRange(), property, property.isDeprecated(), property.getModifiers());
                            newConstructor.addProperty(propertyName, reference);
                            if (newObject.getProperty(propertyName) != null) {
                                for (Occurrence occurrence : newObject.getProperty(propertyName).getOccurrences()) {
                                    reference.addOccurrence(occurrence.getOffsetRange());
                                }
                            }
                        }
                        newObject.getParent().addProperty(objectName, newConstructor);
                        // copy all occurrences
                        for (Occurrence occurrence : newObject.getOccurrences()) {
                            newConstructor.addOccurrence(occurrence.getOffsetRange());
                        }
                        newObject = newConstructor;
                    }
                    for (JsObject property : configObject.getProperties().values()) {
                        String propertyName = property.getName();
                        if (!INITIALIZE_METHOD_NAME.equals(propertyName)) {
                            JsObject reference = factory.newReference(newObject, propertyName, property.getDeclarationName().getOffsetRange(), property, true, property.getModifiers());
                            newObject.addProperty(propertyName, reference);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private String findTheName(JsObject configObject) {
        String name = null;
        FileObject fo = configObject.getFileObject();
        Source source = Source.create(fo);
        TokenHierarchy<?> th = source.createSnapshot().getTokenHierarchy();
        if (th == null) {
            return null;
        }
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, configObject.getOffset());
        Token<? extends JsTokenId> token;
        if (ts == null) {
            return null;
        }
        ts.move(configObject.getOffset());
        if (!ts.moveNext()) {
            return null;
        }
        token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
        while (!(token.id() == JsTokenId.IDENTIFIER && CLASS.equals(token.text().toString())) && ts.movePrevious()) {
            token = ts.token();
        }

        if (token.id() == JsTokenId.IDENTIFIER && CLASS.equals(token.text().toString()) && ts.movePrevious()) {
            // we are at ^Class.create(...)
            // now we need to find = and the identifier after =
            List<JsTokenId> skipTokens = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT);
            token = LexUtilities.findPrevious(ts, skipTokens);
            if (token.id() == JsTokenId.OPERATOR_ASSIGNMENT && ts.movePrevious()) {
                token = LexUtilities.findPrevious(ts, skipTokens);
                if (token.id() == JsTokenId.IDENTIFIER) {
                    name = token.text().toString();
                }
            }

        }
        return name;
    }
    
}
