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

package org.netbeans.core.output2;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Pair;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.windows.IOColors;
import org.openide.windows.OutputListener;
/**
 * Abstract Lines implementation with handling for getLine wrap calculations, etc.
 */
abstract class AbstractLines implements Lines, Runnable, ActionListener {
    private static final Logger LOG =
            Logger.getLogger(AbstractLines.class.getName());

    private OutputLimits outputLimits = OutputLimits.getDefault();

    /** A collections-like lineStartList that maps file positions to getLine numbers */
    IntList lineStartList;
    IntListSimple lineCharLengthListWithTabs;
    /** Maps output listeners to the lines they are associated with */
    IntMap lineWithListenerToInfo;

    /** line index to LineInfo */
    IntMap linesToInfos;

    /** longest line length (in chars)*/
    private int longestLineLen = 0;

    private int knownCharsPerLine = -1;

    /** cache of logical (wrapped) lines count, used to transform logical (wrapped)
     * line index to physical (real) line index */
    private SparseIntList knownLogicalLineCounts = null;

    /** Offset positions of tabs */
    private IntList tabCharOffsets = new IntList(128);
    /** Sums of length of all preceding tabs (length of extra spaces) */
    private IntListSimple tabLengthSums = new IntListSimple(128);

    private final IntListSimple foldOffsets = new IntListSimple(16);
    private final IntListSimple visibleList = new IntListSimple(128);
    private final IntListSimple visibleToRealLine = new IntListSimple(128);
    private final IntListSimple realToVisibleLine = new IntListSimple(128);
    private int hiddenLines = 0;

    private int currentFoldStart = -1;
    /** last storage size (after dispose), in bytes */
    private int lastStorageSize = -1;

    AbstractLines() {
        if (Controller.LOG) Controller.log ("Creating a new AbstractLines");
        init();
    }

    protected abstract Storage getStorage();

    protected abstract boolean isDisposed();

    protected abstract void handleException (Exception e);

    public char[] getText(int start, int end, char[] chars) {
        if (chars == null) {
            chars = new char[end - start];
        }
        if (end < start || start < 0) {
            throw new IllegalArgumentException ("Illogical text range from " + start + " to " + end);
        }
        if (end - start > chars.length) {
            throw new IllegalArgumentException("Array size is too small");
        }
        synchronized(readLock()) {
            if (isDisposed()) {
                // dispose is performed asynchronously, data may be required by
                // events fired before (during) dispose(), return just zeros
                // (output will be cleared soon anyway)
                for (int i = 0; i < end - start; i++) {
                    chars[i] = 0;
                }
                return chars;
            }
            int fileStart = toByteIndex(start);
            int byteCount = toByteIndex(end - start);
            BufferResource<ByteBuffer> br = null;
            try {
                br = getStorage().getReadBuffer(fileStart, byteCount);
                CharBuffer chb = br.getBuffer().asCharBuffer();
                //#68386 satisfy the request as much as possible, but if there's not enough remaining
                // content, not much we can do..
                int len = Math.min(end - start, chb.remaining());
                chb.get(chars, 0, len);
                return chars;
            } catch (Exception e) {
                handleException (e);
                return new char[0];
            } finally {
                if (br != null) {
                    br.releaseBuffer();
                }
            }
        }
    }

    private BufferResource<CharBuffer> getCharBuffer(int start, int len) {
        if (len < 0 || start < 0) {
            throw new IllegalArgumentException ("Illogical text range from " + start + " to " + (start + len));
        }
        synchronized(readLock()) {
            if (isDisposed()) {
                return null;
            }
            int fileStart = AbstractLines.toByteIndex(start);
            int byteCount = AbstractLines.toByteIndex(len);
            int available = getStorage().size();
            if (available < fileStart + byteCount) {
                throw new ArrayIndexOutOfBoundsException ("Bytes from " +
                    fileStart + " to " + (fileStart + byteCount) + " requested, " +
                    "but storage is only " + available + " bytes long");
            }
            BufferResource<ByteBuffer> readBuffer = null;
            try {
                readBuffer = getStorage().getReadBuffer(fileStart, byteCount);
                return new CharBufferResource(readBuffer);
            } catch (Exception e) {
                if (readBuffer != null) {
                    readBuffer.releaseBuffer();
                }
                return null;
            }
        }
    }

    @Override
    public String getText(int start, int end) {
        BufferResource<CharBuffer> br = getCharBuffer(start, end - start);
        try {
            CharBuffer cb = br == null ? null : br.getBuffer();
            String s = cb != null ? cb.toString() : new String(new char[end - start]);
            return s;
        } finally {
            if (br != null) {
                br.releaseBuffer();
            }
        }
    }

    void onDispose(int lastStorageSize) {
        this.lastStorageSize = lastStorageSize;
    }

    int getByteSize() {
        synchronized (readLock()) {
            if (lastStorageSize >= 0) {
                return lastStorageSize;
            }
            Storage storage = getStorage();
            return storage == null ? 0 : storage.size();
        }
    }

    private ChangeListener listener = null;
    public void addChangeListener(ChangeListener cl) {
        this.listener = cl;
        synchronized(readLock()) {
            if (getLineCount() > 0) {
                //May be a new tab for an old output, hide and reshow, etc.
                fire();
            }
        }
    }

    public void removeChangeListener (ChangeListener cl) {
        if (listener == cl) {
            listener = null;
        }
    }

    private javax.swing.Timer timer = null;
    private final AtomicBoolean newEvent = new AtomicBoolean(false);

    public void actionPerformed(ActionEvent e) {
        newEvent.set(false);
        fire();
        synchronized (newEvent) {
            if (!newEvent.get()) {
                timer.stop();
            }
        }
    }

    void delayedFire() {
        newEvent.set(true);
        if (listener == null) {
            return;
        }
        if (timer == null) {
            timer = new javax.swing.Timer(200, this);
        }

        synchronized (newEvent) {
            if (newEvent.get() && !timer.isRunning()) {
                timer.start();
            }
        }
    }

    public void fire() {
        if (Controller.LOG) Controller.log (this + ": Writer firing " + getStorage().size() + " bytes written");
        if (listener != null) {
            Mutex.EVENT.readAccess(this);
        }
    }

