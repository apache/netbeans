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

package org.netbeans.modules.project.ui.actions;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.AssertionFailedError;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class ActionsUtilTest extends NbTestCase {
    
    public ActionsUtilTest(String testName) {
        super(testName);
    }
    
    private static final Object o = new Object();
    
    public void testCacheUpdatesCorrectly() throws Exception {
        Project prj1 = new DummyProject();
        Project prj2 = new DummyProject();
        TestProxyLookup projects = new TestProxyLookup(new Lookup[] {
            prj1.getLookup(),
            prj2.getLookup(),
        });
        
        Set<Project> bothProjects = new HashSet<Project>(Arrays.asList(prj1, prj2));
        Set<Project> result = new HashSet<Project>(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(bothProjects.equals(result));
        
        //make sure cache is somehow updated even after hard GC:
        //and try really hard to reclaim even (potential) SoftReferences:
        boolean wasThrown = false;
        
        try {
            assertGC("", new WeakReference<Object>(o));
        } catch (AssertionFailedError e) {
            //ignore
            wasThrown = true;
        }
        
        assertTrue(wasThrown);
        
        projects.setLookupsOverride(new Lookup[] {prj1.getLookup()});
        
        Set<Project> firstProject = new HashSet<Project>(Arrays.asList(prj1));
        
        result = new HashSet<Project>(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(firstProject.equals(result));
        
        projects.setLookupsOverride(new Lookup[] {});
        
        result = new HashSet<Project>(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(Collections.EMPTY_SET.equals(result));
        
        projects.setLookupsOverride(new Lookup[] {prj1.getLookup(), prj2.getLookup()});

        result = new HashSet<Project>(Arrays.asList(ActionsUtil.getProjectsFromLookup(projects, null)));
        
        assertTrue(bothProjects.equals(result));
    }
    
    public void testCanBeReclaimed() throws Exception {
        Project prj1 = new DummyProject();
        Project prj2 = new DummyProject();
        Lookup projects = new TestProxyLookup(new Lookup[] {
            prj1.getLookup(),
            prj2.getLookup(),
        });
        
        ActionsUtil.getProjectsFromLookup(projects, null);
        
        WeakReference<?> ref1 = new WeakReference<Object>(prj1);
        WeakReference<?> ref2 = new WeakReference<Object>(prj2);
        WeakReference<?> lookup = new WeakReference<Object>(projects);
        
        prj1 = null;
        prj2 = null;
        projects = null;
        
        assertGC("the projects can be reclaimed", ref1);
        assertGC("the projects can be reclaimed", ref2);
        assertGC("the lookup can be reclaimed", lookup);
    }
    
    public void testCanBeReclaimedWithSimpleLookup() throws Exception {
        Project prj1 = new DummyProject();
        Project prj2 = new DummyProject();
        Lookup projects = Lookups.fixed(new Object[] {
            prj1,
            prj2,
        });
        
        ActionsUtil.getProjectsFromLookup(projects, null);
        
        WeakReference<?> ref1 = new WeakReference<Object>(prj1);
        WeakReference<?> ref2 = new WeakReference<Object>(prj2);
        WeakReference<?> lookup = new WeakReference<Object>(projects);
        
        prj1 = null;
        prj2 = null;
        projects = null;
        
        assertGC("the projects can be reclaimed", ref1);
        assertGC("the projects can be reclaimed", ref2);
        assertGC("the lookup can be reclaimed", lookup);
    }
    
    private static final class TestProxyLookup extends ProxyLookup {
        
        public TestProxyLookup(Lookup[] lookups) {
            super(lookups);
        }
        
        public void setLookupsOverride(Lookup[] lookups) {
            setLookups(lookups);
        }
        
    }
    
    private static final class DummyProject implements Project {
        
        private final Lookup lookup = Lookups.singleton(this);
        
        public FileObject getProjectDirectory() {
            return null;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        
    }
    
}
