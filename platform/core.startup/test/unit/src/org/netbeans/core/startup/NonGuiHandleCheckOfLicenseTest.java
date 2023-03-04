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
