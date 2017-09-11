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

