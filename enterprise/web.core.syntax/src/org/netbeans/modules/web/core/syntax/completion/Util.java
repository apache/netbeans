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

package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class Util {
    public static int findPositionForJspDirective(BaseDocument doc){
        int insertPos = 0;
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<JspTokenId> ts = th.tokenSequence(JspTokenId.language());

        ts.moveStart();

        while (ts.moveNext() && (ts.token().id() == JspTokenId.COMMENT || ts.token().id() == JspTokenId.EOL)){
            insertPos = ts.offset() + ts.token().length();
        }

        TokenSequence<HTMLTokenId> tsHTML = ts.embedded(HTMLTokenId.language());

        if (tsHTML != null){
            tsHTML.moveStart();

            while (tsHTML.moveNext() && (tsHTML.token().id() == HTMLTokenId.WS || tsHTML.token().id() == HTMLTokenId.EOL
                    || tsHTML.token().id() == HTMLTokenId.TEXT && isWsToken(tsHTML.token().text()))) {
                insertPos = tsHTML.offset() + tsHTML.token().length();
            }
        }

        return insertPos;
    }

    private static boolean isWsToken(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))){
                return false;
            }
        }

        return true;
    }
}
