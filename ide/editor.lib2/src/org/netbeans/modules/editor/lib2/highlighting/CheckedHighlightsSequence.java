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
    @Deprecated
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
