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
package org.netbeans.modules.ws.qaf.rest.suite;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.modules.ws.qaf.rest.JEE6CRUDTest;
import org.netbeans.modules.ws.qaf.rest.JEE6FromDBTest;
import org.netbeans.modules.ws.qaf.rest.JEE6PatternsTest;
import org.netbeans.modules.ws.qaf.rest.JEE6RestCStubsTest;
import org.netbeans.modules.ws.qaf.rest.RestSamplesTest;

/**
 *
 * @author lukas
 */
public class AntJEE6Suite extends J2eeTestCase {

    public AntJEE6Suite(String name) {
        super(name);
    }

    public static Test suite() {
        // This "nicely recursive" implementation is due to limitations in J2eeTestCase API
        return addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH, emptyConfiguration(), JEE6FromDBTest.class,
                "testFromDB",
                "testDeploy",
                "testUndeploy"), JEE6CRUDTest.class,
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testDeploy", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy"), JEE6PatternsTest.class,
                "testSingletonDef", //NOI18N
                "testContainerIDef", //NOI18N
                "testCcContainerIDef", //NOI18N
                "testSingleton1", //NOI18N
                "testCcContainerI1", //NOI18N
                "testSingleton2", //NOI18N
                "testContainerI1", //NOI18N
                "testContainerI2", //NOI18N
                "testSingleton3", //NOI18N
                "testContainerI3", //NOI18N
                "testCcContainerI2", //NOI18N
                "testCcContainerI3", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy"), JEE6RestCStubsTest.class,
                "testWizard", //NOI18N
                "testCreateSimpleStubs", //NOI18N
                "testFromWADL", //NOI18N
                "testCloseProject"), RestSamplesTest.class,
                "testHelloWorldSample", //NOI18N
                "testCustomerDBSample", //NOI18N
                // sample temporarily hidden until bug 228684 is fixed
                //"testCustomerDBSpringSample", //NOI18N
                "testMessageBoardSample").enableModules(".*").clusters(".*").suite();
    }
}
