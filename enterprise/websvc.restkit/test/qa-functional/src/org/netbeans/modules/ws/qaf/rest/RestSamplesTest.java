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

package org.netbeans.modules.ws.qaf.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JDialog;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.xml.sax.SAXException;

/**
 * Tests for REST samples. Simply said - user must be able to only create
 * and run the particular sample, no additional steps should be needed.
 *
 * Duration of this test suite: approx. 4min
 *
 * @author lukas
 */
public class RestSamplesTest extends RestTestBase {

    public RestSamplesTest(String name) {
        super(name, Server.GLASSFISH);
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
        return Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro");
    }

    /**
     * Test HelloWorld Sample
     *
     * @throws java.io.IOException
     * @throws java.net.MalformedURLException
     * @throws org.xml.sax.SAXException
     */
    public void testHelloWorldSample() throws IOException, MalformedURLException, SAXException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/HelloWorldSampleProject");
        createProject(sampleName, getProjectType(), null);
        OutputOperator.invoke();
        deployProject(getProjectName());
        undeployProject(getProjectName());
    }

    /**
     * Test Customer Database Sample
     *
     * @throws java.io.IOException
     */
    public void testCustomerDBSample() throws IOException {
        new Thread("Close REST Resources Configuration dialog") {

            private boolean found = false;
            private static final String dlgLbl = "REST Resources Configuration";

            @Override
            public void run() {
                while (!found) {
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                    JDialog dlg = JDialogOperator.findJDialog(dlgLbl, true, true);
                    if (null != dlg) {
                        found = true;
                        new NbDialogOperator(dlg).ok();
                    }
                }
            }
        }.start();
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/CustomerDBSampleProject");
        createProject(sampleName, getProjectType(), null);
        deployProject(getProjectName());
        undeployProject(getProjectName());
    }

    /**
     * Test Customer Database on Spring Sample
     *
     * @throws java.io.IOException
     */
    public void testCustomerDBSpringSample() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/CustomerDBSpringSampleProject");
        createProject(sampleName, getProjectType(), null);
        // do not deploy - need to be fixed manually
        //deployProject(getProjectName());
    }

    /**
     * Test Message Board Sample
     *
     * @throws java.io.IOException
     */
    public void testMessageBoardSample() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.samples.resources.Bundle", "Templates/Project/Samples/Metro/MessageBoardSample");
        createProject(sampleName, getProjectType(), null);
        // close dialog about missing JUnit
        if (JDialogOperator.findJDialog("Open Project", true, true) != null) {
            new NbDialogOperator("Open Project").close();
        }
        deployProject(getProjectName());
        undeployProject(getProjectName());
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, RestSamplesTest.class,
                "testHelloWorldSample", //NOI18N
                "testCustomerDBSample", //NOI18N
                "testCustomerDBSpringSample", //NOI18N
                "testMessageBoardSample" //NOI18N
                );
    }
}
