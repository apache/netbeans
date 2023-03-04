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
package org.netbeans.modules.editor.lib2;

import org.netbeans.api.editor.caret.EditorCaret;

/**
 * Accessor of EditorCaret's rectangular selection methods.
 *
 * @author Miloslav Metelka
 */
public abstract class RectangularSelectionCaretAccessor {
    
    private static RectangularSelectionCaretAccessor INSTANCE;

    public static RectangularSelectionCaretAccessor get() {
        if (INSTANCE == null) {
            // Cause api accessor impl to get initialized
            try {
                Class.forName(EditorCaret.class.getName(), true, RectangularSelectionCaretAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                // Should never happen
            }
        }
        return INSTANCE;
    }

    public static void register(RectangularSelectionCaretAccessor accessor) {
        INSTANCE = accessor;
    }

    public abstract void setRectangularSelectionToDotAndMark(EditorCaret editorCaret);

    public abstract void updateRectangularUpDownSelection(EditorCaret editorCaret);

    public abstract void extendRectangularSelection(EditorCaret editorCaret, boolean toRight, boolean ctrl);

}
