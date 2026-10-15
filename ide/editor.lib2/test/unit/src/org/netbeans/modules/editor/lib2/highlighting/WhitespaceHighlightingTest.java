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

package org.netbeans.modules.editor.lib2.highlighting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author Miloslav Metelka
 */
public class WhitespaceHighlightingTest extends NbTestCase {
    
    private static final AttributeSet INDENT_ATTRS = AttributesUtilities.createImmutable(StyleConstants.Background, Color.red);

    private static final AttributeSet TRAILING_ATTRS = AttributesUtilities.createImmutable(StyleConstants.Background, Color.green);
    
    private JEditorPane pane;
    
    private Document doc;
    
    private WhitespaceHighlighting wh;
    
    private List<Object> expectedChangeOffsetRanges = new ArrayList<>();
    
    public WhitespaceHighlightingTest(String name) {
        super(name);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimple");
//        filterTests(includes);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pane = new JEditorPane();
        doc = pane.getDocument();
        wh = new WhitespaceHighlighting(pane);
        wh.testInitEnv(INDENT_ATTRS, TRAILING_ATTRS);
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
    
    public void testInsertsRemovals() throws Exception {
        doc.insertString(0, "  ", null);
        L l = new L();
        wh.addHighlightsChangeListener(l);
        expectedChangeOffsetRanges.add(0);
        expectedChangeOffsetRanges.add(3);
        doc.insertString(1, "a", null);
        
        expectedChangeOffsetRanges.add(0);
        expectedChangeOffsetRanges.add(6);
        doc.insertString(1, "x \n y", null);
        
        expectedChangeOffsetRanges.add(2);
        expectedChangeOffsetRanges.add(4);
        doc.remove(3, 1);
        
        
        wh.removeHighlightsChangeListener(l);
    }
    
    public void testWSHighlights() throws Exception {
        HighlightsSequence hs;

        doc.insertString(0, "   ", null);
        hs = wh.getHighlights(0, doc.getLength());
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 0);
        assertEquals(hs.getEndOffset(), doc.getLength());
        assertEquals(hs.getAttributes(), TRAILING_ATTRS);
        assertFalse(hs.moveNext());

        doc.insertString(1, "a", null);
        hs = wh.getHighlights(0, doc.getLength());
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 0);
        assertEquals(hs.getEndOffset(), 1);
        assertEquals(hs.getAttributes(), INDENT_ATTRS);
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 2);
        assertEquals(hs.getEndOffset(), doc.getLength());
        assertEquals(hs.getAttributes(), TRAILING_ATTRS);
        assertFalse(hs.moveNext());
        
        // Multi-line
        doc.insertString(1, "\n", null);
        hs = wh.getHighlights(0, doc.getLength());
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 0);
        assertEquals(hs.getEndOffset(), 1);
        assertEquals(hs.getAttributes(), TRAILING_ATTRS);
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 3);
        assertEquals(hs.getEndOffset(), doc.getLength());
        assertEquals(hs.getAttributes(), TRAILING_ATTRS);
        assertFalse(hs.moveNext());
        
        doc.insertString(1, "x ", null);
        hs = wh.getHighlights(0, doc.getLength());
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 0);
        assertEquals(hs.getEndOffset(), 1);
        assertEquals(hs.getAttributes(), INDENT_ATTRS);
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 2);
        assertEquals(hs.getEndOffset(), 3);
        assertEquals(hs.getAttributes(), TRAILING_ATTRS);
        assertTrue(hs.moveNext());
        assertEquals(hs.getStartOffset(), 5);
        assertEquals(hs.getEndOffset(), doc.getLength());
        assertEquals(hs.getAttributes(), TRAILING_ATTRS);
        assertFalse(hs.moveNext());
        
    }

    void checkRemoveChangeOffset(int offset, String msg) {
        assertEquals(msg, offset, (int) expectedChangeOffsetRanges.get(0));
        expectedChangeOffsetRanges.remove(0);
    }

    private final class L implements HighlightsChangeListener {

        @Override
        public void highlightChanged(HighlightsChangeEvent event) {
            int startOffset = event.getStartOffset();
            int endOffset = event.getEndOffset();
            String range = "change range <" + startOffset + "," + endOffset + ">";
            checkRemoveChangeOffset(startOffset, "Invalid startOffset of " + range);
            checkRemoveChangeOffset(endOffset, "Invalid endOffset of " + range);
        }
        
    }
}
