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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.test.TestFileUtils;

/**
 * Test functionality of GlobalSourceForBinaryImpl.
 *
 * @author Martin Krauskopf
 */
public class GlobalSourceForBinaryImplTest extends TestBase {
    
    // Doesn't need to be precise and/or valid. Should show what actual
    // GlobalSourceForBinaryImpl works with.
    private static final String LOADERS_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
            "<type>org.netbeans.modules.apisupport.project</type>\n" +
            "<configuration>\n" +
            "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
            "<code-name-base>org.openide.loaders</code-name-base>\n" +
            "</data>\n" +
            "</configuration>\n" +
            "</project>\n";
    
    public GlobalSourceForBinaryImplTest(String name) {
        super(name);
    }

    public void testFindSourceRootForZipWithFirstLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("");
        NbPlatform.getDefaultPlatform().addSourceRoot(FileUtil.urlForArchiveOrDir(nbSrcZip));
        
        URL loadersURL = FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(FileUtil.urlForArchiveOrDir(nbSrcZip), "openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    public void testFindSourceRootForZipWithSecondLevelDepthNbBuild() throws Exception {
        File nbSrcZip = generateNbSrcZip("netbeans-src/");
        NbPlatform.getDefaultPlatform().addSourceRoot(FileUtil.urlForArchiveOrDir(nbSrcZip));
        
        URL loadersURL = FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(FileUtil.urlForArchiveOrDir(nbSrcZip), "netbeans-src/openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    // just sanity check that exception is not thrown
    public void testBehaviourWithNonZipFile() throws Exception {
        GlobalSourceForBinaryImpl.quiet = true;
        File nbSrcZip = new File(getWorkDir(), "wrong-nbsrc.zip");
        nbSrcZip.createNewFile();
        NbPlatform.getDefaultPlatform().addSourceRoot(FileUtil.urlForArchiveOrDir(nbSrcZip));
        URL loadersURL = FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots();
    }
    
    public void testListeningToNbPlatform() throws Exception {
        NbPlatform.getDefaultPlatform(); // initBuildProperties
        File nbSrcZip = generateNbSrcZip("");
        URL loadersURL = FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(loadersURL);
        assertNotNull("got result", res);
        ResultChangeListener resultCL = new ResultChangeListener();
        res.addChangeListener(resultCL);
        assertFalse("not changed yet", resultCL.changed);
        assertEquals("non source root", 0, res.getRoots().length);
        NbPlatform.getDefaultPlatform().addSourceRoot(FileUtil.urlForArchiveOrDir(nbSrcZip));
        assertTrue("changed yet", resultCL.changed);
        assertEquals("one source root", 1, res.getRoots().length);
        URL loadersSrcURL = new URL(FileUtil.urlForArchiveOrDir(nbSrcZip), "openide/loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }
    
    public void testNewModuleLayout() throws Exception {
        File nbSrcZip = FileUtil.toFile(TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "nbsrc.zip",
                "nbbuild/nbproject/project.xml:",
                "openide.loaders/src/dummy:",
                "openide.loaders/nbproject/project.xml:" + LOADERS_XML));
        NbPlatform.getDefaultPlatform().addSourceRoot(FileUtil.urlForArchiveOrDir(nbSrcZip));
        URL loadersURL = FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(FileUtil.urlForArchiveOrDir(nbSrcZip), "openide.loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }

    public void testOldProjectXML() throws Exception { // #136679
        File nbSrcZip = FileUtil.toFile(TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "nbsrc.zip",
                "nbbuild/nbproject/project.xml:",
                "openide.loaders/src/dummy:",
                "openide.loaders/nbproject/project.xml:" + LOADERS_XML.replace("/3", "/2")));
        NbPlatform.getDefaultPlatform().addSourceRoot(FileUtil.urlForArchiveOrDir(nbSrcZip));
        URL loadersURL = FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        URL loadersSrcURL = new URL(FileUtil.urlForArchiveOrDir(nbSrcZip), "openide.loaders/src/");
        assertRoot(loadersURL, URLMapper.findFileObject(loadersSrcURL));
    }

    public void testUnrelatedJAR() throws Exception { // #201043
        File plaf = getWorkDir();
        org.openide.util.test.TestFileUtils.writeZipFile(new File(plaf, "platform/core/core.jar"));
        File mavenJar = org.openide.util.test.TestFileUtils.writeZipFile(new File(plaf, "java/maven/lib/maven-core-3.0.3.jar"));
        assertTrue(NbPlatform.isPlatformDirectory(plaf));
        assertTrue(NbPlatform.isSupportedPlatform(plaf));
        NbPlatform.addPlatform("test", plaf, "Test Plaf");
        assertNull(new GlobalSourceForBinaryImpl().findSourceRoots(FileUtil.urlForArchiveOrDir(mavenJar)));
    }

    private File generateNbSrcZip(String topLevelEntry) throws IOException {
        return FileUtil.toFile(TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "nbsrc.zip",
                topLevelEntry + "nbbuild/nbproject/project.xml:",
                topLevelEntry + "openide/loaders/src/dummy:",
                topLevelEntry + "openide/loaders/nbproject/project.xml:" + LOADERS_XML));
    }
    
    private static void assertRoot(final URL loadersURL, final FileObject loadersSrcFO) {
        assertEquals("right results for " + loadersURL,
                Collections.singletonList(loadersSrcFO),
                Arrays.asList(SourceForBinaryQuery.findSourceRoots(loadersURL).getRoots()));
    }
    
    private static final class ResultChangeListener implements ChangeListener {
        
        private boolean changed;
        
        public void stateChanged(ChangeEvent e) {
            changed = true;
        }
        
    }
    
}
