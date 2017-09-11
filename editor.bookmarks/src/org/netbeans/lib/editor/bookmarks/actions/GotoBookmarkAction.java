/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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


