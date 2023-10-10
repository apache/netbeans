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
package org.openide.text;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import java.io.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Represents one line in a text document.
 * The line number may change
* when the text is modified, but the identity of the line is retained. It is designed to allow line-dependent
* modules of the IDE (such as the compiler and debugger) to make use of a line consistently even as the text is modified.
*
* @author Ales Novak, Petr Hamernik, Jan Jancura, Jaroslav Tulach, David Konecny
*/
public abstract class Line extends Annotatable implements Serializable {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 9113186289600795476L;

    static final Logger LOG = Logger.getLogger(Line.class.getName());

    /** Property name of the line number */
    public static final String PROP_LINE_NUMBER = "lineNumber"; // NOI18N

    /** Shows the line only if the editor is open.
     * @see #show(int) <code>show</code>
     * @deprecated Deprecated since 6.21. Use {@link ShowOpenType#NONE}
     * and {@link ShowVisibilityType#NONE} instead.
     */
    @Deprecated
    public static final int SHOW_TRY_SHOW = 0;

    /** Opens the editor if necessary and shows the line.
     * @see #show(int) <code>show</code>
     * @deprecated Deprecated since 6.21. Use {@link ShowOpenType#OPEN}
     * and {@link ShowVisibilityType#NONE} instead.
     */
    @Deprecated
    public static final int SHOW_SHOW = 1;

    /** Opens the editor if necessary, shows the line, and takes the focus.
     * @see #show(int) <code>show</code>
     * @deprecated Deprecated since 6.21. Use {@link ShowOpenType#OPEN}
     * and {@link ShowVisibilityType#FOCUS} instead.
     */
    @Deprecated
    public static final int SHOW_GOTO = 2;

    /** Same as SHOW_GOTO except that the Window Manager attempts to front the
     * editor window (i.e. make it the top most window).
     * @see #show(int) <code>show</code>
     * @see org.openide.windows.TopComponent#toFront()
     * @since 5.8
     * @deprecated Deprecated since 6.21. Use {@link ShowOpenType#OPEN}
     * and {@link ShowVisibilityType#FRONT} instead.
     */
    @Deprecated
    public static final int SHOW_TOFRONT = 3;

    /** Takes the focus in case the editor is already opened and shows the line.
     * Replaces (closes) the last editor opened using SHOW_REUSE in case 
     * the user haven't interacted with it much (e.g. haven't modified it).
     * Opens a new editor in case there is no such reusable editor
     * and marks it for editor reusal. 
     * @see #show(int) <code>show</code>
     * @since org.openide.text 6.14
     * @deprecated Deprecated since 6.21. Use {@link ShowOpenType#REUSE}
     * and {@link ShowVisibilityType#FOCUS} instead.
     */
    @Deprecated
    public static final int SHOW_REUSE = 4;

    /** Focuses or opens given editor, marking it as reusable editor if it
     * was not opened before. Similar to {@link #SHOW_REUSE} but ignores
     * currently reusable editor.
     * @see #show(int) <code>show</code>
     * @since org.openide.text 6.14
     * @deprecated Deprecated since 6.21. Use {@link ShowOpenType#REUSE_NEW}
     * and {@link ShowVisibilityType#FOCUS} instead.
     */
    @Deprecated
    public static final int SHOW_REUSE_NEW = 5;

    /** ShowOpenType and ShowVisibilityType is replacement for constants SHOW_TRY_SHOW, SHOW_SHOW,
     * SHOW_GOTO, SHOW_TOFRONT, SHOW_REUSE, SHOW_REUSE_NEW. It is to provide full control
     * over show method behavior without need to add new constant for missing flag combination.
     *
     * <br><br>Note: Any modification of editor marked for reuse resets reuse flag. There is one global static reference
     * so only one or none editor can be marked for reuse.
     *
     * @see #show(ShowOpenType, ShowVisibilityType) <code>show</code>
     * @see ShowVisibilityType <code>ShowVisibilityType</code>
     * @since org.openide.text 6.21
     *
     */
    public enum ShowOpenType {
    /** shows the line only if the editor is open */
        NONE,
    /** opens editor if necessary (editor was not opened) and shows the line */
        OPEN,
    /** replaces editor marked for reuse (last editor opened using {@link ShowOpenType#REUSE}
     * or {@link ShowOpenType#REUSE_NEW} and opens editor if necessary, if editor is being opened (editor was not opened)
     * marks it for reuse, shows the line */
        REUSE,
    /** ignores editor marked for reuse (resets reference to editor marked for reuse),
     * opens editor if necessary, if editor is being opened (editor was not opened)
     * marks it for reuse, shows the line */
        REUSE_NEW
    };

