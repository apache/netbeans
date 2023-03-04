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
import java.util.HashMap;
import java.util.Map;

/**
 * Change of project bookmarks.
 *
 * @author Miloslav Metelka
 */
public final class ProjectBookmarksChange {
    
    private final ProjectBookmarks projectBookmarks;
    
    private final Map<URI,FileBookmarksChange> fileBookmarksChanges;

    private boolean added;
    
    private boolean loaded;
    
    private boolean released;
    
    public ProjectBookmarksChange(ProjectBookmarks projectBookmarks) {
        this.projectBookmarks = projectBookmarks;
        this.fileBookmarksChanges = new HashMap<URI, FileBookmarksChange>();
    }
    
    public ProjectBookmarks getProjectBookmarks() {
        return projectBookmarks;
    }

    public boolean isAdded() {
        return added;
    }


    public boolean isLoaded() {
        return loaded;
    }
    
    public boolean isReleased() {
        return released;
    }

    public FileBookmarksChange getFileBookmarksChange(URI relativeURI) {
        return fileBookmarksChanges.get(relativeURI);
    }

    public Collection<FileBookmarksChange> getFileBookmarksChanges() {
        return fileBookmarksChanges.values();
    }

    void markAdded() {
        this.added = true;
    }

    void markLoaded() {
        this.loaded = true;
    }
    
    void markReleased() {
        this.released = true;
    }
    
    void addChange(FileBookmarksChange change) {
        Object o = fileBookmarksChanges.put(change.getFileBookmarks().getRelativeURI(), change);
        if (o != null) {
            throw new IllegalStateException("Change already present: " + change); // NOI18N
        }
    }

}
