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

package org.netbeans.core.startup;

import java.awt.EventQueue;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;

/** Tests the behavior of the license check "api".
 */
public class NonGuiHandleCheckOfLicenseTest extends NbTestCase {
    private static NonGuiHandleCheckOfLicenseTest instance;

    private File user;
    private File userVar;
    private boolean updaterInvoked;
    private Throwable toThrow;

    /** invoked from the test by the core-launcher */
    public static void showLicensePanel () throws Throwable {
        instance.nowDoTheInstall();
    }

    public NonGuiHandleCheckOfLicenseTest (String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        CLIOptions.clearForTests ();
        
        File home = new File (getWorkDir (), "nb/home");
        user = new File (getWorkDir (), "user");
        userVar = new File (user,"var");
        
        assertTrue ("Home dir created", home.mkdirs ());
        assertTrue ("User dir created", user.mkdirs ());
        
        System.setProperty ("netbeans.home", home.toString ());
        System.setProperty ("netbeans.user", user.toString ());
        
        System.setProperty ("netbeans.accept_license_class", NonGuiHandleCheckOfLicenseTest.class.getName ());
        
        File f = new File(userVar,"license_accepted");
        if (f.exists()) {
            f.delete();
        }
        
        instance = this;
        Logger.getLogger(Main.class.getName()).setLevel(Level.OFF);
    }
    
    @Override
    protected void tearDown () throws Exception {
        instance = null;
    }
    
    private void nowDoTheInstall () throws Throwable {
        assertTrue("Called from AWT thread", EventQueue.isDispatchThread());
        if (toThrow != null) {
            Throwable t = toThrow;
            toThrow = null;
            throw t;
        }
        
        updaterInvoked = true;
    }
    
    /** Test if check is invoked when there is not file "var/license_accepted" */
    public void testIfTheUserDirIsEmptyTheLicenseCheckIsInvoked () {
        assertTrue ("Ok, returns without problems", Main.handleLicenseCheck ());
        assertTrue ("the main method invoked", updaterInvoked);
        
        toThrow = new RuntimeException ();
        
        //File "var/license_accepted" is created during first call in user dir
        //then license check is not invoked anymore
        assertTrue ("The check is not called anymore 1", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 2", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 3", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 4", Main.handleLicenseCheck ());
        
        File f = new File(userVar,"license_accepted");
        if (f.exists()) {
            f.delete();
        }
    }
    
    public void testIfInvokedAndThrowsExceptionTheExecutionStops () {
        toThrow = new RuntimeException();
        
        assertFalse ("Says no as exception was thrown", Main.handleLicenseCheck());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
    }
    
    public void testIfThrowsUserCancelExThenLicenseCheckIsCalledAgain () {
        toThrow = new org.openide.util.UserCancelException();
        assertFalse("Says no as user did not accept the license", Main.handleLicenseCheck());
        assertNull("Justs to be sure the exception was cleared", toThrow);
        
        toThrow = new org.openide.util.UserCancelException();
        assertFalse("Says no as user did not accept the license", Main.handleLicenseCheck());
        assertNull("Justs to be sure the exception was cleared", toThrow);
    }
    
}