    /** ShowOpenType and ShowVisibilityType is replacement for constants SHOW_TRY_SHOW, SHOW_SHOW,
     * SHOW_GOTO, SHOW_TOFRONT, SHOW_REUSE, SHOW_REUSE_NEW. It is to provide full control
     * over show method behavior without need to add new constant for missing flag combination.
     *
     * @since org.openide.text 6.21
     * @see #show(ShowOpenType, ShowVisibilityType) <code>show</code>
     * @see ShowOpenType <code>ShowOpenType</code>
     */
    public enum ShowVisibilityType {
    /** no action */
        NONE,
    /** fronts editor component to become visible */
        FRONT,
    /** front editor component to become visible and activates/focuses it. It does
     * the same as {@link ShowVisibilityType#FRONT} plus activates/focuses editor */
        FOCUS
    };

    /** Instance of null implementation of Line.Part */
    private static final Line.Part nullPart = new Line.NullPart();

    /** context of this line */
    private org.openide.util.Lookup dataObject;

    /** Create a new line object based on a given data object.
     * This implementation is abstract, so the specific line number is not used here.
     * Subclasses should somehow specify the position.
     * <P>
     * The context argument shall contain information about the
     * producer of the Line, that can be then extracted by {@link Line#getLookup} call.
     *
     * @param context the context for this line
     */
    public Line(Lookup context) {
        if (context == null) {
            throw new NullPointerException();
        }

        dataObject = context;
    }

    /**
     * Create a new line object based on a given data object.
     * This implementation is abstract, so the specific line number is not used here. Subclasses should somehow specify the position.
     * @param source the object that is producing the Line
     */
    public Line(Object source) {
        this((source instanceof Lookup) ? (Lookup) source : Lookups.singleton(source));

        if (source == null) {
            throw new NullPointerException();
        }
    }

    /** Composes a human presentable name for the line. The default
    * implementation uses the name of data object and the line number
    * to create the display name.
    *
    * @return human presentable name that should identify the line
    */
    public String getDisplayName() {
        return getClass().getName() + ":" + getLineNumber(); // NOI18N
    }

    /** Provides access to the context passed into the line constructor.
     * For example lines produced by <code>DataEditorSupport</code>
     * provide <code>DataObject</code> as the content of the lookup.
     * One can use:
     * <PRE>
     *   dataObjectOrNull = (DataObject)line.getLookup ().lookup (DataObject.class);
     * </PRE>
     * to get the access.
     *
     * @return context associated with the line
     * @since 4.3
     */
    public final org.openide.util.Lookup getLookup() {
        return dataObject;
    }

    /** Get the line number. The last condition in following should
    * always be true:
    * <PRE>{@code 
    *   Line.Set lineSet = <line set>
    *   Line l = <some line from line set lineSet>
    *
    *   l.equals (lineSet.getCurrent (l.getLineNumber ()))
    * }</PRE>
    *
    * @return current line number (may change as text is edited) (starting at 0)
    */
    public abstract int getLineNumber();

    /** Show the line.
    * @param kind one of {@link #SHOW_TRY_SHOW}, {@link #SHOW_SHOW}, or {@link #SHOW_GOTO}
    * @param column the column of this line which should be selected (starting at 0),
    * value -1 does not change previously selected column
    * @deprecated Deprecated since 6.21. Use {@link #show(ShowOpenType, ShowVisibilityType, int)} instead.
    */
    @Deprecated
    public abstract void show(int kind, int column);

