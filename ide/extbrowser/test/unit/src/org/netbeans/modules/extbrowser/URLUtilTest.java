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
/*
 * ExtWebBrowserTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */

package org.netbeans.modules.extbrowser;

import org.netbeans.junit.*;
         
/**
 *
 * @author rk109395
 */
public class URLUtilTest extends NbTestCase {

    public URLUtilTest (java.lang.String testName) {
        super(testName);
    }        
        
    public void testCreateExternalURL() throws Exception {
        // find fileobject for
        // jar:file:/${NB}/ide/modules/docs/org-netbeans-modules-usersguide.jar!/org/netbeans/modules/usersguide/pending.html
        /* XXX no such file; need to rewrite test to find a different resource, say from extbrowser.jar
        File f = InstalledFileLocator.getDefault().locate("modules/docs/org-netbeans-modules-usersguide.jar", null, false);
        assertNotNull("Usersguide module not found", f);
        FileObject fo = FileUtil.toFileObject(f);
        log("jar fileobject is  " + fo);
        assertNotNull("FileObject corresponding to usersguide module not found", fo);
        FileObject jar = FileUtil.getArchiveRoot(fo);
        assertNotNull("FileObject corresponding to usersguide as jar not found", jar);
        FileObject pendingPage = jar.getFileObject("org/netbeans/modules/usersguide/pending.html");
        URL pendingURL = pendingPage.getURL();
        log("original url is " + pendingURL);
        URL newURL1 = URLUtil.createExternalURL(pendingURL, true);
        log("jar url " + newURL1);
        URL newURL2 = URLUtil.createExternalURL(pendingURL, false);
        log("http url " + newURL2);
        assertEquals("HTTP URL is not local - does not contain 127.0.0.1", "127.0.0.1", newURL2.getHost());
         */
    }
    
}
