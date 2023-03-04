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

package org.netbeans.lib.editor.bookmarks.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.util.NbBundle;


/**
 * Action that jumps to next/previous bookmark.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class GotoBookmarkAction extends BaseAction {
    
    public static final String GOTO_NEXT_NAME = "bookmark-next"; // NOI18N
    
    public static final String GOTO_PREVIOUS_NAME = "bookmark-previous"; // NOI18N
    
    static final long serialVersionUID = -5169554640178645108L;
    
    public static GotoBookmarkAction createNext() {
        return new GotoBookmarkAction(true);
    }
    
    public static GotoBookmarkAction createPrevious() {
        return new GotoBookmarkAction(false);
    }
    
    private final boolean gotoNext;
    
    private final boolean select;
    
    public GotoBookmarkAction(boolean gotoNext) {
        this(gotoNext, false);
    }

    /**
     * Construct new goto bookmark action.
     *
     * @param gotoNext <code>true</code> if this action should go to a next bookmark.
     *   <code>false</code> if this action should go to a previous bookmark.
     * @param select whether the selection should extend from the current
     *  caret location to the bookmark.
     */
    public GotoBookmarkAction(boolean gotoNext, boolean select) {
        super(gotoNext ? GOTO_NEXT_NAME : GOTO_PREVIOUS_NAME,
            MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET
        );
        
        this.gotoNext = gotoNext;
        this.select = select;

        putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                gotoNext
                ? "org/netbeans/modules/editor/bookmarks/resources/next_bookmark.png" // NOI18N
                : "org/netbeans/modules/editor/bookmarks/resources/previous_bookmark.png" // NOI18N
        );
        
        updateEnabled();
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateEnabled();
            }
        });
    }

    private void updateEnabled() {
        setEnabled(EditorRegistry.lastFocusedComponent() != null);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            Caret caret = target.getCaret();
            BookmarkList bookmarkList = BookmarkList.get(target.getDocument());
            int dotOffset = caret.getDot();
            Bookmark bookmark = gotoNext
                ? bookmarkList.getNextBookmark(dotOffset, true) // next (wrap)
                : bookmarkList.getPreviousBookmark(dotOffset, true); // previous (wrap)

            if (bookmark != null) {
                if (select) {
                    caret.moveDot(bookmark.getOffset());
                } else {
                    caret.setDot(bookmark.getOffset());
                }
            }
        }
    }

    @Override
    protected Object getDefaultShortDescription() {
        return NbBundle.getBundle(GotoBookmarkAction.class).getString(
                (String)getValue(Action.NAME));
    }

}


