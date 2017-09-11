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
