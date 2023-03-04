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

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author lukas
 */
public class WebServiceSamplesTest extends WebServicesTestBase {

    private static boolean[] deployedApps = {false, false, false, false};

    public WebServiceSamplesTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return getName().substring(4);
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.SAMPLE;
    }

    @Override
    protected String getSamplesCategoryName() {
        return Bundle.getStringTrimmed("org.netbeans.modules.websvc.metro.samples.Bundle", "Templates/Project/Samples/Metro");
    }

    public void testCalculatorApp() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.metro.samples.Bundle", "Templates/Project/Samples/Metro/Calculator");
        createProject(sampleName, getProjectType(), null);
        checkMissingServer("CalculatorApp"); //NOI18N
        deployProject("CalculatorApp"); //NOI18N
        deployedApps[0] = true;
        checkMissingServer("CalculatorClientApp"); //NOI18N
        deployProject("CalculatorClientApp"); //NOI18N
        deployedApps[1] = true;
    }

    public void testSecureCalculatorApp() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.metro.samples.Bundle", "Templates/Project/Samples/Metro/SecureCalculator");
        createProject(sampleName, getProjectType(), null);
        checkMissingServer("SecureCalculatorApp"); //NOI18N
        deployProject("SecureCalculatorApp"); //NOI18N
        deployedApps[2] = true;
        checkMissingServer("SecureCalculatorClientApp"); //NOI18N
        deployProject("SecureCalculatorClientApp"); //NOI18N
        deployedApps[3] = true;
    }

    public void testUndeployAll() throws IOException {
        if (deployedApps[0]) {
            undeployProject("CalculatorApp"); //NOI18N
        }
        if (deployedApps[1]) {
            undeployProject("CalculatorClientApp"); //NOI18N
        }
        if (deployedApps[2]) {
            undeployProject("SecureCalculatorApp"); //NOI18N
        }
        if (deployedApps[3]) {
            undeployProject("SecureCalculatorClientApp"); //NOI18N
        }
    }

    public static Test suite() {
        return NbModuleSuite.create(addServerTests(Server.GLASSFISH,
                NbModuleSuite.createConfiguration(WebServiceSamplesTest.class),
                "testCalculatorApp",
                "testSecureCalculatorApp",
                "testUndeployAll").enableModules(".*").clusters(".*"));
    }
}
