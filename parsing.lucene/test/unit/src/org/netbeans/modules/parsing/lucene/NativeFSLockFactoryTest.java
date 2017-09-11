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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class NativeFSLockFactoryTest extends NbTestCase {
    
    private File indexFolder;

    public NativeFSLockFactoryTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        indexFolder = new File(getWorkDir(),"index");   //NOI18N
        assertTrue(indexFolder.mkdirs());
    }


    public void testLockFreedDuringStoreError() throws Exception {
        final LuceneIndex index = LuceneIndex.create(indexFolder, new KeywordAnalyzer());
        final Collection<? extends Integer> dataSet = generateDataSet(1000);
        final Logger log = Logger.getLogger(LuceneIndex.class.getName());
        final TestHandler handler = new TestHandler(
            new Runnable() {
                @Override
                public void run() {
                    //Break index a bit ;-)
                    for (File f : indexFolder.listFiles()) {
                        if (f.getName().startsWith("nb-lock")) {    //NOI18N
                            continue;
                        }
                        f.delete();
                    }                    
                }
        });
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        boolean success = false;
        try {
            index.store(
                    dataSet,
                    Collections.<String>emptySet(),
                    new Convertor<Integer, Document>() {
                        @Override
                        public Document convert(Integer p) {
                            final Document doc = new Document();
                            doc.add(new Field(
                                    "val",                  //NOI18N
                                    Integer.toString(p),
                                    Field.Store.YES,
                                    Field.Index.ANALYZED_NO_NORMS));
                            return doc;
                        }
                    },
                    new Convertor<String, Query>() {
                        @Override
                        public Query convert(String p) {
                            throw new UnsupportedOperationException();
                        }
                    },
                    true);
            success = true;
        } catch (Throwable t) {
            //Ignore - should be thrown and success should be false
        } finally {
            log.removeHandler(handler);
        }
        assertFalse(success);
        success = false;
        try {
            index.store(
                dataSet,
                Collections.<String>emptySet(),
                new Convertor<Integer, Document>() {
                    @Override
                    public Document convert(Integer p) {
                        final Document doc = new Document();
                        doc.add(new Field(
                                "val",                  //NOI18N
                                Integer.toString(p),
                                Field.Store.YES,
                                Field.Index.ANALYZED_NO_NORMS));
                        return doc;
                    }
                },
                new Convertor<String, Query>() {
                    @Override
                    public Query convert(String p) {
                        throw new UnsupportedOperationException();
                    }
                },
                true);
            success = true;
        } catch (Throwable t) {
            //Should not be thrown and success should be true
            t.printStackTrace();
        }
        assertTrue(success);
    }



    private static Collection<? extends Integer> generateDataSet(final int count) {
        final List<Integer> res = new ArrayList<Integer>(count);
        for (int i=0; i< count; i++) {
            res.add(i);
        }
        return res;
    }

    private static class TestHandler extends Handler {

        private final Runnable action;

        TestHandler(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            this.action = action;
        }

        @Override
        public void publish(LogRecord record) {
            if ("Committing {0}".equals(record.getMessage())) { //NOI18N
                action.run();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }
}
