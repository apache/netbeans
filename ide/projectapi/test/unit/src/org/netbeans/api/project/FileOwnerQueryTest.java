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

package org.netbeans.api.project;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectapi.nb.NbProjectManagerAccessor;
import org.netbeans.modules.projectapi.nb.TimedWeakReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of FileOwnerQuery.
 * @author Jesse Glick
 */
public class FileOwnerQueryTest extends NbTestCase {
    
    public FileOwnerQueryTest(String name) {
        super(name);
    }
    
    static {
        TimedWeakReference.TIMEOUT = 0;
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject randomfile;
    private FileObject projfile;
    private FileObject projfile2;
    private FileObject subprojdir;
    private FileObject subprojfile;
    private FileObject hashedFile;
    private Project p;
    private FileObject zippedfile;
    
    protected @Override void setUp() throws Exception {
        MockLookup.setLayersAndInstances(TestUtil.testProjectFactory());
        NbProjectManagerAccessor.reset();
        FileOwnerQuery.reset();
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("my-project");
        projdir.createFolder("testproject");
        randomfile = scratch.createData("randomfile");
        projfile = projdir.createData("projfile");
        FileObject projsubdir = projdir.createFolder("projsubdir");
        projfile2 = projsubdir.createData("projfile2");
        subprojdir = projdir.createFolder("subproject");
        subprojdir.createFolder("testproject");
        subprojfile = subprojdir.createData("subprojfile");
        scratch.createFolder("external1").createFolder("subdir").createData("file");
        scratch.createFolder("external2").createFolder("subdir").createData("file");
        scratch.createFolder("external3").createFolder("subproject").createFolder("testproject");
        p = ProjectManager.getDefault().findProject(projdir);
        assertNotNull("found a project successfully", p);
        
        // make jar:file:/.../projdir/foo.jar!/zipfile/zippedfile
        FileObject foojar = TestFileUtils.writeZipFile(projdir, "foo.jar", "zipdir/zippedfile:");
        FileObject foojarRoot = FileUtil.getArchiveRoot(foojar);
        assertNotNull("have an archive in " + foojar, foojarRoot);
        zippedfile = foojarRoot.getFileObject("zipdir/zippedfile");
        assertNotNull("zippedfile found in it", zippedfile);
        
        hashedFile = TestFileUtils.writeZipFile(projdir, ".#webapp.jar.1.45", "zipdir/zippedfile:");
        foojarRoot = FileUtil.getArchiveRoot(hashedFile);
        assertNotNull("have an archive in " + hashedFile, foojarRoot);
        hashedFile = foojarRoot.getFileObject("zipdir/zippedfile");

    }
    
    protected @Override void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        randomfile = null;
        projfile = null;
        p = null;
    }
    
    public void testFileOwner() throws Exception {
        assertEquals("correct project from projfile FileObject", p, FileOwnerQuery.getOwner(projfile));
        URI u = Utilities.toURI(FileUtil.toFile(projfile));
        assertEquals("correct project from projfile URI " + u, p, FileOwnerQuery.getOwner(u));
        assertEquals("correct project from projfile2 FileObject", p, FileOwnerQuery.getOwner(projfile2));
        assertEquals("correct project from projfile2 URI", p, FileOwnerQuery.getOwner(Utilities.toURI(FileUtil.toFile(projfile2))));
        assertEquals("correct project from projdir FileObject", p, FileOwnerQuery.getOwner(projdir));
        assertEquals("correct project from projdir URI", p, FileOwnerQuery.getOwner(Utilities.toURI(FileUtil.toFile(projdir))));
        // Check that it loads the project even though we have not touched it yet:
        Project p2 = FileOwnerQuery.getOwner(subprojfile);
        Project subproj = ProjectManager.getDefault().findProject(subprojdir);
        assertEquals("correct project from subprojdir FileObject", subproj, p2);
        assertEquals("correct project from subprojdir URI", subproj, FileOwnerQuery.getOwner(Utilities.toURI(FileUtil.toFile(subprojdir))));
        assertEquals("correct project from subprojfile FileObject", subproj, FileOwnerQuery.getOwner(subprojfile));
        assertEquals("correct project from subprojfile URI", subproj, FileOwnerQuery.getOwner(Utilities.toURI(FileUtil.toFile(subprojfile))));
        assertEquals("no project from randomfile FileObject", null, FileOwnerQuery.getOwner(randomfile));
        assertEquals("no project from randomfile URI", null, FileOwnerQuery.getOwner(Utilities.toURI(FileUtil.toFile(randomfile))));
        assertEquals("no project in C:\\", null, FileOwnerQuery.getOwner(URI.create("file:/C:/")));
    }
    
