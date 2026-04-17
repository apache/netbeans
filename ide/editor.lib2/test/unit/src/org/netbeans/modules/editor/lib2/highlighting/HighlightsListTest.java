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
import java.awt.Font;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import org.junit.Test;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.editor.lib2.view.ViewUtils;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 *
 * @author mmetelka
 */
public class HighlightsListTest {
    
    static final Color[] colors = {
        Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.ORANGE
    };
    static final String[] fontNames = {
        "Monospaced", "Dialog"
    };
    Font defaultFont = new Font(fontNames[0], Font.PLAIN, 12);
    public static final AttributeSet[] attrSets = new AttributeSet[colors.length];
    static {
        for (int i = 0; i < colors.length; i++) {
            attrSets[i] = AttributesUtilities.createImmutable(
                    StyleConstants.Background, colors[i],
                    StyleConstants.FontFamily, fontNames[i & 1]);
        }
    }
    
    private static Document document() throws Exception {
        Document doc = new PlainDocument();
        String s = " ";
        while (s.length() < 100) {
            s += s;
        }
        doc.insertString(0, s, null);
        return doc;
    }

    @Test
    public void testSimple() throws Exception {
        Document doc = document();

        HighlightsList hList = highlightsListSimple(doc);
        // Fetch first
        AttributeSet attrs = hList.cutSameFont(defaultFont, 10, 10, null);
        assert (attrs instanceof CompoundAttributes) : "Non-CompoundAttributes attrs=" + attrs;
        CompoundAttributes cAttrs = (CompoundAttributes) attrs;
        assert (cAttrs.startOffset() == 0) : "startOffset=" + cAttrs.startOffset();
        HighlightItem[] items = cAttrs.highlightItems();
        assert (items.length == 3);
        assertItem(items[0], 2, null);
        assertItem(items[1], 4, attrSets[0]);
        assertItem(items[2], 6, null);
        // Fetch next
        assert (hList.startOffset() == 6);
        attrs = hList.cutSameFont(defaultFont, 10, 10, null);
        assert !(attrs instanceof CompoundAttributes);
        assert attrs == attrSets[1];
        assert (hList.startOffset() == 8);
        attrs = hList.cutSameFont(defaultFont, 10, 10, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == null);
        assert (hList.startOffset() == 10);
        
        
        hList = highlightsListSimple(doc);
        attrs = hList.cutSameFont(defaultFont, 2, 2, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == null);
        attrs = hList.cutSameFont(defaultFont, 10, 10, null);
        assert (hList.startOffset() == 6);
        assert (attrs instanceof CompoundAttributes) : "Non-CompoundAttributes attrs=" + attrs;
        cAttrs = (CompoundAttributes) attrs;
        assert (cAttrs.startOffset() == 2) : "startOffset=" + cAttrs.startOffset();
        items = cAttrs.highlightItems();
        assert (items.length == 2);
        assertItem(items[0], 4, attrSets[0]);
        assertItem(items[1], 6, null);
        
        hList = highlightsListSimple(doc);
        attrs = hList.cutSameFont(defaultFont, 3, 3, null);
        cAttrs = (CompoundAttributes) attrs;
        assert (cAttrs.startOffset() == 0) : "startOffset=" + cAttrs.startOffset();
        items = cAttrs.highlightItems();
        assert (items.length == 2);
        assertItem(items[0], 2, null);
        assertItem(items[1], 3, attrSets[0]);
        // Next
        attrs = hList.cutSameFont(defaultFont, 5, 5, null);
        cAttrs = (CompoundAttributes) attrs;
        assert (cAttrs.startOffset() == 3) : "startOffset=" + cAttrs.startOffset();
        items = cAttrs.highlightItems();
        assert (items.length == 2);
        assertItem(items[0], 4, attrSets[0]);
        assertItem(items[1], 5, null);
        // Next
        attrs = hList.cutSameFont(defaultFont, 7, 7, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == null);
        assert (hList.startOffset() == 6);
        // Next
        attrs = hList.cutSameFont(defaultFont, 7, 7, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == attrSets[1]);
        assert (hList.startOffset() == 7);
        // Next
        attrs = hList.cutSameFont(defaultFont, 10, 10, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == attrSets[1]);
        assert (hList.startOffset() == 8);
        // Next
        attrs = hList.cutSameFont(defaultFont, 10, 10, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == null);
        assert (hList.startOffset() == 10);
    }
    
