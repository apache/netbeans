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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import org.netbeans.api.project.ProjectUtils;
import org.openide.modules.SpecificationVersion;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.test.MockLookup;

public class J2SESharabilityQueryTest extends NbTestCase {

    public J2SESharabilityQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject dist;
    private FileObject build;
    private ProjectManager pm;
    private J2SEProject pp;
    private AntProjectHelper helper;

    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
        dist = FileUtil.createFolder(projdir,"dist");
        build = FileUtil.createFolder(projdir,"build");
        pm = ProjectManager.getDefault();
        Project p = pm.findProject(projdir);
        assertTrue("Invalid project type",p instanceof J2SEProject);
        pp = (J2SEProject) p;
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        tests = null;
        pm = null;
        pp = null;
        helper = null;
        super.tearDown();
    }

    public void testSharability () throws Exception {
        File f = FileUtil.toFile (this.sources);
        int res = SharabilityQuery.getSharability(f);
        assertEquals("Sources must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.tests);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Tests must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (this.build);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Build can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        f = FileUtil.toFile (this.dist);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Dist can't be sharable", SharabilityQuery.NOT_SHARABLE, res);
        FileObject newSourceRoot = addSourceRoot(helper, projdir, "src2.dir",new File(FileUtil.toFile(scratch),"sources2"));
        ProjectUtils.getSources(pp).getSourceGroups(Sources.TYPE_GENERIC);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be sharable", SharabilityQuery.SHARABLE, res);
        FileObject newSourceRoot2 = changeSourceRoot(helper, projdir, "src2.dir", new File(FileUtil.toFile(scratch),"sources3"));
        f = FileUtil.toFile (newSourceRoot2);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources3 must be sharable", SharabilityQuery.SHARABLE, res);
        f = FileUtil.toFile (newSourceRoot);
        res = SharabilityQuery.getSharability(f);
        assertEquals("Sources2 must be unknown", SharabilityQuery.UNKNOWN, res);
    }

    public static FileObject addSourceRoot (AntProjectHelper helper, FileObject projdir,
                                            String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttribute("id", propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

    public static FileObject changeSourceRoot (AntProjectHelper helper, FileObject projdir,
                                               String propName, File folder) throws Exception {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folder.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        return FileUtil.toFileObject(folder);
    }

}
