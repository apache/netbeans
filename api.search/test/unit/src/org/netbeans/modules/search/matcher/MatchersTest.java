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
