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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.impl.actions;

import org.netbeans.modules.editor.MainMenuAction;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class LineActionsMainMenu {

    // From BaseKit
    private static final String moveSelectionElseLineUpAction = "move-selection-else-line-up"; // NOI18N
    private static final String moveSelectionElseLineDownAction = "move-selection-else-line-down"; // NOI18N
    private static final String copySelectionElseLineUpAction = "copy-selection-else-line-up"; // NOI18N
    private static final String copySelectionElseLineDownAction = "copy-selection-else-line-down"; // NOI18N
    
    
    public static final class MoveUp extends MainMenuAction {
        public MoveUp() {
            super();
            postSetMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(moveSelectionElseLineUpAction + "-main_menu_item"); //NOI18N
        }

        protected String getActionName() {
            return moveSelectionElseLineUpAction;
        }
    } // End of MoveUp class
    
    public static final class MoveDown extends MainMenuAction {
        public MoveDown() {
            super();
            postSetMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(moveSelectionElseLineDownAction + "-main_menu_item"); //NOI18N
        }

        protected String getActionName() {
            return moveSelectionElseLineDownAction;
        }
    } // End of MoveDown class
    
    public static final class DuplicateUp extends MainMenuAction {
        public DuplicateUp() {
            super();
            postSetMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(copySelectionElseLineUpAction + "-main_menu_item"); //NOI18N
        }

        protected String getActionName() {
            return copySelectionElseLineUpAction;
        }
    } // End of DuplicateUp class
    
    public static final class DuplicateDown extends MainMenuAction {
        public DuplicateDown() {
            super();
            postSetMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(copySelectionElseLineDownAction + "-main_menu_item"); //NOI18N
        }

        protected String getActionName() {
            return copySelectionElseLineDownAction;
        }
    } // End of DuplicateDown class
    
}
