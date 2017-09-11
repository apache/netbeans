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
    public final static BookmarkInfo BOOKMARKS_WINDOW = new BookmarkInfo(0, "Bookmarks Window", 0, ""); // NOI18N
    
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
