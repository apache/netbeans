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
package org.netbeans.modules.javascript2.jsdoc.model;

import org.netbeans.modules.javascript2.types.api.Type;


/**
 * Represents jsDoc elements with declaration purpose.
 * <p>
 * <i>Examples:</i> @extends otherClass, @type typeClass, ...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class DeclarationElement extends JsDocElementImpl {

    private final Type declaredType;

    private DeclarationElement(JsDocElementType type, Type declaredType) {
        super(type);
        this.declaredType = declaredType;
    }

    /**
     * Creates new {@code DeclarationElement}.
     */
    public static DeclarationElement create(JsDocElementType type, Type declaredType) {
        return new DeclarationElement(type, declaredType);
    }

    /**
     * Gets the type declared by this element.
     * @return declared type
     */
    public Type getDeclaredType() {
        return declaredType;
    }
}
