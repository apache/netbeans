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
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.util.Map;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.common.api.Version;

/**
 *
 * @author Petr Hejl
 */
public class JerseyLibraryHelperTest extends NbTestCase {

    public JerseyLibraryHelperTest(String name) {
        super(name);
    }

    public void testMavenDepsWL1211() {
        Library impl = JerseyLibraryHelper.getJerseyLibrary(
                Version.fromDottedNotationWithFallback("12.1.1"), null);
        assertNotNull(impl);
        Map<String, String> props = impl.getProperties();
        String deps = props.get("maven-dependencies");
        assertMaven12Deps(deps);
    }

    public void testMavenDepsWL120() {
        Library impl = JerseyLibraryHelper.getJerseyLibrary(
                Version.fromDottedNotationWithFallback("12.0"), null);
        assertNotNull(impl);
        Map<String, String> props = impl.getProperties();
        String deps = props.get("maven-dependencies");
        assertMaven12Deps(deps);
    }

    public void testMavenDepsWL12() {
        Library impl = JerseyLibraryHelper.getJerseyLibrary(
                Version.fromDottedNotationWithFallback("12"), null);
        assertNotNull(impl);
        Map<String, String> props = impl.getProperties();
        String deps = props.get("maven-dependencies");
        assertMaven12Deps(deps);
    }

    public void testMavenDepsWL10() {
        Library impl = JerseyLibraryHelper.getJerseyLibrary(
                Version.fromDottedNotationWithFallback("10"), null);
        assertNull(impl);
    }

    private static void assertMaven12Deps(String deps) {
        assertTrue(deps.contains("com.sun.jersey:jersey-client:1.9:jar"));
        assertTrue(deps.contains("com.sun.jersey:jersey-json:1.9:jar"));
        assertTrue(deps.contains("com.sun.jersey.contribs:jersey-multipart:1.9:jar"));
        assertTrue(deps.contains("com.sun.jersey:jersey-server:1.9:jar"));
        assertTrue(deps.contains("org.codehaus.jackson:jackson-core-asl:1.8.3:jar"));
        assertTrue(deps.contains("org.codehaus.jackson:jackson-jaxrs:1.8.3:jar"));
        assertTrue(deps.contains("org.codehaus.jackson:jackson-mapper-asl:1.8.3:jar"));
        assertTrue(deps.contains("org.codehaus.jackson:jackson-xc:1.8.3:jar"));
        assertTrue(deps.contains("org.codehaus.jettison:jettison:1.1:jar"));
    }
}
