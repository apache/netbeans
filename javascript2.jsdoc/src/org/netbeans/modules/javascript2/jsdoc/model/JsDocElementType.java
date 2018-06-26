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
    // unknow type
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
    public synchronized static JsDocElementType fromString(String value) {
        if (types == null) {
           types = new HashMap<String, JsDocElementType>();
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
