/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.document;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.openide.util.NbBundle;

/**
 * Undoable edit of EditorDocumentContent.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */
public abstract class ContentEdit implements UndoableEdit {
    
    // Same meaning like AbstractUndoableEdit.alive; used in statusBits
    private static final int ALIVE = 1;
    
    // Same meaning like AbstractUndoableEdit.hasBeenDone; used in statusBits
    private static final int HAS_BEEN_DONE = 2;
    
    private static final int ALIVE_AND_DONE = ALIVE | HAS_BEEN_DONE;


    final EditorDocumentContent content; // 8 + 4 = 12 bytes
    
    final int offset; // 12 + 4 = 16 bytes

    /**
     * Text that was inserted or which will be removed. For inserts it's
     * the same string passed to Document.insertString(). For removals it needs
     * to be created from content's text in order to undo the removal.
     * It could also be just char[] to save some memory but when wishing
     * to adhere to standard Document interface all the Document.insertString()
     * need to have string anyway so the char[] would in fact have to be re-created.
     * Also the String carries potential for being flyweight e.g. in case
     * when reformatter inserts indent spaces these are mostly flyweight strings.
     * So in the end having String here could save more memory than having char[].
     * A similar technique could be used when building removal texts
     * e.g. when removing single-char latin chars the string could be interned
     * or when removed text are just spaces those could be interned too.
     */
    final String text; // 16 + 4 = 20 bytes
    
    MarkVector.MarkUpdate[] markUpdates; // 20 + 4 = 24 bytes

    MarkVector.MarkUpdate[] bbMarkUpdates; // 24 + 4 = 28 bytes

    // Composition of ALIVE_BIT and HAS_BEEN_DONE_BIT
    private int statusBits; // 28 + 4 = 32 bytes

    protected ContentEdit(EditorDocumentContent content, int offset, String text) {
        this.content = content;
        this.offset = offset;
        this.text = text;
        statusBits = ALIVE_AND_DONE;
    }

    public final String getText() {
        return text;
    }
    
    public final int length() {
        return text.length();
    }

    @Override
    public void undo() throws CannotUndoException {
	if (!canUndo()) {
	    throw new CannotUndoException();
	}
	statusBits &= ~HAS_BEEN_DONE; // hasBeenDone = false;
    }

    @Override
    public void redo() throws CannotRedoException {
	if (!canRedo()) {
	    throw new CannotRedoException();
	}
	statusBits |= HAS_BEEN_DONE;
    }

    @Override
   public void die() {
	statusBits &= ~ALIVE;
    }

    @Override
    public boolean canUndo() {
	return (statusBits & ALIVE_AND_DONE) == ALIVE_AND_DONE; // alive and hasBeenDone
    }

    @Override
    public boolean canRedo() {
	return (statusBits & ALIVE_AND_DONE) == ALIVE; // alive && !hasBeenDone
    }
	
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
	return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
	return false;
    }

    @Override
    public boolean isSignificant() {
	return true;
    }
    
    @Override
    public String getUndoPresentationName() {
	String name = getPresentationName();
	if (!"".equals(name)) {
            name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.undoText.param", name); // NOI18N
	} else {
            name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.undoText"); // NOI18N
	}
	return name;
    }

    @Override
    public String getRedoPresentationName() {
	String name = getPresentationName();
	if (!"".equals(name)) {
            name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.redoText.param", name); // NOI18N
	} else {
            name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.redoText"); // NOI18N
	}

	return name;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder(200);
        sb.append(getPresentationName()). // NOI18N
                append(": o=").append(offset).append(",len=").append(length()). // NOI18N
                append(",hBDone=").append(((statusBits & HAS_BEEN_DONE) != 0) ? "T" : "F"). // NOI18N
                append(",alive=").append(((statusBits & ALIVE) != 0) ? "T" : "F"). // NOI18N
                append(",IHC=").append(System.identityHashCode(this)); // NOI18N
        if (markUpdates != null) {
            sb.append("\n  markUpdates:\n"); // NOI18N
            MarkVector.markUpdatesToString(sb, markUpdates, markUpdates.length);
        } else {
            sb.append(",markUpdates:NONE"); // NOI18N
        }
        if (bbMarkUpdates != null) {
            sb.append("\n  BBmarkUpdates:\n"); // NOI18N
            MarkVector.markUpdatesToString(sb, bbMarkUpdates, bbMarkUpdates.length);
        } else {
            sb.append(",BBmarkUpdates:NONE"); // NOI18N
        }
        return sb.toString();
    }

    static final class InsertEdit extends ContentEdit {
        
        InsertEdit(EditorDocumentContent content, int offset, String text) {
            super(content, offset, text);
        }

        @Override
        public String getPresentationName() {
            return "Insert:\"" + CharSequenceUtilities.debugText(getText()) + "\"";
        }
        
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            content.removeEdit(this, "InsertEditUndo-");
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            content.insertEdit(this, "InsertEditRedo-"); // NOI18N
        }

    }
    
    static final class RemoveEdit extends ContentEdit {
        
        protected RemoveEdit(EditorDocumentContent content, int offset, String text) {
            super(content, offset, text);
        }
        
        @Override
        public String getPresentationName() {
            return "Remove:\"" + CharSequenceUtilities.debugText(getText()) + "\"";
        }
        
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            content.insertEdit(this, "RemoveEditUndo-");
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            content.removeEdit(this, "RemoveEditRedo-");
        }

    }

}
