/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.util.Utilities;

/**
 *
 * 
 */
public class ShellConstructorTest extends NbTestCase {

    public ShellConstructorTest(String name) throws FileNotFoundException, IOException {
        super(name);
    }

    private void resetOs() throws Exception {
        // hack to call reset OS of BaseUtilies
        Class<?> classz = Class.forName("org.openide.util.BaseUtilities");
        Method m = classz.getDeclaredMethod("resetOperatingSystem");
        m.setAccessible(true);
        m.invoke(null);
    }

    /**
     * Test of construct method, of class ShellConstructor.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testShellConstructoronLinux() throws Exception {
        resetOs();
        String previous = System.getProperty("os.name");
        System.getProperties().put("os.name", "Linux");
        assertFalse("Must be linux", Utilities.isWindows());
        System.getProperties().put("os.name", previous);

        assertTrue("2.2 linux", getCLI("2.2", "2.2.1", "mvn"));
        assertTrue("3.0.5 linux", getCLI("3.0.5", "3.0.5", "mvn"));
        assertTrue("3.3.1 linux", getCLI("3.3.1", "3.3.1", "mvn"));
        assertTrue("4.0.0 linux", getCLI("4.0.0", "4.0.0", "mvn"));
        System.getProperties().put("os.name", previous);
        resetOs();

    }

    @Test
    public void testShellconstructoronWindows() throws Exception {
        resetOs();
        String previous = System.getProperty("os.name");
        System.getProperties().put("os.name", "Windows ");
        assertTrue("Must be windows", Utilities.isWindows());
        System.getProperties().put("os.name", previous);
        assertTrue("2.2 windows", getCLI("2.2", "2.2.1", "mvn.bat"));
        assertTrue("3.0.5 windows", getCLI("3.0.5", "3.0.5", "mvn.bat"));
        assertTrue("3.3.1 windows", getCLI("3.3.1", "3.3.1", "mvn.cmd"));
        assertTrue("4.0.0 windows", getCLI("4.0.0", "4.0.0", "mvn.cmd"));
        
        System.getProperties().put("os.name", previous);
        resetOs();
    }

    private boolean getCLI(String folder, String requestedversion, String mvn) {
        File sourceJar = new File(this.getDataDir(), "mavenmock/" + folder + "/");
        String version = MavenSettings.getCommandLineMavenVersion(sourceJar);
        assertEquals(requestedversion, version);
        ShellConstructor shellConstructor = new ShellConstructor(sourceJar);
        List<String> construct = shellConstructor.construct();
        if (Utilities.isWindows()) {
            assertTrue("cli must contains " + mvn, construct.get(2).contains(mvn));
        } else {
            assertTrue("cli must contains " + mvn, construct.get(0).contains(mvn));
        }
        return true;
    }
}
