/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2;

import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.caret.EditorCaret;
import org.openide.util.Parameters;

/**
 * Editor caret undo related functionality.
 *
 * @author Miloslav Metelka
 */
public final class CaretUndo {
    
    /**
     * Create undoable edit that returns caret to its original state when the edit is undone.
     * <br/>
     * This edit is typically created both at the begining and end of an action that does some document modifications.
     *
     * @param caret non-null caret.
     * @param doc non-null document to which the undoable edit will be added.
     * @return edit allowing to restore caret state upon undo call on the returned edit or null
     *  if caret is not installed in a valid document.
     *  <br>
     *  Future optimizations may return null edit also in case when there was no change in carets
     *  since the preceding call to this method inside the same atomic transaction over the document.
     */
    public static UndoableEdit createCaretUndoEdit(@NonNull Caret caret, @NonNull Document doc) {
        Parameters.notNull("caret", caret);
        Parameters.notNull("doc", doc);
        return CaretUndoEdit.create(caret, doc);
    }

    /**
     * Check whether the given edit is a caret snapshot undoable edit.
     * <br>
     * This is typically used during merging of undoable edits.
     *
     * @param edit non-null edit.
     * @return true if the given edit is caret related undoable edit or false otherwise.
     */
    public static boolean isCaretUndoEdit(@NonNull UndoableEdit edit) {
        Class editClass = edit.getClass();
        return (editClass == CaretUndoEdit.class || editClass == CaretUndoEdit.ComplexEdit.class);
    }
    
}
