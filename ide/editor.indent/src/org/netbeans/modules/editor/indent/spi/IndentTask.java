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

package org.netbeans.modules.editor.indent.spi;

import javax.swing.text.BadLocationException;

/**
 * Indent task performs indentation on a single or multiple lines.
 * <br/>
 * Typically it is used to fix indentation after newline was inserted
 * or to fix indentation for a selected block of code.
 *
 * Since org.netbeans.modules.editor.indent/2 1.12 classes implementing this
 * interface can implement also Lookup.Provider and provide a lookup which will be
 * available to formatters via {@link Context#getLookup()}.
 *
 * @author Miloslav Metelka
 */

public interface IndentTask {

    /**
     * Perform reindentation of the line(s) of {@link Context#document()}
     * between {@link Context#startOffset()} and {@link Context#endOffset()}.
     * <br/>
     * It is called from AWT thread and it should process synchronously. It is used
     * after a newline is inserted after the user presses Enter
     * or when a current line must be reindented e.g. when Tab is pressed in emacs mode.
     * <br/>
     * The method should use information from the context and modify
     * indentation at the given offset in the document.
     * 
     * @throws BadLocationException in case the indent task attempted to insert/remove
     *  at an invalid offset or e.g. into a guarded section.
     */
    void reindent() throws BadLocationException;
    
    /**
     * Get an extra locking or null if no extra locking is necessary.
     */
    ExtraLock indentLock();

    /**
     * Indent task factory produces indent tasks for the given context.
     * <br/>
     * It should be registered in MimeLookup via xml layer in "/Editors/&lt;mime-type&gt;"
     * folder.
     */
    public interface Factory {

        /**
         * Create indenting task.
         *
         * @param context non-null indentation context.
         * @return indenting task or null if the factory cannot handle the given context.
         */
        IndentTask createTask(Context context);

    }

}
