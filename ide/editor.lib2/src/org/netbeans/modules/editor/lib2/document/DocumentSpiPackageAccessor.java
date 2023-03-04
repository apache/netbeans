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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.util.Exceptions;

/**
 * Package accessor for o.n.spi.editor.document package.
 *
 * @author Miloslav Metelka
 */
public abstract class DocumentSpiPackageAccessor {

    private static DocumentSpiPackageAccessor INSTANCE;

    public static DocumentSpiPackageAccessor get() {
        if (INSTANCE == null) {
            // Cause api accessor impl to get initialized
            try {
                Class.forName(OnSaveTask.Context.class.getName(), true, DocumentSpiPackageAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
            assert (INSTANCE != null) : "Registration failed"; // NOI18N
        }
        return INSTANCE;
    }

    public static void register(DocumentSpiPackageAccessor accessor) {
        INSTANCE = accessor;
    }

    public abstract OnSaveTask.Context createContext(Document doc);

    public abstract void setUndoEdit(OnSaveTask.Context context, UndoableEdit undoEdit);
        
    public abstract void setTaskStarted(OnSaveTask.Context context, boolean taskStarted);

}
