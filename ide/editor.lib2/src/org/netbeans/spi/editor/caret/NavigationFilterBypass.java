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

import javax.swing.text.NavigationFilter;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.caret.MoveCaretsOrigin;

/**
 * Enhanced FilterBypass which understands multicaret. 
 * <p>
 * Implementations of
 * {@link NavigationFilter} may check if the FilterBypass is instanceof this class,
 * and if so, they can access extended information.
 * </p><p>
 * If the caret move operation is initiated by new caret APIs, the FilterBypass passed
 * to NavigationFilters always satisfies this interface.
 * </p>
 * @author sdedic
 * @since 2.10
 */
public abstract class NavigationFilterBypass extends NavigationFilter.FilterBypass {
    /**
     * Returns the currently changing CaretItem.
     * 
     * @return CaretItem the caret instance being changed
     */
    public abstract @NonNull CaretInfo           getCaretItem();
    
    /**
     * Access to the entire EditorCaret abstraction
     * @return the editor caret
     */
    public abstract @NonNull EditorCaret         getEditorCaret();
    
    /**
     * Describes the origin / reason of the movement.
     * @return The origin object provided by the caret movement initiator.
     */
    public abstract @NonNull MoveCaretsOrigin    getOrigin();
}
