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
 * Identifier of a CSS style.
 *
 * @author Jan Stola
 */
public final class StyleId {
    /** Identifier of the enclosing styleheet. */
    private final String styleSheetId;
    /** Style ordinal within the stylesheet. */
    private final int ordinal;

    /**
     * Creates a new {@code StyleId} that corresponds to the given JSONObject.
     *
     * @param id JSONObject describing the style ID.
     */
    StyleId(JSONObject id) {
        styleSheetId = (String)id.get("styleSheetId"); // NOI18N
        ordinal = ((Number)id.get("ordinal")).intValue(); // NOI18N
    }

    /**
     * Creates a new {@code StyleId} for the given style-sheet ID.
     * 
     * @param styleSheetId style-sheet ID.
     */
    StyleId(String styleSheetId) {
        this.styleSheetId = styleSheetId;
        this.ordinal = -1;
    }

    /**
     * Returns the identifier of the enclosing stylesheet.
     *
     * @return identifier of the enclosing stylesheet.
     */
    public String getStyleSheetId() {
        return styleSheetId;
    }

    /**
     * Returns the style ordinal within the stylesheet.
     *
     * @return style ordinal within the stylesheet.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Returns a {@code JSONObject} that corresponds to this {@code StyleId}.
     *
     * @return {@code JSONObject} that corresponds to this {@code StyleId}.
     */
    JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("styleSheetId", getStyleSheetId()); // NOI18N
        json.put("ordinal", getOrdinal()); // NOI18N
        return json;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89*hash + styleSheetId.hashCode();
        hash = 89*hash + ordinal;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StyleId)) {
            return false;
        }
        StyleId other = (StyleId)object;
        return styleSheetId.equals(other.styleSheetId) && (ordinal == other.ordinal);
    }

}
