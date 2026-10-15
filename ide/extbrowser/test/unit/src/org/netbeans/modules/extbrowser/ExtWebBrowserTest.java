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
 * ExtWebBrowserTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */

package org.netbeans.modules.extbrowser;

import org.netbeans.junit.*;
import java.beans.*;
import org.openide.execution.NbProcessDescriptor;

/**
 *
 * @author rk109395
 */
public class ExtWebBrowserTest extends NbTestCase {

    public ExtWebBrowserTest (java.lang.String testName) {
        super(testName);
    }        
        
    /** Test of getDescription method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
/*    public void testGetName () {
        if (testObject.getName () == null)
            fail ("ExtWebBrowser.getName () returns <null>.");
    }
 */
    
    /** Test of getBrowserExecutable method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testGetBrowserExecutable () {
        if (testObject.getBrowserExecutable () == null)
            fail ("ExtWebBrowser.getBrowserExecutable () returns <null>.");
    }
    
    /** Test of setBrowserExecutable method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testSetBrowserExecutable () {
        testObject.setBrowserExecutable (new NbProcessDescriptor ("netscape", ""));
    }
    
    /** Test of isStartWhenNotRunning method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    /*public void testIsStartWhenNotRunning () {
        testObject.isStartWhenNotRunning ();
    } */
    
    /** Test of setStartWhenNotRunning method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    /*public void testSetStartWhenNotRunning () {
        testObject.setStartWhenNotRunning (true);
    } */
    
    /** Test of defaultBrowserExecutable method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testDefaultBrowserExecutable () {
        if (testObject.defaultBrowserExecutable () == null)
            fail ("ExtWebBrowser.defaultBrowserExecutable () failed.");
    }
    
    /** Test of createHtmlBrowserImpl method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testCreateHtmlBrowserImpl () {
        testObject.createHtmlBrowserImpl ();
    }
    
    /** Test of addPropertyChangeListener method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testAddPropertyChangeListener () {
        testObject.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    /** Test of removePropertyChangeListener method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testRemovePropertyChangeListener () {
        testObject.removePropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    protected ExtWebBrowser testObject;
    
    protected void setUp () {
        testObject = new ExtWebBrowser (PrivateBrowserFamilyId.UNKNOWN);
    }
    
}
