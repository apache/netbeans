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
package org.netbeans.modules.javascript2.extdoc.model;

import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;

/**
 * Represents extDoc elements with type declaration purpose.
 * <p>
 * <i>Examples:</i> @type {String}
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocTypeSimpleElement extends ExtDocBaseElement implements DocParameter {

    protected final List<Type> declaredTypes;

    protected ExtDocTypeSimpleElement(ExtDocElementType type, List<Type> declaredTypes) {
        super(type);
        this.declaredTypes = declaredTypes;
    }

    /**
     * Creates new {@code ExtDocTypeSimpleElement}.
     */
    public static ExtDocTypeSimpleElement create(ExtDocElementType type, List<Type> declaredTypes) {
        return new ExtDocTypeSimpleElement(type, declaredTypes);
    }

    /**
     * Gets the type declared by this element.
     * @return declared type
     */
    public List<Type> getDeclaredTypes() {
        return declaredTypes;
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
        return "";
    }

    @Override
    public List<Type> getParamTypes() {
        return declaredTypes;
    }
}
