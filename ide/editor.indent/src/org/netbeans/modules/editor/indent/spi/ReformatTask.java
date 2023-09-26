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

package org.netbeans.modules.editor.indent.spi;

import javax.swing.text.BadLocationException;

/**
 * Reformat task performs actual reformatting within offset bounds of the given context.
 *
 * Since org.netbeans.modules.editor.indent/2 1.12 classes implementing this
 * interface can implement also Lookup.Provider and provide a lookup which will be
 * available to formatters via {@link Context#getLookup()}.
 *
 * @author Miloslav Metelka
 */

public interface ReformatTask {

    /**
     * Perform reformatting of the {@link Context#document()}
     * between {@link Context#startOffset()} and {@link Context#endOffset()}.
     * <br>
     * This method may be called several times repetitively for different areas
     * of a reformatted area.
     * <br>
     * It is called from AWT thread and it should process synchronously. It is used
     * after a newline is inserted after the user presses Enter
     * or when a current line must be reindented e.g. when Tab is pressed in emacs mode.
     * <br>
     * The method should use information from the context and modify
     * indentation at the given offset in the document.
     * 
     * @throws BadLocationException in case the formatter attempted to insert/remove
     *  at an invalid offset or e.g. into a guarded section.
     */
    void reformat() throws BadLocationException;

    /**
     * Get an extra locking or null if no extra locking is necessary.
     */
    ExtraLock reformatLock();
    
    /**
     * Reformat task factory produces reformat tasks for the given context.
     * <br>
     * It should be registered in MimeLookup via xml layer in "/Editors/&lt;mime-type&gt;"
     * folder.
     */
    public interface Factory {

        /**
         * Create reformatting task.
         *
         * @param context non-null indentation context.
         * @return reformatting task or null if the factory cannot handle the given context.
         */
        ReformatTask createTask(Context context);

    }

}
