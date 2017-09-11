/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.lib2.view;

/**
 * Gap storage speeds up operations when a number of children views exceeds
 * certain number.
 * 
 * @author Miloslav Metelka
 */

final class ViewGapStorage {

    /**
     * Number of child views above which they will start to be managed
     * in a gap-storage way upon modification.
     * Below the threshold the views are updated without gap creation.
     */
    static final int GAP_STORAGE_THRESHOLD = 20;

    /**
     * Length of the visual gap in child view infos along their major axis.
     */
    static final double INITIAL_VISUAL_GAP_LENGTH = (1L << 40);

    static final int INITIAL_OFFSET_GAP_LENGTH = (Integer.MAX_VALUE >>> 1);

    /**
     * Start of the visual gap in child views along their major axis.
     * <br>
     * Place it above end of all views initially.
     */
    double visualGapStart; // 8-super + 8 = 16 bytes

    double visualGapLength; // 16 + 8 = 24 bytes
    
    /**
     * Index where the visual gap is located in children views.
     * This is to help to avoid checking of above/below gap members
     * when doing many single-view span updates.
     */
    int visualGapIndex; // 24 + 4 = 28 bytes

    /**
     * Start of the offset gap used for managing end offsets of HighlightsView views.
     * It is not used for paragraph views.
     * <br>
     * Place it above end of all views initially.
     */
    int offsetGapStart; // 28 + 4 = 32 bytes

    int offsetGapLength; // 32 + 4 = 36 bytes
    
    void initVisualGap(int visualGapIndex, double visualGapStart) {
        this.visualGapIndex = visualGapIndex;
        this.visualGapStart = visualGapStart;
        this.visualGapLength = INITIAL_VISUAL_GAP_LENGTH;
    }

    void initOffsetGap(int offsetGapStart) {
        this.offsetGapStart = offsetGapStart;
        this.offsetGapLength = INITIAL_OFFSET_GAP_LENGTH;
    }
    
    int raw2Offset(int rawEndOffset) {
        // Using <= allows to use prevView.getRawEndOffset() as offsetGapStart (assuming non-empty views)
        return (rawEndOffset <= offsetGapStart)
                ? rawEndOffset
                : rawEndOffset - offsetGapLength;
    }

    int offset2Raw(int offset) {
        // Using <= allows to use prevView.getRawEndOffset() as offsetGapStart (assuming non-empty views)
        return (offset <= offsetGapStart)
                ? offset
                : offset + offsetGapLength;
    }

    double raw2VisualOffset(double rawVisualOffset) {
        // Using <= allows to use prevView.getRawEndVisualOffset() as visualGapStart (assuming non-empty views)
        return (rawVisualOffset <= visualGapStart)
                ? rawVisualOffset
                : rawVisualOffset - visualGapLength;
    }

    double visualOffset2Raw(double visualOffset) {
        // Using <= allows to use prevView.getRawEndVisualOffset() as visualGapStart (assuming non-empty views)
        return (visualOffset <= visualGapStart)
                ? visualOffset
                : visualOffset + visualGapLength;
    }

    boolean isBelowVisualGap(double rawVisualOffset) {
        return (rawVisualOffset <= visualGapStart);
    }

    StringBuilder appendInfo(StringBuilder sb) {
        sb.append("<").append(offsetGapStart).append("|").append(offsetGapLength). // NOI18N
                append(", vis[").append(visualGapIndex).append("]<"). // NOI18N
                append(visualGapStart).append("|").append(visualGapLength); // NOI18N
        return sb;
    }

    @Override
    public String toString() {
        return appendInfo(new StringBuilder(100)).toString();
    }

}
