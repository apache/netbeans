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
package org.openide.awt;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.UIManager;

import javax.swing.event.*;
import javax.swing.undo.*;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/** Undo and Redo manager for top components and workspace elements.
 * It allows <code>UndoAction</code> and <code>RedoAction</code> to listen to editing changes of active
 * components and to changes in their ability to do undo and redo.
 *
 * <p>
 * <b>Related links:</b>
 * <ul>
 *   <li><a href="@org-openide-actions@/org/openide/actions/UndoAction.html">org.openide.actions.UndoAction</a></li>
 *   <li><a href="@org-openide-actions@/org/openide/actions/RedoAction.html">org.openide.actions.RedoAction</a></li>
 *   <li><a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getUndoRedo()">org.openide.windows.TopComponent.getUndoRedo()</a></li>
 * </ul> 
 *
 * @author Jaroslav Tulach
*/
public interface UndoRedo {
    /** Empty implementation that does not allow
    * any undo or redo actions.
    */
    public static final UndoRedo NONE = new Empty();

    /** Test whether the component currently has edits which may be undone.
    * @return <code>true</code> if undo is allowed
    */
    public boolean canUndo();

    /** Test whether the component currently has undone edits which may be redone.
    * @return <code>true</code> if redo is allowed
    */
    public boolean canRedo();

    /** Undo an edit.
    * @exception CannotUndoException if it fails
    */
    public void undo() throws CannotUndoException;

    /** Redo a previously undone edit.
    * @exception CannotRedoException if it fails
    */
    public void redo() throws CannotRedoException;

    /** Add a change listener.
    * The listener will be notified every time the undo/redo
    * ability of this object changes.
    * @param l the listener to add
    */
    public void addChangeListener(ChangeListener l);

    /** Remove a change listener.
    * @param l the listener to remove
    * @see #addChangeListener
    */
    public void removeChangeListener(ChangeListener l);

    /** Get a human-presentable name describing the
    * undo operation.
    * @return the name
    */
    public String getUndoPresentationName();

    /** Get a human-presentable name describing the
    * redo operation.
    * @return the name
    */
    public String getRedoPresentationName();

    /** Components that provide {@link UndoRedo} shall announce that by
     * implementing this provider interface. Both Edit/Undo and Edit/Redo actions
     * seek this interface inside current selection (e.g. {@link Utilities#actionsGlobalContext()}).
     * To control these actions make sure your implementation of this interface
     * is exposed in instance representing {@link Lookup current context}.
     *
     * @since 7.25
     */
    public static interface Provider {
        /** Getter for {@link UndoRedo} implementation associated with this provider.
         * @return non-null implementation
         */
        public UndoRedo getUndoRedo();
    }

    /**
     * An undo manager which fires a change event each time it consumes a new undoable edit.
     * <br/>
     * Compared to Swing this implementation is more stable.
     * If any contained undo edit throws an exception from its undo/redo methods
     * the implementation will fail gracefully (unlike in Swing it will not change
     * an internal pointer inside edits).
     */
    public static class Manager extends UndoManager implements UndoRedo {

        static final long serialVersionUID = 6721367974521509720L;
        
        // Protected field "edits" is retained

        private int indexOfNextAdd;

        private int limit;

        private boolean inProgress;

        private boolean hasBeenDone;

        private boolean alive;

        private final ChangeSupport cs = new ChangeSupport(this);

        public Manager() {
            hasBeenDone = true;
            alive = true;
            inProgress = true;
            // Already done: edits = new Vector<UndoableEdit>();
            indexOfNextAdd = 0;
            limit = 100;
            edits.ensureCapacity(limit);
        }

        @Override
        public void die() {
            int size = edits.size();
            for (int i = size-1; i >= 0; i--) {
                UndoableEdit e = edits.elementAt(i);
                e.die();
            }
            alive = false;
        }

        @Override
        public boolean isInProgress() {
            return inProgress;
        }
        @Override
        public void end() {
            inProgress = false;
            this.trimEdits(indexOfNextAdd, edits.size()-1);
        }

        @Override
        public void undo() throws CannotUndoException {
            if (inProgress) {
                UndoableEdit edit = editToBeUndone();
                if (edit == null) {
                    throw new CannotUndoException();
                }
                undoTo(edit);
            } else {
                if (!canUndo()) {
                    throw new CannotUndoException();
                }
                int i = edits.size() - 1;
                try {
                    for (; i >= 0; i--) {
                        edits.get(i).undo(); // may throw CannotUndoException
                    }
                    hasBeenDone = false;
                } finally {
                    if (i != -1) { // i-th edit's undo failed => redo the ones above
                        int size = edits.size();
                        while (++i < size) {
                            edits.get(i).redo();
                        }
                    }
                }
            }
            cs.fireChange();
        }

