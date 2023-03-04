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
