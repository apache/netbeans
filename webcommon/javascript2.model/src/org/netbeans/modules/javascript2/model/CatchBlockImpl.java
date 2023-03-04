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
package org.netbeans.modules.javascript2.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class CatchBlockImpl extends DeclarationScopeImpl implements JsFunction {

    private final List<JsObject> parameters;

    public CatchBlockImpl(DeclarationScope inFunction, Identifier exception, OffsetRange range, String mimeType) {
        super(inFunction, (JsObject)inFunction, new Identifier(getBlockName((JsObject)inFunction), OffsetRange.NONE),
                range, mimeType, null); //NOI18N
        this.parameters = new ArrayList<>();
        if(exception != null) {
            ParameterObject param = new ParameterObject(this, exception, mimeType, null);
            this.parameters.add(param);
            param.addOccurrence(exception.getOffsetRange());
        }
        ((JsObjectImpl)inFunction).addProperty(this.getName(), this);
    }

    private static String getBlockName(JsObject parent) {
        int index = 1;
        while (parent.getProperty("catch_" + index) != null) {
            index++;
        }
        return "catch_" + index;
    }

    @Override
    public Collection<? extends JsObject> getParameters() {
        return new ArrayList(this.parameters);
    }

    @Override
    public JsObject getParameter(String name) {
        for (JsObject param : parameters) {
            if(name.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }

    @Override
    public void addReturnType(TypeUsage type) {
    }

    @Override
    public Collection<? extends TypeUsage> getReturnTypes() {
        return Collections.emptyList();
    }

    @Override
    public Kind getJSKind() {
        return Kind.CATCH_BLOCK;
    }
}
