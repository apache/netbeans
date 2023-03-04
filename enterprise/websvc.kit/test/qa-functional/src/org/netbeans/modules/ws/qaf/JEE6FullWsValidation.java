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

package org.netbeans.modules.ws.qaf;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 *  Test suite for web services support in various project types in the IDE.
 *
 *  Duration of this test suite: aprox. 20min
 *
 * @author lukas.jungmann@sun.com
 */
public class JEE6FullWsValidation extends J2eeTestCase {

    public JEE6FullWsValidation(String name) {
        super(name);
    }

    public static Test suite() {
        // This "nicely recursive" implementation is due to limitations in J2eeTestCase API
        return NbModuleSuite.create(
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH, NbModuleSuite.emptyConfiguration(), JEE6WsValidation.class,
                    "testCreateNewWs",
                    "testAddOperation",
                    "testSetSOAP",
                    "testStartServer",
                    "testWsHandlers",
                    "testDeployWsProject",
                    "testTestWS",
                    "testGenerateWrapper",
                    "testGenerateWSDL",
                    "testDeployWsProject",
                    "testCreateWsClient",
                    "testCallWsOperationInServlet",
                    "testCallWsOperationInJSP",
                    "testCallWsOperationInJavaClass",
                    "testWsClientHandlers",
                    "testRefreshClient",
                    "testDeployWsClientProject"
                    ), JEE6EjbWsValidation.class,
                    "testCreateNewWs",
                    "testAddOperation",
                    "testSetSOAP",
                    "testGenerateWSDL",
                    "testWsHandlers",
                    "testDeployWsProject",
                    "testTestWS",
                    "testCreateWsClient",
                    "testCallWsOperationInSessionEJB",
                    "testCallWsOperationInJavaClass",
                    "testWsFromEJBinClientProject",
                    "testWsClientHandlers",
                    "testRefreshClientAndReplaceWSDL",
                    "testDeployWsClientProject"
                    ), JEE6AppClientWsValidation.class,
                    "testCreateWsClient",
                    "testCallWsOperationInJavaMainClass",
                    "testCallWsOperationInJavaClass",
                    "testWsClientHandlers",
                    "testRefreshClient",
                    "testRunWsClientProject"
                    ), JavaSEWsValidation.class,
                    "testCreateWsClient",
                    "testCallWsOperationInJavaMainClass",
                    "testWsClientHandlers",
                    "testRefreshClientAndReplaceWSDL",
                    "testRunWsClientProject"
                    ), JEE6WsValidation.class,
                    "testUndeployProjects"
                    ), EjbWsValidation.class,
                    "testUndeployProjects"
                    ), JEE6AppClientWsValidation.class,
                    "testUndeployClientProject"
                    ), JEE6WsValidation.class,
                    "testStopServer"
                    ).enableModules(".*").clusters(".*")
                );
    }
}
