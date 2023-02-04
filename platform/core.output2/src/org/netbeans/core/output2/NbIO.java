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

import java.util.Set;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOFolding;
import org.openide.windows.IOPosition;
import org.openide.windows.IOSelect;
import org.openide.windows.IOTab;

/** Implementation of InputOutput.  Implements calls as a set of
 * "commands" which are passed up to Dispatcher to be run on the event
 * queue.
 *
 * @author  Tim Boudreau
 */
class NbIO implements InputOutput, Lookup.Provider {

    private Boolean focusTaken = null;
    private volatile boolean closed = false;
    private final String name;
    private OutputOptions options = OutputOptions.getDefault().makeCopy();
    
    private Action[] actions;

    private NbWriter out = null;
    private IOContainer ioContainer;
    private Lookup lookup;
    private IOTabImpl ioTab;
    private IOColorsImpl ioColors;
    private IOFoldingImpl ioFolding;
    private IOFoldingImpl.NbIoFoldHandleDefinition currentFold = null;

    /** Creates a new instance of NbIO 
     * @param name The name of the IO
     * @param toolbarActions an optional set of toolbar actions
     * @param iowin parent container accessor (null for default)
     */
    NbIO(String name, Action[] toolbarActions, IOContainer ioContainer) {
        this(name);
        this.actions = toolbarActions;
        this.ioContainer = ioContainer != null ? ioContainer : IOContainer.getDefault();
    }
    
    /** Package private constructor for unit tests */
    NbIO (String name) {
        this.name = name;
    }
    
    @Override
    public void closeInputOutput() {
        if (Controller.LOG) {
            Controller.log("CLOSE INPUT OUTPUT CALLED FOR " + this);    //NOI18N
        }
        final NbWriter currentOut;
        synchronized (this) {
            currentOut = out;
        }
        if (currentOut != null) {
            if (Controller.LOG) {
                Controller.log(
                        " - Its output is non null, calling close() on "//NOI18N
                        + currentOut);
            }
            currentOut.close();
        }
        post (this, IOEvent.CMD_CLOSE, true);
    }
    
    String getName() {
        return name;
    }

    IOContainer getIOContainer() {
        return ioContainer;
    }

    public OutputWriter getErr() {
        return ((NbWriter) getOut()).getErr();
    }

    synchronized NbWriter writer() {
        return out;
    }

    void dispose() {
        if (Controller.LOG) {
            Controller.log(this + ": IO " + getName() + " is being disposed"); //NOI18N
        }
        OutWriter currentOut = null;
        IOReader currentIn = null;
        synchronized (this) {
            if (out != null) {
                if (Controller.LOG) {
                    Controller.log(this + ": Still has an OutWriter.  Disposing it"); //NOI18N
                }
                currentOut = out();
                out = null;
                if (in != null) {
                    currentIn = in;
                    in = null;
                }
                focusTaken = null;
            }
        }
        if (currentOut != null) {
            currentOut.dispose();
        }
        if (currentIn != null) {
            currentIn.eof();
        }
        NbIOProvider.dispose(this);
    }
        
    public OutputWriter getOut() {
        synchronized (this) {
            if (out == null) {
                OutWriter realout = new OutWriter(this);
                out = new NbWriter(realout, this);
            }
            return out;
        }
    }
    
    /** Called by the view when polling */
    synchronized OutWriter out() {
        return out == null ? null : out.out();
    }

    /**
     * Get the current OutWriter object. If it is null, throw exception.
     *
     * @return The current OutWriter object, never null.
     * @throws IllegalStateException if no OutWriter is available.
     */
    private OutWriter outOrException() {
        OutWriter outWriter = out();
        if (outWriter == null) {
            throw new IllegalStateException("No OutWriter available");  //NOI18N
        } else {
            return outWriter;
        }
    }

