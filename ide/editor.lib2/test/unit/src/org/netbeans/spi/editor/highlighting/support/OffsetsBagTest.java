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
import javax.swing.text.SimpleAttributeSet;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.lib2.highlighting.OffsetGapList;
import org.netbeans.spi.editor.highlighting.*;

/**
 *
 * @author vita
 */
public class OffsetsBagTest extends NbTestCase {
    
    private static final AttributeSet EMPTY = SimpleAttributeSet.EMPTY;
    
    private Document doc = new DefaultStyledDocument();
    
    /** Creates a new instance of HighlightSequenceTest */
    public OffsetsBagTest(String name) {
        super(name);
    }

    public void testSimple() {
        OffsetsBag hs = new OffsetsBag(doc);
        assertEquals("Sequence should be empty", 0, hs.getMarks().size());
        
        hs.addHighlight(10, 20, EMPTY);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        assertEquals("Sequence should not be empty", 2, marks.size());
        assertEquals("Wrong highlight's start offset", 10, marks.get(0).getOffset());
        assertEquals("Wrong highlight's end offset", 20, marks.get(1).getOffset());
        
        hs.clear();
        assertEquals("Sequence was not cleared", 0, hs.getMarks().size());
    }

    public void testRemoveBeyondEnd() throws Exception {
        Document d = new PlainDocument();
        d.insertString(0, "0123456789", null);
        OffsetsBag hs = new OffsetsBag(d);
        hs.addHighlight(0, 5, EMPTY);
        hs.addHighlight(6, 8, EMPTY);
        assertEquals("Sequence size", 4, hs.getMarks().size());
        hs.removeHighlights(0, 11, false);
        assertEquals("Highlights were not removed", 0, hs.getMarks().size());
    }

    public void testAddLeftOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(5, 15, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(1).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", marks.get(2).getAttributes());
    }
    
