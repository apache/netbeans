/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.highlighting;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;

/**
 * Vector of visual marks that correspond to certain position on y coordinate
 * according to their corresponding offset.
 *
 * @author Miloslav Metelka
 */
public class VisualMarkVector<M extends VisualMark> {
    
    // -J-Dorg.netbeans.modules.editor.lib2.view.VisualMarkVector.level=FINE
    private static final Logger LOG = Logger.getLogger(VisualMarkVector.class.getName());
    
    private static final double INITIAL_Y_GAP_LENGTH = Integer.MAX_VALUE;
    
    /**
     * Start of the visual gap in child views along their major axis.
     */
    private double yGapStart = INITIAL_Y_GAP_LENGTH; // 8-super + 8 = 16 bytes

    private double yGapLength = INITIAL_Y_GAP_LENGTH; // 16 + 8 = 24 bytes

    /**
     * Index of the visual gap in the contained children.
     */
    int yGapIndex; // 24 + 4 = 28 bytes

    private GapList<M> markList; // 28 + 4 = 32 bytes

    VisualMarkVector() {
        this.markList = new GapList<M>();
        this.yGapIndex = 0;
    }
    
    public final int markCount() {
        return markList.size();
    }
    
    public final M getMark(int index) {
        return markList.get(index);
    }

    double raw2Y(double rawY) {
        return (rawY < yGapStart) ? rawY : (rawY - yGapLength);
    }

    private void moveVisualGap(int index) {
        if (LOG.isLoggable(Level.FINE)) {
            checkGapConsistency();
        }
        if (index < yGapIndex) {
            double lastY = 0d;
            for (int i = yGapIndex - 1; i >= index; i--) {
                M mark = getMark(i);
                lastY = mark.rawY();
                mark.setRawY(lastY + yGapLength);
            }
            yGapStart = lastY;

        } else { // index > yGapIndex
            for (int i = yGapIndex; i < index; i++) {
                M mark = getMark(i);
                mark.setRawY(mark.rawY() - yGapLength);
            }
            if (index < markCount()) { // Gap moved to existing view - the view is right above gap => subtract gap-lengths
                M mark = getMark(index);
                yGapStart = mark.rawY() - yGapLength;
            } else {
                // Gap above at end of all existing Ys => make gap starts high enough
                // so that no offset/visual-offset is >= offsetGapStart/visualGapStart (no y translation occurs)
                yGapStart = INITIAL_Y_GAP_LENGTH;
            }
        }
        yGapIndex = index;
        if (LOG.isLoggable(Level.FINE)) {
            checkGapConsistency();
        }
    }
    
    private void checkGapConsistency() {
        String error = gapConsistency();
        if (error != null) {
            throw new IllegalStateException("");
        }
    }

    private String gapConsistency() {
        String error = null;
        for (int i = 0; i < markCount(); i++) {
            M mark = getMark(i);
            double rawY = mark.rawY();
            double y = mark.getY();
            if (i < yGapIndex) {
                if (rawY >= yGapStart) {
                    error = "Not below y-gap: rawY=" + rawY + // NOI18N
                            " >= yGapStart=" + yGapStart; // NOI18N
                }
            } else { // Above gap
                if (rawY < yGapStart) {
                    error = "Not above y-gap: rawY=" + rawY + // NOI18N
                            " < yGapStart=" + yGapStart; // NOI18N
                }
                if (i == yGapIndex) {
                    if (y != yGapStart) {
                        error = "y=" + y + " != yGapStart=" +  yGapStart; // NOI18N
                    }
                }

            }
            if (error != null) {
                break;
            }
        }
        return error;
    }

    StringBuilder appendInfo(StringBuilder sb) {
        int markCount = markList.size();
        int digitCount = ArrayUtilities.digitCount(markCount);
        for (int i = 0; i < markCount; i++) {
            ArrayUtilities.appendSpaces(sb, 2);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            M mark = markList.get(i);
            sb.append(": ").append(mark);
            sb.append('\n');
        }
        return sb;
    }

    public String toStringDetail() {
        return appendInfo(new StringBuilder().append(this.toString())).toString();
    }

    @Override
    public String toString() {
        return new StringBuilder(80).append(", vis<").append(yGapStart).append("|"). // NOI18N
            append(yGapLength).append(">\n").toString(); // NOI18N
    }
    
}
