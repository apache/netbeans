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
package org.netbeans.modules.editor.bookmarks;

/**
 * Change of a particular bookmark.
 *
 * @author Miloslav Metelka
 */
public final class BookmarkChange {
    
    private static final int ADDED = 1;
    
    private static final int REMOVED = 2;
    
    private static final int FILE_CHANGED = 4; // File to which bookmark belongs has changed
    
    private static final int NAME_CHANGED = 8;
    
    private static final int LINE_INDEX_CHANGED = 16;
    
    private static final int KEY_CHANGED = 32;
    
    private final BookmarkInfo bookmark;
    
    private int statusBits;
    
    BookmarkChange(BookmarkInfo bookmark) {
        this.bookmark = bookmark;
    }
    
    /**
     * Return affected bookmark.
     *
     * @return non-null bookmark.
     */
    public BookmarkInfo getBookmark() {
        return bookmark;
    }

    public boolean isAdded() {
        return (statusBits & ADDED) != 0;
    }
    
    public boolean isRemoved() {
        return (statusBits & REMOVED) != 0;
    }
    
    public boolean isFileChanged() {
        return (statusBits & FILE_CHANGED) != 0;
    }
    
    public boolean isNameChanged() {
        return (statusBits & NAME_CHANGED) != 0;
    }
    
    public boolean isLineIndexChanged() {
        return (statusBits & LINE_INDEX_CHANGED) != 0;
    }
    
    public boolean isKeyChanged() {
        return (statusBits & KEY_CHANGED) != 0;
    }
    
    public boolean isNameKeyOrLineIndexChanged() {
        return (statusBits & (NAME_CHANGED | LINE_INDEX_CHANGED | KEY_CHANGED)) != 0;
    }

    void markAdded() {
        statusBits |= ADDED;
    }
    
    void markRemoved() {
        statusBits |= REMOVED;
    }
    
    void markFileChanged() {
        statusBits |= FILE_CHANGED;
    }
    
    void markNameChanged() {
        statusBits |= NAME_CHANGED;
    }
    
    void markLineIndexChanged() {
        statusBits |= LINE_INDEX_CHANGED;
    }
    
    void markKeyChanged() {
        statusBits |= KEY_CHANGED;
    }
    
}
