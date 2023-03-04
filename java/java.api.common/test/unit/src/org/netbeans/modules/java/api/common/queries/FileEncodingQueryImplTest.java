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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 * Tests for {@link FileEncodingQueryImpl}.
 *
 * @author Tomas Mysik
 */
public class FileEncodingQueryImplTest extends NbTestCase {

    private FileObject projdir;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private Project prj;
    private static final String SOURCE_ENCODING = "source.encoding";

    public FileEncodingQueryImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        super.setUp();
        this.clearWorkDir();
        File wd = getWorkDir();
        FileObject scratch = FileUtil.toFileObject(wd);
        assertNotNull(wd);
        projdir = scratch.createFolder("proj");
        helper = ProjectGenerator.createProject(projdir, "test");
        assertNotNull(helper);
        eval = helper.getStandardPropertyEvaluator();
        assertNotNull(eval);
        prj = ProjectManager.getDefault().findProject(projdir);
        assertNotNull(prj);
    }

    public void testFileEncodingQuery() throws Exception {
        final Charset UTF_8 = StandardCharsets.UTF_8;
        final Charset ISO_8859_2 = Charset.forName("ISO-8859-2");
        
        FileObject dummy = projdir.createData("Dummy.java");
        
        setProjectCharset(UTF_8);
        FileEncodingQueryImplementation fileEncodingQuery = QuerySupport.createFileEncodingQuery(eval, SOURCE_ENCODING);
        
        Charset enc = fileEncodingQuery.getEncoding(dummy);
        assertEquals(UTF_8, enc);
        
        setProjectCharset(ISO_8859_2);
        enc = fileEncodingQuery.getEncoding(dummy);
        assertEquals(ISO_8859_2, enc);
    }

    private void setProjectCharset(final Charset charset) throws Exception {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(SOURCE_ENCODING, charset.name());
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
    }
}
