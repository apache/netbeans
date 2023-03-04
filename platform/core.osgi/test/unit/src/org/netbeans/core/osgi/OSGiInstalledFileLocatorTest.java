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

package org.netbeans.core.osgi;

import java.io.File;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;

public class OSGiInstalledFileLocatorTest extends NbTestCase {

    public OSGiInstalledFileLocatorTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testLocate() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(LocateInstall.class).
                sourceFile("OSGI-INF/files/some/stuff", "some text").
                sourceFile("OSGI-INF/files/some/otherstuff", "hello").
                manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + LocateInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().
                run(false);
        assertEquals("2", System.getProperty("my.file.count"));
        assertEquals("6", System.getProperty("my.file.length"));
        assertEquals("10", System.getProperty("my.url.length"));
    }
    public static class LocateInstall extends ModuleInstall {
        public @Override void restored() {
            try {
                System.setProperty("my.file.count",
                        Integer.toString(InstalledFileLocator.getDefault().locate("some", null, false).list().length));
                System.setProperty("my.file.length",
                        Long.toString(new File(InstalledFileLocator.getDefault().locate("some/stuff", null, false).getParentFile(), "otherstuff").length()));
                System.setProperty("my.url.length",
                        Integer.toString(new URL("nbinst://custom/some/stuff").openConnection().getContentLength()));
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    public void testLocateModuleJARs() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(LocateJARInstall.class).
                manifest(
                "OpenIDE-Module: some.cnb",
                "OpenIDE-Module-Install: " + LocateJARInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().
                run(false);
        String myself = System.getProperty("myself");
        assertNotNull(myself);
        File jar = new File(myself);
        assertTrue(jar.isFile());
        assertEquals("some.cnb.jar", jar.getName());
    }
    public static class LocateJARInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("myself", String.valueOf(InstalledFileLocator.getDefault().locate("modules/some-cnb.jar", "some.cnb", false)));
        }
    }

}
