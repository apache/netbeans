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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.openide.util.Exceptions;


/** An implementation of Document directly over a memory mapped file such that
 * no (or nearly no) memory copies are required to fetch data to display.
 *
 * @author  Tim Boudreau, Jesse Glick
 */
public class OutputDocument implements Document, Element, ChangeListener {
    private static final Logger LOG =
            Logger.getLogger(OutputDocument.class.getName());
    private List<DocumentListener> dlisteners = new ArrayList<DocumentListener>();
    private volatile Timer timer = null;

    private OutWriter writer;
    
    private StringBuffer inBuffer;
    private AbstractOutputPane pane;
    private int lastInputOff = -1; // offset of start of input string, #142721
   
    /** Creates a new instance of OutputDocument */
    OutputDocument(OutWriter writer) {
        if (Controller.LOG) {
            Controller.log ("Creating a Document for " + writer);
        }
        this.writer = writer;
        getLines().addChangeListener(this);
        inBuffer = new StringBuffer();
    }

    OutWriter getWriter() {
        return writer;
    }

    //#119985
    public int getOutputLength() {
        return getLines().getCharCount();
    }
    
    //#114290
    public void setPane(AbstractOutputPane pane) {
        this.pane = pane;
    }

    /**
     * Destroy this OutputDocument and its backing storage.  The document should not be visible
     * in the UI when this method is called.
     */
    public void dispose() {
        if (Controller.LOG) Controller.log ("Disposing document and backing storage for " + getLines().readLock());
        disposeQuietly();
        writer.dispose();
        writer = null;
    }

