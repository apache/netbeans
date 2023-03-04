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
package org.netbeans.modules.derby.test;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.derby.api.DerbyDatabasesTest;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 */
public class TestBase extends NbTestCase {
    protected Lookup sampleDBLookup;
    
    public TestBase(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
       
        clearWorkDir();
        
        DerbyDatabasesTest.SampleDatabaseLocator sdl = new DerbyDatabasesTest.SampleDatabaseLocator();
        sampleDBLookup = new ProxyLookup(Lookup.getDefault(), Lookups.singleton(sdl));
        
        Lookups.executeWith(sampleDBLookup, new Runnable() {
            @Override
            public void run() {
                // Force initialization of JDBCDrivers
                JDBCDriverManager jdm = JDBCDriverManager.getDefault();
                JDBCDriver[] registeredDrivers = jdm.getDrivers();
            }
        });
    }

    public static void createFakeDerbyInstallation(File location) throws IOException {
        if (!location.mkdirs()) {
            throw new IOException("Could not create "
                    + location.getAbsolutePath());
        }
        File lib = new File(location, "lib");
        if (!lib.mkdir()) {
            throw new IOException("Could not create " + lib.getAbsolutePath());
        }
        new File(lib, "derby.jar").createNewFile();
        new File(lib, "derbyclient.jar").createNewFile();
    }
}
