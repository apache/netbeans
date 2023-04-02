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
package org.netbeans.modules.glassfish.tooling.utils;

import java.io.File;
import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandHttpTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandRestTest;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;


/**
 * Test JAR file utilities.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class JarTest extends CommonTest {

    /**
     * Test Manifest reading from JAR file.
     * <p/>
     * @param server GlassFish server to be tested.
     */
    private void doTestJavaVersionCompareTo(final GlassFishServer server) {
        File jersey = ServerUtils.getJerseyCommonJarInModules(
                server.getServerHome());
        if (jersey != null) {
            Jar jar = new Jar(jersey);
            String version = jar.getBundleVersion();
            jar.close();
            assertNotNull(version);
        } else {
            fail("Jersey JAR file does not exist.");
        }
    }

    /**
     * Test Manifest reading from JAR file on Glassfish v3.
     */
    @Test
    public void testJavaVersionCompareToGFv3() {
        doTestJavaVersionCompareTo(CommandHttpTest.glassFishServer());
    }

    /**
     * Test Manifest reading from JAR file on Glassfish v4.
     */
    @Test
    public void testJavaVersionCompareToGFv4() {
        doTestJavaVersionCompareTo(CommandRestTest.glassFishServer());
    }

}
