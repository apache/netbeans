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
package org.netbeans.modules.javascript2.editor;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Pisl
 *
 */

public class JsKeywords {

    public static enum CompletionType {
        SIMPLE,
        CURSOR_INSIDE_BRACKETS,
        ENDS_WITH_CURLY_BRACKETS,
        ENDS_WITH_SPACE,
        ENDS_WITH_SEMICOLON,
        ENDS_WITH_COLON,
        ENDS_WITH_DOT
    };

    protected final static Map<String, CompletionDescription> KEYWORDS = new HashMap<>();
    protected final static Map<String, CompletionDescription> SPECIAL_KEYWORDS_IMPORTEXPORT = new HashMap<>();
    
    static {
        KEYWORDS.put(JsTokenId.KEYWORD_BREAK.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SEMICOLON));
        KEYWORDS.put(JsTokenId.KEYWORD_CASE.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_COLON));
        KEYWORDS.put(JsTokenId.KEYWORD_CATCH.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_CONTINUE.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SEMICOLON));
        KEYWORDS.put(JsTokenId.KEYWORD_DEBUGGER.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SEMICOLON));
        KEYWORDS.put(JsTokenId.KEYWORD_DEFAULT.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_COLON));
        KEYWORDS.put(JsTokenId.KEYWORD_DELETE.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_DO.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_ELSE.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_EXPORT.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_FALSE.fixedText(), new CompletionDescription(CompletionType.SIMPLE));
        KEYWORDS.put(JsTokenId.KEYWORD_FINALLY.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_CURLY_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_FOR.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_FUNCTION.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_IF.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_IMPORT.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_IN.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_INSTANCEOF.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_NEW.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_NULL.fixedText(), new CompletionDescription(CompletionType.SIMPLE));
        KEYWORDS.put(JsTokenId.KEYWORD_RETURN.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_SWITCH.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_THIS.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_DOT));
        KEYWORDS.put(JsTokenId.KEYWORD_THROW.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_TRUE.fixedText(), new CompletionDescription(CompletionType.SIMPLE));
        KEYWORDS.put(JsTokenId.KEYWORD_TRY.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_CURLY_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_TYPEOF.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_VAR.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_VOID.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));
        KEYWORDS.put(JsTokenId.KEYWORD_WHILE.fixedText(), new CompletionDescription(CompletionType.CURSOR_INSIDE_BRACKETS));
        KEYWORDS.put(JsTokenId.KEYWORD_WITH.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE));

        // keywords added with ESCMA Script 6
        KEYWORDS.put(JsTokenId.RESERVED_LET.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_CLASS.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_CONST.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_EXTENDS.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_EXPORT.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_IMPORT.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_SUPER.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        KEYWORDS.put(JsTokenId.KEYWORD_YIELD.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));

        // keywords added with ESCMA Script 7
        KEYWORDS.put(JsTokenId.RESERVED_AWAIT.fixedText(), new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA7));
        KEYWORDS.put("async", new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA7)); // NOI18N
    }
   
    static {
        SPECIAL_KEYWORDS_IMPORTEXPORT.put("as", new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
        SPECIAL_KEYWORDS_IMPORTEXPORT.put("from", new CompletionDescription(CompletionType.ENDS_WITH_SPACE, JsVersion.ECMA6));
    }    

    public static class CompletionDescription {

        private final CompletionType type;

        private final JsVersion version;

        private CompletionDescription(CompletionType type) {
            this(type, null);
        }

        private CompletionDescription(CompletionType type, JsVersion version) {
            this.type = type;
            this.version = version;
        }

        public CompletionType getType() {
            return type;
        }

        public JsVersion getVersion() {
            return version;
        }
    }
}
