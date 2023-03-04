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

/** Used to call "Tools|Options" main menu item. If called on MAC it uses IDE API to
 * open Options.
 * @see Action
 */
public class OptionsViewAction extends Action {
    private static final String menu =
        Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle",
                                "Menu/Tools") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.options.Bundle",
                                "CTL_Options_Window_Action");

    /** Creates new instance. */    
    public OptionsViewAction() {
        super(menu, null, "org.netbeans.modules.options.OptionsWindowAction");
    }
    
    /** performs action through main menu. If called on MAC it uses IDE API to
     * open Options.
     */
    public void performMenu() {
        if(System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) { // NOI18N
            performAPI();
        } else {
            super.performMenu();
        }
    }
}
