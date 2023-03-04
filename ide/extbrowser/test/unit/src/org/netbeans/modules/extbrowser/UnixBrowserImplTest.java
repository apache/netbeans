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

package org.netbeans.modules.extbrowser;

import org.netbeans.junit.*;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author rk109395
 */
public class UnixBrowserImplTest extends NbTestCase {

    public UnixBrowserImplTest (java.lang.String testName) {
        super(testName);
    }        
        
    /** Test of backward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testBackward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.backward ();
    }
    
    /** Test of forward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testForward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.forward ();
    }
    
    /** Test of isBackward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testIsBackward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.isBackward ();
    }
    
    /** Test of isForward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testIsForward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.isForward ();
    }
    
    /** Test of isHistory method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testIsHistory () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        if (testObject.isHistory ())
            fail ("NbDdeBrowserImpl.isHistory retunred true. It should return false.");
    }
    
    /** Test of reloadDocument method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testReloadDocument () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.reloadDocument ();
    }
    
    /** Test of setURL method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
	/** commented out - see bug 194635
    public void testSetURL () throws java.net.MalformedURLException {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.setURL (new java.net.URL ("http://www.netbeans.org/"));
    }
	*/
    
    /** Test of showHistory method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testShowHistory () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.showHistory ();
    }
    
    /** Test of stopLoading method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testStopLoading () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.stopLoading ();
    }
    
    protected HtmlBrowser.Impl testObject;
    
    protected void setUp () {
        if (org.openide.util.Utilities.isUnix ())
            testObject = new ExtWebBrowser (PrivateBrowserFamilyId.UNKNOWN).createHtmlBrowserImpl ();
    }

}
