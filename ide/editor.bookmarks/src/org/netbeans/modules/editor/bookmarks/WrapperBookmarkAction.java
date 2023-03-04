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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.bookmarks.ui.BookmarkHistoryPopupAction;
import org.netbeans.lib.editor.bookmarks.actions.ClearDocumentBookmarksAction;
import org.netbeans.lib.editor.bookmarks.actions.GotoBookmarkAction;
import org.openide.awt.ActionID;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;


/**
 * Action wrapping the bookmark actions.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class WrapperBookmarkAction extends AbstractAction implements PropertyChangeListener{
    
    static final long serialVersionUID = 0L;
    
    protected Action originalAction;

    public WrapperBookmarkAction(Action originalAction) {
        this.originalAction = originalAction;
        putValue(Action.NAME, originalAction.getValue(Action.SHORT_DESCRIPTION));
        putValue(Action.SHORT_DESCRIPTION, Actions.cutAmpersand( (String) originalAction.getValue(Action.SHORT_DESCRIPTION)));
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon( (String) originalAction.getValue(BaseAction.ICON_RESOURCE_PROPERTY),false));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        // Re-add the property as SystemAction.putValue() is final
//        putValue(BaseAction.ICON_RESOURCE_PROPERTY, getValue(BaseAction.ICON_RESOURCE_PROPERTY));
        updateEnabled();
        originalAction.addPropertyChangeListener(this);
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) { // NOI18N
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        setEnabled(originalAction.isEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        originalAction.actionPerformed(e);
    }

    public static final class Next extends WrapperBookmarkAction {
        
        public Next() {
            super(GotoBookmarkAction.createNext());
        }

    }

    public static final class Previous extends WrapperBookmarkAction {
        
        public Previous() {
            super(GotoBookmarkAction.createPrevious());
        }

    }

    // Action ID corresponds to current layer registration
    @ActionID(id = "bookmark.history.popup.next", category = "Edit")
    public static final class PopupNext extends WrapperBookmarkAction {
        
        public PopupNext() {
            super(BookmarkHistoryPopupAction.createNext());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Call action directly to handle state when no last text component is present
            ((BaseAction)originalAction).actionPerformed(e, null);
        }
        
    }

    // Action ID corresponds to current layer registration
    @ActionID(id = "bookmark.history.popup.previous", category = "Edit")
    public static final class PopupPrevious extends WrapperBookmarkAction {
        
        public PopupPrevious() {
            super(BookmarkHistoryPopupAction.createPrevious());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Call action directly to handle state when no last text component is present
            ((BaseAction)originalAction).actionPerformed(e, null);
        }
        
    }

    public static final class ClearDocumentBookmarks extends WrapperBookmarkAction {
        
        public ClearDocumentBookmarks() {
            super(new ClearDocumentBookmarksAction());
        }

    }

}

