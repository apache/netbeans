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
package org.netbeans.modules.php.blade.editor.lexer;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 *
 * @author bogdan
 */
public final class PHPLexerUtils {

    private PHPLexerUtils() {

    }

    public static TokenSequence<PHPTokenId> getTokenSequence(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument) doc;
        TokenSequence<PHPTokenId> tokenSequence = null;
        baseDoc.readLock();
        try {
            TokenHierarchy<Document> hierarchy = TokenHierarchy.get(baseDoc);
            tokenSequence = hierarchy.tokenSequence(PHPTokenId.language());
        } finally {
            baseDoc.readUnlock();
        }
        if (tokenSequence != null) {
            tokenSequence.move(offset);
            tokenSequence.moveNext();
        }
        return tokenSequence;

    }

}
