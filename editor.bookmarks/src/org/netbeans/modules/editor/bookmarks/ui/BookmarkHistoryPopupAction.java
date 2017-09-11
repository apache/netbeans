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
