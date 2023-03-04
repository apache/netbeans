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
package org.netbeans.modules.web.common.api;

import javax.swing.text.BadLocationException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class LinesTest extends NbTestCase {

    public LinesTest(String name) {
        super(name);
    }

    public void testBasic() throws BadLocationException {
        String source = "jedna\ndve\ntri";
        //               012345 6789 012

        Lines l = new Lines(source);

        assertEquals(3, l.getLinesCount());

        assertEquals(0, l.getLineIndex(0));
        assertEquals(0, l.getLineIndex(1));
        assertEquals(0, l.getLineIndex(2));
        assertEquals(0, l.getLineIndex(3));
        assertEquals(0, l.getLineIndex(4));
        assertEquals(0, l.getLineIndex(5));

        assertEquals(1, l.getLineIndex(6));
        assertEquals(1, l.getLineIndex(7));
        assertEquals(1, l.getLineIndex(8));
        assertEquals(1, l.getLineIndex(9));

        assertEquals(2, l.getLineIndex(10));
        assertEquals(2, l.getLineIndex(11));
        assertEquals(2, l.getLineIndex(12));

        assertEquals(0, l.getLineOffset(0));
        assertEquals(6, l.getLineOffset(1));
        assertEquals(10, l.getLineOffset(2));

    }

    public void testEmpty() throws BadLocationException {
        String source = "";
        Lines l = new Lines(source);
        assertEquals(1, l.getLinesCount());
        assertEquals(0, l.getLineIndex(0));
        assertEquals(0, l.getLineOffset(0));
    }

    public void testJustNL() throws BadLocationException {
        String source = "\n";
        Lines l = new Lines(source);
        assertEquals(1, l.getLinesCount());
        assertEquals(0, l.getLineIndex(0));
        assertEquals(0, l.getLineOffset(0));
    }

    public void testJustNLs() throws BadLocationException {
        String source = "\n\n\n";
        //               0 1 2

        Lines l = new Lines(source);
        assertEquals(3, l.getLinesCount());

        assertEquals(0, l.getLineIndex(0));
        assertEquals(1, l.getLineOffset(1));
        assertEquals(2, l.getLineOffset(2));

        assertEquals(0, l.getLineIndex(0));
        assertEquals(1, l.getLineIndex(1));
        assertEquals(2, l.getLineIndex(2));

    }

    public void testCharAfterNL() throws BadLocationException {
        String source = "\nA";
        //               0 1

        Lines l = new Lines(source);
        assertEquals(2, l.getLinesCount());

        assertEquals(0, l.getLineIndex(0));
        assertEquals(1, l.getLineOffset(1));

        assertEquals(0, l.getLineIndex(0));
        assertEquals(1, l.getLineIndex(1));

    }

    public void testCharBeforeNL() throws BadLocationException {
        String source = "A\n";
        //               01

        Lines l = new Lines(source);
        assertEquals(1, l.getLinesCount());

        assertEquals(0, l.getLineIndex(0));
        assertEquals(0, l.getLineIndex(0));

    }

    public void testPerformance() throws BadLocationException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("fkdhfgjdhsgjkfdhgjkfdhkjghpru2iordhsflkdshgjkdshfgkjdshgjkdshfkjlasdhfjkdshfkjdshfjkdsfhj\n");
        }
        
        System.out.println("text size = " + sb.length());
        Lines l = new Lines(sb);
        
        long a = System.currentTimeMillis();
        int lindex = -1;
        for(int i = 0; i < sb.length(); i+=100) {
            int lineIndex = l.getLineIndex(i);
            assertTrue(lindex != lineIndex); //cannot hit same line two times
            lindex = lineIndex;
        }
        long b = System.currentTimeMillis();
        
        System.out.println("took " + (b-a) + "ms.");
        
    }
}
