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

/**
 *
 * @author mmetelka
 */
public class ViewUpdatesTest extends NbTestCase {
    
    public ViewUpdatesTest(String testName) {
        super(testName);

        List<String> includes = new ArrayList<String>();
//        includes.add("testModsNoHighlights");
//        includes.add("testNewlineRemovalNoHighlights");
//        includes.add("testLongInsert");
//        includes.add("testModsWithHighlights");
//        includes.add("testLongHighlightedLine");
//        includes.add("testRemoveAtBeginning");
//        includes.add("testRemoveTrailingWhitespace");
        includes.add("testRemoveSpanningEndOfPartial");
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
        ViewUpdatesTesting.setTestValuesNames(VIEW_BUILDER_TEST_VALUE_NAMES);
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

    public void testModsNoHighlights() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        DocumentView docView = DocumentView.get(pane);
        int offset;
        String text;
        int removeLength;
        
        offset = 0;
        text = "hello";
        // Insert at begining of (first) pView
        // Reuse occurs at ending '\n'
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, NL_VIEW
        );
        
        // Insert after "he"
        offset = 2;
        text = "\n";
        // "hello\n"
        //  =>
        // "he\n"
        // "llo\n"
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         doc.getLength() + text.length(),
/*endCreationOffset*/   doc.getLength() + 1 + text.length(), // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       doc.getLength() + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2, HI_VIEW,
            1, NL_VIEW,
            3, HI_VIEW,
            1, NL_VIEW
        );

        // Insert right before '\n' on first line => Needs rebuild of first local view too
        // since they may (and will) merge into a single TextLayout.
        offset = 2;
        text = "ab";
        // "he\n"
        // "llo\n"
        //  =>
        // "heab\n"
        // "llo\n"
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   1
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            4, HI_VIEW,
            1, NL_VIEW,
            3, HI_VIEW,
            1, NL_VIEW
        );

        
        // Removal inside first view on first line => Needs rebuild of first local view.
        offset = 2;
        removeLength = 1;
        // "heab\n"
        // "llo\n"
        //  =>
        // "heb\n"
        // "llo\n"
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0,
/*matchOffset*/         3, // end of first view
/*endCreationOffset*/   4,
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       3,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   1
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            1, NL_VIEW,
            3, HI_VIEW,
            1, NL_VIEW
        );

        
        // Removal at begining of first line => Needs rebuild of first local view.
        offset = 0;
        removeLength = 1;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0,
/*matchOffset*/         2, // end of first view
/*endCreationOffset*/   3,
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       2,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   1
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2, HI_VIEW,
            1, NL_VIEW,
            3, HI_VIEW,
            1, NL_VIEW
        );

        
        // Insert whole line with \n at begining of line 1.
        offset = 3;
        text = "abcd\n";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 4, // includes orig. second line
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2, HI_VIEW,
            1, NL_VIEW /* e:3 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:8 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:12 */
        );
        
        
        // Insert text at begining of line 0.
        offset = 0;
        text = "xy";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 3, // includes orig. first line
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            4, HI_VIEW,
            1, NL_VIEW /* e:5 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:5 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:14 */
        );
        
        
        // Insert text at begining of line 2.
        offset = 10;
        text = "uv";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 4, // includes orig. line 3
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(2),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       2,
/*amReusePView*/        docView.getParagraphView(2),
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            4, HI_VIEW,
            1, NL_VIEW /* e:5 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:10 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:16 */
        );

        
        // Insert text at begining of line 2.
        offset = 10;
        removeLength = 2;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + 3,
