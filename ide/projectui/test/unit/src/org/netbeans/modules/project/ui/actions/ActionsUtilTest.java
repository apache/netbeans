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
