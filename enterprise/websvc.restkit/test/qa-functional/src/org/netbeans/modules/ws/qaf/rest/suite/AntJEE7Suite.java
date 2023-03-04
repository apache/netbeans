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

import java.io.File;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.rest.JEE6CRUDTest;
import org.netbeans.modules.ws.qaf.rest.JEE6PatternsTest;
import org.netbeans.modules.ws.qaf.rest.JEE6RestCStubsTest;
import org.netbeans.modules.ws.qaf.rest.JEE7FromDBTest;

/**
 *
 * @author Jiri Skrivanek
 */
public class AntJEE7Suite extends J2eeTestCase {

    public AntJEE7Suite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);//register server
        conf = conf.addTest(JEE7FromDBTest.class,
                "testFromDB",
                "testDeploy",
                "testUndeploy");
        conf = conf.addTest(JEE7CRUDTest.class,
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testDeploy", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy");
        conf = conf.addTest(JEE7PatternsTest.class,
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
                "testUndeploy");
        conf = conf.addTest(JEE7RestCStubsTest.class,
                "testWizard", //NOI18N
                "testCreateSimpleStubs", //NOI18N
                "testFromWADL", //NOI18N
                "testCloseProject");
        return conf.suite();
    }

    public static class JEE7CRUDTest extends JEE6CRUDTest {

        public JEE7CRUDTest(String testName) {
            super(testName);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
    }

    public static class JEE7PatternsTest extends JEE6PatternsTest {

        public JEE7PatternsTest(String name) {
            super(name);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }

        @Override
        protected void closeCreatedFiles(Set<File> files) {
            for (File f : files) {
                EditorOperator eo = new EditorOperator(f.getName());
                eo.setCaretPosition("RequestScoped", true);
                eo.deleteLine(eo.getLineNumber());
                eo.setCaretPosition("@RequestScoped", true);
                eo.deleteLine(eo.getLineNumber());
                eo.save();
                eo.close();
            }
        }
    }

    public static class JEE7RestCStubsTest extends JEE6RestCStubsTest {

        public JEE7RestCStubsTest(String name) {
            super(name);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
    }
}