/*endCreationOffset*/   offset + 4, // includes orig. line 3
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(2),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + 3,
/*amReusePIndex*/       2,
/*amReusePView*/        docView.getParagraphView(2),
/*amReuseLocalIndex*/   1
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            4, HI_VIEW,
            1, NL_VIEW /* e:5 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:10 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:14 */
        );

        ViewUpdatesTesting.setTestValues();
    }
    

    public void testNewlineRemovalNoHighlights() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        DocumentView docView = DocumentView.get(pane);
        pane.modelToView(0); // Cause initialization
        int offset;
        String text;
        int removeLength;
        
        offset = 0;
        text = "hello\nworld\ntest\ntest2";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:17 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:23 */
        );
        
        // Insert after "he"
        offset = 2;
        text = "\n";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length() + 3,
/*endCreationOffset*/   offset + text.length() + 4, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length() + 3, // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2, HI_VIEW,
            1, NL_VIEW /* e:3 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:7 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:13 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:18 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:24 */
        );

        offset = 2;
        removeLength = 1;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Remove at local view's boundary => rebuild previous local view
/*matchOffset*/         offset,
/*endCreationOffset*/   6, // Removal of ending newline => till end of next line
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset,
/*amReusePIndex*/       1, // reuse first local view in pView[1]
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:17 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:23 */
        );

        offset = 5;
        removeLength = 7;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Remove at local view's boundary => rebuild previous local view
/*matchOffset*/         offset,
/*endCreationOffset*/   10, // Removal of ending newline => till end of next line
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset,
/*amReusePIndex*/       2,
/*amReusePView*/        docView.getParagraphView(2),
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            9, HI_VIEW,
            1, NL_VIEW /* e:10 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:16 */
        );

        ViewUpdatesTesting.setTestValues();
    }

    public void testLongInsert() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        DocumentView docView = DocumentView.get(pane);
        pane.modelToView(0); // Cause initialization
        int offset;
        String text;
        
        offset = 0;
        StringBuilder sb = new StringBuilder(2048);
        while (sb.length() < 2048) {
            sb.append('a');
        }
        text = sb.toString();
        // Insert at begining of (first) pView
        // Reuse occurs at ending '\n'
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    false,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length() + 1, // includes '\n'
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE, // No NewlineView reusal since createLocalViews==false
/*amReusePIndex*/       1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2049, P_VIEW /* e:2049 */
        );
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);

        // Test removal inside
        offset = 100;
        int removeLength = 10;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    false, // no children originally -> retain same state
/*startCreationOffset*/ 0, // no children -> rebuild from start
/*matchOffset*/         doc.getLength() + 1 - removeLength,
/*endCreationOffset*/   doc.getLength() + 1 - removeLength, // includes orig. line 3
/*bmReuseOffset*/       offset,
/*bmReusePView*/        null, // no children
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2039, P_VIEW /* e:2039 */
        );

        // Test removal at line begining
        offset = 0;
        removeLength = 10;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    false, // no children originally -> retain same state
