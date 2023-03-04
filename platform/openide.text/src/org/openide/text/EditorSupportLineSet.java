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

import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.event.*;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableOpenSupportRedirector;


/** Line set for an EditorSupport.
*
* @author Jaroslav Tulach, David Konecny
*/
final class EditorSupportLineSet extends DocumentLine.Set {
    /** support we are attached to */
    private CloneableEditorSupport support;

    /** Constructor.
    * @param support support to work with
    * @param doc document to use
    */
    public EditorSupportLineSet(CloneableEditorSupport support, StyledDocument doc) {
        super(doc, support);
        this.support = support;
    }

    /** Shares the whm with other line sets based on the same support.
     */
    @Override
    LineVector findLineVector() {
        return support.findLineVector();
    }

    /** Creates a Line for given offset.
    * @param offset the begining of line
    * @return line that should represent the given line
    */
    public Line createLine(int offset) {
        PositionRef ref = new PositionRef(support.getPositionManager(), offset, Position.Bias.Forward);

        return new SupportLine(support.getLookup(), ref, support);
    }

    /** Line for my work.
    */
    private static final class SupportLine extends DocumentLine {
        static final long serialVersionUID = 7282223299866986051L;

        /** Position reference to a place in document
        */
        public SupportLine(org.openide.util.Lookup obj, PositionRef ref, CloneableEditorSupport support) {
            super(obj, ref);
        }

        @Deprecated
        public void show(int kind, int column) {
            CloneableEditorSupport support = getCloneableEditorSupport(pos);

            if ((kind == SHOW_TRY_SHOW) && !support.isDocumentLoaded()) {
                return;
            }

            CloneableEditorSupport.Pane editor;
            
            if (kind == SHOW_REUSE || kind == SHOW_REUSE_NEW) {
                editor = support.openReuse(pos, column, kind);
            } else {
                editor = support.openAt(pos, column);
                if (kind == SHOW_TOFRONT) editor.getComponent().toFront();
            }
            if (kind != SHOW_TRY_SHOW && kind != SHOW_SHOW) {
                editor.getComponent().requestActive();
            }
        }
        
        @Override
        public void show(ShowOpenType openType, ShowVisibilityType visibilityType, int column) {
            CloneableEditorSupport support = getCloneableEditorSupport(pos);

            if ((openType == ShowOpenType.NONE) && !support.isDocumentLoaded()) {
                return;
            }
            
            CloneableEditorSupport.Pane editor = null;
            
            if ((openType == ShowOpenType.REUSE) || (openType == ShowOpenType.REUSE_NEW)) {
                editor = support.openReuse(pos, column, openType);
            } else if ((openType == ShowOpenType.OPEN) || (openType == ShowOpenType.NONE)) {
                //For ShowOpenType.NONE if editor is not yet opened method returns above
                editor = support.openAt(pos, column);
            }
            
            if (editor != null) {
                if (visibilityType == ShowVisibilityType.FRONT) {
                    editor.getComponent().requestVisible();
                    editor.getComponent().toFront();
                } else if (visibilityType == ShowVisibilityType.FOCUS) {
                    editor.getComponent().requestActive();
                }
            }
        }

        @Override
        public Line.Part createPart(int column, int length) {
            DocumentLine.Part part = new DocumentLine.Part(
                    this,
                    new PositionRef(
                            getCloneableEditorSupport(pos).getPositionManager(), pos.getOffset() + column,
                        Position.Bias.Forward
                    ), length
                );
            addLinePart(part);

            return part;
        }

        @Override
        public String getDisplayName() {
            CloneableEditorSupport support = getCloneableEditorSupport(pos);

            return support.messageLine(this);
        }

        @Override
        public String toString() {
            return "SupportLine@" + Integer.toHexString(System.identityHashCode(this)) + " at line: " +
            getLineNumber(); // NOI18N
        }

        private static CloneableEditorSupport getCloneableEditorSupport(final PositionRef pos) {
            return COSHack.getCloneableEditorSupport(pos);
        }
    }

    /** Line set for closed EditorSupport.
    *
    * @author Jaroslav Tulach
    */
    static class Closed extends Line.Set implements ChangeListener {
        
        /** support we are attached to */
        private CloneableEditorSupport support;

        /** line set to delegate to or null if the editor is still closed,
        * is set to non null when the editor opens
        */
        private Line.Set delegate;

        /** Constructor.
        * @param support support to work with
        */
        public Closed(CloneableEditorSupport support) {
            this.support = support;
            support.addChangeListener(org.openide.util.WeakListeners.change(this, support));
        }

        /** Shares the whm with other line sets based on the same support.
         */
        @Override
        LineVector findLineVector() {
            return support.findLineVector();
        }

        /** Returns a set of line objects sorted by their
        * line numbers. This immutable list will contains all lines held by this
        * line set.
        *
        * @return list of element type {@link Line}
        */
        public List<? extends Line> getLines() {
            if (delegate != null) {
                return delegate.getLines();
            }

            // PENDING
            return new ArrayList<Line>();
        }

        /** Find line object in the line set corresponding to original line number.
        * That is, finds the line in the current document which originally had the indicated line number.
        * If there have been modifications of that line, find one as close as possible.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public Line getOriginal(int line) throws IndexOutOfBoundsException {
            if (delegate != null) {
                return delegate.getOriginal(line);
            }

            return getCurrent(line);
        }

        /** Find line object in the line set corresponding to current line number.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public Line getCurrent(int line) throws IndexOutOfBoundsException {
            return findLineVector().findOrCreateLine(line, new SupportLineCreator());
        }

        /** Arrives when the document is opened.
        */
        public synchronized void stateChanged(ChangeEvent ev) {
            if (delegate == null) {
                StyledDocument doc = support.getDocument();

                if (doc != null) {
                    delegate = new EditorSupportLineSet(support, doc);
                }
            } else {
                if (support.getDocument() == null) {
                    delegate = null;
                }
            }
        }
        
        private final class SupportLineCreator implements LineVector.LineCreator {
            
            @Override
            public Line createLine(int lineIndex) {
                PositionRef ref = new PositionRef(support.getPositionManager(), lineIndex, 0, Position.Bias.Forward);
                // obj can be null, sorry...
                org.openide.util.Lookup obj = support.getLookup();
                SupportLine line = new SupportLine(obj, ref, support);
                line.init();
                return line;
            }

        }

    }

// <editor-fold defaultstate="collapsed" desc="COSHack">
    private static final class COSHack {

        private static final Method redirectMethod;

        static {
            Method m = null;
            try {
                m = CloneableOpenSupportRedirector.class.getDeclaredMethod(
                        "findRedirect", // NOI18N
                        new Class[]{CloneableOpenSupport.class});
                m.setAccessible(true);
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } finally {
                redirectMethod = m;
            }
        }

        private static CloneableEditorSupport getCloneableEditorSupport(final PositionRef pos) {
            final CloneableEditorSupport orig = pos.getCloneableEditorSupport();
            if (orig == null || redirectMethod == null) {
                return orig;
            }
            try {
                Object result = redirectMethod.invoke(null, orig);
                if (result instanceof CloneableEditorSupport) {
                    return (CloneableEditorSupport) result;
                }
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (SecurityException ex) {
            } catch (InvocationTargetException ex) {
            }

            return orig;
        }
    }
// </editor-fold>
}
