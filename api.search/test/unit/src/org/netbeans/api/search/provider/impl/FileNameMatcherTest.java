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
