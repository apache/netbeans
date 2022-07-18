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

    protected static final Map<String, CompletionDescription> KEYWORDS = new HashMap<>();
    protected static final Map<String, CompletionDescription> SPECIAL_KEYWORDS_IMPORTEXPORT = new HashMap<>();

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
