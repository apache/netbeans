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
package org.netbeans.api.editor.caret;

import org.netbeans.api.annotations.common.NonNull;

/**
 * Notification event about changes in editor caret.
 *
 * @author Miloslav Metelka
 * @since 2.6
 */
public final class EditorCaretEvent extends java.util.EventObject {
    
    private final int affectedStartOffset;
    
    private final int affectedEndOffset;
    
    private final MoveCaretsOrigin origin;
    
    EditorCaretEvent(EditorCaret source, int affectedStartOffset, int affectedEndOffset, MoveCaretsOrigin origin) {
        super(source);
        this.affectedStartOffset = affectedStartOffset;
        this.affectedEndOffset = affectedEndOffset;
        this.origin = origin;
    }
    
    /**
     * Get caret instance to which this event relates.
     *
     * @return caret instance.
     */
    public @NonNull EditorCaret getCaret() {
        return (EditorCaret) getSource();
    }

    /**
     * Get start of the region that was affected by caret change.
     * <br>
     * This offset region will be repainted automatically by the editor infrastructure.
     *
     * @return &gt;= 0 offset.
     */
    public int getAffectedStartOffset() {
        return affectedStartOffset;
    }
    
    /**
     * Get end of the region that was affected by caret change.
     * <br>
     * This offset region will be repainted automatically by the editor infrastructure.
     *
     * @return &gt;= 0 offset.
     */
    public int getAffectedEndOffset() {
        return affectedEndOffset;
    }
}
