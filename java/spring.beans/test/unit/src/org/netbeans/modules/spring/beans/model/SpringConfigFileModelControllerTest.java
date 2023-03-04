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

package org.netbeans.modules.spring.beans.model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.LockedDocument;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;

/**
 *
 * @author Andrei Badea
 */
public class SpringConfigFileModelControllerTest extends ConfigFileTestCase {

    public SpringConfigFileModelControllerTest(String testName) {
        super(testName);
    }

    public void testReadParse() throws Exception {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        final ParseCountingBeanSource beanSource = new ParseCountingBeanSource();
        final SpringConfigFileModelController controller = new SpringConfigFileModelController(configFile, beanSource);

        ExclusiveAccess.getInstance().runSyncTask(new  Callable<Void>() {
            public Void call() throws IOException {
                controller.getUpToDateBeanSource();
                // Document was parsed, since this was the very first access.
                assertEquals(1, beanSource.getAndResetParseCount());
                // Subsequente getBeanSource() call should not cause any parsing.
                controller.getUpToDateBeanSource();
                // Subsequent getDocumentRead() calls should not cause any parsing,
                // since there have been no changes to the file.
                assertEquals(0, beanSource.getAndResetParseCount());
                return null;
            }
        });
    }

    public void testWriteParse() throws Exception {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        final ParseCountingBeanSource beanSource = new ParseCountingBeanSource();
        final SpringConfigFileModelController controller = new SpringConfigFileModelController(configFile, beanSource);

        ExclusiveAccess.getInstance().runSyncTask(new  Callable<Void>() {
            public Void call() throws IOException {
                beanSource.getAndResetParseCount();
                LockedDocument lockedDoc = controller.getLockedDocument();
                lockedDoc.lock();
                try {
                    lockedDoc.getBeanSource();
                    // Document was parsed, since this was the very first access.
                    assertEquals(1, beanSource.getAndResetParseCount());
                    lockedDoc.getBeanSource();
                    lockedDoc.getBeanSource();
                    // Subsequence getBeanSource() call should not cause any parsing.
                    assertEquals(0, beanSource.getAndResetParseCount());
                } finally {
                    lockedDoc.unlock();
                }
                lockedDoc = controller.getLockedDocument();
                lockedDoc.lock();
                try {
                    lockedDoc.getBeanSource();
                    // Subsequent getDocumentWrite() should parse, even though the file was not changed.
                    assertEquals(1, beanSource.getAndResetParseCount());
                } finally {
                    lockedDoc.unlock();
                }
                return null;
            }
        });
    };

    public void testCanGCDocument() throws Exception {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);

        final SpringConfigFileModelController controller = new SpringConfigFileModelController(configFile, new ConfigFileSpringBeanSource());
        final BaseDocument[] doc = { null };
        ExclusiveAccess.getInstance().runSyncTask(new  Callable<Void>() {
            public Void call() throws IOException {
                LockedDocument lockedDoc = controller.getLockedDocument();
                lockedDoc.lock();
                try {
                    doc[0] = lockedDoc.getDocument();
                    assertTrue(doc[0].isAtomicLock());
                } finally {
                    lockedDoc.unlock();
                    assertFalse(doc[0].isAtomicLock());
                }
                return null;
            }
        });
        WeakReference<Document> docRef = new WeakReference<Document>(doc[0]);
        doc[0] = null;
        assertGC("Should be possible to GC the document", docRef);
    }

    public void testIOErrorInLockedDocumentParse() throws Exception {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);

        final SpringConfigFileModelController controller = new SpringConfigFileModelController(configFile, new IOExceptionSpringBeanSource());
        ExclusiveAccess.getInstance().runSyncTask(new  Callable<Void>() {
            public Void call() throws IOException {
                LockedDocument lockedDoc = controller.getLockedDocument();
                assertFalse(lockedDoc.document.isAtomicLock());
                try {
                    lockedDoc.lock();
                    fail(); // Should have thrown IOException.
                } catch (IOException e) {
                    // Expected.
                } finally {
                    assertFalse(lockedDoc.document.isAtomicLock());
                }
                return null;
            }
        });
    }

    private static final class ParseCountingBeanSource extends ConfigFileSpringBeanSource {

        private int parseCount;

        @Override
        public void parse(BaseDocument document) throws IOException {
            parseCount++;
            super.parse(document);
        }

        public int getAndResetParseCount() {
            int result = parseCount;
            parseCount = 0;
            return result;
        }
    }

    private static final class IOExceptionSpringBeanSource extends ConfigFileSpringBeanSource {

        @Override
        public void parse(BaseDocument document) throws IOException {
            throw new IOException();
        }
    }
}
