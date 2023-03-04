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
package org.netbeans.test.ide;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

public class AUTest extends NbTestCase {

    private static final String USERDIR_PROPERTY = "updates.userdir";

    public AUTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
    }

    public static Test suite() throws IOException {
        // disable 'slowness detection'
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "100000");
        NbTestSuite s = new NbTestSuite();
        s.addTest(
                NbModuleSuite.createConfiguration(
                AUTest.class).gui(true).clusters(".*").enableModules(".*").
                //reuseUserDir(true).
                honorAutoloadEager(true).
                addTest("testUpdatesInUserdir").
                suite());
        return s;
    }

    public void testUpdatesInUserdir() {
        String ud = System.getProperty(USERDIR_PROPERTY);
        assertNotNull("Userdir not set, please set by setting property:" + USERDIR_PROPERTY, ud);
        File userdir = new File(ud);
        assert userdir.exists() : "Userdir " + ud + " must exist!";
        File modules = new File(userdir, "modules");
        assert !modules.exists() : "\"modules\" in userdir exist and contains:" + Arrays.toString(modules.list());
        System.out.println("Test OK - no updates found in userdir");
    }
}
