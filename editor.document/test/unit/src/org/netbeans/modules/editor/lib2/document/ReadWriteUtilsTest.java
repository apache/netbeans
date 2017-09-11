/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.document;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mmetelka
 */
public class ReadWriteUtilsTest extends NbTestCase {
    
    private static final String noLS = "test";
    private static final String endsLF = "test\n";
    private static final String endsCRLF = "test\r\n";
    private static final String endsCR = "test\r";
    private static final String startsLF = "\ntest";
    private static final String startsCRLF = "\r\ntest";
    private static final String startsCR = "\rtest";
    private static final String containsLF = "x\ny";
    private static final String containsCRLF = "x\r\ny";
    private static final String containsCR = "x\ry";
    
    public ReadWriteUtilsTest(String testName) {
        super(testName);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimpleUndo");
//        includes.add("testSimplePositionSharingMods");
//        includes.add("testEndPosition");
//        includes.add("testRandomMods");
//        includes.add("testRemoveAtZero");
//        includes.add("testBackwardBiasPositionsSimple");
//        includes.add("testBackwardBiasPositions");
//        includes.add("testRemoveSimple");
//        filterTests(includes);
    }
    
    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[includeTests.size()]));
        setFilter(filter);
    }

    @Override
    protected Level logLevel() {
//        return Level.FINEST;
//        return Level.FINE;
//        return Level.INFO;
        return null;
    }
    
    public void testReadReader() throws Exception {
        StringBuilder sb = new StringBuilder(500000);
        for (int i = 10000; i < 100000; i++) {
            sb.append(i);
        }
        String input = sb.toString();
        StringReader sr = new StringReader(input);
        ReadWriteBuffer buffer = ReadWriteUtils.read(sr);
        assertEquals(input, buffer.toString());
    }
    
    private static final void assertConvert(String input, String output, String firstLS) throws Exception {
        ReadWriteBuffer buffer = ReadWriteUtils.read(new StringReader(input));
        assertEquals(firstLS, ReadWriteUtils.findFirstLineSeparator(buffer));
        ReadWriteUtils.convertToNewlines(buffer);
        assertEquals(output, buffer.toString());
        assertEquals(output, ReadWriteUtils.convertToNewlines(input));
        assertEquals(input, ReadWriteUtils.convertFromNewlines(output, firstLS).toString());
    }

    public void testConvert() throws Exception {
        assertConvert(noLS, noLS, null);
        assertConvert(endsLF, endsLF, "\n");
        assertConvert(endsCRLF, endsLF, "\r\n");
        assertConvert(endsCR, endsLF, "\r");
        assertConvert(startsLF, startsLF, "\n");
        assertConvert(startsCRLF, startsLF, "\r\n");
        assertConvert(startsCR, startsLF, "\r");
        assertConvert(containsLF, containsLF, "\n");
        assertConvert(containsCRLF, containsLF, "\r\n");
        assertConvert(containsCR, containsLF, "\r");
        
        String convert = "12\n345\n678";
        ReadWriteBuffer buffer = ReadWriteUtils.convertFromNewlines(convert, 1, convert.length() - 2, "\r");
        assertEquals(convert.substring(1, convert.length() - 2).replace('\n', '\r'), buffer.toString());
        
        // Test realloc of extra space for extra "\r" when converting to "\r\n"
        convert = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
        buffer = ReadWriteUtils.convertFromNewlines(convert, 1, convert.length() - 2, "\r\n");
        assertEquals(convert.substring(1, convert.length() - 2).replace("\n", "\r\n"), buffer.toString());
        
        // Test missing realloc due to a bug
        convert = "a\n\n\n\n\n\n\n\n\n\n\n\n";
        buffer = ReadWriteUtils.convertFromNewlines(convert, 0, convert.length(), "\r\n");
        assertEquals(convert.replace("\n", "\r\n"), buffer.toString());
    }

}
