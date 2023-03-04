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
package org.openide.text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Binary searchable list of document lines.
 * <br/>
 * Member lines are weakly held.
 * <br/>
 * Replacement of Line.Set.whm which contains ALL the registered lines in a SINGLE BUCKET
 * (thank to DocumentLine.hashcode()) and is an inappropriate storage type for this
 * kind of information.
 *
 * @author Miloslav Metelka
 */
final class LineVector {
    
    // -J-Dorg.openide.text.LineVector.level=FINE
    private static final Logger LOG = Logger.getLogger(LineVector.class.getName());

    private Ref[] refArray;
    
    /**
     * Index of gap inside lineRefs array.
     */
    private int gapStart;
    
    /**
     * Size of the gap.
     */
    private int gapLength;
    
    /**
     * Number of line refs that were garbage collected. Once it reaches emptyRefsThreshold
     * a task is run that removes empty refs from lineRefs array.
     * If empty refs is more than 1/32 of total ref count then the empty refs are removed
     */
    private int disposedRefCount;
    
    /**
     * If true then something went wrong and the array of references is not sorted properly
     * and therefore binary search cannot be used and the array must be traversed sequentially.
     */
    private boolean refArrayUnsorted;
    
    private Thread lockThread;
    
    private int lockDepth;
    
    /**
     * Line updater that should update lines once the current request completes.
     */
    private List<LineUpdater> pendingLineUpdaters = new ArrayList<LineUpdater>(2);
    
    LineVector() {
        this.refArray = new Ref[4];
        this.gapLength = this.refArray.length;
    }
    
    Line findOrCreateLine(int findLineIndex, LineCreator lineCreator) {
        lockCheckUpdate();
        try {
            int last = refCount() - 1;
            int low = 0;
            int high = last;
            if (!refArrayUnsorted) {
                int lowLineIndex = -1;
                int highLineIndex = Integer.MAX_VALUE;
                while (low <= high) {
                    int mid = (low + high) >>> 1; // mid in the binary search
                    Ref ref = refArray[rawIndex(mid)];
                    Line line = ref.get();
                    if (line == null) {
                        int index = mid - 1;
                        while (index >= 0) {
                            ref = refArray[rawIndex(index)];
                            line = ref.get();
                            if (line != null) {
                                break;
                            }
                            index--;
                        }
                    }
                    int lineIndex = (line != null) ? line.getLineNumber() : -1;
                    if (lineIndex < lowLineIndex || lineIndex > highLineIndex) { // Array became unsorted
                        if (LOG.isLoggable(Level.FINE)) {
                            String msg = "!!!LineVector: ARRAY BECAME UNSORTED!!!\n  " +
                                    toStringDetail() + "    lineIndex=" + lineIndex + // NOI18N
                                    ", lowLineIndex=" + lowLineIndex + ", highLineIndex=" + highLineIndex + // NOI18N
                                    "\n    low=" + low + ", high=" + high + ", mid=" + mid + "\n"; // NOI18N
                            LOG.log(Level.INFO, msg, new Throwable());
                        }
                        refArrayUnsorted = true;
                        break; // Iterate again this time sequential search will be used
                    }
                    if (lineIndex < findLineIndex) {
                        low = mid + 1;
                        lowLineIndex = lineIndex;
                    } else if (lineIndex > findLineIndex) {
                        high = mid - 1;
                        highLineIndex = lineIndex;
                    } else { // line numbers equal
                        return line;
                    }
                }
            }
            if (refArrayUnsorted) { // Unsorted array => use sequential search
                for (; low <= last; low++) {
                    Ref ref = refArray[rawIndex(low)];
                    Line line = ref.get();
                    if (line != null && line.getLineNumber() == findLineIndex) {
                        return line;
                    }
                }
                low = gapStart; // Insert anywhere since the array is no longer sorted
            }

            // Create line at index "low"
            return (lineCreator != null)
                    ? addLine(low, lineCreator.createLine(findLineIndex))
                    : null;
        } finally {
            unlockCheckUpdate();
        }
    }
    
    void updateLines(LineUpdater lineUpdater) {
        synchronized (this) {
            pendingLineUpdaters.add(lineUpdater);
            if (lockThread == null) { // No locker -> do synchronously now
                lockCheckUpdate(); // Lock to ensure no recursive locking would happen
                try {
                } finally {
                    unlockCheckUpdate(); // Perform updateLinesCheck()
                }
            } // else: the locker will perform update lines upon unlockCheckUpdate()
        }
    }
    
