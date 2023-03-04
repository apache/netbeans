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

package org.netbeans.modules.projectimport.eclipse.gui;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.ActionNoBlock;

public class ImporterMenu extends JellyTestCase {
    private static final String menuPath = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File");
    private static final String importMenuPath = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle","Menu/File/Import");
    private static final String menuRootString = menuPath+"|"+importMenuPath+"|";
    static final String menuItemString = menuRootString+Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "CTL_MenuItem");
   
    public ImporterMenu(String testName) {
     super(testName);
    }

    public void testImporterMenuImport() {
	checkMenuItem(menuItemString);
    }
    public void testImporterMenuSync() {
	checkMenuItem(menuRootString+Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "CTL_SynchronizeMenuItem"));
    }

    private void checkMenuItem(String menuItem) {
        new ActionNoBlock(menuItem, null).isEnabled();        
    }
}
