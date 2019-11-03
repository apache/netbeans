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

package org.netbeans.modules.payara.jakartaee.test;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vkraemer
 */
public class ServerTest extends NbTestCase {

    public ServerTest(String testName) {
        super(testName);
    }

    public void testBogus() {

    }

    public static Test suite() {
        Configuration conf = NbModuleSuite.createConfiguration(ServerTest.class).
                addTest(StartStopServer.class, "startPreludeServer").
                addTest(ServerResourceProperties.class, "preludeServerProperties").
                addTest(StartStopServer.class, "restartPreludeServer").
                addTest(StartStopServer.class, "stopPreludeServer", "startDebugPreludeServer", "stopPreludeServer").
                addTest(StartStopServer.class, "startPreludeServer").
                addTest(ServerResourceProperties.class, "VerifyPreludeDerbyPool").
                addTest(StartStopServer.class, "stopPreludeServer");

        String javaExe = System.getProperty("v3.server.javaExe");
        if (null != javaExe && javaExe.trim().length() > 0) {
            conf = conf.addTest(StartStopServer.class, "startServer").
                    addTest(ServerResourceProperties.class, "serverProperties").
                    addTest(StartStopServer.class, "restartServer").
                    addTest(StartStopServer.class, "stopServer", "startDebugServer", "stopServer").
                    addTest(StartStopServer.class, "startServer").
                    addTest(ServerResourceProperties.class, "VerifyDefaultDerbyPool").
                    addTest(ServerResourceProperties.class, "VerifyDefaultTimerResouce").
                    addTest(StartStopServer.class, "stopServer");
        }
        return conf.enableModules(".*").clusters(".*").suite();
    }
    
}
