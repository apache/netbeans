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

import java.io.File;
import java.util.List;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.TextDetail;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Tests to check that all FileMatchers give the same results.
 *
 * @author jhavlin
 */
public class MatchersTest extends NbTestCase {

    private SearchPattern sp = SearchPattern.create("netbeans", false, false, false);

    public MatchersTest(String name) {
        super(name);
    }

    public void testMultiLineMappedMatcherBig() {

        checkFile(new MultiLineMappedMatcherBig(sp));
    }

    public void testMultiLineMappedMatcherSmall() {
        checkFile(new MultiLineMappedMatcherSmall(sp));
    }

    public void testMultiLineStreamMatcher() {
        checkFile(new MultiLineStreamMatcher(sp));
    }

    public void testSingleLineStreamMatcher() {
        checkFile(new SingleLineStreamMatcher(sp));
    }

    public void testAsciiMultiLineMappedMatcher() {
        checkFile(new AsciiMultiLineMappedMatcher(sp));
    }

    /**
     * Test checking of file more_than_4KB.txt
     */
    private void checkFile(AbstractMatcher abstractMatcher) {

        File file = MatcherTestUtils.getFile("matches_2.txt");
        assertNotNull(file);

        MatchingObject.Def def = abstractMatcher.check(
                FileUtil.toFileObject(file),
                new SearchListener() {

                    @Override
                    public void generalError(Throwable t) {
                        Exceptions.printStackTrace(t);
                    }
                });
        assertNotNull(def);
        List<TextDetail> textDetails = def.getTextDetails();
        assertNotNull(textDetails);
        assertEquals(2, textDetails.size());
        TextDetail td = textDetails.get(0);
        assertNotNull(td);
        assertEquals(2, td.getLine());
        assertEquals(10, td.getColumn());
        assertEquals("NETBeans", td.getMatchedText());
        TextDetail td2 = textDetails.get(1);
        assertNotNull(td2);
        assertEquals(7, td2.getLine());
        assertEquals(26, td2.getColumn());
        assertEquals("NetBEANS", td2.getMatchedText());
    }
}
