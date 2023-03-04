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

package gui.windowsystem;

import java.io.PrintStream;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.junit.NbModuleSuite;

/**
 * @author mmirilovic@netbeans.org
 */
public class MainMenu extends JellyTestCase {

    protected PrintStream err;
    protected PrintStream log;

    public MainMenu(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.allModules(MainMenu.class);
    }

    public @Override void setUp() {
        // err = System.out;
        err = getLog();
        log = getRef();

        try {
            // set defaults
            JemmyProperties.getProperties().setOutput(new TestOut(null, new PrintWriter(err, true), new PrintWriter(err, true), null));
        }catch(Exception exc) {
            failTest(exc, "Fail setUp() - maybe MainFrame hasn't menubar");
        }

    }

    public void testMainMenuMnemonicsCollision() {
        String collisions = MenuChecker.checkMnemonicCollision();
        assertFalse("There were mnemonic collisions:" + collisions, collisions.length()>0);
    }


    public void testMainMenuShortCutCollision() {
        String collisions = MenuChecker.checkShortCutCollision();
        assertFalse("There were accelerator collisions:" + collisions, collisions.length()>0);
    }

    /** Print full stack trace to log files, get message and log to test results if test fails.
     * @param exc Exception logged to description
     * @param message written to test results
     */
    private void failTest(Exception exc, String message) {
        try{
            getWorkDir();
            org.netbeans.jemmy.util.PNGEncoder.captureScreen(getWorkDirPath()+System.getProperty("file.separator")+"IDEscreenshot.png");
        }catch(Exception ioexc){
            log("Impossible make IDE screenshot!!! \n" + ioexc.toString());
        }

        err.println("################################");
        exc.printStackTrace(err);
        err.println("################################");
        fail(message);
    }

}
