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

package org.netbeans.modules.php.editor.lexer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.filesystems.FileObject;



/**
 *
 * @author Petr Pisl, Marek Fukala
 */
public final class GSFPHPLexer implements Lexer<PHPTokenId> {
    private final PHP5ColoringLexer scanner;
    private final TokenFactory<PHPTokenId> tokenFactory;

    private GSFPHPLexer(LexerRestartInfo<PHPTokenId> info, boolean shortTag, boolean aspTag, boolean inPHP) {
        scanner = new PHP5ColoringLexer(info, shortTag, aspTag, inPHP);
        tokenFactory = info.tokenFactory();
    }

    public static GSFPHPLexer create(LexerRestartInfo<PHPTokenId> info, boolean inPHP) {
        PhpLanguageProperties languageProperties;
        FileObject fileObject = (FileObject) info.getAttributeValue(FileObject.class);
        if (fileObject != null) {
            languageProperties = PhpLanguageProperties.forFileObject(fileObject);
        } else {
            languageProperties = PhpLanguageProperties.getDefault();
        }
        boolean aspTag = languageProperties.areAspTagsEnabled();
        boolean shortTag = languageProperties.areShortTagsEnabled();
        synchronized (GSFPHPLexer.class) {
            return new GSFPHPLexer(info, shortTag, aspTag, inPHP);
        }
    }

    @Override
    public Token<PHPTokenId> nextToken() {
        try {
            PHPTokenId tokenId = scanner.nextToken();
            Token<PHPTokenId> token = null;
            if (tokenId != null) {
                if (tokenId == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) {
                   PHP5ColoringLexer.LexerState state = scanner.getState();
                   if (state.heredoc != null && state.heredoc.equals("CSS")) {
                       tokenId = PHPTokenId.T_EMBEDDED_CSS;
                   }
                }
                token = tokenFactory.createToken(tokenId);
            }
            return token;
        } catch (IOException ex) {
            Logger.getLogger(GSFPHPLexer.class.getName()).log(Level.SEVERE, null, ex);
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
