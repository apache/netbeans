/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
