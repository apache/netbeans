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
