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

package org.netbeans.modules.subversion.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Tests org.netbeans.modules.subversion.util.SVNUtils utility methods
 * @author ondra.vrabec
 */
public class SvnUtilsTest extends NbTestCase {

    public SvnUtilsTest(String testName) {
        super(testName);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getMatchinIgnoreParterns method, of class SvnUtils.
     */
    @Test
    public void testGetMatchinIgnoreParterns() {
        System.out.println("getMatchinIgnoreParterns test");
        final List<String> patterns = new LinkedList<String>();
        final HashMap<String, List<String>> testData = new HashMap<String, List<String>>();
        final boolean onlyFirstMatch = false;
        LinkedList<String> expectedResults = null;

        // predefined patterns to be tested
        patterns.add("full pattern.file");  // 0
        patterns.add("partial pattern*");   // 1
        patterns.add("[0-9]full pattern.file"); // 2
        patterns.add("[0-9]-[0-9]-[0-9]*.file");    // 3
        patterns.add("  *  ");  // 4
        patterns.add("\\[0-9].file");  // 5
        patterns.add("\\[*]*");  // 6
        patterns.add("\\[*\\]*");  // 7
        patterns.add("[0_a-d]*");  // 8
        patterns.add("?<([0-9][a-z]\\[\\[]]>).file");  // 9
        patterns.add("?*F?.file");  // 10
        patterns.add("??.file");  // 11

        // now setting expected results

        // no pattern returned
        expectedResults = new LinkedList<String>();
        testData.put("notexists.file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(0));
        testData.put("full pattern.file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(1));
        testData.put("partial pattern.file", expectedResults);

        // numbers full pattern
        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(2));
        testData.put("1full pattern.file", expectedResults);

        // numbers pattern with asterisk
        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(3));
        testData.put("1-5-9 anything here.file", expectedResults);

        // some gaps
        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(4));
        testData.put("  .file.  ", expectedResults);

        // [,] in filename
        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(5));
        expectedResults.add(patterns.get(6));
        expectedResults.add(patterns.get(7));
        testData.put("[0-9].file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(6));
        expectedResults.add(patterns.get(7));
        testData.put("[0123456789].file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(8));
        testData.put("0.file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(8));
        testData.put("_.file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(8));
        testData.put("a.file", expectedResults);

        // some crazy filenames
        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(9));
        testData.put("f<(5g[[]]>).file", expectedResults);

        expectedResults = new LinkedList<String>();
        testData.put("(5g[[]]).file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(10));
        testData.put("AFA.file", expectedResults);

        expectedResults = new LinkedList<String>();
        expectedResults.add(patterns.get(11));
        testData.put("AA.file", expectedResults);

        // the testing process
        for (Entry<String, List<String>> entry : testData.entrySet()) {
            System.out.println("testGetMatchinIgnoreParterns: value=" + entry.getKey());
            List<String> result = SvnUtils.getMatchinIgnoreParterns(patterns, entry.getKey(), onlyFirstMatch);
            assertEquals(entry.getValue(), result);
        }
    }

    /**
     * Test of decodeAndEncodeUrl method, of class SvnUtils.
     */
    @Test
    public void testDecodeAndEncodeUrl() throws Exception {
        System.out.println("testDecodeAndEncodeUrl test");
        final HashMap<SVNUrl, SVNUrl> testingData = new HashMap<SVNUrl, SVNUrl>();
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file"), new SVNUrl("file:///var/lib/repository/svn/ja1/file"));
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file[]"), new SVNUrl("file:///var/lib/repository/svn/ja1/file%5B%5D"));
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file%5b%5d"), new SVNUrl("file:///var/lib/repository/svn/ja1/file%5B%5D"));
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file%5B%5D"), new SVNUrl("file:///var/lib/repository/svn/ja1/file%5B%5D"));
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file[ ]"), new SVNUrl("file:///var/lib/repository/svn/ja1/file%5B%20%5D"));
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file-*_.java@"), new SVNUrl("file:///var/lib/repository/svn/ja1/file-*_.java@")); // do not escape '@', '/', ':'
        testingData.put(new SVNUrl("file:///var/lib/repository/svn/ja1/file;?#%[]@"), new SVNUrl("file:///var/lib/repository/svn/ja1/file%3B%23%25%5B%5D@")); // do not escape '@', '/', ':'
        testingData.put(new SVNUrl("file:///repositoř"), new SVNUrl("file:///repositoř"));

        // test
        for (Entry<SVNUrl, SVNUrl> entry : testingData.entrySet()) {
            assertEquals(entry.getValue(), SvnUtils.decodeAndEncodeUrl(entry.getKey()));
        }
    }
}
