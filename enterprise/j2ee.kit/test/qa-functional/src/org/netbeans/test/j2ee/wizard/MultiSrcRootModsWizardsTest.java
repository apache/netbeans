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
import org.netbeans.test.j2ee.libraries.LibraryTest;

/**
 *
 * @author jungi, Jiri Skrivanek
 */
public class MultiSrcRootModsWizardsTest extends NewFileWizardsTest {

    /** Creates a new instance of MultiSrcRootModsWizardsTest */
    public MultiSrcRootModsWizardsTest(String s) {
        super(s, "1.4");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);//register server
        if (isRegistered(Server.GLASSFISH)) {
            conf = conf.addTest(Suite.class);
        }
        return conf.suite();
    }

    public static class Suite extends NbTestSuite {

        public Suite() {
            super();
            addTest(new MultiSrcRootModsWizardsTest("testOpenEjbMultiRootProject"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiLocalSessionBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiRemoteSessionBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiLocalRemoteSessionBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiLocalStatefulSessionBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiRemoteStatefulSessionBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiLocalRemoteStatefulSessionBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiLocalEntityBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiRemoteEntityBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiLocalRemoteEntityBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiQueueMdbBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiTopicMdbBean"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiServiceLocatorInEjb"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiCachingServiceLocatorInEjb"));
            addTest(new MultiSrcRootModsWizardsTest("testBuildEjbMultiRootProject"));

            addTest(new MultiSrcRootModsWizardsTest("testOpenWebMultiRootProject"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiServletInWeb"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiServiceLocatorInWeb"));
            addTest(new MultiSrcRootModsWizardsTest("testMultiCachingServiceLocatorInWeb"));

            addTest(new MultiSrcRootModsWizardsTest("testBuildWebMultiRootProject"));

            addTest(new LibraryTest("testDD"));
            addTest(new LibraryTest("testDDMs"));
        }
    }
}
