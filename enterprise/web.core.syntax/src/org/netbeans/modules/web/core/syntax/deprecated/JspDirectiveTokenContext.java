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

package org.netbeans.modules.web.core.syntax.deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.BaseTokenID;

/**
* Syntax Token Context class for JSP directives. Tokens from this token context
* are used for jsp directives. The only difference between them is that directive
* tokens has a special token context category.
*
* @author Marek Fukala
* @deprecated Use Jsp Lexer instead.
*/
@Deprecated
public class JspDirectiveTokenContext extends JspTagTokenContext {

    //token ids - are inherited from superclass
    
    //token category ids
    public static final int TAG_CATEGORY_ID = WHITESPACE_ID + 1;

    //token category for jsp directives
    public static final TokenCategory tokenCategory = new BaseTokenCategory("directive", TAG_CATEGORY_ID); // NOI18N

    // TokenIDs
    public static final BaseTokenID TEXT = new BaseTokenID("text", TEXT_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID ERROR = new BaseTokenID("error", ERROR_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID TAG = new BaseTokenID("tag-directive", TAG_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID SYMBOL = new BaseTokenID("symbol", SYMBOL_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID SYMBOL2 = new BaseTokenID("scriptlet-delimiter", SYMBOL2_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID COMMENT = new BaseTokenID("comment", COMMENT_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID ATTRIBUTE = new BaseTokenID("attribute-name", ATTRIBUTE_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID ATTR_VALUE = new BaseTokenID("attribute-value", ATTR_VALUE_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID EOL = new BaseTokenID("EOL", EOL_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID AFTER_UNEXPECTED_LT = new BaseTokenID("AFTER_UNEXPECTED_LT", AFTER_UNEXPECTED_LT_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID WHITESPACE = new BaseTokenID("whitespace", WHITESPACE_ID, tokenCategory);   // NOI18N
    
    // Context instance declaration
    public static final JspDirectiveTokenContext context = new JspDirectiveTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    JspDirectiveTokenContext() {
        super();   // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

    }
    
}

