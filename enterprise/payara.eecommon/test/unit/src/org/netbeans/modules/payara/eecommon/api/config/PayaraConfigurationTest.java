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
package org.netbeans.modules.payara.eecommon.api.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.payara.eecommon.api.config.JavaEEModule;
import org.netbeans.modules.payara.eecommon.api.config.PayaraConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 * Payara Java EE server configuration API support tests.
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraConfigurationTest extends NbTestCase {
    
    private GFTestEEModuleImpl moduleImplCar;
    private J2eeModule moduleCar;
    private GFTestEEModuleImpl moduleImplEjb;
    private J2eeModule moduleEjb;
    private GFTestEEModuleImpl moduleImplEar;
    private J2eeModule moduleEar;
    private GFTestEEModuleImpl moduleImplWar;
    private J2eeModule moduleWar;
    private GFTestEEModuleImpl moduleImplRar;
    private J2eeModule moduleRar;

    public PayaraConfigurationTest(final String testName) {
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
        final File dataDir = getDataDir();
        final File rootFolder = new File(dataDir, "gfsample");
        rootFolder.mkdirs();
        final FileObject rootFolderFO = FileUtil.toFileObject(rootFolder);
        moduleImplCar = new GFTestEEModuleImpl(
                rootFolderFO, J2eeModule.Type.CAR, Profile.JAVA_EE_7_FULL.toPropertiesString());
        moduleCar = J2eeModuleFactory.createJ2eeModule(moduleImplCar);
        moduleImplEjb = new GFTestEEModuleImpl(
                rootFolderFO, J2eeModule.Type.EJB, Profile.JAVA_EE_7_FULL.toPropertiesString());
        moduleEjb = J2eeModuleFactory.createJ2eeModule(moduleImplEjb);
        moduleImplEar = new GFTestEEModuleImpl(
                rootFolderFO, J2eeModule.Type.EAR, Profile.JAVA_EE_7_FULL.toPropertiesString());
        moduleEar = J2eeModuleFactory.createJ2eeModule(moduleImplEar);
        moduleImplRar = new GFTestEEModuleImpl(
                rootFolderFO, J2eeModule.Type.RAR, Profile.JAVA_EE_7_FULL.toPropertiesString());
        moduleRar = J2eeModuleFactory.createJ2eeModule(moduleImplRar);
        moduleImplWar = new GFTestEEModuleImpl(
                rootFolderFO, J2eeModule.Type.WAR, Profile.JAVA_EE_7_WEB.toPropertiesString());
        moduleWar = J2eeModuleFactory.createJ2eeModule(moduleImplWar);
    }

    @After
    @Override
    public void tearDown() {
        // Pass everything to GC.
        moduleImplCar = null;
        moduleCar = null;
        moduleImplEjb = null;
        moduleEjb = null;
        moduleImplEar = null;
        moduleEar = null;
        moduleImplRar = null;
        moduleRar = null;
        moduleImplWar = null;
        moduleWar = null;
    }

    /**
     * Test Java EE module directory structure for resource file.
     * @throws NoSuchMethodException when there is a problem with reflection.
     * @throws IllegalAccessException when there is a problem with reflection.
     * @throws IllegalArgumentException when there is a problem with reflection.
     * @throws InvocationTargetException  when there is a problem with reflection.
     */
    @Test
    public void testResourceFilePath()
            throws NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Method resourceFilePath = PayaraConfiguration.class.getDeclaredMethod(
                "resourceFilePath", J2eeModule.class, String.class);
        resourceFilePath.setAccessible(true);
        
        final String pathFragment = "myDirectory";
        final String verifyConfigDirCar = OsUtils.joinPaths(JavaEEModule.META_INF, pathFragment);
        final String verifyConfigDirEjb = OsUtils.joinPaths(JavaEEModule.META_INF, pathFragment);
        final String verifyConfigDirEar = OsUtils.joinPaths(JavaEEModule.META_INF, pathFragment);
        final String verifyConfigDirRar = OsUtils.joinPaths(JavaEEModule.META_INF, pathFragment);
        final String verifyConfigDirWar = OsUtils.joinPaths(JavaEEModule.WEB_INF, pathFragment);
        final String configDirCar = (String)resourceFilePath.invoke(null, moduleCar, pathFragment);
        final String configDirEjb = (String)resourceFilePath.invoke(null, moduleEjb, pathFragment);
        final String configDirEar = (String)resourceFilePath.invoke(null, moduleEar, pathFragment);
        final String configDirRar = (String)resourceFilePath.invoke(null, moduleRar, pathFragment);
        final String configDirWar = (String)resourceFilePath.invoke(null, moduleWar, pathFragment);
        assertEquals("Expected resource file path for CAR is: " + verifyConfigDirCar,
                verifyConfigDirCar, configDirCar);
        assertEquals("Expected resource file path for EJB is: " + verifyConfigDirEjb,
                verifyConfigDirEjb, configDirEjb);
        assertEquals("Expected resource file path for EAR is: " + verifyConfigDirEar,
                verifyConfigDirEar, configDirEar);
        assertEquals("Expected resource file path for RAR is: " + verifyConfigDirRar,
                verifyConfigDirRar, configDirRar);
        assertEquals("Expected resource file path for WAR is: " + verifyConfigDirWar,
                verifyConfigDirWar, configDirWar);
    }

    /**
     * Test new Payara resources file name generation depending on passed {@link PayaraVersion}.
     * Expected values are:<ul>
     * <li>PREFIX/src/conf/META_INF/sun-resources.xml for CAR, EAR, EJB and RAR
     *     on Payara older than 3.1</li\>
     * <li>PREFIX/src/conf/META_INF/sun-resources.xml for CAR, EAR, EJB and RAR
     *     on Payara 3.1 and later</li\>
     * <li>PREFIX/src/conf/WEB_INF/sun-resources.xml for WAR on Payara older than 3.1</li\>
     * <li>PREFIX/src/conf/WEB_INF/sun-resources.xml for WAR on Payara 3.1 and later</li\></ul>
     */
    @Test 
    public void testGetNewResourceFile() {
        for (PayaraVersion version : PayaraVersion.values()) {
            final Pair<File, Boolean> pairCar = PayaraConfiguration.getNewResourceFile(moduleCar, version);
            final Pair<File, Boolean> pairEar = PayaraConfiguration.getNewResourceFile(moduleEar, version);
            final Pair<File, Boolean> pairEjb = PayaraConfiguration.getNewResourceFile(moduleEjb, version);
            final Pair<File, Boolean> pairRar = PayaraConfiguration.getNewResourceFile(moduleRar, version);
            final Pair<File, Boolean> pairWar = PayaraConfiguration.getNewResourceFile(moduleWar, version);
            final File resourceFileCar = pairCar.first();
            final File resourceFileEar = pairEar.first();
            final File resourceFileEjb = pairEjb.first();
            final File resourceFileRar = pairRar.first();
            final File resourceFileWar = pairWar.first();
File verifyPrefixCar;
            File verifyPrefixEar;
            File verifyPrefixEjb;
            File verifyPrefixRar;
            File verifyPrefixWar;
            if (PayaraVersion.lt(version, PayaraVersion.PF_4_1_144)) {
                verifyPrefixCar = moduleCar.getResourceDirectory();
                verifyPrefixEar = moduleEar.getResourceDirectory();
                verifyPrefixEjb = moduleEjb.getResourceDirectory();
                verifyPrefixRar = moduleRar.getResourceDirectory();
                verifyPrefixWar = moduleWar.getResourceDirectory();
            } else {
                verifyPrefixCar = moduleCar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleCar.getType()));
                verifyPrefixEar = moduleEar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleEar.getType()));
                verifyPrefixEjb = moduleEjb.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleEjb.getType()));
                verifyPrefixRar = moduleRar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleRar.getType()));
                verifyPrefixWar = moduleWar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleWar.getType()));
            }
            final String fileName = PayaraVersion.lt(version, PayaraVersion.PF_4_1_144)
                    ? "sun-resources.xml"
                    : "glassfish-resources.xml";
            final File verifyFileCar = new File(verifyPrefixCar, fileName);
            final File verifyFileEar = new File(verifyPrefixEar, fileName);
            final File verifyFileEjb = new File(verifyPrefixEjb, fileName);
            final File verifyFileRar = new File(verifyPrefixRar, fileName);
            final File verifyFileWar = new File(verifyPrefixWar, fileName);
            assertTrue("New resource file for " + version.toString() + " CAR: " + verifyFileCar.toString() + " was " + resourceFileCar,
                    verifyFileCar.equals(resourceFileCar));
            assertTrue("New resource file for " + version.toString() + " EAR: " + verifyFileEar.toString() + " was " + resourceFileEar,
                    verifyFileEar.equals(resourceFileEar));
            assertTrue("New resource file for " + version.toString() + " EJB: " + verifyFileEjb.toString() + " was " + resourceFileEjb,
                    verifyFileEjb.equals(resourceFileEjb));
            assertTrue("New resource file for " + version.toString() + " RAR: " + verifyFileRar.toString() + " was " + resourceFileRar,
                    verifyFileRar.equals(resourceFileRar));
            assertTrue("New resource file for " + version.toString() + " WAR: " + verifyFileWar.toString() + " was " + resourceFileWar,
                    verifyFileWar.equals(resourceFileWar));
        }
    }


    /**
     * Verify that proper resource file from Java EE module is returned.
     * Both {@code sun-resources.xml} and {@code glassfish-resources.xml} are available
     * in Java EE module configuration directory on the disk.
     * Expected values are:<ul>
     * <li>PREFIX/src/conf/META_INF/sun-resources.xml for CAR, EAR, EJB and RAR
     *     on Payara older than 3.1</li\>
     * <li>PREFIX/src/conf/META_INF/sun-resources.xml for CAR, EAR, EJB and RAR
     *     on Payara 3.1 and later</li\>
     * <li>PREFIX/src/conf/WEB_INF/sun-resources.xml for WAR on Payara older than 3.1</li\>
     * <li>PREFIX/src/conf/WEB_INF/sun-resources.xml for WAR on Payara 3.1 and later</li\></ul>
     */
    @Test 
    public void testGetExistingResourceFile() throws IOException {
        final File prefixCar = moduleCar.getDeploymentConfigurationFile(
                JavaEEModule.getConfigDir(moduleCar.getType()));
        final File prefixEar = moduleEar.getDeploymentConfigurationFile(
                JavaEEModule.getConfigDir(moduleEar.getType()));
        final File prefixEjb = moduleEjb.getDeploymentConfigurationFile(
                JavaEEModule.getConfigDir(moduleEjb.getType()));
        final File prefixRar = moduleRar.getDeploymentConfigurationFile(
                JavaEEModule.getConfigDir(moduleRar.getType()));
        final File prefixWar = moduleWar.getDeploymentConfigurationFile(
                JavaEEModule.getConfigDir(moduleWar.getType()));
        final Set<File> prefixes = new HashSet<File>(5);
        prefixes.add(prefixCar);
        prefixes.add(prefixEar);
        prefixes.add(prefixEjb);
        prefixes.add(prefixRar);
        prefixes.add(prefixWar);
        // Create all resource files (they are empty but it's enough for this test).
        for (File prefix : prefixes) {
            final File sunResource = new File(prefix, "sun-resources.xml");
            final File gfResource = new File(prefix, "glassfish-resources.xml");
            prefix.mkdirs();
            sunResource.createNewFile();
            gfResource.createNewFile();
        }
        for (PayaraVersion version : PayaraVersion.values()) {
            final Pair<File, Boolean> pairCar = PayaraConfiguration.getNewResourceFile(moduleCar, version);
            final Pair<File, Boolean> pairEar = PayaraConfiguration.getNewResourceFile(moduleEar, version);
            final Pair<File, Boolean> pairEjb = PayaraConfiguration.getNewResourceFile(moduleEjb, version);
            final Pair<File, Boolean> pairRar = PayaraConfiguration.getNewResourceFile(moduleRar, version);
            final Pair<File, Boolean> pairWar = PayaraConfiguration.getNewResourceFile(moduleWar, version);
            final File resourcesCar = pairCar.first();
            final File resourcesEar = pairEar.first();
            final File resourcesEjb = pairEjb.first();
            final File resourcesRar = pairRar.first();
            final File resourcesWar = pairWar.first();
            File verifyPrefixCar;
            File verifyPrefixEar;
            File verifyPrefixEjb;
            File verifyPrefixRar;
            File verifyPrefixWar;
            if (PayaraVersion.lt(version, PayaraVersion.PF_4_1_144)) {
                verifyPrefixCar = moduleCar.getResourceDirectory();
                verifyPrefixEar = moduleEar.getResourceDirectory();
                verifyPrefixEjb = moduleEjb.getResourceDirectory();
                verifyPrefixRar = moduleRar.getResourceDirectory();
                verifyPrefixWar = moduleWar.getResourceDirectory();
            } else {
                verifyPrefixCar = moduleCar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleCar.getType()));
                verifyPrefixEar = moduleEar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleEar.getType()));
                verifyPrefixEjb = moduleEjb.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleEjb.getType()));
                verifyPrefixRar = moduleRar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleRar.getType()));
                verifyPrefixWar = moduleWar.getDeploymentConfigurationFile(
                        JavaEEModule.getConfigDir(moduleWar.getType()));
            }
            final String fileName = PayaraVersion.lt(version, PayaraVersion.PF_4_1_144)
                    ? "sun-resources.xml"
                    : "glassfish-resources.xml";
            final File verifyFileCar = new File(verifyPrefixCar, fileName);
            final File verifyFileEar = new File(verifyPrefixEar, fileName);
            final File verifyFileEjb = new File(verifyPrefixEjb, fileName);
            final File verifyFileRar = new File(verifyPrefixRar, fileName);
            final File verifyFileWar = new File(verifyPrefixWar, fileName);
            assertTrue("Existing resource file for " + version.toString() + " CAR: "
                    + verifyFileCar.toString() + " was " + resourcesCar,
                    verifyFileCar.equals(resourcesCar));
            assertTrue("Existing resource file for " + version.toString() + " EAR: "
                    + verifyFileEar.toString() + " was " + resourcesEar,
                    verifyFileEar.equals(resourcesEar));
            assertTrue("Existing resource file for " + version.toString() + " EJB: "
                    + verifyFileEjb.toString() + " was " + resourcesEjb,
                    verifyFileEjb.equals(resourcesEjb));
            assertTrue("Existing resource file for " + version.toString() + " RAR: "
                    + verifyFileRar.toString() + " was " + resourcesRar,
                    verifyFileRar.equals(resourcesRar));
            assertTrue("Existing resource file for " + version.toString() + " WAR: "
                    + verifyFileWar.toString() + " was " + resourcesWar,
                    verifyFileWar.equals(resourcesWar));
        }
        // Delete all resource files.
        for (File prefix : prefixes) {
            final File sunResource = new File(prefix, "sun-resources.xml");
            final File gfResource = new File(prefix, "glassfish-resources.xml");
            sunResource.delete();
            gfResource.delete();
        }
    }

}
