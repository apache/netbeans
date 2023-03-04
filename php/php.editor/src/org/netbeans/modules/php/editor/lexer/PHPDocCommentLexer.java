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
package org.netbeans.modules.php.editor.lexer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.util.NbPreferences;


/**
 * Lexical analyzer for PHP Documentor.
 *
 * @author Petr Pisl
 */

public final class PHPDocCommentLexer implements Lexer<PHPDocCommentTokenId> {

    private final TokenFactory<PHPDocCommentTokenId> tokenFactory;
    private final DocumentorColoringScanner scanner;

    public PHPDocCommentLexer(LexerRestartInfo<PHPDocCommentTokenId> info) {
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
        scanner = new DocumentorColoringScanner(info);

    }

    @Override
    public Object state() {
        return null;
    }

    public Preferences getDocscanPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/tasklist/docscan");
    }

    @Override
    public Token<PHPDocCommentTokenId> nextToken() {
         try {
            PHPDocCommentTokenId tokenId = scanner.nextToken();
            Token<PHPDocCommentTokenId> token = null;
            if (tokenId != null) {
                token = tokenFactory.createToken(tokenId);
            }
            return token;
        } catch (IOException ex) {
            Logger.getLogger(GSFPHPLexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void release() {
    }
}
