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
 * Represents sDoc elements with any description.
 * <p>
 * <i>Examples:</i> @author Jackie Chan, @see ChuckNorrisClass#killThemAll(), ...
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocDescriptionElement extends SDocBaseElement {

    private final String description;

    private SDocDescriptionElement(SDocElementType type, String description) {
        super(type);
        this.description = description;
    }

    /**
     * Creates new {@code SDocDescriptionElement}.
     *
     * @param type element type (tag), never {@code null}
     * @param description description of the element, never {@code null}
     */
    public static SDocDescriptionElement create(SDocElementType type, String description) {
        return new SDocDescriptionElement(type, description);
    }

    /**
     * Gets description of the element.
     * @return description
     */
    public String getDescription() {
        return description;
    }

}
