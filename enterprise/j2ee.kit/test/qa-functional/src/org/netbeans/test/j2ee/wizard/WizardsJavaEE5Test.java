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
package org.netbeans.test.j2ee.wizard;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author jungi, Jiri Skrivanek
 */
public class WizardsJavaEE5Test extends NewFileWizardsTest {

    /** Creates a new instance of WizardsJavaEE5Test */
    public WizardsJavaEE5Test(String testName) {
        super(testName, "5");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);  // register server
        if (isRegistered(Server.GLASSFISH)) {
            conf = conf.addTest(Suite.class);
        }
        return conf.suite();
    }

    public static class Suite extends NbTestSuite {

        public Suite() {
            super();
            addTest(new WizardsJavaEE5Test("testLocalSessionBean"));
            addTest(new WizardsJavaEE5Test("testRemoteSessionBean"));
            addTest(new WizardsJavaEE5Test("testLocalRemoteSessionBean"));
            addTest(new WizardsJavaEE5Test("testLocalStatefulSessionBean"));
            addTest(new WizardsJavaEE5Test("testRemoteStatefulSessionBean"));
            addTest(new WizardsJavaEE5Test("testLocalRemoteStatefulSessionBean"));
            addTest(new WizardsJavaEE5Test("testPersistenceUnitInEjb"));
            addTest(new WizardsJavaEE5Test("testEntityClassInEjb"));
            addTest(new WizardsJavaEE5Test("testQueueMdbBean"));
            addTest(new WizardsJavaEE5Test("testTopicMdbBean"));
            addTest(new WizardsJavaEE5Test("testServiceLocatorInEjb"));
            addTest(new WizardsJavaEE5Test("testCachingServiceLocatorInEjb"));
            addTest(new WizardsJavaEE5Test("testBuildDefaultNewEJBMod"));
            // web project
            addTest(new WizardsJavaEE5Test("testServiceLocatorInWeb"));
            addTest(new WizardsJavaEE5Test("testCachingServiceLocatorInWeb"));
            addTest(new WizardsJavaEE5Test("testPersistenceUnitInWeb"));
            addTest(new WizardsJavaEE5Test("testEntityClassInWeb"));
            addTest(new WizardsJavaEE5Test("testBuildDefaultNewWebMod"));
            addTest(new NewProjectWizardsTest("closeProjects", "5"));
        }
    }
}
