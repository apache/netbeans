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
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;

/**
 *
 * @author Radek Matous
 */
public class InstallIntoNewClusterTest extends OperationsTestImpl {

    public InstallIntoNewClusterTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();       
        System.setProperty("netbeans.dirs", getWorkDirPath());
    }

    protected String moduleCodeNameBaseForTest() {
        return "com.example.testmodule.cluster"; //NOI18N
    }

    @RandomlyFails // NB-Core-Build #1191
    public void testSelf() throws Exception {
        UpdateUnit toUnInstall = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull(toUnInstall);
        installModule(toUnInstall, null);
        unInstallModule(toUnInstall);
        installModule(toUnInstall, null);
        unInstallModule(toUnInstall);
    }

    public static final class NetBeansClusterCreator extends AutoupdateClusterCreator {
        protected  File findCluster(String clusterName) {
            String path = System.getProperty("netbeans.dirs", null);
            File f = path != null ? new File(path, clusterName) : null;
            return f != null ? f : null;
        }

        protected File[] registerCluster(String clusterName, File cluster) throws IOException {
            return new File[]{cluster};
        }
    }
}