    public void testJarOwners() throws Exception {
        assertEquals("correct owner of a ZIPped file", p, FileOwnerQuery.getOwner(zippedfile));
        assertEquals("correct owner of a ZIPped file URL", p, FileOwnerQuery.getOwner(URI.create(zippedfile.toURL().toExternalForm())));
        assertEquals("correct owner of a ZIPped file", p, FileOwnerQuery.getOwner(hashedFile));
        assertEquals("correct owner of a ZIPped file URL", p, FileOwnerQuery.getOwner(URI.create(hashedFile.toURL().toExternalForm())));
    }
    
    public void testExternalOwner() throws Exception {
        FileObject ext1 = scratch.getFileObject("external1");
        FileObject extfile1 = ext1.getFileObject("subdir/file");
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(extfile1));
        FileOwnerQuery.markExternalOwner(ext1, p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", p, FileOwnerQuery.getOwner(extfile1));
        assertEquals("even for the projdir", p, FileOwnerQuery.getOwner(ext1));
        assertEquals("but not for something else", null, FileOwnerQuery.getOwner(scratch));
        FileObject ext2 = scratch.getFileObject("external2");
        FileObject extfile2 = ext2.getFileObject("subdir/file");
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(extfile2));
        Project p2 = ProjectManager.getDefault().findProject(subprojdir);
        FileOwnerQuery.markExternalOwner(ext2, p2, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", p2, FileOwnerQuery.getOwner(extfile2));
        assertEquals("even for the projdir", p2, FileOwnerQuery.getOwner(ext2));
        assertEquals("but not for something else", null, FileOwnerQuery.getOwner(scratch));
        assertEquals("still correct for first proj", p, FileOwnerQuery.getOwner(extfile1));
        FileObject ext3 = scratch.getFileObject("external3");
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(ext3));
        FileOwnerQuery.markExternalOwner(ext3, p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", p, FileOwnerQuery.getOwner(ext3));
        FileObject ext3subproj = ext3.getFileObject("subproject");
        Project p3 = FileOwnerQuery.getOwner(ext3subproj);
        assertNotSame("different project", p, p3);
        assertEquals("but subprojects are not part of it", ProjectManager.getDefault().findProject(ext3subproj), p3);
        FileOwnerQuery.markExternalOwner(ext3, null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("unmarking an owner works", null, FileOwnerQuery.getOwner(ext3));
    }

    public void testExternalOwnerFile() throws Exception {
        FileObject ext1 = scratch.getFileObject("external1");
        FileObject extfile1 = ext1.getFileObject("subdir/file");
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(extfile1));
        FileOwnerQuery.markExternalOwner(extfile1, p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", p, FileOwnerQuery.getOwner(extfile1));
        assertEquals("not for the projdir", null, FileOwnerQuery.getOwner(ext1));
        assertEquals("and not for something else", null, FileOwnerQuery.getOwner(scratch));
        FileObject ext2 = scratch.getFileObject("external2");
        FileObject extfile2 = ext2.getFileObject("subdir/file");
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(extfile2));
        Project p2 = ProjectManager.getDefault().findProject(subprojdir);
        FileOwnerQuery.markExternalOwner(extfile2, p2, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", p2, FileOwnerQuery.getOwner(extfile2));
        assertEquals("not for the projdir", null, FileOwnerQuery.getOwner(ext2));
        assertEquals("and not for something else", null, FileOwnerQuery.getOwner(scratch));
        assertEquals("still correct for first proj", p, FileOwnerQuery.getOwner(extfile1));
        
        //XXX: unmarking files.
    }
    
    public void testExternalOwnerURI() throws Exception {
        FileObject ext1 = scratch.getFileObject("external1");
        FileObject extfile1 = ext1.getFileObject("subdir/file");
        assertEquals("no owner yet through FileObjects", null, FileOwnerQuery.getOwner(extfile1));
        assertEquals("no owner yet through URI", null, FileOwnerQuery.getOwner(extfile1));
        FileOwnerQuery.markExternalOwner(ext1.toURI(), p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner through FileObjects", p, FileOwnerQuery.getOwner(extfile1));
        assertEquals("now have an owner through URI", p, FileOwnerQuery.getOwner(extfile1.toURI()));
        assertEquals("even for the projdir throught FileObjects", p, FileOwnerQuery.getOwner(ext1));
        assertEquals("even for the projdir throught URI", p, FileOwnerQuery.getOwner(ext1.toURI()));
        assertEquals("but not for something else throught FileObjects", null, FileOwnerQuery.getOwner(scratch));
        assertEquals("but not for something else throught URI", null, FileOwnerQuery.getOwner(scratch.toURI()));
        FileObject ext2 = scratch.getFileObject("external2");
        FileObject extfile2 = ext2.getFileObject("subdir/file");
        assertEquals("no owner yet through FileObjects", null, FileOwnerQuery.getOwner(extfile2));
        assertEquals("no owner yet through URI", null, FileOwnerQuery.getOwner(extfile2.toURI()));
        Project p2 = ProjectManager.getDefault().findProject(subprojdir);
        FileOwnerQuery.markExternalOwner(ext2.toURI(), p2, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner through FileObjects", p2, FileOwnerQuery.getOwner(extfile2));
        assertEquals("now have an owner through URI", p2, FileOwnerQuery.getOwner(extfile2.toURI()));
        assertEquals("even for the projdir through FileObjects", p2, FileOwnerQuery.getOwner(ext2));
        assertEquals("even for the projdir through URI", p2, FileOwnerQuery.getOwner(ext2));
        assertEquals("but not for something else through FileObjects", null, FileOwnerQuery.getOwner(scratch));
        assertEquals("but not for something else through URI", null, FileOwnerQuery.getOwner(scratch.toURI()));
        assertEquals("still correct for first proj through FileObjects", p, FileOwnerQuery.getOwner(extfile1));
        assertEquals("still correct for first proj through URI", p, FileOwnerQuery.getOwner(extfile1.toURI()));
        FileObject ext3 = scratch.getFileObject("external3");
        assertEquals("no owner yet through FileObjects", null, FileOwnerQuery.getOwner(ext3));
        assertEquals("no owner yet through URI", null, FileOwnerQuery.getOwner(ext3.toURI()));
        FileOwnerQuery.markExternalOwner(ext3.toURI(), p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner through FileObjects", p, FileOwnerQuery.getOwner(ext3));
        assertEquals("now have an owner through URI", p, FileOwnerQuery.getOwner(ext3.toURI()));
        FileObject ext3subproj = ext3.getFileObject("subproject");
        Project p3 = FileOwnerQuery.getOwner(ext3subproj);
        assertNotSame("different project", p, p3);
        assertEquals("but subprojects are not part of it", ProjectManager.getDefault().findProject(ext3subproj), p3);
        FileOwnerQuery.markExternalOwner(ext3.toURI(), null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("unmarking an owner works through FileObjects", null, FileOwnerQuery.getOwner(ext3));
        assertEquals("unmarking an owner works through URI", null, FileOwnerQuery.getOwner(ext3.toURI()));
    }
    
    public void testExternalOwnerFileURI() throws Exception {
        FileObject ext1 = scratch.getFileObject("external1");
        FileObject extfile1 = ext1.getFileObject("subdir/file");
        assertEquals("no owner yet through FileObjects", null, FileOwnerQuery.getOwner(extfile1));
        assertEquals("no owner yet through URI", null, FileOwnerQuery.getOwner(extfile1.toURI()));
        FileOwnerQuery.markExternalOwner(extfile1.toURI(), p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner through FileObjects", p, FileOwnerQuery.getOwner(extfile1));
        assertEquals("now have an owner through URI", p, FileOwnerQuery.getOwner(extfile1.toURI()));
        assertEquals("not for the projdir through FileObjects", null, FileOwnerQuery.getOwner(ext1));
        assertEquals("not for the projdir through URI", null, FileOwnerQuery.getOwner(ext1.toURI()));
        assertEquals("and not for something else through FileObjects", null, FileOwnerQuery.getOwner(scratch));
        assertEquals("and not for something else through URI", null, FileOwnerQuery.getOwner(scratch.toURI()));
        FileObject ext2 = scratch.getFileObject("external2");
        FileObject extfile2 = ext2.getFileObject("subdir/file");
        assertEquals("no owner yet through FileObjects", null, FileOwnerQuery.getOwner(extfile2));
        assertEquals("no owner yet through URI", null, FileOwnerQuery.getOwner(extfile2.toURI()));
        Project p2 = ProjectManager.getDefault().findProject(subprojdir);
        FileOwnerQuery.markExternalOwner(extfile2.toURI(), p2, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner through FileObjects", p2, FileOwnerQuery.getOwner(extfile2));
        assertEquals("now have an owner through URI", p2, FileOwnerQuery.getOwner(extfile2.toURI()));
        assertEquals("not for the projdir through FileObjects", null, FileOwnerQuery.getOwner(ext2));
        assertEquals("not for the projdir through URI", null, FileOwnerQuery.getOwner(ext2.toURI()));
        assertEquals("and not for something else through FileObjects", null, FileOwnerQuery.getOwner(scratch));
        assertEquals("and not for something else through URI", null, FileOwnerQuery.getOwner(scratch.toURI()));
        assertEquals("still correct for first proj through FileObjects", p, FileOwnerQuery.getOwner(extfile1));
        assertEquals("still correct for first proj through URI", p, FileOwnerQuery.getOwner(extfile1.toURI()));
        
        //XXX: unmarking files.
    }

    public void testExternalOwnerDisappearingProject() throws Exception {
        FileObject ext1 = scratch.getFileObject("external1");
        FileObject tempPrjMarker = FileUtil.createFolder(scratch, "tempprj/testproject");
        FileObject tempPrjDir = tempPrjMarker.getParent();
        Project tempPrj = ProjectManager.getDefault().findProject(tempPrjDir);
        assertNotNull(tempPrj);
        FileOwnerQuery.markExternalOwner(ext1.toURI(), tempPrj, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", tempPrj, FileOwnerQuery.getOwner(ext1));
        Reference<FileObject> r = new WeakReference<FileObject>(tempPrjDir);
        tempPrjMarker = tempPrjDir = null;
        tempPrj = null;
        assertGC("can be GCed", r);
        assertNotNull("still has an owner", FileOwnerQuery.getOwner(ext1));
    }
    
    public void testIsProjectDirCollectible() throws Exception {
        Project p2 = ProjectManager.getDefault().findProject(subprojdir);
        FileObject root = p2.getProjectDirectory();
        FileObject ext2 = scratch.getFileObject("external2");
        FileObject extfile2 = ext2.getFileObject("subdir/file");
        
        FileOwnerQuery.markExternalOwner(extfile2.toURI(), p2, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        
        Reference<?> p2WR = new WeakReference<Object>(p2);
        Reference<?> rootWR = new WeakReference<Object>(root);
        
        p2 = null;
        root = null;
        ext2 = null;
        extfile2 = null;
        subprojdir = null;
        subprojfile = null;
        
        assertGC("project 2 collected", p2WR);
        assertGC("project 2's project dir collected", rootWR);
    }
    
    
    /**
     * Tests the issue 60297. GC causes previosly registered extenral roots
     * for project to be released. Only one extenral root per project is kept.
     *
     */
    public void testIssue60297 () throws Exception {
        FileObject ext1 = scratch.getFileObject("external1");                
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(ext1));
        FileOwnerQuery.markExternalOwner(ext1, p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals("now have an owner", p, FileOwnerQuery.getOwner(ext1));        
        FileObject ext2 = scratch.getFileObject("external2");
        assertEquals("no owner yet", null, FileOwnerQuery.getOwner(ext2));
        FileOwnerQuery.markExternalOwner(ext2, p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        System.gc();
        assertEquals("now have an owner", p, FileOwnerQuery.getOwner(ext2));        
        assertEquals("still correct for the first external root", p, FileOwnerQuery.getOwner(ext1));
    }
    
    public void testUnowned() throws Exception {
        FileObject subdir = projdir.createFolder("subUnowned");
        FileObject subfile = subdir.createData("subfile");
        assertEquals(p, FileOwnerQuery.getOwner(subfile));
        FileOwnerQuery.markExternalOwner(subdir, FileOwnerQuery.UNOWNED, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals(null, FileOwnerQuery.getOwner(subfile));
        FileOwnerQuery.markExternalOwner(subdir, null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        assertEquals(p, FileOwnerQuery.getOwner(subfile));
        
    }
    
    // XXX test URI usage of external owner
    // XXX test GC of roots and projects used in external ownership:
    // - the owning Project is not held strongly (just PM's soft cache)
    // - the root is not held strongly (note - FOQ won't be accurate after it is collected)
    // XXX test IAE from illegal calls to FOQ.markExternalOwner
    // XXX test an owner which is above the project directory

}
