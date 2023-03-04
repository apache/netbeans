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

import javax.swing.text.Position;

/**
 * Position implementation in document.
 * <br/>
 * Each position has its corresponding mark which holds a weak reference to it
 * and there's queue that notifies mark vector once the position can be GCed.
 * <br/>
 * Position object does not reference document directly (see EditorDocumentContent javadoc).
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

final class EditorPosition implements Position {

    /**
     * The mark that serves this position.
     * It's non-final field since the instance must be given to Mark's constructor.
     */
    private Mark mark; // 8-super + 4 = 12 bytes
    
    public EditorPosition() {
    }

    /** Get offset in document for this position */
    @Override
    public int getOffset() {
        return mark.getOffset();
    }
    
    public boolean isBackwardBias() {
        return mark.isBackwardBias();
    }
    
    public Mark getMark() {
        return mark;
    }
    
    void initMark(Mark mark) { // Should only be called once from Mark's constructor
        this.mark = mark;
    }
    
    @Override
    public String toString() {
        return mark.toString(); // NOI18N
    }

    public String toStringDetail() {
        return mark.toStringDetail(); // NOI18N
    }

}
