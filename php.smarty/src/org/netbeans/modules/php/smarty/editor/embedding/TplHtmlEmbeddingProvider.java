/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.php.smarty.editor.TplMetaData;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.utlis.TplUtils;

/**
 * Provides embedding of HTML sources within TPL files.
 * Inspired by JspEmbeddingProvider.
 */
public class TplHtmlEmbeddingProvider extends EmbeddingProvider {

    private static final String MIME_TYPE_HTML = "text/html"; //NOI18N
    private static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TplMetaData tplMetaData = TplUtils.getProjectPropertiesForFileObject(snapshot.getSource().getFileObject());
        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(TplTopTokenId.language(), TplMetaData.class, tplMetaData, false);
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), false, TplTopTokenId.language(), null, inputAttributes);
        TokenSequence<TplTopTokenId> sequence = th.tokenSequence(TplTopTokenId.language());

        if (sequence == null) {
            Logger.getLogger("TplHtmlEmbeddingProvider").warning(
                    "TokenHierarchy.tokenSequence(TplTopTokenId.language()) == null "
                    + "for static immutable TPL TokenHierarchy!\nFile = '"
                    + snapshot.getSource().getFileObject().getPath()
                    + "' ;snapshot mimepath='" + snapshot.getMimePath() + "'");

            return Collections.emptyList();
        }

        sequence.moveStart();
        boolean lastEmbeddingIsVirtual = false;
        List<Embedding> embeddings = new ArrayList<Embedding>();
        while (sequence.moveNext()) {
            Token<TplTopTokenId> t = sequence.token();
            if (t.id() == TplTopTokenId.T_HTML) {
                embeddings.add(snapshot.create(sequence.offset(), t.length(), MIME_TYPE_HTML));
                lastEmbeddingIsVirtual = false;
            } else {
                if (!lastEmbeddingIsVirtual) {
                    embeddings.add(snapshot.create(GENERATED_CODE, MIME_TYPE_HTML));
                    lastEmbeddingIsVirtual = true;
                }
            }
        }

        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", MIME_TYPE_HTML)); //NOI18N
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 140;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    @MimeRegistration(service = TaskFactory.class, mimeType = "text/x-tpl")
    public static final class Factory extends TaskFactory {

        @Override
        public Collection create(final Snapshot snapshot) {
            return Collections.singletonList(new TplHtmlEmbeddingProvider());
        }
    }
}