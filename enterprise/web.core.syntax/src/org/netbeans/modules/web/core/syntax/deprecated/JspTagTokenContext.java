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

package org.netbeans.modules.web.core.syntax.deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.BaseTokenID;

/**
* Syntax class for JSP tags. It is not meant to be used by itself, but as one of syntaxes with
* MultiSyntax. Recognizes JSP tags, comments and directives. Does not recognize scriptlets,
* expressions and declarations, which should be rocognized by the master syntax, as expressions
* can appear embedded in a JSP tag. Moreover, they all share Java syntax.
*
* @author Petr Jiricka
* @deprecated Use JSP Lexer instead
*/
@Deprecated
public class JspTagTokenContext extends TokenContext {

    //there is not any token category for jsp tags
    //TODO - consider whether there is a need to create a category for jsp tags
    public static final TokenCategory tokenCategory = null;
    
    // Numeric-ids
    public static final int TEXT_ID = 1;
    public static final int ERROR_ID = 2;
    public static final int TAG_ID = 3;
    public static final int SYMBOL_ID = 4;
    public static final int SYMBOL2_ID = 5;
    public static final int COMMENT_ID = 6;
    public static final int ATTRIBUTE_ID = 7;
    public static final int ATTR_VALUE_ID = 8;
    public static final int EOL_ID = 9;
    public static final int AFTER_UNEXPECTED_LT_ID = 10;
    public static final int WHITESPACE_ID = 11;
    
    

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
    public static final JspTagTokenContext context = new JspTagTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    JspTagTokenContext() {
        super("jsptag-");   // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

    }

}

