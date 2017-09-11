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
