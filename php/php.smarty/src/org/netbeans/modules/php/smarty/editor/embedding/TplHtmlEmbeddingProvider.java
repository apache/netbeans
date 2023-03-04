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