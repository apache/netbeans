/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.extbrowser.plugins.chrome;

import java.io.File;
import java.util.ArrayList;
import org.netbeans.modules.extbrowser.plugins.ExtensionManagerAccessor;
import org.netbeans.modules.extbrowser.plugins.Utils;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;


import org.openide.util.Utilities;



/**
 * @author ads
 *
 */
public class ChromiumManagerAccessor implements ExtensionManagerAccessor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.plugins.ExtensionManagerAccessor#getManager()
     */
    @Override
    public BrowserExtensionManager getManager() {
        return new ChromiumExtensionManager();
    }

    
    private static class ChromiumExtensionManager extends ChromeManagerAccessor.ChromeExtensionManager {

        @Override
        public BrowserFamilyId getBrowserFamilyId() {
            return BrowserFamilyId.CHROMIUM;
        }

        protected String[] getUserData(){
            if (Utilities.isWindows()) {
                ArrayList<String> result = new ArrayList<String>();
                String localAppData = System.getenv("LOCALAPPDATA");                // NOI18N
                if (localAppData != null) {
                    result.add(localAppData+"\\Chromium\\User Data");
                } else {
                    localAppData = Utils.getLOCALAPPDATAonWinXP();
                    if (localAppData != null) {
                        result.add(localAppData+"\\Chromium\\User Data");
                    }
                }
                
                String appData = System.getenv("APPDATA");                // NOI18N
                if (appData != null) {
                    // we are in C:\Documents and Settings\<username>\Application Data\ on XP
                    File f = new File(appData);
                    if (f.exists()) {
                        String fName = f.getName();
                        // #219824 - below code will not work on some localized WinXP where
                        //    "Local Settings" name might be "Lokale Einstellungen";
                        //     no harm if we try though:
                        f = new File(f.getParentFile(),"Local Settings");
                        f = new File(f, fName);
                        if (f.exists())
                            result.add(f.getPath()+"\\Chromium\\User Data");
                    }
                }
                return result.toArray(new String[0]);
            } 
            else if (Utilities.isMac()) {
                return Utils.getUserPaths("/Library/Application Support/Chromium");// NOI18N
            } 
            else {
                return Utils.getUserPaths("/snap/chromium/current/.config/chromium", "/.config/chromium");// NOI18N
            }
        }
        
    }
}
