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

package org.netbeans.api.db.explorer;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.sql.Driver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.netbeans.modules.db.test.DBTestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class JDBCDriverManagerTest extends DBTestBase {

    public JDBCDriverManagerTest(String testName) {
        super(testName);
    }

    /**
     * Tests that JDBCDriverManager manages the same instance that was
     * added using the {@link JDBCDriverManager#addDriver} method.
     */
    public void testSameInstanceAfterAdd() throws Exception {
        Util.deleteDriverFiles();
        assertEquals(0, JDBCDriverManager.getDefault().getDrivers().length);

        JDBCDriver driver = JDBCDriver.create("bar_driver", "Bar Driver", "org.bar.BarDriver", new URL[0]);
        // We are testing JDBCDriverManager.addDriver(), but that doesn't return a DataObject.
        DataObject driverDO = JDBCDriverConvertor.create(driver);

        WeakReference<DataObject> driverDORef = new WeakReference<DataObject>(driverDO);
        driverDO = null;
        for (int i = 0; i < 50; i++) {
            System.gc();
            if (driverDORef.get() == null) {
                break;
            }
        }

        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);

        // This used to fail as described in issue 75204.
        assertSame(driver, JDBCDriverManager.getDefault().getDrivers("org.bar.BarDriver")[0]);

        // this currently fails.  The driverRef isn't being GCd.  This appears to be due to changes
        // in the open ide platform.
        Util.deleteDriverFiles();
        WeakReference<JDBCDriver> driverRef = new WeakReference<JDBCDriver>(driver);
        driver = null;
        assertGC("Should be able to GC driver", driverRef);
    }

    public void testGetDriver() throws Exception {
        JDBCDriver jdbcDriver = getJDBCDriver();
        Driver driver = jdbcDriver.getDriver();

        assertNotNull(driver);
    }
}
