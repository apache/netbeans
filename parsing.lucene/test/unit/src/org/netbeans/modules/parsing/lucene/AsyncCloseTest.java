/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class AsyncCloseTest extends NbTestCase {

    private static final String FLD_KEY = "key";    //NOI18N

    private File indexFolder;


    public AsyncCloseTest(final String name){
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        indexFolder = FileUtil.normalizeFile(new File(getWorkDir(),"index"));   //NOI18N
        indexFolder.mkdirs();
    }



    public void testAsyncClose() throws Exception {
        final CountDownLatch slot = new CountDownLatch(1);
        final CountDownLatch signal = new CountDownLatch(1);
        final  CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();

        final Index index = IndexManager.createTransactionalIndex(indexFolder, new KeywordAnalyzer());
        final Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    index.store(
                       new ArrayList<String>(Arrays.asList("foo")), //NOI18N
                       Collections.<String>emptySet(),
                       new TestInsertConvertor(slot, signal),
                       new TestDeleteConvertor(),
                       true);
                } catch (Exception ex) {
                    exception.set(ex);
                } finally {
                    done.countDown();
                }
            }
        });
        worker.start();

        signal.await();
        slot.countDown();
        index.close();
        done.await();
        assertNull(exception.get());
    }

    public void testConcurrentReadWrite() throws Exception {
        final Index index = IndexManager.createTransactionalIndex(indexFolder, new KeywordAnalyzer());
        index.store(
            new ArrayList<String>(Arrays.asList("a")), //NOI18N
            Collections.<String>emptySet(),
            new TestInsertConvertor(),
            new TestDeleteConvertor(),
            true);

        final CountDownLatch slot = new CountDownLatch(1);
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<Exception> result = new AtomicReference<Exception>();

        final Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    index.store(
                           new ArrayList<String>(Arrays.asList("b")), //NOI18N
                           Collections.<String>emptySet(),
                           new TestInsertConvertor(slot, signal),
                           new TestDeleteConvertor(),
                           true);
                } catch (Exception e) {
                    result.set(e);
                } finally {
                    done.countDown();
                }
            }
        });

        worker.start();
        signal.await();

        final Collection<String> data = new ArrayList<String>();
        index.query(
            data,
            new Convertor<Document,String>(){
                @Override
                public String convert(Document p) {
                    return p.get(FLD_KEY);
                }
            },
            null,
            new AtomicBoolean(),
            new PrefixQuery(new Term(FLD_KEY,""))); //NOI18N
        assertEquals(1, data.size());
        assertEquals("a", data.iterator().next());  //NOI18N
        slot.countDown();
        done.await();
        assertNull(result.get());
    }

    private static final class TestInsertConvertor implements Convertor<String, Document> {

        private final CountDownLatch slot;
        private final CountDownLatch signal;

        TestInsertConvertor(
                @NonNull final CountDownLatch slot,
                @NonNull final CountDownLatch signal) {
            Parameters.notNull("slot", slot);   //NOI18N
            Parameters.notNull("signal", signal);   //NOI18N
            this.slot = slot;
            this.signal = signal;
        }

        TestInsertConvertor() {
            slot = null;
            signal = null;
        }

        @Override
        public Document convert(String p) {
            if (signal != null) {
                signal.countDown();
            }

            if (slot != null) {
                try {
                    this.slot.await();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            final Document doc = new Document();
            doc.add(new Field(FLD_KEY, p, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));   //NOI18N
            return doc;
        }
    }

    private static final class TestDeleteConvertor implements Convertor<String, Query> {
        @Override
        public Query convert(String p) {
            return new TermQuery(new Term(FLD_KEY,p));    //NOI18N
        }
    }

}
