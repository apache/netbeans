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

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;

/**
 * Tests for New REST from Patterns wizard
 *
 * @author lukas
 */
public class JEE6PatternsTest extends PatternsTest {

    protected static Server server = Server.GLASSFISH;

    /**
     * Def constructor.
     *
     * @param testName name of particular test case
     */
    public JEE6PatternsTest(String name) {
        super(name, server);
    }

    @Override
    protected JavaEEVersion getJavaEEversion() {
        return JavaEEVersion.JAVAEE6;
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, JEE6PatternsTest.class,
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
                "testUndeploy"); //NOI18N
    }

    static class Pkg extends JComponentByLabelFinder {

        public Pkg() {
            //Resource Package:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Package"));
        }
    }

    static class ClsName extends JComponentByLabelFinder {

        public ClsName() {
            //Class Name:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClassName"));
        }
    }

    static class Mime extends JComponentByLabelFinder {

        public Mime() {
            //MIME Type:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_MimeType"));
        }
    }

    static class RCls extends JComponentByLabelFinder {

        public RCls() {
            //Representation Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RepresentationClass"));
        }
    }

    static class Path extends JComponentByLabelFinder {

        public Path() {
            //Path:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_UriTemplate"));
        }
    }

    static class CClsName extends JComponentByLabelFinder {

        public CClsName() {
            //Container Class Name:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerClass"));
        }
    }

    static class CPath extends JComponentByLabelFinder {

        public CPath() {
            //Container Path:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerUriTemplate"));
        }
    }

    static class CRCls extends JComponentByLabelFinder {

        public CRCls() {
            //Container Representation Class:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ContainerRepresentationClass"));
        }
    }

    static class Loc extends JComponentByLabelFinder {

        public Loc() {
            //Location:
            super(Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_SrcLocation"));
        }
    }
}
