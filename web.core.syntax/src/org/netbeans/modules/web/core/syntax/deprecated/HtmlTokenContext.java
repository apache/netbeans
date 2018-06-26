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

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
* HTML token-context defines token-ids and token-categories
* used in HTML language.
*
* @author Miloslav Metelka
* @version 1.00
*
* @deprecated Use Lexer API instead. See {@link HTMLTokenId}.
*/

public class HtmlTokenContext extends TokenContext {

    // Numeric-ids for token-ids
    public static final int TEXT_ID = 1;
    public static final int WS_ID = TEXT_ID + 1;
    public static final int ERROR_ID = WS_ID + 1;
    public static final int TAG_OPEN_ID = ERROR_ID + 1;
    public static final int TAG_CLOSE_ID = TAG_OPEN_ID + 1; 
    public static final int ARGUMENT_ID = TAG_CLOSE_ID + 1;
    public static final int OPERATOR_ID = ARGUMENT_ID + 1;
    public static final int VALUE_ID = OPERATOR_ID + 1;
    public static final int BLOCK_COMMENT_ID = VALUE_ID + 1;
    public static final int SGML_COMMENT_ID = BLOCK_COMMENT_ID + 1;
    public static final int DECLARATION_ID = SGML_COMMENT_ID + 1;
    public static final int CHARACTER_ID = DECLARATION_ID + 1;
    public static final int EOL_ID = CHARACTER_ID + 1;
    public static final int TAG_OPEN_SYMBOL_ID = EOL_ID + 1; // '<' or '</';
    public static final int TAG_CLOSE_SYMBOL_ID = TAG_OPEN_SYMBOL_ID + 1; // '>' or '/>';

    //token category id for tag tokens
    public static final int TAG_CATEGORY_ID = TAG_CLOSE_SYMBOL_ID + 1;

    /** Token category for all tag tokens. */
    public static final BaseTokenCategory TAG_CATEGORY
        = new BaseTokenCategory("tag", TAG_CATEGORY_ID); // NOI18N

    
    // Token-ids
    /** Plain text */
    public static final BaseTokenID TEXT = new BaseTokenID( "text", TEXT_ID ); // NOI18N
    /** Erroneous Text */
    public static final BaseTokenID WS = new BaseTokenID( "ws", WS_ID ); // NOI18N
    /** Plain Text*/
    public static final BaseTokenID ERROR = new BaseTokenID( "error", ERROR_ID ); // NOI18N
    /** Html Open Tag */
    public static final BaseTokenID TAG_OPEN = new BaseTokenID( "open-tag", TAG_OPEN_ID, TAG_CATEGORY ); // NOI18N
    /** Html Close Tag */
    public static final BaseTokenID TAG_CLOSE = new BaseTokenID( "close-tag", TAG_CLOSE_ID, TAG_CATEGORY ); // NOI18N
    /** Argument of a tag */
    public static final BaseTokenID ARGUMENT = new BaseTokenID( "argument", ARGUMENT_ID ); // NOI18N
    /** Operators - '=' between arg and value */
    public static final BaseTokenID OPERATOR = new BaseTokenID( "operator", OPERATOR_ID ); // NOI18N
    /** Value - value of an argument */
    public static final BaseTokenID VALUE = new BaseTokenID( "value", VALUE_ID ); // NOI18N
    /** Block comment */
    public static final BaseTokenID BLOCK_COMMENT = new BaseTokenID( "block-comment", BLOCK_COMMENT_ID ); // NOI18N
    /** SGML comment - e.g. in DOCTYPE */
    public static final BaseTokenID SGML_COMMENT = new BaseTokenID( "sgml-comment", SGML_COMMENT_ID ); // NOI18N
    /** SGML declaration in HTML document - e.g. <!DOCTYPE> */
    public static final BaseTokenID DECLARATION = new BaseTokenID( "sgml-declaration", DECLARATION_ID ); // NOI18N
    /** Character reference, e.g. &amp;lt; = &lt; */
    public static final BaseTokenID CHARACTER = new BaseTokenID( "character", CHARACTER_ID ); // NOI18N
    /** End of line */
    public static final BaseTokenID EOL = new BaseTokenID( "EOL", EOL_ID ); // NOI18N
    /** Html Tag open symbol: '<' or '</' */
    public static final BaseTokenID TAG_OPEN_SYMBOL = new BaseTokenID("tag-open-symbol", TAG_OPEN_SYMBOL_ID, TAG_CATEGORY); //NOI18N
    /** Html Tag close symbol: '>' or '/>' */
    public static final BaseTokenID TAG_CLOSE_SYMBOL = new BaseTokenID("tag-close-symbol", TAG_CLOSE_SYMBOL_ID, TAG_CATEGORY); //NOI18N

    // Context instance declaration
    public static final HtmlTokenContext context = new HtmlTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    private HtmlTokenContext() {
        super("html-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }

}
