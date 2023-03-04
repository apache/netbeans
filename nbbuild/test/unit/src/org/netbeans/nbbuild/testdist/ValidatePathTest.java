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

package org.netbeans.nbbuild.testdist;

import org.netbeans.nbbuild.testdist.ValidatePath;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.NbTestCase;

/**
 * It tests ValidatePath ant tasks
 * @author pzajac, Jesse Glick
 */
public class ValidatePathTest extends NbTestCase {

    public ValidatePathTest(String name) {
        super(name);
    }

    private ValidatePath vp;
    private Path path;

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Project prj = new Project();
        prj.setBaseDir(getWorkDir());
        path = new Path(prj);
        vp = new ValidatePath();
        vp.setPath(path);
    }

    public void testEmptyPath() throws Exception {
        vp.execute();
    }

    public void testValidFile() throws Exception {
        File f = new File(getWorkDir(),"file1");
        assertTrue("Cannot create temporary file",f.createNewFile());
        path.setPath(f.getAbsolutePath());
        vp.execute();
    }

    public void testValidPlusInvalidFile() throws Exception {
        File f = new File(getWorkDir(),"file1");
        assertTrue("Cannot create temporary file",f.createNewFile());
        File f2 = new File(getWorkDir(),"file2");
        path.setPath(f.getAbsolutePath() + ":" + f2.getAbsolutePath());
        try {
            vp.execute();
            fail("File " + f2.getPath() + " doesn't exist but task passed");
        } catch (BuildException be) {
            // ok
        }
    }

}
