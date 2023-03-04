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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;

/**
 * Node for either real Bookmark or a BookmarkInfo.
 *
 * @author Miloslav Metelka
 */
public class BookmarkNode extends AbstractNode {
    
    private static final Action DEFAULT_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source instanceof BookmarkNode) {
                BookmarkNode node = (BookmarkNode) source;
                node.openInEditor();
            }
        }
    };
    
    private final BookmarkInfo bookmarkInfo;
    
    private static Image bookmarkIcon;

    public BookmarkNode(BookmarkInfo bookmarkInfo) {
        super(Children.LEAF);
        assert (bookmarkInfo != null);
        this.bookmarkInfo = bookmarkInfo;
        setShortDescription(bookmarkInfo.getFullPathDescription());
    }
    
    @Override
    public String getName() {
        return bookmarkInfo.getName();
    }

    @Override
    public void setName(String name) {
        String origName = getName();
        if (!name.equals(origName)) {
            BookmarkUtils.setBookmarkNameUnderLock(bookmarkInfo, name);
            fireNameChange(origName, name);
            fireDisplayNameChange(null, null);
        }
    }

    void notifyBookmarkChanged() {
        fireNameChange(null, null);
        fireDisplayNameChange(null, null);
    }

    @Override
    public String getDisplayName() {
        return bookmarkInfo.getDisplayName();
    }

    @Override
    public Action getPreferredAction() {
        return DEFAULT_ACTION;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(RenameAction.class),
            SystemAction.get(DeleteAction.class)
        };
    }
    
    @Override
    public Image getIcon(int type) {
        if (bookmarkIcon == null) {
            bookmarkIcon = ImageUtilities.loadImage(
                    "org/netbeans/modules/editor/bookmarks/resources/bookmark_16.png", false);
        }
        return bookmarkIcon;

    }

    @Override
    public <T extends Cookie> T getCookie(Class<T> type) {
        if (type == OpenCookie.class) {
            @SuppressWarnings("unchecked")
            T impl = (T) new OpenCookieImpl();
            return impl;
        }
        return super.getCookie(type);
    }
    
    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
        BookmarkUtils.removeBookmarkUnderLock(bookmarkInfo);
    }
    
    void openInEditor() {
        BookmarkUtils.postOpenEditor(bookmarkInfo);
    }
    
    public BookmarkInfo getBookmarkInfo() {
        return bookmarkInfo;
    }
    
    private final class OpenCookieImpl implements OpenCookie {

        @Override
        public void open() {
            openInEditor();
        }
        
    }

}
