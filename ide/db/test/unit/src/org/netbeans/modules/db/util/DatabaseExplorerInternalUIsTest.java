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

package org.netbeans.modules.db.util;

import java.net.URL;
import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseExplorerInternalUIsTest extends TestBase {

    private JDBCDriver driver1 = null;
    private JDBCDriver driver2 = null;

    public DatabaseExplorerInternalUIsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }
    
    private void setUpDrivers() throws Exception {
        removeDrivers();

        driver1 = JDBCDriver.create("foo_driver", "FooDriver", "org.foo.FooDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver1);
        driver2 = JDBCDriver.create("bar_driver", "BarDriver", "org.bar.BarDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver2);
        assertEquals(2, JDBCDriverManager.getDefault().getDrivers().length);
    }

    private void removeDrivers() throws Exception {
        FileObject driversFO = Util.getDriversFolder();
        FileObject[] children = driversFO.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].delete();
        }
        assertEquals(0, JDBCDriverManager.getDefault().getDrivers().length);
    }

    public void testEmptyComboboxContent() throws Exception {
        removeDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertEquals(1, combo.getItemCount());
    }

    public void testComboboxWithDrivers() throws Exception {
        setUpDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertEquals(3, combo.getItemCount());
        JdbcUrl url = (JdbcUrl)combo.getItemAt(0);
        assertDriversEqual(driver2, url.getDriver());
        assertEquals(driver2.getClassName(), url.getClassName());
        assertEquals(driver2.getDisplayName(), url.getDisplayName());
        
        url = (JdbcUrl)combo.getItemAt(1);
        assertDriversEqual(driver1, url.getDriver());
        assertEquals(driver1.getClassName(), url.getClassName());
        assertEquals(driver1.getDisplayName(), url.getDisplayName());
    }

    public void testComboboxWithDriversOfSameClass() throws Exception {
        removeDrivers();

        String name1 = "foo_driver";
        String name2 = "foo_driver2";

        String displayName1 = "FooDriver";
        String displayName2 = "FooDriver2";

        driver1 = JDBCDriver.create(name1, displayName1, "org.foo.FooDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver1);

        driver2 = JDBCDriver.create(name2, displayName2, "org.foo.FooDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver2);

        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertEquals(3, combo.getItemCount());

        JdbcUrl url = (JdbcUrl)combo.getItemAt(0);
        assertDriversEqual(driver1, url.getDriver());
        assertEquals(driver1.getClassName(), url.getClassName());
        assertEquals(driver1.getDisplayName(), url.getDisplayName());
        assertEquals(driver1.getName(), url.getName());

        url = (JdbcUrl)combo.getItemAt(1);
        assertDriversEqual(driver2, url.getDriver());
        assertEquals(driver2.getClassName(), url.getClassName());
        assertEquals(driver2.getDisplayName(), url.getDisplayName());
        assertEquals(driver2.getName(), url.getName());
    }

    private void assertDriversEqual(JDBCDriver driver1, JDBCDriver driver2) throws Exception {
        // Sometimes Lookup does not return the same driver but we end up
        // creating a new one.  So we can't be assured they are the same
        // instance
        assertEquals(driver1.getClassName(), driver2.getClassName());
        assertEquals(driver1.getDisplayName(), driver2.getDisplayName());
        assertEquals(driver1.getName(), driver2.getName());
    }

    public void testComboBoxWithDriverClass() throws Exception {
        setUpDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault(), "org.bar.BarDriver");

        assertEquals(1, combo.getItemCount());
        JdbcUrl url = (JdbcUrl)combo.getItemAt(0);
        assertDriversEqual(driver2, url.getDriver());
        assertEquals(driver2.getClassName(), url.getClassName());
        assertEquals(driver2.getDisplayName(), url.getDisplayName());
    }
}
