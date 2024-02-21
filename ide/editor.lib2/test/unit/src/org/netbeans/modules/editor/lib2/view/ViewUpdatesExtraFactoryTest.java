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
package org.netbeans.modules.editor.lib2.view;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

import static org.netbeans.modules.editor.lib2.view.ViewUpdatesTesting.HI_VIEW;
import static org.netbeans.modules.editor.lib2.view.ViewUpdatesTesting.TAB_VIEW;
import static org.netbeans.modules.editor.lib2.view.ViewUpdatesTesting.NL_VIEW;
import static org.netbeans.modules.editor.lib2.view.ViewUpdatesTesting.P_VIEW;
import static org.netbeans.modules.editor.lib2.view.ViewUpdatesTesting.VIEW_BUILDER_TEST_VALUE_NAMES;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 *
 * @author mmetelka
 */
public class ViewUpdatesExtraFactoryTest extends NbTestCase {
    
    public ViewUpdatesExtraFactoryTest(String testName) {
        super(testName);

        List<String> includes = new ArrayList<String>();
//        includes.add("testExtraHighlightSpansNewline");
//        filterTests(includes);
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ViewUpdatesTesting.registerTestFactory();
        ViewUpdatesTesting.setTestValuesNames(VIEW_BUILDER_TEST_VALUE_NAMES);

    }

    @Override
    protected int timeOut() {
        return super.timeOut();
//        return 10000000;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO; // null;
//        return Level.FINEST;
    }

    private static void loggingOn() {
        // FINEST throws ISE for integrity error in EditorView
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
        ViewHierarchyImpl.CHECK_LOG.setLevel(Level.FINEST);
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // Check gap-storage correctness
//        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorBoxViewChildren").setLevel(Level.FINE);
    }
    
    private static void addHighlight(Document doc, PositionsBag bag, int startOffset, int endOffset, AttributeSet attrs)
    throws BadLocationException
    {
        bag.addHighlight(doc.createPosition(startOffset), doc.createPosition(endOffset), attrs);
    }

    public void testExtraHighlightSpansNewline() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        int removeLength;
        int HIGHLIGHT_COUNT = 30;
        // Test at least GAP_STORAGE_THRESHOLD
        assert (HIGHLIGHT_COUNT > ViewGapStorage.GAP_STORAGE_THRESHOLD);
        StringBuilder sbLine = new StringBuilder(200);
        String hlText = "ab";
        for (int i = HIGHLIGHT_COUNT; i > 0; i--) {
            sbLine.append(hlText);
        }
        sbLine.append('\n');
        int lineLen = sbLine.length();
        int LINE_COUNT = 2;
        StringBuilder sb = new StringBuilder(400);
        for (int i = LINE_COUNT; i > 0; i--) {
            sb.append(sbLine);
        }
        
        text = sb.toString();
        offset = 0;
        doc.insertString(offset, text, null);

        // Add highlights
        final PositionsBag hlts = new PositionsBag(doc);
        offset = 0;
        for (int i = LINE_COUNT; i > 0; i--) {
            for (int j = HIGHLIGHT_COUNT; j > 0; j--) {
                addHighlight(doc, hlts, offset, offset + 2, ViewUpdatesTesting.FONT_ATTRS[j % 2]);
                offset += hlText.length();
            }
            assert (text.charAt(offset) == '\n');
            offset++; // skip newline
        }
        doc.render(new Runnable() {
            @Override
            public void run() {
                highlights.setHighlights(hlts); // since non-empty setTestValues() is active no delayed rebuild occurs
            }
        });
        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        docView.ensureAllParagraphsChildrenAndLayoutValid();

        ViewUpdatesTesting.checkViews(docView, 0, 6,
            hlText.length(), HI_VIEW,
            hlText.length(), HI_VIEW,
            hlText.length(), HI_VIEW
        );
        
