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
package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.junit.Test;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.editor.lib2.highlighting.CompoundAttributes;
import org.netbeans.modules.editor.lib2.highlighting.HighlightItem;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author Miloslav Metelka
 */
public class ViewPaintHighlightsTest {
    
    public ViewPaintHighlightsTest() {
    }

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
    
    @Test
    public void testSimple() throws Exception {
        HighlightItem[] phItems = new HighlightItem[] {
            new HighlightItem(4, attrSets[0]), // <2,4> [0]
            new HighlightItem(8, attrSets[1]), // <4,8> [1]
            new HighlightItem(12, attrSets[0]), // <8,12> [0]
            new HighlightItem(14, attrSets[1]), // <12,14> [1]
            new HighlightItem(16, null),        // <14,16> null
            new HighlightItem(18, attrSets[0]), // <16,18> [0]
            new HighlightItem(20, attrSets[2]), // <18,20> [2]
        };
        HighlightsList phList = new HighlightsList(2, phItems);
        ViewPaintHighlights vph = new ViewPaintHighlights(phList);
        AttributeSet attrSets31 = AttributesUtilities.createComposite(attrSets[1], attrSets[3]);
        AttributeSet attrSets30 = AttributesUtilities.createComposite(attrSets[0], attrSets[3]);
        AttributeSet attrSets02 = AttributesUtilities.createComposite(attrSets[2], attrSets[0]);

        HighlightItem[] vhItems = new HighlightItem[] {
            new HighlightItem(13, null),        // <11,13> null
            new HighlightItem(15, attrSets[3]), // <13,15> [3]
            new HighlightItem(17, attrSets[0]), // <15,17> [0]
        };
        CompoundAttributes cvAttrs = new CompoundAttributes(11, vhItems);
        TestHighlightsView testView = new TestHighlightsView(11, 6, cvAttrs); // length ignored
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 11, 12, attrSets[0],
                13, attrSets[1],
                14, attrSets31,
                15, attrSets[3],
                16, attrSets[0],
                17, attrSets[0]);
        
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 11,
                12, attrSets[0],
                13, attrSets[1],
                14, attrSets31,
                15, attrSets[3],
                16, attrSets[0],
                17, attrSets[0]);
        
        vph.reset(testView, 2); // have shift 2 chars
        assertHighlightsSequence(vph, 13,
                14, attrSets31,
                15, attrSets[3],
                16, attrSets[0],
                17, attrSets[0]);
        
        // Simulate the view move forward by 2 chars
        testView.setStartOffset(13);
            // View highlights are as follows:
            // <13,15> null         <10,12> [0]
            // <15,17> [3]          <12,14> [1]
            // <17,19> [0]          <14,16> null
            //                      <16,18> [0]
            //                      <18,20> [2]
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 13,
                14, attrSets[1],
                15, null,
                16, attrSets[3],
                17, attrSets30,
                18, attrSets[0],
                19, attrSets02);
        
        vph.reset(testView, 1);
        assertHighlightsSequence(vph, 14,
                15, null,
                16, attrSets[3],
                17, attrSets30,
                18, attrSets[0],
                19, attrSets02);

        // Simulate the view moves back by 1 char
        testView.setStartOffset(10);
            // View highlights are as follows:
            // <10,12> null         <10,12> [0]
            // <12,14> [3]          <12,14> [1]
            // <14,16> [0]          <14,16> null
            //                      <16,18> [0]
            //                      <18,20> [2]
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 10,
                12, attrSets[0],
                14, attrSets31,
                16, attrSets[0]);

        // Simulate the view moves back by 7 chars
        testView.setStartOffset(4);
            // View highlights are as follows:
            // <4,6> null           <2,4> [0]
            // <6,8> [3]            <4,8> [1]
            // <8,10> [0]           <8,12> [0]
            //                      <12,14> [1]
            //                      <14,16> null
            //                      <16,18> [0]
            //                      <18,20> [2]
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 4,
                6, attrSets[1],
                8, attrSets31,
                10, attrSets[0]);

        vph.reset(testView, 3); // Shift 3
        assertHighlightsSequence(vph, 7,
                8, attrSets31,
                10, attrSets[0]);
        
        
        // Regular attributes
        testView.setAttributes(attrSets[3]);
        testView.setStartOffset(6);
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 6,
                8, attrSets31,
                12, attrSets30);
        
        vph.reset(testView, 2);
        assertHighlightsSequence(vph, 8,
                12, attrSets30);

        // Regular attributes
        testView.setAttributes(null);
        testView.setStartOffset(6);
        vph.reset(testView, 0);
        assertHighlightsSequence(vph, 6,
                8, attrSets[1],
                12, attrSets[0]);
    }
    
    /**
     * @param args start-offset, end-offset, attrs[, end-offset, attrs] etc.
     */
    public static void assertHighlightsSequence(HighlightsSequence hs, int startOffset, Object... args) {
        for (int i = 0; i < args.length;) {
            int endOffset = (Integer) args[i++];
            AttributeSet attrs = (AttributeSet) args[i++];
            boolean next = hs.moveNext();
            assert next : "Expecting hs.moveNext(): <" + startOffset + "," + endOffset + "> attrs=" + attrs;
            boolean attrsOk = (attrs == null && hs.getAttributes() == null) ||
                    (attrs != null && attrs.equals(hs.getAttributes()));
            assert attrsOk : "Expecting " + attrs + " but found " + hs.getAttributes();
            assert (startOffset == hs.getStartOffset() && endOffset == hs.getEndOffset()) :
                    "Expecting <" + startOffset + "," + endOffset + "> but found " +
                    hs.getStartOffset() + "," + hs.getEndOffset();
            startOffset = endOffset;
        }
        assert !hs.moveNext() : "Unexpected hs.moveNext(): <" + hs.getStartOffset() + "," + hs.getEndOffset() + ">";
    }

}
