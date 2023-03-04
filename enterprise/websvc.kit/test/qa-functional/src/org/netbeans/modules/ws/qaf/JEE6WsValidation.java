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

/**
 * Basic validation suite for web services support in the IDE
 *
 * Duration of this test suite: aprox. 8min
 *
 * @author lukas.jungmann@sun.com
 */
public class JEE6WsValidation extends WsValidation {

    /**
     * Creates a new instance of WsValidation
     */
    public JEE6WsValidation(String name) {
        super(name);
    }

    @Override
    protected JavaEEVersion getJavaEEversion() {
        return JavaEEVersion.JAVAEE6;
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, JEE6WsValidation.class,
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
                "testRefreshClient",
                "testWsClientHandlers",
                "testDeployWsClientProject",
                "testUndeployProjects",
                "testStopServer");
    }
}
