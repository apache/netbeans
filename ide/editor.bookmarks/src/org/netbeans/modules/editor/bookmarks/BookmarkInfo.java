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

import java.lang.ref.Reference;
import java.util.Comparator;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;

/**
 * Description of a bookmark that does not have a corresponding document
 * constructed yet (or is a snapshot of a document bookmark).
 *
 * @author Miloslav Metelka
 */
public final class BookmarkInfo {
    
    /**
     * Special entry used in popup switcher to represent jumping to bookmarks view.
     */
    public static final BookmarkInfo BOOKMARKS_WINDOW = new BookmarkInfo(0, "Bookmarks Window", 0, ""); // NOI18N
    
    public static final Comparator<BookmarkInfo> CURRENT_LINE_COMPARATOR = new Comparator<BookmarkInfo>() {

        @Override
        public int compare(BookmarkInfo bookmark1, BookmarkInfo bookmark2) {
            return bookmark1.getCurrentLineIndex() - bookmark2.getCurrentLineIndex();
        }
        
    };
    
    public static BookmarkInfo create(int id, String name, int lineIndex, String key) {
        return new BookmarkInfo(id, name, lineIndex, key);
    }

    private int id;
    
    private String name;

    private int lineIndex;
    
    private int currentLineIndex;

    private String key;

    private FileBookmarks fileBookmarks;
    
    private Reference<Annotation> annotationRef;
    
    private BookmarkInfo(int id, String name, int lineIndex, String key) {
        this.id = id;
        if (name == null) {
            throw new IllegalArgumentException("Null name not allowed"); // NOI18N
        }
        this.name = name;
        setLineIndex(lineIndex); // Also call setCurrentLineIndex()
        if (key == null) {
            throw new IllegalArgumentException("Null key not allowed"); // NOI18N
        }
        this.key = key;
    }
    
    public int getId() {
        return id;
    }

    void shiftId(int byCount) {
        id += byCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return getDescription(false, true, true);
    }

    /**
     * Get line index of this info.
     * <br/>
     * Note that line index information may be obsolete in case a corresponding Bookmark instance exists
     * for this info.
     *
     * @return zero-based line index.
     */
    public int getLineIndex() {
        return lineIndex;
    }
    
    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
        setCurrentLineIndex(lineIndex);
    }

    public int getCurrentLineIndex() {
        return currentLineIndex;
    }

    public void setCurrentLineIndex(int currentLineIndex) {
        this.currentLineIndex = currentLineIndex;
    }
    
    public String getDescription(boolean fullPath, boolean useName, boolean useKey) {
        String fileDescription;
        if (this != BOOKMARKS_WINDOW) {
            FileObject fo = getFileBookmarks().getFileObject();
            if (fo != null) {
                fileDescription = fullPath ? fo.getPath() : fo.getNameExt();
            } else {
                fileDescription = NbBundle.getMessage(BookmarkInfo.class, "LBL_NonExistentFile");
            }
        } else {
            fileDescription = null;
        }
        return getDescription(fileDescription, useName, useKey, false);
    }

    public String getDescription(String fileDescription, boolean useName, boolean useKey, boolean forHtml) {
        StringBuilder description = new StringBuilder(100);
        if (this != BOOKMARKS_WINDOW) {
            description.append(NbBundle.getMessage(BookmarkInfo.class, "CTL_BookmarkFileAndLine",
                        fileDescription, (getCurrentLineIndex() + 1)));
            if (useName && name.length() > 0) {
                description.append(" \"").append(name).append("\""); // NOI18N
            }
            if (useKey && key.length() > 0) {
                description.append(' ').append(forHtml ? "&lt;" : "<"); // NOI18N
                description.append(key).append(forHtml ? "&gt;" : ">"); // NOI18N
            }
        } else {
            description.append(getBookmarksWindowDisplayName());
        }
        return description.toString();
    }

    public void setAnnotationRef(Reference<Annotation> annotationRef) {
        this.annotationRef = annotationRef;
    }

    public Annotation getAnnotation() {
        return (annotationRef != null) ? annotationRef.get() : null;
    }
  
    public String getFullPathDescription() {
        if (this != BOOKMARKS_WINDOW) {
            FileObject fo = getFileBookmarks().getFileObject();
            return (fo != null)
                    ? fo.getPath()
                    : NbBundle.getMessage(BookmarkInfo.class, "LBL_NonExistentFile");
        } else {
            return NbBundle.getMessage(BookmarkInfo.class, "CTL_BookmarksWindowDescription");
        }
    }
    
    private String getBookmarksWindowDisplayName() {
        return NbBundle.getMessage(BookmarkInfo.class, "CTL_BookmarksWindowItem");
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        if (key.length() > 0) {
            key = key.substring(0, 1).toUpperCase();
            if (!key.matches("[0-9A-Z]")) {
                key = "";
            }
        } else {
            key = "";
        }
        this.key = key;
    }

    public FileBookmarks getFileBookmarks() {
        return fileBookmarks;
    }

    public void setFileBookmarks(FileBookmarks fileBookmarks) {
        this.fileBookmarks = fileBookmarks;
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=\"" + name + "\", key='" + key + // NOI18N
                "' at line=" + lineIndex + ", fileBookmarks-IHC=" + System.identityHashCode(fileBookmarks); // NOI18N
    }

}
