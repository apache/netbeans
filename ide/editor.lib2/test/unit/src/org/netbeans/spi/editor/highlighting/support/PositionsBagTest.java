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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
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
public class PositionsBagTest extends NbTestCase {
    
    private static final AttributeSet EMPTY = SimpleAttributeSet.EMPTY;
    
    private Document doc = new DefaultStyledDocument();
    
    /** Creates a new instance of HighlightSequenceTest */
    public PositionsBagTest(String name) {
        super(name);
    }

    public void testSimple() {
        PositionsBag hs = new PositionsBag(doc);
        assertEquals("Sequence should be empty", 0, hs.getMarks().size());
        
        hs.addHighlight(pos(10), pos(20), EMPTY);
        GapList<Position> marks = hs.getMarks();
        assertEquals("Sequence should not be empty", 2, marks.size());
        assertEquals("Wrong highlight's start offset", 10, marks.get(0).getOffset());
        assertEquals("Wrong highlight's end offset", 20, marks.get(1).getOffset());
        
        hs.clear();
        assertEquals("Sequence was not cleared", 0, hs.getMarks().size());
    }

    public void testAddLeftOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(5), pos(15), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(1).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", atttributes.get(2));
    }
    
    public void testAddRightOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(15), pos(25), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));
        assertNull(  "2. highlight - wrong end", atttributes.get(2));
    }

    public void testAddCompleteOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(5), pos(25), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 25, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }
    
    public void testAddSplit() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(25), attribsA);
        hs.addHighlight(pos(15), pos(20), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }

    public void testAddAligned() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(20), pos(30), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", atttributes.get(2));
        
        hs.addHighlight(pos(0), pos(10), attribsB);
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }

    public void testAddAligned2() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(40), attribsA);
        hs.addHighlight(pos(10), pos(20), attribsB);
        hs.addHighlight(pos(30), pos(40), attribsB);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 30, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 40, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }

    public void testAddMiddle() {
        for(int i = 0; i < 10; i++) {
            addMiddle(i + 1);
        }
    }
    
    private void addMiddle(int middleMarks) {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(pos(10 * i + 10), pos(10 * i + 20), i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.addHighlight(pos(15), pos(middleMarks * 10 + 15), attribsC);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsC", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            atttributes.get(3));
    }
    
    public void testRemoveLeftOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(5, 15);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveLeftOverlapClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(5), pos(15), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 15, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveRightOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(15, 25);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveRightOverlapClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(15), pos(25), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveCompleteOverlap() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(5, 25);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveCompleteOverlapClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(5), pos(25), true);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplit() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(25), attribsA);
        hs.removeHighlights(15, 20);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplitClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(25), attribsA);
        hs.removeHighlights(pos(15), pos(20), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", atttributes.get(3));
    }

    public void testRemoveAlignedClip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(0), pos(10), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
        
        hs.removeHighlights(pos(20), pos(30), true);
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveAligned2Clip() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(40), attribsA);
        hs.removeHighlights(pos(10), pos(20), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 40, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
        
        hs.removeHighlights(pos(30), pos(40), true);
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 30, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", atttributes.get(1));
    }

    public void testRemoveMiddle() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.addHighlight(pos(20), pos(30), attribsB);
        hs.removeHighlights(15, 25);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }
    
    public void testRemoveMiddleEmptyHighlight() {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(pos(10), pos(20), attribsA);
        hs.removeHighlights(pos(15), pos(15), false);
        GapList<Position> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }
    
    public void testRemoveMiddleClip() {
        for(int i = 0; i < 10; i++) {
            removeMiddleClip(i + 1);
        }
    }

    private void removeMiddleClip(int middleMarks) {
        PositionsBag hs = new PositionsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(pos(10 * i + 10), pos(10 * i + 20), i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.removeHighlights(pos(15), pos(middleMarks * 10 + 15), true);
        GapList<Position> marks = hs.getMarks();
        GapList<AttributeSet> atttributes = hs.getAttributes();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", atttributes.get(0).getAttribute("set-name"));
        assertNull("  1. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            atttributes.get(1));

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", atttributes.get(2).getAttribute("set-name"));
        assertNull("  2. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            atttributes.get(3));
    }
    
    public void testRemoveHighlights_165270() throws Exception {
        PositionsBag hs = new PositionsBag(doc);
        hs.addHighlight(pos(5), pos(10), EMPTY);
        assertEquals("Sequence size", 2, hs.getMarks().size());
        hs.removeHighlights(pos(5), pos(10), true);
        assertEquals("Highlights were not removed", 0, hs.getMarks().size());

        hs.addHighlight(pos(10), pos(20), EMPTY);
        hs.addHighlight(pos(30), pos(40), EMPTY);
        hs.addHighlight(pos(50), pos(60), EMPTY);
        assertEquals("Sequence size", 6, hs.getMarks().size());
        hs.removeHighlights(pos(30), pos(40), true);
        assertEquals("Highlights were not removed", 4, hs.getMarks().size());
        assertMarks("Wrong highlights after remove", createPositionsBag(10, 20, EMPTY, 50, 60, EMPTY), hs);
    }

    public void testRemoveHighlights_114642() throws Exception {
        PositionsBag hs = new PositionsBag(doc);
        hs.addHighlight(pos(5), pos(10), EMPTY);
        assertEquals("Sequence size", 2, hs.getMarks().size());

        final PositionsBag expected = createPositionsBag(5, 10, EMPTY);
        hs.removeHighlights(pos(1), pos(5), true);
        assertMarks("Highlights should not be removed by <1, 5, true>", expected, hs);

        hs.removeHighlights(pos(10), pos(15), true);
        assertMarks("Highlights should not be removed <10, 15, true>", expected, hs);

        hs.removeHighlights(pos(1), pos(5), false);
        assertMarks("Highlights should not be removed <1, 5, false>", expected, hs);

        hs.removeHighlights(pos(10), pos(15), false);
        assertMarks("Highlights should not be removed <10, 15, false>", expected, hs);
    }

    public void testRemoveHighlights_114642_2() throws Exception {
        PositionsBag expected = createPositionsBag(10, 20, EMPTY, 30, 40, EMPTY, 50, 60, EMPTY);
        PositionsBag hs = createPositionsBag(10, 20, EMPTY, 30, 40, EMPTY, 50, 60, EMPTY);
        assertEquals("Sequence size", 6, hs.getMarks().size());

        hs.removeHighlights(pos(25), pos(30), true);
        assertMarks("Highlights should not be removed <25, 30, true>", expected, hs);

        hs.removeHighlights(pos(40), pos(45), true);
        assertMarks("Highlights should not be removed <40, 45, true>", expected, hs);

        hs.removeHighlights(pos(25), pos(30), false);
        assertMarks("Highlights should not be removed <25, 30, false>", expected, hs);

        hs.removeHighlights(pos(40), pos(45), false);
        assertMarks("Highlights should not be removed <40, 45, false>", expected, hs);
    }

    public void test158249_AddToRemoveFromLeft() {
        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(0), pos(1), EMPTY);
        assertMarks("Expecting 1 highlight", createPositionsBag(0, 1, EMPTY), bag);
        bag.addHighlight(pos(1), pos(2), EMPTY);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.addHighlight(pos(2), pos(3), EMPTY);
        assertMarks("Expecting 3 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.addHighlight(pos(3), pos(4), EMPTY);
        assertMarks("Expecting 4 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(pos(3), pos(4), false);
        assertMarks("Expecting 3 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.removeHighlights(pos(2), pos(3), false);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.removeHighlights(pos(1), pos(2), false);
        assertMarks("Expecting 1 highlights", createPositionsBag(0, 1, EMPTY), bag);
        bag.removeHighlights(pos(0), pos(1), false);
        assertMarks("Expecting no highlights", createPositionsBag(), bag);
    }

    public void test158249_AddToRemoveFromLeftClip() {
        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(0), pos(1), EMPTY);
        assertMarks("Expecting 1 highlight", createPositionsBag(0, 1, EMPTY), bag);
        bag.addHighlight(pos(1), pos(2), EMPTY);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.addHighlight(pos(2), pos(3), EMPTY);
        assertMarks("Expecting 3 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.addHighlight(pos(3), pos(4), EMPTY);
        assertMarks("Expecting 4 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(pos(3), pos(4), true);
        assertMarks("Expecting 3 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.removeHighlights(pos(2), pos(3), true);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.removeHighlights(pos(1), pos(2), true);
        assertMarks("Expecting 1 highlights", createPositionsBag(0, 1, EMPTY), bag);
        bag.removeHighlights(pos(0), pos(1), true);
        assertMarks("Expecting no highlights", createPositionsBag(), bag);
    }

    public void test158249_AddToRemoveFromRight() {
        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(3), pos(4), EMPTY);
        assertMarks("Expecting 1 highlight", createPositionsBag(3, 4, EMPTY), bag);
        bag.addHighlight(pos(2), pos(3), EMPTY);
        assertMarks("Expecting 2 highlights", createPositionsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(pos(1), pos(2), EMPTY);
        assertMarks("Expecting 3 highlights", createPositionsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(pos(0), pos(1), EMPTY);
        assertMarks("Expecting 4 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(pos(0), pos(1), false);
        assertMarks("Expecting 3 highlights", createPositionsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(pos(1), pos(2), false);
        assertMarks("Expecting 2 highlights", createPositionsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(pos(2), pos(3), false);
        assertMarks("Expecting 1 highlights", createPositionsBag(3, 4, EMPTY), bag);
        bag.removeHighlights(pos(3), pos(4), false);
        assertMarks("Expecting no highlights", createPositionsBag(), bag);
    }

    public void test158249_AddToRemoveFromRightClip() {
        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(3), pos(4), EMPTY);
        assertMarks("Expecting 1 highlight", createPositionsBag(3, 4, EMPTY), bag);
        bag.addHighlight(pos(2), pos(3), EMPTY);
        assertMarks("Expecting 2 highlights", createPositionsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(pos(1), pos(2), EMPTY);
        assertMarks("Expecting 3 highlights", createPositionsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(pos(0), pos(1), EMPTY);
        assertMarks("Expecting 4 highlights", createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(pos(0), pos(1), true);
        assertMarks("Expecting 3 highlights", createPositionsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(pos(1), pos(2), true);
        assertMarks("Expecting 2 highlights", createPositionsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(pos(2), pos(3), true);
        assertMarks("Expecting 1 highlights", createPositionsBag(3, 4, EMPTY), bag);
        bag.removeHighlights(pos(3), pos(4), true);
        assertMarks("Expecting no highlights", createPositionsBag(), bag);
    }

    public void test158249_AddRemove() {
        PositionsBag bag = createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY);
        bag.removeHighlights(pos(1), pos(3), false);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 1, EMPTY, 3, 4, EMPTY), bag);

        bag = createPositionsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(pos(15), pos(25), false);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 10, EMPTY, 30, 40, EMPTY), bag);

        bag = createPositionsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(pos(5), pos(35), false);
        assertMarks("Expecting no highlights", createPositionsBag(), bag);
    }

    public void test158249_AddRemoveClip() {
        PositionsBag bag = createPositionsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY);
        bag.removeHighlights(pos(1), pos(3), true);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 1, EMPTY, 3, 4, EMPTY), bag);

        bag = createPositionsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(pos(15), pos(25), true);
        assertMarks("Expecting 4 highlights", createPositionsBag(0, 10, EMPTY, 10, 15, EMPTY, 25, 30, EMPTY, 30, 40, EMPTY), bag);

        bag = createPositionsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(pos(5), pos(35), true);
        assertMarks("Expecting 2 highlights", createPositionsBag(0, 5, EMPTY, 35, 40, EMPTY), bag);
    }

    public void testAddAll() {
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(pos(0), pos(30), attribsA);
        hsA.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marksA = hsA.getMarks();
        GapList<AttributeSet> atttributesA = hsA.getAttributes();
        
        hsB.addHighlight(pos(0), pos(40), attribsC);
        hsB.addAllHighlights(hsA);
        GapList<Position> marksB = hsB.getMarks();
        GapList<AttributeSet> atttributesB = hsB.getAttributes();
        
        assertEquals("Wrong number of highlights", marksA.size() + 1, marksB.size());
        for (int i = 0; i < marksA.size() - 1; i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i + 1).getOffset(), marksB.get(i + 1).getOffset());
            assertEquals(i + ". highlight - wrong attribs",
                atttributesA.get(i).getAttribute("set-name"),
                atttributesB.get(i).getAttribute("set-name"));
        }

        assertEquals("4. highlight - wrong start offset", 30, marksB.get(3).getOffset());
        assertEquals("4. highlight - wrong end offset", 40, marksB.get(4).getOffset());
        assertEquals("4. highlight - wrong attribs", "attribsC", atttributesB.get(3).getAttribute("set-name"));
    }

    public void testSet() {
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(pos(0), pos(30), attribsA);
        hsA.addHighlight(pos(10), pos(20), attribsB);
        GapList<Position> marksA = hsA.getMarks();
        GapList<AttributeSet> atttributesA = hsA.getAttributes();
        
        hsB.addHighlight(pos(0), pos(40), attribsC);
        hsB.setHighlights(hsA);
        GapList<Position> marksB = hsB.getMarks();
        GapList<AttributeSet> atttributesB = hsB.getAttributes();
        
        assertEquals("Wrong number of highlights", marksA.size(), marksB.size());
        for (int i = 0; i < marksA.size(); i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            
            AttributeSet attrA = atttributesA.get(i);
            AttributeSet attrB = atttributesB.get(i);
            
            if (attrA != null && attrB != null) {
                assertEquals(i + ". highlight - wrong attribs",
                    attrA.getAttribute("set-name"), 
                    attrB.getAttribute("set-name"));
            } else {
                assertTrue(i + ". highlight - wrong attribs", attrA == null && attrB == null);
            }
        }
    }
    
    public void testGetHighlights() {
        PositionsBag hs = new PositionsBag(doc);
        assertFalse("Sequence should be empty", hs.getHighlights(
            Integer.MIN_VALUE, Integer.MAX_VALUE).moveNext());
        
        hs.addHighlight(pos(10), pos(30), EMPTY);

        {
            // Do not clip the highlights
            HighlightsSequence highlights = hs.getHighlights(20, 25);
            assertTrue("Sequence should not be empty", highlights.moveNext());
            assertEquals("Wrong highlight's start offset", 20, highlights.getStartOffset());
            assertEquals("Wrong highlight's end offset", 25, highlights.getEndOffset());
            assertFalse("There should be no more highlights", highlights.moveNext());
        }
        
        hs.clear();
        assertFalse("Sequence was not cleared", hs.getHighlights(
            Integer.MIN_VALUE, Integer.MAX_VALUE).moveNext());
    }

    public void testGetHighlights2() {
        PositionsBag hb = new PositionsBag(doc);
        hb.addHighlight(pos(10), pos(20), SimpleAttributeSet.EMPTY);
        
        HighlightsSequence hs = hb.getHighlights(0, 5);
        assertFalse("HighlightsSequence should be empty", hs.moveNext());
        
        hs = hb.getHighlights(25, 30);
        assertFalse("HighlightsSequence should be empty", hs.moveNext());
        
        hs = hb.getHighlights(0, 15);
        assertTrue("HighlightsSequence should not be empty", hs.moveNext());
        assertFalse("Too many highlights in the sequence", hs.moveNext());

        hs = hb.getHighlights(12, 22);
        assertTrue("HighlightsSequence should not be empty", hs.moveNext());
        assertFalse("Too many highlights in the sequence", hs.moveNext());
        
        hs = hb.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertTrue("HighlightsSequence should not be empty", hs.moveNext());
        assertFalse("Too many highlights in the sequence", hs.moveNext());
    }
    
    public void testConcurrentModification() {
        PositionsBag hb = new PositionsBag(doc);

        // Modify the bag
        hb.addHighlight(pos(5), pos(10), EMPTY);
        hb.addHighlight(pos(15), pos(20), EMPTY);
        hb.addHighlight(pos(25), pos(30), EMPTY);
        
        HighlightsSequence hs = hb.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertTrue("There should be some highlights", hs.moveNext());
        
        int s = hs.getStartOffset();
        int e = hs.getEndOffset();
        AttributeSet a = hs.getAttributes();
        
        // Modification after the sequence was acquired
        hb.addHighlight(pos(100), pos(110), EMPTY);
        
        assertEquals("Wrong highlight start", s, hs.getStartOffset());
        assertEquals("Wrong highlight end", e, hs.getEndOffset());
        assertEquals("Wrong highlight attributes", a, hs.getAttributes());
        assertFalse("There should be no more highlights after co-modification", hs.moveNext());
    }

    public void testDocumentChanges() throws BadLocationException {
        Document d = new PlainDocument();
        d.insertString(0, "01234567890123456789012345678901234567890123456789", SimpleAttributeSet.EMPTY);
        
        PositionsBag bag = new PositionsBag(d);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        bag.addHighlight(d.createPosition(0), d.createPosition(30), attribsA);
        bag.addHighlight(d.createPosition(10), d.createPosition(20), attribsB);
        GapList<Position> marks = bag.getMarks();
        GapList<AttributeSet> atttributes = bag.getAttributes();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
        
        d.insertString(12, "----", SimpleAttributeSet.EMPTY);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 24, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 24, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 34, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
        
        d.remove(1, 5);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 5, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", atttributes.get(0).getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 5, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 19, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", atttributes.get(1).getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 19, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 29, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", atttributes.get(2).getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", atttributes.get(3));
    }
    
    public void test122663_AddLeftMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(5), pos(10), attribsA);
        bag.addHighlight(pos(5), pos(15), attribsB);

        assertMarks("Wrong highlights", createPositionsBag(5, 15, attribsB), bag);
    }

    public void test122663_AddMultipleLeftMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(5), pos(10), attribsA);
        bag.addHighlight(pos(15), pos(20), attribsA);
        bag.addHighlight(pos(25), pos(30), attribsA);
        bag.addHighlight(pos(5), pos(35), attribsB);

        assertMarks("Wrong highlights", createPositionsBag(5, 35, attribsB), bag);
    }

    public void test122663_AddRightMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(10), pos(15), attribsA);
        bag.addHighlight(pos(5), pos(15), attribsB);
        assertMarks("Wrong highlights", createPositionsBag(5, 15, attribsB), bag);
    }

    public void test122663_AddMultipleRightMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(30), pos(35), attribsA);
        bag.addHighlight(pos(20), pos(25), attribsA);
        bag.addHighlight(pos(10), pos(15), attribsA);
        bag.addHighlight(pos(5), pos(35), attribsB);
        assertMarks("Wrong highlights", createPositionsBag(5, 35, attribsB), bag);
    }

    public void test122663_AddBothMatch() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(10), pos(15), attribsA);
        bag.addHighlight(pos(10), pos(15), attribsB);
        assertMarks("Wrong highlights", createPositionsBag(10, 15, attribsB), bag);
    }

    public void test122663_AddMultipleBothMatch() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        PositionsBag bag = new PositionsBag(doc);
        bag.addHighlight(pos(10), pos(15), attribsA);
        bag.addHighlight(pos(20), pos(25), attribsA);
        bag.addHighlight(pos(30), pos(35), attribsA);
        bag.addHighlight(pos(10), pos(35), attribsB);
        assertMarks("Wrong highlights", createPositionsBag(10, 35, attribsB), bag);
    }

    private void dumpHighlights(HighlightsSequence seq) {
        System.out.println("Dumping highlights from: " + seq + "{");
        while(seq.moveNext()) {
            System.out.println("<" + seq.getStartOffset() + ", " + seq.getEndOffset() + ", " + seq.getAttributes() + ">");
        }
        System.out.println("} --- End of Dumping highlights from: " + seq + " ---------------------");
    }
    
    private Position pos(int offset) {
        return new SimplePosition(offset);
    }

    private PositionsBag createPositionsBag(Object... triples) {
        assert triples != null;
        assert triples.length % 3 == 0;

        PositionsBag bag = new PositionsBag(doc);
        for(int i = 0; i < triples.length / 3; i++) {
            bag.addHighlight(pos((Integer) triples[3 * i]), pos((Integer) triples[3 * i + 1]), (AttributeSet) triples[3 * i + 2]);
        }

        return bag;
    }

    private void assertMarks(String message, PositionsBag expected, PositionsBag actual) {
        try {
            GapList<Position> expectedPositions = expected.getMarks();
            GapList<Position> actualPositions = actual.getMarks();
            GapList<AttributeSet> expectedAttributes = expected.getAttributes();
            GapList<AttributeSet> actualAttributes = actual.getAttributes();
            assertEquals("Different number of marks", expectedPositions.size(), actualPositions.size());
            for(int i = 0; i < expectedPositions.size(); i++) {
                Position expectedPos = expectedPositions.get(i);
                Position actualPos = actualPositions.get(i);
                assertEquals("Different offset at the " + i + "-th mark", expectedPos.getOffset(), actualPos.getOffset());

                AttributeSet expectedAttrib = expectedAttributes.get(i);
                AttributeSet actualAttrib = actualAttributes.get(i);
                assertSame("Different attributes at the " + i + "-th mark", expectedAttrib, actualAttrib);
            }
        } catch (AssertionFailedError afe) {
            StringBuilder sb = new StringBuilder();
            sb.append(message);
            sb.append('\n');
            sb.append("Expected marks (size=");
            sb.append(expected.getMarks().size());
            sb.append("):\n");
            dumpMarks(expected, sb);
            sb.append('\n');
            sb.append("Actual marks (size=");
            sb.append(actual.getMarks().size());
            sb.append("):\n");
            dumpMarks(actual, sb);
            sb.append('\n');

            AssertionFailedError afe2 = new AssertionFailedError(sb.toString());
            afe2.initCause(afe);
            throw afe2;
        }
    }

    private StringBuilder dumpMarks(PositionsBag bag, StringBuilder sb) {
        for (int i = 0; i < bag.getMarks().size(); i++) {
            Position pos = bag.getMarks().get(i);
            sb.append('[').append(i).append("] = {");
            sb.append(pos.toString());
            sb.append("; attribs=");
            AttributeSet attribs = bag.getAttributes().get(i);
            sb.append(attribs);
            sb.append('}');
            if (i + 1 < bag.getMarks().size()) {
                sb.append('\n');
            }
        }
        return sb;
    }
}
