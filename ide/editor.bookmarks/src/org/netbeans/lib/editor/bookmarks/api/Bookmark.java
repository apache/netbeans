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

package org.netbeans.lib.editor.bookmarks.api;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.bookmarks.BookmarkAPIAccessor;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;


/**
 * Interface to a bookmark.
 *
 * @author Miloslav Metelka
 */

public final class Bookmark {
    
    static {
        BookmarkAPIAccessor.INSTANCE = new BookmarkAPIAccessorImpl();
    }

    public static final String BOOKMARK_ANNOTATION_TYPE = "editor-bookmark"; // NOI18N

    // cary mary fuk!
    private static Map<Line,Reference<Annotation>> lineToAnnotation = new WeakHashMap<Line,Reference<Annotation>> (); // Hanziii;)

    /**
     * Bookmark list to which this bookmark belongs.
     */
    private BookmarkList    bookmarkList;

    /**
     * Whether this mark was released or not.
     */
    private boolean         released;
    
    private final BookmarkInfo info; // Hold name and key

    private Line            line;
    
    /**
     * Construct new instance of bookmark.
     *
     * <p>
     * The constructor is not public intentionally.
     * Please see <code>BookmarksApiPackageAccessor</code> for details.
     */
    Bookmark (BookmarkList bookmarkList, BookmarkInfo info, int offset) {
        if (info == null) {
            throw new IllegalArgumentException("info cannot be null"); // NOI18N
        }
        this.bookmarkList = bookmarkList;
        this.info = info;
        Document document = bookmarkList.getDocument ();
        int lineIndex = BookmarkUtils.offset2LineIndex(document, offset);
        DataObject dataObject = NbEditorUtilities.getDataObject (document);
        for (Line _line : lineToAnnotation.keySet ()) {
            if (_line.getLineNumber () == lineIndex &&
                _line.getLookup().lookup (DataObject.class).equals (dataObject)
            ) {
                this.line = _line;
                Reference<Annotation> annoRef = lineToAnnotation.get (_line);
                Annotation a = annoRef.get();
                if (a != null) {
                    info.setAnnotationRef(annoRef);
                }
            }
        }
        line = NbEditorUtilities.getLine (bookmarkList.getDocument (), offset, false);
        if (line != null) { // In tests it may be null
            if (info.getAnnotation() == null) {
                AAnnotation annotation = new AAnnotation ();
                info.setAnnotationRef(new WeakReference<Annotation>(annotation));
                annotation.attach (line);
            } 
        }
    }

    /**
     * Bookmark name may be used to identify bookmark in a bookmark manager.
     * <br/>
     * All its characters satisfy {@link Character#isJavaIdentifierPart(char) }.
     * <br/>
     * Since bookmarks are stored on a per-project manner the bookmark names may be duplicate
     * across projects and there is no restriction to have unique names even
     * within a single project.
     */
    /*public*/ String getName() {
        return info.getName();
    }
    
    /**
     * Get offset of this bookmark.
     * <br>
     * Offsets behave like {@link javax.swing.text.Position}s (they track
     * inserts/removals).
     */
    public int getOffset () {
        return BookmarkUtils.lineIndex2Offset(bookmarkList.getDocument(), line.getLineNumber());
    }

    /**
     * Get zero-based index of line at which this bookmark resides.
     */
    public int getLineNumber () {
        return line.getLineNumber ();
    }
    
    /**
     * Current implementation returns a single char [0-9a-z] used for jumping
     * to the bookmark by a keystroke in a Goto dialog or an empty string
     * when no shortcut was assigned yet.
     * <br/>
     * Non-single char values are reserved for future use (current code ignores them).
     * <br/>
     * Since bookmarks are stored on a per-project manner the bookmark keys may be duplicate
     * across projects and there is no restriction to have unique keys even
     * within a single project. In case of conflict an arbitrary mark with the given key is chosen.
     */
    public String getKey() {
        return info.getKey();
    }

    /**
     * Get the bookmark list for which this bookmark was created.
     */
    public BookmarkList getList() {
        return bookmarkList;
    }
    
    /**
     * Return true if this mark was released (removed from its bookmark list)
     * and is no longer actively used.
     */
    public boolean isReleased() {
        return released;
    }
    
    /**
     * Mark the current bookmark as invalid.
     */
    void release () {
        assert (!released);
        released = true;
        if (info.getAnnotation() != null) {
            info.getAnnotation().detach();
        }
        lineToAnnotation.remove (line);
    }
    
    BookmarkInfo info() {
        return info;
    }

    @Override
    public String toString() {
        return "Bookmark: " + info + "\n"; // NOI18N
    }
    
    // innerclasses ............................................................
    
    public final class AAnnotation extends Annotation {

        @Override
        public String getAnnotationType () {
            return BOOKMARK_ANNOTATION_TYPE;
        }

        @Override
        public String getShortDescription () {
            String fmt = NbBundle.getBundle (Bookmark.class).getString ("Bookmark_Tooltip"); // NOI18N
            int lineIndex = getLineNumber ();
            return MessageFormat.format (fmt, new Object[] {Integer.valueOf(lineIndex + 1)});
        }

        @Override
        public String toString() {
            return getShortDescription();
        }
    }

    private static final class BookmarkAPIAccessorImpl extends BookmarkAPIAccessor {

        @Override
        public BookmarkInfo getInfo(Bookmark b) {
            return b.info();
        }

        @Override
        public Bookmark getBookmark(Document doc, BookmarkInfo b) {
            return BookmarkList.get(doc).getBookmark(b);
        }
        
    }
    
}

