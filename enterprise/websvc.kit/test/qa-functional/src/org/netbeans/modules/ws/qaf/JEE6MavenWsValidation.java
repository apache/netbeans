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
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author lukas
 */
public class JEE6MavenWsValidation extends MavenWsValidation {

    public JEE6MavenWsValidation(String name) {
        super(name);
    }

    @Override
    protected JavaEEVersion getJavaEEversion() {
        return JavaEEVersion.JAVAEE6;
    }

    public static Test suite() {
        return NbModuleSuite.create(addServerTests(Server.GLASSFISH, NbModuleSuite.createConfiguration(JEE6MavenWsValidation.class),
                "testCreateNewWs",
                "testAddOperation",
                "testSetSOAP",
                "testStartServer",
                "testWsHandlers",
                "testRunWsProject",
                "testTestWS",
// IZ# 175975              "testGenerateWrapper",
// IZ# 175974              "testGenerateWSDL",
//                "testRunWsProject",
                "testCreateWsClient",
                "testCallWsOperationInServlet",
                "testCallWsOperationInJSP",
                "testCallWsOperationInJavaClass",
                "testRefreshClient",
                "testWsClientHandlers",
                "testRunWsClientProject",
                "testUndeployProjects",
                "testStopServer").enableModules(".*").clusters(".*"));
    }
}
