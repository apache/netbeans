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
/*
 * NbDdeBrowserImplTest.java
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
public class NbDdeBrowserImplTest extends NbTestCase {

    public NbDdeBrowserImplTest (java.lang.String testName) {
        super(testName);
    }        
        
    private NbDdeBrowserImpl getDDEBrowserImpl() {
        return (NbDdeBrowserImpl)((DelegatingWebBrowserImpl)testObject).getImplementation();
    }
    
    /** Test of getBrowserPath method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testGetBrowserPath () throws NbBrowserException {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        getDDEBrowserImpl().getBrowserPath ("IEXPLORE");
    }
    
    /** Test of getDefaultOpenCommand method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testGetDefaultOpenCommand () throws NbBrowserException {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        getDDEBrowserImpl().getDefaultWindowsOpenCommand();
        
        /** if not found with getDefaultWindowsOpenCommand function
        *  fallback to previous method
        */
         if (getDDEBrowserImpl().getDefaultWindowsOpenCommand().isEmpty())
        {
            getDDEBrowserImpl().getDefaultOpenCommand();
        }
    }
    
    /** Test of backward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testBackward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        
        testObject.backward ();
    }
    
    /** Test of forward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testForward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.forward ();
    }
    
    /** Test of isBackward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testIsBackward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.isBackward ();
    }
    
    /** Test of isForward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testIsForward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.isForward ();
    }
    
    /** Test of isHistory method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testIsHistory () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        if (testObject.isHistory ())
            fail ("NbDdeBrowserImpl.isHistory retunred true. It should return false.");
    }
    
    /** Test of reloadDocument method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testReloadDocument () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.reloadDocument ();
    }
    
    /** Test of setURL method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testSetURL () throws java.net.MalformedURLException {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.setURL (new java.net.URL ("http://www.netbeans.org/"));
    }
    
    /** Test of showHistory method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testShowHistory () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.showHistory ();
    }
    
    /** Test of stopLoading method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testStopLoading () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.stopLoading ();
    }
    
    protected ExtBrowserImpl testObject;
    
    protected void setUp () {
        if (org.openide.util.Utilities.isWindows ())
            testObject = (ExtBrowserImpl)new ExtWebBrowser (PrivateBrowserFamilyId.UNKNOWN).createHtmlBrowserImpl ();
    }

}
