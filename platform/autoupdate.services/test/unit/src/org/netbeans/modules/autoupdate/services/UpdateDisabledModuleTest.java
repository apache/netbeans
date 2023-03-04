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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.netbeans.updater.UpdaterInternal;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class UpdateDisabledModuleTest extends NbTestCase {
    static Manifest man;
    private File ud;

    public UpdateDisabledModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "com.example.testmodule.cluster");
        man.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");

        ud = new File(getWorkDir(), "ud");
        System.setProperty("netbeans.user", ud.getPath());
        new File(ud, "config").mkdirs();

        final File install = new File(getWorkDir(), "install");
        File platform = new File(install, "platform");
        System.setProperty("netbeans.home", platform.getPath());
        new File(platform, "config").mkdirs();

        File middle = new File(install, "middle");
        File last = new File(install, "last");
        System.setProperty("netbeans.dirs", middle.getPath() + File.pathSeparator + last.getPath());

        final String fn = moduleCodeNameBaseForTest().replace('.', '-') + ".xml";
        File conf = new File(new File(new File(middle, "config"), "Modules"), fn);
        conf.getParentFile().mkdirs();
        writeConfXML(conf, false);

        final File jar = new File(new File(last, "modules"), "com-example-testmodule-cluster.jar");
        jar.getParentFile().mkdirs();
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar), man);
        os.close();

        File real = new File(new File(new File(last, "config"), "Modules"), fn);
        real.getParentFile().mkdirs();
        writeConfXML(real, true);

        FileUtil.getConfigRoot().getFileSystem().refresh(true);

        File ut = new File(new File(last, "update_tracking"), fn);
        ut.getParentFile().mkdirs();
        OutputStream utos = new FileOutputStream(ut);
        String utcfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
"<module codename=\"com.example.testmodule.cluster\">\n" +
"    <module_version install_time=\"1280356738644\" last=\"true\" origin=\"installer\" specification_version=\"0.99\">\n" +
"        <file crc=\"3486416273\" name=\"config/Modules/com-example-testmodule-cluster.xml\"/>\n" +
"        <file crc=\"3486416273\" name=\"modules/com-example-testmodule-cluster.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";
        utos.write(utcfg.getBytes(StandardCharsets.UTF_8));
        utos.close();

        StringBuilder msg = new StringBuilder();
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            msg.append(mi.getCodeNameBase()).append("\n");
            if (mi.getCodeNameBase().equals("com.example.testmodule.cluster")) {
                assertFalse("Disabled", mi.isEnabled());
                return;
            }
        }
        fail("No com.example.testmodule.cluster module found:\n" + msg);
    }

    private void writeConfXML(File conf, boolean enabled) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(conf);
        String cfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
                "<module name='com.example.testmodule.cluster'>\n" +
                "   <param name='autoload'>false</param>\n" +
                "   <param name='eager'>false</param>\n" +
                "   <param name='enabled'>" + enabled + "</param>\n" +
                "   <param name='jar'>modules/com-example-testmodule-cluster.jar</param>\n" +
                "   <param name='reloadable'>false</param>\n" +
                "   <param name='specversion'>1.0</param>\n" +
                "</module>\n" +
                "\n";
        os.write(cfg.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    String moduleCodeNameBaseForTest() {
        return "com.example.testmodule.cluster"; //NOI18N
    }

    public void testSelf() throws Exception {
        File f = new File(new File(new File(ud, "config"), "Modules"), "com-example-testmodule-cluster.xml");
        f.delete();

        assertFalse("No Config file before: " + f, f.exists());

        MockServices.setServices(UP.class);
        UpdateUnit update = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());

        assertNotNull("There is an NBM to update", update);
        OperationContainer<InstallSupport> oc = OperationContainer.createForUpdate();
        oc.add(update, update.getAvailableUpdates().get(0));
        final InstallSupport support = oc.getSupport();
        Validator down = support.doDownload(null, true);
        Installer inst = support.doValidate(down, null);
        Restarter res = support.doInstall(inst, null);
        System.setProperty("netbeans.close.no.exit", "true");
        support.doRestart(res, null);
        UpdaterInternal.update(null, null, null);

        assertFalse("No Config file created in for upgrade: " + f, f.exists());
    }

    public static final class UP implements UpdateProvider {

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public String getDisplayName() {
            return "test view";
        }

        @Override
        public String getDescription() {
            return "none";
        }

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.STANDARD;
        }

        @Override
        public Map<String, UpdateItem> getUpdateItems() throws IOException {
            Map<String, UpdateItem> m = new HashMap<String, UpdateItem>();
            m.put("com.example.testmodule.cluster", UpdateItem.createModule(
                "com.example.testmodule.cluster", "1.0",
                DefaultTestCase.class.getResource("data/com-example-testmodule-cluster.nbm"),
                "jarda", "4000", "http://netbeans.de",
                "2010/10/27",
                "OK",
                man, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE,
                "platform",
                UpdateLicense.createUpdateLicense("CDDL", "Free to use")
            ));
            return m;
        }

        @Override
        public boolean refresh(boolean force) throws IOException {
            return true;
        }

    }
}
