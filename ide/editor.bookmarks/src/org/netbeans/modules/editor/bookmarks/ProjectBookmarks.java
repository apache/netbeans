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
import java.util.WeakHashMap;

/**
 * Bookmarks for a project consist of bookmarks for all URLs (where the bookmarks exist)
 * within the project.
 * 
 * @author Miloslav Metelka
 */
public final class ProjectBookmarks {
    
    private final URI projectURI;

    private volatile int lastBookmarkId;

    private final Map<URI,FileBookmarks> relativeURI2FileBookmarks;
    
    /**
     * Whether bookmarks for this project were loaded.
     * If not yet and the project bookmarks were modified then
     * the bookmarks must be loaded first before saving
     * (otherwise the original not-yet loaded bookmarks would be lost).
     */
    private boolean loaded;

    /**
     * Loading task was scheduled already. Once marked it remains true forever.
     */
    private boolean loadingScheduled;
    
    /**
     * Whether bookmarks were modified by an explicit change (loading is not a change).
     */
    private boolean modified;
    
    private final Map<Object,Boolean> activeClients = new WeakHashMap<Object, Boolean>();
    
    /**
     * Cached project's display name (required for proper nodes sorting).
     */
    private String projectDisplayName;
    
    public ProjectBookmarks(URI projectURI) {
        this.projectURI = projectURI;
        relativeURI2FileBookmarks = new HashMap<URI, FileBookmarks>();
        if (projectURI == null) { // Mark loaded (in future remove this if persistent storage would exist)
            markLoadScheduled();
            markLoaded();
        }
    }
    
    public boolean isLoaded() {
        return loaded;
    }
    
    void markLoaded() {
        loaded = true;
    }
    
    public boolean isLoadingScheduled() {
        return loadingScheduled;
    }
    
    void markLoadScheduled() {
        loadingScheduled = true;
    }
    
    public boolean isModified() {
        return modified;
    }
    
    void setModified(boolean modified) {
        this.modified = modified;
    }

    public URI getProjectURI() {
        return projectURI;
    }
    
    public int getLastBookmarkId() {
        return lastBookmarkId;
    }
    
    void setLastBookmarkId(int lastBookmarkId) {
        this.lastBookmarkId = lastBookmarkId;
        // Shift IDs of any currently contained bookmarks up by lastBookmarkId
        for (FileBookmarks fileBookmarks : relativeURI2FileBookmarks.values()) {
            for (BookmarkInfo bookmark : fileBookmarks.getBookmarks()) {
                bookmark.shiftId(lastBookmarkId);
            }
        }
    }

    public int generateBookmarkId() {
        return ++lastBookmarkId;
    }
    
    public void ensureBookmarkIdIsSkipped(int bookmarkId) {
        lastBookmarkId = Math.max(lastBookmarkId, bookmarkId);
    }

    public FileBookmarks getFileBookmarks(URI relativeURI) {
        return relativeURI2FileBookmarks.get(relativeURI);
    }
    
    void add(FileBookmarks fileBookmarks) {
        relativeURI2FileBookmarks.put(fileBookmarks.getRelativeURI(), fileBookmarks);
    }
    
    void remove(URI relativeURI) {
        relativeURI2FileBookmarks.remove(relativeURI);
    }
    
    public Collection<FileBookmarks> getFileBookmarks() {
        return relativeURI2FileBookmarks.values();
    }
    
    public boolean containsAnyBookmarks() {
        for (FileBookmarks fileBookmarks : relativeURI2FileBookmarks.values()) {
            if (fileBookmarks.containsAnyBookmarks()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clients notify itself into this method so that the bookmarks are aware
     * of them and create a weak reference to them. Once all the clients get released
     * the projects bookmarks may be released as well.
     *
     * @param activeClient
     */
    public void activeClientNotify(Object activeClient) {
        activeClients.put(activeClient, Boolean.TRUE);
    }

    public boolean hasActiveClients() {
        return (activeClients.size() > 0);
    }

    public String getProjectDisplayName() {
        return projectDisplayName;
    }

    void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }
    
    @Override
    public String toString() {
        return "project=" + projectURI + ", lastBId=" + lastBookmarkId + // NOI18N
                ", loaded=" + loaded + // NOI18N
                ", loadingScheduled=" + loadingScheduled + // NOI18N
                ", modified=" + modified; // NOI18N
    }

}