    void setClosed (boolean val) {
        closed = val;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isErrSeparated() {
        return false;
    }
    
    public boolean isFocusTaken() {
        return Boolean.TRUE.equals(focusTaken);
    }
    
    synchronized boolean isStreamClosed() {
        return out == null ? true : streamClosed;
    }
    
    public void select() {
        if (Controller.LOG) Controller.log (this + ": select");
        post (this, IOEvent.CMD_SELECT, true);
    }
    
    public void setErrSeparated(boolean value) {
        //do nothing
    }
    
    public void setErrVisible(boolean value) {
        //do nothing
    }
    
    public void setFocusTaken(boolean value) {
        focusTaken = value ? Boolean.TRUE : Boolean.FALSE;
        post (this, IOEvent.CMD_FOCUS_TAKEN, value);
    }
    
    public void setInputVisible(boolean value) {
        if (Controller.LOG) Controller.log(NbIO.this + ": SetInputVisible");
        post (this, IOEvent.CMD_INPUT_VISIBLE, value);
    }
    
    public void setOutputVisible(boolean value) {
        //do nothing
    }

    private boolean streamClosed = false;
    public void setStreamClosed(boolean value) {
        if (streamClosed != value) {
            streamClosed = value;
            post (this, IOEvent.CMD_STREAM_CLOSED, value);
        }
    }

    public void setToolbarActions(Action[] a) {
        this.actions = a;
        post (this, IOEvent.CMD_SET_TOOLBAR_ACTIONS, a);
    }

    Action[] getToolbarActions() {
        return actions;
    }
    
    public void reset() {
        if (Controller.LOG) Controller.log (this + ": reset");
        closed = false;
        streamClosed = false;

        final IOReader currentIn;
        synchronized (this) {
            currentIn = in;
        }
        if (currentIn != null) {
            currentIn.eof();
            currentIn.reuse();
        }
        post (this, IOEvent.CMD_RESET, true);
    }
    
    private static void post (NbIO io, int command, boolean val) {
        IOEvent evt = new IOEvent (io, command, val);
        post (evt);
    }

    private static void post (NbIO io, int command, Object data) {
        IOEvent evt = new IOEvent (io, command, data);
        post (evt);
    }

    static void post (IOEvent evt) {
        if (SwingUtilities.isEventDispatchThread()) {
            if (Controller.LOG) Controller.log ("Synchronously dispatching " + evt + " from call on EQ");
            evt.dispatch();
        } else {
            if (Controller.LOG) Controller.log ("Asynchronously posting " + evt + " to EQ");
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eq.postEvent(evt);
        }
    }
    
    @Override public String toString() {
        return "NbIO@" + System.identityHashCode(this) + " " + getName();
    }

    synchronized IOReader in() {
        return in;
    }

    private IOReader in = null;
    public synchronized Reader getIn() {
        if (in == null) {
            in = new IOReader();
        }
        return in;
    }

    @SuppressWarnings("deprecation")
    public Reader flushReader() {
        return getIn();
    }

    public synchronized IOFoldingImpl getIoFolding() {
        if (ioFolding == null) {
            ioFolding = new IOFoldingImpl();
        }
        return ioFolding;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            ioTab = new IOTabImpl();
            ioColors = new IOColorsImpl();
            ioFolding = getIoFolding();
            lookup = Lookups.fixed(ioTab, ioColors, new IOPositionImpl(),
                    new IOColorLinesImpl(), new IOColorPrintImpl(),
                    new IOSelectImpl(), ioFolding, options);
        }
        return lookup;
    }

    class IOReader extends Reader {
        private boolean pristine = true;
        IOReader() {
            super (new StringBuffer());
        }

        void reuse() {
             pristine = true;
             synchronized (lock) {
                inputClosed = false;
             }
        }

        private StringBuffer buffer() {
            return (StringBuffer) lock;
        }
        
        public void pushText (String txt) {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input text: " + txt);
            synchronized (lock) {
                buffer().append (txt);
                lock.notifyAll();
            }
        }
        
        private boolean inputClosed = false;
        public void eof() {
            synchronized (lock) {
                try {
                    close();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        private void checkPristine() throws IOException {
            if (SwingUtilities.isEventDispatchThread()) {
                throw new IOException ("Cannot call read() from the event thread, it will deadlock");
            }
            if (pristine) {
                NbIO.this.setInputVisible(true);
                pristine = false;
            }
        }
       
        public int read(char cbuf[], int off, int len) throws IOException {
             if (Controller.LOG) Controller.log  (NbIO.this + ":Input read: " + off + " len " + len);
            checkPristine();
            synchronized (lock) {
                while (!inputClosed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException("Interrupted: " +
                                                            e.getMessage()).initCause(e);
                    }
                }
                if (inputClosed) {
                    reuse();
                    return -1;
                }
                int realLen = Math.min (buffer().length(), len);
                buffer().getChars(0, realLen, cbuf, off);
                buffer().delete(0, realLen);
                return realLen;
            }
        }
        
        @Override
        public int read() throws IOException {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input read one char");
            checkPristine();
            synchronized (lock) {
                while (!inputClosed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException("Interrupted: " +
                                                            e.getMessage()).initCause(e);
                    }
                }
                if (inputClosed) {
                    reuse();
                    return -1;
                }
                int i = (int) buffer().charAt(0);
                buffer().deleteCharAt(0);
                return i;
            }
        }

        @Override
        public boolean ready() throws IOException {
            synchronized (lock) {
                if (inputClosed) {
                    reuse();
                    return false;
                }
                return buffer().length() > 0;
            }
        }
        
        @Override
        public long skip(long n) throws IOException {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input skip " + n);
            checkPristine();
            synchronized (lock) {
                while (!inputClosed && buffer().length() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException("Interrupted: " +
                                                            e.getMessage()).initCause(e);
                    }
                }
                if (inputClosed) {
                    reuse();
                    return 0;
                }
                int realLen = Math.min (buffer().length(), (int) n);
                buffer().delete(0, realLen);
                return realLen;
            }
        }

