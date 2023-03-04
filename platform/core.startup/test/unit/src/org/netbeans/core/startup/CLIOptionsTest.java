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

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class CLIOptionsTest extends NbTestCase {

    public CLIOptionsTest(String testName) {
        super(testName);
    }

    public void testNoSplash() {
        boolean orig = CLIOptions.isNoSplash();
        new CLIOptions().cli(new String[] { "-nosplash" });
        assertTrue("-nosplash is not valid option", orig == CLIOptions.isNoSplash());

        new CLIOptions().cli(new String[] { "--branding", "noexiting" });
        assertFalse("Splash is on in default branding", CLIOptions.isNoSplash());

	CLIOptions.defaultsLoaded = false;
        new CLIOptions().cli(new String[] { "--branding", "nosplash" });
        assertTrue("this branding disables the splash", CLIOptions.isNoSplash());
        
	CLIOptions.defaultsLoaded = false;
        new CLIOptions().cli(new String[] { "--branding", "noexiting", "--nosplash"});
        assertTrue("Splash is explicitly disabled", CLIOptions.isNoSplash());
    }
    
    /**
     * Test translation from L&F ID to L&F class.
     */
    public void testLafId2LafClass () {
        new CLIOptions().cli(new String[] { "--laf", "Metal" });
        try {
            assertEquals("Must be Metal", CLIOptions.uiClass, Class.forName("javax.swing.plaf.metal.MetalLookAndFeel"));
        } catch (ClassNotFoundException exc) {
        }
        if (Utilities.isWindows()) {
            new CLIOptions().cli(new String[] { "--laf", "Windows" });
            try {
                assertEquals("Must be Windows", CLIOptions.uiClass, Class.forName("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
            } catch (ClassNotFoundException exc) {
            }
        }
        if (Utilities.isMac()) {
            new CLIOptions().cli(new String[] { "--laf", "Aqua" });
            try {
                assertEquals("Must be MacOS", CLIOptions.uiClass, Class.forName("com.apple.laf.AquaLookAndFeel"));
            } catch (ClassNotFoundException exc) {
            }
        }
    }
    
    public void testUserdir() {
        String orig = System.setProperty("netbeans.user", "before");
        new CLIOptions().cli(new String[] { "-userdir", "wrong" });
        assertFalse("-userdir is not supported", "wrong".equals(System.getProperty("netbeans.user")));
        
        new CLIOptions().cli(new String[] { "--userdir", "correct" });
        final File exp = FileUtil.normalizeFile(new File("correct"));
        assertEquals("--userdir is supported via places", exp, Places.getUserDirectory());
        assertEquals("--userdir is supported", exp.getAbsolutePath(), System.getProperty("netbeans.user"));
        
        if (orig != null) {
            System.setProperty("netbeans.user", orig);
        }
    }
    
}
