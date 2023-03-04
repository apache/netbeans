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
package org.netbeans.modules.profiler.api;

import org.netbeans.modules.profiler.spi.EditorSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Decouples the profiler implementation from the underlying source code
 * editor. Provides support for finding out eg. the currently edited file,
 * the offset within that file etc.
 * 
 * @author Jiri Sedlacek
 * @author Jaroslav Bachorik
 */
public final class EditorSupport {
    
    private static EditorSupportProvider getSupport() {
        EditorSupportProvider support = Lookup.getDefault().lookup(EditorSupportProvider.class);
        return support != null ? support : EditorSupportProvider.NULL;
    }
    
    /**
     * Returns true if currently focused IDE component is Java editor.
     * 
     * @return true if currently focused IDE component is Java editor, false otherwise
     */
    public static boolean currentlyInJavaEditor() {
        return getSupport().currentlyInJavaEditor();
    }
    
    /**
     * Returns editor context of the most active Java editor.
     * 
     * @return editor context of the most active Java editor or null if not available
     */
    public static EditorContext getMostActiveJavaEditorContext() {
        return getSupport().getMostActiveJavaEditorContext();
    }

    /**
     * Returns the FileObject of the most active editor document
     * @return A FileObject or null
     */
    public static FileObject getCurrentFile() {
        return getSupport().getCurrentFile();
    }
    
    /**
     * Returns the caret position within the active editor document
     * converted into line number
     * @return The line number or -1
     */
    public static int getCurrentLine() {
        return getLineForOffset(getCurrentFile(), getCurrentOffset());
    }
    
    /**
     * Returns the caret position within the active editor document
     * @return The caret offset or -1
     */
    public static int getCurrentOffset() {
        return getSupport().getCurrentOffset();
    }

    /**
     * Validates the current offset
     * @return Returns TRUE if the current offset is valid within the bounds of the current file
     */
    public static boolean isCurrentOffsetValid() {
        return isOffsetValid(getCurrentFile(), getCurrentOffset());
    }
    
    /**
     * Validates an offset within a particular file
     * @param file The file to check
     * @param offset The offset within the file
     * @return Returns TRUE if the given offset is valid
     */
    public static boolean isOffsetValid(FileObject file, int offset) {
        return getSupport().isOffsetValid(file, offset);
    }
    
    /**
     * Calculates the line number for a given offset
     * @return Returns the line number within the active editor document or -1
     */
    public static int getLineForOffset(FileObject file, int offset) {
        return getSupport().getLineForOffset(file, offset);
    }
    
    /**
     * Calculates the offset for a given line number
     * @return Returns the offset for the provided file and line number or -1
     */
    public static int getOffsetForLine(FileObject file, int line) {
        return getSupport().getOffsetForLine(file, line);
    }
    
    /**
     * Returns the tuple of start/end selection offset in the currently activated editor
     * @return Tuple [startOffset, endOffset] or [-1, -1] if there is no selection
     */
    public static int[] getSelectionOffsets() {
        return getSupport().getSelectionOffsets();
    }

    /**
     * Returns the project the currently activated document belongs to
     * @return The most active project or null
     */
    public static Lookup.Provider getCurrentProject() {
        return getSupport().getCurrentProject();
    }
}
