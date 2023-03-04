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

/**
 * Represents jsDoc elements with description text.
 * <p>
 * <i>Examples:</i> @author Jackie Chan, @fileOverview about this file, ...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class DescriptionElement extends JsDocElementImpl {

    private final String description;

    private DescriptionElement(JsDocElementType type, String description) {
        super(type);
        this.description = description;
    }

    /**
     * Creates new {@code DescriptionElement}.
     *
     * @param type element type (tag), never null
     * @param description description of the element, never null
     */
    public static DescriptionElement create(JsDocElementType type, String description) {
        return new DescriptionElement(type, description);
    }

    /**
     * Gets description of the element.
     * @return description
     */
    public String getDescription() {
        return description;
    }

}
