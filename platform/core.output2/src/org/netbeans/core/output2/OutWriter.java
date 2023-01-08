/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 * Implementation of OutputWriter backed by an implementation of Storage (memory mapped file, heap array, etc).
 *
 * @author  Tim Boudreau
 */
class OutWriter extends PrintWriter {
    private static final Logger LOG =
            Logger.getLogger(OutWriter.class.getName());
    /** A flag indicating an io exception occurred */
    private boolean trouble = false;

    private NbIO owner;
    
    private boolean disposed = false;

    private boolean disposeOnClose = false;

    //IZ 44375 - Memory mapping fails with bad file handle on win 98
    private static final boolean USE_HEAP_STORAGE =
        Boolean.getBoolean("nb.output.heap") || Utilities.getOperatingSystem() == //NOI18N
        Utilities.OS_WIN98 || 
        Utilities.getOperatingSystem() == Utilities.OS_WIN95;

    /**
     * Byte array used to write the line separator after line writes.
     */
    static final String LINE_SEPARATOR = "\n";

    /** The read-write backing storage.  May be heap or */
    private Storage storage;
    
    /** The Lines object that will be used for reading data out of the 
     * storage */
    private AbstractLines lines = new LinesImpl();
    
    /** Flag set if a write failed due to disk space limits.  Subsequent
     * instances will use HeapStorage in this case */
    static boolean lowDiskSpace = false;

    
    /**
     * Need to remember the line start and lenght over multiple calls to
     * write(ByteBuffer), needed to facilitate other calls than println()
     */
    private int lineStart;
    private int lineLength;
    private int lineCharLengthWithTabs;

    /** Creates a new instance of OutWriter */
    OutWriter(NbIO owner) {
        this();
        this.owner = owner;
    }

    /**
     * Package constructor for unit tests
     */
    OutWriter() {
        super (new DummyWriter());
        lineStart = -1;
        lineLength = 0;
        lineCharLengthWithTabs = 0;
    }

    Storage getStorage() {
        if (disposed) {
            throw new IllegalStateException ("Output file has been disposed!");
        }
        if (storage == null) {
            storage = USE_HEAP_STORAGE || lowDiskSpace ? 
                new HeapStorage() : new FileMapStorage();
        }
        return storage;
    }
    
    boolean hasStorage() {
        return storage != null;
    }
    
    boolean isDisposed() {
        return disposed;
    }
    
    boolean isEmpty() {
        return storage == null ? true : storage.size() == 0;
    }

    @Override
    public String toString() {
        return "OutWriter@" + System.identityHashCode(this) + " for " + owner + " closed ";
    }

    private int errorCount = 0;

