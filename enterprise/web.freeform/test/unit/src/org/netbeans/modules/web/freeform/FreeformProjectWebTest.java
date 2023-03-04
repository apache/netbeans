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

package org.netbeans.modules.web.freeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Lookup;

// XXX testAuxiliaryConfiguration
// XXX testCacheProvider
// XXX testSubprojectProvider
// XXX testLogicalViewItems
// XXX testAntArtifact
// XXX testExternalSourceRoots

/**
 * Test functionality of FreeformProject.
 * This class just tests the basic functionality found in the "jakarta" project.
 * @author Pavel Buzek
 */
public class FreeformProjectWebTest extends TestBaseWeb {
    
    public FreeformProjectWebTest (String name) {
        super(name);
    }
    
    public void testPropertyEvaluation() throws Exception {
        PropertyEvaluator eval = jakarta.evaluator();
        assertEquals("right src.home", "src", eval.getProperty("src.home"));
    }
    
    public void testProjectInformation() throws Exception {
        ProjectInformation info = ProjectUtils.getInformation(jakarta);
        assertEquals("correct name", "My_App", info.getName());
        assertEquals("same display name", "My App", info.getDisplayName());
    }
    
    public void testSources() throws Exception {
        Sources s = ProjectUtils.getSources(jakarta);
        SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("one generic group", 1, groups.length);
        assertEquals("right root folder", jakarta.getProjectDirectory(), groups[0].getRootFolder());
        assertEquals("right display name", "My App", groups[0].getDisplayName());
        groups = s.getSourceGroups("java");
        assertEquals("one Java group", 1, groups.length);
        assertEquals("right root folder #1", jakarta.getProjectDirectory().getFileObject("src"), groups[0].getRootFolder());
        assertEquals("right display name #1", "Web Module Sources", groups[0].getDisplayName());
        groups = s.getSourceGroups("doc_root");
        assertEquals("one doc root group", 1, groups.length);
        assertEquals("right root folder #1", jakarta.getProjectDirectory().getFileObject("web"), groups[0].getRootFolder());
        assertEquals("right display name #1", "Web Pages", groups[0].getDisplayName());
    }
    
}
