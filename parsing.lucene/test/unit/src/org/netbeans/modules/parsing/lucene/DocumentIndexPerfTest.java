/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex2;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentIndexPerfTest extends NbTestCase {

    private static final int NO_RUNS = 0;
    private static final int NO_ROUNDS = 100;
    private static final int ROUND_SIZE = 1_000;

    private final AtomicLong seq = new AtomicLong();

    public DocumentIndexPerfTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testRegExpQuery() throws IOException, InterruptedException {
        if (NO_RUNS > 0) {
            long t = 0L;
            final File indexFolder = getWorkDir();
            for (int cr = 0; cr < NO_RUNS; cr++) {
                deleteContent(indexFolder);
                final DocumentIndex2 index = (DocumentIndex2) IndexManager.createDocumentIndex(indexFolder);
                prepareData(index);
                Query q = Queries.createQuery("bin", "bin", "101.*1", Queries.QueryKind.REGEXP);  //NOI18N
                final Convertor<IndexDocument,Long> c = new IndexDocumentToLong();
                for (int i = 0; i< ROUND_SIZE; i++) {
                    long st = System.nanoTime();
                    index.query(q, c, "bin","hex"); //NOI18N
                    t+= System.nanoTime() - st;
                }
                q = Queries.createQuery("bin", "bin", Pattern.quote("101")+".*1", Queries.QueryKind.REGEXP);  //NOI18N
                for (int i = 0; i< ROUND_SIZE; i++) {
                    long st = System.nanoTime();
                    index.query(q, c, "bin","hex"); //NOI18N
                    t+= System.nanoTime() - st;
                }
                index.close();
            }
            System.out.println("Regexp query: " + (t/(1_000_000 * NO_RUNS)));
        }
    }

    private void prepareData(@NonNull final DocumentIndex2 index) throws IOException {
        for (int i=0; i < NO_ROUNDS; i++) {
            List<Long> data = generateData(ROUND_SIZE);
            addDataToIndex(index, data);
        }
    }

    private List<Long> generateData(int size) {
        final List<Long> res = new ArrayList<>(size);
        for (int i=0; i<size; i++) {
            res.add(seq.incrementAndGet());
        }
        return res;
    }

    private void addDataToIndex(
            @NonNull final DocumentIndex2 index,
            @NonNull final List<Long> data) throws IOException {
        for (long l : data) {
            final IndexDocument doc = IndexManager.createDocument(Long.toString(l));
            doc.addPair("bin", Long.toBinaryString(l), true, true); //NOI18N
            doc.addPair("hex", Long.toHexString(l), true, true);    //NOI18N
            index.addDocument(doc);
        }
        index.store(false);
    }

    private static void deleteContent(File folder) {
        final File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteContent(child);
                }
                child.delete();
            }
        }
    }

    private static final class IndexDocumentToLong implements Convertor<IndexDocument, Long> {
        @Override
        public Long convert(IndexDocument p) {
            final String hexStr = p.getValue("hex");    //NOI18N
            return Long.valueOf(hexStr, 16);
        }
    }

}
