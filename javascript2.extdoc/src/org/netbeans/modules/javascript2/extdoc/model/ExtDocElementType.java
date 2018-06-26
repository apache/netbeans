/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    // unknow type
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
