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

import java.awt.Font;
import java.awt.font.TextLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.View;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.DirectMergeContainer;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsReader;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.openide.util.WeakListeners;

/**
 * View factory returning highlights views. It is specific in that it always
 * covers the whole document area by views even if there are no particular highlights
 * <br>
 * Currently the factory coalesces highlights change requests from non-AWT thread.
 *
 * @author Miloslav Metelka
 */

public final class HighlightsViewFactory extends EditorViewFactory implements HighlightsChangeListener, ChangeListener {
    
    /**
     * Length of the highlights view (text layout) above which the infrastructure will search
     * for a whitespace in the text and if it finds one then it will end and create the view
     * (even though the text layout could continue since the text attributes would allow it).
     */
    private static final int SPLIT_TEXT_LAYOUT_LENGTH = 1024;

    /**
     * Maximum Length of the highlights view (text layout). When reached the infrastructure will
     * create the view regardless whitespace occurrence and whether text attributes would allow
     * the view to continue.
     */
    private static final int MAX_TEXT_LAYOUT_LENGTH = SPLIT_TEXT_LAYOUT_LENGTH + 256;
    
    /**
     * When view is considered long (it has a minimum length SPLIT_TEXT_LAYOUT_LENGTH - MODIFICATION_TOLERANCE)
     * then the infrastructure will attempt to end current long view creation
     * at a given nextOrigViewOffset parameter in order to save views creation and reuse
     * existing text layouts (and their slit text layouts for line wrapping).
     * <br>
     * The user would have to insert or remove LONG_VIEW_TOLERANCE of characters into long view
     * in order to force the factory to not match to the given nextOrigViewOffset.
     */
    private static final int MODIFICATION_TOLERANCE = 100;

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsViewFactory.class.getName());
    
    private static final int UNKNOWN_CHAR_TYPE = 0;
    private static final int LTR_CHAR_TYPE = 1;
    private static final int RTL_CHAR_TYPE = 2;
    private static final int TAB_CHAR_TYPE = 3;
    
    private final HighlightingManager highlightingManager;

    private HighlightsContainer highlightsContainer;
    
    private HighlightsContainer paintHighlightsContainer;
    
    private HighlightsChangeListener weakHL;
    
    private HighlightsChangeListener paintWeakHL;

    private CharSequence docText;

    private Element lineElementRoot;

    private int lineIndex;
    
    private int lineEndOffset;
    
    private HighlightsReader highlightsReader;
    
    private Font defaultFont;
    
    /**
     * Offset where either '\t' occurs or where boundary between LTR and RTL text is located.
     */
    private int nextTabOrRTLOffset;

    /**
     * Char type below nextTabOrRTLOffset (updated in createView() so it's actual
     * from created view's startOffset till nextTabOrRTLOffset.
     */
    private int charType;
    
    /**
     * Char type of character right at nextTabOrRTLOffset.
     */
    private int nextCharType;
    
    private boolean createViews;
    
    private int usageCount = 0; // Avoid nested use of the factory
    
    public HighlightsViewFactory(View documentView) {
        super(documentView);
        highlightingManager = HighlightingManager.getInstance(textComponent());
        highlightingManager.addChangeListener(this);
        updateHighlightsContainer();
    }

    private void updateHighlightsContainer() {
        if (highlightsContainer != null && weakHL != null) {
            highlightsContainer.removeHighlightsChangeListener(weakHL);
            paintHighlightsContainer.removeHighlightsChangeListener(paintWeakHL);
            weakHL = null;
            paintWeakHL = null;
        }
        highlightsContainer = highlightingManager.getBottomHighlights();
        highlightsContainer.addHighlightsChangeListener(weakHL = WeakListeners.create(
                HighlightsChangeListener.class, this, highlightsContainer));
        paintHighlightsContainer = highlightingManager.getTopHighlights();
        paintHighlightsContainer.addHighlightsChangeListener(paintWeakHL = WeakListeners.create(
                HighlightsChangeListener.class, this, paintHighlightsContainer));
    }

    @Override
    public void restart(int startOffset, int endOffset, boolean createViews) {
        if (usageCount != 0) {
            throw new IllegalStateException("Race condition: usageCount = " + usageCount); // NOI18N
        }
        usageCount++;
        this.createViews = createViews;
        docText = DocumentUtilities.getText(document());
        lineElementRoot = document().getDefaultRootElement();
        assert (lineElementRoot != null) : "lineElementRoot is null."; // NOI18N
        lineIndex = lineElementRoot.getElementIndex(startOffset);
        lineEndOffset = lineElementRoot.getElement(lineIndex).getEndOffset();
        defaultFont = textComponent().getFont();
        nextTabOrRTLOffset = -1;
        if (createViews) {
            highlightsReader = new HighlightsReader(highlightsContainer, startOffset, endOffset);
            highlightsReader.readUntil(endOffset);
        }
    }

    @Override
    public int nextViewStartOffset(int offset) {
        // This layer returns a view for any given offset
        // since it must cover all the offset space with views.
        return offset;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset, boolean forcedLimit,
    EditorView origView, int nextOrigViewOffset) {
        assert (startOffset >= 0) : "Invalid startOffset=" + startOffset + " < 0\nHVF: " + this; // NOI18N
        assert (startOffset < limitOffset) : "startOffset=" + startOffset + // NOI18N
                " >= limitOffset=" + limitOffset + "\nHVF: " + this; // NOI18N
        // Possibly update lineEndOffset since updateHighlight() will read till it
        updateLineEndOffset(startOffset);
        HighlightsList hList = highlightsReader.highlightsList();
        if (hList.startOffset() < startOffset) {
            hList.skip(startOffset);
        }
        if (startOffset == lineEndOffset - 1) {
            AttributeSet attrs = hList.cutSingleChar();
            return wrapWithPrependedText(new NewlineView(attrs), attrs);
        } else { // Regular view with possible highlight(s) or tab view
            updateTabsAndHighlightsAndRTL(startOffset);
            if (charType == TAB_CHAR_TYPE) {
                int tabsEndOffset = nextTabOrRTLOffset; 
                AttributeSet attrs;
                if (limitOffset > tabsEndOffset) {
                    limitOffset = tabsEndOffset;
                }
                attrs = hList.cut(limitOffset);
                return wrapWithPrependedText(new TabView(limitOffset - startOffset, attrs), attrs);

            } else { // Create regular view with either LTR or RTL text
                limitOffset = Math.min(limitOffset, nextTabOrRTLOffset); // nextTabOrRTLOffset < lineEndOffset 
                int wsEndOffset = limitOffset;
                if (limitOffset - startOffset > SPLIT_TEXT_LAYOUT_LENGTH - MODIFICATION_TOLERANCE) {
                    if (nextOrigViewOffset <= limitOffset &&
                        nextOrigViewOffset - startOffset >= SPLIT_TEXT_LAYOUT_LENGTH - MODIFICATION_TOLERANCE &&
                        nextOrigViewOffset - startOffset <= MAX_TEXT_LAYOUT_LENGTH + MODIFICATION_TOLERANCE)
                    { // Stick to existing bounds if possible
                        limitOffset = nextOrigViewOffset;
                        wsEndOffset = nextOrigViewOffset;
                    } else {
                        limitOffset = Math.min(limitOffset, startOffset + MAX_TEXT_LAYOUT_LENGTH);
                        wsEndOffset = Math.min(wsEndOffset, startOffset + SPLIT_TEXT_LAYOUT_LENGTH);
                    }
                            
                }
                AttributeSet attrs = hList.cutSameFont(defaultFont, limitOffset, wsEndOffset, docText);
                int length = hList.startOffset() - startOffset;
                EditorView view = wrapWithPrependedText(new HighlightsView(length, attrs), attrs);
                EditorView origViewUnwrapped = origView instanceof PrependedTextView ? ((PrependedTextView) origView).getDelegate() : origView;
                if (origViewUnwrapped != null && origViewUnwrapped.getClass() == HighlightsView.class && origViewUnwrapped.getLength() == length) {
                    HighlightsView origHView = (HighlightsView) origViewUnwrapped;
                    TextLayout origTextLayout = origHView.getTextLayout();
                    if (origTextLayout != null) {
                        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
                            String origText = documentView().getTextLayoutVerifier().get(origTextLayout);
                            if (origText != null) {
                                CharSequence text = docText.subSequence(startOffset, startOffset + length);
                                if (!CharSequenceUtilities.textEquals(text, origText)) {
                                    throw new IllegalStateException("TextLayout text differs:\n current:" + // NOI18N
                                            CharSequenceUtilities.debugText(text) + "\n!=\n" +
                                            CharSequenceUtilities.debugText(origText) + "\n");
                                }
                            }
                        }
                        Font font = ViewUtils.getFont(attrs, defaultFont);
                        Font origFont = ViewUtils.getFont(origViewUnwrapped.getAttributes(), defaultFont);
                        if (font != null && font.equals(origFont)) {
                            float origWidth = origHView.getWidth();
                            HighlightsView hv = (HighlightsView) (view instanceof PrependedTextView ? ((PrependedTextView) view).getDelegate() : view);
                            hv.setTextLayout(origTextLayout, origWidth);
                            hv.setBreakInfo(origHView.getBreakInfo());
                            ViewStats.incrementTextLayoutReused(length);
                        }
                    }
                }
                return view;
            }
        }
    }

    private @NonNull EditorView wrapWithPrependedText(@NonNull EditorView origView, @NullAllowed AttributeSet attrs) {
        if (attrs != null && attrs.getAttribute(ViewUtils.KEY_VIRTUAL_TEXT_PREPEND) instanceof String) {
            return new PrependedTextView(documentView().op, attrs, origView);
        }

        return origView;
    }

    private void updateTabsAndHighlightsAndRTL(int offset) {
        if (offset >= nextTabOrRTLOffset) { // Update nextTabOrRTLOffset
            // Determine situation right at offset
            if (nextCharType == UNKNOWN_CHAR_TYPE || offset > nextTabOrRTLOffset) {
                char ch = docText.charAt(offset);
                charType = getCharType(ch);
            } else { // Reuse nextCharType
                charType = nextCharType;
            }

            for (nextTabOrRTLOffset = offset + 1; nextTabOrRTLOffset < lineEndOffset - 1; nextTabOrRTLOffset++) {
                char ch = docText.charAt(nextTabOrRTLOffset);
                nextCharType = getCharType(ch);
                if (charType == RTL_CHAR_TYPE && Character.isWhitespace(ch)) {
                    nextCharType = RTL_CHAR_TYPE; // RTL followed by WS -> retain RTL
                }
                if (nextCharType != charType) {
                    break;
                }
            }
        }
    }
    
    private int getCharType(char ch) {
        if (ch == '\t') {
            return TAB_CHAR_TYPE;
        } else {
            byte dir = Character.getDirectionality(ch);
            switch (dir) {
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
                case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                    return RTL_CHAR_TYPE;
                default:
                    return LTR_CHAR_TYPE;
            }
        }
    }
    
    @Override
    public int viewEndOffset(int startOffset, int limitOffset, boolean forcedLimit) {
        updateLineEndOffset(startOffset);
        return Math.min(lineEndOffset, limitOffset);
    }

    @Override
    public void continueCreation(int startOffset, int endOffset) {
        if (createViews) {
            highlightsReader = new HighlightsReader(highlightsContainer, startOffset, endOffset);
            highlightsReader.readUntil(endOffset);
        }
    }

    private void updateLineEndOffset(int offset) {
        // Several lines may be skipped at once in case there's e.g. a collapsed fold (FoldView gets created)
        while (offset >= lineEndOffset) {
            lineIndex++;
            Element line = lineElementRoot.getElement(lineIndex);
            lineEndOffset = line.getEndOffset();
        }
    }

    @Override
    public void finishCreation() {
        highlightsReader = null;
        docText = null;
        lineElementRoot = null;
        lineIndex = -1;
        lineEndOffset = -1;
        usageCount--;
    }

    @Override
    public void highlightChanged(final HighlightsChangeEvent evt) {
        if (isReleased()) {
            return;
        }
        // Since still many highlighting layers fire changes without document lock acquired
        // do an extra read lock so that view hierarchy surely operates under document lock
        document().render(new Runnable() {
            @Override
            public void run() {
                int startOffset = evt.getStartOffset();
                int endOffset = evt.getEndOffset();
                if (evt.getSource() == highlightsContainer) {
                    if (usageCount != 0) { // When views are being created => notify stale creation
                        notifyStaleCreation();
                    }
                    int docTextLength = document().getLength() + 1;
                    assert (startOffset >= 0) : "startOffset=" + startOffset + " < 0"; // NOI18N
                    assert (endOffset >= 0) : "startOffset=" + endOffset + " < 0"; // NOI18N
                    startOffset = Math.min(startOffset, docTextLength);
                    endOffset = Math.min(endOffset, docTextLength);
                    if (ViewHierarchyImpl.CHANGE_LOG.isLoggable(Level.FINE)) {
                        HighlightsChangeEvent layerEvent = (highlightsContainer instanceof DirectMergeContainer)
                                ? ((DirectMergeContainer) highlightsContainer).layerEvent()
                                : null;
                        String layerInfo = (layerEvent != null)
                                ? " " + highlightingManager.findLayer((HighlightsContainer)layerEvent.getSource()) // NOI18N
                                : ""; // NOI18N
                        ViewUtils.log(ViewHierarchyImpl.CHANGE_LOG, "VIEW-REBUILD-HC:<" + // NOI18N
                                startOffset + "," + endOffset + ">" + layerInfo + "\n"); // NOI18N
                    }

                    if (startOffset <= endOffset) { // May possibly be == e.g. for cut-line action
                        fireEvent(EditorViewFactoryChange.createList(startOffset, endOffset,
                                EditorViewFactoryChange.Type.CHARACTER_CHANGE));
                    }

                } else if (evt.getSource() == paintHighlightsContainer) { // Paint highlights change
                    if (ViewHierarchyImpl.CHANGE_LOG.isLoggable(Level.FINE)) {
                        HighlightsChangeEvent layerEvent = (paintHighlightsContainer instanceof DirectMergeContainer)
                                ? ((DirectMergeContainer) paintHighlightsContainer).layerEvent()
                                : null;
                        String layerInfo = (layerEvent != null)
                                ? " " + highlightingManager.findLayer((HighlightsContainer) layerEvent.getSource()) // NOI18N
                                : ""; // NOI18N
                        ViewUtils.log(ViewHierarchyImpl.CHANGE_LOG, "REPAINT-HC:<" + // NOI18N
                                startOffset + "," + endOffset + ">" + layerInfo + "\n"); // NOI18N
                    }

                    offsetRepaint(startOffset, endOffset);
                } // else: can happen when updateHighlightsContainer() being called => ignore
            }
        });
    }

    @Override
    protected void released() {
        highlightingManager.removeChangeListener(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("lineIndex=").append(lineIndex). // NOI18N
                append(", lineEndOffset=").append(lineEndOffset). // NOI18N
                append(", charType=").append(charType). // NOI18N
                append(", nextTabOrRTLOffset=").append(nextTabOrRTLOffset). // NOI18N
                append(", nextCharType=").append(nextCharType); // NOI18N
        sb.append(", ").append(super.toString());
        return sb.toString();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (isReleased()) {
            return;
        }
        notifyStaleCreation();
        updateHighlightsContainer();
        fireEvent(EditorViewFactoryChange.createList(0, document().getLength() + 1,
                EditorViewFactoryChange.Type.REBUILD));
    }

    public static final class HighlightsFactory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new HighlightsViewFactory(documentView);
        }

        @Override
        public int weight() {
            return 0;
        }

    }

}
