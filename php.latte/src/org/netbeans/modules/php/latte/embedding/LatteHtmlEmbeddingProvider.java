/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@EmbeddingProvider.Registration(mimeType = LatteLanguage.LATTE_MIME_TYPE, targetMimeType = LatteHtmlEmbeddingProvider.TARGET_MIME_TYPE)
public class LatteHtmlEmbeddingProvider extends EmbeddingProvider {
    public static final String TARGET_MIME_TYPE = "text/html"; //NOI18N
    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), LatteTopTokenId.language());
        TokenSequence<LatteTopTokenId> ts = th.tokenSequence(LatteTopTokenId.language());
        if (ts == null) {
            return Collections.<Embedding>emptyList();
        }
        ts.moveStart();
        List<Embedding> embeddings = new ArrayList<>();
        int from = -1;
        int length = 0;
        while (ts.moveNext()) {
            Token<LatteTopTokenId> token = ts.token();
            if (token != null && isPureHtmlToken(token)) {
                if (from < 0) {
                    from = ts.offset();
                }
                length += token.length();
            } else {
                if (from >= 0) {
                    embeddings.add(snapshot.create(from, length, TARGET_MIME_TYPE));
                    embeddings.add(snapshot.create(GENERATED_CODE, TARGET_MIME_TYPE));
                    from = -1;
                    length = 0;
                }
            }
        }
        if (from >= 0) {
            embeddings.add(snapshot.create(from, length, TARGET_MIME_TYPE));
        }
        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", TARGET_MIME_TYPE)); //NOI18N
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    private boolean isPureHtmlToken(Token<LatteTopTokenId> token) {
        CharSequence tokenText = token.text();
        return token.id() == LatteTopTokenId.T_HTML
                && (tokenText == null || (!tokenText.toString().endsWith("{") && !tokenText.toString().startsWith("}"))); //NOI18N
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void cancel() {
    }

}
