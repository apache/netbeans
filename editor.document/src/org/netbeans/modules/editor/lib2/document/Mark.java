/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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