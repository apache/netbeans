/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