/*startCreationOffset*/ 0, // no children -> rebuild from start
/*matchOffset*/         doc.getLength() + 1 - removeLength,
/*endCreationOffset*/   doc.getLength() + 1 - removeLength, // includes orig. line 3
/*bmReuseOffset*/       offset,
/*bmReusePView*/        null, // no children
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2029, P_VIEW /* e:2039 */
        );

        ViewUpdatesTesting.setTestValues();
    }

    public void testLongLineInsert() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        DocumentView docView = DocumentView.get(pane);
        pane.modelToView(0); // Cause initialization
        int offset;
        String text;
        
        offset = 0;
        int lineLen = 4000;
        StringBuilder sb = new StringBuilder(lineLen + 10);
        for (int i = 0; i < lineLen; i++) {
            sb.append('a');
        }
        sb.append('\n');
        text = sb.toString();
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    false,
/*startCreationOffset*/ 0,
/*matchOffset*/         offset + text.length() + 1, // createLocalViews == false
/*endCreationOffset*/   offset + text.length() + 1,
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE, // createLocalViews == false
/*amReusePIndex*/       1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            4001, P_VIEW /* e:4001 */,
            1, P_VIEW /* e:4002 */
        );

        ViewUpdatesTesting.setTestValues();
    }

    public void testModsWithHighlights() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        pane.modelToView(0); // Cause initialization
        int offset;
        String text;
        int removeLength;
        
        offset = 0;
        text = "hello world\nhere we are\nman how are you doing?\n\nhere";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            11, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            11, HI_VIEW,
            1, NL_VIEW /* e:24 */,
            22, HI_VIEW,
            1, NL_VIEW /* e:47 */,
            1, NL_VIEW /* e:48 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:53 */
        );

        // Add highlights
        final PositionsBag hlts = new PositionsBag(doc);
        addHighlight(doc, hlts, 0, 5, ViewUpdatesTesting.FONT_ATTRS[0]);
        addHighlight(doc, hlts, 6, 11, ViewUpdatesTesting.FONT_ATTRS[1]);
        
        addHighlight(doc, hlts, 12, 16, ViewUpdatesTesting.FONT_ATTRS[0]);
        addHighlight(doc, hlts, 17, 19, ViewUpdatesTesting.FONT_ATTRS[1]);
        addHighlight(doc, hlts, 20, 23, ViewUpdatesTesting.FONT_ATTRS[2]);

        addHighlight(doc, hlts, 28, 31, ViewUpdatesTesting.FONT_ATTRS[0]);
        addHighlight(doc, hlts, 32, 35, ViewUpdatesTesting.FONT_ATTRS[1]);
        addHighlight(doc, hlts, 36, 39, ViewUpdatesTesting.FONT_ATTRS[2]);
        int endHighlightsOffset = 39;

        doc.render(new Runnable() {
            @Override
            public void run() {
                highlights.setHighlights(hlts); // since non-empty setTestValues() is active no delayed rebuild occurs
            }
        });
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        
        int matchOffset = docView.getParagraphView(docView.getViewIndex(endHighlightsOffset)).getEndOffset();
        ViewUpdatesTesting.setTestValues(
ViewBuilder.RebuildCause.INIT_PARAGRAPHS,
/*createLocalViews*/    true, // ensureParagraphsValid() requires true
/*startCreationOffset*/ 0,
/*matchOffset*/         matchOffset,
/*endCreationOffset*/   matchOffset,
/*bmReuseOffset*/       0,
/*bmReusePView*/        null, // no children
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       0,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0
        );
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            4, HI_VIEW, // e: 16
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:24 */,
            4, HI_VIEW,
            3, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            7, HI_VIEW,
            1, NL_VIEW /* e:47 */,
            1, NL_VIEW /* e:48 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:53 */
        );

        // Insert after first local view (will extend the existing highlight)
        offset = 16;
        text = "x";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 12,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   25, // includes '\n'
/*bmReuseOffset*/       12,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       17, // Possibly reuse NewlineView
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 12, 25,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:25 */
        );


        // Insert in the middle of 2-char highlight
        offset = 19;
        text = "y";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 18,
/*matchOffset*/         offset + text.length() + 1,
/*endCreationOffset*/   26,
/*bmReuseOffset*/       18,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   2,
/*amReuseOffset*/       21, // Possibly reuse NewlineView
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   3 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 12, 26,
            5, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW, // e:25
            1, NL_VIEW /* e:26 */
        );


        // Insert TAB
        offset = 23;
        text = "\t";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 1,
/*matchOffset*/         offset + text.length() + 2,
/*endCreationOffset*/   27,
/*bmReuseOffset*/       offset - 1,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   4,
/*amReuseOffset*/       offset + text.length() + 2,
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   5
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 12, 27,
            5, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, HI_VIEW,
            1, HI_VIEW,
            1, TAB_VIEW,
            2, HI_VIEW,
            1, NL_VIEW /* e:27 */
        );
        
        if (!docView.getParagraphView(1).containsTabableViews()) {
            TestCase.fail("Should contain tabable views");
        }


        // Insert TAB on same line before previously inserted TAB
        offset = 20;
        text = "\t";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 2,
