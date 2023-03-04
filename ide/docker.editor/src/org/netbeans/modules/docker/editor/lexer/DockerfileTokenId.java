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
package org.netbeans.modules.docker.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.docker.editor.DockerfileResolver;
import org.netbeans.modules.docker.editor.parser.Command;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public enum DockerfileTokenId implements TokenId {
    ERROR(null, "error"),                       //NOI18N
    WHITESPACE(null, "whitespace"),             //NOI18N
    IDENTIFIER(null, "identifier"),             //NOI18N
    STRING_LITERAL(null, "string"),             //NOI18N
    NUMBER_LITERAL(null, "number"),             //NOI18N
    LINE_COMMENT(null, "comment"),              //NOI18N
    LBRACKET("[", "separator"),                 //NOI18N
    RBRACKET("]", "separator"),                 //NOI18N
    COMMA(",", "separator"),                    //NOI18N
    ESCAPE("\\", "separator"),                  //NOI18N

    ADD(Command.ADD),
    ARG(Command.ARG),
    CMD(Command.CMD),
    COPY(Command.COPY),
    ENTRYPOINT(Command.ENTRYPOINT),
    ENV(Command.ENV),
    EXPOSE(Command.EXPOSE),
    FROM(Command.FROM),
    LABEL(Command.LABEL),
    MAINTAINER(Command.MAINTAINER),
    ONBUILD(Command.ONBUILD),
    RUN(Command.RUN),
    STOPSIGNAL(Command.STOPSIGNAL),
    USER(Command.USER),
    VOLUME(Command.VOLUME),
    WORKDIR(Command.WORKDIR)
    ;

    private final String fixedText;
    private final String primaryCategory;
    private final boolean onBuildSupported;

    private DockerfileTokenId(
        @NullAllowed final String fixedText,
        @NonNull final String primaryCategory) {
        this(fixedText, primaryCategory, false);
    }

    private DockerfileTokenId(@NonNull Command cmd) {
        this(
                cmd.getName(),
                "keyword",  //NOI18N
                cmd.isOnBuildSupported());
    }

    private DockerfileTokenId(
        @NullAllowed final String fixedText,
        @NonNull final String primaryCategory,
        final boolean onBuildSupported) {
        Parameters.notNull("primaryCategory", primaryCategory); //NOI18N
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
        this.onBuildSupported = onBuildSupported;
    }

    @CheckForNull
    public String fixedText() {
        return fixedText;
    }

    @NonNull
    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public boolean isKeyword() {
        return "keyword".equals(primaryCategory);   //NOI18N
    }

    boolean isOnBuildSupported() {
        return onBuildSupported;
    }

    private static final Language<DockerfileTokenId> language = new LanguageHierarchy<DockerfileTokenId>() {

        @NonNull
        @Override
        protected String mimeType() {
            return DockerfileResolver.MIME_TYPE;
        }

        @NonNull
        @Override
        protected Collection<DockerfileTokenId> createTokenIds() {
            return EnumSet.allOf(DockerfileTokenId.class);
        }

        @Override
        protected Map<String,Collection<DockerfileTokenId>> createTokenCategories() {
            Map<String,Collection<DockerfileTokenId>> cats = new HashMap<>();

            // Literals category
            EnumSet<DockerfileTokenId> lits = EnumSet.of(
                    DockerfileTokenId.STRING_LITERAL,
                    DockerfileTokenId.NUMBER_LITERAL);
            cats.put("literal", lits);

            // Reserved words category
            EnumSet<DockerfileTokenId> kws = EnumSet.noneOf(DockerfileTokenId.class);
            kws.add(ADD);
            kws.add(ARG);
            kws.add(CMD);
            kws.add(COPY);
            kws.add(ENTRYPOINT);
            kws.add(ENV);
            kws.add(EXPOSE);
            kws.add(FROM);
            kws.add(LABEL);
            kws.add(MAINTAINER);
            kws.add(ONBUILD);
            kws.add(RUN);
            kws.add(STOPSIGNAL);
            kws.add(USER);
            kws.add(VOLUME);
            kws.add(WORKDIR);
            cats.put("keyword", kws); //NOI18N
            return cats;
        }

        @Override
        protected Lexer<DockerfileTokenId> createLexer(LexerRestartInfo<DockerfileTokenId> info) {
            return new DockerfileLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
                Token<DockerfileTokenId> token,
                LanguagePath languagePath,
                InputAttributes inputAttributes) {
            //Todo: No embeddings for now, add commets, string embedding
            return null;
        }

    }.language();

    @NonNull
    public static Language<DockerfileTokenId> language() {
        return language;
    }

}
