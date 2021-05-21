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
