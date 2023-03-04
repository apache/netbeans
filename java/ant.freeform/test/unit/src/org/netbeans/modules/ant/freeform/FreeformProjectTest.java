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

package org.netbeans.modules.ant.freeform;

import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

// XXX testAuxiliaryConfiguration
// XXX testCacheProvider
// XXX testAntArtifact

/**
 * Test functionality of FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class FreeformProjectTest extends TestBase {

    public FreeformProjectTest(String name) {
        super(name);
    }

    public void testProjectInformation() throws Exception {
        ProjectInformation info = ProjectUtils.getInformation(simple);
        assertEquals("correct name", "Simple_Freeform_Project", info.getName());
        assertEquals("same display name", "Simple Freeform Project", info.getDisplayName());
        // XXX test changes
    }
    
}
