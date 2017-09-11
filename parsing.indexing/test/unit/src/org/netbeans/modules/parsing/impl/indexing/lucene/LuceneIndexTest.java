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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {

    private static File wd;
    private File indexFolder;
    private DocumentIndex index;
    
    public LuceneIndexTest(final String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        wd = getWorkDir();
        indexFolder = new File (wd, "index");   //NOI18N
        indexFolder.mkdirs();
        index = IndexManager.createDocumentIndex(indexFolder);
    }

    @After
    @Override
    public void tearDown() throws IOException {
        index.close();
    }

    @Test
    public void testIndexAddDelete() throws Exception {
        for (int i=0; i< 1000; i++) {
            IndexDocument docwrap = IndexManager.createDocument(Integer.toString(i));
            docwrap.addPair("bin", Integer.toBinaryString(i), true, true);
            docwrap.addPair("oct", Integer.toOctalString(i), true, true);
            index.addDocument(docwrap);
        }
        index.store(true);
        BitSet expected = new BitSet(1000);
        expected.set(0, 1000);
        assertIndex(expected);
        for (int i = 100; i<200; i++) {
            index.removeDocument(Integer.toString(i));
            expected.clear(i);
        }
        index.store(true);
        assertIndex(expected);
    }

// Commented out as it takes a long time
//    @Test
//    public void testPerformance() throws Exception {
//        System.gc(); System.gc(); System.gc();
//        long st = System.currentTimeMillis();
//        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
//        long start = bean.getHeapMemoryUsage().getUsed();
//        for (int ic = 0; ic < 2; ic++) {
//            final LuceneIndex index = new LuceneIndex(new File(getWorkDir(),Integer.toString(ic)).toURI().toURL());
//            for (int i=0; i< 1000000; i++) {
//                LuceneDocument doc = new LuceneDocument(SPIAccessor.getInstance().create(new FakeIndexableImpl(i)));
//                doc.addPair("bin-value", Integer.toBinaryString(i), true, true);
//                doc.addPair("dec-value", Integer.toString(i), true, true);
//                index.addDocument(doc);
//            }
//            index.store();
//        }
//        long et = System.currentTimeMillis();
//        for (int i=0; i< 2; i++) {
//            System.gc(); System.gc(); System.gc();
//            Thread.sleep(500);
//        }
//
//        long end = bean.getHeapMemoryUsage().getUsed();
//        assertTrue(end < 3 * start);
//    }



    private void assertIndex(final BitSet expected) throws IOException, InterruptedException {
        for (int i=0; i < expected.length(); i++) {
            final Collection<? extends IndexDocument> res = index.query("bin", Integer.toBinaryString(i), Queries.QueryKind.EXACT, "bin","oct");
            boolean should = expected.get(i);
            assertEquals(should, res.size()==1);
            if (should) {
                assertEquals(res.iterator().next().getValue("bin"), Integer.toBinaryString(i));
                assertEquals(res.iterator().next().getValue("oct"), Integer.toOctalString(i));
            }
        }
    }    

}
