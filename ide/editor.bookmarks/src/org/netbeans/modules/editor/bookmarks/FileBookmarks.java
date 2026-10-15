/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.bookmarks;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Bookmarks for a file represented by URL.
 *
 * @author Miloslav Metelka
 */
public final class FileBookmarks {
    
    private final ProjectBookmarks projectBookmarks;

    private URI relativeURI;
    
    private List<BookmarkInfo> bookmarks; // Sorted by line number
    
    private FileObject fileObject;
    
    FileBookmarks(ProjectBookmarks projectBookmarks, URI relativeURI) {
        this.projectBookmarks = projectBookmarks;
        this.relativeURI = relativeURI;
        this.bookmarks = new ArrayList<BookmarkInfo>();
    }

    public ProjectBookmarks getProjectBookmarks() {
        return projectBookmarks;
    }

    public URI getRelativeURI() {
        return relativeURI;
    }

    /**
     * Get file object for relative URI of this file bookmarks.
     * 
     * @return valid file object or null if e.g. file for URI does not exist.
     */
    public FileObject getFileObject() {
        if (fileObject == null) {
            URI fileURI;
            URI projectURI = projectBookmarks.getProjectURI();
            if (projectURI != null) {
                fileURI = projectURI.resolve(relativeURI);
            } else {
                fileURI = relativeURI;
            }
            try {
                fileObject = URLMapper.findFileObject(fileURI.toURL());
            } catch (MalformedURLException ex) {
                // Leave null
            }
        }
        return fileObject;
    }
    
    public boolean containsAnyBookmarks() {
        return (!bookmarks.isEmpty());
    }

    public List<BookmarkInfo> getBookmarks() {
        return bookmarks;
    }
    
    void add(BookmarkInfo bookmark) {
        bookmarks.add(bookmark);
        bookmark.setFileBookmarks(this);
        bookmarks.sort(BookmarkInfo.CURRENT_LINE_COMPARATOR);
    }

    boolean remove(BookmarkInfo bookmark) {
        return bookmarks.remove(bookmark);
    }

    StringBuilder appendInfo(StringBuilder sb, int indent) {
        sb.append("uri=").append(relativeURI); // NOI18N
        sb.append(", bookmarkCount=").append(bookmarks.size()).append('\n'); // NOI18N
        for (BookmarkInfo bookmark : bookmarks) {
            ArrayUtilities.appendSpaces(sb, indent + 4);
            sb.append(bookmark).append('\n');
        }
        return sb;
    }

    @Override
    public String toString() {
        return appendInfo(new StringBuilder(100), 0).toString();
    }

}