    /** Shows the line (at the first column).
    * @param kind one of {@link #SHOW_TRY_SHOW}, {@link #SHOW_SHOW}, {@link #SHOW_GOTO},
    * {@link #SHOW_REUSE} or {@link #SHOW_REUSE_NEW}
    * @see #show(int, int)
    * @deprecated Deprecated since 6.21. Use {@link #show(ShowOpenType, ShowVisibilityType)} instead.
    */
    @Deprecated
    public void show(int kind) {
        show(kind, 0);
    }
    
    /** Show the line.
    * @param openType one of {@link ShowOpenType#NONE}, {@link ShowOpenType#OPEN},
    *   {@link ShowOpenType#REUSE} or {@link ShowOpenType#REUSE_NEW}
    * @param visibilityType one of {@link ShowVisibilityType#NONE},
    *   {@link ShowVisibilityType#FRONT} or {@link ShowVisibilityType#FOCUS}
    * @param column the column of this line which should be selected (starting at 0),
    *   value -1 does not change previously selected column
    * @since org.openide.text 6.21
    */
    public void show(ShowOpenType openType, ShowVisibilityType visibilityType, int column) {
        if (openType == ShowOpenType.NONE) {
            if (visibilityType == ShowVisibilityType.NONE) {
                show(SHOW_TRY_SHOW, column);
            } else {
                LOG.warning("Line.show(ShowOpenType, ShowVisibilityType, int) uses unsupported combination of parameters");
                show(SHOW_TRY_SHOW, column);
            }
        } else if (openType == ShowOpenType.OPEN) {
            if (visibilityType == ShowVisibilityType.NONE) {
                show(SHOW_SHOW, column);
            } else if (visibilityType == ShowVisibilityType.FOCUS) {
                show(SHOW_GOTO, column);
            } else if (visibilityType == ShowVisibilityType.FRONT) {
                show(SHOW_TOFRONT, column);
            }
        } else if (openType == ShowOpenType.REUSE) {
            if (visibilityType == ShowVisibilityType.FOCUS) {
                show(SHOW_REUSE, column);
            } else {
                LOG.warning("Line.show(ShowOpenType, ShowVisibilityType, int) uses unsupported combination of parameters");
                show(SHOW_REUSE, column);
            }
        } else if (openType == ShowOpenType.REUSE_NEW) {
            if (visibilityType == ShowVisibilityType.FOCUS) {
                show(SHOW_REUSE_NEW, column);
            } else {
                LOG.warning("Line.show(ShowOpenType, ShowVisibilityType, int) uses unsupported combination of parameters");
                show(SHOW_REUSE_NEW, column);
            }
        }
    }

    /** Shows the line (at the first column).
    * @param openType one of {@link ShowOpenType#NONE}, {@link ShowOpenType#OPEN},
    *   {@link ShowOpenType#REUSE} or {@link ShowOpenType#REUSE_NEW}
    * @param visibilityType one of {@link ShowVisibilityType#NONE},
    *   {@link ShowVisibilityType#FRONT} or {@link ShowVisibilityType#FOCUS}
    * @see #show(ShowOpenType, ShowVisibilityType, int)
    * @since org.openide.text 6.21
    */
    public void show(ShowOpenType openType, ShowVisibilityType visibilityType) {
        show(openType, visibilityType, 0);
    }

    /** Set or clear a (debugger) breakpoint at this line.
     * @param b <code>true</code> to turn on
     * @deprecated Deprecated since 1.20. Use {@link Annotation#attach} instead.
     */
    @Deprecated
    public abstract void setBreakpoint(boolean b);

    /** Test if there is a breakpoint set at this line.
     * @return <code>true</code> is there is
     * @deprecated Deprecated since 1.20. Use {@link Annotation} instead.
     */
    @Deprecated
    public abstract boolean isBreakpoint();

    /** Mark an error at this line.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#attach} instead.
     */
    @Deprecated
    public abstract void markError();

    /** Unmark error at this line.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#detach} instead.
     */
    @Deprecated
    public abstract void unmarkError();

    /** Mark this line as current.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#attach} instead.
     */
    @Deprecated
    public abstract void markCurrentLine();

