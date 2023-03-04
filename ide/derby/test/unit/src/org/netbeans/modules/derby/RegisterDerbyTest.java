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
import org.netbeans.modules.derby.test.TestBase;
import org.netbeans.spi.db.explorer.DatabaseRuntime;

/**
 *
 * @author Andrei Badea
 */
public class RegisterDerbyTest extends TestBase {

    public RegisterDerbyTest(String testName) {
        super(testName);
    }

    public void testAcceptsDatabaseURL() {
        DatabaseRuntime runtime = RegisterDerby.getDefault();
        assertTrue(runtime.acceptsDatabaseURL("jdbc:derby://localhost"));
        assertTrue("Leading spaces should be ignored", runtime.acceptsDatabaseURL("   jdbc:derby://localhost"));
        assertFalse(runtime.acceptsDatabaseURL("jdbc:derby://remote"));
    }

    public void testCanStart() throws Exception {
        DatabaseRuntime runtime = RegisterDerby.getDefault();

        DerbyOptions.getDefault().setLocation("");
        
        assertTrue(DerbyOptions.getDefault().getLocation().length() == 0);
        assertFalse(runtime.canStart());

        clearWorkDir();
        File derbyLocation = new File(getWorkDir(), "derby");
        createFakeDerbyInstallation(derbyLocation);
        DerbyOptions.getDefault().setLocation(derbyLocation.getAbsolutePath());

        assertTrue(runtime.canStart());
    }
}
