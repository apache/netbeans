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
import org.netbeans.modules.csl.api.OffsetRange;
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
@FunctionInterceptor.Registration(priority = 50)
public class JQueryWidgetInterceptor implements FunctionInterceptor {

    private static final Pattern PATTERN = Pattern.compile("(\\$|jQuery)\\.widget");  //NOI18N

    @Override
    public Pattern getNamePattern() {
        return PATTERN;
    }

    @Override
    public Collection<TypeUsage> intercept(Snapshot snapshot, String name, JsObject globalObject, DeclarationScope scope, ModelElementFactory factory, Collection<FunctionArgument> args) {
        String widgetName = null;
        int widgetNameOffset = -1;
        JsObject widget = null;
        for (FunctionArgument arg : args) {
             if (arg.getKind() == FunctionArgument.Kind.STRING) {
                 widgetName = (String)arg.getValue();
                 widgetNameOffset = arg.getOffset();
             } else if (arg.getKind() == FunctionArgument.Kind.ANONYMOUS_OBJECT) {
                 widget = (JsObject)arg.getValue();
             }
        }
        if (widgetName != null && widget != null) {
            String[] parts = widgetName.split("\\.");   //NOI18N
            JsObject parent = globalObject;
            JsObject newObject;
            for (int i = 0; i < parts.length - 1; i++) {
                newObject = factory.newObject(parent, parts[i], new OffsetRange(widgetNameOffset, widgetNameOffset + parts[i].length()), true);
                widgetNameOffset = widgetNameOffset + parts[i].length() + 1;
                parent.addProperty(parts[i], newObject);
                parent = newObject;
            }
            newObject = factory.newReference(parent, parts[parts.length - 1], new OffsetRange(widgetNameOffset, widgetNameOffset + parts[parts.length - 1].length()), widget, true, null);
            parent.addProperty(parts[parts.length - 1], newObject);
        }
        return Collections.emptyList();
    }

}
