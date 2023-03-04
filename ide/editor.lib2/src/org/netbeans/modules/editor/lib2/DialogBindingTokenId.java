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
package org.netbeans.modules.editor.lib2;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek
 */
public enum DialogBindingTokenId implements TokenId {

    TEXT;

    public @Override String primaryCategory() {
        return "text"; //NOI18N
    }

    private static final Language<DialogBindingTokenId> language = new LanguageHierarchy<DialogBindingTokenId>() {
        @Override
        protected Collection<DialogBindingTokenId> createTokenIds() {
            return EnumSet.allOf(DialogBindingTokenId.class);
        }

        @Override
        protected Map<String,Collection<DialogBindingTokenId>> createTokenCategories() {
            return null;
        }

        @Override
        protected Lexer<DialogBindingTokenId> createLexer(LexerRestartInfo<DialogBindingTokenId> info) {
            return new LexerImpl(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<DialogBindingTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            if (inputAttributes == null)
                return null;
            String mimeType = null;
            Document doc = (Document) inputAttributes.getValue(languagePath, "dialogBinding.document"); //NOI18N
            if (doc != null) {
                mimeType = (String)doc.getProperty("mimeType"); //NOI18N
            } else {
                FileObject fo = (FileObject)inputAttributes.getValue(languagePath, "dialogBinding.fileObject"); //NOI18N
                if (fo != null)
                    mimeType = fo.getMIMEType();
            }
            if (mimeType == null)
                return null;
            Language<?> l = MimeLookup.getLookup(mimeType).lookup(Language.class);
            return l != null ? LanguageEmbedding.create(l, 0, 0) : null;
        }

        @Override
        protected EmbeddingPresence embeddingPresence(DialogBindingTokenId id) {
            return EmbeddingPresence.ALWAYS_QUERY;
        }

        @Override
        protected String mimeType() {
            return "text/x-dialog-binding"; //NOI18N
        }
    }.language();

    public static Language<DialogBindingTokenId> language() {
        return language;
    }

    private static final class LexerImpl implements Lexer<DialogBindingTokenId> {

        private TokenFactory<DialogBindingTokenId> tokenFactory;
        private LexerInput input;

        public LexerImpl(LexerRestartInfo<DialogBindingTokenId> info) {
            tokenFactory = info.tokenFactory();
            input = info.input();
        }

        public @Override Token<DialogBindingTokenId> nextToken() {
            int actChar;
            while (true) {
                actChar = input.read();
                if (actChar == LexerInput.EOF) {
                    if (input.readLengthEOF() == 1) {
                        return null;
                    } else {
                        input.backup(1);
                        break;
                    }
                }
            }
            return tokenFactory.createToken(DialogBindingTokenId.TEXT);
        }

        public @Override Object state() {
            return null;
        }

        public @Override void release() {
        }
    }
}
