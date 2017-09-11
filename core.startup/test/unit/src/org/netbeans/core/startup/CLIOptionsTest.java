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
                assertEquals("Must be MacOS", CLIOptions.uiClass, Class.forName("apple.laf.AquaLookAndFeel"));
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
