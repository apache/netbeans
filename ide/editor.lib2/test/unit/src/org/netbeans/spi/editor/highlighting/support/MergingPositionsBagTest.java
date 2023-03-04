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

package org.netbeans.spi.editor.highlighting.support;

import java.util.Enumeration;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.spi.editor.highlighting.*;
import org.netbeans.spi.editor.highlighting.performance.SimplePosition;

/**
 *
 * @author vita
 */
public class MergingPositionsBagTest extends NbTestCase {
    
    private static final AttributeSet EMPTY = SimpleAttributeSet.EMPTY;
    
    private Document doc = new DefaultStyledDocument();
    
    /** Creates a new instance of HighlightSequenceTest */
    public MergingPositionsBagTest(String name) {
        super(name);
    }

    public void testSimple() {
        PositionsBag hs = new PositionsBag(doc, true);
        assertEquals("Sequence should be empty", 0, hs.getMarks().size());
        
        hs.addHighlight(pos(10), pos(20), EMPTY);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        assertEquals("Sequence should not be empty", 2, marks.size());
        assertEquals("Wrong highlight's start offset", 10, marks.get(0).getOffset());
        assertEquals("Wrong highlight's end offset", 20, marks.get(1).getOffset());
        assertNull("Wrong highlight's end", attributes.get(1));
        
        hs.clear();
        assertEquals("Sequence was not cleared", 0, hs.getMarks().size());
    }

    public void testAddLeftOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(5), pos(15), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-B");
        
        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 15, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A", "set-B");

