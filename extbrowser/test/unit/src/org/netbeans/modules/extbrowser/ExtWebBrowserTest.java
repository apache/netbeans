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
    
    /** Test of getDDEServer method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testGetDDEServer () {
        testObject.getDDEServer ();
    }
    
    /** Test of setDDEServer method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testSetDDEServer () {
        testObject.setDDEServer ("NETSCAPE");
    }
    
    protected ExtWebBrowser testObject;
    
    protected void setUp () {
        testObject = new ExtWebBrowser (PrivateBrowserFamilyId.UNKNOWN);
    }
    
}
