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

package org.netbeans.upgrade.systemoptions;

import junit.framework.*;

/**
 * @author Radek Matous
 */
public class IDESettingsTest extends BasicTestForImport {
    public IDESettingsTest(String testName) {
        super(testName, "org-netbeans-core-IDESettings.settings");
    }
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "IgnoredFiles",
            "UIMode",
            "WWWBrowser",
            "confirmDelete",
            "homePage",
            "modulesSortMode", // ignored
            "proxyNonProxyHosts",
            "proxyType",
            "showFileExtensions",
            "showToolTipsInIDE",
            "useProxy",
            "proxyHttpHost",
            "proxyHttpPort"
        });
    }
    
    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/core");
    }
    
    public void testIgnoredFiles() throws Exception {
        //java.lang.String
        assertProperty("IgnoredFiles","^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store))$|^\\.[#_]|~$");
    }
    
    public void testUIMode() throws Exception {
        //java.lang.Integer
        assertProperty("UIMode","2");
    }
    
    public void testWWWBrowser() throws Exception {
        //java.lang.String
        assertProperty("WWWBrowser","SL[/Browsers/FirefoxBrowser");
    }
    
    public void testConfirmDelete() throws Exception {
        //java.lang.Boolean
        assertProperty("confirmDelete","true");
    }
    public void testHomePage() throws Exception {
        //java.lang.String
        assertProperty("homePage","http://www.netbeans.org/");
    }
    
    public void testProxyType() throws Exception{
        //java.lang.Integer
        assertProperty("proxyType","1");
    }
    public void testShowFileExtensions() throws Exception{
        //java.lang.Boolean
        assertProperty("showFileExtensions","false");
    }
    
    public void testShowToolTipsInIDE() throws Exception{
        //java.lang.Boolean
        assertProperty("showToolTipsInIDE","false");
    }
    
    public void testUseProxy() throws Exception{
        //java.lang.Boolean
        assertProperty("useProxy","false");
    }
    
    public void testProxyHttpHost() throws Exception{
        //java.lang.String
        assertProperty("proxyHttpHost","");
    }
    
    public void testProxyHttpPort() throws Exception{
        //java.lang.String
        assertProperty("proxyHttpPort","");
    }

    public void testProxyNonProxyHosts() throws Exception{
        //java.lang.String
        assertProperty("proxyNonProxyHosts","localhost|127.0.0.1");
    }        
}
