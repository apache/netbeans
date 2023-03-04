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
package org.netbeans.modules.search.matcher;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class AbstractMatcherTest extends NbTestCase {

    private SearchPattern searchPattern;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        searchPattern = SearchPattern.create("a", false, false, false);
    }

    @Override
    protected void tearDown() throws Exception {
        searchPattern = null;
        super.tearDown();
    }

    public AbstractMatcherTest(String name) {
        super(name);
    }

    /**
     * Check that an exception is thrown if a decoding error occurs.
     */
    public void testStrictModeOn() {
        testStrinctMode(true, true);
    }

    /**
     * Check that no expception is thrown if a decoding error occurs.
     */
    public void testStrictModeOff() {
        testStrinctMode(false, false);
    }

    private void testStrinctMode(boolean strict, boolean expectException) {
        MockServices.setServices(Utf8FileEncodingQueryImpl.class);
        FileObject dir = FileUtil.toFileObject(getDataDir());
        assertNotNull(dir);
        FileObject file = dir.getFileObject("textFiles/latin2file.txt");
        AbstractMatcher am = new DefaultMatcher(searchPattern);
        am.setStrict(strict);
        assertTrue(file.isValid());
        ExceptionListener el = new ExceptionListener();
        am.check(file, el);
        assertEquals(expectException, el.exception);
    }

    /**
     * Listener storing information about decoding problems.
     */
    private class ExceptionListener extends SearchListener {

        private boolean exception = false;

        @Override
        public void fileContentMatchingError(String fileName,
                Throwable throwable) {
            exception = true;
        }
    }

    /**
     * Set incorrect encoding for ISO-8859-2 files.
     */
    public static class Utf8FileEncodingQueryImpl
            extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject file) {
            if (file.getName().equals("latin2file")) {
                return StandardCharsets.UTF_8;
            } else {
                return null;
            }
        }
    }
}
