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

package test.pkg.not.in.junit;

import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

public class NbModuleSuiteMockServiceTest extends NbTestCase {

    public static Test suite() {
        NbModuleSuite.Configuration testConfig = NbModuleSuite.createConfiguration(NbModuleSuiteMockServiceTest.class);
        testConfig = testConfig.gui(false);
        return testConfig.suite();
    }

    public NbModuleSuiteMockServiceTest(String name) {
        super(name);
    }

    public void testMockService() {
        MockServices.setServices(DD.class);
        DD dd = Lookup.getDefault().lookup(DD.class);
        assertNotNull("DD found", dd);
        assertEquals("Same class", DD.class, dd.getClass());
    }

   public static class DD {


   }

}
