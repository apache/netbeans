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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;

/** Used to call "Find" popup menu item, "Edit|Find in Projects" main menu item,
 * "org.netbeans.modules.search.FindInFilesAction".
 * @see Action
 * @see ActionNoBlock
 */
public class FindInFilesAction extends ActionNoBlock {
    // "Edit|Find in Projects..."
    private static final String menu =
            Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit") +
            "|" +
            Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "LBL_Action_FindInProjects");
    // "Find"
    private static final String popup =
            Bundle.getStringTrimmed("org.openide.actions.Bundle", "Find");
    
    /** Creates new instance. */
    public FindInFilesAction() {
        super(menu, popup, "org.netbeans.modules.search.FindInFilesAction");
    }
    
    /** Performs action through API. It selects a node first.
     * @throws UnsupportedOperationException when action does not support API mode */
    public void performAPI() {
        new ProjectsTabOperator().tree().selectRow(0);
        super.performAPI();
    }
}
