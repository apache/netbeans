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
package org.netbeans.modules.derby;

import java.io.File;
import org.netbeans.modules.derby.DerbyOptionsTest.InstalledFileLocatorImpl;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.util.lookup.Lookups;

/**
 * Test was seperated from DerbyOptionsTest, as the test fail in combination.
 * 
 * @author abadea
 */
public class DerbyOptionsTest2 extends TestBase {

    private File userdir;
    private File externalDerby;

    public DerbyOptionsTest2(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        userdir = new File(getWorkDir(), ".netbeans");
        userdir.mkdirs();

        // create a fake installation of an external derby database
        externalDerby = new File(userdir, "derby");
        createFakeDerbyInstallation(externalDerby);
    }

    public void testDerbyLocationIsNotNullWhenBundledDerbyInstalled() throws Exception {
        // create a fake bundled derby database installation
        File bundledDerby = new File(userdir, DerbyOptions.INST_DIR);
        createFakeDerbyInstallation(bundledDerby);

        // assert the bundled derby is installed
        Lookups.executeWith(Lookups.singleton(new InstalledFileLocatorImpl(userdir)),
                new Runnable() {

                    @Override
                    public void run() {
                        String derbyLocation = DerbyOptions.getDefaultInstallLocation();
                        assertNotNull(derbyLocation);

                        DerbyOptions.getDefault().setLocation(externalDerby.getAbsolutePath());
                        assertFalse(DerbyOptions.getDefault().isLocationNull());

                        DerbyOptions.getDefault().setLocation(""); // this should set the location to the one of the bundled derby
                        assertFalse(DerbyOptions.getDefault().isLocationNull());
                        assertEquals(DerbyOptions.getDefault().getLocation(), derbyLocation);
                    }
                });

    }

}
