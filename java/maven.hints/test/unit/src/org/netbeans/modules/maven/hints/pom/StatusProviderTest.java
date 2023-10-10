/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.hints.pom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.test.MockLookup;

/**
 *
 * @author mkleint
 */
public class StatusProviderTest extends NbTestCase {

    public StatusProviderTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    //@org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
    public static class InstalledFileLocator extends DummyInstalledFileLocator {
        public InstalledFileLocator() {
            registerDestDir(new File(System.getProperty("test.netbeans.dest.dir")));
        }
    }

    public void testModelLoading() throws Exception { // #212152
        MockLookup.setLayersAndInstances(new InstalledFileLocator());
        // new File(StatusProviderTest.class.getResource(...).toURI()) may not work in all environments, e.g. testdist
        File pom = new File(getWorkDir(), "pom.xml");
        try (InputStream is = StatusProviderTest.class.getResourceAsStream("pom-with-warnings.xml");
             OutputStream os = new FileOutputStream(pom)) {
            is.transferTo(os);
        }
        String warnings = PomModelUtils.runMavenValidationImpl(pom, null).toString().replace(pom.getAbsolutePath(), "pom.xml");
        assertEquals("["
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 41, column 12, "
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-surefire-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 91, column 12, "
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-war-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 83, column 12]"
//#223562                + "[WARNING] The <reporting> section is deprecated, please move the reports to the <configuration> section of the new Maven Site Plugin. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 102, column 13, "
                // not reported since upgrade to 3.3.9 + "[WARNING] 'reporting.plugins.plugin.version' for org.apache.maven.plugins:maven-surefire-report-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 127, column 12]"
                , warnings);
    }

}
