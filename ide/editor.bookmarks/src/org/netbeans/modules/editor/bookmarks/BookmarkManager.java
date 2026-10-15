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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Services to update or save bookmarks to persistent format.
 *
 * @author Miloslav Metelka
 */

public class BookmarkManager {
    
    private static final BookmarkManager INSTANCE = new BookmarkManager();
    
    public static BookmarkManager getLocked() {
        return INSTANCE.lock();
    }

    /**
     * Contains mapping of a project and its corresponding bookmarks (lazily read upon request).
     * <br/>
     * Once the bookmarks exist in the map they will be written to project's private.xml upon project close.
     */
    private final Map<URI,ProjectBookmarks> projectURI2Bookmarks =
            new HashMap<URI,ProjectBookmarks> ();
    
    private List<BookmarkManagerListener> listenerList = new CopyOnWriteArrayList<BookmarkManagerListener>();
    
    private Thread locker;
    
    private int lockDepth;
    
    private Map<URI,ProjectBookmarksChange> transactionChanges;
    
    /**
     * Whether transaction changes change the structure of nodes.
     */
    private boolean structureChange;
    
    private boolean firingChange;
    
    private BookmarkManager() {
        resetTransactionChanges();
    }
    
    synchronized BookmarkManager lock() {
        try {
            Thread currentThread = Thread.currentThread();
            while (locker != null) {
                if (currentThread == locker) {
                    lockDepth++;
                    return this;
                }
                wait();
            }
            locker = currentThread;
            lockDepth = 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted lock attempt.");
        }
        return this;
    }
    
