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
import org.netbeans.jellytools.Bundle;

/** Used to call "File|New Project..."  main menu item, "org.netbeans.modules.project.ui.actions.NewProject" action
 *  or Ctrl+Shift+N shortcut.<br>
 * Usage:
 * <pre>
 *  new NewProjectAction.performMenu();
 *  new NewProjectAction().performShortcut();
 * </pre>
 * @see Action
 * @see ActionNoBlock
 * @author tb115823
 */
public class NewProjectAction extends ActionNoBlock {

    /** File|New Project..." main menu path. */
    private  static final String menuPath = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File")
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewProjectAction_Name");
    
    /** Creates new NewProjectAction instance. */
    public NewProjectAction() {
        super(menuPath, null, "org.netbeans.modules.project.ui.actions.NewProject");
    }
    
}
