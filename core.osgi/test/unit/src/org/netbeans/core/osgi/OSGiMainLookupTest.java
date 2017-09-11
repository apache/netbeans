/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
