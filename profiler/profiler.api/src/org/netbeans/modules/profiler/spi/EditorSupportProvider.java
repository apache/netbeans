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
package org.netbeans.modules.profiler.spi;

import org.netbeans.modules.profiler.api.EditorContext;
import org.netbeans.modules.profiler.api.EditorSupport;
import org.netbeans.modules.profiler.api.ProfilerProject;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * An SPI interface for {@linkplain EditorSupport} functionality
 * @author Jaroslav Bachorik
 */
public abstract class EditorSupportProvider {
    public static EditorSupportProvider NULL = new EditorSupportProvider() {
        
        @Override
        public boolean currentlyInJavaEditor() {
            return false;
        }
        
        @Override
        public EditorContext getMostActiveJavaEditorContext() {
            return null;
        }

        @Override
        public FileObject getCurrentFile() {
            return null;
        }

        @Override
        public int getCurrentOffset() {
            return -1;
        }

        @Override
        public boolean isOffsetValid(FileObject file, int offset) {
            return false;
        }

        @Override
        public int getLineForOffset(FileObject file, int offset) {
            return -1;
        }
        
        @Override
        public int getOffsetForLine(FileObject file, int line) {
            return -1;
        }

        @Override
        public ProfilerProject getCurrentProject() {
            return null;
        }

        @Override
        public int[] getSelectionOffsets() {
            return new int[]{-1, -1};
        }
    };
   
    /**
     * Returns true if currently focused IDE component is Java editor.
     * 
     * @return true if currently focused IDE component is Java editor, false otherwise
     */
    public abstract boolean currentlyInJavaEditor();
    /**
     * Returns editor context of the most active Java editor.
     * 
     * @return editor context of the most active Java editor or null if not available
     */
    public abstract EditorContext getMostActiveJavaEditorContext();
    /**
     * Returns the FileObject of the most active editor document
     * @return A FileObject or null
     */
    public abstract FileObject getCurrentFile();
    /**
     * Returns the caret position within the active editor document
     * @return The caret offset or -1
     */
    public abstract int getCurrentOffset();
    /**
     * Validates an offset within a particular file
     * @param file The file to check
     * @param offset The offset within the file
     * @return Returns TRUE if the given offset is valid
     */
    public abstract boolean isOffsetValid(FileObject file, int offset);
    /**
     * Calculates the line number for a given offset
     * @return Returns the line number within the active editor document or -1
     */
    public abstract int getLineForOffset(FileObject file, int offset);
    /**
     * Calculates the offset for a given line number
     * @return Returns the offset for the provided file and line number or -1
     */
    public abstract int getOffsetForLine(FileObject file, int line);
    /**
     * Returns the project the currently activated document belongs to
     * @return The most active project or null
     */
    public abstract Lookup.Provider getCurrentProject();
    /**
     * Returns the tuple of start/end selection offset in the currently activated editor
     * @return Tuple [startOffset, endOffset] or [-1, -1] if there is no selection
     */
    public abstract int[] getSelectionOffsets();
}
