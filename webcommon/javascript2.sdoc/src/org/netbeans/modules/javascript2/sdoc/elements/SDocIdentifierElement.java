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

/**
 * Represents sDoc elements with any identifier (like namespace, type etc.).
 * <p>
 * <i>Examples:</i> @alias foofighter, @namespace empty.space, ...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocIdentifierElement extends SDocBaseElement {

    private final String identifier;

    private SDocIdentifierElement(SDocElementType type, String identifier) {
        super(type);
        this.identifier = identifier;
    }

    /**
     * Creates new {@code SDocDescriptionElement}.
     *
     * @param type element type (tag), never {@code null}
     * @param identifier identifier of the element, never {@code null}
     */
    public static SDocIdentifierElement create(SDocElementType type, String identifier) {
        return new SDocIdentifierElement(type, identifier);
    }

    /**
     * Gets identifier of the element.
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

}
