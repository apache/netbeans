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
package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.openide.util.NbBundle;

/**
 * History of visited bookmarks.
 *
 * @author Miloslav Metelka
 */
public class BookmarkHistoryPopupAction extends BaseAction {
    
    public static final String GOTO_NEXT_NAME = "bookmark-history-popup-next"; // NOI18N
    
    public static final String GOTO_PREVIOUS_NAME = "bookmark-history-popup-previous"; // NOI18N
    
    static final long serialVersionUID = 1L;
    
    public static BookmarkHistoryPopupAction createNext() {
        return new BookmarkHistoryPopupAction(true);
    }
    
    public static BookmarkHistoryPopupAction createPrevious() {
        return new BookmarkHistoryPopupAction(false);
    }
    
    private final boolean gotoNext;
    
    /**
     * Construct new goto bookmark action.
     *
     * @param gotoNext <code>true</code> if this action should go to a next bookmark.
     *   <code>false</code> if this action should go to a previous bookmark.
     * @param select whether the selection should extend from the current
     *  caret location to the bookmark.
     */
    public BookmarkHistoryPopupAction(boolean gotoNext) {
        super(gotoNext ? GOTO_NEXT_NAME : GOTO_PREVIOUS_NAME,
            MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET
        );
        
        this.gotoNext = gotoNext;

        putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                gotoNext
                ? "org/netbeans/modules/editor/bookmarks/resources/next_bookmark.png" // NOI18N
                : "org/netbeans/modules/editor/bookmarks/resources/previous_bookmark.png" // NOI18N
        );
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        BookmarkHistoryPopup.get().show(gotoNext);
    }

    @Override
    protected Object getDefaultShortDescription() {
        return NbBundle.getMessage(
                BookmarkHistoryPopupAction.class, (String) getValue(Action.NAME));
    }

    
}
