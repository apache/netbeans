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
                return result.toArray(new String[result.size()]);
            } 
            else if (Utilities.isMac()) {
                return Utils.getUserPaths("/Library/Application Support/Chromium");// NOI18N
            } 
            else {
                return Utils.getUserPaths("/.config/chromium");// NOI18N
            }
        }
        
    }
}
