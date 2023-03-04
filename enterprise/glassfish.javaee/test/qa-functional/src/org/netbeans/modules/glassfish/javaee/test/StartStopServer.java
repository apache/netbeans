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
/**
 * @author davisn
 */

package org.netbeans.modules.glassfish.javaee.test;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
//import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;


public class StartStopServer extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public StartStopServer(String testName) {
        super(testName);
    }

     private static final String V_URL = "["+Util._V3_LOCATION+"]deployer:gfv3ee6:localhost:4848";


    public void startV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getProvider();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(V_URL);

            if(inst.isRunning())
                return;

            org.netbeans.api.server.ServerInstance si = gip.getInstances().get(0);
             ((GlassfishModule) si.getBasicNode().getLookup().lookup(GlassfishModule.class)).setEnvironmentProperty(GlassfishModule.JAVA_PLATFORM_ATTR,
                     System.getProperty("v3.server.javaExe"), true);

            ProgressUI pui = new ProgressUI("Start GlassFish V3", true);
            inst.start(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("GlassFish V3 server start failed");

            if (inst.isDebuggable(null))
                fail("Server started in debug... it should not have done that");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void stopV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getProvider();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(V_URL);

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Stop GlassFish V3", true);
            inst.stop(pui);

            Util.sleep(SLEEP);

            if(inst.isRunning())
                throw new Exception("GlassFish V3 server stop failed");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }  


    public void restartV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getProvider();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(V_URL);

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Restart GlassFish V3", true);
            inst.restart(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("GlassFish server restart failed");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void startDebugV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getProvider();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(V_URL);

            if(inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Start Debug GlassFIsh V3", true);
            inst.startDebug(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("GlassFish V3 server start debug failed");

            if (!inst.isDebuggable(null))
                fail("server isn't debuggable...");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
}
