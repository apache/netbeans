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
 * Property of a CSS style/rule.
 *
 * @author Jan Stola
 */
public class Property {
    /** Name of the property. */
    private final String name;
    /** Value of the property. */
    private final String value;
    /** Priority of the property. */
    private final String priority;
    /** Determines whether the property is implicit. */
    private boolean implicit;
    /** Full property text as specified in the style. */
    private final String text;
    /** Determines whether the property is understood by the browser. */
    private final boolean parsedOk;
    /** Status of the property. */
    private final Status status;
    /** Related shorthand property name (or {@code null} if the property is not a longhand). */
    private final String shorthandName;
    /** Entire property range in the enclosing style declaration (if available). */
    private SourceRange range;

    /**
     * Creates a new {@code Property} that corresponds to the given JSONObject.
     *
     * @param property JSONObject describing the property.
     */
    Property(JSONObject property) {
        name = (String)property.get("name"); // NOI18N
        value = (String)property.get("value"); // NOI18N
        priority = (String)property.get("priority"); // NOI18N
        if (property.containsKey("implicit")) { // NOI18N
            implicit = (Boolean)property.get("implicit"); // NOI18N
        }
        text = (String)property.get("text"); // NOI18N
        if (property.containsKey("parsedOk")) { // NOI18N
            parsedOk = (Boolean)property.get("parsedOk"); // NOI18N
        } else {
            parsedOk = true;
        }
        String statusCode = (String)property.get("status"); // NOI18N
        status = Status.forCode(statusCode);
        shorthandName = (String)property.get("shorthandName"); // NOI18N
        if (property.containsKey("range")) { // NOI18N
            range = new SourceRange((JSONObject)property.get("range")); // NOI18N
        }
    }

    /**
     * Returns the name of the property.
     *
     * @return name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the property.
     *
     * @return value of the property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the priority of the property.
     *
     * @return priority of the property.
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Determines whether the property is implicit.
     *
     * @return {@code true} when the property is implict,
     * returns {@code false} otherwise.
     */
    public boolean isImplicit() {
        return implicit;
    }

    /**
     * Returns the full property text as specified in the style.
     *
     * @return full property text as specified in the style.
     */
    public String getText() {
        return text;
    }

    /**
     * Determines whether the property is understood by the browser.
     *
     * @return {@code true} when the property is understood by the browser,
     * returns {@code false} otherwise.
     */
    public boolean isParsedOk() {
        return parsedOk;
    }

    /**
     * Returns the status of the property.
     *
     * @return status of the property.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the related shorthand property name.
     * 
     * @return related shorthand property name (or {@code null} if the property is not a longhand).
     */
    public String getShorthandName() {
        return shorthandName;
    }

    /**
     * Entire property range in the enclosing style declaration.
     *
     * @return entire property range in the enclosing style declaration
     * or {@code null} when this information is not available.
     */
    public SourceRange getRange() {
        return range;
    }

    /**
     * Status of a property.
     */
    public static enum Status {
        /** Property is effective in the style. */
        ACTIVE,
        /** Property is overriden by a same-named property in the style later on. */
        INACTIVE,
        /** Property is disabled by the user. */
        DISABLED,
        /** Property is reported by the browser rather than by the CSS source parser. */
        STYLE;

        /**
         * Returns the property status for the given code.
         *
         * @param code code of the property status.
         * @return property status matching the given code or {@code null}
         * for an unknown code.
         */
        static Status forCode(String code) {
            Status status = ACTIVE;
            if ("active".equals(code)) { // NOI18N
                status = ACTIVE;
            } else if ("inactive".equals(code)) { // NOI18N
                status = INACTIVE;
            } else if ("disabled".equals(code)) { // NOI18N
                status = DISABLED;
            } else if ("style".equals(code)) { // NOI18N
                status = STYLE;
            }
            return status;
        }
    }

}
