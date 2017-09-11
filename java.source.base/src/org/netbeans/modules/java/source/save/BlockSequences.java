/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.save;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.lexer.TokenSequence;

import static org.netbeans.modules.java.source.save.PositionEstimator.nonRelevant;

/**
 * Provides information on sequences of writable and readonly text.
 * Extracts guarded blocks from the document, and can identify the end of a sequence either
 * guarded or not guarded and possibly to determine the (user) writability.
 * 
 * @author sdedic
 */
public final class BlockSequences {
    private final   TokenSequence seq;
    private final   Document    doc;
    /**
     * Start and end offsets of guarded blocks, 
     */
    private int[]   boundOffsets;
    
    private int     max;
    
    private int len;

    BlockSequences(TokenSequence seq, Document doc, int textLen) {
        this.doc = doc;
        this.seq = seq;
        this.len = textLen;
        initialize();
    }
    
    /**
     * Enumerates starting offsets of individual text sections
     * @return  Iterator that produces offsets of section boundaries
     */
    public Iterator<Integer> getBoundaries() {
        return boundOffsets == null ? 
                Collections.<Integer>emptyList().iterator() :
                new Iterator<Integer>() {
                    int p = 0;
                    
                    @Override
                    public boolean hasNext() {
                        return p < max;
                    }

                    @Override
                    public Integer next() {
                        return boundOffsets[p++];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported");
                    }
        };
                
    }
    
    /**
     * Checks if a particular offset is writable
     * 
     * @param anchor offset to check
     * @return true, if the offset can be written to
     */
    public boolean isWritable(int anchor) {
        if (boundOffsets == null) {
            return true;
        }
        int index = findSectionIndex(anchor);
        int s = boundOffsets[index];
        if (anchor >= s) {
            int e = boundOffsets[index];
            return anchor >= e;
        }
        return true;
    }
    
    /**
     * Finds the appropriate writable position given the anchor pos.
     * If the anchor position is right at the start of a section, position - 1 is returned.
     * This assumes that document is divided into sections that span whole lines; the returned position
     * is then before the preceding newline and writable.
     * <p/>
     * If the anchor is outside readonly section, the anchor pos itself is returned.
     * <p/>
     * If the anchor is inside the section, the position after the section is returned IF the
     * token starting at `anchor' leaks outside the section, or the anchor points to a whitespace+comments
     * that extend past the section.
     * 
     * @param anchor
     * @return writable position suitable for the anchor
     */
    @SuppressWarnings("empty-statement")
    public int findNextWritablePos(int anchor) {
        if (boundOffsets == null) {
            return anchor;
        }
        int index = findSectionIndex(anchor);
        int s = boundOffsets[index];
        
        if (anchor < s) {
            return anchor;
        } else if (anchor == s) {
            return anchor - 1;
        }
        int e = boundOffsets[index];
        if (anchor > e) {
            return anchor;
        }
        
        seq.move(anchor);
        while (seq.moveNext() && nonRelevant.contains(seq.token().id())) ;
        if (seq.offset() < e) {
            return anchor;
        } else {
            return e;
        }
    }
    
    /**
     * Returns boundaries of section that contains the anchor.
     * Returns 2-item array that contain start-end positions of the section.
     * There's an implicit section that contains the head / tail of the document 
     * (before/after first/last defined section) or the whole document, if no
     * sections are defined.
     * 
     * @param anchor the inspected position
     * @return start & end of the containing section
     */
    public int[] getContainingSection(int anchor) {
        if (boundOffsets == null) {
            return new int[] { 0, len };
        }
        int index = Arrays.binarySearch(boundOffsets, 0, max, anchor);
        if (index >= 0) {
            return new int[] {
                index == 0 ? 0 : boundOffsets[index],
                index == max - 1 ? len : boundOffsets[index + 1]
            };
        } else if (index == -1) {
            return new int[] {
                0, boundOffsets[0]
            };
        }
        index = -(index + 1) - 1;
        return new int[] {
            boundOffsets[index],
            index == max - 1 ? len : boundOffsets[index + 1]
        };
    }
    
    /**
     * Returns start of section that contains the anchor.
     * If there are no sections, 0 is returned (start of text). If the anchor
     * matches some section boundary, that boundary is returned (start of the
     * section).
     * 
     * @param anchor the anchor position
     * @return Start of the containing section.
     */
    public int findSectionStart(int anchor) {
        if (boundOffsets == null) {
            return 0;
        }
        int index = Arrays.binarySearch(boundOffsets, 0, max, anchor);
        if (index >= 0) {
            return boundOffsets[index];
        } else if (index == -1) {
            return 0;
        }
        index = -(index + 1) - 1;
        return boundOffsets[index];
    }
    
    /**
     * Returns the end of a section that contains the given offset.
     * If no sections are defined, the document end is returned (whole document forms one section).
     * 
     * @param fromOffset
     * @return 
     */
    public int findSectionEnd(int anchor) {
        if (boundOffsets == null) {
            return len;
        }
        int index = findSectionIndex(anchor);
        int s = boundOffsets[index];
        
        if (anchor < s) {
            return s;
        }
        if (index < max) {
            int e = boundOffsets[index + 1];
            if (e > anchor) {
                return e;
            }
        }
        return len;
    }
    
    /**
     * Finds an index of a section that might contain the fromOffset. The method may return
     * a nearby following section, so that caller may return the start of the following section 
     * as a boundary.
     * 
     * @param fromOffset inspected offset
     * @return section index into boundOffsets array
     */
    private int findSectionIndex(int fromOffset) {
        int hi = max - 2;
        int lo = 0;
        
        if (fromOffset < boundOffsets[lo]) {
            return lo;
        } else if (fromOffset >= boundOffsets[hi + 1]) {
            return hi;
        }
        
        while (lo < hi) {
            int mid = ((hi +  lo) / 2) & ~0x01;
            if (fromOffset < boundOffsets[mid]) {
                hi = mid - 2;
                if (fromOffset >= boundOffsets[hi + 1]) {
                    return hi;
                }
            } else if (fromOffset >= boundOffsets[mid + 1]) {
                lo = mid + 2;
                if (fromOffset < boundOffsets[lo]) {
                    return lo;
                }
            } else { 
                return mid;
            }
        }
        return lo;
    }
    
    private void initialize() {
        if (!(doc instanceof StyledDocument)) {
            return;
        }
        GuardedSectionManager mgr = GuardedSectionManager.getInstance((StyledDocument)doc);
        if (mgr == null) {
            return;
        }
        len = doc.getLength();
        
        int[] arr = new int[10];
        int p = 0;
        
        for (GuardedSection s : mgr.getGuardedSections()) {
            
            if (s instanceof InteriorSection) {
                InteriorSection is = (InteriorSection)s;
                arr = ensureSize(arr, p + 2);
                arr[p++] = is.getStartPosition().getOffset();
                arr[p++] = is.getBodyStartPosition().getOffset();
                arr[p++] = is.getBodyEndPosition().getOffset() + 1; // ???
                arr[p++] = is.getEndPosition().getOffset() + 1;
            } else {
                arr = ensureSize(arr, p);
                arr[p++] = s.getStartPosition().getOffset();
                arr[p++] = s.getEndPosition().getOffset() + 1;
            }
        }
        if (p == 0) {
            // boundOffsets remain null for further tests.
            return;
        }
        this.max = p;
        this.boundOffsets = arr;
    }

    private static int[] ensureSize(int[] arr, int p) {
        if (arr.length > p + 1) {
            return arr;
        }
        return Arrays.copyOf(arr, p * 2);
    }
}
