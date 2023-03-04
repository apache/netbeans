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
 * @author Lukas Jungmann
 */
public class FullWsValidation extends J2eeTestCase {

    public FullWsValidation(String name) {
        super(name);
    }

    public static Test suite() {
        // This "nicely recursive" implementation is due to limitations in J2eeTestCase API
        return  addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH, NbModuleSuite.emptyConfiguration(), WsValidation.class,
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
                    ), EjbWsValidation.class,
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
                    ), AppClientWsValidation.class,
                    "testCreateWsClient",
                    "testCallWsOperationInJavaMainClass",
                    "testCallWsOperationInJavaClass",
                    "testWsClientHandlers",
                    "testRefreshClient",
                    "testRunWsClientProject"
                    ), WsValidation.class,
                    "testUndeployProjects"
                    ), EjbWsValidation.class,
                    "testUndeployProjects"
                    ), AppClientWsValidation.class,
                    "testUndeployClientProject"
                    ), WsValidation.class,
                    "testStopServer"
                    ).enableModules(".*").clusters(".*").suite();
    }
}
