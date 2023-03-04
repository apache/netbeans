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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallDisabledModuleTest extends OperationsTestImpl {
    public InstallDisabledModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        File test = new File(getWorkDir(), "test");
        
        System.setProperty("netbeans.dirs", test.getPath());
        LOG.log(Level.INFO, "Setting netbeans.dirs property to {0}", System.getProperty("netbeans.dirs"));
        clearWorkDir();
        super.setUp();        
        assertEquals(test.getPath(), System.getProperty("netbeans.dirs"));
        
        File jar = new File(new File(test, "modules"), "com-example-testmodule-cluster.jar");
        jar.getParentFile().mkdirs();
        jar.createNewFile();
        
        final String fn = moduleCodeNameBaseForTest().replace('.', '-') + ".xml";
        FileObject fo = FileUtil.getConfigFile("Modules").createData(fn);
        OutputStream os = fo.getOutputStream();
        String cfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
                "<module name='com.example.testmodule.cluster'>\n" +
                "   <param name='autoload'>false</param>\n" +
                "   <param name='eager'>false</param>\n" +
                "   <param name='enabled'>false</param>\n" +
                "   <param name='jar'>modules/com-example-testmodule-cluster.jar</param>\n" +
                "   <param name='reloadable'>false</param>\n" +
                "   <param name='specversion'>1.0</param>\n" +
                "</module>\n" +
                "\n";
        os.write(cfg.getBytes(StandardCharsets.UTF_8));
        os.close();
        LOG.info("Config file created");

        assertNotNull("File exists", FileUtil.getConfigFile("Modules/" + fn));
    }

    @Override
    boolean incrementNumberOfModuleConfigFiles() {
        return false;
    }
    @Override
    boolean writeDownConfigFile() {
        return true;
    }

    protected String moduleCodeNameBaseForTest() {
        return "com.example.testmodule.cluster"; //NOI18N
    }

    @RandomlyFails @Override
    public void testSelf() throws Throwable {
        LOG.info("testSelf starting");
        UpdateUnit install = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull("There is an NBM to install", install);
        LOG.log(Level.INFO, "module install found: {0}", install);
        Throwable t = null;
        try {
            installModule(install, null);//fail("OK");
        } catch (Throwable ex) {
            t = ex;
        }
        LOG.log(Level.INFO, "Info installModule over with {0}", t);
        
        File f = new File(new File(new File(new File(System.getProperty("netbeans.user")), "config"), "Modules"), "com-example-testmodule-cluster.xml");
        LOG.log(Level.INFO, "Does {0} exists: {1}", new Object[]{f, f.exists()});
        File m = new File(new File(new File(getWorkDir(), "test"), "modules"), "com-example-testmodule-cluster.jar");
        LOG.log(Level.INFO, "Does {0} exists: {1}", new Object[]{m, m.exists()});
        if (t != null) {
            throw t;
        }
        assertTrue("Config file created in userdirectory for install of new module: " + f, f.exists());
    }
}
