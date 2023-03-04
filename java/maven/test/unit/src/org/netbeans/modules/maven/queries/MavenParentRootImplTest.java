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
package org.netbeans.modules.maven.queries;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ParentProjectProvider;
import org.netbeans.spi.project.RootProjectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class MavenParentRootImplTest extends NbTestCase {

    public MavenParentRootImplTest(String name) {
        super(name);
    }
    
    private FileObject  rootFolder;
    private Project  rootProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String s = getName();
        int under = s.lastIndexOf('_');
        if (under == -1) {
            return;
        }
        FileObject fo = FileUtil.toFileObject(getDataDir());
        rootFolder = fo.getFileObject("parent-projects/" + s.substring(under + 1));
        rootProject = FileOwnerQuery.getOwner(rootFolder);
    }
    
    public void testProject_single() throws Exception {
        Project p = FileOwnerQuery.getOwner(rootFolder);
        assertNotNull(p);
        
        ParentProjectProvider ppp = p.getLookup().lookup(ParentProjectProvider.class);
        assertNotNull(ppp);
        
        Project parent = ppp.getPartentProject();
        assertNull(parent);
        
        
        RootProjectProvider rpp = p.getLookup().lookup(RootProjectProvider.class);
        assertNotNull(rpp);
        assertSame(p, rpp.getRootProject());
    }

    public void testProject_basic() throws Exception {
        FileObject subDir = rootFolder.getFileObject("child");
        Project p = FileOwnerQuery.getOwner(subDir);
        assertNotNull(p);
        
        ParentProjectProvider ppp = p.getLookup().lookup(ParentProjectProvider.class);
        assertNotNull(ppp);
        
        Project parent = ppp.getPartentProject();
        assertSame(rootProject, parent);
        
        
        RootProjectProvider rpp = p.getLookup().lookup(RootProjectProvider.class);
        assertNotNull(rpp);
        assertSame(rootProject, rpp.getRootProject());
    }

    public void testProject_unrelated() throws Exception {
        FileObject subDir = rootFolder.getFileObject("child");
        Project p = FileOwnerQuery.getOwner(subDir);
        assertNotNull(p);
        
        ParentProjectProvider ppp = p.getLookup().lookup(ParentProjectProvider.class);
        assertNotNull(ppp);
        
        Project parent = ppp.getPartentProject();
        assertNull(parent);
        
        
        RootProjectProvider rpp = p.getLookup().lookup(RootProjectProvider.class);
        assertNotNull(rpp);
        assertSame(p, rpp.getRootProject());
    }

    public void testProject_nested() throws Exception {
        FileObject childDir = rootFolder.getFileObject("first/child");
        
        Project p = FileOwnerQuery.getOwner(childDir);
        assertNotNull(p);
        
        ParentProjectProvider ppp = p.getLookup().lookup(ParentProjectProvider.class);
        assertNotNull(ppp);
        
        Project parent = ppp.getPartentProject();
        assertSame(rootProject, parent);
        
        
        RootProjectProvider rpp = p.getLookup().lookup(RootProjectProvider.class);
        assertNotNull(rpp);
        assertSame(rootProject, rpp.getRootProject());
    }

    public void testProject_multi() throws Exception {
        FileObject childDir = rootFolder.getFileObject("first/child");
        FileObject firstDir = rootFolder.getFileObject("first");
        
        Project p = FileOwnerQuery.getOwner(childDir);
        Project fp = FileOwnerQuery.getOwner(firstDir);
        assertNotNull(p);
        assertNotNull(fp);
        
        ParentProjectProvider ppp = p.getLookup().lookup(ParentProjectProvider.class);
        assertNotNull(ppp);
        
        Project parent = ppp.getPartentProject();
        assertSame(fp, parent);
        
        
        RootProjectProvider rpp = p.getLookup().lookup(RootProjectProvider.class);
        assertNotNull(rpp);
        assertSame(rootProject, rpp.getRootProject());
    }

    public void testProject_skipped() throws Exception {
        FileObject childDir = rootFolder.getFileObject("first/child");
        
        Project p = FileOwnerQuery.getOwner(childDir);
        assertNotNull(p);
        
        ParentProjectProvider ppp = p.getLookup().lookup(ParentProjectProvider.class);
        assertNotNull(ppp);
        
        Project parent = ppp.getPartentProject();
        assertSame(rootProject, parent);
        
        
        RootProjectProvider rpp = p.getLookup().lookup(RootProjectProvider.class);
        assertNotNull(rpp);
        assertSame(rootProject, rpp.getRootProject());
    }

}
