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
package org.netbeans.modules.maven.j2ee;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.junit.Test;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;

/**
 *
 * @author Benjamin Asbach
 */
public class MavenJsfReferenceImplementationProviderTest {

    @Test
    public void testAllJsfVersionsAreMapped() {
        for (JsfVersion jsfVersion : JsfVersion.values()) {
            assertTrue(MavenJsfReferenceImplementationProvider.JSF_VERSION_MAVEN_COORDINATES_MAPPING.containsKey(jsfVersion));
        }
    }

    @Test
    public void testAllMavenCoordinatesAreWellFormatted() {
        for (String mavenCoordinates : MavenJsfReferenceImplementationProvider.JSF_VERSION_MAVEN_COORDINATES_MAPPING.values()) {
            assertEquals(3, mavenCoordinates.split(":").length);
        }
    }
}
