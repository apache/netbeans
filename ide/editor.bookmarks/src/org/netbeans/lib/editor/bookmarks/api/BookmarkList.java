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

package org.netbeans.lib.editor.bookmarks.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.bookmarks.BookmarkChange;
import org.netbeans.modules.editor.bookmarks.BookmarkHistory;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkManager;
import org.netbeans.modules.editor.bookmarks.BookmarkManagerEvent;
import org.netbeans.modules.editor.bookmarks.BookmarkManagerListener;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.netbeans.modules.editor.bookmarks.FileBookmarks;
import org.netbeans.modules.editor.bookmarks.FileBookmarksChange;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarks;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarksChange;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;


/**
 * Management of bookmarks for a single document (file).
 * <br/>
 * Bookmarks are sorted by increasing offsets.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class BookmarkList {
    
    public static BookmarkList get (Document doc) {
        BookmarkList bookmarkList = (BookmarkList) doc.getProperty (BookmarkList.class);
        if (bookmarkList == null) {
            bookmarkList = new BookmarkList (doc);
            doc.putProperty (BookmarkList.class, bookmarkList);
        }
        return bookmarkList;
    }

    private static final String PROP_BOOKMARKS = "bookmarks"; //NOI18N

    private static Set<Observable> observedObservables = new WeakSet<Observable> ();

    /**
     * Document for which the bookmark list was created.
     */
    private Document document;
    
    /**
     * List of bookmark instances sorted by offset.
     */
    private List<Bookmark> bookmarks;
    
    private Map<BookmarkInfo, Bookmark> info2bookmark;
    
    private FileBookmarks fileBookmarks;
    
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport (this);

    private final BookmarkManagerListener bmListener = new BookmarkManagerListener() {

        @Override
        public void bookmarksChanged(BookmarkManagerEvent evt) {
            if (fileBookmarks != null) {
                ProjectBookmarksChange prjChange = evt.getProjectBookmarksChange(
                        fileBookmarks.getProjectBookmarks().getProjectURI());
                if (prjChange != null) {
                    FileBookmarksChange fileChange = 
                            prjChange.getFileBookmarksChange(fileBookmarks.getRelativeURI());
                    if (fileChange != null) {
                        Collection<BookmarkChange> bookmarkChanges = fileChange.getBookmarkChanges();
                        if (bookmarkChanges != null) {
                            for (BookmarkChange change : bookmarkChanges) {
                                if (change.isAdded()) {
                                    addBookmarkForInfo(change.getBookmark(), -1);
                                }
                                if (change.isRemoved()) {
                                    removeBookmarkImpl(change.getBookmark());
                                }
                            }
                        }
                    }
                }
            }
        }

    };

    private boolean pendingFireChange;
    
    private BookmarkList(final Document document) {
        if (document == null) {
            throw new NullPointerException ("Document cannot be null"); // NOI18N
        }
        this.document = document;
        this.bookmarks = new ArrayList<Bookmark> ();
        this.info2bookmark = new HashMap<BookmarkInfo, Bookmark>();

        BookmarkUtils.postTask(new Runnable() {
            @Override
            public void run() {
                BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
                try {
                    fileBookmarks = lockedBookmarkManager.getFileBookmarks(document);
                    if (fileBookmarks != null) {
                        ProjectBookmarks projectBookmarks = fileBookmarks.getProjectBookmarks();
                        projectBookmarks.activeClientNotify(this);
                        for (BookmarkInfo bookmarkInfo : fileBookmarks.getBookmarks()) {
                            try {
                                addBookmarkForInfo(bookmarkInfo, -1);
                            } catch (IndexOutOfBoundsException ex) {
                                // line does not exists now (some external changes)
                            }
                        }
                    }
                    // Passing lockedBookmarkManager as "source" parameter is unclean
                    lockedBookmarkManager.addBookmarkManagerListener(WeakListeners.create(
                            BookmarkManagerListener.class, bmListener, lockedBookmarkManager));
                } finally {
                    lockedBookmarkManager.unlock();
                }
            }
        });

        DataObject dataObject = NbEditorUtilities.getDataObject (document);
        if (dataObject != null) {
            Observable observable = dataObject.getCookie (Observable.class);
            if (observable != null) {
                if (!observedObservables.contains (observable)) {
                    observable.addPropertyChangeListener (documentModifiedListener);
                    observedObservables.add (observable);
                }
            }
        }
    }

    /**
     * Get document on which this bookmark list operates.
     *
     * @return non-null document.
     */
    public Document getDocument () {
        return document;
    }
    
    /**
     * Returns list of all bookmarks sorted by increasing offsets.
     * 
     * @return list of all bookmarks
     */
    public synchronized  List<Bookmark> getBookmarks () {
        return Collections.<Bookmark> unmodifiableList (bookmarks);
    }
    
    /**
     * Get the first bookmark
     * that has the offset greater than the specified offset.
     *
     * @param offset &gt;=-1 offset for searching of the next bookmark.
     *  The offset -1 searches for the first bookmark.
     * @param wrapSearch if true then continue searching from the begining of document
     *  in case a bookmark was not found.
     * @return valid bookmark or null if there is no bookmark satisfying the condition.
     */
    public Bookmark getNextBookmark (int offset, boolean wrapSearch) {
        offset++;
        checkOffsetNonNegative (offset);
        Bookmark bookmark = getNextBookmark (offset);
        return bookmark != null || !wrapSearch ?
            bookmark :
            getNextBookmark (-1, false);
    }
    
    /**
     * Get the first bookmark in backward direction
     * that has the offset lower than the specified offset.
     *
     * @param offset &gt;=0 offset for searching of the previous bookmark.
     *  The offset <code>Integer.MAX_VALUE</code> searches for the last bookmark.
     * @param wrapSearch if true then continue searching from the end of document
     *  in case a bookmark was not found.
     * @return valid bookmark or null if there is no bookmark satisfying the condition.
     */
    public Bookmark getPreviousBookmark (int offset, boolean wrapSearch) {
        checkOffsetNonNegative (offset);
        List<Bookmark> bookmarks = new ArrayList<Bookmark> (getBookmarks ());
        Bookmark bookmark; // result
        if (!bookmarks.isEmpty ()) {
            offset--; // search from previous offset
            bookmark = getNextBookmark (offset);
            if (bookmark == null || bookmark.getOffset () != offset) {
                // go below
                int index = bookmark == null ?
                    bookmarks.size () :
                    bookmarks.indexOf (bookmark);
                index--;
                if (index >= 0) {
                    bookmark = bookmarks.get (index);
                } else { // prior first bookmark
                    if (wrapSearch) {
                        bookmark = getPreviousBookmark(Integer.MAX_VALUE, false);
                    } else { // no previous bookmark
                        bookmark = null;
                    }
                }
            } // else -> bookmark right at offset is assigned
        } else { // no bookmarks available
             bookmark = null;
        }
        return bookmark;
    }

    /**
     * First bookmark that has the line index greater or equal
     * to the requested offset.
     * <br>
     * Return <code>getBookmarkCount()</code> in case there is no such mark.
     * <br>
     * The algorithm uses binary search.
     *
     * @param offset offset by which the bookmarks will be searched.
     * @return &gt;=0 and &lt;={@link #getBookmarkCount()} index of the first bookmark
     *  with the offset greater or equal to the requested one.
     */
    private Bookmark getNextBookmark (int offset) {
        // Find next bookmark by binary search
        int low = 0;
        List<Bookmark> bookmarks = new ArrayList<Bookmark> (getBookmarks ());
        if (bookmarks.isEmpty ()) return null;
        int high = bookmarks.size () - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midOffset = bookmarks.get (mid).getOffset();
            
            if (midOffset < offset) {
                low = mid + 1;
            } else if (midOffset > offset) {
                high = mid - 1;
            } else { // bookmark right at the offset
                // Goto first bookmark of possible ones at the same line
                mid--;
                while (mid >= 0) {
                    if (bookmarks.get (mid).getOffset() != offset) {
                        break;
                    }
                    mid--;
                }
                mid++;
                return bookmarks.get (mid);
            }
        }
        if (low < bookmarks.size ())
            return bookmarks.get (low);
        return null;
    }
    
    /**
     * Create an unnamed bookmark if it did not exist before at the line containing
     * the given offset.
     * <br>
     * Drop an existing bookmark if it was already present for the line
     * containing the given offset.
     *
     * @param offset offset on a line in the document for which the presence of bookmark
     *  should be checked. The bookmarks are checked in a line-wise way.
     * @return bookmark that was either created or removed by the operation.
     *  Calling {@link Bookmark#isValid()} determines whether the returned
     *  bookmark was added or removed by the operation.
     *  <br>
     *  <code>null</code> is returned if the offset is above the end of document.
     */
    public Bookmark toggleLineBookmark (int offset) {
        try {
            // Ensure that the bookmarks are loaded prior completing the task
            final Position pos = document.createPosition(offset);

            BookmarkUtils.postTask(new Runnable() {

                private boolean docLocked;

                @Override
                public void run() {
                    if (docLocked) {
                        int lineIndex = BookmarkUtils.offset2LineIndex(document, pos.getOffset());
                        Bookmark bookmark = null;

                        Element lineElem = document.getDefaultRootElement().getElement(lineIndex);
                        int lineStartOffset = lineElem.getStartOffset();
                        bookmark = getNextBookmark(lineStartOffset);
                        if (bookmark != null
                                && bookmark.getOffset() < lineElem.getEndOffset() // inside line
                                ) { // remove the existing bookmark
                            removeBookmark(bookmark);
                        } else { // add bookmark
                            bookmark = addBookmark(lineStartOffset);
                        }
                    } else {
                        docLocked = true;
                        document.render(this);
                    }
                }

            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        // Return null since the task performs asynchronously
        return null;
    }
    
    void updateBookmarkLineIndexes() {
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            for (Bookmark b : bookmarks) {
                BookmarkInfo info = b.info();
                lockedBookmarkManager.updateLineIndex(info, b.getLineNumber());
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
    }
    
    /**
     * Remove bookmark at the given index among the bookmarks.
     *
     * @param index index at which the bookmark should be removed.
     * @return removed (and invalidated) bookmark
     */
    public synchronized boolean removeBookmark (Bookmark bookmark) {
        boolean removed = bookmarks.contains(bookmark);
        BookmarkUtils.removeBookmarkUnderLock(bookmark.info());
        // Rest will be done by BookmarkManagerListener
        return removed;
    }

    private synchronized void removeBookmarkImpl(BookmarkInfo bInfo) {
        Bookmark bookmark = info2bookmark.remove(bInfo);
        if (bookmark != null) {
            bookmarks.remove(bookmark);
            bookmark.release();
            fireChange();
        }
    }
    
    /** Removes all bookmarks */
    public synchronized void removeAllBookmarks (){
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            lockedBookmarkManager.removeBookmarks(new ArrayList<BookmarkInfo>(info2bookmark.keySet()));
        } finally {
            lockedBookmarkManager.unlock();
        }
    }
    
    /**
     * Add an unnamed bookmark to this bookmark list on given line.
     * This method should only be called once bookmarks for the given project were loaded
     * in order to assign e.g. a proper id to an underlying bookmark info of the new bookmark.
     *
     * @param offset offset where the bookmark will be created.
     */
    public synchronized Bookmark addBookmark (int offset) {
        Bookmark bookmark = null;
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            if (fileBookmarks != null) {
                int id = fileBookmarks.getProjectBookmarks().generateBookmarkId();
                BookmarkInfo info = BookmarkInfo.create(id, "", offset, "");
                // Add bookmark early (not during change firing) since the API needs Bookmark instance
                bookmark = addBookmarkForInfo(info, offset);
                lockedBookmarkManager.addBookmark(fileBookmarks, info);
                BookmarkHistory.get().add(info);
                fireChange();
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
        return bookmark;
    }

    private @NonNull Bookmark addBookmarkForInfo(BookmarkInfo bookmarkInfo, int offset) {
        // It's possible that the bookmark instance for the given info already exists - see addBookmark()
        Bookmark bookmark = info2bookmark.get(bookmarkInfo);
        if (bookmark == null) {
            int lineIndex = bookmarkInfo.getLineIndex();
            if (offset == -1) {
                offset = BookmarkUtils.lineIndex2Offset(document, lineIndex);
            } else {
                lineIndex = BookmarkUtils.offset2LineIndex(document, offset);
                bookmarkInfo.setLineIndex(lineIndex);
            }
            bookmark = new Bookmark (this, bookmarkInfo, offset);
            bookmarks.add (bookmark);
            bookmarks.sort (bookmarksComparator);
            info2bookmark.put(bookmarkInfo, bookmark);
        }
        return bookmark;
    }

    Bookmark getBookmark(BookmarkInfo info) {
        return info2bookmark.get(info);
    }

    private void fireChange() {
        synchronized (bmListener) {
            if (pendingFireChange) {
                return;
            }
            pendingFireChange = true;
        }
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run() {
                synchronized (bmListener) {
                    pendingFireChange = false;
                }
                propertyChangeSupport.firePropertyChange (PROP_BOOKMARKS, null, null);
            }
        });
    }
    
    private void checkOffsetNonNegative(int offset) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset=" + offset + " < 0"); // NOI18N
        }
    }
    
    @Override
    public synchronized String toString() {
        return "Bookmarks: " + bookmarks; // NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    
    // innerclassses ...........................................................
    
    private static PropertyChangeListener documentModifiedListener = new PropertyChangeListener () {

        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            if ("modified".equals (evt.getPropertyName ()) &&  //NOI18N
                Boolean.FALSE.equals (evt.getNewValue ())
            ) {
                Observable observable = (Observable) evt.getSource();
                Document document = observable.getDocument ();
                if (document != null) {
                    // Document is being saved
                    BookmarkList.get(document).updateBookmarkLineIndexes();
                }
            }
        }
    };
    
    private static final Comparator<Bookmark> bookmarksComparator = new Comparator<Bookmark> () {

        @Override
        public int compare (Bookmark bookmark1, Bookmark bookmark2) {
            return bookmark1.getOffset () - bookmark2.getOffset ();
        }
    };
}