    /** Unmark this line as current.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#detach} instead.
     */
    @Deprecated
    public abstract void unmarkCurrentLine();

    /** Method that should allow the debugger to communicate with lines that
    * wants to have a control over the current line of debugger. It allows the
    * line to refuse the current status and force the debugger to continue
    * over this line.
    * <P>
    * The default implementation simply returns true.
    *
    * @param action type of action that is trying to mark this line as current
    *    one of constants (Debugger.ACTION_BREAKPOINT_HIT,
    *    Debugger.ACTION_TRACE_OVER, etc.)
    * @param previousLine previous line (if any) or null
    *
    * @return true if this line accepts the "current" state or false if the
    *    line wants the debugger to proceed with next instruction
    *
    * @deprecated Deprecated since 1.20, as {@link #markCurrentLine} is deprecated by {@link Annotation#attach}.
    */
    @Deprecated
    public boolean canBeMarkedCurrent(int action, Line previousLine) {
        return true;
    }

    /** Create object which represent part of the text on the line. This part
     * of the line can be used for attaching of annotations.
     * @param column starting column of the part of the text (starting at 0)
     * @param length length of the part of the text
     * @return instance of the Line.Part which represent the part of the text
     * @since 1.20
     */
    public Line.Part createPart(int column, int length) {
        return nullPart;
    }

    public String getText() {
        return null;
    }

    /** Representation of the part of the Line's text. The part of the text is defined by
     * the starting column, length of the part and reference to Line. The length of the
     * part never cross the end of the line.
     * @since 1.20
     */
    public abstract static class Part extends Annotatable {
        /** Property name for the line attribute */
        public static final String PROP_LINE = "line"; // NOI18N

        /** Property name for the column attribute */
        public static final String PROP_COLUMN = "column"; // NOI18N

        /** Property name for the length attribute */
        public static final String PROP_LENGTH = "length"; // NOI18N

        /** Start column of annotation
         * @return column at which this part begining (starting at 0)
         */
        public abstract int getColumn();

        /** Length of the annotated text. The length does not cross line end. If the annotated text is
         * split during the editing, the annotation is shorten till the end of the line. Modules can listen on
         * changes of this value
         * @return length of the part
         */
        public abstract int getLength();

        /** Line can change during editting
         * @return reference to the Line to which this part belongs
         */
        public abstract Line getLine();
    }

    /** Implementation of Line.Part which is presenting empty part */
    private static final class NullPart extends Part {
        NullPart() {
        }

        public int getColumn() {
            return 0;
        }

        public int getLength() {
            return 0;
        }

        public Line getLine() {
            return null;
        }

        public String getText() {
            return null;
        }
    }

    /** Object that represents a snapshot of lines at the time it was created.
    * It is used to create a mapping from line
    * numbers to line objects, for example when the file is saved.
    * Such a mapping can then be used by the compiler, e.g., to find
    * the correct {@link Line} object, assuming it has a line number.
    * <P>
    * Mappings of line numbers to line objects will survive modifications
    * of the text, and continue to represent the original lines as close as possible.
    * For example: if a new line is inserted at the 10th line of a document
    * and the compiler module asks for the 25th line (because the compiler reports an error at line 25 in the saved file) via the line set, the 26th line
    * of the current document will be marked as being in error.
    */
    public abstract static class Set extends Object {
        /** date when the object has been created */
        private Date date;

        /**
         * Binary-searchable vector of lines.
         */
        private LineVector lineVector;

        /** Create a new snapshot. Remembers the date when it was created. */
        public Set() {
            date = new Date();
        }

        /** Returns a set of line objects sorted by their
        * line numbers. This immutable list will contains all lines held by this
        * line set.
        *
        * @return list of lines
        */
        public abstract List<? extends Line> getLines();

        /** Get creation time for this line set.
         * @return time
        */
        public final Date getDate() {
            return date;
        }

