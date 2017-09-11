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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.lexer;

/**
 * A structure holding lookahead and state of a token list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LAState {

    private static final LAState EMPTY = new NoState(0);
    
    public static LAState empty() {
        return EMPTY;
    }
    
    static int withExtraCapacity(int capacity) {
        return capacity * 3 / 2 + 4;
    }

    static boolean isByteState(Object state) {
        int intState;
        return (state.getClass() == Integer.class
                && (intState = ((Integer)state).intValue()) >= 0 && intState <= Byte.MAX_VALUE);
    }


    int gapStart;

    int gapLength;

    public LAState(int capacity) {
        gapLength = capacity;
    }

    /**
     * Get lookahead at the particular index
     * preparing the state as well.
     */
    public abstract int lookahead(int index);

    public abstract Object state(int index);

    public final LAState trimToSize() {
        if (gapLength > 0) {
            LAState laState = upgrade(size(), getClass());
            reallocate(laState, size());
            return laState;
        }
        return this;
    }

    public final int size() {
        return capacity() - gapLength;
    }

    /**
     * Add a particular lookahead and state.
     *
     * @param lookahead
     * @param state
     * @return either same or a new LAState containing the given lookahead and state.
     */
    public final LAState add(int lookahead, Object state) {
        LAState ret;
        if (gapLength > 0) { // enough space
            moveGap(size());
            ret = this;
        } else { // no more space
            // Called when doing TokenSequence.moveNext() - tokens get created lazily
            // Double the capacity - Token lists should call trimToSize() after finishing
            ret = upgrade((capacity() + 1) << 1, getClass());
            reallocate(ret, size());
        }

        Class laStateCls;
        if ((laStateCls = ret.addToGapStart(lookahead, state)) != null) {
            ret = upgrade(capacity() + 1, laStateCls);
            reallocate(ret, size());
            ret.addToGapStart(lookahead, state);
        }

        ret.gapStart += 1;
        ret.gapLength -= 1;
        return ret;
    }

    public final LAState addAll(int index, LAState laState) {
        LAState ret;
        int laStateSize = laState.size();
        if (!isUpgrade(laState.getClass()) && gapLength > laStateSize) { // enough space
            moveGap(index);
            ret = this;
        } else { // no more space
            // Called when fixing token list by TokenListUpdater
            // Leave 10% for growth
            ret = upgrade((int)((capacity() + laStateSize) * 110L / 100), laState.getClass());
            reallocate(ret, index);
        }

        laState.copyData(0, ret, ret.gapStart, laState.gapStart);
        int laStateGapEnd = laState.gapStart + laState.gapLength;
        laState.copyData(laStateGapEnd, ret, ret.gapStart + laState.gapStart,
                laState.capacity() - laStateGapEnd);

        ret.gapStart += laStateSize;
        ret.gapLength -= laStateSize;
        return ret;
    }

    protected abstract LAState upgrade(int capacity, Class laStateClass);

    /**
     * Whether an upgrade is necessary when the given laStateClass needs to be used.
     *
     * @param laStateClass non-null requested laStateClass
     * @return true if upgrade is necessary.
     */
    protected abstract boolean isUpgrade(Class laStateClass);

    protected abstract Class addToGapStart(int lookahead, Object state);

    public final void remove(int index, int count) {
        moveGap(index + count);
        removeUpdate(index, count); // Perform fully below gap
        gapStart -= count;
        gapLength += count;
    }

    protected void removeUpdate(int index, int count) {
        // Do nothing
    }

    protected final int rawIndex(int index) {
        return (index < gapStart) ? index : index + gapLength;
    }

    final void reallocate(LAState tgt, int newGapStart) {
        tgt.gapStart = newGapStart;
        tgt.gapLength = gapLength + tgt.capacity() - capacity();
        int gapEnd = gapStart + gapLength;
        if (newGapStart < gapStart) { // only partly allocate
            copyData(0, tgt, 0, newGapStart);
            int tgtRawIndex = newGapStart + tgt.gapLength;
            int len = gapStart - newGapStart;
            copyData(newGapStart, tgt, tgtRawIndex, len);
            tgtRawIndex += len;
            copyData(gapEnd, tgt, tgtRawIndex, capacity() - gapEnd);

        } else { // index above or equals gapStart
            copyData(0, tgt, 0, gapStart);
            int len = newGapStart - gapStart;
            copyData(gapEnd, tgt, gapStart, len);
            gapEnd += len;
            copyData(gapEnd, tgt, newGapStart + tgt.gapLength, capacity() - gapEnd);
        }
    }

    protected abstract void copyData(int srcRawIndex, LAState tgt, int dstRawIndex, int len);

    protected abstract int capacity();

    final void moveGap(int index) {
        if (index == gapStart)
            return;
        if (gapLength > 0) {
            if (index < gapStart) { // move gap down
                int moveSize = gapStart - index;
                moveData(index, gapStart + gapLength - moveSize, moveSize);
                //clearEmpty(index, Math.min(moveSize, gapLength));

            } else { // above gap
                int gapEnd = gapStart + gapLength;
                int moveSize = index - gapStart;
                moveData(gapEnd, gapStart, moveSize);
                if (index < gapEnd) {
                    //clearEmpty(gapEnd, moveSize);
                } else {
                    //clearEmpty(index, gapLength);
                }
            }
        }
        gapStart = index;
    }

    protected abstract void moveData(int srcRawIndex, int dstRawIndex, int len);

    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(lookahead(i));
            sb.append(", ");
            sb.append(state(i));
        }
        sb.append(']');
        return sb.toString();
    }


    static final class NoState extends LAState {

        byte[] laBytes;

        NoState(int capacity) {
            super(capacity);
            laBytes = new byte[capacity];
        }

        public int lookahead(int index) {
            int rawIndex = rawIndex(index);
            return laBytes[rawIndex];
        }

        public Object state(int index) {
            return null;
        }

        protected LAState upgrade(int capacity, Class laStateClass) {
            if (laStateClass == LargeState.class) {
                return new LargeState(capacity);
            } else if (laStateClass == ByteState.class) {
                return new ByteState(capacity);
            } else {
                return new NoState(capacity);
            }
        }

        protected boolean isUpgrade(Class laStateClass) {
            return (laStateClass == LargeState.class) || (laStateClass == ByteState.class);
        }

        protected void copyData(int srcRawIndex, LAState tgt, int dstRawIndex, int len) {
            if (tgt.getClass() == getClass()) { // same type
                System.arraycopy(laBytes, srcRawIndex, ((NoState)tgt).laBytes, dstRawIndex, len);
            } else if (tgt.getClass() == ByteState.class) {
                short[] laStateShorts = ((ByteState)tgt).laStateShorts;
                while (--len >= 0) {
                    laStateShorts[dstRawIndex++] = (short)(laBytes[srcRawIndex++] | 0xFF00);
                }
            } else { // large state
                int[] las = ((LargeState)tgt).lookaheads;
                // No need to operate on tgt.states as they will remain nulls
                while (--len >= 0) {
                    las[dstRawIndex++] = laBytes[srcRawIndex++];
                }
            }
        }

        protected void moveData(int srcRawIndex, int dstRawIndex, int len) {
            System.arraycopy(laBytes, srcRawIndex, laBytes, dstRawIndex, len);
        }

        protected Class addToGapStart(int lookahead, Object state) {
            if (lookahead <= Byte.MAX_VALUE) {
                if (state == null) {
                    laBytes[gapStart] = (byte)lookahead;
                    return null;
                } else if (isByteState(state)) {
                    return ByteState.class;
                }
            }
            return LargeState.class;
        }

        protected int capacity() {
            return laBytes.length;
        }

    }


    static final class ByteState extends LAState {

        short[] laStateShorts;

        ByteState(int capacity) {
            super(capacity);
            laStateShorts = new short[capacity];
        }

        public int lookahead(int index) {
            return laStateShorts[rawIndex(index)] & 0xFF;
        }

        public Object state(int index) {
            int val = laStateShorts[rawIndex(index)] & 0xFF00;
            return (val == 0xFF00) ? null : IntegerCache.integer(val >> 8);
        }

        protected LAState upgrade(int capacity, Class laStateClass) {
            if (laStateClass == LargeState.class) {
                return new LargeState(capacity);
            } else {
                return new ByteState(capacity);
            }
        }

        protected boolean isUpgrade(Class laStateClass) {
            return (laStateClass == LargeState.class);
        }

        protected void copyData(int srcRawIndex, LAState tgt, int dstRawIndex, int len) {
            if (tgt.getClass() == getClass()) { // same type
                System.arraycopy(laStateShorts, srcRawIndex, ((ByteState)tgt).laStateShorts, dstRawIndex, len);
            } else { // large state
                int[] las = ((LargeState)tgt).lookaheads;
                Object[] states = ((LargeState)tgt).states;
                while (--len >= 0) {
                    int val = laStateShorts[srcRawIndex++] & 0xFFFF;
                    las[dstRawIndex] = val & 0xFF;
                    val &= 0xFF00;
                    if (val != 0xFF00) { // not null state
                        states[dstRawIndex] = IntegerCache.integer(val >> 8);
                    }
                    dstRawIndex++;
                }
            }
        }

        protected void moveData(int srcRawIndex, int dstRawIndex, int len) {
            System.arraycopy(laStateShorts, srcRawIndex, laStateShorts, dstRawIndex, len);
        }

        protected Class addToGapStart(int lookahead, Object state) {
            if (lookahead <= Byte.MAX_VALUE) {
                int intState;
                if (state == null) {
                    intState = 0xFF00;
                } else if (isByteState(state)) {
                    intState = (((Integer)state).intValue() << 8);
                } else
                    return LargeState.class;
                laStateShorts[gapStart] = (short)(intState | lookahead);
                return null;
            }
            return LargeState.class;
        }

        protected int capacity() {
            return laStateShorts.length;
        }

    }


    static final class LargeState extends LAState {

        int[] lookaheads;

        Object[] states;

        LargeState(int capacity) {
            super(capacity);
            lookaheads = new int[capacity];
            states = new Object[capacity];
        }

        public int lookahead(int index) {
            return lookaheads[rawIndex(index)];
        }

        public Object state(int index) {
            return states[rawIndex(index)];
        }

        protected LAState upgrade(int capacity, Class laStateClass) {
            return new LargeState(capacity);
        }

        protected boolean isUpgrade(Class laStateClass) {
            return false;
        }

        protected void copyData(int srcRawIndex, LAState tgt, int dstRawIndex, int len) {
            System.arraycopy(lookaheads, srcRawIndex, ((LargeState)tgt).lookaheads, dstRawIndex, len);
            System.arraycopy(states, srcRawIndex, ((LargeState)tgt).states, dstRawIndex, len);
        }

        protected void moveData(int srcRawIndex, int dstRawIndex, int len) {
            System.arraycopy(lookaheads, srcRawIndex, lookaheads, dstRawIndex, len);
            System.arraycopy(states, srcRawIndex, states, dstRawIndex, len);
        }

        protected Class addToGapStart(int lookahead, Object state) {
            lookaheads[gapStart] = lookahead;
            states[gapStart] = state;
            return null;
        }

        @Override
        protected void removeUpdate(int index, int count) {
            while (--count >= 0) {
                states[index + count] = null; // clear the state to allow its gc
            }
        }

        protected int capacity() {
            return lookaheads.length;
        }

    }

}
