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
package org.netbeans.spi.editor.caret;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.caret.CaretMoveContext;

/**
 * Handle possible moving of individual carets to new positions or change their selections.
 *
 * @author Miloslav Metelka
 * @since 2.6
 */
public interface CaretMoveHandler {
    
    /**
     * Possibly move one or more carets to new position or change their selections
     * by using methods in the given context.
     * <br>
     * The method will be called with a document lock acquired.
     * <br>
     * The method is allowed to make document mutations in case the caller
     * of {@link org.netbeans.api.editor.caret.EditorCaret#moveCarets(CaretMoveHandler) }
     * acquired document write-lock.
     * <br>
     * To prevent deadlocks the method should not acquire any additional locks.
     * <br>
     * The method is not allowed to call methods of EditorCaret that mutate its state
     * and do nested calls to <code>EditorCaret.moveCarets()</code>.
     *
     * @param context non-null context containing the manipulation methods.
     */
    void moveCarets(@NonNull CaretMoveContext context);
    
}
