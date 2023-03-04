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
import java.util.logging.Level;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallHiddenModuleTest extends OperationsTestImpl {

    public InstallHiddenModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();        
        System.setProperty("netbeans.dirs", getWorkDirPath());
        String udp = System.getProperty("netbeans.user");
        assertNotNull("User dir is provided", udp);
        File ud = new File(udp);
        File hidden = new File(new File(new File(ud, "config"), "Modules"),
            moduleCodeNameBaseForTest().replace('.', '-') + ".xml_hidden"
        );
        hidden.getParentFile().mkdirs();
        hidden.createNewFile();
    }

    protected String moduleCodeNameBaseForTest() {
        return "com.example.testmodule.cluster"; //NOI18N
    }

    @RandomlyFails // NB-Core-Build #2967
    public void testSelf() throws Exception {
        UpdateUnit install = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull("There is an NBM to install", install);
        installModule(install, null);
    }
}
