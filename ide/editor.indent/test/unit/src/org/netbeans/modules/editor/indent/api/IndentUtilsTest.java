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

package org.netbeans.modules.editor.indent.api;

import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.ArrayUtilities;

/**
 *
 * @author Miloslav Metelka
 */
public class IndentUtilsTest extends NbTestCase {
    
    private static final String MIME_TYPE = "text/x-test";

    public IndentUtilsTest(String name) {
        super(name);
    }

    public void testIndentUtils() throws Exception {
        // Test empty indent
        assertSame("", IndentUtils.createIndentString(0, true, 8));
        assertSame("", IndentUtils.createIndentString(0, false, 4));
        
        // Test <tabSize
        String s;
        assertEquals("   ", s = IndentUtils.createIndentString(3, false, 4));
        // Test caching of indent strings
        assertSame(s, IndentUtils.createIndentString(3, false, 4));
        
        // Test ==tabSize
        assertEquals("\t", s = IndentUtils.createIndentString(4, false, 4));
        assertSame(s, IndentUtils.createIndentString(4, false, 4));

        // Test >tabSize
        assertEquals("\t  ", s = IndentUtils.createIndentString(6, false, 4));
        assertSame(s, IndentUtils.createIndentString(6, false, 4));
        assertEquals("\t\t\t   ", s = IndentUtils.createIndentString(15, false, 4));
        assertSame(s, IndentUtils.createIndentString(15, false, 4));
        
        // Test spaces-only
        assertEquals("          ", s = IndentUtils.createIndentString(10, true, 4));
        assertEquals(s, IndentUtils.createIndentString(10, true, 4));
        
        // Test many (non-cached) spaces
        int testUncachedIndent = 90;
        StringBuilder sb = new StringBuilder(testUncachedIndent);
        ArrayUtilities.appendSpaces(sb, testUncachedIndent);
        assertEquals(sb.toString(), IndentUtils.createIndentString(testUncachedIndent, true, 4));
        
        // Test long (non-cached) tab indent
        int i = testUncachedIndent;
        sb.setLength(0);
        while (i >= 8) {
            sb.append('\t');
            i -= 8;
        }
        ArrayUtilities.appendSpaces(sb, i);
        assertEquals(sb.toString(), IndentUtils.createIndentString(testUncachedIndent, false, 8));
        
        // Test cache limits (#124352)
        assertEquals("\t   ", IndentUtils.createIndentString(11, false, 8));
    }

}