        // Set a test highlight that spans end of first line. View creation should
        // continue to next line (EVFactory.continueCreation() should be called).
        TestHighlightsViewFactory testFactory = ViewUpdatesTesting.getTestFactory(pane);
        List<TestHighlight> testHlts = ViewUpdatesTesting.getHighlightsCopy(testFactory);
        TestHighlight testHlt = TestHighlight.create(
                ViewUtils.createPosition(doc, lineLen - 2),
                ViewUtils.createPosition(doc, lineLen + 1),
                ViewUpdatesTesting.FONT_ATTRS[2]
        );
        testHlts.add(testHlt);
        testFactory.setHighlights(testHlts);
        // No firing of change - just change whether infrastructure handles situation well.
        // Now perform insert at point where the extra highlight starts.
        // Force extra view factory to produce view that spans newline.
        offset = testHlt.startOffset(); // one char before newline
        text = "x";
        // Expect the test factory will get continueCreation() call:
        // startOffset: end of produced highlight (that spanned newline); held by position => text length incorporated.
        // endOffset: end of second line + inserted text length
        testFactory.setContinueCreationRange(testHlt.endOffset() + text.length(), lineLen * 2 + text.length());

        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 1,
/*matchOffset*/         offset + 1 +text.length(),
/*endCreationOffset*/   lineLen + text.length(), // includes '\n'
/*bmReuseOffset*/       offset - 1,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   29,
/*amReuseOffset*/       offset + 2,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   30
        );
        doc.insertString(offset, text, null);
        
        if (docView.getParagraphView(0).children.gapStorage == null) {
//            TestCase.fail("Expecting non-null gapStorage");
        }
        if (!testFactory.isContinueCreationUnset()) {
//            TestCase.fail("Expecting continueCreation() called.");
        }

        ViewUpdatesTesting.setTestValues();
    }

    public void testNullChildren() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        // Insert twice the length of chars that would be otherwise serviced with local views creation
        int insertCount = ViewBuilder.MAX_CHARS_FOR_CREATE_LOCAL_VIEWS;
        String insertText = "a\n";
        for (int i = 0; i <= insertCount; i++) {
            doc.insertString(0, insertText, null);
        }
        int docLen = doc.getLength();
        int lineCount = doc.getDefaultRootElement().getElementCount();

        docView.op.releaseChildren(false);
        docView.ensureLayoutValidForInitedChildren();
        docView.children.ensureParagraphsChildrenAndLayoutValid(docView, 0, lineCount / 2, 0, 0);

        int hlStartOffset = docLen * 1 / 5;
        int hlStartPIndex = doc.getDefaultRootElement().getElementIndex(hlStartOffset);
        int hlEndOffset = hlStartOffset * 2;
        int hlEndPIndex = hlStartPIndex * 2;
        TestHighlightsViewFactory testFactory = ViewUpdatesTesting.getTestFactory(pane);
        List<TestHighlight> testHlts = ViewUpdatesTesting.getHighlightsCopy(testFactory);
        TestHighlight testHlt = TestHighlight.create(
                ViewUtils.createPosition(doc, hlStartOffset),
                ViewUtils.createPosition(doc, hlEndOffset),
                ViewUpdatesTesting.FONT_ATTRS[2]
        );
        testHlts.add(testHlt);
        testFactory.setHighlights(testHlts);
        testFactory.fireChange(hlStartOffset, hlEndOffset);
        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid

        docView.children.ensureParagraphsChildrenAndLayoutValid(docView,
                hlStartPIndex, hlEndPIndex, 0, 0);

        ViewUpdatesTesting.setTestValues();
    }

    public void testHighlightBeyondDocEnd() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final OffsetsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayerOffsets(pane);
        String text = "abcd\nefgh";
        doc.insertString(0, text, null);
        highlights.addHighlight(0, 5, ViewUpdatesTesting.FONT_ATTRS[0]);
        highlights.addHighlight(7, doc.getLength() + 2, ViewUpdatesTesting.FONT_ATTRS[1]); // Beyond doc's end

        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        docView.ensureAllParagraphsChildrenAndLayoutValid();

    }

    public void testCustomHighlightBeyondDocEnd() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final TestOffsetsHighlightsContainer highlights = new TestOffsetsHighlightsContainer();
        ViewUpdatesTesting.getSingleHighlightingLayerCustom(pane, highlights);
        highlights.setOffsetPairs(new int[] { 0, 5 });
        String text = "abcd\nefgh";
        doc.insertString(0, text, null);

        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        highlights.setOffsetPairs(new int[] { 3, 9 });
        doc.remove(0, doc.getLength());
    }

}
