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

import java.net.URI;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Event describing changes in bookmark manager.
 *
 * @author Miloslav Metelka
 */
public final class BookmarkManagerEvent extends EventObject {
    
    private final Map<URI,ProjectBookmarksChange> projectBookmarksChanges;
    
    private final boolean structureChange;
    
    private Map<BookmarkInfo,BookmarkChange> bookmarkChanges;
    
    BookmarkManagerEvent(BookmarkManager boookmarkManager,
            Map<URI,ProjectBookmarksChange> projectBookmarksChanges, boolean structureChange)
    {
        super(boookmarkManager);
        assert (projectBookmarksChanges != null) : "Null bookmarkChanges";
        this.projectBookmarksChanges = projectBookmarksChanges;
        this.structureChange = structureChange;
    }

    public Collection<ProjectBookmarksChange> getProjectBookmarksChanges() {
        return projectBookmarksChanges.values();
    }
    
    public ProjectBookmarksChange getProjectBookmarksChange(URI projectURI) {
        return projectBookmarksChanges.get(projectURI);
    }
    
    public boolean isStructureChange() {
        return structureChange;
    }
    
    public synchronized BookmarkChange getChange(BookmarkInfo bookmark) {
        if (bookmarkChanges == null) {
            bookmarkChanges = new HashMap<BookmarkInfo, BookmarkChange>();
            for (ProjectBookmarksChange prjChange : projectBookmarksChanges.values()) {
                for (FileBookmarksChange fileChange : prjChange.getFileBookmarksChanges()) {
                    for (BookmarkChange change : fileChange.getBookmarkChanges()) {
                        Object o = bookmarkChanges.put(change.getBookmark(), change);
                        if (o != null) {
                            throw new IllegalStateException(
                                    "Bookmark contained in multiple changes: " + change.getBookmark()); // NOI18N
                        }
                    }
                }
            }
        }
        return bookmarkChanges.get(bookmark);
    }
    
}
