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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Change in a file bookmarks.
 *
 * @author Miloslav Metelka
 */
public class FileBookmarksChange {
    
    private final FileBookmarks fileBookmarks;
    
    private final Map<BookmarkInfo,BookmarkChange> bookmarkChanges;

    public FileBookmarksChange(FileBookmarks fileBookmarks) {
        this.fileBookmarks = fileBookmarks;
        this.bookmarkChanges = new HashMap<BookmarkInfo,BookmarkChange>();
    }

    public FileBookmarks getFileBookmarks() {
        return fileBookmarks;
    }
    
    public BookmarkChange getBookmarkChange(BookmarkInfo bookmark) {
        return bookmarkChanges.get(bookmark);
    }

    public Collection<BookmarkChange> getBookmarkChanges() {
        return bookmarkChanges.values();
    }
    
    void addChange(BookmarkChange change) {
        Object o = bookmarkChanges.put(change.getBookmark(), change);
        if (o != null) {
            throw new IllegalStateException(
                    "BookmarkChange already present: " + change); // NOI18N
        }
    }

}
