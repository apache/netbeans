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
 * Identifier of a CSS rule.
 *
 * @author Jan Stola
 */
public final class RuleId {
    /** Identifier of the enclosing styleheet. */
    private final String styleSheetId;
    /** Rule ordinal within the stylesheet. */
    private final int ordinal;

    /**
     * Creates a new {@code RuleId} that corresponds to the given JSONObject.
     *
     * @param id JSONObject describing the rule ID.
     */
    RuleId(JSONObject id) {
        styleSheetId = (String)id.get("styleSheetId"); // NOI18N
        ordinal = ((Number)id.get("ordinal")).intValue(); // NOI18N
    }

    /**
     * Creates a new {@code RuleId} for the given style-sheet ID.
     * 
     * @param styleSheetId style-sheet ID.
     */
    RuleId(String styleSheetId) {
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
     * Returns the rule ordinal within the stylesheet.
     *
     * @return rule ordinal within the stylesheet.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Returns a {@code JSONObject} that corresponds to this {@code RuleId}.
     *
     * @return {@code JSONObject} that corresponds to this {@code RuleId}.
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
        if (!(object instanceof RuleId)) {
            return false;
        }
        RuleId other = (RuleId)object;
        return styleSheetId.equals(other.styleSheetId) && (ordinal == other.ordinal);
    }

}
