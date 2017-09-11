/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
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
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[includeTests.size()]));
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
