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

import org.openide.util.RequestProcessor;

import java.io.*;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;


/** Reference to one position in a document.
* This position is held as an integer offset, or as a {@link Position} object.
* There is also support for serialization of positions.
*
* @author Petr Hamernik
*/
public final class PositionRef extends Object implements Serializable, Position {
    static final long serialVersionUID = -4931337398907426948L;

    // -J-Dorg.openide.text.PositionRef.level=FINE
    private static final Logger LOG = Logger.getLogger(PositionRef.class.getName());

    /** Which type of position is currently holded - int X Position */
    private transient Manager.Kind kind;

    /** Manager for this position */
    private Manager manager;

    /** insert after? */
    private boolean insertAfter;

    /** Creates new <code>PositionRef</code> using the given manager at the specified
    * position offset.
    * @param manager manager for the position
    * @param offset - position in the document
    * @param bias the bias for the position
    */
    PositionRef(Manager manager, int offset, Position.Bias bias) {
        this(manager, new Manager.OffsetKind(offset, manager), bias);
    }

    /** Creates new <code>PositionRef</code> using the given manager at the specified
    * line and column.
    * @param manager manager for the position
    * @param line line number
    * @param column column number
    * @param bias the bias for the position
    */
    PositionRef(Manager manager, int line, int column, Position.Bias bias) {
        this(manager, new Manager.LineKind(line, column, manager), bias);
    }

    /** Constructor for everything.
    * @param manager manager that we are refering to
    * @param kind kind of position we hold
    * @param bias bias for the position
    */
    private PositionRef(Manager manager, Manager.Kind kind, Position.Bias bias) {
        this.manager = manager;
        insertAfter = (bias == Position.Bias.Backward);
        init(kind, "new");
    }

