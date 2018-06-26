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
package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.GSFPHPParser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Provides model for html code.
 *
 * @author Marek Fukala
 */
public class PhpEmbeddingProvider extends EmbeddingProvider {

    public static final String GENERATED_CODE = "@@@"; //NOI18N
    private static final int MAX_EMBEDDING_LENGTH = 5000000; //cca 5M
    private static final Logger LOGGER = Logger.getLogger(PhpEmbeddingProvider.class.getName());
    private static final String HTML_MIME_TYPE = "text/html"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (!GSFPHPParser.PARSE_BIG_FILES) {
            FileObject fileObject = snapshot.getSource().getFileObject();
            long textSize = getTextSize(fileObject, snapshot);
            if (textSize > GSFPHPParser.BIG_FILE_SIZE) {
                LOGGER.log(Level.INFO, "Parsing of big file cancelled. Size: {0} Name: {1}",
                        new Object[] {textSize, fileObject != null ? FileUtil.getFileDisplayName(fileObject) : "<no file>"});
                return Collections.emptyList();
            }
        }

        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<PHPTokenId> sequence = th.tokenSequence(PHPTokenId.language());

        //issue #159775 logging >>>
        if (sequence == null) {
            Logger.getLogger("PhpEmbeddingProvider").log(
                    Level.WARNING,
                    "TokenHierarchy.tokenSequence(PhpTokenId.language()) == null " + "for static immutable PHP TokenHierarchy!\nFile = ''{0}'' ;snapshot mimepath=''{1}''",
                    new Object[]{snapshot.getSource().getFileObject().getPath(), snapshot.getMimePath()});

            return Collections.emptyList();
        }
        //<<< end of the logging

        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<>();

        //marek (workaround): there seems to be a bug in parsing api - if I create
        //the embedding for each PHPTokenId.T_INLINE_HTML token separatelly then the offsets
        //translation is broken
        int from = -1;
        int len = 0;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == PHPTokenId.T_INLINE_HTML) {
                if (from < 0) {
                    from = sequence.offset();
                }
                len += t.length();
            } else {
                if (from >= 0) {
                    //lets suppose the text is always html :-(
                    createHtmlEmbedding(embeddings, snapshot, from, len);
                    //add only one virtual generated token for a sequence of PHP tokens
                    embeddings.add(snapshot.create(GENERATED_CODE, HTML_MIME_TYPE));
                }

                from = -1;
                len = 0;
            }
        }

        if (from >= 0) {
            createHtmlEmbedding(embeddings, snapshot, from, len);
        }

        if (embeddings.isEmpty()) {
            //always embed html even if there isn't any
            //this causes the parsing api to run tasks registered to text/html
            //even if there isn't any html content
            return Collections.singletonList(snapshot.create("", HTML_MIME_TYPE));
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    private static void createHtmlEmbedding(List<Embedding> embeddings, Snapshot snapshot, int from, int length) {
        assert embeddings != null;
        assert snapshot != null;
        if (length <= MAX_EMBEDDING_LENGTH) {
            embeddings.add(snapshot.create(from, length, HTML_MIME_TYPE)); //NOI18N
        } else {
            LOGGER.log(Level.FINE, "HTML embedding wasn''t created - from: {0}, length: {1}", new Object[] {from, length});
        }
    }

    @Override
    public int getPriority() {
        return 110;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    private long getTextSize(@NullAllowed FileObject fileObject, Snapshot snapshot) {
        if (fileObject != null) {
            return fileObject.getSize();
        }
        return snapshot.getText().length();
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new PhpEmbeddingProvider());
        }
    }
}