    public void testAddRightOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(15, 25, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        
        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));
        assertNull(  "2. highlight - wrong end", marks.get(2).getAttributes());
    }

    public void testAddCompleteOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(5, 25, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 5, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 25, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }
    
    public void testAddSplit() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 25, attribsA);
        hs.addHighlight(15, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testAddAligned() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(20, 30, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 3, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", marks.get(2).getAttributes());
        
        hs.addHighlight(0, 10, attribsB);
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testAddAligned2() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 40, attribsA);
        hs.addHighlight(10, 20, attribsB);
        hs.addHighlight(30, 40, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsB", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 20, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 30, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 30, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 40, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testAddMiddle() {
        for(int i = 0; i < 10; i++) {
            addMiddle(i + 1);
        }
    }
    
    private void addMiddle(int middleMarks) {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(10 * i + 10, 10 * i + 20, i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.addHighlight(15, middleMarks * 10 + 15, attribsC);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsC", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            marks.get(3).getAttributes());
    }
    
    public void testRemoveLeftOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 15, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveLeftOverlapClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 15, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 15, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveRightOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(15, 25, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveRightOverlapClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(15, 25, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveCompleteOverlap() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 25, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveCompleteOverlapClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(5, 25, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplit() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 25, attribsA);
        hs.removeHighlights(15, 20, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveSplitClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 25, attribsA);
        hs.removeHighlights(15, 20, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());

        assertEquals("2. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset", 25, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end", marks.get(3).getAttributes());
    }

    public void testRemoveAlignedClip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(0, 10, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
        
        hs.removeHighlights(20, 30, true);
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 20, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveAligned2Clip() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 40, attribsA);
        hs.removeHighlights(10, 20, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 40, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
        
        hs.removeHighlights(30, 40, true);
        
        assertEquals("Wrong number of highlights", 2, marks.size());
        assertEquals("1. highlight - wrong start offset", 20, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 30, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end", marks.get(1).getAttributes());
    }

    public void testRemoveMiddle() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        hs.addHighlight(10, 20, attribsA);
        hs.addHighlight(20, 30, attribsB);
        hs.removeHighlights(15, 25, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }

    public void testRemoveMiddleEmptyHighlight() {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        
        hs.addHighlight(10, 20, attribsA);
        hs.removeHighlights(15, 15, false);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights", 0, marks.size());
    }
    
    public void testRemoveMiddleClip() {
        for(int i = 0; i < 10; i++) {
            removeMiddleClip(i + 1);
        }
    }

    private void removeMiddleClip(int middleMarks) {
        OffsetsBag hs = new OffsetsBag(doc);
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        for (int i = 0; i < middleMarks + 1; i++) {
            hs.addHighlight(10 * i + 10, 10 * i + 20, i % 2 == 0 ? attribsA : attribsB);
        }
        
        hs.removeHighlights(15, middleMarks * 10 + 15, true);
        OffsetGapList<OffsetsBag.Mark> marks = hs.getMarks();
        
        assertEquals("Wrong number of highlights (middleMarks = " + middleMarks + ")", 
            4, marks.size());
        assertEquals("1. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            10, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            15, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));
        assertNull("  1. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            marks.get(1).getAttributes());

        assertEquals("2. highlight - wrong start offset (middleMarks = " + middleMarks + ")", 
            middleMarks * 10 + 15, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong end offset (middleMarks = " + middleMarks + ")", 
            (middleMarks + 2) * 10, marks.get(3).getOffset());
        assertEquals("2. highlight - wrong attribs (middleMarks = " + middleMarks + ")", 
            middleMarks % 2 == 0 ? "attribsA" : "attribsB", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  2. highlight - wrong end (middleMarks = " + middleMarks + ")", 
            marks.get(3).getAttributes());
    }
    
    public void testRemoveHighlights_165270() throws Exception {
        OffsetsBag hs = new OffsetsBag(doc);
        hs.addHighlight(5, 10, EMPTY);
        assertEquals("Sequence size", 2, hs.getMarks().size());
        hs.removeHighlights(5, 10, true);
        assertEquals("Highlights were not removed", 0, hs.getMarks().size());

        hs.addHighlight(10, 20, EMPTY);
        hs.addHighlight(30, 40, EMPTY);
        hs.addHighlight(50, 60, EMPTY);
        assertEquals("Sequence size", 6, hs.getMarks().size());
        hs.removeHighlights(30, 40, true);
        assertEquals("Highlights were not removed", 4, hs.getMarks().size());
        assertMarks("Wrong highlights after remove", createOffsetsBag(10, 20, EMPTY, 50, 60, EMPTY), hs);
    }

    public void testRemoveHighlights_114642() throws Exception {
        OffsetsBag hs = new OffsetsBag(doc);
        hs.addHighlight(5, 10, EMPTY);
        assertEquals("Sequence size", 2, hs.getMarks().size());

        final OffsetsBag expected = createOffsetsBag(5, 10, EMPTY);
        hs.removeHighlights(1, 5, true);
        assertMarks("Highlights should not be removed by <1, 5, true>", expected, hs);

        hs.removeHighlights(10, 15, true);
        assertMarks("Highlights should not be removed <10, 15, true>", expected, hs);

        hs.removeHighlights(1, 5, false);
        assertMarks("Highlights should not be removed <1, 5, false>", expected, hs);

        hs.removeHighlights(10, 15, false);
        assertMarks("Highlights should not be removed <10, 15, false>", expected, hs);
    }

    public void testRemoveHighlights_114642_2() throws Exception {
        OffsetsBag expected = createOffsetsBag(10, 20, EMPTY, 30, 40, EMPTY, 50, 60, EMPTY);
        OffsetsBag hs = createOffsetsBag(10, 20, EMPTY, 30, 40, EMPTY, 50, 60, EMPTY);
        assertEquals("Sequence size", 6, hs.getMarks().size());

        hs.removeHighlights(25, 30, true);
        assertMarks("Highlights should not be removed <25, 30, true>", expected, hs);

        hs.removeHighlights(40, 45, true);
        assertMarks("Highlights should not be removed <40, 45, true>", expected, hs);

        hs.removeHighlights(25, 30, false);
        assertMarks("Highlights should not be removed <25, 30, false>", expected, hs);

        hs.removeHighlights(40, 45, false);
        assertMarks("Highlights should not be removed <40, 45, false>", expected, hs);
    }

    public void test158249_AddToRemoveFromLeft() {
        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(0, 1, EMPTY);
        assertMarks("Expecting 1 highlight", createOffsetsBag(0, 1, EMPTY), bag);
        bag.addHighlight(1, 2, EMPTY);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.addHighlight(2, 3, EMPTY);
        assertMarks("Expecting 3 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.addHighlight(3, 4, EMPTY);
        assertMarks("Expecting 4 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(3, 4, false);
        assertMarks("Expecting 3 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.removeHighlights(2, 3, false);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.removeHighlights(1, 2, false);
        assertMarks("Expecting 1 highlights", createOffsetsBag(0, 1, EMPTY), bag);
        bag.removeHighlights(0, 1, false);
        assertMarks("Expecting no highlights", createOffsetsBag(), bag);
    }

    public void test158249_AddToRemoveFromLeftClip() {
        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(0, 1, EMPTY);
        assertMarks("Expecting 1 highlight", createOffsetsBag(0, 1, EMPTY), bag);
        bag.addHighlight(1, 2, EMPTY);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.addHighlight(2, 3, EMPTY);
        assertMarks("Expecting 3 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.addHighlight(3, 4, EMPTY);
        assertMarks("Expecting 4 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(3, 4, true);
        assertMarks("Expecting 3 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY), bag);
        bag.removeHighlights(2, 3, true);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY), bag);
        bag.removeHighlights(1, 2, true);
        assertMarks("Expecting 1 highlights", createOffsetsBag(0, 1, EMPTY), bag);
        bag.removeHighlights(0, 1, true);
        assertMarks("Expecting no highlights", createOffsetsBag(), bag);
    }

    public void test158249_AddToRemoveFromRight() {
        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(3, 4, EMPTY);
        assertMarks("Expecting 1 highlight", createOffsetsBag(3, 4, EMPTY), bag);
        bag.addHighlight(2, 3, EMPTY);
        assertMarks("Expecting 2 highlights", createOffsetsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(1, 2, EMPTY);
        assertMarks("Expecting 3 highlights", createOffsetsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(0, 1, EMPTY);
        assertMarks("Expecting 4 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(0, 1, false);
        assertMarks("Expecting 3 highlights", createOffsetsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(1, 2, false);
        assertMarks("Expecting 2 highlights", createOffsetsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(2, 3, false);
        assertMarks("Expecting 1 highlights", createOffsetsBag(3, 4, EMPTY), bag);
        bag.removeHighlights(3, 4, false);
        assertMarks("Expecting no highlights", createOffsetsBag(), bag);
    }

    public void test158249_AddToRemoveFromRightClip() {
        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(3, 4, EMPTY);
        assertMarks("Expecting 1 highlight", createOffsetsBag(3, 4, EMPTY), bag);
        bag.addHighlight(2, 3, EMPTY);
        assertMarks("Expecting 2 highlights", createOffsetsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(1, 2, EMPTY);
        assertMarks("Expecting 3 highlights", createOffsetsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.addHighlight(0, 1, EMPTY);
        assertMarks("Expecting 4 highlights", createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);

        bag.removeHighlights(0, 1, true);
        assertMarks("Expecting 3 highlights", createOffsetsBag(1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(1, 2, true);
        assertMarks("Expecting 2 highlights", createOffsetsBag(2, 3, EMPTY, 3, 4, EMPTY), bag);
        bag.removeHighlights(2, 3, true);
        assertMarks("Expecting 1 highlights", createOffsetsBag(3, 4, EMPTY), bag);
        bag.removeHighlights(3, 4, true);
        assertMarks("Expecting no highlights", createOffsetsBag(), bag);
    }

    public void test158249_AddRemove() {
        OffsetsBag bag = createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY);
        bag.removeHighlights(1, 3, false);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 1, EMPTY, 3, 4, EMPTY), bag);

        bag = createOffsetsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(15, 25, false);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 10, EMPTY, 30, 40, EMPTY), bag);

        bag = createOffsetsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(5, 35, false);
        assertMarks("Expecting no highlights", createOffsetsBag(), bag);
    }

    public void test158249_AddRemoveClip() {
        OffsetsBag bag = createOffsetsBag(0, 1, EMPTY, 1, 2, EMPTY, 2, 3, EMPTY, 3, 4, EMPTY);
        bag.removeHighlights(1, 3, true);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 1, EMPTY, 3, 4, EMPTY), bag);

        bag = createOffsetsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(15, 25, true);
        assertMarks("Expecting 4 highlights", createOffsetsBag(0, 10, EMPTY, 10, 15, EMPTY, 25, 30, EMPTY, 30, 40, EMPTY), bag);

        bag = createOffsetsBag(0, 10, EMPTY, 10, 20, EMPTY, 20, 30, EMPTY, 30, 40, EMPTY);
        bag.removeHighlights(5, 35, true);
        assertMarks("Expecting 2 highlights", createOffsetsBag(0, 5, EMPTY, 35, 40, EMPTY), bag);
    }

    public void testAddAll() {
        OffsetsBag hsA = new OffsetsBag(doc);
        OffsetsBag hsB = new OffsetsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(0, 30, attribsA);
        hsA.addHighlight(10, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marksA = hsA.getMarks();
        
        hsB.addHighlight(0, 40, attribsC);
        hsB.addAllHighlights(hsA.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE));
        OffsetGapList<OffsetsBag.Mark> marksB = hsB.getMarks();
        
        assertEquals("Wrong number of highlights", marksA.size() + 1, marksB.size());
        for (int i = 0; i < marksA.size() - 1; i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i + 1).getOffset(), marksB.get(i + 1).getOffset());
            assertEquals(i + ". highlight - wrong attribs",
                marksA.get(i).getAttributes().getAttribute("set-name"),
                marksB.get(i).getAttributes().getAttribute("set-name"));
        }

        assertEquals("4. highlight - wrong start offset", 30, marksB.get(3).getOffset());
        assertEquals("4. highlight - wrong end offset", 40, marksB.get(4).getOffset());
        assertEquals("4. highlight - wrong attribs", "attribsC", marksB.get(3).getAttributes().getAttribute("set-name"));
    }

    public void testSet() {
        OffsetsBag hsA = new OffsetsBag(doc);
        OffsetsBag hsB = new OffsetsBag(doc);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        attribsC.addAttribute("set-name", "attribsC");
        
        hsA.addHighlight(0, 30, attribsA);
        hsA.addHighlight(10, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marksA = hsA.getMarks();
        
        hsB.addHighlight(0, 40, attribsC);
        hsB.setHighlights(hsA.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE));
        OffsetGapList<OffsetsBag.Mark> marksB = hsB.getMarks();
        
        assertEquals("Wrong number of highlights", marksA.size(), marksB.size());
        for (int i = 0; i < marksA.size(); i++) {
            assertEquals(i + ". highlight - wrong start offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            assertEquals(i + ". highlight - wrong end offset", 
                marksA.get(i).getOffset(), marksB.get(i).getOffset());
            
            AttributeSet attrA = marksA.get(i).getAttributes();
            AttributeSet attrB = marksB.get(i).getAttributes();
            
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
        OffsetsBag hs = new OffsetsBag(doc);
        assertFalse("Sequence should be empty", hs.getHighlights(
            Integer.MIN_VALUE, Integer.MAX_VALUE).moveNext());
        
        hs.addHighlight(10, 30, EMPTY);

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
        OffsetsBag hb = new OffsetsBag(doc);
        hb.addHighlight(10, 20, SimpleAttributeSet.EMPTY);
        
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
        OffsetsBag hb = new OffsetsBag(doc);

        // Modify the bag
        hb.addHighlight(5, 10, EMPTY);
        hb.addHighlight(15, 20, EMPTY);
        hb.addHighlight(25, 30, EMPTY);
        
        HighlightsSequence hs = hb.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertTrue("There should be some highlights", hs.moveNext());
        
        int s = hs.getStartOffset();
        int e = hs.getEndOffset();
        AttributeSet a = hs.getAttributes();
        
        // Modification after the sequence was acquired
        hb.addHighlight(100, 110, EMPTY);
        
        assertEquals("Wrong highlight start", s, hs.getStartOffset());
        assertEquals("Wrong highlight end", e, hs.getEndOffset());
        assertEquals("Wrong highlight attributes", a, hs.getAttributes());
        assertFalse("There should be no more highlights after co-modification", hs.moveNext());
    }

    public void testDocumentChanges() throws BadLocationException {
        Document d = new PlainDocument();
        d.insertString(0, "01234567890123456789012345678901234567890123456789", SimpleAttributeSet.EMPTY);
        
        OffsetsBag bag = new OffsetsBag(d);
        
        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();

        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");
        
        bag.addHighlight(0, 30, attribsA);
        bag.addHighlight(10, 20, attribsB);
        OffsetGapList<OffsetsBag.Mark> marks = bag.getMarks();
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 20, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 20, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 30, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
        
        d.insertString(12, "----", SimpleAttributeSet.EMPTY);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 10, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 10, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 24, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 24, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 34, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
        
        d.remove(1, 5);
        
        assertEquals("Wrong number of highlights", 4, marks.size());
        assertEquals("1. highlight - wrong start offset", 0, marks.get(0).getOffset());
        assertEquals("1. highlight - wrong end offset", 5, marks.get(1).getOffset());
        assertEquals("1. highlight - wrong attribs", "attribsA", marks.get(0).getAttributes().getAttribute("set-name"));

        assertEquals("2. highlight - wrong start offset", 5, marks.get(1).getOffset());
        assertEquals("2. highlight - wrong end offset", 19, marks.get(2).getOffset());
        assertEquals("2. highlight - wrong attribs", "attribsB", marks.get(1).getAttributes().getAttribute("set-name"));

        assertEquals("3. highlight - wrong start offset", 19, marks.get(2).getOffset());
        assertEquals("3. highlight - wrong end offset", 29, marks.get(3).getOffset());
        assertEquals("3. highlight - wrong attribs", "attribsA", marks.get(2).getAttributes().getAttribute("set-name"));
        assertNull("  3. highlight - wrong end", marks.get(3).getAttributes());
    }
    
    public void test122663_AddLeftMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(5, 10, attribsA);
        bag.addHighlight(5, 15, attribsB);

        assertMarks("Wrong highlights", createOffsetsBag(5, 15, attribsB), bag);
    }

    public void test122663_AddMultipleLeftMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(5, 10, attribsA);
        bag.addHighlight(15, 20, attribsA);
        bag.addHighlight(25, 30, attribsA);
        bag.addHighlight(5, 35, attribsB);

        assertMarks("Wrong highlights", createOffsetsBag(5, 35, attribsB), bag);
    }

    public void test122663_AddRightMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(10, 15, attribsA);
        bag.addHighlight(5, 15, attribsB);
        assertMarks("Wrong highlights", createOffsetsBag(5, 15, attribsB), bag);
    }

    public void test122663_AddMultipleRightMatches() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(30, 35, attribsA);
        bag.addHighlight(20, 25, attribsA);
        bag.addHighlight(10, 15, attribsA);
        bag.addHighlight(5, 35, attribsB);
        assertMarks("Wrong highlights", createOffsetsBag(5, 35, attribsB), bag);
    }

    public void test122663_AddBothMatch() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(10, 15, attribsA);
        bag.addHighlight(10, 15, attribsB);
        assertMarks("Wrong highlights", createOffsetsBag(10, 15, attribsB), bag);
    }

    public void test122663_AddMultipleBothMatch() throws BadLocationException {
        doc.insertString(0, "01234567890123456789", null);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        attribsA.addAttribute("set-name", "attribsA");
        attribsB.addAttribute("set-name", "attribsB");

        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(10, 15, attribsA);
        bag.addHighlight(20, 25, attribsA);
        bag.addHighlight(30, 35, attribsA);
        bag.addHighlight(10, 35, attribsB);
        assertMarks("Wrong highlights", createOffsetsBag(10, 35, attribsB), bag);
    }

    private void dumpHighlights(HighlightsSequence seq) {
        System.out.println("Dumping highlights from: " + seq + "{");
        while(seq.moveNext()) {
            System.out.println("<" + seq.getStartOffset() + ", " + seq.getEndOffset() + ", " + seq.getAttributes() + ">");
        }
        System.out.println("} --- End of Dumping highlights from: " + seq + " ---------------------");
    }

    private OffsetsBag createOffsetsBag(Object... triples) {
        assert triples != null;
        assert triples.length % 3 == 0;

        OffsetsBag bag = new OffsetsBag(doc);
        for(int i = 0; i < triples.length / 3; i++) {
            bag.addHighlight((Integer) triples[3 * i], (Integer) triples[3 * i + 1], (AttributeSet) triples[3 * i + 2]);
        }

        return bag;
    }

    private void assertMarks(String message, OffsetsBag expected, OffsetsBag actual) {
        try {
            OffsetGapList<OffsetsBag.Mark> expectedMarks = expected.getMarks();
            OffsetGapList<OffsetsBag.Mark> actualMarks = actual.getMarks();
            assertEquals("Different number of marks", expectedMarks.size(), actualMarks.size());
            for(int i = 0; i < expectedMarks.size(); i++) {
                OffsetsBag.Mark expectedMark = expectedMarks.get(i);
                OffsetsBag.Mark actualMark = actualMarks.get(i);
                assertEquals("Different offset at the " + i + "-th mark", expectedMark.getOffset(), actualMark.getOffset());
                assertSame("Different attributes at the " + i + "-th mark", expectedMark.getAttributes(), actualMark.getAttributes());
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

    private StringBuilder dumpMarks(OffsetsBag bag, StringBuilder sb) {
        for (int i = 0; i < bag.getMarks().size(); i++) {
            OffsetsBag.Mark mark = bag.getMarks().get(i);
            sb.append('[').append(i).append("] = ");
            sb.append('{').append(mark.toString()).append('}');
            if (i + 1 < bag.getMarks().size()) {
                sb.append('\n');
            }
        }
        return sb;
    }
}
