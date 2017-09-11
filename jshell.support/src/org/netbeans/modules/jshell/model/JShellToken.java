/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