    public void run() {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    public boolean hasListeners() {
        return firstListenerLine() != -1;
    }

    public OutputListener getListener(int pos, int[] range) {
        int line = getLineAt(pos);
        int lineStart = getLineStart(line);
        pos -= lineStart;
        LineInfo info = (LineInfo) lineWithListenerToInfo.get(line);
        if (info == null) {
            return null;
        }
        int start = 0;
        for (LineInfo.Segment seg : info.getLineSegments()) {
            if (pos < seg.getEnd()) {
                if (seg.getListener() != null) {
                    if (range != null) {
                        range[0] = lineStart + start;
                        range[1] = lineStart + seg.getEnd();
                    }
                    return seg.getListener();
                } else {
                    return null;
                }
            }
            start = seg.getEnd();
        }
        return null;
    }

    public boolean isListener(int start, int end) {
        int[] range = new int[2];
        OutputListener l = getListener(start, range);
        return l == null ? false : (range[0] == start && range[1] == end);
    }

    private void init() {
        knownLogicalLineCounts = null;
        lineStartList = new IntList(128);
        lineStartList.add(0);
        lineCharLengthListWithTabs = new IntListSimple(100);
        linesToInfos = new IntMap();
        lineWithListenerToInfo = new IntMap();
        longestLineLen = 0;
        listener = null;
        dirty = false;
        curDefColors = getDefColors().clone();
    }

    private boolean dirty;

    public boolean checkDirty(boolean clear) {
        if (isDisposed()) {
            return false;
        }
        boolean wasDirty = dirty;
        if (clear) {
            dirty = false;
        }
        return wasDirty;
    }

    public int[] getLinesWithListeners() {
        return lineWithListenerToInfo.getKeys();
    }

    public int getCharCount() {
        return AbstractLines.toCharIndex(getByteSize());
    }

    /**
     * Get a single getLine as a string.
     */
    public String getLine (int idx) throws IOException {
        int lineStart = getCharLineStart(idx);
        int lineEnd = toCharIndex(idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) : getByteSize());
        return getText(lineStart, lineEnd);
    }

