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
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Hejl
 */
public class ExtNamespaceFunctionInterceptor implements FunctionInterceptor {

    public ExtNamespaceFunctionInterceptor() {
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String functionName, JsObject globalObject, DeclarationScope scope,
            ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (args.size() == 1) {
            Iterator<FunctionArgument> iterator = args.iterator();
            FunctionArgument arg1 = iterator.next();
            int offset = arg1.getOffset();
            if (arg1.getKind() == FunctionArgument.Kind.STRING) {
                JsObject parent = globalObject;
                JsObject oldParent = parent;
                for (StringTokenizer st = new StringTokenizer((String) arg1.getValue(), "."); st.hasMoreTokens();) {
                    String name = st.nextToken();
                    JsObject jsObject = oldParent.getProperty(name);
                    if (jsObject == null) {
                        OffsetRange offsetRange = new OffsetRange(offset, offset + name.length());
                        jsObject = factory.newObject(parent, name, offsetRange, true);
                        parent.addProperty(name, jsObject);
                        oldParent = jsObject;
                    }
                    else if (!jsObject.isDeclared()) {
                        OffsetRange offsetRange = new OffsetRange(offset, offset + name.length());
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
        }
        return Collections.emptyList();
    }

    @Override
    public Pattern getNamePattern() {
        return Pattern.compile("Ext\\.namespace");
    }

}
