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

package org.netbeans.upgrade.systemoptions;

import junit.framework.*;

/**
 * @author Radek Matous
 */
public class IDESettingsTest extends BasicTestForImport {
    public IDESettingsTest(String testName) {
        super(testName, "org-netbeans-core-IDESettings.settings");
    }
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
