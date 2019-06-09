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

package org.netbeans.modules.payara.jakartaee.db;

import org.netbeans.modules.payara.jakartaee.db.Hk2DatasourceManager;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Payara server data source manager tests.
 * <p/>
 * @author Vince Kraemer, Tomas Kraus
 */
public class Hk2DatasourceManagerTest extends NbTestCase {

    private J2eeModule j2eeModule;
    private HK2TestEEModuleImpl j2eeModuleImpl;

    public Hk2DatasourceManagerTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        File dataDir = getDataDir();
        File rootFolder = new File(dataDir, "hk2sample");
        rootFolder.mkdirs();
        FileObject rootFolderFO = FileUtil.toFileObject(rootFolder);
        j2eeModuleImpl = new HK2TestEEModuleImpl(
                rootFolderFO, J2eeModule.Type.WAR, Profile.JAVA_EE_7_FULL.toPropertiesString());
        j2eeModule = J2eeModuleFactory.createJ2eeModule(j2eeModuleImpl);
    }

    @After
    @Override
    public void tearDown() {
    }


    /**
     * Test of getDatasources method, of class Hk2DatasourceManager.
     */
    @Test
    public void testGetDatasources_File() {
        // expected data in source
        URL codebase = getClass().getProtectionDomain().getCodeSource().getLocation();
        if (!codebase.getProtocol().equals("file")) {  // NOI18N
            throw new Error("Cannot find data directory from " + codebase); // NOI18N
        }
        File resourceDir = null;
        try {
            resourceDir = new File(
                    new File(codebase.toURI()).getParentFile(), "data/178776");  // NOI18N
        } catch (URISyntaxException x) {
            throw new Error(x);
        }
        Set<Datasource> result = Hk2DatasourceManager.getDatasources(
                j2eeModule, PayaraVersion.PF_4_1_144);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        assert null != result : "null result";
        for (Datasource ds : result) {
            assertNotNull(ds.getDriverClassName());
        }
    }

}