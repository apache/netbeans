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
package org.netbeans.modules.javascript2.jquery.model;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.model.spi.FunctionArgument;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 */
@FunctionInterceptor.Registration(priority = 51)
public class JQueryExtendInterceptor implements FunctionInterceptor {

    private static final Pattern PATTERN = Pattern.compile("(\\$|jQuery)\\.extend");  //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject,
            DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        if (args.size() == 1) {
            FunctionArgument arg = args.iterator().next();
            if (arg.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                JsObject possiblePlugin = (JsObject)arg.getValue();
                if (possiblePlugin.getProperties().size() == 1) {
                    JsObject parent = globalObject;
                    JsObject property = possiblePlugin.getProperties().values().iterator().next();
                    JsObject newObject = factory.newReference(parent, property.getName(), property.getDeclarationName().getOffsetRange(), property, true, null);
                    parent.addProperty(property.getName(), newObject);
                }
            }
        }
        return Collections.emptyList();
    }
}
