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

package org.netbeans.modules.editor.lib2.highlighting;

import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * List of highlights that can dynamically add/remove existing highlights fed from a highlights sequence.
 *
 * @author Miloslav Metelka
 */
public final class HighlightsReader {
    
    private final HighlightsList highlightsList;

    private final CoveringHighlightsSequence cHighlightsSequence;
    
    private final int endOffset;
    
    public HighlightsReader(HighlightsContainer highlightsContainer, int startOffset, int endOffset) {
        // Expecting that highlights are either bottomHighlights or topHighlights of HighlightingManager which are both
        // DirectMergeContainer instances
        this.cHighlightsSequence = (CoveringHighlightsSequence) highlightsContainer.getHighlights(startOffset, endOffset);
        assert cHighlightsSequence.isCovering() : "Non-covering HS=" + cHighlightsSequence;
        this.highlightsList = new HighlightsList(startOffset);
        this.endOffset = endOffset;
    }
    
    public HighlightsSequence highlightsSequence() {
        return cHighlightsSequence;
    }
    
    public HighlightsList highlightsList() {
        return highlightsList;
    }
    
    public void readUntil(int offset) {
        int hlEndOffset = highlightsList.endOffset();
        int hlEndSplitOffset = highlightsList.endSplitOffset();
        while (cHighlightsSequence.moveNext()) {
            int hlStartOffset = cHighlightsSequence.getStartOffset();
            int hlStartSplitOffset = cHighlightsSequence.getStartSplitOffset();
            if (hlStartOffset > hlEndOffset || hlStartOffset == hlEndOffset && hlStartSplitOffset > hlEndSplitOffset) {
                HighlightItem fillItem;
                if (hlStartSplitOffset != 0) {
                    fillItem = new SplitOffsetHighlightItem(hlStartOffset, hlStartSplitOffset, null);
                } else {
                    fillItem = new HighlightItem(hlStartOffset, null);
                }
                highlightsList.add(fillItem);
            }
            hlEndOffset = cHighlightsSequence.getEndOffset();
            hlEndSplitOffset = cHighlightsSequence.getEndSplitOffset();
            HighlightItem item;
            AttributeSet attrs = cHighlightsSequence.getAttributes();
            if (hlEndSplitOffset != 0) {
                item = new SplitOffsetHighlightItem(hlEndOffset, hlEndSplitOffset, attrs);
            } else {
                item = new HighlightItem(hlEndOffset, attrs);
            }
            highlightsList.add(item);
            if (hlEndOffset >= offset) {
                return;
            }
        }
        // Highlights from highlightsSequence end below endOffset
        if (hlEndOffset < endOffset) { // Add extra highlight till endOffset
            highlightsList.add(new HighlightItem(endOffset, null));
        }
    }

}