        /** Find line object in the line set corresponding to original line number.
         * That is, finds the line in the current document which originally had the indicated line number.
         * If there have been modifications of that line, find one as close as possible.
        *
        * @param line number of the line (starting at 0)
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public abstract Line getOriginal(int line) throws IndexOutOfBoundsException;

        /** Find line object representing the line in current document.
        *
        *
        * @param line number of the line in current state of the document (starting at 0)
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public abstract Line getCurrent(int line) throws IndexOutOfBoundsException;

        /** Finds an original line number for given line in this line set.
         * @param line the line to look for
         * @return the number (starting at 0) that best matches the line number of the line or -1
         *    if the line does seem to be produced by this line set
         * @since 4.38
         */
        public int getOriginalLineNumber(Line line) {
            return computeOriginal(this, line);
        }

        /**
         * Lazily creates or finds binary-searchable vector of registered lines.
         */
        LineVector findLineVector() {
            synchronized (date) {
                if (lineVector != null) {
                    return lineVector;
                }

                lineVector = new LineVector();

                return lineVector;
            }
        }

        /** Registers the line to this <code>Line.Set</code>.
         * @param lineIndex line index
         * @param lineCreator <code>Line</code> to register
         * @return registered <code>Line</code>. <em>Note:</em> the retruned
         * <code>Line</code> could be different (identityHashCode not equal)
         * from the one passed in */
        final Line findOrCreateLine(int lineIndex, LineVector.LineCreator lineCreator) {
            // beware of null argument
            if (lineCreator == null) {
                throw new NullPointerException();
            }

            LineVector lineVector = findLineVector();
            return lineVector.findOrCreateLine(lineIndex, lineCreator);
        }

        /** Finds whether a line equal to provided is already registered.
         * @param line the line to register
         * @return the registered line equal to line or null
         */
        final Line findLine(Line line) {
            LineVector lineVector = findLineVector();
            return lineVector.findOrCreateLine(line.getLineNumber(), null);
        }

        /** A method that for a given Line.Set and a line computes the best
         * original line number based on the querying the set. This is called
         * in default implementation of getOriginal (Line) to provide
         * inefficient (but better then most people would write) way to
         * compute the number. It is static so it can be tested from
         * tests working on DocumentLine objects that override the
         * getOriginal (Line) method.
         *
         * @param set the set to search in
         * @param line the line to look for
         * @return closest possible line number for given line
         */
        static int computeOriginal(Line.Set set, Line line) {
            int n = line.getLineNumber();
            Line current = null;

            try {
                current = set.getOriginal(n);

                if (line.equals(current)) {
                    return n;
                }
            } catch (IndexOutOfBoundsException ex) {
                // ok, few lines have been added and this one is now
                // bellow the end of the document
            }

            if (current == null) {
                return binarySearch(set, n, 0, findMaxLine(set));
            }

            if (n < current.getLineNumber()) {
                return binarySearch(set, n, 0, current.getLineNumber());
            } else {
                return binarySearch(set, n, current.getLineNumber(), findMaxLine(set));
            }
        }

        /** Does a search for a given line number in a given Line.Set.
         */
        private static int binarySearch(Line.Set set, int number, int from, int to) {
            while (from < to) {
                int middle = (from + to) / 2;

                Line l = set.getOriginal(middle);
                int ln = l.getLineNumber();

                if (ln == number) {
                    return middle;
                }

                if (ln < number) {
                    // try after the middle
                    from = middle + 1;
                } else {
                    // try before the middle
                    to = middle - 1;
                }
            }

            return from;
        }

        private static int findMaxLine(Line.Set set) {
            int from = 0;
            int to = 32000;

            // probably larger than any existing document
            for (;;) {
                try {
                    set.getOriginal(to);

                    // if the line exists, double the max number, but keep
                    // for reference that it exists
                    from = to;
                    to *= 2;
                } catch (IndexOutOfBoundsException ex) {
                    break;
                }
            }

            while (from < to) {
                int middle = (from + to + 1) / 2;

                try {
                    set.getOriginal(middle);

                    // line exists
                    from = middle;
                } catch (IndexOutOfBoundsException ex) {
                    // line does not exists, we have to search lower
                    to = middle - 1;
                }
            }

            return from;
        }
    }
     // End of class Line.Set.
}
