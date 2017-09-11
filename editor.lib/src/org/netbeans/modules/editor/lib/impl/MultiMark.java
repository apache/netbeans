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

package org.netbeans.modules.editor.lib.impl;

import java.lang.ref.WeakReference;
import javax.swing.text.Position;


/**
 * Multipurpose mark that can be used
 * both as the traditional swing mark
 * or the bias mark.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class MultiMark extends WeakReference<BasePosition> implements Runnable {

    /** Whether mark has a backward (or forward) bias */
    static final int BACKWARD_BIAS = 1;

    /** Whether mark was disposed and can no longer be used */
    static final int VALID = 2;

    /** Storage of the marks uses this flag
     * to indicate that the diposed mark was physically removed
     * from the underlying array.
     */
    static final int REMOVED = 4;
    
    /** Whether mark behaves so that it conforms to the behavior
     * of the swing positions. This behavior requires the mark
     * to keep its offset to be zero once the mark
     * reaches the zero offset (by removal in the document).
     */
    static final int COMPATIBLE = 8;
    
    /** Whether the compatible mark has zero offset regardless of
     * what MarkVector.getOffset() would return.
     */
    static final int ZERO = 16;
    
    /** Offset at which the mark is located in the document. */
    int rawOffset;

    /** Composition of the flags */
    int flags;
    
    /** Mark vector that hosts this mark. */
    private final MarkVector markVector;
    
    /** Construct compatible mark */
    MultiMark(BasePosition pos, MarkVector markVector, int offset) {
        this(pos, markVector, offset,
            (offset != 0) ? COMPATIBLE : (COMPATIBLE | ZERO | BACKWARD_BIAS));
    }
    
    /** Construct bias mark */
    MultiMark(BasePosition pos, MarkVector markVector, int offset, Position.Bias bias) {
        this(pos, markVector, offset,
            (bias == Position.Bias.Backward) ? BACKWARD_BIAS : 0);
    }
        
    /** Construct new mark. The mark is invalid by default.
     */
    private MultiMark(BasePosition pos, MarkVector markVector, int offset, int flags) {
        super(pos, org.openide.util.Utilities.activeReferenceQueue());
        if (pos != null) {
            pos.setMark(this);
        }
        this.markVector = markVector;
        this.rawOffset = offset; // will be corrected once the mark is inserted
        this.flags = flags;
    }

    /** @return the bias of this mark. It will be either
     * {@link javax.swing.text.Position.Bias.Forward}
     * or {@link javax.swing.text.Position.Bias.Backward}.
     */
    public Position.Bias getBias() {
        return ((flags & BACKWARD_BIAS) != 0)
            ? Position.Bias.Backward
            : Position.Bias.Forward;
    }
    
    /** Get the position of this mark */
    public int getOffset() {
        synchronized (markVector) {
            if ((flags & VALID) != 0) {
                return ((flags & ZERO) == 0)
                    ? markVector.getOffset(rawOffset)
                    : 0;
            } else { // already disposed
                throw new IllegalStateException();
            }
        }
    }
    
    public void run() {
        // Called by Utilities.activeReferenceQueue() once the BasePosition
        // is no longer reachable
        dispose();

    }

    /** Mark will no longer represent a valid place in the document.
     * Attempts to use the mark will result into throwing of
     * {@link java.lang.IllegalStateException}.
     * @throws IllegalStateException if the mark was already disposed before.
     */
    public void dispose() {
        synchronized (markVector) {
            if ((flags & VALID) != 0) {
                flags &= ~VALID;
                markVector.notifyMarkDisposed();
            } else { // already disposed before
                throw new IllegalStateException();
            }
        }
    }
    
    /** @return true if this mark was not disposed yet.
     */
    public boolean isValid() {
        synchronized(markVector) {
            return ((flags & VALID) != 0);
        }
    }

    public @Override String toString() {
        StringBuffer sb = new StringBuffer();
        synchronized(markVector) {
            if ((flags & VALID) != 0) {
                sb.append("offset=" + getOffset()); // NOI18N
            } else {
                sb.append("removed"); // NOI18N
            }
            sb.append(", bias="); // NOI18N
            sb.append(getBias());
            
            return sb.toString();
        }
    }

    public String toStringDetail() {
        StringBuffer sb = new StringBuffer();
        synchronized(markVector) {
            sb.append(System.identityHashCode(this));
            sb.append(" ("); // NOI18N
            sb.append(rawOffset);
            sb.append(" -> "); // NOI18N
            if ((flags & VALID) != 0) {
                sb.append(getOffset());
            } else {
                sb.append('X');
                sb.append(markVector.getOffset(rawOffset));
                sb.append('X');
            }
            sb.append(", "); // NOI18N
            sb.append(((flags & BACKWARD_BIAS) != 0) ? 'B' : 'F');
            if ((flags & VALID) != 0) {
                sb.append('V');
            }
            if ((flags & REMOVED) != 0) {
                sb.append('R');
            }
            if ((flags & COMPATIBLE) != 0) {
                sb.append('C');
            }
            if ((flags & ZERO) != 0) {
                sb.append('Z');
            }
            sb.append(')');

            return sb.toString();
        }
    }


}