        public void close() throws IOException {
            if (Controller.LOG) Controller.log (NbIO.this + ": Input close");
            setInputVisible(false);
            synchronized (lock) {
                inputClosed = true;
                buffer().setLength(0);
                lock.notifyAll();
            }
        }
        
        public boolean isClosed() {
            return inputClosed;
        }
    }

    Icon getIcon() {
        return ioTab != null ? ioTab.getIcon() : null;
    }

    String getToolTipText() {
        return ioTab != null ? ioTab.getToolTipText() : null;
    }

    void setTooltipText(String toolTip) {
        post(NbIO.this, IOEvent.CMD_SET_TOOLTIP, toolTip);
    }

    Color getColor(IOColors.OutputType type) {
        return ioColors != null ? ioColors.getColor(type) : AbstractLines.getDefColors()[type.ordinal()];
    }

    private class IOTabImpl extends IOTab {
        Icon icon;
        String toolTip;

        @Override
        protected Icon getIcon() {
            return icon;
        }

        @Override
        protected String getToolTipText() {
            return toolTip;
        }

        @Override
        protected void setIcon(Icon icon) {
            this.icon = icon;
            post(NbIO.this, IOEvent.CMD_SET_ICON, this.icon);
        }

        @Override
        protected void setToolTipText(String text) {
            toolTip = text;
            NbIO.this.setTooltipText(toolTip);
        }
    }

    private class IOPositionImpl extends IOPosition {

        @Override
        protected Position currentPosition() {
            OutWriter out = out();
            int size = 0;
            if (out != null) {
                size = out.getLines().getCharCount();
            }
            return new PositionImpl(size);
        }
    }

    private class PositionImpl implements IOPosition.Position {
        private int pos;

        public PositionImpl(int pos) {
            this.pos = pos;
        }

        public void scrollTo() {
            post(NbIO.this, IOEvent.CMD_SCROLL, pos);
        }
    }

    private class IOColorLinesImpl extends IOColorLines {

        @Override
        protected void println(CharSequence text, OutputListener listener, boolean important, Color color) throws IOException {
            OutWriter out = out();
            if (out != null) {
                out.print(text, listener, important, color, null, OutputKind.OUT, true);
            }
        }
    }

    private class IOColorPrintImpl extends IOColorPrint {

        @Override
        protected void print(CharSequence text, OutputListener listener, boolean important, Color color) throws IOException {
            OutWriter out = out();
            if (out != null) {
                out.print(text, listener, important, color, null, OutputKind.OUT, false);
            }
        }
    }

    private class IOSelectImpl extends IOSelect {

	@Override
	protected void select(Set<AdditionalOperation> extraOps) {
	    if (Controller.LOG) Controller.log (this + ": IOSelect.select");
	    NbIO.post (NbIO.this, IOEvent.CMD_FINE_SELECT, extraOps);
	}
    }

    private class IOColorsImpl extends IOColors {
        Color[] clrs = new Color[OutputType.values().length];

        @Override
        protected Color getColor(OutputType type) {
            return clrs[type.ordinal()] != null ? clrs[type.ordinal()] : options.getColorForType(type);
        }

        @Override
        protected void setColor(OutputType type, Color color) {
            clrs[type.ordinal()] = color;
            post(NbIO.this, IOEvent.CMD_DEF_COLORS, type);
        }
    }

    private class IOFoldingImpl extends IOFolding {

        @Override
        protected FoldHandleDefinition startFold(boolean expanded) {
            final OutWriter outWriter = out();
            if (outWriter == null) {
                return new DummyFoldHandleDefinition();
            }
            synchronized (outWriter) {
                if (currentFold != null) {
                    throw new IllegalStateException(
                            "The last fold hasn't been finished yet");  //NOI18N
                }
                return new NbIoFoldHandleDefinition(null,
                        getLastLineNumber(), expanded);
            }
        }