    /** Generic exception handling, marking the error flag and notifying it with ErrorManager */
    private void handleException(Exception e) {
        setError();
        if (Controller.LOG) {
            StackTraceElement[] el = e.getStackTrace();
            Controller.log("EXCEPTION: " + e.getClass() + e.getMessage());
            for (int i = 1; i < el.length; i++) {
                Controller.log(el[i].toString());
            }
        }
        if (errorCount++ < 3) {
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Write the passed buffer to the backing storage, recording the line start in the mapping of lines to
     * byte offsets.
     *
     * @param bb
     * @throws IOException
     */
    private synchronized void write(ByteBuffer bb, int lineCharLengthWithTabs, boolean completeLine) {
        if (checkError()) {
            return;
        }
        closed = false;
        int start = -1;
        try {
            start = getStorage().write(bb);
        } catch (ClosedByInterruptException cbiex) {
            onWriteException();
        } catch (java.nio.channels.AsynchronousCloseException ace) {
            //Execution termination has sent ThreadDeath to the process in the
            //middle of a write
            Exceptions.printStackTrace(ace);
            onWriteException();
        } catch (IOException ioe) {
            //Out of disk space
            if (ioe.getMessage().indexOf("There is not enough space on the disk") != -1) { //NOI18N
                lowDiskSpace = true;
                String msg = NbBundle.getMessage(OutWriter.class, 
                    "MSG_DiskSpace", storage); //NOI18N
                Exceptions.attachLocalizedMessage(ioe, msg);
                Exceptions.printStackTrace(ioe);
                setError();
                storage.dispose();
            } else {
                //Existing output may still be readable - close, but leave
                //open for reads - if there's a problem there too, the error
                //flag will be set when a read is attempted
                Exceptions.printStackTrace(ioe);
                onWriteException();
            }
        }
        if (checkError()) {
            return;
        }
        int length = bb.limit();
        lineLength = lineLength + length;
        this.lineCharLengthWithTabs += lineCharLengthWithTabs;
        if (start >= 0 && lineStart == -1) {
            lineStart = start;
        }
        lines.lineUpdated(lineStart, lineLength, this.lineCharLengthWithTabs, completeLine);
        if (completeLine) {
            lineStart = -1;
            lineLength = 0;
            this.lineCharLengthWithTabs = 0;
        }
        if (owner != null && owner.isStreamClosed()) {
            owner.setStreamClosed(false);
            lines.fire();
        }
    }

    /**
     * An exception has occurred while writing, which has left us in a readable state, but not
     * a writable one.  Typically this happens when an executing process was sent Thread.stop()
     * in the middle of a write.
     */
    void onWriteException() {
        trouble = true;
        if (Controller.LOG) Controller.log (this + " Close due to termination");
        ErrWriter err = owner.writer().err();
        if (err != null) {
            err.closed=true;
        }
        owner.setStreamClosed(true);
        close();
    }

    /**
     * Dispose this writer.  If reuse is true, the underlying storage will be disposed, but the
     * OutWriter will still be usable.  If reuse if false, note that any current ChangeListener is cleared.
     *
     */
    public synchronized void dispose() {
        if (disposed) {
            //This can happen if a tab was closed, so we were already disposed, but then the
            //ant module tries to reuse the tab -
            return;
        }
        if (Controller.LOG) Controller.log (this + ": OutWriter.dispose - owner is " + (owner == null ? "null" : owner.getName()));
        clearListeners();
        if (storage != null) {
            lines.onDispose(storage.size());
            storage.dispose();
            storage = null;
        }
        if (Controller.LOG) Controller.log (this + ": Setting owner to null, trouble to true, dirty to false.  This OutWriter is officially dead.");
        owner = null;
        disposed = true;
    }


    private void clearListeners() {
        if (Controller.LOG) Controller.log (this + ": Sending outputLineCleared to all listeners");
        if (owner == null) {
            //Somebody called reset() twice
            return;
        }
        synchronized (this) {
            if (lines.hasListeners()) {
                int[] listenerLines = lines.getLinesWithListeners();
                Controller.ControllerOutputEvent e = new Controller.ControllerOutputEvent(owner, 0);
                for (int i=0; i < listenerLines.length; i++) {
                    Collection<OutputListener> ols = lines.getListenersForLine(listenerLines[i]);
                    e.setLine(listenerLines[i]);
                    for (OutputListener ol : ols) {
                        ol.outputLineCleared(e);
                    }
                }
            } else {
                if (Controller.LOG) Controller.log (this + ": No listeners to clear");
            }
        }
    }

    public synchronized boolean isClosed() {
        if (checkError() || disposed || (storage != null && storage.isClosed())) {
            return true;
        } else {
            return closed;
        }
    }

    public Lines getLines() {
        return lines;
    }

    private boolean closed = false;
    @Override
    public synchronized void close() {
        closed = true;
        try {
            //#49955 - possible (but difficult) to end up with close()
            //called twice
            if (storage != null) {
                storage.close();
            }
            lines.fire();
            if (isDisposeOnClose() && !isDisposed()) {
                dispose();
            }
        } catch (IOException ioe) {
            onWriteException();
        }
    }

    /**
     * Is this writer set to dispose automatically when it is closed?
     */
    boolean isDisposeOnClose() {
        return disposeOnClose;
    }

    /**
     * Set the writer to dispose or not to dispose itself automatically when it
     * is closed.
     */
    void setDisposeOnClose(boolean disposeOnClose) {
        this.disposeOnClose = disposeOnClose;
    }

    @Override
    public synchronized void println(String s) {
        doWrite(s, null, 0, s.length());
        println();
    }

    @Override
    public synchronized void flush() {
        if (checkError()) {
            return;
        }
        try {
            getStorage().flush();
            lines.fire();
        } catch (IOException e) {
            onWriteException();
        }
    }

    @Override
    public boolean checkError() {
        return disposed || trouble;
    }

    static class CharArrayWrapper implements CharSequence {

        private char[] arr;
        private int off;
        private int len;

        public CharArrayWrapper(char[] arr) {
            this(arr, 0, arr.length);
        }

        public CharArrayWrapper(char[] arr, int off, int len) {
            this.arr = arr;
            this.off = off;
            this.len = len;
        }

        public char charAt(int index) {
            return arr[off + index];
        }

        public int length() {
            return len;
        }

        public CharSequence subSequence(int start, int end) {
            return new CharArrayWrapper(arr, off + start, end - start);
        }

        @Override
        public String toString() {
            return new String(arr, off, len);
        }
    }

    @Override
    public synchronized void write(int c) {
        doWrite(new String(new char[]{(char)c}), null, 0, 1);
        checkLimits();
    }

    private void checkLimits() {
        int shift = lines.checkLimits();
        if (lineStart > 0) {
            lineStart -= shift;
        }
    }
    
    @Override
    public synchronized void write(char data[], int off, int len) {
        doWrite(new CharArrayWrapper(data), null, off, len);
        checkLimits();
    }
    
    /** write buffer size in chars */
    private static final int WRITE_BUFF_SIZE = 16*1024;
    private synchronized void doWrite(CharSequence s, OutputListener l, int off, int len) {
        if (checkError() || len == 0) {
            return;
        }
        
        // XXX will not pick up ANSI sequences broken across write blocks, but this is likely rare
        if (printANSI(s.subSequence(off, off + len), l, false, OutputKind.OUT, false)) {
            return;
        }
        /* XXX causes stack overflow
        if (ansiColor != null) {
            print(s, null, false, null, false, false);
            return;
        }
         */

        int lineCLVT = 0; // Character Lenght With Tabs
        try {
            boolean written = false;
            char lastChar = 0;
            ByteBuffer byteBuff = getStorage().getWriteBuffer(WRITE_BUFF_SIZE * 2);
            CharBuffer charBuff = byteBuff.asCharBuffer();
            int charOffset = AbstractLines.toCharIndex(getStorage().size());
            int skipped = 0;
            int tabLength = 0; // last tab length
            for (int i = off; i < off + len; i++) {
                if (charBuff.position() + 1 >= WRITE_BUFF_SIZE) {
                    write((ByteBuffer) byteBuff.position(charBuff.position() * 2), lineCLVT, false);
                    written = true;
                }
                if (written) {
                    byteBuff = getStorage().getWriteBuffer(WRITE_BUFF_SIZE * 2);
                    charBuff = byteBuff.asCharBuffer();
                    lineCLVT = 0;
                    written = false;
                }
                char c = s.charAt(i);
                if (lastChar == '\r' && c != '\n') { // \r without following \n
                    int p;
                    for (p = charBuff.position(); p > 0; p--) {
                        char charP1 = charBuff.get(p - 1);
                        if (charP1 == '\n') {
                            break;
                        }
                        if (charP1 == '\t') {
                            lineCLVT -= lines.removeLastTab();
                        }
                        skipped++;
                        charBuff.position(p - 1);
                    }
                    if (p == 0) {
                        clearLine();
                    }
                }
                if (c == '\t') {
                    charBuff.put(c);
                    tabLength = WrappedTextView.TAB_SIZE - ((this.lineCharLengthWithTabs + lineCLVT) % WrappedTextView.TAB_SIZE);
                    LOG.log(Level.FINEST, "Going to add tab: charOffset = {0}, i = {1}, off = {2}," //NOI18N
                            + "tabLength = {3}, tabIndex = {4}, skipped = {5}", //NOI18N
                            new Object[]{charOffset, i, off, tabLength, charOffset + (i - off), skipped}); // #201450
                    lines.addTabAt(charOffset + (i - off) - skipped, tabLength);
                    lineCLVT += tabLength;
                } else if (c == '\b') {
                    int skip = handleBackspace(charBuff);
                    if (skip == -2) {
                        lineCLVT -= lines.removeLastTab();
                    } else if (skip == 2) {
                        lineCLVT--;
                    } else if (skip == 1) {
                        Pair<Integer, Integer> removedInfo;
                        removedInfo = lines.removeCharsFromLastLine(1);
                        storage.removeBytesFromEnd(removedInfo.first() * 2);
                        lineLength -= removedInfo.first() * 2;
                        lineCharLengthWithTabs -= removedInfo.second();
                    }
                    skipped += Math.abs(skip);
                } else if (c == '\n') {
                    charBuff.put('\n');
                    int pos = charBuff.position() * 2;
                    ByteBuffer bf = (ByteBuffer) byteBuff.position(pos);
                    write(bf, lineCLVT, true);
                    written = true;
                } else if (c != '\r') {
                    charBuff.put(c);
                    lineCLVT++;
                } else {
                    assert c == '\r';
                    skipped++;
                }
                lastChar = c;
            }
            if (!written) {
                write((ByteBuffer) byteBuff.position(charBuff.position() * 2), lineCLVT, false);
            }
        } catch (IOException ioe) {
            onWriteException();
        } catch (RuntimeException ex) {
            LOG.log(Level.INFO, "Cannot write text: off={0}, "          //NOI18N
                    + "len={1}, lineStart={2}",                         //NOI18N
                    new Object[]{off, len, lineStart});
            throw ex;
        }
        lines.delayedFire();
        return;
    }

    /**
     * Update state of character buffer after a backspace character has been
     * read.
     *
     * @return Value -2, 1 or 2: number of character that will be skipped (has
     * no corresponding visible character) in the resulting output. In standard
     * case, it is 2 (the last character + the \b character), if the buffer is
     * empty, it is 1 (only the \b character). If tab character was deleted,
     * return -2.
     */
    private int handleBackspace(CharBuffer charBuff) {
        if (charBuff.position() > 0) {
            char deletedChar = charBuff.get(charBuff.position() - 1);
            charBuff.position(charBuff.position() - 1);
            return deletedChar == '\t' ? -2 : 2;
        } else {
            return 1;
        }
    }

    @Override
    public synchronized void write(char data[]) {
        doWrite(new CharArrayWrapper(data), null, 0, data.length);
        checkLimits();
    }

    @Override
    public synchronized void println() {
        printLineEnd();
        checkLimits();
    }

    private void printLineEnd() {
        doWrite("\n", null, 0, 1);
    }

    /**
     * Write a portion of a string.
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    @Override
    public synchronized void write(String s, int off, int len) {
        doWrite(s, null, off, len);
        checkLimits();
    }

    @Override
    public synchronized void write(String s) {
        doWrite(s, null, 0, s.length());
        checkLimits();
    }

    public synchronized void println(String s, OutputListener l) {
        println(s, l, false);
    }

    public synchronized void println(String s, OutputListener l, boolean important) {
        print(s, l, important, null, null, OutputKind.OUT, true);
    }

    synchronized void print(CharSequence s, OutputListener l, boolean important, Color c, Color b, OutputKind outKind, boolean addLS) {
        if (c == null) {
            if (l == null && printANSI(s, null, important, outKind, addLS)) {
                return;
            }
            c = ansiColor; // carry over from previous line
        }
        int lastLine = lines.getLineCount() - 1;
        int lastPos = lines.getCharCount();
        doWrite(s, l, 0, s.length());
        if (addLS) {
            printLineEnd();
        }
        lines.updateLinesInfo(s, lastLine, lastPos, l, important, outKind, c, b);
        checkLimits();
    }
    private Color ansiColor;
    private Color ansiBackground;
    private int ansiColorCode;
    private int ansiBackgroundCode = 9;
    private boolean ansiBright;
    private boolean ansiFaint;
    private static final Pattern ANSI_CSI = Pattern.compile("\u001B\\[(\\d+(;\\d+)*)?(\\p{Alpha})"); // XXX or x9B for single-char CSI?
    private static final Color[] COLORS = { // xterm from http://en.wikipedia.org/wiki/ANSI_escape_code#Colors
        null, // default color (black for stdout)
        new Color(205, 0, 0),
        new Color(0, 205, 0),
        new Color(205, 205, 0),
        new Color(0, 0, 238),
        new Color(205, 0, 205),
        new Color(0, 205, 205),
        new Color(229, 229, 229),
        // bright variants:
        new Color(127, 127, 127),
        new Color(255, 0, 0),
        new Color(0, 255, 0),
        new Color(255, 255, 0),
        new Color(92, 92, 255),
        new Color(255, 0, 255),
        new Color(0, 255, 255),
        new Color(255, 255, 255),
    };
    private boolean printANSI(CharSequence s, OutputListener l, boolean important, OutputKind outKind, boolean addLS) { // #192779
        int len = s.length();
        boolean hasEscape = false; // fast initial check
        for (int i = 0; i < len - 1; i++) {
            if (s.charAt(i) == '\u001B' && s.charAt(i + 1) == '[') { // XXX or x9B for single-char CSI?
                hasEscape = true;
                break;
            }
        }
        if (!hasEscape) {
            return false;
        }
        Matcher m = ANSI_CSI.matcher(s);
        int text = 0;
        while (m.find()) {
            int esc = m.start();
            if (esc > text) {
                print(s.subSequence(text, esc), l, important, ansiColor, ansiBackground, outKind, false);
            }
            text = m.end();
            if ("K".equals(m.group(3)) && "2".equals(m.group(1))) {     //NOI18N
                clearLine();
                continue;
            } else if (!"m".equals(m.group(3))) {                       //NOI18N
                continue; // not a SGR ANSI sequence
            }
            String paramsS = m.group(1);
            if (Controller.VERBOSE) {
                Controller.log("ANSI CSI+SGR: " + paramsS);
            }
            if (paramsS == null) { // like ["0"]
                ansiColorCode = 0;
                ansiBackgroundCode = 9;
                ansiBright = false;
                ansiFaint = false;
            } else {
                for (String param : paramsS.split(";")) {
                    int code = Integer.parseInt(param);
                    if (code == 0) { // Reset / Normal
                        ansiColorCode = 0;
                        ansiBackgroundCode = 9; // default
                        ansiBright = false;
                        ansiFaint = false;
                    } else if (code == 1) { // Bright (increased intensity) or Bold
                        ansiBright = true;
                        ansiFaint = false;
                    } else if (code == 2) { // Faint (decreased intensity)
                        ansiBright = false;
                        ansiFaint = true;
                    } else if (code == 21) { // Bright/Bold: off or Underline: Double
                        ansiBright = false;
                    } else if (code == 22) { // Normal color or intensity
                        ansiBright = false;
                        ansiFaint = false;
                    } else if (code >= 30 && code <= 37) { // Set text color
                        ansiColorCode = code - 30;
                    } else if (code == 39) { // Default text color
                        ansiColorCode = 0;
                    } else if (code >= 40 && code <= 47) { // Set background
                        ansiBackgroundCode = code - 40;
                    } else if (code == 49) { // Set background
                        ansiBackgroundCode = 9; // default color
                    }
                }
            }
            assert ansiColorCode >= 0 && ansiColorCode <= 7;
            assert ansiBackgroundCode >= 0 && ansiBackgroundCode <= 9;
            assert !(ansiBright && ansiFaint);
            Color setColor = COLORS[ansiColorCode + (ansiBright ? 8 : 0)];
            ansiBackground = ansiBackgroundCode == 9 ? null
                    : COLORS[ansiBackgroundCode];
            ansiColor = fixTextColor(setColor, ansiBackground);
            if (ansiFaint && ansiColor != null) {
                ansiColor = ansiColor.darker();
            }
        }
        if (text == 0) {
            // That was an unknown sequence
            return false;
        }
        if (text < len) { // final segment
            print(s.subSequence(text, len), l, important, ansiColor, ansiBackground, outKind, addLS);
        } else if (addLS) { // line ended w/ control seq
            printLineEnd();
        }
        return true;
    }

    /**
     * Clears the current line. Called when ANSI sequence "\u001B[2K" is
     * detected, or a CR (\r) character not followed by LN (\n) is reached.
     */
    private void clearLine() {
        //NOI18N
        Pair<Integer, Integer> r = lines.removeCharsFromLastLine(-1);
        try {
            getStorage().removeBytesFromEnd(r.first() * 2);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        lineLength -= r.first() * 2;
        lineCharLengthWithTabs -= r.second();
    }

    synchronized void print(CharSequence s, LineInfo info, boolean important) {
        int line = lines.getLineCount() - 1;
        doWrite(s, null, 0, s.length());
        if (info != null) {
            lines.addLineInfo(line, info, important);
        }
        checkLimits();
    }

    /**
     * A useless writer object to pass to the superclass constructor.  We override all methods
     * of it anyway.
     */
    static class DummyWriter extends Writer {
        
        DummyWriter() {
            super (new Object());
        }
        
        public void close() throws IOException {
        }
        
        public void flush() throws IOException {
        }
        
        public void write(char[] cbuf, int off, int len) throws IOException {
        }
    }

    private class LinesImpl extends AbstractLines {
        LinesImpl() {
            super();
        }

        protected Storage getStorage() {
            return OutWriter.this.getStorage();
        }

        protected boolean isDisposed() {
             return OutWriter.this.disposed;
        }

        public Object readLock() {
            return OutWriter.this;
        }

        public boolean isGrowing() {
            return !isClosed();
        }

        protected void handleException (Exception e) {
            OutWriter.this.handleException(e);
        }
    }

    /**
     * Fix foreground color if it is not readable on the background.
     */
    private static Color fixTextColor(Color textColor, Color background) {
        if (background == null && textColor != null
                && textColor.equals(Color.WHITE)) {
            return Color.BLACK;
        } else if (textColor != null && background != null
                && colorDiff(textColor, background) < 10) {
            if (colorDiff(textColor, Color.WHITE)
                    > colorDiff(textColor, Color.BLACK)) {
                return Color.WHITE;
            } else {
                return Color.BLACK;
            }
        } else {
            return textColor;
        }
    }

    /**
     * Difference of two colors. The bigger number, the more different the two
     * colors are.
     */
    private static int colorDiff(Color c1, Color c2) {
        int redDiff = Math.abs(c1.getRed() - c2.getRed());
        int greenDiff = Math.abs(c1.getGreen() - c2.getGreen());
        int blueDiff = Math.abs(c1.getBlue() - c2.getBlue());
        return Math.max(redDiff, Math.max(greenDiff, blueDiff));
    }
}
