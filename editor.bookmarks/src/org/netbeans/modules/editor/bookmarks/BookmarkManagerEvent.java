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
