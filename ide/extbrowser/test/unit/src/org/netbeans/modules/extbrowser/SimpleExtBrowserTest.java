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
 * SimpleExtBrowserTest.java
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
public class SimpleExtBrowserTest extends NbTestCase {

    public SimpleExtBrowserTest (java.lang.String testName) {
        super(testName);
    }        
        
    /** Test of getName method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testGetName () {
        if (testObject.getName () == null)
            fail ("SimpleExtBrowser.getName () returns <null>.");
    }
    
    /** Test of setNamemethod, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testSetName () {
        testObject.setName ("Dummy");
    }
    
    /** Test of createHtmlBrowserImpl method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testCreateHtmlBrowserImpl () {
        if (testObject.createHtmlBrowserImpl () == null)
            fail ("SimpleExtBrowser.createHtmlBrowserImpl () returns <null>.");
    }
    
    /** Test of getBrowserExecutable method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testGetBrowserExecutable () {
        if (testObject.getBrowserExecutable () == null)
            fail ("SimpleExtBrowser.getBrowserExecutable () returns <null>.");
    }
    
    /** Test of setBrowserExecutable method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testSetBrowserExecutable () {
        testObject.setBrowserExecutable (new NbProcessDescriptor ("netscape", ""));
    }
    
    /** Test of addPropertyChangeListener method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testAddPropertyChangeListener () {
        testObject.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    /** Test of removePropertyChangeListener method, of class org.netbeans.modules.extbrowser.SimpleExtBrowser. */
    public void testRemovePropertyChangeListener () {
        testObject.removePropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    protected SimpleExtBrowser testObject;
    
    protected void setUp () {
        testObject = new SimpleExtBrowser ();
    }

}
