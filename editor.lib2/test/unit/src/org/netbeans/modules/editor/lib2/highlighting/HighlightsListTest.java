/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
    
    private static void assertItem(HighlightItem item, int endOffset, AttributeSet attrs) {
        assert (item.getEndOffset() == endOffset) : "itemEndOffset=" + item.getEndOffset() + " != endOffset=" + endOffset; // NOI18N
        assert (item.getAttributes() == attrs) : "itemAttrs=" + item.getAttributes() + " != attrs=" + attrs; // NOI18N
    }

}
