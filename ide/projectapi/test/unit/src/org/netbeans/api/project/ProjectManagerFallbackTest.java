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
package org.netbeans.api.project;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectapi.nb.NbProjectManagerAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

public class ProjectManagerFallbackTest extends NbTestCase {

    private ProjectManager pm;
    private FileObject dir;

    public ProjectManagerFallbackTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        FileObject scratch = TestUtil.makeScratchDir(this);
        MockLookup.setInstances(TestUtil.testProjectFactory());
        pm = ProjectManager.getDefault();
        NbProjectManagerAccessor.reset();
        pm.clearNonProjectCache();
        dir = scratch.createFolder("folder");
    }

    public void testLooseJavaFileRemainsUnowned() throws Exception {
        FileObject file = dir.createData("Hello.java");
        assertNull(FileOwnerQuery.getOwner(file));
        Project fallback = pm.findProjectOrFallback(dir);
        assertSame(fallback, pm.findProject(dir));
        assertEquals("Fallback project gains ownershipt", fallback, FileOwnerQuery.getOwner(file));
    }

    public void testReplacementUnderWriteLock() throws Exception {
        Project fallback = pm.findProjectOrFallback(dir);
        Project real = ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Project>) () -> {
            dir.createFolder("testproject");
            pm.clearNonProjectCache();
            return pm.findProject(dir);
        });
        assertNotSame("Metadata must be recognized before leaving the write lock", fallback, real);
    }

    public void testReplacedFallbackIsInvalid() throws Exception {
        Project fallback = pm.findProjectOrFallback(dir);
        dir.createFolder("testproject");
        pm.clearNonProjectCache();
        assertNotSame(fallback, pm.findProject(dir));
        assertFalse("Replaced fallback must be invalid", pm.isValid(fallback));
    }

    public void testFactoryChangeUpdatesFileOwner() throws Exception {
        FileObject file = dir.createData("Hello.java");
        Project fallback = pm.findProjectOrFallback(dir);
        assertSame(fallback, FileOwnerQuery.getOwner(file));
        dir.createFolder("testproject");
        MockLookup.setInstances(TestUtil.testProjectFactory());
        Project real = pm.findProject(dir);
        assertNotSame(fallback, real);
        final Project ownerOfFle = FileOwnerQuery.getOwner(file);
        assertSame("Factory change must retire the cached fallback owner", real, ownerOfFle);
    }

    public void testNestedFallbackContainsOwnRoot() throws Exception {
        FileObject testProject = dir.createFolder("testproject");
        Project parent = pm.findProject(dir);
        FileObject child = dir.createFolder("child");
        assertSame("Owned by " + testProject + " parent", parent, FileOwnerQuery.getOwner(child));
        Project fallback = pm.findProjectOrFallback(child);
        SourceGroup group = ProjectUtils.getSources(fallback).getSourceGroups(Sources.TYPE_GENERIC)[0];
        assertTrue("Every source group must contain its own root", group.contains(child));
    }
}
