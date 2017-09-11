/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

/**
 * Performer of various document services implemented currently
 * by org.netbeans.editor.BaseDocument in editor.lib module.
 *
 * @author Miloslav Metelka
 */
public final class EditorDocumentHandler {
    
    private EditorDocumentHandler() {
        // no instances
    }

    private static Class<? extends Document> editorDocClass;
    
    private static EditorDocumentServices editorDocServices;
    
    private static EditorCharacterServices charServices = new DefaultEditorCharacterServices(); // Until other impls exist
    
    public static void setEditorDocumentServices(Class<? extends Document> docClass, EditorDocumentServices docServices) {
        // Currently expect just a single implementation: BaseDocument
        if (editorDocClass != null) {
            throw new IllegalStateException("Only single registration expected. Already registered: " + editorDocClass);
        }
        EditorDocumentHandler.editorDocClass = docClass;
        EditorDocumentHandler.editorDocServices = docServices;
    }
    
    public static void runExclusive(Document doc, Runnable r) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            editorDocServices.runExclusive(doc, r);
        } else {
            synchronized (doc) {
                r.run();
            }
        }
    }
    
    public static void resetUndoMerge(Document doc) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            editorDocServices.resetUndoMerge(doc);
        }
    }

    public static UndoableEdit startOnSaveTasks(Document doc) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            return editorDocServices.startOnSaveTasks(doc);
        }
        return null;
    }

    public static void endOnSaveTasks(Document doc, boolean success) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            editorDocServices.endOnSaveTasks(doc, success);
        }
    }

    public static int getIdentifierEnd(Document doc, int offset, boolean backward) {
        return charServices.getIdentifierEnd(doc, offset, backward);
    }

}
