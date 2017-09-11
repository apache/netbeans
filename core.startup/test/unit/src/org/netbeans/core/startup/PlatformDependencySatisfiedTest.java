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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.MockEvents;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;

/** Checks whether a module with generated
 * @author Jaroslav Tulach
 */
public class PlatformDependencySatisfiedTest extends SetupHid {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(PlatformDependencySatisfiedTest.class);
    }

    private File moduleJarFile;

    public PlatformDependencySatisfiedTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getWorkDir().toPath();  //Initialize FileSystems with correct OS.
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        
        clearWorkDir();
        moduleJarFile = new File(getWorkDir(), "PlatformDependencySatisfiedModule.jar");

        // clean the operatingSystem field
        Field f = BaseUtilities.class.getDeclaredField("operatingSystem");
        f.setAccessible(true);
        f.set(null, -1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWindows2000() throws Exception {
        System.setProperty("os.name", "Windows 2000");
        assertTrue("We are on windows", Utilities.isWindows());
        
        assertEnableModule("org.openide.modules.os.Windows", true);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", false);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }
    
    public void testMacOSX() throws Exception {
        System.setProperty("os.name", "Mac OS X");
        assertTrue("We are on mac", Utilities.isMac());
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", true);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }

    public void testDarwin() throws Exception {
        System.setProperty("os.name", "Darwin");
        assertTrue("We are on mac", Utilities.isMac());
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", true);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }
    
    public void testLinux() throws Exception {
        System.setProperty("os.name", "Fedora Linux");
        assertTrue("We are on linux", (Utilities.getOperatingSystem() & Utilities.OS_LINUX) != 0);
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", true);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
        assertEnableModule("org.openide.modules.os.Linux", true);
        assertEnableModule("org.openide.modules.os.Solaris", false);
    }

    public void testSolaris() throws Exception {
        System.setProperty("os.name", "SunOS");
        assertTrue("We are on Solaris", (Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0);
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", true);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
        assertEnableModule("org.openide.modules.os.Linux", false);
        assertEnableModule("org.openide.modules.os.Solaris", true);
    }

    public void testBSD() throws Exception {
        System.setProperty("os.name", "FreeBSD X1.4");
        assertTrue("We are on unix", Utilities.isUnix());
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", true);
        assertEnableModule("org.openide.modules.os.PlainUnix", true);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", false);
    }

    public void testOS2() throws Exception {
        System.setProperty("os.name", "OS/2");
        assertEquals ("We are on os/2", Utilities.OS_OS2, Utilities.getOperatingSystem());
        
        assertEnableModule("org.openide.modules.os.Windows", false);
        assertEnableModule("org.openide.modules.os.MacOSX", false);
        assertEnableModule("org.openide.modules.os.Unix", false);
        assertEnableModule("org.openide.modules.os.PlainUnix", false);
        assertEnableModule("org.openide.modules.os.Garbage", false);
        assertEnableModule("org.openide.modules.os.OS2", true);
    }
    
    private void assertEnableModule(String req, boolean enable) throws Exception {
        Manifest man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "org.test.PlatformDependency/1");
        man.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        
        man.getMainAttributes ().putValue ("OpenIDE-Module-Requires", req);
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (moduleJarFile), man);
        os.putNextEntry (new JarEntry ("empty/test.txt"));
        os.close ();
        
        final MockEvents ev = new MockEvents();
        NbInstaller installer = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        ModuleFormatSatisfiedTest.addOpenideModules(mgr);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(moduleJarFile, null, false, false, false);
            if (enable) {
                assertEquals(Collections.EMPTY_SET, m1.getProblems());
                mgr.enable(m1);
                mgr.disable(m1);
            } else {
                assertFalse("We should not be able to enable the module", m1.getProblems().isEmpty());
            }
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
