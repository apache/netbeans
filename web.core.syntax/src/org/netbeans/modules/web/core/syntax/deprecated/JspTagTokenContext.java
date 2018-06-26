/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