        assertEquals("3. highlight - wrong start offset", 15, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 20, marks.get(3).getOffset());
        assertAttribs("3. highlight - wrong attribs", attributes.get(2), "set-A");
        assertNull("3. highlight - wrong end", attributes.get(3));
    }

    public void testAddRightOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(15), pos(25), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A");
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A", "set-B");

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertAttribs("3. highlight - wrong attribs", attributes.get(2), "set-B");
        assertNull("3. highlight - wrong end", attributes.get(3));
    }

    public void testAddLeftMatchBiggerOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(10), pos(15), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A", "set-B");
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A");
        assertNull("2. highlight - wrong end", attributes.get(2));
    }

    public void testAddRightMatchBiggerOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(15), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A");
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A", "set-B");
        assertNull("2. highlight - wrong end", attributes.get(2));
    }

    public void testAddCompleteMatchOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A", "set-B");
        assertNull("1. highlight - wrong end", attributes.get(1));
    }

    public void testAddBiggerOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(5), pos(25), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-B");

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A", "set-B");

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertAttribs("3. highlight - wrong attribs", attributes.get(2), "set-B");
        assertNull("3. highlight - wrong end", attributes.get(3));
    }
    
    public void testAddLeftMatchSmallerOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(10), pos(15), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A", "set-B");
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A");
        assertNull("2. highlight - wrong end", attributes.get(2));
    }

    public void testAddRightMatchSmallerOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(15), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A");
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A", "set-B");
        assertNull("2. highlight - wrong end", attributes.get(2));
    }

    public void testAddSmallerOverlap() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-A", "attribsA");
        attribsB.addAttribute("set-B", "attribsB");
        
        hs.addHighlight(pos(5), pos(25), attribsA);
        hs.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertAttribs("1. highlight - wrong attribs", attributes.get(0), "set-A");

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertAttribs("2. highlight - wrong attribs", attributes.get(1), "set-A", "set-B");

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertAttribs("3. highlight - wrong attribs", attributes.get(0), "set-A");
        assertNull("3. highlight - wrong end", attributes.get(3));
    }

    public void testOrdering() {
        PositionsBag hs = new PositionsBag(doc, true);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("attribute", "value-A");
        attribsB.addAttribute("attribute", "value-B");
        
        hs.addHighlight(pos(5), pos(15), attribsA);
        hs.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();

        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong attribs", "value-A", attributes.get(0).getAttribute("attribute"));
        assertEquals("2. highlight - wrong attribs", "value-B", attributes.get(1).getAttribute("attribute"));
        assertEquals("3. highlight - wrong attribs", "value-B", attributes.get(2).getAttribute("attribute"));
        assertNull("3. highlight - wrong end", attributes.get(3));
    }
    
    public void testAddMultipleOverlaps() {
        Object [] src = new Object [] {
            5, 10, new String [] { "set-A" },
            15, 20, new String [] { "set-B" },
            25, 30, new String [] { "set-C" },
            0, 40, new String [] { "set-D" },
        };
        Object [] trg = new Object [] {
            0, 5, new String [] { "set-D" },
            5, 10, new String [] { "set-A", "set-D" },
            10, 15, new String [] { "set-D" },
            15, 20, new String [] { "set-B", "set-D" },
            20, 25, new String [] { "set-D" },
            25, 30, new String [] { "set-C", "set-D" },
            30, 40, new String [] { "set-D" },
        };
        
        checkMerging(src, trg, true);
    }

    public void testAddMultipleOverlaps2() {
        Object [] src = new Object [] {
            2, 47, new String [] { "set-A" },
            49, 74, new String [] { "set-B" },
            74, 100, new String [] { "set-C" },
            9, 48, new String [] { "set-D" },
            49, 98, new String [] { "set-E" },
            0, 44, new String [] { "set-F" },
            46, 74, new String [] { "set-G" },
            74, 100, new String [] { "set-H" },
        };
        Object [] trg = new Object [] {
            0, 2, new String [] { "set-F" },
            2, 9, new String [] { "set-A", "set-F" },
            9, 44, new String [] { "set-A", "set-D", "set-F" },
            44, 46, new String [] { "set-A", "set-D" },
            46, 47, new String [] { "set-A", "set-D", "set-G" },
            47, 48, new String [] { "set-D", "set-G" },
            48, 49, new String [] { "set-G" },
            49, 74, new String [] { "set-B", "set-E", "set-G" },
            74, 98, new String [] { "set-C", "set-E", "set-H" },
            98, 100, new String [] { "set-C", "set-H" },
        };
        
        checkMerging(src, trg, true);
    }
    
    public void testAddMultipleOverlaps3() {
        Object [] src = new Object [] {
            5, 13, new String [] { "set-A" },
            20, 49, new String [] { "set-B" },
            50, 53, new String [] { "set-C" },
            62, 100, new String [] { "set-D" },
            9, 54, new String [] { "set-E" },
            57, 100, new String [] { "set-F" },
            1, 41, new String [] { "set-G" },
            41, 46, new String [] { "set-H" },
            54, 83, new String [] { "set-I" },
            88, 100, new String [] { "set-J" },
        };
        Object [] trg = new Object [] {
            1, 5, new String [] { "set-G" },
            5, 9, new String [] { "set-A", "set-G" },
            9, 13, new String [] { "set-A", "set-E", "set-G" },
            13, 20, new String [] { "set-E", "set-G" },
            20, 41, new String [] { "set-B", "set-G", "set-E" },
            41, 46, new String [] { "set-B", "set-E", "set-H" },
            46, 49, new String [] { "set-B", "set-E" },
            49, 50, new String [] { "set-E" },
            50, 53, new String [] { "set-C", "set-E" },
            53, 54, new String [] { "set-E" },
            54, 57, new String [] { "set-I" },
            57, 62, new String [] { "set-F", "set-I" },
            62, 83, new String [] { "set-D", "set-F", "set-I" },
            83, 88, new String [] { "set-D", "set-F" },
            88, 100, new String [] { "set-D", "set-F", "set-J" },
        };
        
        checkMerging(src, trg, true);
    }

    public void testAddMultipleOverlaps4() {
        Object [] src = new Object [] {
            4, 51, new String [] { "set-A-0" },
            51, 63, new String [] { "set-A-1" },
            69, 75, new String [] { "set-A-2" },
            83, 85, new String [] { "set-A-3" },
            92, 100, new String [] { "set-A-4" },
            5, 17, new String [] { "set-B-0" },
            23, 37, new String [] { "set-B-1" },
            39, 63, new String [] { "set-B-2" },
            69, 100, new String [] { "set-B-3" },
            2, 28, new String [] { "set-C-0" },
            31, 64, new String [] { "set-C-1" },
            66, 93, new String [] { "set-C-2" },
            95, 100, new String [] { "set-C-3" },
        };
        Object [] trg = new Object [] {
            2, 28, new String [] { "set-C-0" },
            28, 31, new String [] { "set-B-1" },
            31, 64, new String [] { "set-C-1" },
            66, 93, new String [] { "set-C-2" },
            93, 95, new String [] { "set-B-3" },
            95, 100, new String [] { "set-C-3" },
        };
        
        checkMerging(src, trg, false);
    }
    
    public void checkMerging(Object [] src, Object [] trg, boolean merge) {
        PositionsBag hs = new PositionsBag(doc, merge);
        
        for (int i = 0; i < src.length / 3; i++) {
            SimpleAttributeSet as = new SimpleAttributeSet();
            String [] keys = (String []) src[3 * i + 2];
            
            for (int j = 0; j < keys.length; j++) {
                as.addAttribute(keys[j], Boolean.TRUE);
            }
            
            hs.addHighlight(
                pos(((Integer) src[3 * i + 0]).intValue()),
                pos(((Integer) src[3 * i + 1]).intValue()),
                as
            );
        }

        int lastOffset = Integer.MIN_VALUE;
        int differentOffsets = 0;
        
        for (int i = 0; i < trg.length / 3; i++) {
            if (lastOffset != ((Integer) trg[3 * i + 0]).intValue()) {
                differentOffsets++;
                lastOffset = ((Integer) trg[3 * i + 0]).intValue();
            }
            if (lastOffset != ((Integer) trg[3 * i + 1]).intValue()) {
                differentOffsets++;
                lastOffset = ((Integer) trg[3 * i + 1]).intValue();
            }
        }
        
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> attributes = hs.getAttributes();
        
        try {
            assertEquals("Wrong number of highlights", differentOffsets, marks.size());

            int trgIdx = 0;
            for (int idx = 0; idx < marks.size(); idx++) {
                if (attributes.get(idx) == null) {
                    assertTrue("Mark at index 0 must have attributes", idx > 0);
                    continue;
                }
                
                assertTrue("Too few marks", idx + 1 < marks.size());
                assertTrue("Too many marks", trgIdx < trg.length);
                
                // Compare one pair
                assertEquals(trgIdx + ". highlight - wrong start offset", 
                    ((Integer) trg[3 * trgIdx + 0]).intValue(), marks.get(idx).getOffset());
                assertEquals(trgIdx + ". highlight - wrong end offset", 
                    ((Integer) trg[3 * trgIdx + 1]).intValue(), marks.get(idx + 1).getOffset());
                assertAttribs(trgIdx + ". highlight - wrong attribs",
                    attributes.get(idx), (String []) trg[3 * trgIdx + 2]);
            
                trgIdx++;
            }
            
            assertTrue("Wrong number of marks: marks.size() = " + marks.size() + 
                       ", trg.length = " + trg.length, 3 * trgIdx == trg.length);
            
        } catch (AssertionFailedError afe) {
            dumpMarks(marks, attributes);

            System.out.println("Dump through getHighlights {");
            HighlightsSequence sequence = hs.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            for ( ; sequence.moveNext(); ) {
                System.out.println("   <" + sequence.getStartOffset() + ", " + sequence.getEndOffset() + ">");
            }
            System.out.println("} ---- End of Dump through getHighlights ------------------");
            
            throw afe;
        }
    }
    
    private void assertAttribs(String msg, AttributeSet as, String... keys) {
        assertEquals(msg, keys.length, as.getAttributeCount());
        for (String key : keys) {
            if (null == as.getAttribute(key)) {
                fail(msg + " attribute key: " + key);
            }
        }
    }

    private String dumpHighlight(Position start, Position end, AttributeSet attribs) {
        StringBuilder sb = new StringBuilder();

        sb.append("<");
        sb.append(start == null ? " " : start.getOffset());
        sb.append(",");
        sb.append(end == null ? " " : end.getOffset());
        sb.append(",");
        dumpAttributes(sb, attribs);
        sb.append(">");

        return sb.toString();
    }

    private String dumpAttributes(StringBuilder sb, AttributeSet attribs) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        
        if (attribs == null) {
            sb.append(" ");
        } else {
            Enumeration en = attribs.getAttributeNames();
            while (en.hasMoreElements()) {
                Object attrName = en.nextElement();
                Object attrValue = attribs.getAttribute(attrName);

                sb.append("'");
                sb.append(attrName.toString());
                sb.append("' = '");
                sb.append(attrValue == null ? "null" : attrValue.toString());
                sb.append("'");
                if (en.hasMoreElements()) {
                    sb.append(", ");
                }
            }
        }
        
        return sb.toString();
    }
    
    private void dumpMarks(GapList<Position> marks, GapList<AttributeSet> attributes) {
        String signature = marks.getClass() + "@" + Integer.toHexString(System.identityHashCode(marks));
        System.out.println("Dumping marks from " + signature + " {");
        for(int i = 0; i < marks.size(); i++) {
            System.out.println("<" + marks.get(i).getOffset() + ", [" + dumpAttributes(null, attributes.get(i)) + "]>");
        }
        System.out.println("} ---- End of Dumping marks from " + signature + " --------");
    }
    
    private Position pos(int offset) {
        return new SimplePosition(offset);
    }
}
