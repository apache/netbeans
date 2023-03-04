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

/** Tests the behaviour of the import user dir "api".
 */
public class NonGuiHandleImportOfUserDirTest extends NbTestCase {
    private static NonGuiHandleImportOfUserDirTest instance;

    private File user;
    private boolean updaterInvoked;
    private Throwable toThrow;

    /** invoked from the test by the core-launcher */
    public static void main(String[] args) throws Throwable {
        instance.nowDoTheInstall();
    }

    public NonGuiHandleImportOfUserDirTest (String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        CLIOptions.clearForTests ();
        
        File home = new File (getWorkDir (), "nb/home");
        user = new File (getWorkDir (), "user");
        
        assertTrue ("Home dir created", home.mkdirs ());
        assertTrue ("User dir created", user.mkdirs ());
        
        System.setProperty ("netbeans.home", home.toString ());
        System.setProperty ("netbeans.user", user.toString ());
        
        System.setProperty ("netbeans.importclass", NonGuiHandleImportOfUserDirTest.class.getName ());
        
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
    
    public void testIfTheUserDirIsEmptyTheUpdaterIsInvoked () {
        assertTrue ("Ok, returns without problems", Main.handleImportOfUserDir ());
        assertTrue ("the main method invoked", updaterInvoked);
        
        toThrow = new RuntimeException ();

        assertTrue ("The install is not called anymore 1", Main.handleImportOfUserDir ());
        assertTrue ("The install is not called anymore 2", Main.handleImportOfUserDir ());
        assertTrue ("The install is not called anymore 3", Main.handleImportOfUserDir ());
        assertTrue ("The install is not called anymore 4", Main.handleImportOfUserDir ());
    }

    public void testIfInvokedAndThrowsExceptionTheExecutionStops () {
        toThrow = new RuntimeException ();
        
        assertFalse ("Says no as exception was thrown", Main.handleImportOfUserDir ());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
    }
    
    public void testIfThrowsUserCancelExThenUpdateIsFinished () {
        toThrow = new org.openide.util.UserCancelException ();
        
        assertTrue ("Says yes as user canceled the import", Main.handleImportOfUserDir ());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
        
        assertTrue ("The install is not called anymore 1", Main.handleImportOfUserDir ());
    }
	
    public void testExecutionGoesOnWhenThereIsIncorrctClass() {
        System.setProperty ("netbeans.importclass", "IDoNotExists");
        assertFalse ("Says no as class does not exists", Main.handleImportOfUserDir ());
    }
}
