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

import org.netbeans.junit.NbTestCase;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;

public class OSGiLifecycleManagerTest extends NbTestCase {

    public OSGiLifecycleManagerTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testModuleInstallClosing() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(ModuleInstallClosingInstall.class).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + ModuleInstallClosingInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().run(false);
        assertTrue(Boolean.getBoolean("my.bundle.was.closing"));
        assertTrue(Boolean.getBoolean("my.bundle.closed"));
    }
    public static class ModuleInstallClosingInstall extends ModuleInstall {
        public @Override void restored() {
            LifecycleManager.getDefault().exit();
        }
        public @Override boolean closing() {
            System.setProperty("my.bundle.was.closing", "true");
            return true;
        }
        public @Override void close() {
            System.setProperty("my.bundle.closed", "true");
        }
    }

}
