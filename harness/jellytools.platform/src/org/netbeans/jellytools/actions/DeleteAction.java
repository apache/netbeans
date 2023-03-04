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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;

/** Used to call "Delete" popup menu item, "Edit|Delete" main menu item,
 * "org.openide.actions.DeleteAction" or Delete shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DeleteAction extends ActionNoBlock {

    private static final String deletePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
    private static final String deleteMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit")
                                            + "|" + deletePopup;

    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

    /** creates new DeleteAction instance */    
    public DeleteAction() {
        super(deleteMenu, deletePopup, "org.openide.actions.DeleteAction", keystroke);
    }
    
    /**
     * Waits for confirmation dialog and approves deletion of object. It handles
     * both Confirm Object Deletion dialog and Delete dialog with refactoring.
     */
    public static void confirmDeletion() {
        NbDialogOperator deleteDialogOper = new NbDialogOperator("Delet");
        if (deleteDialogOper.getTitle().equals("Delete")) {
            // "Delete" - safe delete when scanning is not running
            deleteDialogOper.ok();
        } else {
            // "Confirm Object Deletion" - if scanning is in progress
            deleteDialogOper.yes();
        }
        deleteDialogOper.waitClosed();
    }
}
