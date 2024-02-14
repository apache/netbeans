/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[0]));
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
