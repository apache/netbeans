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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

public class FileEncodingQueryTest extends NbTestCase {

    public FileEncodingQueryTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private AntProjectHelper helper;
    private J2SEProject prj;

    @Override
    protected void setUp() throws Exception {
        ClassLoader l = this.getClass().getClassLoader();
        MockLookup.setLayersAndInstances(l, new DummyXMLEncodingImpl());
        super.setUp();
        this.clearWorkDir();
        File wd = getWorkDir();
        scratch = FileUtil.toFileObject(wd);
        assertNotNull(wd);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false);
        Project p = FileOwnerQuery.getOwner(projdir);
        assertNotNull(p);
        prj = p.getLookup().lookup(J2SEProject.class);
        assertNotNull(prj);
        sources = projdir.getFileObject("src");
    }

    public void testFileEncodingQuery () throws Exception {
        final Charset UTF8 = StandardCharsets.UTF_8;
        final Charset ISO15 = Charset.forName("ISO-8859-15");
        final Charset CP1252 = Charset.forName("CP1252");
        FileObject java = sources.createData("a.java");
        Charset enc = FileEncodingQuery.getEncoding(java);
        assertEquals(UTF8,enc);
        FileObject xml = sources.createData("b.xml");
        enc = FileEncodingQuery.getEncoding(xml);
        assertEquals(ISO15,enc);
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.SOURCE_ENCODING, CP1252.name());
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
        enc = FileEncodingQuery.getEncoding(java);
        assertEquals(CP1252,enc);
        FileObject standAloneJava = scratch.createData("b.java");
        enc = FileEncodingQuery.getEncoding(standAloneJava);
        assertEquals(Charset.defaultCharset(), enc);
    }

    public static class DummyXMLEncodingImpl extends FileEncodingQueryImplementation {

        public Charset getEncoding(FileObject file) {
            if ("xml".equals(file.getExt())) {
                return Charset.forName("ISO-8859-15");
            }
            else {
                return null;
            }
        }
    }

}
