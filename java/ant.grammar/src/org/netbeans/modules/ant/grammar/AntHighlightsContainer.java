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

package org.netbeans.modules.ant.grammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Exceptions;

class AntHighlightsContainer extends AbstractHighlightsContainer {
    
    private static final Pattern EXPR = Pattern.compile("(?<![$])[$@][{][^{}]+[}]");

    private final AbstractDocument doc;
    private final AttributeSet attrs;

    public AntHighlightsContainer(AbstractDocument doc, AttributeSet attrs) {
        this.doc = doc;
        this.attrs = attrs;
    }
    
    public @Override HighlightsSequence getHighlights(final int startOffset, final int endOffset) {
        return new HighlightsSequence() {
            int start, end;
            final Matcher m;
            final int scanStart;
            final int _endOffset;
            {
                scanStart = doc.getParagraphElement(startOffset).getStartOffset();
                int scanEnd;
                if (endOffset == Integer.MAX_VALUE) {
                    scanEnd = _endOffset = doc.getLength();
                } else {
                    _endOffset = endOffset;
                    scanEnd = doc.getParagraphElement(endOffset).getEndOffset();
                }
//                System.err.println("start/endOffset=" + startOffset + "/" + endOffset + " scanStart/End=" + scanStart + "/" + scanEnd);
                // XXX could use a custom CharSequence that can take advantage of partialReturn
                Segment text = new Segment();
                try {
                    doc.getText(scanStart, scanEnd - scanStart, text);
                } catch (BadLocationException x) {
                    Exceptions.printStackTrace(x);
                }
                m = EXPR.matcher(text);
            }
            public @Override boolean moveNext() {
                if (!m.find()) {
                    return false;
                }
                start = scanStart + m.start();
                end = scanStart + m.end();
//                System.err.println("start=" + start + " end=" + end);
                if (end < startOffset) {
                    return moveNext();
                } else if (end == startOffset) {
                    end++;
                    return moveNext();
                } else if (start < startOffset) {
                    start = startOffset;
                } else if (start >= _endOffset) {
                    return false;
                } else if (end > _endOffset) {
                    end = _endOffset;
                }
                assert start >= startOffset;
                if (end > endOffset) {
                    // #189668; happens when debugger tooltip created while hovering over var.
                    return false;
                }
                assert end > start;
                return true;
            }
            public @Override int getStartOffset() {
                return start;
            }
            public @Override int getEndOffset() {
                return end;
            }
            public @Override AttributeSet getAttributes() {
                return attrs;
            }
        };
    }
    
}
