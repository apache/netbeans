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
package org.netbeans.modules.glassfish.tooling.server;

import java.io.File;
import java.util.*;
import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandStopDAS;
import org.netbeans.modules.glassfish.tooling.admin.ResultProcess;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.StartupArgs;
import org.netbeans.modules.glassfish.tooling.utils.NetUtils;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;

/**
 * Tests for various server tasks.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ServerTasksTest extends CommonTest {

    /** GlassFish test server property file. */
    private static final String GLASSFISH_PROPERTES = "src/test/java/org/netbeans/modules/glassfish/tooling/server/GF.properties";
    
    private static Properties gfProperties;
    private static GlassFishServer gfServer;
    private static StartupArgs args;
    
    @BeforeClass
    public static void init() {
        gfProperties = readProperties(GLASSFISH_PROPERTES);
        gfServer = createGlassfishServer();
        args = new StartupArgs() {
            
            private List<String> javaArgs = Arrays.asList("-Xms128m", "-Dtest=true");
            private List<String> glassfishArgs = Arrays.asList("--domaindir " + gfServer.getDomainsFolder() + File.separator + gfServer.getDomainName(),
                    "--domain " + gfServer.getDomainName());
            private HashMap<String, String> envVars;

            @Override
            public List<String> getGlassfishArgs() {
                return glassfishArgs;
            }

            @Override
            public List<String> getJavaArgs() {
                return javaArgs;
            }

            @Override
            public Map<String, String> getEnvironmentVars() {
                return null;
            }

            @Override
            public String getJavaHome() {
                return jdkProperties().getProperty(JDKPROP_HOME);
            }
        };
    }
    
    //@Test
    public void startServerNoArgsTest() {
        ResultProcess process = ServerTasks.startServer(gfServer, new StartupArgs() {

            @Override
            public List<String> getGlassfishArgs() {
                return null;
            }

            @Override
            public List<String> getJavaArgs() {
                return null;
            }

            @Override
            public Map<String, String> getEnvironmentVars() {
                return null;
            }

            @Override
            public String getJavaHome() {
                return jdkProperties().getProperty(JDKPROP_HOME);
            }
        });
        
        assertEquals(process.getState(), TaskState.COMPLETED);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            fail();
        }
        assertTrue(ServerUtils.isHttpPortListening(
                gfServer, NetUtils.PORT_CHECK_TIMEOUT));
        CommandStopDAS.stopDAS(gfServer);
    }
    
    //@Test
    public void startServerWithArgsTest() {
        ResultProcess process = ServerTasks.startServer(gfServer, args);
        
        assertEquals(process.getState(), TaskState.COMPLETED);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            fail();
        }
        assertTrue(ServerUtils.isHttpPortListening(
                gfServer, NetUtils.PORT_CHECK_TIMEOUT));
        CommandStopDAS.stopDAS(gfServer);
    }
   
}
