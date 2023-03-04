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
package org.netbeans.modules.jshell.model;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.jshell.parsing.JShellLexer;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author sdedic
 */
public enum JShellToken implements TokenId {
    /**
     * Prompt for the user input
     *//**
     * Prompt for the user input
     */
    PROMPT("prompt", "-> "),
    
    /**
     * Continuation prompt
     */
    CONTINUATION_PROMPT("prompt", "->> "),
    
    /**
     * Marks message from the shell
     */
    MESSAGE_MARK("delimiter", "|  "),
    
    /**
     * Text of a message
     */
    MESSAGE_TEXT("message"),
    
    /**
     * Marks/highlights error position
     */
    ERROR_MARKER("mark-error"),
    
    /**
     * Java(jshell) input
     */
    JAVA("java"),
    
    /**
     * JShell command
     */
    COMMAND("command"),
    
    /**
     * parameters for the command
     */
    COMMAND_WHITESPACE("command-whitespace"),
    
    /**
     * parameters for the command
     */
    COMMAND_PARAM("command-text"),
    COMMAND_STRING("command-string"),
    
    /**
     * Dash option for a command
     */
    COMMAND_OPTION("command-option"),
    
    /**
     * Unknown command
     */
    ERR_COMMAND("error"),
    
    /**
     * Primarily command output
     */
    OUTPUT("output"),
    
    WHITESPACE("whitespace");
    ;
    
    @Override
    public String primaryCategory() {
        return prim;
    }
    
    @MimeRegistration(service = Language.class,
            mimeType = "text/x-repl")
    public static Language<JShellToken> language() {
        return lang;
    }
    
    private static final Language<JShellToken> lang = new LanguageHierarchy<JShellToken>() {

        @Override
        protected Collection<JShellToken> createTokenIds() {
            return EnumSet.allOf(JShellToken.class);
        }

        @Override
        protected Lexer<JShellToken> createLexer(LexerRestartInfo<JShellToken> info) {
            return new JShellLexer(info);
        }

        @Override
        protected String mimeType() {
            return "text/x-repl";
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<JShellToken> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            if (token.id() != JAVA) {
                return null;
            }
            return LanguageEmbedding.create(JavaTokenId.language(), 0, 0, true);
        }

        @Override
        protected EmbeddingPresence embeddingPresence(JShellToken id) {
            return id == JAVA ? EmbeddingPresence.CACHED_FIRST_QUERY : EmbeddingPresence.NONE;
        }
    }.language();

    private JShellToken(String prim) {
        this.prim = prim;
        this.text = null;
    }
    
    private JShellToken(String prim, String text) {
        this.prim = prim;
        this.text = text;
    }
    
    public String fixedText() {
        return text;
    }

    private final String prim;
    
    private final String text;
}
