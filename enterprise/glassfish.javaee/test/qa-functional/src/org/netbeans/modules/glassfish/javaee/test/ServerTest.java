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

package org.netbeans.modules.glassfish.javaee.test;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vkraemer
 */
public class ServerTest extends NbTestCase {

    private final int SLEEP = 10000;

    public ServerTest(String testName) {
        super(testName);
    }

    public void testBogus() {

    }

    public static Test suite() {
        Configuration conf = NbModuleSuite.createConfiguration(ServerTest.class).
                addTest(StartStopServer.class, "startPreludeServer").
                addTest(ServerResourceProperties.class, "V3PreludeServerProperties").
                addTest(StartStopServer.class, "restartPreludeServer").
                addTest(StartStopServer.class, "stopPreludeServer", "startDebugPreludeServer", "stopPreludeServer").
                addTest(StartStopServer.class, "startPreludeServer").
                addTest(ServerResourceProperties.class, "VerifyV3PreludeDerbyPool").
                addTest(StartStopServer.class, "stopPreludeServer");

        conf = conf.addTest(AddRemoveV3InstanceMethods.class, "addV3Instance");
        String javaExe = System.getProperty("v3.server.javaExe");
        if (null != javaExe && javaExe.trim().length() > 0) {
            conf = conf.addTest(StartStopServer.class, "startV3Server").
                    addTest(ServerResourceProperties.class, "V3ServerProperties").
                    addTest(StartStopServer.class, "restartV3Server").
                    addTest(StartStopServer.class, "stopV3Server", "startDebugV3Server", "stopV3Server").
                    addTest(StartStopServer.class, "startV3Server").
                    addTest(ServerResourceProperties.class, "VerifyDefaultV3DerbyPool").
                    addTest(ServerResourceProperties.class, "VerifyDefaultTimerV3Resouce").
                    addTest(StartStopServer.class, "stopV3Server");
        }
        return conf.addTest(AddRemoveV3InstanceMethods.class, "removeV3Instance").
                addTest(AddRemoveV3InstanceMethods.class, "deleteJunkInstall").enableModules(".*").clusters(".*").suite();
    }
    
}