    private void updateLinesCheck() {
        List<LineUpdater> lineUpdaters;
        synchronized (this) {
            if (pendingLineUpdaters.size() > 0) {
                lineUpdaters = new ArrayList<LineUpdater>(pendingLineUpdaters);
                pendingLineUpdaters.clear();
            } else {
                lineUpdaters = null;
            }
        }

        if (lineUpdaters != null) {
            for (LineUpdater lineUpdater : lineUpdaters) {
                for (int rawIndex = 0; rawIndex < gapStart; rawIndex++) {
                    Line line = refArray[rawIndex].get();
                    lineUpdater.updateLine(line);
                }
                for (int rawIndex = gapStart + gapLength; rawIndex < refArray.length; rawIndex++) {
                    Line line = refArray[rawIndex].get();
                    lineUpdater.updateLine(line);
                }
            }
        }
    }
    
    List<Line> getLinesInRange(int startLineIndex, int endLineIndex) {
        lockCheckUpdate();
        try {
            List<Line> lines = new ArrayList<Line>();
            int last = refCount() - 1;
            int low = 0;
            int high = last;
            if (!refArrayUnsorted) {
                int lowLineIndex = -1;
                int highLineIndex = Integer.MAX_VALUE;
                while (low <= high) {
                    int mid = (low + high) >>> 1; // mid in the binary search
                    Ref ref = refArray[rawIndex(mid)];
                    Line line = ref.get();
                    if (line == null) {
                        int index = mid - 1;
                        while (index >= 0) {
                            ref = refArray[rawIndex(index)];
                            line = ref.get();
                            if (line != null) {
                                break;
                            }
                            index--;
                        }
                    }
                    int lineIndex = (line != null) ? line.getLineNumber() : -1;
                    if (lineIndex < lowLineIndex || lineIndex > highLineIndex) { // Array became unsorted
                        refArrayUnsorted = true;
                        if (LOG.isLoggable(Level.FINE)) {
                            String msg = "!!!LineVector: ARRAY BECAME UNSORTED!!!\n  " +
                                    toStringDetail() + "    lineIndex=" + lineIndex + // NOI18N
                                    ", lowLineIndex=" + lowLineIndex + ", highLineIndex=" + highLineIndex + // NOI18N
                                    "\n    low=" + low + ", high=" + high + ", mid=" + mid + "\n"; // NOI18N
                            LOG.log(Level.INFO, msg, new Throwable());
                        }
                        break; // Iterate again this time sequential search will be used
                    }
                    if (lineIndex < startLineIndex) {
                        low = mid + 1;
                        lowLineIndex = lineIndex;
                    } else if (lineIndex > startLineIndex) {
                        high = mid - 1;
                        highLineIndex = lineIndex;
                    } else { // line numbers equal -> find first one
                        while (--mid >= 0) {
                            ref = refArray[rawIndex(mid)];
                            line = ref.get();
                            if (line != null && line.getLineNumber() < startLineIndex) {
                                break;
                            }
                        }
                        low = mid + 1;
                        break;
                    }
                }
                if (!refArrayUnsorted) {
                    for (; low <= last; low++) {
                        Line line = refArray[rawIndex(low)].get();
                        if (line != null) {
                            int lineIndex = line.getLineNumber();
                            if (startLineIndex <= lineIndex && lineIndex <= endLineIndex) {
                                lines.add(line);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (refArrayUnsorted) { // Unsorted array => use sequential search
                for (; low <= last; low++) {
                    Line line = refArray[rawIndex(low)].get();
                    if (line != null) {
                        int lineIndex = line.getLineNumber();
                        if (startLineIndex <= lineIndex && lineIndex <= endLineIndex) {
                            lines.add(line);
                        }
                    }
                }
            }
            return lines;
        } finally {
            unlockCheckUpdate();
        }
    }
    
    private Line addLine(int index, Line line) {
        moveGap(index);
        if (gapLength == 0) {
            reallocate((refArray.length + 8) >> 2);
        }
        refArray[gapStart++] = new Ref(line);
        gapLength--;
        return line;
    }
    
    private int refCount() {
        return refArray.length - gapLength;
    }
    
    private int rawIndex(int index) {
        return (index < gapStart)
                ? index
                : index + gapLength;
    }

    private void moveGap(int index) {
        // No need to clear the no-longer occupied space in refs array after arraycopy()
        // since these are only refs still present in the array and in the end removeEmptyRefsLockAcquired() will clean them
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(refArray, index, refArray, gapStart + gapLength - moveSize, moveSize);
        } else { // above gap
            int moveSize = index - gapStart;
            System.arraycopy(refArray, gapStart + gapLength, refArray, gapStart, moveSize);
        }
        gapStart = index;
    }

    synchronized void refGC() {
        disposedRefCount++;
    }
    
    private void checkRemoveEmptyRefs() {
        int cnt;
        synchronized (this) {
            cnt = disposedRefCount;
        }
        if (cnt > 4 && cnt > (refCount() >>> 3)) {
            removeEmptyRefs();
        }
    }

    private void removeEmptyRefs() {
        int rawIndex = 0;
        int validIndex = 0;
        int emptyCount = 0;
        int gapEnd = gapStart + gapLength;
        // Only retain refs with valid lines
        while (rawIndex < gapStart) {
            Ref ref = refArray[rawIndex];
            if (ref.get() != null) {
                if (rawIndex != validIndex) {
                    refArray[validIndex] = ref;
                }
                validIndex++;
            } else {
                emptyCount++;
            }
            rawIndex++;
        }
        gapStart = validIndex;

        // Go back from end till gap end
        rawIndex = refArray.length;
        int topValidIndex = rawIndex; // validIndex points to first valid ref above gap
        while (--rawIndex >= gapEnd) {
            Ref ref = refArray[rawIndex];
            if (ref.get() != null) {
                if (rawIndex != --topValidIndex) {
                    refArray[topValidIndex] = ref;
                }
            } else {
                emptyCount++;
            }
        }
        int newGapLength = topValidIndex - gapStart;
        gapLength = newGapLength;
        // Clear the area between valid indices (also because moveGap() does not clear the stale areas)
        while (validIndex < topValidIndex) {
            refArray[validIndex++] = null;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("LineVector.removeDisposedRefsLockAcquired() refCount=" + refCount() + ", emptyCount=" + emptyCount + "\n");
        }
        synchronized (this) {
            disposedRefCount -= emptyCount;
        }
    }
    
    private void reallocate(int newGapLength) {
        int gapEnd = gapStart + gapLength;
        int aboveGapLength = refArray.length - gapEnd;
        int newLength = gapStart + aboveGapLength + newGapLength;
        Ref[] newRefArray = new Ref[newLength];
        System.arraycopy(refArray, 0, newRefArray, 0, gapStart);
        System.arraycopy(refArray, gapEnd, newRefArray, newLength - aboveGapLength, aboveGapLength);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("LineVector.reallocate() from refArray.length=" + refArray.length + " to newLength=" + newLength + "\n");
        }
        // gapStart is same
        gapLength = newGapLength;
        refArray = newRefArray;
    }
    
    private void lockCheckUpdate() {
        lock();
        checkRemoveEmptyRefs();
    }
    
    private synchronized void lock() {
        Thread currentThread = Thread.currentThread();
        while (lockThread != null && currentThread != lockThread) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Interrupted attempt to aquire lock");
            }
        }
        if (lockThread != null) { // Recursive lock
            throw new IllegalStateException("Recursive line vector locking prohibited. LineVector: " + this);
        }
        lockThread = currentThread;
        lockDepth++;
    }
    
    private void unlockCheckUpdate() {
        // Check pending lines update
        updateLinesCheck();
        unlock();
    }
    
    private synchronized void unlock() {
        lockDepth--;
        if (lockDepth == 0) {
            lockThread = null;
            notifyAll();
        }
    }

    @Override
    public String toString() {
        return "refArray.length=" + refArray.length + ", gapStart=" + gapStart + ", gapLength=" + gapLength + // NOI18N
                ", disposedRefCount=" + disposedRefCount + ", activeRefCount=" + (refCount()-disposedRefCount) +
                "\n  refArrayUnsorted=" + refArrayUnsorted + // NOI18N
                ", lockThread=" + lockThread + ", lockDepth=" + lockDepth + // NOI18N
                ", pendingLineUpdaters=" + pendingLineUpdaters; // NOI18N
    }

    private String toStringDetail() {
        StringBuilder sb = new StringBuilder(256);
        lock();
        try {
            sb.append(this.toString()).append('\n');
            for (int i = 0; i < refCount(); i++) {
                Ref ref = refArray[rawIndex(i)];
                Line line = ref.get();
                sb.append("[").append(i).append("]:\t").append(line).append('\n');
            }
        } finally {
            unlock();
        }
        return sb.toString();
    }
    
    
    private final class Ref extends WeakReference<Line> implements Runnable {

        public Ref(Line line) {
            super(line, org.openide.util.BaseUtilities.activeReferenceQueue()); // The queue calls run() when unreachable
        }

        @Override
        public void run() {
            refGC();
        }

    
    }

    interface LineCreator {
        
        Line createLine(int line);

    }
    
    interface LineUpdater {
        
        void updateLine(Line line);

    }

}