    /**
     * Destory this OutputDocument, but not its backing storage.  The document should not be
     * visible in the UI when this method is called.
     */
    public void disposeQuietly() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        dlisteners.clear();
        lastEvent = null;
        getLines().removeChangeListener(this);
    }

    public synchronized void addDocumentListener(DocumentListener documentListener) {
        dlisteners.add (documentListener);
        lastEvent = null;
    }
    
    public void addUndoableEditListener(UndoableEditListener l) {
        //do nothing
    }
    
    public Position createPosition(int offset) throws BadLocationException {
        if (offset < 0 || offset > getLines().getCharCount() + inBuffer.length()) {
            throw new BadLocationException ("Bad position", offset); //NOI18N
        }
        //TODO
        return new ODPosition (offset);
    }
    
    public Element getDefaultRootElement() {
        return this;
    }
    
    public Position getEndPosition() {
        return new ODEndPosition();
    }
    
    public int getLength() {
        return getLines().getCharCount() + inBuffer.length();
    }
    
    public Object getProperty(Object obj) {
        return null;
    }

    public Element[] getRootElements() {
        return new Element[] {this};
    }
    
    public Position getStartPosition() {
        return new ODStartPosition();
    }
    
    public String getText(int offset, int length) throws BadLocationException {
        if (length == 0) {
            return ""; //NOI18N
        }
        String result;
        synchronized (getLines().readLock()) {
            if (offset < 0 || offset + length > getLines().getCharCount() + inBuffer.length() || length < 0) {
                throw new BadLocationException("Bad: " + offset + "," + //NOI18N
                        length + " (" + getLines().getCharCount() + ", " + inBuffer.length() + ")", offset);
            }
            int linesOffset = Math.min(getLines().getCharCount(), offset);
            int linesEnd = Math.min(getLines().getCharCount(), offset + length);
            result = getLines().getText(linesOffset, linesEnd);
            if (offset + length > getLines().getCharCount()) {
                int inEnd = offset + length - getLines().getCharCount();
                result = result + inBuffer.substring(0, inEnd);
            }
        }
        return result;
    }
    
    private char[] reusableSubrange = new char [256];
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        if (length < 0) {
            //document is empty
            txt.array = new char[0];
            txt.offset=0;
            txt.count = 0;
            return;
        }
        
        if (offset < 0) {
            throw new BadLocationException ("Negative offset", offset); //NOI18N
        }
        if (getLines().getLineCount() == -1) {
            txt.array = new char[] {'\n'};
            txt.offset = 0;
            txt.count = 1;
            return;
        }
        if (length > reusableSubrange.length) {
            reusableSubrange = new char[length];
        }
        try {
            synchronized (getLines().readLock()) {
                int charCount = getLines().getCharCount();
                // #180404
                if (charCount < 0) {
                    txt.array = new char[0];
                    txt.offset=0;
                    txt.count = 0;
                    return;
                }
                int linesOffset = Math.min(charCount, offset);
                int linesEnd = Math.min(charCount, offset + length);
                char[] chars = getLines().getText(linesOffset, linesEnd, reusableSubrange);
                if (offset + length > charCount) {
                    int inEnd = offset - charCount + length;
                    int inStart = Math.max(0, offset - charCount);
                    // calling Math.min to prevent nasty AOOBE wich seem to come out of nowhere..
                    inBuffer.getChars(Math.min(inStart, inBuffer.length()), Math.min(inEnd, inBuffer.length()), 
                            chars, linesEnd - linesOffset);
                }
                txt.array = chars;
                txt.offset = 0;
                txt.count = Math.min(length, chars.length);
            }
        } catch (OutOfMemoryError error) {
            //#50189 - try to salvage what we can
            OutWriter.lowDiskSpace = true;
            //mkleint: is not necessary low disk space, can also mean too many mapped buffers were requirested too fast..
            //Sets the error flag and releases the storage
            writer.dispose();
            Logger.getAnonymousLogger().log(Level.WARNING,
                "OOME while reading output.  Cleaning up.", //NOI18N
            error);
            System.gc();
        }
    }
    public void insertString(int offset, String str, AttributeSet attributeSet) throws BadLocationException {
        final int off;
        final int docLen = getLength();
        final int inputOff = docLen - inBuffer.length(); // start of input
        if (inputOff != lastInputOff) { // output written since last input
            lastInputOff = inputOff;
            off = docLen;
        } else if (offset < inputOff) { // cursor before start of input
            off = inputOff;
        } else {
            off = Math.min(offset, inBuffer.length() + inputOff);
        }
        inBuffer.insert(off - inputOff, str);
        final int len = str.length();
        DocumentEvent ev = new DocumentEvent() {
            public int getOffset() {
                return off;
            }

            public int getLength() {
                return len;
            }

            public Document getDocument() {
                return OutputDocument.this;
            }

            public EventType getType() {
                return EventType.INSERT;
            }

            public ElementChange getChange(Element arg0) {
                return null;
            }
        };

        if (getLines() instanceof AbstractLines) {
            AbstractLines lines = (AbstractLines) getLines();
            int start = lines.getLineStart(lines.getLineCount() - 1);
            int length = getLength() - start;
            lines.lineUpdated(2*start, 2*length, length, false);
        }
        fireDocumentEvent(ev);
    }
    
    public String sendLine() {
        final int off = getLength() - inBuffer.length();
        final int len = inBuffer.length();
        String toReturn = inBuffer.toString();
        inBuffer = new StringBuffer();
        DocumentEvent ev = new DocumentEvent() {
            public int getOffset() {
                return off;
            }

            public int getLength() {
                return len;
            }

            public Document getDocument() {
                return OutputDocument.this;
            }

            public EventType getType() {
                return EventType.REMOVE;
            }

            public ElementChange getChange(Element arg0) {
                return null;
            }
        };
        if (getLines() instanceof AbstractLines) {
            AbstractLines lines = (AbstractLines) getLines();
            int start = lines.getLineStart(lines.getLineCount() - 1);
            lines.lineUpdated(2*start, 0, 0, false);
        }        
        fireDocumentEvent(ev);
        return toReturn;
    }
    
    public void putProperty(Object obj, Object obj1) {
        //do nothing
    }
    
    public void remove(int offset, int length) throws BadLocationException {
        int startOff = getLength() - inBuffer.length();
        final int off = Math.max(startOff, offset);
        final int len = Math.min(length, inBuffer.length());
        if (startOff != lastInputOff) { // output written since last input
            lastInputOff = startOff;
            inBuffer.delete(inBuffer.length() - len, inBuffer.length());
        } else if (off - startOff + len <= getLength()) {
            inBuffer.delete(off - startOff, off - startOff + len);
            DocumentEvent ev = new DocumentEvent() {
            public int getOffset() {
                return off;
            }

            public int getLength() {
                return len;
            }

            public Document getDocument() {
                return OutputDocument.this;
            }

            public EventType getType() {
                return EventType.REMOVE;
            }

            public ElementChange getChange(Element arg0) {
                return null;
            }
            };
            if (getLines() instanceof AbstractLines) {
                AbstractLines lines = (AbstractLines) getLines();
                int start = lines.getLineStart(lines.getLineCount() - 1);
                int l = getLength() - start;
                lines.lineUpdated(2*start, 2*l, l, false);
            }            
            fireDocumentEvent(ev);
        }
    }
    
    public synchronized void removeDocumentListener(DocumentListener documentListener) {
        dlisteners.remove(documentListener);
        lastEvent = null;
        if (dlisteners.isEmpty() && timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public Lines getLines() {
        //Unit test will check for null to determine if dispose succeeded
        return writer != null ? writer.getLines() : null;
    }

    public int getLineStart (int line) {
        return getLines().getLineCount() > 0 ? getLines().getLineStart(line) : 0;
    }

    public int getLineEnd (int lineIndex) {
        if (getLines().getLineCount() == 0) {
            return 0;
        }
        int endOffset;
        if (lineIndex >= getLines().getLineCount()-1) {
            endOffset = getLines().getCharCount() + inBuffer.length();
        } else {
            endOffset = getLines().getLineStart(lineIndex+1) - 1;
        }
        return endOffset;
    }

    public void removeUndoableEditListener(UndoableEditListener undoableEditListener) {
        //do nothing
    }
    
    public void render(Runnable runnable) {
        getElementCount(); //Force a refresh of lastPostedLine
        runnable.run();
    }
    
    public AttributeSet getAttributes() {
        return SimpleAttributeSet.EMPTY;
    }
    
    public Document getDocument() {
        return this;
    }
    
    public Element getElement(int index) {
        int realIndex = getLines().visibleToRealLine(index);
        return new ODElement(realIndex);
    }
    
    public int getElementCount() {
        return Math.max(1, getLines().getVisibleLineCount());
    }
    
    public int getElementIndex(int offset) {
        int realLine = getLines().getLineAt(offset);
        if (realLine < 0) {
            return realLine;
        } else {
            return getLines().realToVisibleLine(realLine);
        }
    }
    
    public int getEndOffset() {
        return getLength() + 1;
    }
    
    public String getName() {
        return "foo"; //XXX
    }
    
    public Element getParentElement() {
        return null;
    }
    
    public int getStartOffset() {
        return 0;
    }
    
    public boolean isLeaf() {
        return false;
    }

    private volatile DO lastEvent = null;
    private int lastFiredLineCount = 0;
    private int lastFiredLength = 0;
    private int lastVisibleLineCount = 0;
    public void stateChanged(ChangeEvent changeEvent) {
        assert SwingUtilities.isEventDispatchThread();

        if (Controller.VERBOSE) Controller.log(changeEvent != null ? "Document got change event from writer" : "Document timer polling");
        if (dlisteners.isEmpty()) {
            if (Controller.VERBOSE) Controller.log("listeners empty, not firing");
            return;
        }
        Lines lines = getLines();
        if (lines.checkDirty(true)) {
            if (lastEvent != null && !lastEvent.isConsumed()) {
                if (Controller.VERBOSE) Controller.log("Last event not consumed, not firing");
                return;
            }
            boolean lastLineChanged;
            int lineCount;
            int visibleLineCount;
            int size;
            synchronized (lines.readLock()) {
                lineCount = lines.getLineCount();
                visibleLineCount = lines.getVisibleLineCount();
                size = lines.getCharCount() + inBuffer.length();
                if (size == lastFiredLength && visibleLineCount == lastVisibleLineCount) {
                    // nothing changed
                    if (Controller.VERBOSE) {
                        Controller.log("Size is same " + size + " - not firing");
                    }
                    return;
                }
                lastLineChanged = lastFiredLineCount == lineCount;
                if (lastFiredLineCount > 0 && lineCount > lastFiredLineCount) {
                    int lastFiredLineEnd = lines.getLineStart(lastFiredLineCount);
                    if (lastFiredLineEnd > lastFiredLength) {
                        lastLineChanged = true;
                    }
                }
            }
            if (lastFiredLineCount == lineCount
                    && lastVisibleLineCount != visibleLineCount) {
                lastEvent = new DO(0);
            } else {
                lastEvent = new DO(
                        lastLineChanged
                        ? lastFiredLineCount - 1
                        : lastFiredLineCount);
            }
            lastFiredLineCount = lineCount;
            lastVisibleLineCount = visibleLineCount;
            lastFiredLength = size;

            if (Controller.VERBOSE) Controller.log("Firing document event on EQ with start index " + lastEvent.first);
            fireDocumentEvent(lastEvent);
            if (pane != null) {
                pane.getFoldingSideBar().repaint();
            }
        } else {
            if (Controller.VERBOSE) Controller.log("Writer says it is not dirty, firing no change");
        }
    }

    private void fireDocumentEvent (DocumentEvent de) {
        for (DocumentListener dl: new ArrayList<DocumentListener>(dlisteners)) {
            //#114290
            if (!(de instanceof DO)) {
                if (pane != null) {
                    pane.doUpdateCaret();
                }
            }
            if (de.getType() == DocumentEvent.EventType.REMOVE) {
                dl.removeUpdate(de);
            } else if (de.getType() == DocumentEvent.EventType.CHANGE) {
                dl.changedUpdate(de);
            } else {
                dl.insertUpdate(de);
            }
            //#114290
            if (!(de instanceof DO)) {
                if (pane != null) {
                    pane.dontUpdateCaret();
                }
            }
        }
    }

    static final class ODPosition implements Position {
        private int offset;
        
        ODPosition (int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
        
        @Override
        public int hashCode() {
            return offset * 11;
        }
        
        @Override
        public boolean equals (Object o) {
            return (o instanceof ODPosition) && 
                ((ODPosition) o).getOffset() == offset;
        }
    }
    
    final class ODEndPosition implements Position {
        public int getOffset() {
            return getLines().getCharCount() + inBuffer.length();
        }
        
        private Document doc() {
            return OutputDocument.this;
        }
        
        @Override
        public boolean equals (Object o) {
            return (o instanceof ODEndPosition) && ((ODEndPosition) o).doc() == 
                doc();
        }
        
        @Override
        public int hashCode() {
            return -2390481;
        }
    }
    
    final class ODStartPosition implements Position {
        public int getOffset() {
            return 0;
        }
        
        private Document doc() {
            return OutputDocument.this;
        }
        
        @Override
        public boolean equals (Object o) {
            return (o instanceof ODStartPosition) && ((ODStartPosition) o).doc() == 
                doc();
        }
        
        @Override
        public int hashCode() {
            return 2190481;
        }
    }
    
    final class ODElement implements Element {
        private int lineIndex;
        private int startOffset = -1;
        private int endOffset = -1;
        ODElement (int lineIndex) {
            this.lineIndex = lineIndex;
        }
        
        @Override
        public int hashCode() {
            return lineIndex;
        }
        
        @Override
        public boolean equals (Object o) {
            return (o instanceof ODElement) && ((ODElement) o).lineIndex == lineIndex &&
                ((ODElement) o).getDocument() == getDocument();
        }
        
        public AttributeSet getAttributes() {
            return SimpleAttributeSet.EMPTY;
        }
        
        public Document getDocument() {
            return OutputDocument.this;
        }
        
        public Element getElement(int param) {
            return null;
        }
        
        public int getElementCount() {
            return 0;
        }
        
        public int getElementIndex(int param) {
            return -1;
        }
        
        public int getEndOffset() {
            calc();
            return endOffset;
        }
        
        public String getName() {
            return null;
        }
        
        public Element getParentElement() {
            return OutputDocument.this;
        }
        
        public int getStartOffset() {
            calc();
            return startOffset;
        }
        
        void calc() {
            synchronized (getLines().readLock()) {
                if (lineIndex < 0) {
                    return;
                }
                if (startOffset == -1) {
                    if (lineIndex >= getLines().getLineCount()) {
                        // line has been removed, probably due to reached limit
                        startOffset = 0;
                        endOffset = 0;
                        return;
                    }
                    startOffset = getLines().getLineStart(lineIndex);
                    if (lineIndex >= getLines().getLineCount()-1) {
                        endOffset = getLines().getCharCount() + inBuffer.length() + 1;
                    } else {
                        endOffset = getLines().getLineStart(lineIndex+1);
                    }
                    assert endOffset >= startOffset : "Illogical getLine #" + lineIndex
                        + ", startOffset=" + startOffset + ", endOffset=" + endOffset
                        + ", charCount=" + getLines().getCharCount()
                        + " with lines " + getLines() + " or writer has been reset"
                        + ". writer: " + (writer == null ? "is null" : 
                            ("writer.isDisposed(): " + writer.isDisposed()
                            + ". writer.getStorage(): " + writer.getStorage()));
                } else if (lineIndex >= getLines().getLineCount()-1) {
                    //always recalculate the last line...
                    endOffset = getLines().getCharCount() + inBuffer.length() + 1;
                }
            }
        }
        
        public boolean isLeaf() {
            return true;
        }
        
        @Override
        public String toString() {
            try {
                return OutputDocument.this.getText(getStartOffset(), getEndOffset() 
                    - getStartOffset());
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                return "";
            }
        }
    }

    public class DO implements DocumentEvent, DocumentEvent.ElementChange {
        private int offset = -1;
        private int length = -1;
        private int lineCount = -1;
        private boolean consumed = false;
        private int first = -1;
        DO(int start) {
            this.first = start;
            if (start < 0) {
                throw new IllegalArgumentException ("Illogical start: " + start);
            }
        }
        
        private void calc() {
            //#60414 related assertion. The exceptions in the bug can only happen
            // when this method is called from 2 threads? but that should not be happening
            assert SwingUtilities.isEventDispatchThread() : "Should be accessed from AWT only or we have a synchronization problem"; //NOI18N
            if (!consumed) {
                consumed = true;

                // update lastFired info
                synchronized (getLines().readLock()) {
                    lastFiredLineCount = getLines().getLineCount();
                    lastFiredLength = getLines().getCharCount() + inBuffer.length();

                    // fill event info
                    if (first < lastFiredLineCount) {
                        offset = getLines().getLineStart(first);
                        lineCount = lastFiredLineCount - first;
                        length = lastFiredLength - offset;
                    } else {
                        lineCount = 0;
                        length = 0;
                        offset = 0;
                    }
                }
            }
        }
        
        public boolean isConsumed() {
            return consumed;
        }
        
        @Override
        public String toString() {
            boolean wasConsumed = isConsumed();
            calc();
            return "Event: first=" + first + " linecount=" + lineCount + " offset=" + offset + " length=" + length + " consumed=" + wasConsumed;
        }
        
        public DocumentEvent.ElementChange getChange(Element element) {
            if (element == OutputDocument.this) {
                return this;
            } else {
                return null;
            }
        }
        
        public Document getDocument() {
            return OutputDocument.this;
        }
        
        public int getLength() {
            calc();
            return length;
        }
        
        public int getOffset() {
            calc();
            return offset;
        }
        
        public DocumentEvent.EventType getType() {
            return first == 0 ? DocumentEvent.EventType.CHANGE : 
                DocumentEvent.EventType.INSERT;
        }
        
        public Element[] getChildrenAdded() {
            calc();
            if (first + lineCount > getLines().getLineCount()) {
                 LOG.log(Level.INFO, "Document line count: {0}, OD line count: {1}",
                         new Object[]{getLines().getLineCount(), first + lineCount});
                 return new Element[0];
            }
            Element[] e = new Element[lineCount];
            for (int i = 0; i < lineCount; i++) {
                e[i] = new ODElement(first + i);
            }
            return e;
        }
        
        public Element[] getChildrenRemoved() {
            return new Element[0];
        }
        
        public Element getElement() {
            return OutputDocument.this;
        }
        
        public int getIndex() {
            calc();
            return first;
        }
    }
    
    @Override
    public String toString() {
        return "OD@" + System.identityHashCode(this) + " for " + getLines().readLock();
    }
}