        @Override
        protected void undoTo(UndoableEdit edit) throws CannotUndoException {
            int i = indexOfNextAdd;
            boolean done = false;
            try {
                while (!done) {
                    UndoableEdit next = edits.get(--i);
                    next.undo(); // may throw CannotUndoException
                    done = next == edit;
                }
                indexOfNextAdd = i;
            } finally {
                if (!done) { // i-th edit's undo failed => redo the ones above
                    i++;
                    for (; i < indexOfNextAdd; i++) {
                        edits.get(i).redo();
                    }
                }
            }

            
        }
        
        @Override
        public boolean canUndo() {
            if (inProgress) {
                UndoableEdit edit = editToBeUndone();
                return edit != null && edit.canUndo();
            } else {
                return !isInProgress() && alive && hasBeenDone;
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            if (inProgress) {
                UndoableEdit edit = editToBeRedone();
                if (edit == null) {
                    throw new CannotRedoException();
                }
                redoTo(edit);
            } else {
                if (!canRedo()) {
                    throw new CannotRedoException();
                }
                int i = 0;
                int size = edits.size();
                try {
                    for (; i < size; i++) {
                        edits.get(i).redo(); // may throw CannotRedoException
                    }
                    hasBeenDone = true;
                } finally {
                    if (i != size) { // i-th edit's redo failed => undo the ones below
                        while (--i >= 0) {
                            edits.get(i).undo();
                        }
                    }
                }
            }
            cs.fireChange();
        }

        @Override
        protected void redoTo(UndoableEdit edit) throws CannotRedoException {
            int i = indexOfNextAdd;
            boolean done = false;
            try {
                while (!done) {
                    UndoableEdit next = edits.elementAt(i++);
                    next.redo(); // may throw CannotRedoException
                    done = next == edit;
                }
                indexOfNextAdd = i;
            } finally {
                if (!done) { // (i-1)-th edit's redo failed => undo the ones below
                    i -= 2;
                    for (; i >= indexOfNextAdd; i--) {
                        edits.get(i).undo();
                    }
                }
            }
        }

        @Override
        public boolean canRedo() {
            if (inProgress) {
                UndoableEdit edit = editToBeRedone();
                return edit != null && edit.canRedo();
            } else {
                return !isInProgress() && alive && !hasBeenDone;
            }
        }

        @Override
        public void undoOrRedo() throws CannotRedoException, CannotUndoException {
            if (indexOfNextAdd == edits.size()) {
                undo();
            } else {
                redo();
            }
        }

        @Override
        public boolean canUndoOrRedo() {
            if (indexOfNextAdd == edits.size()) {
                return canUndo();
            } else {
                return canRedo();
            }
        }

        @Override
        public int getLimit() {
            return limit;
        }

        @Override
        public void setLimit(int l) {
            if (!inProgress) throw new RuntimeException("Attempt to call UndoManager.setLimit() after UndoManager.end() has been called");
            limit = l;
            trimForLimit();
        }

        @Override
        protected void trimForLimit() {
            if (limit >= 0) {
                int size = edits.size();
                if (size > limit) {
                    int halfLimit = limit/2;
                    int keepFrom = indexOfNextAdd - 1 - halfLimit;
                    int keepTo   = indexOfNextAdd - 1 + halfLimit;

                    // These are ints we're playing with, so dividing by two
                    // rounds down for odd numbers, so make sure the limit was
                    // honored properly. Note that the keep range is
                    // inclusive.

                    if (keepTo - keepFrom + 1 > limit) {
                        keepFrom++;
                    }

                    // The keep range is centered on indexOfNextAdd,
                    // but odds are good that the actual edits Vector
                    // isn't. Move the keep range to keep it legal.

                    if (keepFrom < 0) {
                        keepTo -= keepFrom;
                        keepFrom = 0;
                    }
                    if (keepTo >= size) {
                        int delta = size - keepTo - 1;
                        keepTo += delta;
                        keepFrom += delta;
                    }

                    trimEdits(keepTo+1, size-1);
                    trimEdits(0, keepFrom-1);
                }
            }
        }

        @Override
        protected void trimEdits(int from, int to) {
            if (from <= to) {
                for (int i = to; from <= i; i--) {
                    UndoableEdit e = edits.elementAt(i);
                    e.die();
                    edits.removeElementAt(i);
                }

                if (indexOfNextAdd > to) {
                    indexOfNextAdd -= to-from+1;
                } else if (indexOfNextAdd >= from) {
                    indexOfNextAdd = from;
                }

            }
        }

        @Override
        public void discardAllEdits() {
            Enumeration cursor = edits.elements();
            while (cursor.hasMoreElements()) {
                UndoableEdit e = (UndoableEdit)cursor.nextElement();
                e.die();
            }
            edits = new Vector<UndoableEdit>();
            indexOfNextAdd = 0;
            cs.fireChange();
        }

        @Override
        protected UndoableEdit lastEdit() {
            int count = edits.size();
            if (count > 0)
                return edits.elementAt(count-1);
            else
                return null;
        }

        @Override
        protected UndoableEdit editToBeUndone() {
            int i = indexOfNextAdd;
            while (i > 0) {
                UndoableEdit edit = edits.elementAt(--i);
                if (edit.isSignificant()) {
                    return edit;
                }
            }

            return null;
        }

        @Override
        protected UndoableEdit editToBeRedone() {
            int count = edits.size();
            int i = indexOfNextAdd;

            while (i < count) {
                UndoableEdit edit = edits.elementAt(i++);
                if (edit.isSignificant()) {
                    return edit;
                }
            }

            return null;
        }

        /** Consume an undoable edit.
        * Delegates to superclass and notifies listeners.
        * @param ue the edit
        */
        @Override
        public void undoableEditHappened(final UndoableEditEvent ue) {
            addEdit(ue.getEdit());
            cs.fireChange();
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            boolean retVal;

            // Trim from the indexOfNextAdd to the end, as we'll
            // never reach these edits once the new one is added.
            trimEdits(indexOfNextAdd, edits.size()-1);

            if (!inProgress) {
                retVal = false;
            } else {
                UndoableEdit last = lastEdit();
                if (last == null) {
                    edits.addElement(anEdit);
                } else if (!last.addEdit(anEdit)) {
                    if (anEdit.replaceEdit(last)) {
                        edits.removeElementAt(edits.size() - 1);
                    }
                    edits.addElement(anEdit);
                }
                retVal = true;
            }

            // Maybe super added this edit, maybe it didn't (perhaps
            // an in progress compound edit took it instead. Or perhaps
            // this UndoManager is no longer in progress). So make sure
            // the indexOfNextAdd is pointed at the right place.
            indexOfNextAdd = edits.size();

            // Enforce the limit
            trimForLimit();

            return retVal;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        @Override
        public boolean isSignificant() {
            Enumeration cursor = edits.elements();
            while (cursor.hasMoreElements()) {
                if (((UndoableEdit)cursor.nextElement()).isSignificant()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getPresentationName() {
            UndoableEdit last = lastEdit();
            if (last != null) {
                return last.getPresentationName();
            } else {
                return "";
            }
        }

        @Override
        public String getUndoPresentationName() {
            // The following code does an original code of: return this.canUndo() ? super.getUndoPresentationName() : "";
            if (canUndo()) {
                // UndoManager.getUndoPresentationName() follows
                if (inProgress) {
                    if (canUndo()) {
                        return editToBeUndone().getUndoPresentationName();
                    } else {
                        return UIManager.getString("AbstractUndoableEdit.undoText");
                    }
                } else {
                    UndoableEdit last = lastEdit();
                    if (last != null) {
                        return last.getUndoPresentationName();
                    } else {
                        String name = getPresentationName();
                        if (!"".equals(name)) {
                            name = UIManager.getString("AbstractUndoableEdit.undoText")
                                    + " " + name;
                        } else {
                            name = UIManager.getString("AbstractUndoableEdit.undoText");
                        }
                        return name;
                    }
                }
            } else {
                return "";
            }
        }

        @Override
        public String getRedoPresentationName() {
            // The following code does an original code of: return this.canRedo() ? super.getRedoPresentationName() : "";
            if (canRedo()) {
                // UndoManager.getRedoPresentationName() follows
                UndoableEdit last = lastEdit();
                if (last != null) {
                    if (inProgress) {
                        if (canRedo()) {
                            return editToBeRedone().getRedoPresentationName();
                        } else {
                            return UIManager.getString("AbstractUndoableEdit.redoText");
                        }
                    } else {
                        return super.getRedoPresentationName();
                    }
                } else {
                    String name = getPresentationName();
                    if (!"".equals(name)) {
                        name = UIManager.getString("AbstractUndoableEdit.redoText")
                                + " " + name;
                    } else {
                        name = UIManager.getString("AbstractUndoableEdit.redoText");
                    }
                    return name;
                }
            } else {
                return "";
            }
        }

        @Override
        public String getUndoOrRedoPresentationName() {
            if (indexOfNextAdd == edits.size()) {
                return getUndoPresentationName();
            } else {
                return getRedoPresentationName();
            }
        }

        @Override
        public String toString() {
            return super.toString()
                + " hasBeenDone: " + hasBeenDone
                + " alive: " + alive
                + " inProgress: " + inProgress
                + " edits: " + edits
                + " limit: " + limit 
                + " indexOfNextAdd: " + indexOfNextAdd;
        }
    
        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        /* Removes the listener
        */
        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }

    // XXX cannot be made private in an interface, consider removing later

    /** Empty implementation that does not support any undoable edits.
    * @deprecated Use {@link UndoRedo#NONE} rather than instantiating this.
    */
    @Deprecated
    public static final class Empty extends Object implements UndoRedo {
        @Override
        public boolean canUndo() {
            return false;
        }

        @Override
        public boolean canRedo() {
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            throw new CannotUndoException();
        }

        @Override
        public void redo() throws CannotRedoException {
            throw new CannotRedoException();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public String getUndoPresentationName() {
            return ""; // NOI18N
        }

        @Override
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }
    }
}
