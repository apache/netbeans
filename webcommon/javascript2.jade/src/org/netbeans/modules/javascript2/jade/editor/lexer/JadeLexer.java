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
package org.netbeans.modules.javascript2.jade.editor.lexer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Petr Pisl
 */
public class JadeLexer implements Lexer<JadeTokenId> {

    private static final Logger LOGGER = Logger.getLogger(JadeLexer.class.getName());
    
    private final JadeColoringLexer scanner;
    private TokenFactory<JadeTokenId> tokenFactory;
    
    private JadeLexer(LexerRestartInfo<JadeTokenId> info) {
        scanner = new JadeColoringLexer(info);
        tokenFactory = info.tokenFactory();
    }
    
    public static JadeLexer create(LexerRestartInfo<JadeTokenId> info) {
        synchronized (JadeLexer.class) {
            return new JadeLexer(info);
        }
    }
    
    @Override
    public Token<JadeTokenId> nextToken() {
        try {
            JadeTokenId tokenId = scanner.nextToken();
            Token<JadeTokenId> token = null;
            if (tokenId != null) {
                token = tokenFactory.createToken(tokenId);
            }
            if (token != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(token.id());
                if (token != null) {
                    sb.append(" length: " + token.length());
                }
                LOGGER.log(Level.FINEST, "Lexed token is {0}", sb.toString());
            } 
            return token;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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
