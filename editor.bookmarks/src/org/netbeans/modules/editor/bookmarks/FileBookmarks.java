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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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
        Collections.sort(bookmarks, BookmarkInfo.CURRENT_LINE_COMPARATOR);
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
