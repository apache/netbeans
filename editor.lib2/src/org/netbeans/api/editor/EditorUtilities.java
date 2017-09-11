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

package org.netbeans.api.editor;

import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.CustomUndoDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.editor.lib2.CaretUndo;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;


/**
 * Various utility methods related to editor.
 *
 * @author Miloslav Metelka
 * @since 1.13
 */

public final class EditorUtilities {
    
    /**
     * Client property of editor component which determines
     * whether caret is currently in overwrite mode (Boolean.TRUE) or insert mode (Boolean.FALSE or null).
     * <br>
     * It's modified by appropriate editor actions.
     * @since 2.6
     */
    public static final String CARET_OVERWRITE_MODE_PROPERTY = "caret-overwrite-mode";

    private EditorUtilities() {
        // No instances
    }

    /**
     * Find an action with the given name in the editor kit.
     *
     * @param editorKit non-null editor kit in which search is performed.
     * @param actionName non-null action name to search for.
     * @return action instance with the given name or null if action not found.
     */
    public static Action getAction(EditorKit editorKit, String actionName) {
        return EditorActionUtilities.getAction(editorKit, actionName);
    }

    /**
     * Add an undoable edit describing current state of caret(s) during document's atomic section.
     * <br>
     * This method is typically called at the beginning of the atomic section over the document
     * so that a subsequent undo would restore original caret offsets that were not yet modified
     * by the actual changes performed during the atomic section.
     * <br>
     * The method may also be called at the end of the atomic section
     * in case the atomic section performed explicit caret movements.
     * <br>
     * The created undoable edit will be added to document's compound undoable edit created for the atomic section.
     * That edit will be fired by the document to an undo manager's listener upon completion of the atomic section.
     * Therefore the document should adhere to {@link CustomUndoDocument} otherwise the method would do nothing.
     *
     * @param doc document to which the created undoable edit will be added.
     *   Null may be passed then the method has no effect.
     * @param caret non-null caret which state should be stored
     * @see CustomUndoDocument
     * @see AtomicLockDocument
     * @throws IllegalStateException if this method is called outside of an atomic section.
     * @since 2.11
     */
    public static void addCaretUndoableEdit(Document doc, Caret caret) {
        CustomUndoDocument customUndoDocument = LineDocumentUtils.as(doc, CustomUndoDocument.class);
        if (customUndoDocument != null) {
            UndoableEdit caretUndoEdit = CaretUndo.createCaretUndoEdit(caret, doc);
            if (caretUndoEdit != null) {
                customUndoDocument.addUndoableEdit(caretUndoEdit);
            } // Might be null if caret is not installed in a text component and its document
        }
    }
    

//    /**
//     * Reset caret's magic position.
//     * @param component target text component.
//     */
//    public static void resetCaretMagicPosition(JTextComponent component) {
//        Caret caret;
//        if (component != null && (caret = component.getCaret()) != null) {
//            caret.setMagicCaretPosition(null);
//        }
//    }
//    
//    /**
//     * Reset a possible undo merging so any upcoming edits will be undone separately.
//     * @param component target text component.
//     */
//    public static void resetUndoMerge(JTextComponent component) {
//        Document doc;
//        if (component != null && (doc = component.getDocument()) != null) {
//            EditorDocumentUtils.resetUndoMerge(doc);
//        }
//    }
//    
//    /**
//     * Reset word match so that a possible next press of Ctrl+K/L starts from scratch.
//     *
//     * @param component target text component.
//     */
//    public static void resetWordMatch(JTextComponent component) {
//        if (component != null) {
//            resetWordMatch(component.getDocument());
//        }
//    }
//    
//    public static void resetWordMatch(@NonNull Document doc) {
//        WordMatch.get(doc).reset();
//    }

}
