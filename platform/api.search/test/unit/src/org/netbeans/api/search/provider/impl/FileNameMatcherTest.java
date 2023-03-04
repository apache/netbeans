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
package org.netbeans.api.search.provider.impl;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.FileNameMatcher;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test for file name matchers.
 *
 * @author jhavlin
 */
public class FileNameMatcherTest extends NbTestCase {

    FileObject fileObject;
    File file;

    public FileNameMatcherTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws IOException {
        fileObject = FileUtil.createMemoryFileSystem().getRoot().
                createFolder("testdir").createData("testfile.txt");
        file = new File("testdir" + File.separator + "testfile.txt");
    }

    public void testTrivial() {
        FileNameMatcher fnm = FileNameMatcher.create(SearchScopeOptions.create("", false));

        assertTrue(fnm.pathMatches(file));
        assertTrue(fnm.pathMatches(fileObject));
    }

    public void testSimplePattern() {

        FileNameMatcher fnm = FileNameMatcher.create(
                SearchScopeOptions.create("*file.txt", false));

        assertTrue(fnm.pathMatches(file));
        assertTrue(fnm.pathMatches(fileObject));

        FileNameMatcher fnm2 = FileNameMatcher.create(
                SearchScopeOptions.create("*false.txt", false));

        assertFalse(fnm2.pathMatches(file));
        assertFalse(fnm2.pathMatches(fileObject));
    }

    public void testExtension() {

        FileNameMatcher fnm = FileNameMatcher.create(
                SearchScopeOptions.create("*.txt", false));

        assertTrue(fnm.pathMatches(file));
        assertTrue(fnm.pathMatches(fileObject));

        FileNameMatcher fnm2 = FileNameMatcher.create(
                SearchScopeOptions.create("*.php", false));

        assertFalse(fnm2.pathMatches(file));
        assertFalse(fnm2.pathMatches(fileObject));
    }

    public void testRegexpPattern() {
        FileNameMatcher fnm = FileNameMatcher.create(
                SearchScopeOptions.create("testdir.*file", true));

        assertTrue(fnm.pathMatches(file));
        assertTrue(fnm.pathMatches(fileObject));

        FileNameMatcher fnm2 = FileNameMatcher.create(
                SearchScopeOptions.create("anotherdir.*file", true));

        assertFalse(fnm2.pathMatches(file));
        assertFalse(fnm2.pathMatches(fileObject));
    }
}
