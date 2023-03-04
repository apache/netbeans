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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;

/**
 * Tests {@link AddModuleFilter}.
 * @author Jesse Glick
 */
public class AddModuleFilterTest extends TestBase {
    
    public AddModuleFilterTest(String name) {
        super(name);
    }
    
    private AddModuleFilter filter;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        ModuleList ml = ModuleList.getModuleList(resolveEEPFile("suite1/action-project"));
        Set<ModuleDependency> deps = new HashSet<ModuleDependency>();
        for (ModuleEntry entry : ml.getAllEntries()) {
            deps.add(new ModuleDependency(entry));
        }
        filter = new AddModuleFilter(deps, "some.random.module");
    }

//    XXX: failing test, fix or delete
//    public void testSimpleMatches() throws Exception {
//        // JAR:
//        assertMatches("boot.jar", new String[] {"org.netbeans.bootstrap"});
//        // Class-Path JAR:
//        assertMatches("project-ant.jar", new String[] {"org.netbeans.modules.project.ant"});
//        // Display name:
//        assertMatches("demo library", new String[] {"org.netbeans.examples.modules.lib"});
//    }

//    XXX: failing test, fix or delete
//    public void testClassAndPackageNameMatches() throws Exception {
//        // Using binaries:
//        assertMatches("callablesys", new String[] {"org.openide.util"}); // org.openide.util.actions.CallableSystemAction
//        assertMatches("org.openide.nodes", new String[] {"org.openide.nodes"});
//        // This is an impl class, exclude it:
//        assertMatches("simplefileownerqueryimpl", new String[0]);
//        // Using sources:
//        assertMatches("libclass", new String[] {"org.netbeans.examples.modules.lib"});
//        // Impl class:
//        assertMatches("magicaction", new String[0]);
//        // Using class-path extensions:
//        assertMatches("javax.help", new String[] {"org.netbeans.modules.javahelp"});
//        // XXX test that friend APIs only match if "I" am a friend (needs API change in ModuleDependency)
//    }
    
    public void testMatchStrings() throws Exception {
        ModuleDependency dep = filter.getMatches(null, "callablesys", false).iterator().next();
        assertEquals(Collections.singleton("org.openide.util.actions.CallableSystemAction"), filter.getMatchesFor("callablesys", dep));
    }
    
    public void testMatchOrdering() throws Exception { // #71995
        List<String> matches = new ArrayList<String>();
        for (ModuleDependency dep : filter.getMatches(null, "systemaction", false)) {
            matches.add(dep.getModuleEntry().getCodeNameBase());
        }
        assertEquals(Arrays.asList(
            "org.openide.util.ui", // etc.SystemAction: matchLevel=0
            "org.netbeans.modules.editor", // etc.NbEditorUI.SystemActionPerformer: matchLevel=1
            "org.openide.loaders" // etc.FileSystemAction: matchLevel=2
        ), matches);
    }
    
    private void assertMatches(String text, String[] cnbs) {
        Set<String> matchedCNBs = new HashSet<String>();
        for (ModuleDependency dep : filter.getMatches(null, text, false)) {
            matchedCNBs.add(dep.getModuleEntry().getCodeNameBase());
        }
        assertEquals("correct matches for '" + text + "'", new HashSet<String>(Arrays.asList(cnbs)), matchedCNBs);
    }
    
}