    public synchronized void unlock() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != locker) {
            throw new IllegalStateException("currentThread=" + currentThread + " != locker=" + locker);
        }
        if (--lockDepth <= 0) {
            locker = null;
            lockDepth = 0;
            if (!transactionChanges.isEmpty()) {
                fireChange();
            }
            notifyAll();
        }
    }
    
    private void resetTransactionChanges() {
        transactionChanges = new HashMap<URI, ProjectBookmarksChange>();
        structureChange = false;
    }
    
    public List<ProjectBookmarks> activeProjectBookmarks() {
        return new ArrayList<ProjectBookmarks>(projectURI2Bookmarks.values());
    }
    
    /**
     * Load bookmarks for the given projects (if not loaded yet).
     */
    public void keepOpenProjectsBookmarksLoaded() {
        BookmarksPersistence.get().keepOpenProjectsBookmarksLoaded();
    }
    
    public BookmarkInfo findBookmarkByNameOrKey(String nameOrKey, boolean byKey) {
        for (ProjectBookmarks projectBookmarks : activeProjectBookmarks()) {
            for (FileBookmarks fileBookmarks : projectBookmarks.getFileBookmarks()) {
                for (BookmarkInfo info : fileBookmarks.getBookmarks()) {
                    if (nameOrKey.equals(info.getName())) {
                        return info;
                    }
                }
            }
        }
        return null;
    }
    
    public void addBookmarkManagerListener(BookmarkManagerListener l) {
        listenerList.add(l);
    }
    
    public void removeBookmarkManagerListener(BookmarkManagerListener l) {
        listenerList.remove(l);
    }
    
    void fireChange() {
        BookmarkManagerEvent evt = new BookmarkManagerEvent(this, transactionChanges, structureChange);
        resetTransactionChanges();
        firingChange = true;
        try {
            for (BookmarkManagerListener l : listenerList) {
                l.bookmarksChanged(evt);
            }
        } finally {
            firingChange = false;
        }
    }

    public ProjectBookmarks[] getSortedActiveProjectBookmarks() {
        List<ProjectBookmarks> sortedProjectBookmarks = new ArrayList<ProjectBookmarks>(projectURI2Bookmarks.values());
        for (Iterator<ProjectBookmarks> it = sortedProjectBookmarks.iterator(); it.hasNext();) {
            ProjectBookmarks projectBookmarks = it.next();
            if (projectBookmarks.containsAnyBookmarks()) {
                URI prjURI = projectBookmarks.getProjectURI();
                Project prj = BookmarkUtils.findProject(prjURI);
                String projectDisplayName = (prj != null)
                        ? ProjectUtils.getInformation(prj).getDisplayName()
                        : NbBundle.getMessage(BookmarkManager.class, "LBL_NullProjectDisplayName");
                projectBookmarks.setProjectDisplayName(projectDisplayName);
            } else {
                it.remove();
            }
        }
        sortedProjectBookmarks.sort(new Comparator<ProjectBookmarks>() {
            @Override
            public int compare(ProjectBookmarks pb1, ProjectBookmarks pb2) {
                return pb1.getProjectDisplayName().compareTo(pb2.getProjectDisplayName());
            }
        });
        return sortedProjectBookmarks.toArray(new ProjectBookmarks[0]);
    }
        
    /**
     * Get all file objects that contain bookmarks located in the given project.
     * 
     * @param prj 
     */
    public FileBookmarks[] getSortedFileBookmarks(ProjectBookmarks projectBookmarks) {
        List<FileBookmarks> fbList;
        if (projectBookmarks != null) {
            Collection<FileBookmarks> allFileBookmarks = projectBookmarks.getFileBookmarks();
            fbList = new ArrayList<FileBookmarks>(allFileBookmarks.size());
            for (FileBookmarks fileBookmarks : allFileBookmarks) {
                if (fileBookmarks.containsAnyBookmarks()) {
                    fbList.add(fileBookmarks);
                } // else: could be obsolete URL of a removed file
            }
            fbList.sort(new Comparator<FileBookmarks>() {
                @Override
                public int compare(FileBookmarks fb1, FileBookmarks fb2) {
                    FileObject fo1 = fb1.getFileObject();
                    FileObject fo2 = fb2.getFileObject();
                    if (fo1 == null) {
                        return (fo2 == null) ? 0 : -1;
                    }
                    if (fo2 == null) {
                        return 1;
                    }
                    return fo1.getNameExt().compareTo(fo2.getNameExt());
                }
            });
        } else {
            fbList = Collections.emptyList();
        }
        return fbList.toArray(new FileBookmarks[0]);
    }
        
    public BookmarkInfo[] getSortedBookmarks(FileBookmarks fileBookmarks) {
        List<BookmarkInfo> sortedBookmarks = new ArrayList<BookmarkInfo>(fileBookmarks.getBookmarks());
        sortedBookmarks.sort(new Comparator<BookmarkInfo>() {
            @Override
            public int compare(BookmarkInfo b1, BookmarkInfo b2) {
                return b1.getCurrentLineIndex() - b2.getCurrentLineIndex();
            }
        });
        return sortedBookmarks.toArray(new BookmarkInfo[0]);
    }
        
    /**
     * Loads bookmarks for a given document.
     * 
     * @param document non-null document for which the bookmarks should be loaded.
     */
    public FileBookmarks getFileBookmarks(Document document) {
        ProjectBookmarks projectBookmarks = getProjectBookmarks(document);
        if (projectBookmarks != null) {
            FileObject fo = NbEditorUtilities.getFileObject(document); // fo should be non-null
            URI relativeURI = BookmarkUtils.getRelativeURI(projectBookmarks, fo.toURI());
            return getOrAddFileBookmarks(projectBookmarks, relativeURI);
        }
        return null;
    }

    public ProjectBookmarks getProjectBookmarks(Document document) {
        FileObject fo = NbEditorUtilities.getFileObject(document);
        return getProjectBookmarks(fo);
    }
    
    public ProjectBookmarks getProjectBookmarks(FileObject fo) {
        ProjectBookmarks projectBookmarks = null;
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            projectBookmarks = getProjectBookmarks(project, true, true);
        }
        return projectBookmarks;
    }
    
    public ProjectBookmarks getProjectBookmarks(Project project, boolean load, boolean forceCreation) {
        URI projectURI = (project != null) ? project.getProjectDirectory().toURI() : null;
        return getProjectBookmarks(project, projectURI, load, forceCreation);
    }

    ProjectBookmarks getProjectBookmarks(final Project project, URI projectURI,
            boolean load, boolean forceCreation)
    {
        ProjectBookmarks projectBookmarks;
        projectBookmarks = projectURI2Bookmarks.get(projectURI);
        if (projectBookmarks == null && forceCreation) {
            projectBookmarks = new ProjectBookmarks(projectURI);
            projectURI2Bookmarks.put(projectURI, projectBookmarks);
        }

        if (load && projectBookmarks != null && !projectBookmarks.isLoadingScheduled()) {
            projectBookmarks.markLoadScheduled();
            final ProjectBookmarks finalProjectBookmarks = projectBookmarks;
            BookmarkUtils.postTask(new Runnable() {
                @Override
                public void run() {
                    BookmarksPersistence.get().loadProjectBookmarks(finalProjectBookmarks, project);
                }
            });
        }
        return projectBookmarks;
    }
    
    private ProjectBookmarksChange getProjectBookmarksChange(ProjectBookmarks projectBookmarks) {
        ProjectBookmarksChange change = transactionChanges.get(projectBookmarks.getProjectURI());
        if (change == null) {
            change = new ProjectBookmarksChange(projectBookmarks);
            transactionChanges.put(projectBookmarks.getProjectURI(), change);
        }
        return change;
    }
    
    private FileBookmarksChange getFileBookmarksChange(FileBookmarks fileBookmarks) {
        ProjectBookmarksChange prjChange = getProjectBookmarksChange(fileBookmarks.getProjectBookmarks());
        FileBookmarksChange change = prjChange.getFileBookmarksChange(fileBookmarks.getRelativeURI());
        if (change == null) {
            change = new FileBookmarksChange(fileBookmarks);
            prjChange.addChange(change);
        }
        return change;
    }
    
    private BookmarkChange getBookmarkChange(BookmarkInfo bookmark) {
        FileBookmarksChange fileChange = getFileBookmarksChange(bookmark.getFileBookmarks());
        BookmarkChange change = fileChange.getBookmarkChange(bookmark);
        if (change == null) {
            change = new BookmarkChange(bookmark);
            fileChange.addChange(change);
        }
        return change;
    }
    
    public void releaseProjectBookmarks(ProjectBookmarks projectBookmarks) {
        projectURI2Bookmarks.remove(projectBookmarks.getProjectURI());
        getProjectBookmarksChange(projectBookmarks).markReleased();
        structureChange = true;
    }
    
    /**
     * Add a new bookmark was added to file bookmarks.
     */
    public FileBookmarks getOrAddFileBookmarks(ProjectBookmarks projectBookmarks, URI relativeURI) {
        FileBookmarks fileBookmarks = projectBookmarks.getFileBookmarks(relativeURI);
        if (fileBookmarks == null) {
            checkModDuringFire();
            fileBookmarks = new FileBookmarks(projectBookmarks, relativeURI);
            projectBookmarks.add(fileBookmarks);
            fileBookmarks.getProjectBookmarks().setModified(true);
            structureChange = true;
        }
        return fileBookmarks;
    }
    
    /**
     * Add a new bookmark was added to file bookmarks.
     */
    public void addBookmark(FileBookmarks fileBookmarks, BookmarkInfo bookmark) {
        checkModDuringFire();
        fileBookmarks.add(bookmark);
        BookmarkChange change = getBookmarkChange(bookmark);
        change.markAdded();
        bookmark.getFileBookmarks().getProjectBookmarks().setModified(true);
        structureChange = true;
    }
    
    public void removeBookmarks(List<BookmarkInfo> bookmarks) {
        checkModDuringFire();
        for (BookmarkInfo bookmark : bookmarks) {
            FileBookmarks fileBookmarks = bookmark.getFileBookmarks();
            fileBookmarks.remove(bookmark);
            BookmarkChange change = getBookmarkChange(bookmark);
            change.markRemoved();
            bookmark.getFileBookmarks().getProjectBookmarks().setModified(true);
            structureChange = true;
        }
    }
    
    private void checkModDuringFire() {
        if (firingChange) {
            throw new IllegalStateException("Modification during change firing"); // NOI18N
        }
    }
    
    public void updateLineIndex(BookmarkInfo bookmark, int lineIndex) {
        if (bookmark.getLineIndex() != lineIndex) {
            BookmarkChange change = getBookmarkChange(bookmark);
            bookmark.setLineIndex(lineIndex); // Also calls setCurrentLineIndex()
            bookmark.getFileBookmarks().getProjectBookmarks().setModified(true);
            change.markLineIndexChanged();
        }
    }
    
    public void updateNameOrKey(BookmarkInfo bookmark, boolean nameChanged, boolean keyChanged) {
        BookmarkChange change = getBookmarkChange(bookmark);
        if (nameChanged) {
            change.markNameChanged();
            bookmark.getFileBookmarks().getProjectBookmarks().setModified(true);
        }
        if (keyChanged) {
            change.markKeyChanged();
            bookmark.getFileBookmarks().getProjectBookmarks().setModified(true);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        for (ProjectBookmarks projectBookmarks : activeProjectBookmarks()) {
            sb.append("Project ").append(projectBookmarks.getProjectURI()).append('\n');
            for (FileBookmarks fileBookmarks : projectBookmarks.getFileBookmarks()) {
                sb.append("    ");
                fileBookmarks.appendInfo(sb, 4);
            }
        }
        return sb.toString();
    }

}
