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
package org.netbeans.modules.javascript2.extjs.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 9)
public class ExtApplyFunctionInterceptor implements FunctionInterceptor {

    @Override
    public Pattern getNamePattern() {
        return Pattern.compile("Ext\\.apply");
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String functionName, JsObject globalObject,
            DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (args.size() == 2) {
            Iterator<FunctionArgument> iterator = args.iterator();
            FunctionArgument arg1 = iterator.next();
            FunctionArgument arg2 = iterator.next();
            int offset = arg1.getOffset();
            if (arg1.getKind() == FunctionArgument.Kind.REFERENCE && arg2.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                JsObject parent = globalObject;
                JsObject oldParent = parent;

                if (arg1.getValue() instanceof Collection) {
                    for (String name : (Collection<String>) arg1.getValue()) {
                        JsObject jsObject = oldParent.getProperty(name);
                        OffsetRange offsetRange = new OffsetRange(offset, offset + name.length());
                        if (jsObject == null) {
                            jsObject = factory.newObject(parent, name, offsetRange, true);
                            parent.addProperty(name, jsObject);
                            oldParent = jsObject;
                        } else if (!jsObject.isDeclared()) {
                            JsObject newJsObject = factory.newObject(parent, name, offsetRange, true);
                            parent.addProperty(name, newJsObject);
                            for (Occurrence occurrence : jsObject.getOccurrences()) {
                                newJsObject.addOccurrence(occurrence.getOffsetRange());
                            }
                            newJsObject.addOccurrence(jsObject.getDeclarationName().getOffsetRange());
                            oldParent = jsObject;
                            jsObject = newJsObject;
                        }

                        parent = jsObject;
                        offset += name.length() + 1;
                    }
                }

                JsObject definedObject = (JsObject) arg2.getValue();
                if (definedObject.getModifiers().remove(org.netbeans.modules.csl.api.Modifier.PRIVATE)) {
                    definedObject.getModifiers().add(org.netbeans.modules.csl.api.Modifier.PUBLIC);
                }


                for (JsObject property : definedObject.getProperties().values()) {
                    if (property.isDeclared()) {
                        parent.addProperty(property.getName(), factory.newReference(parent, property.getName(), property.getDeclarationName().getOffsetRange(), property, true, property.getModifiers()));
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
