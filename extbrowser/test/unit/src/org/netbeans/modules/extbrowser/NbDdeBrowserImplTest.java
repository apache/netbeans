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
        getDDEBrowserImpl().getDefaultOpenCommand ();
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
