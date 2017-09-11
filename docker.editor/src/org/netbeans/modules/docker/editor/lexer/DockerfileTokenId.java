/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
