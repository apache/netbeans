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
package org.netbeans.api.search;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.search.SearchPattern.MatchType;

public class SearchPatternTest {
    
    @Test
    public void testManuallyParse() throws Exception {
        String canString= "MrW-Test";
        SearchPattern sp = SearchPattern.parsePattern(canString);
        assertTrue(sp.isMatchCase());
        assertFalse(sp.isRegExp());
        assertTrue(sp.isWholeWords());
        assertEquals("Test", sp.getSearchExpression());
        assertEquals(MatchType.BASIC, sp.getMatchType());
    }

    @Test
    public void testManuallyParseRegexp() throws Exception {
        String canString = "MRW-Test";
        SearchPattern sp = SearchPattern.parsePattern(canString);
        assertEquals(MatchType.REGEXP, sp.getMatchType());
    }

    @Test
    public void testManuallyParseLiteral() throws Exception {
        String canString = "MLW-Test";
        SearchPattern sp = SearchPattern.parsePattern(canString);
        assertEquals(MatchType.LITERAL, sp.getMatchType());
    }

    @Test
    public void testManuallyCanString() throws Exception {
        SearchPattern sp = SearchPattern.create("ta", true, false, true);
        String canString = sp.toCanonicalString();
        
        assertEquals("mRW-ta", canString);

        sp = sp.changeMatchType(MatchType.BASIC);
        assertEquals("mrW-ta", sp.toCanonicalString());

        sp = sp.changeMatchType(MatchType.LITERAL);
        assertEquals("mLW-ta", sp.toCanonicalString());
    }

    @Test
    public void testMatchType() {

        assertFalse("Literal match type shoudn't be a regexp pattern",
                SearchPattern.create("test", false, false,
                MatchType.LITERAL).isRegExp());

        assertFalse("Basic match type shoudn't be a regexp pattern",
                SearchPattern.create("test", false, false,
                MatchType.BASIC).isRegExp());

        assertTrue("Regexp match type should be a regexp pattern",
                SearchPattern.create("test", false, false,
                MatchType.REGEXP).isRegExp());

        assertTrue("Chaning match type to REGEXP should create regexp pattern",
                SearchPattern.create("test", false, false,
                MatchType.LITERAL).changeMatchType(MatchType.REGEXP)
                .isRegExp());

        assertEquals("Changing match type to LITERAL should work",
                MatchType.LITERAL, SearchPattern.create("test",
                false, false, true).changeMatchType(MatchType.LITERAL)
                .getMatchType());

        assertEquals("Chaning match type to BASIC should work",
                MatchType.BASIC, SearchPattern.create("test",
                false, false, true).changeMatchType(MatchType.BASIC)
                .getMatchType());

        assertEquals("If not specified exactly, match type should be LITERAL",
                MatchType.LITERAL, SearchPattern.create("test",
                false, false, false).getMatchType());
    }
}
