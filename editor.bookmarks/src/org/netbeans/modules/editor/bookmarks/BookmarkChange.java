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
