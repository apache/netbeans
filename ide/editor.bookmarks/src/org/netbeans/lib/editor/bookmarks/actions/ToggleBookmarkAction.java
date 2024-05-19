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

package org.netbeans.lib.editor.bookmarks.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.awt.Actions;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Toggles a bookmark in a line in an opened document.
 *
 * @author Vita Stejskal
 */
public final class ToggleBookmarkAction extends BaseAction {

    private static final String ACTION_NAME = "bookmark-toggle"; // NOI18N
    private static final String ACTION_ICON = "org/netbeans/modules/editor/bookmarks/resources/toggle_bookmark.png"; // NOI18N

    public ToggleBookmarkAction() {
        super(NbBundle.getMessage(ToggleBookmarkAction.class, ACTION_NAME));
        putValue(ICON_RESOURCE_PROPERTY, ACTION_ICON);
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand((String) getValue(NAME)));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N

        updateEnabled();
        EditorRegistry.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            updateEnabled();
        });

    }

    private void updateEnabled() {
        setEnabled(EditorRegistry.lastFocusedComponent() != null);
    }

    @Override
    public void actionPerformed(ActionEvent arg0, JTextComponent target) {
        if (target != null) {
            if (org.netbeans.editor.Utilities.getEditorUI(target).isGlyphGutterVisible()) {
                Caret caret = target.getCaret();
                BookmarkList bookmarkList = BookmarkList.get(target.getDocument());
                bookmarkList.toggleLineBookmark(caret.getDot());

            } else { // Glyph gutter not visible -> just beep
                target.getToolkit().beep();
            }
        }
    }

    public static JTextComponent findComponent(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        return ec == null ? null : NbDocument.findRecentEditorPane(ec);
    }

}
