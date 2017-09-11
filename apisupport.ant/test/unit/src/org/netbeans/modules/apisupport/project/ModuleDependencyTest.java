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

package org.netbeans.modules.apisupport.project;

import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import java.text.Collator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NonexistentModuleEntry;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * @author Martin Krauskopf
 */
public class ModuleDependencyTest extends TestBase {
    
    public ModuleDependencyTest(String testName) {
        super(testName);
    }
    
    public void testHashCodeAndEqualsAndCompareTo() throws Exception {
        NbModuleProject module = generateStandaloneModule("module");
        ModuleList ml = module.getModuleList();
        ModuleEntry antME = ml.getEntry("org.apache.tools.ant.module");
        ModuleDependency d1 = new ModuleDependency(antME);
        ModuleDependency sameAsD1 = new ModuleDependency(antME);
        ModuleDependency alsoSameAsD1 = new ModuleDependency(antME, antME.getReleaseVersion(), antME.getSpecificationVersion(), true, false);
        ModuleDependency d2 = new ModuleDependency(antME, "0-1", null, true, false);
        ModuleDependency d3 = new ModuleDependency(antME, null, null, true, false);
        ModuleDependency d4 = new ModuleDependency(antME, antME.getReleaseVersion(), null, true, true);
        ModuleDependency d5 = new ModuleDependency(antME, antME.getReleaseVersion(), null, true, false);
        
        // test hash code and equals
        Set<ModuleDependency> set = new HashSet<ModuleDependency>();
        Set<ModuleDependency> sorted = new TreeSet<ModuleDependency>();
        set.add(d1);
        sorted.add(d1);
        assertFalse("already there", set.add(sameAsD1));
        assertFalse("already there", sorted.add(sameAsD1));
        assertFalse("already there", set.add(alsoSameAsD1));
        assertFalse("already there", sorted.add(alsoSameAsD1));
        assertTrue("is not there yet", set.add(d2));
        assertTrue("is not there yet", sorted.add(d2));
        assertTrue("is not there yet", set.add(d3));
        assertTrue("is not there yet", sorted.add(d3));
        assertTrue("is not there yet", set.add(d4));
        assertTrue("is not there yet", sorted.add(d4));
        assertTrue("is not there yet", set.add(d5));
        assertTrue("is not there yet", sorted.add(d5));
        
        ModuleDependency[] expectedOrder = new ModuleDependency[] {
            d3, d2, d5, d4, d1
        };
        Iterator<ModuleDependency> it = sorted.iterator();
        for (int i = 0; i < expectedOrder.length; i++) {
            assertSame("expected order", expectedOrder[i], it.next());
        }
        assertFalse("sanity check", it.hasNext());
    }
    
    public void testLocalizedNameComparator() throws Exception {
        NbModuleProject module = generateStandaloneModule("module");
        ModuleList ml = module.getModuleList();
        ModuleDependency[] deps = new ModuleDependency[] {
            new ModuleDependency(ml.getEntry("org.apache.tools.ant.module")),
            new ModuleDependency(ml.getEntry("org.openide.loaders")),
            new ModuleDependency(ml.getEntry("org.apache.tools.ant.module")),
            new ModuleDependency(ml.getEntry("org.openide.io")),
            new ModuleDependency(ml.getEntry("org.jdesktop.layout")),
            new ModuleDependency(ml.getEntry("org.openide.filesystems")),
            new ModuleDependency(ml.getEntry("org.openide.execution")),
        };
        
        for (int i = 0; i < deps.length; i++) {
            for (int j = 0; j < deps.length; j++) {
                int locNameResult = Collator.getInstance().compare(
                        deps[i].getModuleEntry().getLocalizedName(),
                        deps[j].getModuleEntry().getLocalizedName());
                int realResult = ModuleDependency.LOCALIZED_NAME_COMPARATOR.compare(deps[i], deps[j]);
                assertTrue("ordering works: " + deps[i] + " <--> " + deps[j],
                        locNameResult > 0 ? realResult > 0 :
                            (locNameResult == 0 ? realResult == 0 : realResult < 0));
//                (int) Math.signum(locNameResult), (int) Math.signum(realResult));
            }
        }
    }
    
    public void testSpecVersionBaseSourceEntries() throws Exception { // #72463
        SuiteProject suite = generateSuite("suite");
        NbModuleProject p = TestBase.generateSuiteComponent(suite, "module");
        ModuleList ml = ModuleList.getModuleList(p.getProjectDirectoryFile());
        ModuleEntry e = ml.getEntry("org.example.module");
        assertNotNull("have entry", e);
        ModuleDependency dep = new ModuleDependency(e);
        assertEquals("right initial spec vers from manifest", "1.0", dep.getSpecificationVersion());
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.1.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        Util.storeManifest(p.getManifestFile(), em);
        ProjectManager.getDefault().saveProject(p);
        dep = new ModuleDependency(e);
        assertEquals("right spec.version.base", "1.1", dep.getSpecificationVersion());
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.2.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        dep = new ModuleDependency(e);
        assertEquals("right modified spec.version.base", "1.2", dep.getSpecificationVersion());
        dep = new ModuleDependency(e, null, "1.0", true, false);
        assertEquals("right explicit spec vers", "1.0", dep.getSpecificationVersion());
    }
    
    public void testAppropriateDefaultCompileDependency() throws Exception { // #73666
        NbModuleProject p = generateStandaloneModule("module");
        ModuleList ml = ModuleList.getModuleList(p.getProjectDirectoryFile());
        ModuleDependency d = new ModuleDependency(ml.getEntry("org.example.module"));
        assertFalse("no public packages -> no compile dependency by default", d.hasCompileDependency());
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testBadSpecVersion() throws Exception { // #180868
        try {
            new ModuleDependency(new NonexistentModuleEntry("dep"), null, "1,2", true, false);
            fail();
        } catch (NumberFormatException x) {
            // OK
        }
    }
    
}
