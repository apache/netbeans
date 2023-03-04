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
import org.netbeans.modules.ws.qaf.rest.JEE6MvnCRUDTest;
import org.netbeans.modules.ws.qaf.rest.JEE6MvnFromDBTest;
import org.netbeans.modules.ws.qaf.rest.JEE6MvnPatternsTest;

/**
 * Maven Java EE 7 tests.
 *
 * @author Jiri Skrivanek
 */
public class MvnJEE7Suite extends J2eeTestCase {

    public MvnJEE7Suite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);//register server
        conf = conf.addTest(JEE7MvnFromDBTest.class,
                "testFromDB",
                "testRun",
                "testUndeploy");
        conf = conf.addTest(JEE7MvnCRUDTest.class,
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testRun", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy");
        conf = conf.addTest(JEE7MvnPatternsTest.class,
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
                "testRun", //NOI18N
                "testUndeploy");
        return conf.suite();
    }

    public static class JEE7MvnFromDBTest extends JEE6MvnFromDBTest {

        public JEE7MvnFromDBTest(String name) {
            super(name);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
    }

    public static class JEE7MvnCRUDTest extends JEE6MvnCRUDTest {

        public JEE7MvnCRUDTest(String testName) {
            super(testName);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
    }

    public static class JEE7MvnPatternsTest extends JEE6MvnPatternsTest {

        public JEE7MvnPatternsTest(String name) {
            super(name);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
        

        @Override
        protected void closeCreatedFiles(Set<File> files) {
            for (File f : files) {
                // remove annotation just to have one set of golden files for all EE levels
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
}
