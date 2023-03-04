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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import static org.netbeans.modules.php.api.util.FileUtils.PHP_MIME_TYPE;
/**
 * Provides embedding of PHP sources within TPL files.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplPhpEmbeddingProvider extends EmbeddingProvider {

    private static final String GENERATED_CODE = "@@@"; //NOI18N
    private boolean isPhpEnabled;

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {

        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), TplTopTokenId.language());
        TokenSequence<TplTopTokenId> sequence = th.tokenSequence(TplTopTokenId.language());

        if (sequence == null) {
            Logger.getLogger("TplPhpEmbeddingProvider").warning(
                    "TokenHierarchy.tokenSequence(TplTopTokenId.language()) == null "
                    + "for static immutable TPL TokenHierarchy!\nFile = '"
                    + snapshot.getSource().getFileObject().getPath()
                    + "' ;snapshot mimepath='" + snapshot.getMimePath() + "'");

            return Collections.emptyList();
        }

        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<Embedding>();

        int from = -1;
        int len = 0;
        while (sequence.moveNext()) {
            Token<TplTopTokenId> t = sequence.token();
            if (isSmartyToken(t.id())) {
                if (isPhpEnabled) {
                    isPhpEnabled = false;
                    embeddings.add(snapshot.create(";?>", PHP_MIME_TYPE)); //NOI18N
                }
                if (from < 0) {
                    from = sequence.offset();
                }
                len += t.length();
            } else {
                if (from < 0) {
                    from = sequence.offset();
                }
                if (len > 0 && from >= 0) {
                    from += len;
                    len = 0;
                }
                len += t.length();
                if (from >= 0) {
                    if (t.id() == TplTopTokenId.T_PHP) {
                        if (!isPhpEnabled) {
                            isPhpEnabled = true;
                            embeddings.add(snapshot.create("<?", PHP_MIME_TYPE)); //NOI18N
                        }
                        embeddings.add(snapshot.create(from, len, PHP_MIME_TYPE));
                    } else {
                        if (isPhpEnabled) {
                            isPhpEnabled = false;
                            embeddings.add(snapshot.create(";?>", PHP_MIME_TYPE)); //NOI18N
                        }
                    }
                }

                from = -1;
                len = 0;
            }
        }
        if (from >= 0) {
            embeddings.add(snapshot.create(from, len, PHP_MIME_TYPE));
            embeddings.add(snapshot.create(GENERATED_CODE, PHP_MIME_TYPE));
        }
        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", PHP_MIME_TYPE)); //NOI18N
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 130;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    // XXX - Provider is turned off since it brings many troubles with PHP/HTML embedding clashing and the
    // real advantage of the i.e. PHP CC was almost zero since the PHP cannot be embedded into another language
//    @MimeRegistration(service=TaskFactory.class, mimeType="text/x-tpl")
    public static final class Factory extends TaskFactory {

        @Override
        public Collection create(final Snapshot snapshot) {
            return Collections.singletonList(new TplPhpEmbeddingProvider());
        }
    }

    private static boolean isSmartyToken(TplTopTokenId tokenId) {
        return ((tokenId == TplTopTokenId.T_COMMENT) || (tokenId == TplTopTokenId.T_LITERAL_DEL)
                || (tokenId == TplTopTokenId.T_PHP_DEL) || (tokenId == TplTopTokenId.T_SMARTY)
                || (tokenId == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) || (tokenId == TplTopTokenId.T_SMARTY_OPEN_DELIMITER));

    }
}
