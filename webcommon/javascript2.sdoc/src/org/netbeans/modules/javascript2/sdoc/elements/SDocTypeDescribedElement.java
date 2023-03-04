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
 * Represents parameter element which does not need any parameter name.
 * <p>
 * <i>Examples:</i> @return {String} whole string, ...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocTypeDescribedElement extends SDocTypeSimpleElement {

    protected final String typeDescription;

    protected SDocTypeDescribedElement(SDocElementType type, List<Type>declaredTypes, String description) {
        super(type, declaredTypes);
        this.typeDescription = description;
    }

    /** Creates type described element.
     * @param type type of the element
     * @param paramTypes type of the parameter
     * @param description description of the parameter
     */
    public static SDocTypeDescribedElement create(SDocElementType type, List<Type> declaredTypes, String description) {
        return new SDocTypeDescribedElement(type, declaredTypes, description);
    }

    /**
     * Gets the description of the parameter.
     * @return parameter description
     */
    public String getTypeDescription() {
        return typeDescription;
    }

    @Override
    public Identifier getParamName() {
        return null;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public String getParamDescription() {
        return typeDescription;
    }

    @Override
    public List<Type> getParamTypes() {
        return declaredTypes;
    }

}
