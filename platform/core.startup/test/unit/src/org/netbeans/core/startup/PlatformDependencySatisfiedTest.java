/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