        /**
         * FoldHandleDefinition used when the output is already closed.
         */
        class DummyFoldHandleDefinition extends IOFolding.FoldHandleDefinition {

            @Override
            public void finish() {
            }

            @Override
            public FoldHandleDefinition startFold(boolean expanded) {
                return new DummyFoldHandleDefinition();
            }

            @Override
            public void setExpanded(boolean expanded) {
            }
        }

        class NbIoFoldHandleDefinition extends IOFolding.FoldHandleDefinition {

            private final NbIoFoldHandleDefinition parent;
            private final int start;
            private int end = -1;
            private NbIoFoldHandleDefinition nested = null;

            public NbIoFoldHandleDefinition(NbIoFoldHandleDefinition parent,
                    int start, boolean expanded) {
                this.parent = parent;
                this.start = start;
                setCurrentFoldStart(start);
                setExpanded(expanded, false);
            }

            @Override
            public void finish() {
                synchronized (outOrException()) {
                    if (nested != null) {
                        throw new IllegalStateException(
                                "Nested fold hasn't been finished.");   //NOI18N
                    }
                    if (end != -1) {
                        throw new IllegalStateException(
                                "Fold has been already finished.");     //NOI18N
                    }
                    if (parent == null) {
                        currentFold = null;
                        setCurrentFoldStart(-1);
                    } else {
                        parent.nested = null;
                        setCurrentFoldStart(parent.start);
                    }
                    end = getLastLineNumber();
                }
            }

            @Override
            public FoldHandleDefinition startFold(boolean expanded) {
                synchronized (outOrException()) {
                    if (end != -1) {
                        throw new IllegalStateException(
                                "The fold has been alredy finished.");  //NOI18N
                    }
                    if (nested != null) {
                        throw new IllegalStateException(
                                "An unfinished nested fold exists.");   //NOI18N
                    }
                    NbIoFoldHandleDefinition def = new NbIoFoldHandleDefinition(
                            this, getLastLineNumber(), expanded);
                    this.nested = def;
                    return def;
                }
            }

            @Override
            public final void setExpanded(boolean expanded) {
                setExpanded(expanded, true);
            }

            /**
             * Expand or collapse a fold.
             *
             * @param expanded True to expand the fold, false to collapse it.
             * @param expandParents If true, parent folds will be expanded if
             * needed.
             */
            private void setExpanded(boolean expanded,
                    boolean expandParents) {
                synchronized (outOrException()) {
                    if (expanded) {
                        if (expandParents) {
                            getLines().showFoldAndParentFolds(start);
                        } else {
                            getLines().showFold(start);
                        }
                    } else {
                        getLines().hideFold(start);
                    }
                }
            }

            private void setCurrentFoldStart(int foldStartIndex) {
                getLines().setCurrentFoldStart(foldStartIndex);
            }

            private AbstractLines getLines() {
                return ((AbstractLines) out().getLines());
            }
        }

        /**
         * Access to fold creation via org.netbeans.api.io API.
         *
         * @return The new fold handle definition.
         */
        private NbIoFoldHandleDefinition createFold(
                NbIoFoldHandleDefinition parent, int foldStartIndex,
                boolean expanded) {

            return new NbIoFoldHandleDefinition(parent, foldStartIndex,
                    expanded);

        }
    }

    private int getLastLineNumber() {
        return Math.max(0, out().getLines().getLineCount() - 2);
    }

    /**
     * Set option values. The object itself is not replaced, all registered
     * listeners remains untouched.
     */
    void setOptions(OutputOptions options) {
        this.options.assign(options);
    }

    /**
     * Get Options object.
     */
    OutputOptions getOptions() {
        return this.options;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    int startFold(boolean expanded) {

        synchronized (outOrException()) {
            int foldStartIndex = getLastLineNumber();
            if (currentFold != null && currentFold.start == foldStartIndex) {
                return foldStartIndex;
            } else {
                currentFold = getIoFolding().createFold(currentFold,
                        foldStartIndex, expanded);
                return foldStartIndex;
            }
        }
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    void endFold(int foldStartIndex) {
        synchronized (outOrException()) {
            IOFoldingImpl.NbIoFoldHandleDefinition fold = currentFold;
            while (fold != null && fold.start != foldStartIndex) {
                fold = fold.parent;
            }

            if (fold != null) {
                IOFoldingImpl.NbIoFoldHandleDefinition nested = currentFold;
                while (nested != fold) {
                    nested.finish();
                    nested = nested.parent;
                }
                fold.finish();
                currentFold = fold.parent;
            }
        }
    }
}
