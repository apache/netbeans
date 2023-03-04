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
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

public class OSGiMainLookupTest extends NbTestCase {

    public OSGiMainLookupTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testModuleInfo() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().clazz(ModuleInfoInstall.class).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + ModuleInfoInstall.class.getName(),
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup").done().run(false);
        String numberOfModules = System.getProperty("number.of.modules");
        assertNotNull(numberOfModules);
        assertTrue(numberOfModules, Integer.parseInt(numberOfModules) > 2);
    }
    public static class ModuleInfoInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("number.of.modules", String.valueOf(Lookup.getDefault().lookupAll(ModuleInfo.class).size()));
        }
    }

    public void testServicesFolder() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().
                clazz(Interface.class).clazz(Service.class).clazz(ServicesFolderInstall.class).
                sourceFile("custom/layer.xml", "<filesystem>",
                "<folder name='Services'>",
                "<file name='svc.instance'><attr name='instanceClass' stringvalue='" + Service.class.getName() + "'/>" +
                "<attr name='instanceOf' stringvalue='" + Interface.class.getName() + "'/></file>",
                "</folder>",
                "</filesystem>").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: " + ServicesFolderInstall.class.getName(),
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup, org.netbeans.core/2").done().
                module("org.netbeans.core").
                run(false);
        assertEquals("[ok]", System.getProperty("custom.service.result"));
    }
    public interface Interface {}
    public static class Service implements Interface {
        public @Override String toString() {return "ok";}
    }
    public static class ServicesFolderInstall extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("custom.service.result", Lookup.getDefault().lookupAll(Interface.class).toString());
        }
    }

    public void testUnloadedServices() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().
                clazz(ServiceInterface.class).clazz(LoadedService.class).service(ServiceInterface.class, LoadedService.class).
                manifest(
                "OpenIDE-Module: core",
                "OpenIDE-Module-Public-Packages: " + ServiceInterface.class.getPackage().getName() + ".*",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup").done().
                newModule().clazz(UnloadedService.class).service(ServiceInterface.class, UnloadedService.class).
                manifest("OpenIDE-Module: other",
                "OpenIDE-Module-Requires: org.openide.modules.os.Nonexistent",
                "OpenIDE-Module-Module-Dependencies: core").done().newModule().
                clazz(ServiceFinder.class).
                manifest("OpenIDE-Module: runner",
                "OpenIDE-Module-Install: " + ServiceFinder.class.getName(),
                "OpenIDE-Module-Module-Dependencies: core").done().run(false);
        assertEquals("[LoadedService]", System.getProperty("services"));
    }
    public static class ServiceFinder extends ModuleInstall {
        public @Override void restored() {
            System.setProperty("services", Lookup.getDefault().lookupAll(ServiceInterface.class).toString());
        }
    }
    public interface ServiceInterface {}
    public static class LoadedService implements ServiceInterface {
        @Override public String toString() {return "LoadedService";}
    }
    public static class UnloadedService implements ServiceInterface {
        @Override public String toString() {return "UnloadedService";}
    }

}
