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

/**
 * Represents extDoc elements with any described identifier (like class, link etc.).
 * <p>
 * <i>Examples:</i> @class MyClass any description, @link Here link which links to Here, ...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocIdentDescribedElement extends ExtDocIdentSimpleElement {

    private final String description;

    private ExtDocIdentDescribedElement(ExtDocElementType type, String identifier, String description) {
        super(type, identifier);
        this.description = description;
    }

    /**
     * Creates new {@code ExtDocIdentDescribedElement}.
     *
     * @param type element type (tag), never {@code null}
     * @param identifier identifier of the element, never {@code null}
     * @param description description of the element, can be {@code null}
     */
    public static ExtDocIdentDescribedElement create(ExtDocElementType type, String identifier, String description) {
        return new ExtDocIdentDescribedElement(type, identifier, description);
    }

    /**
     * Gets description of the identifier element.
     * @return description
     */
    public String getDescription() {
        return description;
    }

}
