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

package org.netbeans.modules.javascript2.lexer;

import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;



/**
 *
 * @author Petr Hejl
 */
public class JsLexer implements Lexer<JsTokenId> {

    private static final Logger LOGGER = Logger.getLogger(JsLexer.class.getName());

    private final JsColoringLexer scanner;

    private TokenFactory<JsTokenId> tokenFactory;

    private JsLexer(LexerRestartInfo<JsTokenId> info) {
        scanner = new JsColoringLexer(info);
        tokenFactory = info.tokenFactory();
    }

    public static JsLexer create(LexerRestartInfo<JsTokenId> info) {
        synchronized (JsLexer.class) {
            return new JsLexer(info);
        }
    }

    @Override
    public Token<JsTokenId> nextToken() {
        try {
            JsTokenId tokenId = scanner.nextToken();
            LOGGER.log(Level.FINEST, "Lexed token is {0}", tokenId);
            Token<JsTokenId> token = null;
            if (tokenId != null) {
                token = tokenFactory.createToken(tokenId);
            }
            return token;
        } catch (IOException ex) {
            Logger.getLogger(JsLexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }
}
