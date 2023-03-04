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
 * Contains ExtDoc element types.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public enum ExtDocElementType {

    // description type - at the start of comments
    DESCRIPTION("description", Category.DESCRIPTION), //NOI18N
    // unknown type
    UNKNOWN("unknown", Category.UNKNOWN), //NOI18N
    
    // common ExtDoc tags
    CFG("@cfg", Category.TYPE_NAMED), //NOI18N
    CLASS("@class", Category.IDENT_DESCRIBED), //NOI18N
    CONSTANT("@constant", Category.SIMPLE), //NOI18N
    CONSTRUCTOR("@constructor", Category.SIMPLE), //NOI18N
    EVENT("@event", Category.IDENT_DESCRIBED), //NOI18N
    EXTENDS("@extends", Category.IDENT_SIMPLE), //NOI18N
    HIDE("@hide", Category.SIMPLE), //NOI18N
    IGNORE("@ignore", Category.SIMPLE), //NOI18N
    LINK("@link", Category.IDENT_DESCRIBED), //NOI18N
    MEMBER("@member", Category.IDENT_SIMPLE), //NOI18N
    METHOD("@method", Category.SIMPLE), //NOI18N
    NAMESPACE("@namespace", Category.IDENT_SIMPLE), //NOI18N
    PARAM("@param", Category.TYPE_NAMED), //NOI18N
    PRIVATE("@private", Category.SIMPLE), //NOI18N
    PROPERTY("@property", Category.SIMPLE), //NOI18N
    RETURN("@return", Category.TYPE_DESCRIBED), //NOI18N
    SINGLETON("@singleton", Category.SIMPLE), //NOI18N
    STATIC("@static", Category.SIMPLE), //NOI18N
    TYPE("@type", Category.TYPE_SIMPLE); //NOI18N

    private final String value;
    private final Category category;

    private ExtDocElementType(String textValue, Category category) {
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
    public static ExtDocElementType fromString(String value) {
        if (value != null) {
            for (ExtDocElementType type : ExtDocElementType.values()) {
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
        DESCRIPTION, // description at the beggining of the comment
        IDENT_SIMPLE, // @extends MyClass
        IDENT_DESCRIBED, // @class MyClass this is Chuck Noris's class
        SIMPLE, // @private
        UNKNOWN, // @unknownTag anything here

        TYPE_SIMPLE, // @type {type}
        TYPE_NAMED, // @return {type} name
        TYPE_DESCRIBED, // @param {type} name description
    }

}
