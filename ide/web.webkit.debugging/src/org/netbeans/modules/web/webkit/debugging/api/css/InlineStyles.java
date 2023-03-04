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
package org.netbeans.modules.web.webkit.debugging.api.css;

import org.json.simple.JSONObject;

/**
 * Inline styles of a DOM node.
 *
 * @author Jan Stola
 */
public class InlineStyles {
    /** Style defined by {@code style} attribute of the node. */
    private Style inlineStyle;
    /** Style defined by DOM attributes (other than {@code style}). */
    private Style attributesStyle;

    /**
     * Creates a new {@code InlineStyles} that corresponds to the given JSONObject.
     *
     * @param styles JSONObject describing the property.
     */
    InlineStyles(JSONObject styles) {
        if (styles.containsKey("inlineStyle")) { // NOI18N
            inlineStyle = new Style((JSONObject)styles.get("inlineStyle")); // NOI18N
        }
        if (styles.containsKey("attributesStyle")) { // NOI18N
            attributesStyle = new Style((JSONObject)styles.get("attributesStyle")); // NOI18N
        }
    }

    /**
     * Returns the style defined by {@code style} attribute of the node.
     *
     * @return style defined by {@code style} attribute of the node.
     */
    public Style getInlineStyle() {
        return inlineStyle;
    }

    /**
     * Returns the style defined by DOM attributes (other than {@code style}).
     *
     * @return style defined by DOM attributes (other than {@code style}).
     */
    public Style getAttributesStyle() {
        return attributesStyle;
    }

}