    /**
     * Get line length (in characters), not counting input characters on the
     * last line.
     *
     * This method returns the same result as {@code getLine(idx).length()}, but
     * is much faster and cheaper, because the text doesn't have to be
     * constructed (see bug bug 250084).
     *
     * Method {@link #length(int)} is very similar, but returns different value
     * for the last line (it counts also input characters).
     *
     * @param idx Line index.
     *
     * @see #getLine(int)
     */
    private int getLineLength(int idx) {
        int lineStart = getCharLineStart(idx);
        int lineEnd = toCharIndex(idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) : getByteSize());
        return Math.max(0, lineEnd - lineStart);
    }

    /**
     * Get a length of single line in bytes.
     */
    private int getByteLineLength(int idx) {
        if (idx == lineStartList.size()-1) {
            return Math.max(0, toByteIndex(lastLineLength));
        }
        int lineStart = getByteLineStart(idx);
        int lineEnd = idx < lineStartList.size() - 1 ? lineStartList.get(idx + 1) - 2 * OutWriter.LINE_SEPARATOR.length() : getByteSize();
        return lineEnd - lineStart;
    }

    public boolean isLineStart (int chpos) {
        int bpos = toByteIndex(chpos);
        return lineStartList.contains (bpos) || bpos == 0 || (bpos == getByteSize() && lastLineFinished);
    }

    /**
     * Returns the length of the specified lin characters.
     *
     * @param idx A getLine number
     * @return The number of characters
     */
    public int length (int idx) {
        return toCharIndex(getByteLineLength(idx));
    }

    /**
     * Returns the length of the specified lin characters.
     *
     * @param idx A getLine number
     * @return The number of characters
     */
    public int lengthWithTabs (int idx) {
        if (idx == lineCharLengthListWithTabs.size()) {
            return Math.max(0, lastCharLengthWithTabs);
        }
        return lineCharLengthListWithTabs.get(idx);
    }

    /**
     * Get the <strong>character</strong> index of a getLine as a position in
     * the output file.
     */
    public int getLineStart (int line) {
        return getCharLineStart(line);
    }

    private int getByteLineStart(int line) {
        if (line == lineStartList.size() && lastLineFinished) {
            return getByteSize();
        }
        return lineStartList.get(line);
    }

    private int getCharLineStart(int line) {
        return toCharIndex(getByteLineStart(line));
    }

    /** Get the getLine number of a <strong>character</strong> index in the
     * file (as distinct from a byte position)
     */
    public int getLineAt (int position) {
        int bytePos = toByteIndex (position);
        if (bytePos >= getByteSize()) {
            return getLineCount() - 1;
        }
        return lineStartList.findNearest(bytePos);
    }

    public int getLineCount() {
        synchronized (readLock()) {
            return lineStartList.size();
        }
    }

    public Collection<OutputListener> getListenersForLine(int line) {
        LineInfo info = (LineInfo) lineWithListenerToInfo.get(line);
        if (info == null) {
            return Collections.emptyList();
        }
        return info.getListeners();
    }

    public int firstListenerLine () {
        if (isDisposed()) {
            return -1;
        }
        return lineWithListenerToInfo.isEmpty() ? -1 : lineWithListenerToInfo.first();
    }

    public OutputListener nearestListener(int pos, boolean backward, int[] range) {
        int posLine = getLineAt(pos);
        int line = lineWithListenerToInfo.nearest(posLine, backward);
        if (line < 0) {
            return null;
        }
        LineInfo info = (LineInfo) lineWithListenerToInfo.get(line);
        int lineStart = getLineStart(line);
        OutputListener l = null;
        int[] lpos = new int[2];
        if (posLine == line) {
            if (backward) {
                info.getFirstListener(lpos);
                if (lpos[0] + lineStart > pos) {
                    line = lineWithListenerToInfo.nearest(line - 1, backward);
                    info = (LineInfo) lineWithListenerToInfo.get(line);
                    lineStart = getLineStart(line);
                    l = info.getLastListener(lpos);
                }
            } else {
                info.getLastListener(lpos);
                if (lpos[1] + lineStart <= pos) {
                    line = lineWithListenerToInfo.nearest(line + 1, backward);
                    info = (LineInfo) lineWithListenerToInfo.get(line);
                    lineStart = getLineStart(line);
                    l = info.getFirstListener(lpos);
                }
            }
        } else {
            pos = lineStart;
            l = backward ? info.getLastListener(lpos) : info.getFirstListener(lpos);
        }
        if (l == null) {
            l = backward ? info.getListenerBefore(pos - lineStart, lpos) : info.getListenerAfter(pos - lineStart, lpos);
        }
        if (l != null) {
            range[0] = lpos[0] + lineStart;
            range[1] = lpos[1] + lineStart;
        }
        return l;
    }

    public int getLongestLineLength() {
        return longestLineLen;
    }

    /** recalculate logical (wrapped) line index to physical (real) line index
     *  <p> [in]
     *  info[0] - "global" logical (wrapped) line index
     *  <p> [out]
     *  info[0] - physical (real) line index;
     *  info[1] - index of wrapped line on current realLineIdx;
     *  info[2] - total number of wrapped lines of found physical line
     */
    public void toPhysicalLineIndex(final int[] info, int charsPerLine) {
        int logicalLineIdx = info[0];

        if (logicalLineIdx <= 0) {
            //First getLine never has lines above it
            info[0] = 0;
            info[1] = 0;
            info[2] = lengthToLineCount(lengthWithTabs(0), charsPerLine);
            return;
        }

        if (charsPerLine >= longestLineLen || (getLineCount() < 1)) {
            //The doc is empty, or there are no lines long enough to wrap anyway
            info[0] = visibleToRealLine(logicalLineIdx);
            info[1] = 0;
            info[2] = 1;
            return;
        }

        // find physical (real) line index which corresponds to logical line idx
        int physLineIdx = Math.min(findPhysicalLine(logicalLineIdx, charsPerLine), getLineCount() - 1);

        // compute how many logical lines is above our physical line
        int linesAbove = getLogicalLineCountAbove(physLineIdx, charsPerLine);

        int len = lengthWithTabs(physLineIdx);
        int wrapCount = lengthToLineCount(len, charsPerLine);

        info[0] = physLineIdx;
        info[1] = logicalLineIdx - linesAbove;
        info[2] = wrapCount;
    }

    private int findPhysicalLine(int logicalLineIdx, int charsPerLine) {
        if (logicalLineIdx == 0) {
            return 0;
        }
        if (charsPerLine != knownCharsPerLine || knownLogicalLineCounts == null) {
            calcLogicalLineCount(charsPerLine);
        }
        return knownLogicalLineCounts.getNextKey(logicalLineIdx);
    }

    /**
     * Get the number of logical lines if character wrapped at the specified width.
     */
    public int getLogicalLineCountAbove(int line, int charsPerLine) {
        if (line == 0) {
            return 0;
        }
        if (charsPerLine >= longestLineLen) {
            return realToVisibleLine(line);
        }
        if (charsPerLine != knownCharsPerLine || knownLogicalLineCounts == null) {
            calcLogicalLineCount(charsPerLine);
        }
        return knownLogicalLineCounts.get(line - 1);
    }

    /**
     * Get the number of logical lines above a given physical getLine if character
     * wrapped at the specified
     */
    public int getLogicalLineCountIfWrappedAt (int charsPerLine) {
        if (charsPerLine >= longestLineLen) {
            return getVisibleLineCount();
        }
        int lineCount = getLineCount();
        if (charsPerLine == 0 || lineCount == 0) {
            return 0;
        }
        synchronized (readLock()) {
            if (charsPerLine != knownCharsPerLine || knownLogicalLineCounts == null) {
                calcLogicalLineCount(charsPerLine);
            }
            return knownLogicalLineCounts.get(lineCount-1);
        }
    }

    /**
     * Get number of physical characters for the given logical (expanded TABs) length.
     * @param offset
     * @param logicalLength
     * @param tabShiftPtr
     * @return number of physical characters
     */
    public int getNumPhysicalChars(int offset, int logicalLength, int[] tabShiftPtr) {
        synchronized (readLock()) {
            int tabIndex1 = tabCharOffsets.findNearest(offset);
            int tabSum1;
            if (tabIndex1 >= 0) {
                if (tabCharOffsets.get(tabIndex1) < offset) {
                    tabSum1 = tabLengthSums.get(tabIndex1);
                } else if (tabIndex1 > 0) {
                    tabSum1 = tabLengthSums.get(tabIndex1 - 1);
                } else {
                    tabSum1 = 0;
                }
            } else {
                tabSum1 = 0;
            }
            int tabIndex2 = tabCharOffsets.findNearest(offset + logicalLength);
            if (tabIndex2 < 0) {
                return logicalLength;
            }
            int tabSum2 = (tabIndex2 >= 0) ? tabLengthSums.get(tabIndex2) : 0;
            if (tabSum1 == tabSum2) {
                return logicalLength;
            }
            int realLength = logicalLength + tabSum1 - tabSum2; // rough guess, might be too small
            if (realLength < 0) realLength = 0;
            int i = offset + realLength;
            tabIndex2 = tabCharOffsets.findNearest(i);
            if (tabIndex2 < 0) {
                tabIndex2 = - 1;
                tabSum2 = 0;
            } else {
                tabSum2 = tabLengthSums.get(tabIndex2);
            }
            int n = tabCharOffsets.size();
            for (tabIndex2++; tabIndex2 < n; tabIndex2++) {
                if (realLength + tabSum2 - tabSum1 < logicalLength) {
                    int nextTabOffset = tabCharOffsets.get(tabIndex2);
                    tabSum2 = tabLengthSums.get(tabIndex2);
                    if (nextTabOffset - offset + tabSum2 - tabSum1 > logicalLength) {
                        tabSum2 = (tabIndex2 > 0) ? tabLengthSums.get(tabIndex2 - 1) : 0;
                        realLength = logicalLength + tabSum1 - tabSum2;
                        if (realLength > (nextTabOffset - offset)) {
                            realLength = nextTabOffset - offset;
                        }
                        break;
                    } else {
                        realLength = nextTabOffset - offset;
                    }
                } else {
                    if (tabShiftPtr != null) {
                        tabShiftPtr[0] = realLength + tabSum2 - tabSum1 - logicalLength;
                    }
                    break;
                }
            }
            if (realLength < 0) realLength = 0;
            return realLength;
        }
    }

    public int getNumLogicalChars(int offset, int physicalLength) {
        synchronized (readLock()) {
            int tabIndex1 = tabCharOffsets.findNearest(offset);
            int tabSum1;
            if (tabIndex1 >= 0 && tabCharOffsets.get(tabIndex1) < offset) {
                tabSum1 = tabLengthSums.get(tabIndex1);
            } else if (tabIndex1 > 0) {
                tabSum1 = tabLengthSums.get(tabIndex1 - 1);
            } else {
                tabSum1 = 0;
            }

            int tabIndex2 = tabCharOffsets.findNearest(offset + physicalLength - 1);
            if (tabIndex2 < 0) {
                return physicalLength;
            }
            int tabSum2 = tabLengthSums.get(tabIndex2);
            return physicalLength + tabSum2 - tabSum1;
        }
    }

    private void registerLineWithListener(int line, LineInfo info, boolean important) {
        lineWithListenerToInfo.put(line, info);
        if (important) {
            importantLines.add(line);
        }
    }

    private IntList importantLines = new IntList(16);

    public int firstImportantListenerLine() {
        return importantLines.size() == 0 ? -1 : importantLines.get(0);
    }

    public boolean isImportantLine(int line) {
        return importantLines.contains(line);
    }

    /**
     * We use SparseIntList to create a cache which only actually holds
     * the counts for lines that *are* wrapped, and interpolates the rest, so
     * we don't need to create an int[]  as big as the number of lines we have.
     * This presumes that most lines don't wrap.
     */
    private void calcLogicalLineCount(int width) {
        synchronized (readLock()) {
            int lineCount = getLineCount();
            knownLogicalLineCounts = new SparseIntList(30);

            int val = 0;
            for (int i = 0; i < lineCount; i++) {
                if (!isVisible(i)) {
                    knownLogicalLineCounts.add(i, val);
                    continue;
                }
                int len = lengthWithTabs(i);

                if (len > width) {
                    val += lengthToLineCount(len, width);
                    knownLogicalLineCounts.add(i, val);
                } else {
                    val++;
                }
            }
            knownCharsPerLine = width;
        }
    }

    static int lengthToLineCount(int len, int charsPerLine) {
        return len > charsPerLine ? (charsPerLine == 0 ? len : (len + charsPerLine - 1) / charsPerLine) : 1;
    }

    void markDirty() {
        dirty = true;
    }

    boolean isLastLineFinished() {
        return lastLineFinished;
    }

    private boolean lastLineFinished = true;
    private int lastLineLength = -1;
    private int lastCharLengthWithTabs = -1;

    // lineLength with tabs
    private void updateLastLine(int lineIdx, int lineLength) {
        synchronized (readLock()) {
            longestLineLen = Math.max(longestLineLen, lineLength);
            if (knownLogicalLineCounts == null) {
                return;
            }
            if (currentFoldStart >= 0 && (visibleList.get(currentFoldStart) == 0
                    || !isVisible(currentFoldStart))) {
                if (knownLogicalLineCounts.lastIndex() != lineIdx) {
                    knownLogicalLineCounts.add(lineIdx,
                            knownLogicalLineCounts.get(lineIdx - 1));
                }
                return;
            };
            // nummber of logical lines above for knownLogicalLineCounts
            int aboveLineCount;
            boolean alreadyAdded = knownLogicalLineCounts.lastIndex() == lineIdx;
            if (alreadyAdded) {
                assert lastLineFinished == false;
                if (lineLength <= knownCharsPerLine) {
                    knownLogicalLineCounts.removeLast();
                } else {
                    aboveLineCount = knownLogicalLineCounts.lastAdded()
                            - lengthToLineCount(lastCharLengthWithTabs, knownCharsPerLine)
                            + lengthToLineCount(lineLength, knownCharsPerLine);
                    knownLogicalLineCounts.updateLast(lineIdx, aboveLineCount);
                }
            } else {
                if (lineLength <= knownCharsPerLine) {
                    return;
                }
                if (knownLogicalLineCounts.lastIndex() != -1) {
                    //If the cache already has some entries, calculate the
                    //values from the last entry - this is less expensive
                    //than looking it up
                    aboveLineCount = (lineIdx - (knownLogicalLineCounts.lastIndex() + 1)) + knownLogicalLineCounts.lastAdded();
                } else {
                    //Otherwise, it's just the number of lines above this
                    //one - it's the first entry
                    aboveLineCount = Math.max(0, lineIdx-1);
                }
                //Add in the number of times this getLine will wrap
                aboveLineCount += lengthToLineCount(lineLength, knownCharsPerLine);
                knownLogicalLineCounts.add(lineIdx, aboveLineCount);
            }
        }
    }

    public void lineUpdated(int lineStart, int lineLength, int charLengthWithTabs, boolean isFinished) {
        synchronized (readLock()) {
            int charLineLength = toCharIndex(lineLength);
            if (isFinished) {
                charLineLength -= 1;
            }
            int lineIndex = lineStartList.size() - 1;
            updateLastLine(lineIndex, charLengthWithTabs);
            if (isFinished) {
                lineStartList.add(lineStart + lineLength);
                updateFolds(lineIndex);
                lineCharLengthListWithTabs.add(charLengthWithTabs);
            }
            lastLineFinished = isFinished;
            lastLineLength = isFinished ? -1 : charLineLength;
            lastCharLengthWithTabs = isFinished ? -1 : charLengthWithTabs;
        }
        markDirty();
    }

    /**
     * Update data structures with info about folds. Called after a new line is
     * finished.
     */
    private void updateFolds(int lineIndex) {
        if (currentFoldStart >= visibleList.size()) {
            LOG.log(Level.FINE, "currentFoldStart = {0}, visibleList" //NOI18N
                    + ".size() = {1}: Forgetting current fold.", //NOI18N
                    new Object[]{currentFoldStart, visibleList.size()});
            currentFoldStart = -1; // #229544, caused e.g. by invalid FoldHandle
        }
        if (currentFoldStart == -1) {
            foldOffsets.add(0);
        } else {
            foldOffsets.add(lineIndex - currentFoldStart);
        }
        if (currentFoldStart != -1 && (visibleList.get(currentFoldStart) == 0
                || !isVisible(currentFoldStart))) {
            hiddenLines++;
            realToVisibleLine.add(-1);
        } else {
            visibleToRealLine.add(lineIndex);
            realToVisibleLine.add(lineIndex - hiddenLines);
        }
        visibleList.add(1);
    }

    void setCurrentFoldStart(int foldStart) {
        synchronized (readLock()) {
            if (foldStart > -1 && foldStart + 1 < foldOffsets.size()
                    && foldOffsets.get(foldStart + 1) != 1) {
                LOG.log(Level.FINE, "Ignoring currentFoldStart at " //NOI18N
                        + "{0}, because foldOffset at {1} is {2}", //NOI18N
                        new Object[]{foldStart, foldStart + 1, foldOffsets.get(
                            foldStart + 1)});
                this.currentFoldStart = -1;
            } else if (foldStart >= foldOffsets.size()) {
                LOG.log(Level.FINE, "Ignoring currentFoldStart at " //NOI18N
                        + "{0}, foldOffsets.size() is only {1}", //NOI18N
                        new Object[]{foldStart, foldOffsets.size()});
                this.currentFoldStart = -1;
            } else {
                this.currentFoldStart = foldStart;
            }
        }
    }

    IntListSimple getFoldOffsets() {
        return this.foldOffsets;
    }

    /** Convert an index from chars to byte count (*2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toByteIndex (int charIndex) {
        return charIndex << 1;
    }

    /** Convert an index from bytes to chars (/2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toCharIndex (int byteIndex) {
        assert byteIndex % 2 == 0 : "bad index: " + byteIndex;  //NOI18N
        return byteIndex >> 1;
    }

    public void saveAs(String path) throws IOException {
        Storage storage = getStorage();
        if (storage == null) {
            throw new IOException ("Data has already been disposed"); //NOI18N
        }
        FileOutputStream fos = new FileOutputStream(path);
        try {
            String encoding = System.getProperty ("file.encoding"); //NOI18N
            if (encoding == null) {
                encoding = "UTF-8"; //NOI18N
            }
            Charset charset = Charset.forName (encoding); //NOI18N
            CharsetEncoder encoder = charset.newEncoder ();
            String ls = System.getProperty("line.separator");
            FileChannel ch = fos.getChannel();
            ByteBuffer lsbb = encoder.encode(CharBuffer.wrap(ls));
            for (int i = 0; i < getLineCount(); i++) {
                int lineStart = getCharLineStart(i);
                int lineLength = length(i);
                BufferResource<CharBuffer> br = getCharBuffer(lineStart,
                        lineLength);
                try {
                    CharBuffer cb = br.getBuffer();
                    ByteBuffer bb = encoder.encode(cb);
                    ch.write(bb);
                    if (i != getLineCount() - 1) {
                        lsbb.rewind();
                        ch.write(lsbb);
                    }
                } finally {
                    if (br != null) {
                        br.releaseBuffer();
                    }
                }
            }
            ch.close();
        } finally {
            fos.close();
            FileUtil.refreshFor(new java.io.File(path));
        }
    }

    /** initial default colors */
    private static Color[] DEF_COLORS = null;

    /** current default colors */
    Color[] curDefColors;

    static Color[] getDefColors() {
        if (DEF_COLORS != null) {
            return DEF_COLORS;
        }
        return DEF_COLORS = new Color[]{
            OutputOptions.getDefault().getColorStandard(),
            OutputOptions.getDefault().getColorError(),
            OutputOptions.getDefault().getColorLink(),
            OutputOptions.getDefault().getColorLinkImportant(),
            OutputOptions.getDefault().getColorInput(),
        };
    }

    public void setDefColor(IOColors.OutputType type, Color color) {
        curDefColors[type.ordinal()] = color;
    }

    public Color getDefColor(IOColors.OutputType type) {
        return curDefColors[type.ordinal()];
    }

    public LineInfo getLineInfo(int line) {
        synchronized (readLock()) {
            LineInfo info = (LineInfo) linesToInfos.get(line);
            if (info != null) {
                int lineLength = length(line);
                if (lineLength > info.getEnd()) {
                    // This is an input
                    info.addSegment(lineLength, OutputKind.IN, null, null, null, false);
                }
                return info;
            } else {
                // The last line can contain input
                if (line == getLineCount() - 1) {
                    LineInfo li = new LineInfo(this);
                    li.addSegment(getLineLength(line), OutputKind.OUT, null, null, null, false);
                    li.addSegment(length(line), OutputKind.IN, null, null, null, false);
                    return li;
                } else {
                    return new LineInfo(this, length(line));
                }
            }
        }
    }

    public LineInfo getExistingLineInfo(int line) {
        return (LineInfo) linesToInfos.get(line);
    }

    private static final int MAX_FIND_SIZE = 16*1024;
    private Pattern pattern;

    private boolean regExpChanged(String pattern, boolean matchCase) {
        return this.pattern != null && (!this.pattern.toString().equals(pattern) || (this.pattern.flags() == Pattern.CASE_INSENSITIVE) == matchCase);
    }

    public int[] find(int start, String pattern, boolean regExp, boolean matchCase) {
        Storage storage = getStorage();
        if (storage == null) {
            return null;
        }
        if (regExp && regExpChanged(pattern, matchCase)) {
            this.pattern = null;
        }
        if (!regExp && !matchCase) {
            pattern = pattern.toLowerCase();
        }
        while (true) {
            BufferResource<ByteBuffer> br = null;
            int size = getCharCount() - start;
            if (size > MAX_FIND_SIZE) {
                int l = getLineAt(start + MAX_FIND_SIZE);
                size = getLineStart(l) + length(l) - start;
            } else if (size <= 0) {
                break;
            }
            CharBuffer buff = null;
            try {
                try {
                    br = storage.getReadBuffer(toByteIndex(start), toByteIndex(size));
                    buff = br.getBuffer().asCharBuffer();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (buff == null) {
                    break;
                }
                if (regExp) {
                    if (this.pattern == null) {
                        this.pattern = matchCase
                                ? Pattern.compile(pattern)
                                : Pattern.compile(pattern,
                                Pattern.CASE_INSENSITIVE);
                    }
                    Matcher matcher = this.pattern.matcher(buff);
                    if (matcher.find()) {
                        return new int[]{start + matcher.start(), start + matcher.end()};
                    }
                } else {
                    int idx = matchCase ? buff.toString().indexOf(pattern)
                            : buff.toString().toLowerCase().indexOf(pattern);
                    if (idx != -1) {
                        return new int[]{start + idx, start + idx + pattern.length()};
                    }
                }
                start += buff.length();
            } finally {
                if (br != null) {
                    br.releaseBuffer();
                }
            }
        }
        return null;
    }

    public int[] rfind(int start, String pattern, boolean regExp, boolean matchCase) {
        Storage storage = getStorage();
        if (storage == null) {
            return null;
        }
        if (regExp && regExpChanged(pattern, matchCase)) {
            this.pattern = null;
        }
        if (!regExp && !matchCase) {
            pattern = pattern.toLowerCase();
        }
        while (true) {
            int end = start;
            start = end - MAX_FIND_SIZE;
            if (start < 0) {
                start = 0;
            } else {
                int l = getLineAt(start);
                start = getLineStart(l);
            }
            if (start == end) {
                break;
            }
            BufferResource<ByteBuffer> br = null;
            try {
                CharBuffer buff = null;
                try {
                    br = storage.getReadBuffer(toByteIndex(start), toByteIndex(end - start));
                    buff = br.getBuffer().asCharBuffer();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (buff == null) {
                    break;
                }
                if (regExp) {
                    if (this.pattern == null) {
                        this.pattern = matchCase
                                ? Pattern.compile(pattern)
                                : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    }
                    Matcher matcher = this.pattern.matcher(buff);
                    int mStart = -1;
                    int mEnd = -1;
                    while (matcher.find()) {
                        mStart = matcher.start();
                        mEnd = matcher.end();
                    }
                    if (mStart != -1) {
                        return new int[]{start + mStart, start + mEnd};
                    }
                } else {
                    int idx = matchCase ? buff.toString().lastIndexOf(pattern)
                            : buff.toString().toLowerCase().lastIndexOf(pattern);
                    if (idx != -1) {
                        return new int[]{start + idx, start + idx + pattern.length()};
                    }
                }
            } finally {
                if (br != null) {
                    br.releaseBuffer();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return lineStartList.toString();
    }

    private int addSegment(CharSequence s, int offset, int lineIdx, int pos, OutputListener l, boolean important, OutputKind outKind, Color c, Color b) {
        int len = length(lineIdx);
        if (len > 0) {
            LineInfo info = (LineInfo) linesToInfos.get(lineIdx);
            if (info == null) {
                info = new LineInfo(this);
                linesToInfos.put(lineIdx, info);
            }
            int curEnd = info.getEnd();
            if (pos > 0 && pos != curEnd) {
                info.addSegment(pos, OutputKind.OUT, null, null, null, false);
                curEnd = pos;
            }
            if (l != null) {
                int endPos = Math.min(curEnd + s.length() - offset, len);
                int strlen = Math.min(s.length(), offset + len);
                if (s.charAt(strlen - 1) == '\n') {
                    strlen--;
                }
                if (s.charAt(strlen - 1) == '\r') {
                    strlen--;
                }
                int leadingCnt = 0;
                while (leadingCnt + offset < strlen && Character.isWhitespace(s.charAt(offset + leadingCnt))) {
                    leadingCnt++;
                }
                int trailingCnt = 0;
                if (leadingCnt != strlen) {
                    while (trailingCnt < strlen && Character.isWhitespace(s.charAt(strlen - trailingCnt - 1))) {
                        trailingCnt++;
                    }
                }
                if (leadingCnt > 0) {
                    if (info.segments.size() > 0) {
                        // do not underline leading spaces only in the first segment
                        info.addSegment(curEnd + leadingCnt, outKind, l, c, b, important);
                    } else {
                        info.addSegment(curEnd + leadingCnt, OutputKind.OUT, null, null, null, false);
                    }
                }
                info.addSegment(endPos - trailingCnt, outKind, l, c, b, important);
                if (trailingCnt > 0) {
                    // have to underline all trailing spaces (we cannot know if there are more segments)
                    // TODO: do not underline trailing spaces of the last segment
                    info.addSegment(endPos, outKind, l, c, b, important);
                }
                registerLineWithListener(lineIdx, info, important);
            } else {
                info.addSegment(len, outKind, l, c, b, important);
                if (important) {
                    importantLines.add(lineIdx);
                }
            }
        }
        return len;
    }

    void updateLinesInfo(CharSequence s, int startLine, int startPos, OutputListener l, boolean important, OutputKind outKind, Color c, Color b) {
        int offset = 0;
        /* If it's necessary to translate tabs to spaces, use this.
         * But it seems that it works fine without the translation. Translation breaks character indexes.
        CharSequence noTabsStr = s;
        if (l != null) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '\t') {
                    StringBuilder str = new StringBuilder(s.length() + 100);
                    int start = 0;
                    for (int k = i; k < s.length(); k++) {
                        if (s.charAt(k) == '\t') {
                            str.append(s, start, k);
                            int tabLength;
                            synchronized (readLock()) {
                                int tabIndex = tabCharOffsets.findNearest(startPos + k);
                                tabLength = getTabLength(tabIndex);
                            }
                            while (tabLength-- > 0) {
                                str.append(' ');
                            }
                            start = k + 1;
                        }
                    }
                    str.append(s, start, s.length());
                    noTabsStr = str;
                    break;
                }
            }
        }
         */
        synchronized (readLock()) {
            int startLinePos = startPos - getLineStart(startLine);
            for (int i = startLine; i < getLineCount(); i++) {
                offset += addSegment(s, offset, i, startLinePos, l, important, outKind, c, b) + 1;
                startLinePos = 0;
            }
        }
    }

    void addLineInfo(int idx, LineInfo info, boolean important) {
        synchronized (readLock()) {
            linesToInfos.put(idx, info);
            if (!info.getListeners().isEmpty()) {
                registerLineWithListener(idx, info, important);
            }
        }
    }

    private int getTabLength(int i) {
        if (i == 0) {
            return tabLengthSums.get(0);
        } else {
            return tabLengthSums.get(i) - tabLengthSums.get(i-1) + 1;
        }
    }

    int checkLimits() {
        synchronized (readLock()) {
            int lines = getLineCount();
            int chars = getCharCount();
            int infos = linesToInfos.size();
            if (lines >= outputLimits.getMaxLines()
                    || chars >= outputLimits.getMaxChars()
                    || infos >= 524288) { // #239445
                LOG.log(Level.INFO, "Removing old lines: lines: {0},"   //NOI18N
                        + " chars: {1}, infos: {2}", new Object[]{      //NOI18N
                            lines, chars, infos});
                return removeOldLines();
            } else {
                return 0;
            }
        }
    }

    /**
     * Tell the storage that oldest bytes can be forgotten, and update all data
     * structures.
     */
    private int removeOldLines() {
        int newFirstLine = Math.min(outputLimits.getRemoveLines(),
                lineStartList.size() / 2);
        int firstByteOffset = lineStartList.get(newFirstLine);
        lineStartList.compact(newFirstLine, firstByteOffset);
        lineCharLengthListWithTabs.compact(newFirstLine, 0);
        lineWithListenerToInfo.decrementKeys(newFirstLine);
        linesToInfos.decrementKeys(newFirstLine);

        int firstCharOffset = toCharIndex(firstByteOffset);
        int firstTabIndex = tabCharOffsets.findNearest(firstCharOffset);
        tabCharOffsets.compact(Math.max(0, firstTabIndex), firstCharOffset);
        if (firstTabIndex > 0) {
            tabLengthSums.compact(firstTabIndex,
                    tabLengthSums.get(firstTabIndex - 1));
        }
        int firstImportantLine = importantLines.findNearest(newFirstLine);
        importantLines.compact(Math.max(0, firstImportantLine), newFirstLine);
        knownLogicalLineCounts = null;
        foldOffsets.compact(newFirstLine, 0);
        int foIndex = 0;
        while (foIndex < foldOffsets.size() && foldOffsets.get(foIndex) != 0) {
            foldOffsets.set(foIndex, 0);
            foIndex++;
        }
        visibleList.compact(newFirstLine, 0);
        visibleList.set(0, 1);
        realToVisibleLine.compact(newFirstLine, 0);
        currentFoldStart = Math.max(-1, currentFoldStart - newFirstLine);
        recomputeRealToVisibleLine();
        updateVisibleToRealLines(0);
        getStorage().shiftStart(firstByteOffset);
        fire();
        return firstByteOffset;
    }

    private void recomputeRealToVisibleLine() {
        int currentVisibleLine = 0;
        int currentHiddenLineCount = 0;
        int hiddenFoldStart = -1;
        for (int i = 0; i < realToVisibleLine.size(); i++) {
            if (hiddenFoldStart == -1 && visibleList.get(i) == 0) {
                hiddenFoldStart = i;
                realToVisibleLine.set(i, currentVisibleLine++);
            } else if (hiddenFoldStart > -1 &&foldOffsets.get(i) > 0
                    && foldOffsets.get(i) <= i - hiddenFoldStart) {
                realToVisibleLine.set(i, -1);
                currentHiddenLineCount++;
            } else {
                realToVisibleLine.set(i, currentVisibleLine++);
                hiddenFoldStart = -1;
            }
        }
        hiddenLines = currentHiddenLineCount;
    }

    /**
     * Redefine output limits. Can be called from test cases.
     */
    void setOutputLimits(OutputLimits outputLimits) {
        this.outputLimits = outputLimits;
    }

    void addTabAt(int i, int tabLength) {
        tabLength--;    // substract the tab character as such, to have the extra length
        synchronized (readLock()) {
            LOG.log(Level.FINEST, "addTabAt: i = {0}", i);   // #201450 //NOI18N
            tabCharOffsets.add(i);
            int n = tabLengthSums.size();
            if (n > 0) {
                tabLength += tabLengthSums.get(n - 1);
            }
            tabLengthSums.add(tabLength);
        }
    }

    /**
     * Remove last tab.
     *
     * @return Size of the last tab (characters).
     */
    int removeLastTab() {
        synchronized (readLock()) {
            LOG.log(Level.FINEST, "removeLastTabAt");
            int size = tabLengthSums.size();
            if (size == 0) {
                return 0;
            }
            tabCharOffsets.shorten(tabCharOffsets.size() - 1);
            int tabLen;
            if (size > 1) {
                tabLen = tabLengthSums.get(size - 1) - tabLengthSums.get(size - 2);
            } else {
                tabLen = tabLengthSums.get(0);
            }
            tabLengthSums.shorten(size - 1);
            return tabLen;
        }
    }

    private boolean isFoldStartValid(int foldStartIndex) {
        return foldStartIndex >= 0 && foldStartIndex < foldOffsets.size();
    }

    /**
     * Character buffer resource for a Byte buffer resource. At most one
     * CharBufferResource can exists for a ByteBufferResource.
     */
    private class CharBufferResource implements BufferResource<CharBuffer> {

        private BufferResource<ByteBuffer> parentResource;
        private CharBuffer cb;

        public CharBufferResource(BufferResource<ByteBuffer> parentResource) {
            this.parentResource = parentResource;
            this.cb = parentResource.getBuffer().asCharBuffer();
        }

        @Override
        public CharBuffer getBuffer() {
            return cb;
        }

        @Override
        public void releaseBuffer() {
            cb = null;
            parentResource.releaseBuffer();
        }
    }

    @Override
    public void showFold(int foldStartIndex) {
        synchronized (readLock()) {
            if (isFoldStartValid(foldStartIndex)) {
                setFoldExpanded(foldStartIndex, true);
            }
        }
    }

    @Override
    public void hideFold(int foldStartIndex) {
        synchronized (readLock()) {
            if (isFoldStartValid(foldStartIndex)) {
                setFoldExpanded(foldStartIndex, false);
            }
        }
    }

    /**
     * @param foldStartIndex Real index of the first line of the fold.
     */
    private void setFoldExpanded(int foldStartIndex, boolean expanded) {
        synchronized (readLock()) {
            if (visibleList.get(foldStartIndex) == (expanded ? 1 : 0)) {
                return;
            }
            visibleList.set(foldStartIndex, expanded ? 1 : 0);
            if (!isVisible(foldStartIndex)) {
                return; // No need to recompute any mapping.
            }
            int len = foldLength(foldStartIndex);
            if (len > 0) {
                int changed = updateRealToVisibleIndexesInFold(foldStartIndex,
                        len, expanded);
                for (int i = foldStartIndex + len + 1;
                        i < realToVisibleLine.size(); i++) {
                    int currentValue = realToVisibleLine.get(i);
                    if (currentValue != -1) {
                        realToVisibleLine.set(i,
                                currentValue + (expanded ? changed : -changed));
                    }
                }
                hiddenLines += expanded ? -changed : changed;
                updateVisibleToRealLines(foldStartIndex);
                foldVisibilityUpdated();
            }
        }
    }

    @Override
    public void hideAllFolds() {
        synchronized (readLock()) {
            int currentVisibleLine = 0;
            int currentHiddenLineCount = 0;
            for (int i = 0; i < foldOffsets.size(); i++) {
                boolean isFoldStart = (i + 1 < foldOffsets.size()
                        && foldOffsets.get(i + 1) == 1);
                if (isFoldStart) {
                    visibleList.set(i, 0);
                }
                if (foldOffsets.get(i) > 0) {
                    realToVisibleLine.set(i, -1);
                    currentHiddenLineCount++;
                } else {
                    realToVisibleLine.set(i, currentVisibleLine);
                    currentVisibleLine++;
                }
            }
            hiddenLines = currentHiddenLineCount;
            updateVisibleToRealLines(0);
            foldVisibilityUpdated();
        }
    }

    @Override
    public void showAllFolds() {
        synchronized (readLock()) {
            for (int i = 0; i < foldOffsets.size(); i++) {
                boolean isFoldStart = (i + 1 < foldOffsets.size()
                        && foldOffsets.get(i + 1) == 1);
                if (isFoldStart) {
                    visibleList.set(i, 1);
                }
                realToVisibleLine.set(i, i);
            }
            hiddenLines = 0;
            updateVisibleToRealLines(0);
            foldVisibilityUpdated();
        }
    }

    @Override
    public void showFoldTree(int foldStartIndex) {
        synchronized (readLock()) {
            if (isFoldStartValid(foldStartIndex)) {
                setFoldTreeExpanded(foldStartIndex, true);
            }
        }
    }

    @Override
    public void showFoldAndParentFolds(int foldStartIndex) {
        synchronized (readLock()) {
            int foldOffset = getFoldOffsets().get(foldStartIndex);
            if (foldOffset > 0) {
                int parentFoldStart = foldStartIndex - foldOffset;
                if (parentFoldStart >= 0) {
                    showFoldAndParentFolds(parentFoldStart);
                }
            }
            showFold(foldStartIndex);
        }
    }

    @Override
    public void showFoldsForLine(int realLineIndex) {
        synchronized (readLock()) {
            int foldStart = getFoldStart(realLineIndex);
            showFoldAndParentFolds(foldStart);
        }
    }

    @Override
    public void hideFoldTree(int foldStartIndex) {
        synchronized (readLock()) {
            if (isFoldStartValid(foldStartIndex)) {
                setFoldTreeExpanded(foldStartIndex, false);
            }
        }
    }

    private void setFoldTreeExpanded(int foldStartIndex, boolean expanded) {
        int visibleValue = expanded ? 1 : 0;
        synchronized (readLock()) {
            assert isVisible(foldStartIndex);
            visibleList.set(foldStartIndex, visibleValue);
            int visibleFoldStartIndex = realToVisibleLine.get(foldStartIndex);
            int delta = 0; // difference in count of visible lines
            int lastUpdatedIndex = Integer.MAX_VALUE - 1;
            for (int i = foldStartIndex + 1; i < foldOffsets.size(); i++) {
                int offset = i - foldStartIndex;
                if (foldOffsets.get(i) == 0 || foldOffsets.get(i) > offset) {
                    break;
                }
                if (i + 1 < foldOffsets.size() && foldOffsets.get(i + 1) == 1) {
                    visibleList.set(i, visibleValue);
                }
                int visibleIndex = realToVisibleLine.get(i);
                if ((expanded && visibleIndex < 0)
                        || (!expanded && visibleIndex >= 0)) {
                    delta += expanded ? 1 : -1;
                }
                realToVisibleLine.set(i,
                        expanded ? visibleFoldStartIndex + offset : -1);
                lastUpdatedIndex = i;
            }
            for (int i = lastUpdatedIndex + 1; i < realToVisibleLine.size(); i++) {
                realToVisibleLine.set(i, realToVisibleLine.get(i) + delta);
            }
            hiddenLines -= delta;
            updateVisibleToRealLines(foldStartIndex);
            foldVisibilityUpdated();
        }
    }

    private void foldVisibilityUpdated() {
        if (knownCharsPerLine > 0) {
            calcLogicalLineCount(knownCharsPerLine);
        }
        markDirty();
        delayedFire();
    }

    /**
     * Update realToVisibleLine indexes in a fold. Handle nested folds, keep
     * their expanded/collapsed state.
     *
     * @param foldStart Real index of the first line of the fold.
     * @param len Total length of the fold, including nested folds
     * @param expanded True to expand the fold, false to collapse the fold.
     *
     * @return Number of newly hidden of shown lines.
     */
    private int updateRealToVisibleIndexesInFold(
            int foldStart, int len, boolean expanded) {

        int changed = 0;
        int nestedHidden = -1; // start index of currently processed hidden fold
        for (int i = 0; i < len; i++) {
            int lineIndex = foldStart + i + 1;
            int foldOffset = foldOffsets.get(lineIndex);
            if (i > 0 && foldOffset == 1 && visibleList.get(lineIndex - 1) == 0
                    && nestedHidden == -1) {
                // a nested hidden fold encountered
                nestedHidden = lineIndex - 1;
            } else if (foldOffset <= i + 1 && (nestedHidden == -1
                    || foldOffset > lineIndex - nestedHidden)) {
                assert !expanded || realToVisibleLine.get(lineIndex) == -1;
                assert expanded || realToVisibleLine.get(lineIndex) != -1;
                nestedHidden = -1;
                changed++;
                realToVisibleLine.set(lineIndex, expanded
                        ? realToVisibleLine.get(foldStart) + changed
                        : -1); // invisible: -1
            } else if (foldOffset <= len + 1 && nestedHidden != -1) {
                assert foldOffset <= lineIndex - nestedHidden;
            } else {
                assert false : "Only nested fold expected";             //NOI18N
            }
        }
        return changed;
    }

    private void updateVisibleToRealLines(int fromRealIndex) {
        for (int i = fromRealIndex; i < getLineCount() - 1; i++) {
            int visibleLine = realToVisibleLine.get(i);
            if (visibleLine == -1) {
                continue;
            }
            if (visibleLine >= visibleToRealLine.size()) {
                visibleToRealLine.add(i);
            } else {
                visibleToRealLine.set(visibleLine, i);
            }
        }
        if (visibleToRealLine.size() > realToVisibleLine.size() - hiddenLines) {
            visibleToRealLine.shorten(realToVisibleLine.size() - hiddenLines);
        }
    }

    @Override
    public int visibleToRealLine(int visibleLineIndex) {
        synchronized (readLock()) {
            if (visibleLineIndex >= visibleToRealLine.size()) {
                return visibleLineIndex + hiddenLines;
            } else if (visibleLineIndex < 0) {
                return visibleLineIndex;
            }
            return visibleToRealLine.get(visibleLineIndex);
        }
    }

    @Override
    public int realToVisibleLine(int realLineIndex) {
        synchronized (readLock()) {
            if (realLineIndex >= realToVisibleLine.size()) {
                return realLineIndex - hiddenLines;
            }
            return realToVisibleLine.get(realLineIndex);
        }
    }

    /**
     * @param lineIndex Real line index.
     */
    @Override
    public boolean isVisible(int lineIndex) {
        synchronized (readLock()) {
            if (lineIndex >= foldOffsets.size()) {
                return true;
            }
            int fo = foldOffsets.get(lineIndex);
            if (fo == 0) {
                return true;
            }
            int parentFold = lineIndex - foldOffsets.get(lineIndex);
            while (parentFold >= 0) {
                if (visibleList.get(parentFold) == 0) {
                    return false;
                } else {
                    fo = foldOffsets.get(parentFold);
                    if (fo == 0) {
                        break;
                    } else {
                        parentFold = parentFold - fo;
                    }
                }
            }
            return true;
        }
    }

    /**
     * @param foldStart Real fold start index.
     */
    int foldLength(int foldStart) {
        int i = foldStart + 1;
        while (i < foldOffsets.size()
                && foldOffsets.get(i) > 0
                && foldOffsets.get(i) <= i - foldStart) {
            i++;
        }
        return i - foldStart - 1;
    }

    @Override
    public int getVisibleLineCount() {
        synchronized (readLock()) {
            return getLineCount() - hiddenLines;
        }
    }

    @Override
    public int getFoldStart(int realLineIndex) {
        synchronized (readLock()) {
            if (realLineIndex + 1 < foldOffsets.size()
                    && foldOffsets.get(realLineIndex + 1) == 1) {
                return realLineIndex;
            } else if (realLineIndex < 0
                    || realLineIndex >= foldOffsets.size()) {
                return Math.max(0, realLineIndex);
            } else {
                return realLineIndex - foldOffsets.get(realLineIndex);
            }
        }
    }

    @Override
    public int getParentFoldStart(int realLineIndex) {
        synchronized (readLock()) {
            if (realLineIndex < 0 || realLineIndex >= foldOffsets.size()) {
                return -1;
            } else {
                int offset = foldOffsets.get(realLineIndex);
                if (offset > 0) {
                    int result = realLineIndex - offset;
                    return result >= 0 ? result : -1;
                } else {
                    return -1;
                }
            }
        }
    }

    /**
     * @param length Value 1 for last character, -1 for the whole line.
     */
    @Override
    public Pair<Integer, Integer> removeCharsFromLastLine(int length) {
        synchronized (readLock()) {
            if (lastLineFinished) {
                return Pair.of(0, 0);
            } else {
                String lastLine;
                try {
                    lastLine = getLine(getLineCount() - 1);
                } catch (IOException e) {
                    LOG.log(Level.INFO, null, e);
                    return Pair.of(0, 0);
                }
                if (lastLine.isEmpty()) {
                    return Pair.of(0, 0);
                } else {
                    int removed;
                    if (length < 0) {
                        removed = lastLine.length();
                    } else {
                        removed = Math.max(length, lastLine.length());
                    }
                    int tabs = 0;
                    for (int i = 0; i < removed; i++) {
                        if (lastLine.charAt(lastLine.length() - 1 - i) == '\t') {
                            tabs += removeLastTab();
                        }
                    }
                    lastLineLength -= removed;
                    return Pair.of(removed, tabs);
                }
            }
        }
    }
}