/*matchOffset*/         offset + text.length() + 1,
/*endCreationOffset*/   28,
/*bmReuseOffset*/       offset - 2,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   2,
/*amReuseOffset*/       offset + text.length() + 1,
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   3
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 12, 28,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, TAB_VIEW,
            1, HI_VIEW,
            1, HI_VIEW,
            1, HI_VIEW,
            1, TAB_VIEW,
            2, HI_VIEW,
            1, NL_VIEW /* e:28 */
        );


        ViewUpdatesTesting.setTestValues();
    }

    public void testLongHighlightedLine() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        pane.modelToView(0); // Cause initialization
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
        
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
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
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        ViewUpdatesTesting.setTestValues(
ViewBuilder.RebuildCause.INIT_PARAGRAPHS,
/*createLocalViews*/    true, // ensureParagraphsValid() requires true
/*startCreationOffset*/ 0,
/*matchOffset*/         text.length(),
/*endCreationOffset*/   text.length(),
/*bmReuseOffset*/       0,
/*bmReusePView*/        null, // no children
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       0,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0
        );
        docView.ensureAllParagraphsChildrenAndLayoutValid();

        ViewUpdatesTesting.checkViews(docView, 0, 6,
            hlText.length(), HI_VIEW,
            hlText.length(), HI_VIEW,
            hlText.length(), HI_VIEW
        );

        offset = 3;
        text = "x";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 1,
/*matchOffset*/         offset + 1 +text.length(),
/*endCreationOffset*/   lineLen + text.length(), // includes '\n'
/*bmReuseOffset*/       2,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   1,
/*amReuseOffset*/       offset + 2,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   2
        );
        doc.insertString(offset, text, null);
        
        if (docView.getParagraphView(0).children.gapStorage == null) {
            TestCase.fail("Expecting non-null gapStorage");
        }

        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        offset = lineLen + 15;
        text = "\t";
        doc.insertString(offset, text, null);
        if (!docView.getParagraphView(1).containsTabableViews()) {
            TestCase.fail("Should contain tabable views");
        }

        offset = lineLen + 5;
        text = "\t\t";
        doc.insertString(offset, text, null);
        
        
        ViewUpdatesTesting.setTestValues();
    }

    public void testPartialHierarchyModsWithHighlights() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        pane.modelToView(0); // Cause initialization (for docView.getParagraphView() operation)
        int removeLength;
        
        offset = 0;
        text = "hello world\nhere we are\n\nhere";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            11, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            11, HI_VIEW,
            1, NL_VIEW /* e:24 */,
            1, NL_VIEW /* e:25 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:30 */
        );

        // Add highlights
        final PositionsBag hlts = new PositionsBag(doc);
        addHighlight(doc, hlts, 0, 5, ViewUpdatesTesting.FONT_ATTRS[0]);
        addHighlight(doc, hlts, 6, 11, ViewUpdatesTesting.FONT_ATTRS[1]);
        
        addHighlight(doc, hlts, 12, 16, ViewUpdatesTesting.FONT_ATTRS[0]);
        addHighlight(doc, hlts, 17, 19, ViewUpdatesTesting.FONT_ATTRS[1]);
        addHighlight(doc, hlts, 20, 23, ViewUpdatesTesting.FONT_ATTRS[2]);

        int endHighlightsOffset = 23;

        doc.render(new Runnable() {
            @Override
            public void run() {
                highlights.setHighlights(hlts); // since non-empty setTestValues() is active no delayed rebuild occurs
            }
        });
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        
        int matchOffset = docView.getParagraphView(docView.getViewIndex(endHighlightsOffset)).getEndOffset();
        ViewUpdatesTesting.setTestValues(
ViewBuilder.RebuildCause.INIT_PARAGRAPHS,
/*createLocalViews*/    true, // ensureParagraphsValid() requires true
/*startCreationOffset*/ 0,
/*matchOffset*/         matchOffset,
/*endCreationOffset*/   matchOffset,
/*bmReuseOffset*/       0,
/*bmReusePView*/        null, // no children
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       0,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0
        );
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            4, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:24 */,
            1, NL_VIEW /* e:25 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:30 */
        );

        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        ViewUpdatesTesting.setViewBounds(pane, 6, 24);
        pane.modelToView(14); // Init
        pane.modelToView(5); // Should not fail
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        
        // Insert after first local view (will extend the existing highlight)
        offset = 16;
        text = "x";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 12,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   25, // includes '\n'
