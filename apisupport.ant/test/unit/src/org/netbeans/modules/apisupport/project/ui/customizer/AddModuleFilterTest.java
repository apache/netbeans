/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