    private HighlightsList highlightsListSimple(Document doc) {
        OffsetsBag bag = new OffsetsBag(doc);
        bag.addHighlight(2, 4, attrSets[0]);
        bag.addHighlight(6, 8, attrSets[1]);

        DirectMergeContainer dmc = new DirectMergeContainer(new HighlightsContainer[]{ bag }, true);
        HighlightsReader reader = new HighlightsReader(dmc, 0, 10);
        reader.readUntil(10);
        return reader.highlightsList();
    }
    
    @Test
    public void testSplitPrependText() throws Exception {
        Document doc = document();

        OffsetsBag bag = new OffsetsBag(doc);
        AttributeSet attrs1 = AttributesUtilities.createImmutable(
                StyleConstants.Foreground, Color.RED,
                StyleConstants.FontFamily, fontNames[0]);
        AttributeSet attrs2 = AttributesUtilities.createImmutable(
                StyleConstants.Foreground, Color.RED,
                StyleConstants.FontFamily, fontNames[0],
                ViewUtils.KEY_VIRTUAL_TEXT_PREPEND, "test");
        AttributeSet attrs3 = AttributesUtilities.createImmutable(
                StyleConstants.Foreground, Color.RED,
                StyleConstants.FontFamily, fontNames[1]);

        bag.addHighlight(0, 2, attrs1);
        bag.addHighlight(2, 4, attrs2);
        bag.addHighlight(4, 6, attrs1);
        bag.addHighlight(8, 10, attrs3);

        int end = 14;
        DirectMergeContainer dmc = new DirectMergeContainer(new HighlightsContainer[]{ bag }, true);

        {
        HighlightsReader reader = new HighlightsReader(dmc, 0, end);
        reader.readUntil(end);
        HighlightsList hList = reader.highlightsList();

        // Fetch first
        AttributeSet attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert (attrs instanceof CompoundAttributes) : "Non-CompoundAttributes attrs=" + attrs;
        CompoundAttributes cAttrs = (CompoundAttributes) attrs;
        assert (cAttrs.startOffset() == 0) : "startOffset=" + cAttrs.startOffset();
        HighlightItem[] items = cAttrs.highlightItems();
        assert (items.length == 4);
        assertItem(items[0], 2, attrs1);
        assertItem(items[1], 4, attrs2);
        assertItem(items[2], 6, attrs1);
        assertItem(items[3], 8, null);
        // Fetch next
        assert (hList.startOffset() == 8);
        attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert !(attrs instanceof CompoundAttributes);
        assert attrs == attrs3;
        assert (hList.startOffset() == 10);
        attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == null);
        assert (hList.startOffset() == 14);
        }

        {
        HighlightsReader reader = new HighlightsReader(dmc, 0, end);
        reader.readUntil(end);
        HighlightsList hList = reader.highlightsList();

        // Fetch first
        AttributeSet attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert !(attrs instanceof CompoundAttributes) : "CompoundAttributes attrs=" + attrs;
        assert attrs == attrs1;
        assert (hList.startOffset() == 2);
        attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == attrs2);
        assert (hList.startOffset() == 4);
        attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert (attrs instanceof CompoundAttributes) : "Non-CompoundAttributes attrs=" + attrs;
        CompoundAttributes cAttrs = (CompoundAttributes) attrs;
        assert (cAttrs.startOffset() == 4) : "startOffset=" + cAttrs.startOffset();
        HighlightItem[] items = cAttrs.highlightItems();
        assert (items.length == 2);
        assertItem(items[0], 6, attrs1);
        assertItem(items[1], 8, null);
        // Fetch next
        assert (hList.startOffset() == 8);
        attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert !(attrs instanceof CompoundAttributes);
        assert attrs == attrs3;
        assert (hList.startOffset() == 10);
        attrs = hList.cutSameFont(defaultFont, end, end, null);
        assert !(attrs instanceof CompoundAttributes);
        assert (attrs == null);
        assert (hList.startOffset() == 14);
        }
    }

    private static void assertItem(HighlightItem item, int endOffset, AttributeSet attrs) {
        assert (item.getEndOffset() == endOffset) : "itemEndOffset=" + item.getEndOffset() + " != endOffset=" + endOffset; // NOI18N
        assert (item.getAttributes() == attrs) : "itemAttrs=" + item.getAttributes() + " != attrs=" + attrs; // NOI18N
    }

}
