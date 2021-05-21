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

package org.netbeans.modules.editor.lib;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.InvalidMarkException;
import org.netbeans.editor.Mark;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.lib.impl.MarkVector;
import org.netbeans.modules.editor.lib.impl.MultiMark;


/**
 * Accessor for the package-private functionality in org.netbeans.editor package.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class EditorPackageAccessor {

    private static EditorPackageAccessor ACCESSOR = null;

    public static synchronized void register(EditorPackageAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!"; //NOI18N
        ACCESSOR = accessor;
    }

    public static synchronized EditorPackageAccessor get() {
        // Trying to wake up BaseDocument ...
        try {
            Class<?> clazz = Class.forName(BaseDocument.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        assert ACCESSOR != null : "There is no package accessor available!"; //NOI18N
        return ACCESSOR;
    }

    protected EditorPackageAccessor() {
    }

    public abstract UndoableEdit BaseDocument_markAtomicEditsNonSignificant(BaseDocument doc);
    public abstract void BaseDocument_clearAtomicEdits(BaseDocument doc);
    public abstract MarkVector BaseDocument_getMarksStorage(BaseDocument doc);
    public abstract Mark BaseDocument_getMark(BaseDocument doc, MultiMark multiMark);
    public abstract void Mark_insert(Mark mark, BaseDocument doc, int pos) throws InvalidMarkException, BadLocationException;
    public abstract void ActionFactory_reformat(Reformat formatter, Document doc, int startPos, int endPos, AtomicBoolean canceled) throws BadLocationException;

    public abstract Object BaseDocument_newServices(BaseDocument doc);
    
    public abstract int MarkBlockChain_adjustPos(MarkBlockChain chain, int pos, boolean thisBlock);
}
