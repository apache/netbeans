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
