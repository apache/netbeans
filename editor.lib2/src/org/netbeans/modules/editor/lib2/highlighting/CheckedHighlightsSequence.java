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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * Checks that highlights returned from the original HighlightsSequence are consistent. It
 * will skip any inconsistent highlight and will also clip the highlights to &lt;startOffset, endOffset&gt;.
 * The original code was contributed by Emilian Bold (emi@netbeans.org).
 *
 * @author Vita Stejskal
 */
public final class CheckedHighlightsSequence implements HighlightsSequence {

    private static final Logger LOG = Logger.getLogger(CheckedHighlightsSequence.class.getName());

    private final HighlightsSequence originalSeq;
    private final int startOffset;
    private final int endOffset;
    private String containerDebugId = null;
    private int start = -1;
    private int end = -1;

    /**
     *
     * @param seq
     * @param startOffset
     * @param endOffset
     * @param containerDebugId
     * @deprecated Use the other constructor and setContainerDebugId
     */
    public CheckedHighlightsSequence(HighlightsSequence seq, int startOffset, int endOffset, String containerDebugId) {
        assert seq != null : "seq must not be null"; //NOI18N
        assert 0 <= startOffset : "startOffset must be greater than or equal to zero"; //NOI18N
        assert 0 <= endOffset : "endOffset must be greater than or equal to zero"; //NOI18N
        assert startOffset <= endOffset : "startOffset must be less than or equal to endOffset; " + //NOI18N
            "startOffset = " + startOffset + " endOffset = " + endOffset; //NOI18N

        this.originalSeq = seq;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        if (LOG.isLoggable(Level.FINE)) {
            this.containerDebugId = containerDebugId != null ?
                containerDebugId :
                seq.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(seq)); //NOI18N
        }
    }

    public CheckedHighlightsSequence(HighlightsSequence seq, int startOffset, int endOffset) {
        this(seq, startOffset, endOffset, null);
    }
    
    public void setContainerDebugId(String containerDebugId) {
        this.containerDebugId = containerDebugId;
    }

    public @Override boolean moveNext() {
        boolean hasNext = originalSeq.moveNext();
        //XXX: the problem here is if the sequence we are wrapping is sorted by startOffset.
        // In practice I think it is, but I cannot afford to make that assumption now.
        // So I have to check both boundaries, not only start and end offset separately.
        boolean retry = hasNext;
        while (retry) {
            start = originalSeq.getStartOffset();
            end = originalSeq.getEndOffset();

            if (start > end) {
                // this highlight is invalid
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, containerDebugId
                        + " supplied invalid highlight " + CompoundHighlightsContainer.dumpHighlight(originalSeq, null) //NOI18N
                        + ", requested range <" + startOffset + ", " + endOffset + ">." //NOI18N
                        + " Highlight ignored."); //NOI18N
                }

                retry = hasNext = originalSeq.moveNext();
            } else if (start > endOffset || end < startOffset) {
                // this highlight is totally outside our rage, there is nothing we can clip, we must retry
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, containerDebugId
                        + " supplied highlight " + CompoundHighlightsContainer.dumpHighlight(originalSeq, null) //NOI18N
                        + ", which is outside of the requested range <" + startOffset + ", " + endOffset + ">." //NOI18N
                        + " Highlight skipped."); //NOI18N
                }

                retry = hasNext = originalSeq.moveNext();
            } else if (originalSeq.getAttributes() == null) {
                // this highlight has no attributes
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, containerDebugId
                        + " supplied highlight " + CompoundHighlightsContainer.dumpHighlight(originalSeq, null) //NOI18N
                        + ", which has null attributes <" + startOffset + ", " + endOffset + ">." //NOI18N
                        + " Highlight skipped."); //NOI18N
                }

                retry = hasNext = originalSeq.moveNext();
            } else {
                // highlight appears ok
                retry = false;
            }
        }

        if (hasNext) {
            // clip the highlight if neccessary
            boolean unclipped = false;
            if (start < startOffset) {
                start = startOffset;
                unclipped = true;
            }
            if (end > endOffset) {
                end = endOffset;
                unclipped = true;
            }
            if (unclipped && LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, containerDebugId
                    + " supplied unclipped highlight " + CompoundHighlightsContainer.dumpHighlight(originalSeq, null) //NOI18N
                    + ", requested range <" + startOffset + ", " + endOffset + ">." //NOI18N
                    + " Highlight clipped."); //NOI18N
            }
        }

        return hasNext;
    }

    public @Override int getStartOffset() {
        return start;
    }

    public @Override int getEndOffset() {
        return end;
    }

    public @Override AttributeSet getAttributes() {
        return originalSeq.getAttributes();
    }
}
