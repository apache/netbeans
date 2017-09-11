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
 *
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
