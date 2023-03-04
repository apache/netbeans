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

package org.netbeans.modules.editor.lib2.document;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mark is an element of the {@link MarkVector}.
 * <br/>
 * It holds an offset in the document which is updated with document modifications.
 * In addition the mark may be "unsharable" which means that a client asking
 * for a position at a given offset will not get an existing position referencing
 * this mark but instead it will get a new instance of mark and position.
 * Mark becomes unsharable once it's at begining or inside a region being removed
 * by a text removal. Upon undo these marks restore their original offsets
 * while a regular fresh mark (inserted at the offset) would go to its end
 * when undoing the removal.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

final class Mark extends WeakReference<EditorPosition> implements Runnable {

    // -J-Dorg.netbeans.modules.editor.lib2.document.Mark.level=FINE
    static final Logger LOG = Logger.getLogger(Mark.class.getName());
    
    /**
     * Offset at which the mark is located in the document.
     * It can be ORed with UNSHARABLE_BIT and it must be pre-processed with
     * markVector.offset() to get real offset.
     */
    int rawOffset; // 24-super + 4 = 28 bytes

    /**
     * Mark vector that hosts this mark or null if mark was removed from mark vector.
     * <br/>
     * Knowing that mark is no longer in the vector helps to make the algorithm
     * that restores positions' offsets upon undo more manageable and efficient.
     */
    private MarkVector markVector; // 28 + 4 = 32 bytes
    
    /**
     * Construct mark instance.
     */
    Mark(MarkVector markVector, int rawOffset, EditorPosition pos) {
        super(pos, org.openide.util.BaseUtilities.activeReferenceQueue()); // The queue calls run() when unreachable
        this.markVector = markVector;
        this.rawOffset = rawOffset;
        pos.initMark(this);
    }
    
    public int getOffset() {
        MarkVector lMarkVector = markVector;
        // Note that markVector==null for zeroPos all the time
        int offset = (lMarkVector != null) ? lMarkVector.offset(rawOffset) : rawOffset;
        return offset;
    }
    
    public boolean isBackwardBias() {
        MarkVector lMarkVector = markVector;
        return (lMarkVector != null) ? lMarkVector.isBackwardBiasMarks() : false;
    }
    
    public int rawOffset() {
        return rawOffset;
    }
    
    public void run() {
        // Called by Utilities.activeReferenceQueue() once the EditorPosition
        // is no longer reachable
        MarkVector lMarkVector = markVector;
        if (lMarkVector != null) {
            lMarkVector.notifyMarkDisposed();
        }
    }
    
    /**
     * Clear markVector to null to ensure that such mark can be identified
     * during processing of the markUpdates in MarkVector.insertUpdate().
     */
    void clearMarkVector() {
        markVector = null;
    }
    
    boolean isActive() {
        return (markVector != null);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(8);
        sb.append(getOffset());
        if (LOG.isLoggable(Level.FINE)) {
            EditorPosition pos = get();
            if (isBackwardBias()) {
                sb.append("B"); // Means backward-bias mark
            }
            if (pos == null) {
                sb.append('D'); // Disposed mark
            }
            if (LOG.isLoggable(Level.FINER)) {
                sb.append(";M@").append(Integer.toHexString(System.identityHashCode(this)));
            }
        }
        return sb.toString();
    }
    
    public String toStringDetail() {
        return toString() + ";R:" + rawOffset; // NOI18N
    }

}