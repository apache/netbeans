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

package org.netbeans.modules.editor.lib2.highlighting;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Highlighting of indentation and trailing whitespace.
 * <br>
 * If there's no non-WS text on the line the whitespace is treated as trailing.
 *
 * @author Miloslav Metelka
 */
public class WhitespaceHighlighting extends AbstractHighlightsContainer
implements ReleasableHighlightsContainer, DocumentListener, LookupListener
{
    
    static final int FIRST_CHAR = 0; // First character on line
    static final int WS_BEFORE = 1; // Whitespace only till line's start
    static final int NON_WS_BEFORE = 2; // Non whitespace found when iterating back on line

    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.WhitespaceHighlighting"; // NOI18N
    
    private static final String INDENT_ATTRS_FCS_NAME = "indent-whitespace"; // NOI18N

    private static final String TRAILING_ATTRS_FCS_NAME = "trailing-whitespace"; // NOI18N
    
    private final Document doc;
    
    private final CharSequence docText;
    
    private AttributeSet indentAttrs;
    
    private AttributeSet trailingAttrs;
    
    private boolean active;
    
    private boolean customAttrs;
    
    private Lookup.Result<FontColorSettings> result;

    WhitespaceHighlighting(JTextComponent c) {
        // Upon doc change all layers become recreated by infrastructure (no need to listen for doc change)
        this.doc = c.getDocument();
        this.docText = DocumentUtilities.getText(doc);
        doc.addDocumentListener(this);
        String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
        if (mimeType == null) {
            mimeType = "";
        }
        Lookup lookup = MimeLookup.getLookup(mimeType);
        result = lookup.lookupResult(FontColorSettings.class);
        result.addLookupListener(WeakListeners.create(LookupListener.class, this, result));
        resultChanged(null); // Update attrs
    }
        
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return active ? new HS(startOffset, endOffset) : HighlightsSequence.EMPTY;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (active) {
            // Compute changed area.
            // By inserting non-WS:
            // 1) a trailing WS may become indent-WS on WS-only line.
//            String modText = DocumentUtilities.getModificationText(e);
            int offset = e.getOffset();
            int len = e.getLength();
            int startChangeOffset = -1;
            int endChangeOffset = offset + len;
            boolean checkForWSAboveModText = false;
            for (int i = offset - 1; i >= 0; i--) {
                char ch = docText.charAt(i);
                if (ch == '\n') {
                    startChangeOffset = i + 1;
                    break;
                }
                if (!Character.isWhitespace(ch)) {
                    startChangeOffset = i + 1;
                    break;
                }
            }
            if (startChangeOffset == -1) {
                startChangeOffset = 0;
                checkForWSAboveModText = true;
            }
            // For simplicity temporarily just always scan for WS above
            int docTextLen = docText.length();
            for (int i = endChangeOffset; i < docTextLen; i++) {
                char ch = docText.charAt(i);
                if (ch == '\n') {
                    endChangeOffset = i;
                    break;
                }
                if (!Character.isWhitespace(ch)) {
                    endChangeOffset = i;
                    break;
                }
            }
            fireHighlightsChange(startChangeOffset, endChangeOffset);
        }
    }
    
    private int lineStartOffset(int offset) {
        while (offset > 0) {
            char ch = docText.charAt(--offset);
            if (ch == '\n') {
                return offset + 1;
            }
        }
        return 0;
    }
    
    private int beforeOffsetState(int offset) {
        int i;
        for (i = offset - 1; i >= 0; i--) {
            char ch = docText.charAt(i);
            if (ch == '\n') {
                break;
            }
            if (!Character.isWhitespace(ch)) {
                return NON_WS_BEFORE;
            }
        }
        return (i == offset - 1) ? FIRST_CHAR : WS_BEFORE;
    }
    
    private boolean isWSTillFirstNL(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                break;
            }
            if (!Character.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void removeUpdate(DocumentEvent e) {
        if (active) {
            int offset = e.getOffset();
            int len = e.getLength();
            int startChangeOffset = -1;
            int endChangeOffset = offset;
            for (int i = offset - 1; i >= 0; i--) {
                char ch = docText.charAt(i);
                if (ch == '\n') {
                    startChangeOffset = i + 1;
                    break;
                }
                if (!Character.isWhitespace(ch)) {
                    startChangeOffset = i + 1;
                    break;
                }
            }
            if (startChangeOffset == -1) {
                startChangeOffset = 0;
            }
            // For simplicity temporarily just always scan for WS above
            int docTextLen = docText.length();
            for (int i = offset; i < docTextLen; i++) {
                char ch = docText.charAt(i);
                if (ch == '\n') {
                    endChangeOffset = i;
                    break;
                }
                if (!Character.isWhitespace(ch)) {
                    endChangeOffset = i;
                    break;
                }
            }
            fireHighlightsChange(startChangeOffset, endChangeOffset);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
    
    private void assignAttrs(AttributeSet indentAttrs, AttributeSet trailingAttrs) {
        synchronized (this) {
            this.indentAttrs = indentAttrs;
            this.trailingAttrs = trailingAttrs;
            this.active = indentAttrs != null && trailingAttrs != null;
        }
    }
    
    void testInitEnv(AttributeSet indentAttrs, AttributeSet trailingAttrs) {
        customAttrs = true;
        assignAttrs(indentAttrs, trailingAttrs);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        FontColorSettings fcs = result.allInstances().iterator().next();
        synchronized (this) {
            if (!customAttrs) {
                assignAttrs(
                        fcs.getFontColors(INDENT_ATTRS_FCS_NAME),
                        fcs.getFontColors(TRAILING_ATTRS_FCS_NAME));
            }
        }
    }

    @Override
    public void released() {
        doc.removeDocumentListener(this);
    }

    private final class HS implements HighlightsSequence {
        
        private int startOffset;
        
        private int endOffset;
        
        private int hltStartOffset;
        
        private int hltEndOffset;
        
        private AttributeSet hltAttrs;
        
        private int state;
        
        public HS(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.hltStartOffset = -1;
            this.hltEndOffset = startOffset;
        }
        
        @Override
        public boolean moveNext() {
            int docTextLen = docText.length();
            if (endOffset > docTextLen) {
                endOffset = docTextLen;
            }
            if (hltEndOffset >= endOffset) {
                return false;
            }
            if (hltStartOffset == -1) { // Init
                state = beforeOffsetState(startOffset);
            }
            hltStartOffset = hltEndOffset;
            // hltStartOffset < endOffset (see above)
            do {
                switch (state) {
                    case FIRST_CHAR:
                    case WS_BEFORE:
                        for (int i = hltStartOffset; i < endOffset; i++) {
                            char ch = docText.charAt(i);
                            if (ch == '\n') {
                                if (i != hltStartOffset) { // Existing highlight
                                    hltEndOffset = i;
                                    // WS till '\n' => trailing WS
                                    hltAttrs = trailingAttrs;
                                    if (hltAttrs != null) {
                                        return true;
                                    }
                                } else { // Just newline => skip
                                    hltEndOffset = i + 1;
                                    state = FIRST_CHAR;
                                }
                                break;
                            }
                            if (!Character.isWhitespace(ch)) {
                                state = NON_WS_BEFORE;
                                hltEndOffset = i;
                                if (i != hltStartOffset) { // Non-empty
                                    hltAttrs = indentAttrs;
                                    if (hltAttrs != null) {
                                        return true;
                                    }
                                }
                                break;
                            } else {
                                state = WS_BEFORE; // Might be FIRST_CHAR state originally
                            }
                        }

                        if (state == WS_BEFORE) { // endOffset before line's end; endOffset > hltStartOffset
                            // Scan line till end to resolve whether trailing WS or indent WS
                            hltEndOffset = endOffset;
                            for (int i = endOffset; i < docTextLen; i++) {
                                char ch = docText.charAt(i);
                                if (ch == '\n') {
                                    // WS till '\n' => trailing WS
                                    hltAttrs = trailingAttrs;
                                    if (hltAttrs != null) {
                                        return true;
                                    }
                                    break;
                                }
                                if (!Character.isWhitespace(ch)) {
                                    hltAttrs = indentAttrs;
                                    if (hltAttrs != null) {
                                        return true;
                                    }
                                    break;
                                }
                            }
                        }
                        break;

                    case NON_WS_BEFORE:
                        for (int i = hltStartOffset; i < docTextLen; i++) {
                            char ch = docText.charAt(i);
                            if (ch == '\n') {
                                if (i != hltStartOffset) { // Non-empty trailing WS highlight
                                    hltEndOffset = Math.min(i, endOffset);
                                    hltAttrs = trailingAttrs;
                                    if (hltAttrs != null) {
                                        return true;
                                    }
                                } else { // Just '\n' -> skip
                                    hltEndOffset = Math.min(i + 1, endOffset);
                                    state = FIRST_CHAR;
                                }
                                break;
                            }
                            if (!Character.isWhitespace(ch)) {
                                // Skip highlight till next char
                                hltStartOffset = i + 1;
                                hltEndOffset = hltStartOffset;
                                if (i >= endOffset) {
                                    break; // No more highlights till endOffset
                                }
                            } else {
                                // Possible hlt or skip area will include this char
                                hltEndOffset = i + 1; // Prevent infinite cycle when reaching docTextLen
                            }
                        }
                        break;

                    default:
                        throw new IllegalStateException("Unknown state=" + state); // NOI18N
                }

                hltStartOffset = hltEndOffset; // Skip the explored area
            } while (hltStartOffset < endOffset);

            return false;
        }

        @Override
        public int getStartOffset() {
            return hltStartOffset;
        }

        @Override
        public int getEndOffset() {
            return hltEndOffset;
        }

        @Override
        public AttributeSet getAttributes() {
            return hltAttrs;
        }
        
        
    }

}