/*bmReuseOffset*/       12,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       17, // Possibly reuse NewlineView
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, -1, -1,
            5, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:25 */
        );

        offset = docView.getStartOffset();
        text = "x";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   13,
/*bmReuseOffset*/       6,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       7, // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, -1, -1,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:13 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:26 */
        );

        offset = docView.getEndOffset();
        text = "x";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 1,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   27,
/*bmReuseOffset*/       25,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   5,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       2,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 6, -1,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:13 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:26 */,
            1, HI_VIEW
        );

        offset = 3;
        text = "xy";
        ViewUpdatesTesting.setTestValues( // Use values that would never be matched (no rebuild takes place)
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ -1,
/*matchOffset*/         -1,
/*endCreationOffset*/   -1,
/*bmReuseOffset*/       -1,
/*bmReusePView*/        null,
/*bmReuseLocalIndex*/   -1,
/*amReuseOffset*/       -1,
/*amReusePIndex*/       -1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   -1
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 8, -1,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:13 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:26 */,
            1, HI_VIEW
        );
    
        offset = docView.getEndOffset();
        removeLength = 2;
        ViewUpdatesTesting.setTestValues( // Use values that would never be matched (no rebuild takes place)
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ -1,
/*matchOffset*/         -1,
/*endCreationOffset*/   -1,
/*bmReuseOffset*/       -1,
/*bmReusePView*/        null,
/*bmReuseLocalIndex*/   -1,
/*amReuseOffset*/       -1,
/*amReusePIndex*/       -1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   -1
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 8, -1,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:15 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW,
            1, NL_VIEW /* e:28 */,
            1, HI_VIEW
        );

    
        offset = docView.getEndOffset() - 2; // 27
        removeLength = 2;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 3,
