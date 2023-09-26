/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.jsdoc.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents jsDoc element type.
 */
public enum JsDocElementType {
    // special context sensitive type
    CONTEXT_SENSITIVE("contextSensitive", JsDocElement.Category.DESCRIPTION), //NOI18N
    // unknown type
    UNKNOWN("unknown", JsDocElement.Category.UNKNOWN), //NOI18N
    // common jsDoc tags
    ARGUMENT("@argument", JsDocElement.Category.NAMED_PARAMETER), //NOI18N
    AUGMENTS("@augments", JsDocElement.Category.DECLARATION), //NOI18N
    AUTHOR("@author", JsDocElement.Category.DESCRIPTION), //NOI18N
    BORROWS("@borrows", JsDocElement.Category.ASSIGN), //NOI18N
    CALLBACK("@callback", JsDocElement.Category.DECLARATION),
    CLASS("@class", JsDocElement.Category.DESCRIPTION), //NOI18N
    CONSTANT("@constant", JsDocElement.Category.SIMPLE), //NOI18N
    CONSTRUCTOR("@constructor", JsDocElement.Category.SIMPLE), //NOI18N
    CONSTRUCTS("@constructs", JsDocElement.Category.SIMPLE), //NOI18N
    DEFAULT("@default", JsDocElement.Category.DESCRIPTION), //NOI18N
    DEPRECATED("@deprecated", JsDocElement.Category.DESCRIPTION), //NOI18N
    DESCRIPTION("@description", JsDocElement.Category.DESCRIPTION), //NOI18N
    EVENT("@event", JsDocElement.Category.SIMPLE), //NOI18N
    EXAMPLE("@example", JsDocElement.Category.DESCRIPTION), //NOI18N
    EXTENDS("@extends", JsDocElement.Category.DECLARATION), //NOI18N
    FIELD("@field", JsDocElement.Category.SIMPLE), //NOI18N
    FILE_OVERVIEW("@fileOverview", JsDocElement.Category.DESCRIPTION), //NOI18N
    FUNCTION("@function", JsDocElement.Category.SIMPLE), //NOI18N
    IGNORE("@ignore", JsDocElement.Category.SIMPLE), //NOI18N
    INNER("@inner", JsDocElement.Category.SIMPLE), //NOI18N
    LENDS("@lends", JsDocElement.Category.LINK), //NOI18N
    LINK("@link", JsDocElement.Category.DESCRIPTION), //NOI18N
    MEMBER_OF("@memberOf", JsDocElement.Category.LINK), //NOI18N
    NAME("@name", JsDocElement.Category.LINK), //NOI18N
    NAMESPACE("@namespace", JsDocElement.Category.DESCRIPTION), //NOI18N
    PARAM("@param", JsDocElement.Category.NAMED_PARAMETER), //NOI18N
    PRIVATE("@private", JsDocElement.Category.SIMPLE), //NOI18N
    PROPERTY("@property", JsDocElement.Category.NAMED_PARAMETER), //NOI18N
    PUBLIC("@public", JsDocElement.Category.SIMPLE), //NOI18N
    REQUIRES("@requires", JsDocElement.Category.DESCRIPTION), //NOI18N
    RETURN("@return", JsDocElement.Category.UNNAMED_PARAMETER), //NOI18N
    RETURNS("@returns", JsDocElement.Category.UNNAMED_PARAMETER), //NOI18N
    SEE("@see", JsDocElement.Category.DESCRIPTION), //NOI18N
    SINCE("@since", JsDocElement.Category.DESCRIPTION), //NOI18N
    STATIC("@static", JsDocElement.Category.SIMPLE), //NOI18N
    SYNTAX("@syntax", JsDocElement.Category.DESCRIPTION), //NOI18N
    THROWS("@throws", JsDocElement.Category.UNNAMED_PARAMETER), //NOI18N
    TYPE("@type", JsDocElement.Category.DECLARATION), //NOI18N
    TYPEDEF("@typedef", JsDocElement.Category.NAMED_PARAMETER), //NOI18N
    VERSION("@version", JsDocElement.Category.DESCRIPTION);
    //NOI18N
    private final String value;
    private final JsDocElement.Category category;

    private JsDocElementType(String textValue, JsDocElement.Category category) {
        this.value = textValue;
        this.category = category;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Gets the category of the jsDoc element.
     * @return category
     */
    public JsDocElement.Category getCategory() {
        return category;
    }

    private static Map<String, JsDocElementType> types = null;
    /**
     * Gets {@code Type} corresponding to given value.
     * @param value {@code String} value of the {@code Type}
     * @return {@code Type}
     */
    public static synchronized JsDocElementType fromString(String value) {
        if (types == null) {
           types = new HashMap<>();
           for (JsDocElementType type : JsDocElementType.values()) {
               types.put(type.toString().toLowerCase(Locale.ENGLISH), type);
           }
        }
        if (value != null) {
            JsDocElementType type = types.get(value.toLowerCase(Locale.ENGLISH));
            if (type != null) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
