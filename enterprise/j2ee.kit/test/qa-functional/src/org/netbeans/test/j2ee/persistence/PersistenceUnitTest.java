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
package org.netbeans.test.j2ee.persistence;

import java.io.File;
import java.io.IOException;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.test.j2ee.lib.FilteringLineDiff;
import org.netbeans.test.j2ee.lib.Utils;

/**
 *
 * @author rroska
 */
public class PersistenceUnitTest extends J2eeTestCase {

    public static File PROJECT_FILE;
    protected static final String PATH = "/persistence.xml";
    protected static PUDataObject dataObject;
    private static Project project;
    private static WebProject webproj;
    Utils utils;

    public PersistenceUnitTest(String test) {
        super(test);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanPersistenceUnits();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.ANY, PersistenceUnitTest.class,
                "testOpenProject",
                "testPUProviders",
                "testPUDataSource");
    }

    private void cleanPersistenceUnits() {
        for (PersistenceUnit pu : dataObject.getPersistence().getPersistenceUnit()) {
            System.out.println(pu.getName());
            dataObject.removePersistenceUnit(pu);
        }
        dataObject.save();
        assertTrue("not zero count of pu", 0 == dataObject.getPersistence().getPersistenceUnit().length);
    }

    public void testOpenProject() throws Exception {
        PROJECT_FILE = new File(getDataDir(), "projects/TestPersistence");
        project = (Project) J2eeProjectSupport.openProject(PROJECT_FILE);
        assertNotNull("Project is null.", project);
        webproj = (WebProject) project;
        assertNotNull("Project is not webproject", webproj);

        dataObject = ProviderUtil.getPUDataObject(webproj);
        assertNotNull(" PU data object is null", dataObject);

        utils = new org.netbeans.test.j2ee.lib.Utils(this);
    }

    public void testPUProviders() throws Exception {
        int i = 0;

        String[] table_generations = {Provider.TABLE_GENERATION_CREATE, Provider.TABLE_GENERATION_DROPCREATE, Provider.TABLE_GENERATTION_UNKOWN};

        for (String s : table_generations) {
            System.out.println(s);
            for (Provider p : ProviderUtil.getAllProviders()) {
                System.out.println(i);
                PersistenceUnit persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
                assertNotNull("Persistence unit not created ", persistenceUnit);

                persistenceUnit.setName("pu" + Integer.toString(i++));
                persistenceUnit.setDescription("description of persistence unit");
                persistenceUnit.setJtaDataSource(p.getDefaultJtaDatasource());
                persistenceUnit.setProvider(p.getProviderClass());
                ProviderUtil.setTableGeneration(persistenceUnit, s, p);

                dataObject.addPersistenceUnit(persistenceUnit);
            }
        }
        dataObject.save();
        assertPersistenceFile(new File(PROJECT_FILE, "src/conf/persistence.xml"), getName() + "_");

    }

    public void testPUDataSource() throws Exception {
        int i = 0;

        PersistenceUnit persistenceUnit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        assertNotNull("Persistence unit not created ", persistenceUnit1);

        Provider p = ProviderUtil.DEFAULT_PROVIDER;
        persistenceUnit1.setName("pu" + Integer.toString(i++));
        persistenceUnit1.setDescription("description of persistence unit");
        persistenceUnit1.setJtaDataSource("jta_data_source");
        persistenceUnit1.setProvider(p.getProviderClass());
        ProviderUtil.setTableGeneration(persistenceUnit1, Provider.TABLE_GENERATION_CREATE, p);

        dataObject.addPersistenceUnit(persistenceUnit1);

        PersistenceUnit persistenceUnit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        assertNotNull("Persistence unit not created ", persistenceUnit2);

        persistenceUnit2.setName("pu" + Integer.toString(i++));
        persistenceUnit2.setDescription("description of persistence unit");
        persistenceUnit2.setNonJtaDataSource("nonjta_data_source");
        persistenceUnit2.setProvider(p.getProviderClass());
        ProviderUtil.setTableGeneration(persistenceUnit2, Provider.TABLE_GENERATION_CREATE, p);

        dataObject.addPersistenceUnit(persistenceUnit2);
        dataObject.save();

        assertPersistenceFile(new File(PROJECT_FILE, "src/conf/persistence.xml"), getName() + "_");
    }

    public void assertPersistenceFile(File targetFile, String goldenFilePrefix) throws IOException {

        try {
            File goldenFile = getGoldenFile(goldenFilePrefix + "persistence.xml");
            assertFile("File " + targetFile.getAbsolutePath() + " is different than golden file " + goldenFile.getAbsolutePath() + ".", targetFile, goldenFile, new File(getWorkDir(), targetFile.getName() + ".diff"), new FilteringLineDiff());
        } catch (AssertionFailedError e) {
            if (!getWorkDir().exists()) {
                if (getWorkDir().mkdir()) {
                    log("Workdir created.");
                } else {
                    log("Cannot create workdir.");
                }
            }
            File copy = new File(getWorkDirPath(), goldenFilePrefix + "persistence.xml");
            Utils.copyFile(targetFile, copy);

            throw e;
        }
    }
}