/*matchOffset*/         offset,
/*endCreationOffset*/   offset,
/*bmReuseOffset*/       offset - 3,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   4,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       3,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 8, -1,
            1, HI_VIEW,
            5, HI_VIEW,
            1, NL_VIEW /* e:15 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW
        );

        offset = docView.getStartOffset(); // 8
        removeLength = 2;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + 4,
/*endCreationOffset*/   offset + 5,
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + 4, // Possibly reuse NewlineView
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   2
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 8, -1,
            4, HI_VIEW,
            1, NL_VIEW /* e:13 */,
            5, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, HI_VIEW,
            3, HI_VIEW
        );

        offset = docView.getStartOffset(); // 8
        removeLength = docView.getEndOffset() - offset; // Remove whole content
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset,
/*endCreationOffset*/   offset,
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       2,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, -1, -1);

        TestCase.assertEquals(8, docView.getStartOffset());
        TestCase.assertEquals(8, docView.getEndOffset());

        offset = 8; // Empty view hierarchy at startOffset==8
        text = "x";
        ViewUpdatesTesting.setTestValues( // Use values that would never be matched (no rebuild takes place)
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length(),
/*bmReuseOffset*/       offset,
/*bmReusePView*/        null,
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       0,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 8, -1,
            1, HI_VIEW
        );
    
        offset = docView.getStartOffset() - 2; // 6
        removeLength = 4;
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset,
/*endCreationOffset*/   offset,
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       1,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 8, -1);

        ViewUpdatesTesting.setTestValues();
    }

    public void testInsertCharAtLineBeginningWithCharRegion() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        pane.modelToView(0); // Cause initialization (for docView.getParagraphView() operation)
        
        offset = 0;
        text = "hello\nhere";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(),
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:11 */
        );

        // Add highlights
        final PositionsBag hlts = new PositionsBag(doc);
        addHighlight(doc, hlts, 0, 3, ViewUpdatesTesting.FONT_ATTRS[0]);
        addHighlight(doc, hlts, 3, 5, ViewUpdatesTesting.FONT_ATTRS[1]);
        addHighlight(doc, hlts, 6, 7, ViewUpdatesTesting.FONT_ATTRS[0]);
        int endHighlightsOffset = 8;
        addHighlight(doc, hlts, 7, endHighlightsOffset, ViewUpdatesTesting.FONT_ATTRS[1]);

        doc.render(new Runnable() {
            @Override
            public void run() {
                highlights.setHighlights(hlts); // since non-empty setTestValues() is active no delayed rebuild occurs
            }
        });
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        docView.op.viewsRebuildOrMarkInvalid(); // Manually mark affected children as invalid
        
        int matchOffset = docView.getParagraphView(docView.getViewIndex(endHighlightsOffset)).getEndOffset();
        ViewUpdatesTesting.setTestValues(
ViewBuilder.RebuildCause.INIT_PARAGRAPHS,
/*createLocalViews*/    true, // ensureParagraphsValid() requires true
/*startCreationOffset*/ 0,
/*matchOffset*/         matchOffset,
/*endCreationOffset*/   matchOffset,
/*bmReuseOffset*/       0,
/*bmReusePView*/        null, // no children
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       0,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0
        );
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            2, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            1, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, NL_VIEW /* e:11 */
        );

        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        
        // Insert after first local view (will extend the existing highlight)
        offset = docView.getParagraphView(0).getEndOffset(); // 6
        text = "x";
        int cRegionStartOffset = offset - 1;
        int cRegionEndOffset = offset + 2; // At boundary of second view
        docView.op.viewUpdates.extendCharRebuildRegion(OffsetRegion.create(doc, cRegionStartOffset, cRegionEndOffset));
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         cRegionEndOffset + text.length(),
/*endCreationOffset*/   docView.getEndOffset() + text.length(),
/*bmReuseOffset*/       offset,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(), // Possibly reuse NewlineView
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, -1, -1,
            3, HI_VIEW,
            2, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            1, HI_VIEW,
            1, HI_VIEW,
            1, HI_VIEW,
            2, HI_VIEW,
            1, NL_VIEW /* e:12 */
        );

        ViewUpdatesTesting.setTestValues();
    }

    public void testRemoveAtBeginning() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        int cRegionStartOffset;
        int cRegionEndOffset;
        
        offset = 0;
        text = " \t\tabcdef\ta\nebxsu";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0, // Insert at end of existing local view => rebuild prev one
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(),
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            1, HI_VIEW,
            2, TAB_VIEW,
            6, HI_VIEW,
            1, TAB_VIEW,
            1, HI_VIEW,
            1, NL_VIEW /* e:12 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:18 */
        );
        // Force line wrap
        // SimpleValueNames.TEXT_LINE_WRAP
        pane.putClientProperty("text-line-wrap", "words"); // Force line wrap type
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE); // Will rebuild children
        docView.ensureLayoutValidForInitedChildren(); // acquires lock

        offset = 0;
        int removeLength = 1;
        cRegionStartOffset = 0;
        cRegionEndOffset = removeLength + 1; // 2
        docView.op.viewUpdates.extendCharRebuildRegion(OffsetRegion.create(doc, cRegionStartOffset, cRegionEndOffset));
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset,
/*matchOffset*/         offset + 2, // First HI_VIEW#1 removed and one char from second TAB_VIEW#2 removed by cRegion
/*endCreationOffset*/   docView.getParagraphView(0).getEndOffset() - removeLength,
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset,
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            2, TAB_VIEW,
            6, HI_VIEW,
            1, TAB_VIEW,
            1, HI_VIEW,
            1, NL_VIEW /* e:11 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:17 */
        );


        ViewUpdatesTesting.setTestValues();
    }
    
    public void testInsertAndRemoveNewline() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        int cRegionStartOffset;
        int cRegionEndOffset;
        
        offset = 0;
        text = "abc\nd\n";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(),
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:4 */,
            1, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            1, NL_VIEW /* e:7 */
        );

        offset = 5;
        text = "\n"; // Newline at end of second line
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 1,  // Rebuild prev view since insert at view boundary
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       offset - 1,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(),
/*amReusePIndex*/       1,
/*amReusePView*/        docView.getParagraphView(1),
/*amReuseLocalIndex*/   1
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:4 */,
            1, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            1, NL_VIEW /* e:7 */,
            1, NL_VIEW /* e:8 */
        );

        offset = 5;
        int removeLength = 1;
        cRegionStartOffset = offset;
        cRegionEndOffset = offset + removeLength;
        docView.op.viewUpdates.extendCharRebuildRegion(OffsetRegion.create(doc, cRegionStartOffset, cRegionEndOffset));
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ offset - 1,
/*matchOffset*/         offset, // First HI_VIEW#1 removed and one char from second TAB_VIEW#2 removed by cRegion
/*endCreationOffset*/   docView.getParagraphView(2).getEndOffset() - removeLength, // Remove from line end so plan rebuild till next line's end
/*bmReuseOffset*/       offset - 1,
/*bmReusePView*/        docView.getParagraphView(1),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset,
/*amReusePIndex*/       2,
/*amReusePView*/        docView.getParagraphView(2),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:4 */,
            1, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            1, NL_VIEW /* e:7 */
        );
        NbTestCase.assertEquals(3, docView.getViewCount());


        ViewUpdatesTesting.setTestValues();
    }
    
    public void testRemoveTrailingWhitespace() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        int cRegionStartOffset;
        int cRegionEndOffset;
        
        offset = 0;
        text = "abc  \nde  \nf  ";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(),
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            4, HI_VIEW,
            1, NL_VIEW /* e:11 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:15 */        );

        ParagraphView pView1 = docView.getParagraphView(1);
        int removeLength = 2;
        offset = pView1.getEndOffset() - 1 - removeLength; // Remove two last spaces in front of ending newline
        assert (offset == 8);
