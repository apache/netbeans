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
package org.netbeans.modules.java.api.common.queries;

import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;


/**
 *
 * @author sdedic
 */
public class MultiModuleGroupQueryImplTest extends NbTestCase {
    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod1c;
    private FileObject mod1d;
    private FileObject mod1e;
    
    private FileObject mod2a;
    private FileObject mod2c;
    private FileObject mod2d;
    private TestProject tp;
    private ModuleTestUtilities mtu;

    public MultiModuleGroupQueryImplTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private FileObject projDir;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private Project project;
    private SourceRoots testRoots;
    private Sources src;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        
        final FileObject scratch = TestUtil.makeScratchDir(this);
        projDir = scratch.createFolder("proj"); //NOI18N
        final Project prj = TestProject.createProject(projDir, null, null);
        tp = prj.getLookup().lookup(TestProject.class);
        helper = tp.getUpdateHelper().getAntProjectHelper();
        eval = helper.getStandardPropertyEvaluator();
        project = FileOwnerQuery.getOwner(projDir);

        src1 = projDir.createFolder("src1"); //NOI18N
        assertNotNull(src1);
        src2 = projDir.createFolder("src2"); //NOI18N
        assertNotNull(src2);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        mod1c = src1.getFileObject("lib.event").createFolder("i386");   //NOI18N
        assertNotNull(mod1c);
        mod1e = src1.getFileObject("lib.event").createFolder("xxx");    //NOI18N
        assertNotNull(mod1e);
        
        
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        mod2a = src2.getFileObject("lib.discovery").createFolder("amd64");          //NOI18N
        assertNotNull(mod2a);
        mod2d = src2.createFolder("lib.event").createFolder("amd64");         //NOI18N
        assertNotNull(mod2d);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
        testRoots = mtu.newSourceRoots(false);
        src = QuerySupport.createSources(project, helper, eval, testRoots, Roots.nonSourceRoots("dist.dir")); //NOI18N
    }
    
    private SourceGroup find(SourceGroup[] groups, FileObject root, boolean fail) {
        for (SourceGroup g : groups) {
            if (g.getRootFolder().equals(root)) {
                return g;
            }
        }
        if (fail) {
            fail("Could not find group for " + root);//NOI18N
        }
        return null; // never reached.
    }
    
    public void testFindModuleInfo() throws Exception {
        assertTrue(mtu.updateModuleRoots(false, "classes:i386", src1));   //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "classes:amd64", false, src2));   //NOI18N
        testModInfoCommon();
    }

    public void testFindModuleInfoAlternatives() throws Exception {
        assertTrue(mtu.updateModuleRoots(false, "{classes,i386}", src1));   //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "{classes,amd64}", false, src2));   //NOI18N
        testModInfoCommon();
    }

    /**
     * Checks that caches are flushed after project changes
     */
    public void testFindModuleInfoTrackChanges() throws Exception {
        assertTrue(mtu.updateModuleRoots(false, "{classes,i386}", src1));   //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "{classes,amd64}", false, src2));   //NOI18N
        MultiModuleGroupQuery mq = testModInfoCommon();
        SourceGroup[] grps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup grp1c = find(grps, mod1c, true);
        assertNotNull(mq.findModuleInfo(grp1c));
        
        // change project definition:
        assertTrue(mtu.updateModuleRoots(false, "{classes,xxx}", src1));   //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "{classes,xamd64}", false, src2));   //NOI18N
        
        grps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        MultiModuleGroupQuery.Result r;

        r = mq.findModuleInfo(find(grps, mod1e, true));
        assertNotNull(r);
        assertEquals("lib.event", r.getModuleName());//NOI18N
        assertEquals("xxx", r.getPathFromModule());//NOI18N
        
        // old information was thrown away
        assertNull(mq.findModuleInfo(grp1c));
    }
    
    private MultiModuleGroupQuery testModInfoCommon() throws Exception {
        
        MultiModuleGroupQuery mq = QuerySupport.createMultiModuleGroupQuery(helper, eval, src, testRoots);
        SourceGroup[] grps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        MultiModuleGroupQuery.Result r;
        
        r = mq.findModuleInfo(find(grps, mod1a, true));
        assertNotNull(r);
        assertEquals("lib.common", r.getModuleName()); // NOI18N
        assertEquals("classes", r.getPathFromModule()); // NOI18N
        
        r = mq.findModuleInfo(find(grps, mod1b, true));
        assertNotNull(r);
        assertEquals("lib.util", r.getModuleName()); // NOI18N
        assertEquals("classes", r.getPathFromModule()); // NOI18N
        
        r = mq.findModuleInfo(find(grps, mod1c, true));
        assertNotNull(r);
        assertEquals("lib.event", r.getModuleName()); // NOI18N
        assertEquals("i386", r.getPathFromModule()); // NOI18N
        
        r = mq.findModuleInfo(find(grps, mod1d, true));
        assertNotNull(r);
        assertEquals("lib.event", r.getModuleName()); // NOI18N
        assertEquals("classes", r.getPathFromModule()); // NOI18N
        
        r = mq.findModuleInfo(find(grps, mod2a, true));
        assertNotNull(r);
        assertEquals("lib.discovery", r.getModuleName()); // NOI18N
        assertEquals("amd64", r.getPathFromModule()); // NOI18N
        return mq;
    }
    
    public void testFilterModuleGroups() throws Exception {
        assertTrue(mtu.updateModuleRoots(false, "{classes,i386}", src1));   //NOI18N
        assertTrue(mtu.updateModuleRoots(false, "{classes,amd64}", false, src2));   //NOI18N
        MultiModuleGroupQuery mq = QuerySupport.createMultiModuleGroupQuery(helper, eval, src, testRoots);
        SourceGroup[] grps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        SourceGroup[] owned = mq.filterModuleGroups("lib.event", grps); // NOI18N
        assertEquals(3, owned.length);
        find(owned, mod1c, true);
        find(owned, mod1c, true);
        find(owned, mod2d, true);
    }
    
}