    /** Initialize variables after construction and after deserialization. */
    private void init(Manager.Kind initialKind, String originMsg) {
        this.kind = initialKind;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(originMsg + " PositionRef@" + System.identityHashCode(this) + "(manager=" + // NOI18N
                    System.identityHashCode(this.manager) + ", kind=" + this.kind + // NOI18N
                    ", backwardBias=" + this.insertAfter + ")\n"); // NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINE, originMsg + "PositionRef", new Throwable());  // NOI18N
            }
        }
        // Possibly update the kind to PositionKind if the document is loaded
        manager.addPosition(this);
    }
    
    void setKind(Manager.Kind kind) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("PositionRef@" + System.identityHashCode(this) + ": Setting kind from " + this.kind + // NOI18N
                    " to kind=" + kind + '\n'); // NOI18N
        }
        this.kind = kind;
    }

    /** Writes the manager and the offset (int). */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(insertAfter);
        out.writeObject(manager);
        kind.write(out);
    }

    /** Reads the manager and the offset (int). */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        insertAfter = in.readBoolean();
        manager = (Manager) in.readObject();
        init(manager.readKind(in), "Deserialized");
    }

    /** @return the appropriate manager for this position ref.
    */
    public CloneableEditorSupport getCloneableEditorSupport() {
        return manager.getCloneableEditorSupport();
    }

    /** @return the bias of the position
    */
    public Position.Bias getPositionBias() {
        return insertAfter ? Position.Bias.Backward : Position.Bias.Forward;
    }

    /** @return the position as swing.text.Position object.
    * @exception IOException when an exception occured during reading the file.
    */
    public Position getPosition() throws IOException {
        // Hold the document reference to prevent document to be closed
        StyledDocument doc = manager.getCloneableEditorSupport().getDocument();
        if (doc == null) {
            doc = manager.getCloneableEditorSupport().openDocument();
        }
        
        Object old = Manager.DOCUMENT.get();
        
        try {
            Manager.DOCUMENT.set(doc);
            synchronized (manager.getLock()) {
                // Fix for IZ#67761 - ClassCastException: org.openide.text.PositionRef$Manager$OffsetKind
                Manager.PositionKind p = kind.toMemory( insertAfter );

                return p.pos;
            }
        } finally {
            Manager.DOCUMENT.set(old);
        }
    }

    /** @return the position as offset index in the file.
    */
    public int getOffset() {
        return kind.getOffset();
    }

    /** Get the line number where this position points to.
    * @return the line number for this position
    * @throws IOException if the document could not be opened to check the line number
    */
    public int getLine() throws IOException {
        return kind.getLine();
    }

    /** Get the column number where this position points to.
    * @return the column number within a line (counting starts from zero)
    * @exception IOException if the document could not be opened to check the column number
    */
    public int getColumn() throws IOException {
        return kind.getColumn();
    }

    public String toString() {
        return "Pos[" + getOffset() + "]" + ", kind=" + kind; // NOI18N
    }

    /** This class is responsible for the holding the Document object
    * and the switching the status of PositionRef (Position X offset)
    * objects which depends to this manager.
    * It has one abstract method for the creating the StyledDocument.
    */
    static final class Manager extends Object implements Runnable, Serializable {
        /** document that this thread should use */
        // XXX never read, does it have some purpose?
        private static ThreadLocal<Object> DOCUMENT = new ThreadLocal<Object>();
        static final long serialVersionUID = -4374030124265110801L;

        /** Head item of data structure replacing linked list here.
         * @see ChainItem */
        private transient ChainItem head;

        /** ReferenceQueue where all <code>ChainedItem</code>'s will be enqueued to. */
        private transient ReferenceQueue<PositionRef> queue;

        /** Counter which counts enqued items and after reaching
         * number 100 schedules sweepTask. */
        private transient int counter;

        /** Task which is run in RequestProcessor thread and provides
         * full pass sweep, i.e. removes items with garbaged referents from
         * data strucure. */
        private transient RequestProcessor.Task sweepTask;
        private static final RequestProcessor RP = new RequestProcessor(PositionRef.class);

        /** support for the editor */
        private transient CloneableEditorSupport support;

        /** the document for this manager or null if the manager is not in memory */
        private transient Reference<StyledDocument> doc;
        
        /**
         * Whether positions were turned into in-memory representation.
         * <br/>
         * Document impls typically allow itself to be GCed while positions
         * may be refernced by hard refs so this flag determines whether positions
         * are converted to in-memory state or not (regardless of document reference value).
         * <br/>
         * The flag may be tested from multiple threads and its subsequent positions conversion
         * is readlocked.
         */
        private transient AtomicBoolean inMemory = new AtomicBoolean();

        /** Creates new manager
        * @param supp support to work with
        */
        public Manager(CloneableEditorSupport supp) {
            support = supp;
            init();
        }

        /** Initialize the variables to the default values. */
        protected void init() {
            queue = new ReferenceQueue<PositionRef>();

            // A stable mark used to simplify operations with the list
            head = new ChainItem(null, queue, null);
        }

        private Object getLock() {
            return support.getLock();
        }

        /** Reads the object and initialize */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object firstObject = in.readObject();

            /* Get rid of backward compatibility

            if (firstObject instanceof DataObject) {
                DataObject obj = (DataObject)firstObject;
                support = (CloneableEditorSupport) obj.getCookie(CloneableEditorSupport.class);
            } else */
            {
                // first object is environment
                CloneableEditorSupport.Env env = (CloneableEditorSupport.Env) firstObject;
                support = (CloneableEditorSupport) env.findCloneableOpenSupport();
            }

            if (support == null) {
                //PENDING - what about now ? does exist better way ?
                throw new IOException();
            }
        }

        final Object readResolve() {
            return support.getPositionManager();
        }

        private void writeObject(ObjectOutputStream out)
        throws IOException {
            // old serialization version            out.writeObject(support.findDataObject());
            out.writeObject(support.cesEnv());
        }

        /** @return the styled document or null if the document is not loaded.
        */
        public CloneableEditorSupport getCloneableEditorSupport() {
            return support;
        }

        /** Converts all positions into document one.
        */
        void documentOpened(Reference<StyledDocument> doc) {
            if (inMemory.compareAndSet(false, true)) {
                this.doc = doc;
                processPositions(true);
            }
        }

        /** Closes the document and switch all positionRefs to the offset (int)
        * holding status (Position objects willbe forgotten.
        */
        void documentClosed() {
            if (inMemory.compareAndSet(true, false)) {
                processPositions(false);
                this.doc = null;
            }
        }

        /** Gets the document this object should work on.
         * @return docoument or null
         */
        private StyledDocument getDoc() {
            Object d = DOCUMENT.get();

            if (d instanceof StyledDocument) {
                return (StyledDocument) d;
            }

            if (d == this) {
                return null;
            }
            Reference<StyledDocument> w = this.doc;
            StyledDocument x = w == null ? null : w.get();
            return x;
        }

        /** Puts/gets positions to/from memory. It also provides full
         * pass sweep of the data structure (inlined in the code).
         * @param toMemory puts positions to memory if <code>true</code>,
         * from memory if <code>false</code> */
        private void processPositions(final boolean toMemory) {
            // clear the queue, we'll do the sweep inline anyway
            while (queue.poll() != null)
                ;

            counter = 0;

            // doc.render() acquires doc's readlock
            new DocumentRenderer(DocumentRenderer.PROCESS_POSITIONS, this, toMemory).render();
        }

        /** Polls queue and increases the <code>counter</code> accordingly.
         * Schedule full sweep task if counter exceedes 100. */
        private void checkQueue() {
            while (queue.poll() != null) {
                counter++;
            }

            if (counter > 100) {
                counter = 0;

                if (sweepTask == null) {
                    sweepTask = RP.post(this);
                } else if (sweepTask.isFinished()) {
                    sweepTask.schedule(0);
                }
            }
        }

        /** Implements <code>Runnable</code> interface.
         * Does full pass sweep in <code>RequestProcessor</code> thread. */
        public synchronized void run() {
            synchronized (getLock()) {
                ChainItem previous = head;
                ChainItem ref = previous.next;

                while (ref != null) {
                    if (ref.get() == null) {
                        // Remove the item from data structure.
                        previous.next = ref.next;
                    } else {
                        previous = ref;
                    }

                    ref = ref.next;
                }
            }
        }

        /** Adds the position to this manager. */
        void addPosition(final PositionRef pos) {
            new DocumentRenderer(DocumentRenderer.ADD_POSITION, this, pos).renderToObject();
            checkQueue();
        }

        //
        // Kinds
        //

        /** Loads the kind from the stream */
        Kind readKind(DataInput is) throws IOException {
            int offset = is.readInt();
            int line = is.readInt();
            int column = is.readInt();

            if (offset == -1) {
                // line and column must be valid
                return new LineKind(line, column, this);
            }

            if ((line == -1) || (column == -1)) {
                // offset kind
                return new OffsetKind(offset, this);
            }

            // out of memory representation
            return new OutKind(offset, line, column, this);
        }

        // #19694. Item of special data structure replacing
        // for our purposed LinkedList due to performance reasons.

        /** One item which chained instanced provides data structure
         * keeping positions for this Manager. */
        private static class ChainItem extends WeakReference<PositionRef> {
            /** Next reference keeping the position. */
            ChainItem next;

            /** Cointructs chanined item.
             * @param position <code>PositionRef</code> as referent for this
             * instance
             * @param queue <code>ReferenceQueue</code> to be used for this instance
             * @param next next chained item */
            public ChainItem(PositionRef position, ReferenceQueue<PositionRef> queue, ChainItem next) {
                super(position, queue);

                this.next = next;
            }
        }
         // End of class ChainItem.

        /** Base kind with all methods */
        private abstract static class Kind extends Object {
            protected final PositionRef.Manager mgr;

            Kind(PositionRef.Manager mgr) {
                this.mgr = mgr;
            }

            /** Offset */
            public abstract int getOffset();

            /** Get the line number */
            public abstract int getLine() throws IOException;

            /** Get the column number */
            public abstract int getColumn() throws IOException;

            /** Writes the kind to stream */
            public abstract void write(DataOutput os) throws IOException;

            /** Converts the kind to representation in memory */
            public PositionKind toMemory(boolean insertAfter) {
                return (PositionKind) new DocumentRenderer(DocumentRenderer.KIND_TO_MEMORY, this, insertAfter).renderToObject();
            }
            
            protected PositionKind toMemoryLockAcquired(boolean insertAfter) {
                // try to find the right position
                Position p;

                int offset = getOffset();

                        // #33165
                // Try to use line:column instead
                // Following code can be commented out to retain old behavior
                if (getClass() == OutKind.class) {
                    try {
                        int line = getLine();
                        int col = getColumn();
                        Element lineRoot = NbDocument.findLineRootElement(mgr.getDoc());

                        if (line < lineRoot.getElementCount()) {
                            Element lineElem = lineRoot.getElement(line);
                            int lineStartOffset = lineElem.getStartOffset();
                            int lineLen = lineElem.getEndOffset() - lineStartOffset;

                            if (lineLen >= 1) { // should always be at least '\n'
                                col = Math.min(col, lineLen - 1);
                                offset = lineStartOffset + col;
                            }
                        }
                    } catch (IOException e) {
                        // use offset in that case
                    }
                }

                try {
                    p = NbDocument.createPosition(
                            mgr.getDoc(), offset,
                            insertAfter ? Position.Bias.Backward : Position.Bias.Forward
                    );
                } catch (BadLocationException e) {
                    p = mgr.getDoc().getEndPosition();
                }

                return new PositionKind(p, mgr);
            }
            
            /**
             * Whether this is already a memory implementation so calling {@link #toMemory(boolean)}
             * would make no difference.
             *
             * @return true for in-memory impl or false otherwise.
             */
            public boolean isMemoryType() {
                return false;
            }

            /** Converts the kind to representation out from memory */
            protected Kind fromMemoryLockAcquired() {
                return this;
            }
        }

        /** Kind for representing position when the document is
        * in memory.
        */
        private static final class PositionKind extends Kind {
            /** position */
            private Position pos;

            /** Constructor */
            public PositionKind(Position pos, PositionRef.Manager mgr) {
                super(mgr);
                this.pos = pos;
            }

            /** Offset */
            public int getOffset() {
                return pos.getOffset();
            }

            /** Get the line number */
            public int getLine() {
                return new DocumentRenderer(DocumentRenderer.POSITION_KIND_GET_LINE, this).renderToInt();
            }
            
            int getLineLockAcquired() {
                return NbDocument.findLineNumber(mgr.getDoc(), getOffset());
            }

            /** Get the column number */
            public int getColumn() {
                return new DocumentRenderer(DocumentRenderer.POSITION_KIND_GET_COLUMN, this).renderToInt();
            }

            int getColumnLockAcquired() {
                return NbDocument.findLineColumn(mgr.getDoc(), getOffset());
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                DocumentRenderer renderer = new DocumentRenderer(DocumentRenderer.POSITION_KIND_WRITE, this);

                int offset = renderer.renderToIntIOE();
                int line = renderer.getLine();
                int column = renderer.getColumn();

                if ((offset < 0) || (line < 0) || (column < 0)) {
                    throw new IOException(
                        "Illegal PositionKind: " + pos + "[offset=" // NOI18N
                         +offset + ",line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(offset);
                os.writeInt(line);
                os.writeInt(column);
            }

            /** Converts the kind to representation in memory */
            @Override
            public PositionKind toMemory(boolean insertAfter) {
                return this;
            }

            @Override
            public PositionKind toMemoryLockAcquired(boolean insertAfter) {
                return this;
            }

            @Override
            public boolean isMemoryType() {
                return true;
            }
            
            /** Converts the kind to representation out from memory */
            @Override
            protected Kind fromMemoryLockAcquired() {
                return new OutKind(this, mgr);
            }

            @Override
            public String toString() {
                return "PositionKind(offset=" + (pos != null ? pos.getOffset() : -1) + ")"; // NOI18N
            }
            
        }

        /** Kind for representing position when the document is
        * out from memory. There are all infomation about the position,
        * including offset, line and column.
        */
        private static final class OutKind extends Kind {
            private int offset;
            private int line;
            private int column;

            /** Constructs the out kind from the position kind.
            */
            OutKind(PositionKind kind, PositionRef.Manager mgr) {
                super(mgr);
                // This constructor is only called from the fromMemoryLockAcquired() method
                // thus no extra document locking is necessary
                int offset = kind.getOffset();
                int line = kind.getLineLockAcquired();
                int column = kind.getColumnLockAcquired();

                if ((offset < 0) || (line < 0) || (column < 0)) {
                    throw new IndexOutOfBoundsException(
                        "Illegal OutKind[offset=" // NOI18N
                         +offset + ",line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                this.offset = offset;
                this.line = line;
                this.column = column;
            }

            /** Constructs the out kind.
            */
            OutKind(int offset, int line, int column, PositionRef.Manager mgr) {
                super(mgr);
                this.offset = offset;
                this.line = line;
                this.column = column;
            }

            /** Offset */
            public int getOffset() {
                return offset;
            }

            /** Get the line number */
            public int getLine() {
                return line;
            }

            /** Get the column number */
            public int getColumn() {
                return column;
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                if ((offset < 0) || (line < 0) || (column < 0)) {
                    throw new IOException(
                        "Illegal OutKind[offset=" // NOI18N
                         +offset + ",line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(offset);
                os.writeInt(line);
                os.writeInt(column);
            }

            @Override
            public String toString() {
                return "OutKind(offset=" + offset + "[" + (line+1) + ":" + (column+1) + "])"; // NOI18N
            }
            
        }
         // OutKind

        /** Kind for representing position when the document is
        * out from memory. Represents only offset in the document.
        */
        private static final class OffsetKind extends Kind {
            private int offset;

            /** Constructs the out kind from the position kind.
            */
            public OffsetKind(int offset, PositionRef.Manager mgr) {
                super(mgr);

                if (offset < 0) {
                    throw new IndexOutOfBoundsException(
                        "Illegal OffsetKind[offset=" // NOI18N
                         +offset + "] in " + mgr.getDoc() + " used by " // NOI18N
                         +mgr.support + "." // NOI18N
                        
                    );
                }

                this.offset = offset;
            }

            /** Offset */
            public int getOffset() {
                return offset;
            }

            /** Get the line number */
            public int getLine() throws IOException {
                mgr.getCloneableEditorSupport().openDocument(); // make sure document is fully read

                return new DocumentRenderer(DocumentRenderer.OFFSET_KIND_GET_LINE, this, offset).renderToIntIOE();
            }

            /** Get the column number */
            public int getColumn() throws IOException {
                mgr.getCloneableEditorSupport().openDocument(); // make sure document fully read

                return new DocumentRenderer(DocumentRenderer.OFFSET_KIND_GET_COLUMN, this, offset).renderToIntIOE();
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                if (offset < 0) {
                    throw new IOException(
                        "Illegal OffsetKind[offset=" // NOI18N
                         +offset + "] in " + mgr.getDoc() + " used by " // NOI18N
                         +mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(offset);
                os.writeInt(-1);
                os.writeInt(-1);
            }

            @Override
            public String toString() {
                return "OffsetKind(offset=" + offset + ")"; // NOI18N
            }
            
        }

        /** Kind for representing position when the document is
        * out from memory. Represents only line and column in the document.
        */
        private static final class LineKind extends Kind {
            private int line;
            private int column;

            /** Constructor.
            */
            public LineKind(int line, int column, PositionRef.Manager mgr) {
                super(mgr);

                if ((line < 0) || (column < 0)) {
                    throw new IndexOutOfBoundsException(
                        "Illegal LineKind[line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                this.line = line;
                this.column = column;
            }

            /** Offset */
            public int getOffset() {
                try {
                    StyledDocument doc = mgr.getCloneableEditorSupport().getDocument();

                    if (doc == null) {
                        doc = mgr.getCloneableEditorSupport().openDocument();
                    }

                    try {
                        // PositionRefs can still have LineKind even after doc's opening.
                        // Therefore service the case when the line is above doc's end here.
                        int retOffset = new DocumentRenderer(
                                DocumentRenderer.LINE_KIND_GET_OFFSET, this, doc
                            ).renderToInt();
                        return retOffset;
                    } catch (IndexOutOfBoundsException e) {
                        return doc.getEndPosition().getOffset();
                    }

                } catch (IOException e) {
                    // what to do? hopefully unlikelly
                    return 0;
                }
            }

            /** Get the line number */
            public int getLine() throws IOException {
                return line;
            }

            /** Get the column number */
            public int getColumn() throws IOException {
                return column;
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                if ((line < 0) || (column < 0)) {
                    throw new IOException(
                        "Illegal LineKind[line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(-1);
                os.writeInt(line);
                os.writeInt(column);
            }

            @Override
            public PositionKind toMemoryLockAcquired(boolean insertAfter) {
                Position p;
                try {
                    p = NbDocument.createPosition(
                            mgr.getDoc(), NbDocument.findLineOffset(mgr.getDoc(), line) + column,
                            insertAfter ? Position.Bias.Backward : Position.Bias.Forward
                        );
                } catch (BadLocationException e) {
                    p = mgr.getDoc().getEndPosition();
                } catch (IndexOutOfBoundsException e) {
                    p = mgr.getDoc().getEndPosition();
                }

                return new PositionKind(p, mgr);
            }

            @Override
            public String toString() {
                return "LineKind([" + (line+1) + ":" + (column+1) + "])"; // NOI18N
            }
            
        }

        /**
         * Helper class ensuring that critical parts will run under document's read lock
         * by using {@link javax.swing.text.Document#render(Runnable)}.
         */
        private static final class DocumentRenderer implements Runnable {
            private static final int KIND_TO_MEMORY = 0;
            private static final int POSITION_KIND_GET_LINE = KIND_TO_MEMORY + 1;
            private static final int POSITION_KIND_GET_COLUMN = POSITION_KIND_GET_LINE + 1;
            private static final int POSITION_KIND_WRITE = POSITION_KIND_GET_COLUMN + 1;
            private static final int OFFSET_KIND_GET_LINE = POSITION_KIND_WRITE + 1;
            private static final int OFFSET_KIND_GET_COLUMN = OFFSET_KIND_GET_LINE + 1;
            private static final int LINE_KIND_GET_OFFSET = OFFSET_KIND_GET_COLUMN + 1;
            private static final int PROCESS_POSITIONS = LINE_KIND_GET_OFFSET + 1;
            private static final int ADD_POSITION = PROCESS_POSITIONS + 1;
            private final int opCode;
            private final Manager mgr;
            private Kind argKind;
            private boolean argInsertAfter;
            private boolean argToMemory;
            private int argInt;
            private PositionRef argPos;
            private StyledDocument argDoc;
            private Object retObject;
            private int retInt;
            private int retLine;
            private int retColumn;
            private IOException ioException;

            DocumentRenderer(int opCode, Kind argKind) {
                this.opCode = opCode;
                this.argKind = argKind;
                this.mgr = argKind.mgr;
            }

            DocumentRenderer(int opCode, Kind argKind, boolean argInsertAfter) {
                this(opCode, argKind);
                this.argInsertAfter = argInsertAfter;
            }

            DocumentRenderer(int opCode, Kind argKind, int argInt) {
                this(opCode, argKind);
                this.argInt = argInt;
            }

            DocumentRenderer(int opCode, Kind argKind, StyledDocument argDoc) {
                this(opCode, argKind);
                this.argDoc = argDoc;
            }

            DocumentRenderer(int opCode, Manager mgr, boolean toMemory) {
                this.opCode = opCode;
                this.mgr = mgr;
                this.argToMemory = toMemory;
            }

            DocumentRenderer(int opCode, Manager mgr, PositionRef argPos) {
                this.opCode = opCode;
                this.mgr = mgr;
                this.argPos = argPos;
            }

            void render() {
                StyledDocument d = mgr.getDoc();
                Object prev = DOCUMENT.get();

                try {
                    if (d != null) {
                        DOCUMENT.set(d);
                        d.render(this);
                    } else {
                        DOCUMENT.set(mgr);
                        this.run();
                    }
                } finally {
                    DOCUMENT.set(prev);
                }
            }

            Object renderToObjectIOE() throws IOException {
                Object o = renderToObject();

                if (ioException != null) {
                    throw ioException;
                }

                return o;
            }

            Object renderToObject() {
                render();

                return retObject;
            }

            int renderToIntIOE() throws IOException {
                int i = renderToInt();

                if (ioException != null) {
                    throw ioException;
                }

                return i;
            }

            int renderToInt() {
                render();

                return retInt;
            }

            int getLine() {
                return retLine;
            }

            int getColumn() {
                return retColumn;
            }

            public void run() {
                try {
                    switch (opCode) {
                    case KIND_TO_MEMORY: {
                        retObject = argKind.toMemoryLockAcquired(argInsertAfter);
                        break;
                    }

                    case POSITION_KIND_GET_LINE: {
                        retInt = ((PositionKind)argKind).getLineLockAcquired();
                        break;
                    }

                    case POSITION_KIND_GET_COLUMN: {
                        retInt = ((PositionKind)argKind).getColumnLockAcquired();
                        break;
                    }

                    case POSITION_KIND_WRITE: {
                        retInt = argKind.getOffset();
                        retLine = argKind.getLine();
                        retColumn = argKind.getColumn();

                        break;
                    }

                    case OFFSET_KIND_GET_LINE: {
                        retInt = NbDocument.findLineNumber(argKind.mgr.getCloneableEditorSupport().openDocument(), argInt);

                        break;
                    }

                    case OFFSET_KIND_GET_COLUMN: {
                        retInt = NbDocument.findLineColumn(argKind.mgr.getCloneableEditorSupport().openDocument(), argInt);

                        break;
                    }

                    case LINE_KIND_GET_OFFSET: {
                        retInt = NbDocument.findLineOffset(argDoc, argKind.getLine()) + argKind.getColumn();

                        break;
                    }

                    case PROCESS_POSITIONS: {
                        synchronized (mgr.getLock()) {
                            ChainItem previous = mgr.head;
                            ChainItem ref = previous.next;
                            int refCount = 0;
                            int emptyRefCount = 0;
                            while (ref != null) {
                                PositionRef pos = ref.get();
                                refCount++;
                                if (pos == null) {
                                    // Remove the item from data structure.
                                    previous.next = ref.next;
                                    emptyRefCount++;
                                } else {
                                    // Process the PostionRef.
                                    if (argToMemory) {
                                        if (!pos.kind.isMemoryType()) {
                                            pos.setKind(pos.kind.toMemoryLockAcquired(pos.insertAfter));
                                        }
                                    } else { // From memory
                                        if (pos.kind.isMemoryType()) {
                                            pos.setKind(pos.kind.fromMemoryLockAcquired());
                                        }
                                    }

                                    previous = ref;
                                }

                                ref = ref.next;
                            }
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("PositionRef.PROCESS_POSITIONS: toMemory=" + argToMemory + // NOI18N
                                        ", refCount=" + refCount + ", emptyRefCount=" + emptyRefCount); // NOI18N
                            }
                        }

                        break;
                    }

                    case ADD_POSITION: {
                        // [pnejedly] these are testability hooks
                        mgr.support.howToReproduceDeadlock40766(true);

                        synchronized (mgr.getLock()) {
                            mgr.support.howToReproduceDeadlock40766(false);
                            mgr.head.next = new ChainItem(argPos, mgr.queue, mgr.head.next);

                            if (mgr.getDoc() != null) {
                                argPos.setKind(argPos.kind.toMemory(argPos.insertAfter));
                            }
                        }

                        break;
                    }

                    default:
                        throw new IllegalStateException(); // Unknown opcode
                    }
                } catch (IOException e) {
                    ioException = e;
                }
            }
        }
    }
}