//        cRegionStartOffset = offset;
//        cRegionEndOffset = offset + removeLength;
//        docView.op.viewUpdates.extendCharRebuildRegion(OffsetRegion.create(doc, cRegionStartOffset, cRegionEndOffset));
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ pView1.getStartOffset(),
/*matchOffset*/         offset,
/*endCreationOffset*/   pView1.getEndOffset() - removeLength,
/*bmReuseOffset*/       pView1.getStartOffset(),
/*bmReusePView*/        pView1,
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset,
/*amReusePIndex*/       1,
/*amReusePView*/        pView1,
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            5, HI_VIEW,
            1, NL_VIEW /* e:6 */,
            2, HI_VIEW,
            1, NL_VIEW /* e:9 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:13 */
        );

        ParagraphView pView0 = docView.getParagraphView(0);
        removeLength = 2;
        offset = pView0.getEndOffset() - 1 - removeLength; // Remove two last spaces in front of ending newline
        assert (offset == 3);
        cRegionStartOffset = docView.getParagraphView(1).getEndOffset() - 1;
        cRegionEndOffset = cRegionStartOffset;
        docView.op.viewUpdates.extendCharRebuildRegion(OffsetRegion.create(doc, cRegionStartOffset, cRegionEndOffset));
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ pView0.getStartOffset(),
/*matchOffset*/         offset,
/*endCreationOffset*/   pView0.getEndOffset() - removeLength,
/*bmReuseOffset*/       pView0.getStartOffset(),
/*bmReusePView*/        pView0,
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset,
/*amReusePIndex*/       0,
/*amReusePView*/        pView0,
/*amReuseLocalIndex*/   1 // Could reuse NewlineView
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:4 */,
            2, HI_VIEW,
            1, NL_VIEW /* e:7 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:11 */
        );

        ViewUpdatesTesting.setTestValues();
    }

    public void testRemoveSpanningEndOfPartial() throws Exception {
        loggingOn();
        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        JEditorPane pane = ViewUpdatesTesting.createPane();
        Document doc = pane.getDocument();
        final DocumentView docView = DocumentView.get(pane);
        final PositionsBag highlights = ViewUpdatesTesting.getSingleHighlightingLayer(pane);
        int offset;
        String text;
        int cRegionStartOffset;
        int cRegionEndOffset;
        
        offset = 0;
        //      0123 456789 0123 456789 0123 4567 890
        text = "abc\ndefgh\nijk\nlmnop\nrst\nuvw\nxyz";
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ 0,
/*matchOffset*/         offset + text.length(),
/*endCreationOffset*/   offset + text.length() + 1, // includes '\n'
/*bmReuseOffset*/       0,
/*bmReusePView*/        docView.getParagraphView(0),
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       offset + text.length(),
/*amReusePIndex*/       0,
/*amReusePView*/        docView.getParagraphView(0),
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.insertString(offset, text, null);
        ViewUpdatesTesting.checkViews(docView, 0, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:4 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:10 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:14 */,
            5, HI_VIEW,
            1, NL_VIEW /* e:20 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:24 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:28 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:32 */
        );

        ViewUpdatesTesting.setTestValues(ViewUpdatesTesting.NO_OP_TEST_VALUE);
        ViewUpdatesTesting.setViewBounds(pane, 6, 19);
        pane.modelToView(7); // Init
        docView.ensureAllParagraphsChildrenAndLayoutValid();
        ViewUpdatesTesting.checkIntegrity(pane);
        ViewUpdatesTesting.checkViews(docView, 6, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:10 */,
            3, HI_VIEW,
            1, NL_VIEW /* e:14 */,
            5, HI_VIEW
        );

        ParagraphView pView1 = docView.getParagraphView(1);
        ParagraphView pView2 = docView.getParagraphView(2);
        int removeLength = 12;
        offset = pView1.getStartOffset() + 3; // Remove before NL
        assert (offset == 13) : "offset=" + offset;
//        cRegionStartOffset = offset;
//        cRegionEndOffset = offset + removeLength;
//        docView.op.viewUpdates.extendCharRebuildRegion(OffsetRegion.create(doc, cRegionStartOffset, cRegionEndOffset));
        ViewUpdatesTesting.setTestValues(
/*rebuildCause*/        ViewBuilder.RebuildCause.MOD_UPDATE,
/*createLocalViews*/    true,
/*startCreationOffset*/ pView1.getStartOffset(),
/*matchOffset*/         pView1.getEndOffset() - 1,
/*endCreationOffset*/   pView1.getEndOffset() - 1,
/*bmReuseOffset*/       pView1.getStartOffset(),
/*bmReusePView*/        pView1,
/*bmReuseLocalIndex*/   0,
/*amReuseOffset*/       Integer.MAX_VALUE,
/*amReusePIndex*/       3,
/*amReusePView*/        null,
/*amReuseLocalIndex*/   0 // Could reuse NewlineView
        );
        doc.remove(offset, removeLength);
        ViewUpdatesTesting.checkViews(docView, 6, -1,
            3, HI_VIEW,
            1, NL_VIEW /* e:4 */,
            3, HI_VIEW
        );

        ViewUpdatesTesting.checkIntegrity(pane);
        ViewUpdatesTesting.setTestValues();
    }

}
