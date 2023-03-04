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
package org.netbeans.modules.javascript2.sdoc.elements;

import java.util.List;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Represents named parameter element.
 * <p>
 * <i>Examples:</i> @param {MyType} [myName] myDescription,...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocTypeNamedElement extends SDocTypeDescribedElement {

    private final Identifier typeName;
    private final boolean optional;

    private SDocTypeNamedElement(SDocElementType type, List<Type> declaredTypes, String description,
            Identifier typeName, boolean optional) {
        super(type, declaredTypes, description);
        this.typeName = typeName;
        this.optional = optional;
    }

    /**
     * Creates named parameter element.
     * <p>
     * This creates mandatory parameter with no default value.
     * @param type type of the element
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     * @param paramName name of the parameter
     */
    public static SDocTypeNamedElement create(SDocElementType type, List<Type> declaredTypes, String description, Identifier typeName) {
        return new SDocTypeNamedElement(type, declaredTypes, description, typeName, false);
    }

    /**
     * Creates named parameter element.
     * <p>
     * This creates optional parameter with no default value.
     * @param type type of the element
     * @param paramTypes type of the parameter
     * @param paramDescription description of the parameter
     * @param paramName name of the parameter
     * @param optional flag if the parameter is optional
     */
    public static SDocTypeNamedElement create(SDocElementType type, List<Type> declaredTypes, String description, Identifier typeName, boolean optional) {
        return new SDocTypeNamedElement(type, declaredTypes, description, typeName, optional);
    }

    public Identifier getTypeName() {
        return typeName;
    }

    @Override
    public Identifier getParamName() {
        return typeName;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

}
