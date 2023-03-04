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
 * Contains ScriptDoc element types.
 */
public enum SDocElementType {
    // description type - at the start of comments
    DESCRIPTION("description", Category.DESCRIPTION), //NOI18N
    // unknown type
    UNKNOWN("unknown", Category.UNKNOWN), //NOI18N
    // common ScriptDoc tags
    ALIAS("@alias", Category.IDENT), //NOI18N
    AUTHOR("@author", Category.DESCRIPTION), //NOI18N
    CLASS_DESCRIPTION("@classDescription", Category.DESCRIPTION), //NOI18N
    CONSTANT("@constant", Category.SIMPLE), //NOI18N
    CONSTRUCTOR("@constructor", Category.SIMPLE), //NOI18N
    DEPRECATED("@deprecated", Category.SIMPLE), //NOI18N
    EXAMPLE("@example", Category.DESCRIPTION), //NOI18N
    EXCEPTION("@exception", Category.TYPE_DESCRIBED), //NOI18N
    ID("@id", Category.DESCRIPTION), //NOI18N
    INHERITS("@inherits", Category.IDENT), //NOI18N
    INTERNAL("@internal", Category.SIMPLE), //NOI18N
    MEMBER_OF("@memberOf", Category.IDENT), //NOI18N
    METHOD("@method", Category.SIMPLE), //NOI18N
    NAMESPACE("@namespace", Category.IDENT), //NOI18N
    PARAM("@param", Category.TYPE_NAMED), //NOI18N
    PRIVATE("@private", Category.SIMPLE), //NOI18N
    PROJECT_DESCRIPTION("@projectDescription", Category.DESCRIPTION), //NOI18N
    PROPERTY("@property", Category.TYPE_SIMPLE), //NOI18N
    RETURN("@return", Category.TYPE_DESCRIBED), //NOI18N
    SEE("@see", Category.DESCRIPTION), //NOI18N
    SINCE("@since", Category.DESCRIPTION), //NOI18N
    TYPE("@type", Category.TYPE_SIMPLE), //NOI18N
    VERSION("@version", Category.DESCRIPTION);
    //NOI18N
    private final String value;
    private final Category category;

    private SDocElementType(String textValue, Category category) {
        this.value = textValue;
        this.category = category;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Gets the category of the element.
     * @return category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets {@code Type} corresponding to given value.
     * @param value {@code String} value of the {@code Type}
     * @return {@code Type}
     */
    public static SDocElementType fromString(String value) {
        if (value != null) {
            for (SDocElementType type : SDocElementType.values()) {
                if (value.equalsIgnoreCase(type.toString())) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * Contains information about element kind.
     */
    public enum Category {
        DESCRIPTION, // @author description with spaces etc.
        IDENT, // @namespace ident
        SIMPLE, // @private
        UNKNOWN,

        TYPE_SIMPLE, // @type {type}
        TYPE_NAMED, // @return {type} name
        TYPE_DESCRIBED, // @param {type} name description
    }

}
