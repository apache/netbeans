/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;

@EmbeddingProvider.Registration(mimeType = TwigLanguage.TWIG_MIME_TYPE, targetMimeType = TwigHtmlEmbeddingProvider.TARGET_MIME_TYPE)
public class TwigHtmlEmbeddingProvider extends EmbeddingProvider {

    public static final String TARGET_MIME_TYPE = "text/html"; //NOI18N
    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), TwigTopTokenId.language());
        TokenSequence<TwigTopTokenId> sequence = th.tokenSequence(TwigTopTokenId.language());
        if (sequence == null) {
            return Collections.emptyList();
        }
        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<>();
        int offset = -1;
        int length = 0;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == TwigTopTokenId.T_HTML) {
                if (offset < 0) {
                    offset = sequence.offset();
                }
                length += t.length();
            } else if (offset >= 0) {
                embeddings.add(snapshot.create(offset, length, TARGET_MIME_TYPE));
                embeddings.add(snapshot.create(GENERATED_CODE, TARGET_MIME_TYPE));
                offset = -1;
                length = 0;
            }
        }
        if (offset >= 0) {
            embeddings.add(snapshot.create(offset, length, TARGET_MIME_TYPE));
        }
        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", TARGET_MIME_TYPE));
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void cancel() {
    }

}
