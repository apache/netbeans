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

package org.netbeans.nbbuild.extlibs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DirSet;
import org.netbeans.junit.NbTestCase;

public class CreateDependenciesTest extends NbTestCase {

    public CreateDependenciesTest(String name) {
        super(name);
    }

    private File nb_all;

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        nb_all = getWorkDir();
        
        write(new File(nb_all, "lib.x/external/binaries-list"),
              "0000000000000000000000000000000000000000 x.jar\n");

        write(new File(nb_all, "lib.x/external/x-license.txt"),
              "Name: XA\n" +
              "Description: XB\n" +
              "Origin: XC\n" +
              "URL: XD\n" +
              "License: XE\n" +
              "\n" +
              "Text");

        write(new File(nb_all, "lib.y/external/binaries-list"),
              "0000000000000000000000000000000000000000 y.jar\n");

        write(new File(nb_all, "lib.y/external/y-license.txt"),
              "Name: XA2\n" +
              "Description: XB2\n" +
              "Origin: XC2\n" +
              "URL: XD2\n" +
              "License: XE2\n" +
              "Type: compile-time\n" +
              "\n" +
              "Text2");

        write(new File(nb_all, "nbbuild/licenses/names.properties"),
              "XE=Test license\n" +
              "XE2=Test license2\n");
    }

    private static void write(File f, String contents) throws IOException {
        f.getParentFile().mkdirs();

        try (OutputStream os = new FileOutputStream(f)) {
            os.write(contents.getBytes("UTF-8"));
        }
    }

    public void testDependencies() throws Exception {
        CreateDependencies d = new CreateDependencies();
        
        Project prj = new Project();
        DirSet ds = new DirSet();
        File dependencies = new File(nb_all, "dependencies");

        ds.setProject(prj);
        ds.setDir(nb_all);
        prj.addReference("x", ds);
        prj.setProperty("nb_all", nb_all.getAbsolutePath());
        d.setProject(prj);
        d.getProject();
        d.setRefid("x");
        d.setDependencies(dependencies);
        d.execute();

        assertFileContent(dependencies,
                          "This project's dependencies\n" +
                          "\n" +
                          "\n" +
                          "Runtime dependencies:\n" +
                          "=====================\n" +
                          "\n" +
                          "From: XC\n" +
                          "  - XA: XB (XD)\n" +
                          "    License: Test license\n" +
                          "\n");
    }
    
    public void testCompileTimeDependencies() throws Exception {
        CreateDependencies d = new CreateDependencies();
        
        Project prj = new Project();
        DirSet ds = new DirSet();
        File dependencies = new File(nb_all, "dependencies");

        ds.setProject(prj);
        ds.setDir(nb_all);
        prj.addReference("x", ds);
        prj.setProperty("nb_all", nb_all.getAbsolutePath());
        d.setProject(prj);
        d.getProject();
        d.setRefid("x");
        d.setDependencies(dependencies);
        d.setSourceDependencies(true);
        d.execute();

        assertFileContent(dependencies,
                          "This project's dependencies\n" +
                          "\n" +
                          "\n" +
                          "Runtime dependencies:\n" +
                          "=====================\n" +
                          "\n" +
                          "From: XC\n" +
                          "  - XA: XB (XD)\n" +
                          "    License: Test license\n" +
                          "\n" +
                          "\n" +
                          "Compile time dependencies:\n" +
                          "==========================\n" +
                          "\n" +
                          "From: XC2\n" +
                          "  - XA2: XB2 (XD2)\n" +
                          "    License: Test license2\n" +
                          "\n");
    }
    
    private void assertFileContent(File f, String content) throws Exception {
        String actual = new String(Files.readAllBytes(f.toPath()),
                                   "UTF-8");
        assertEquals(content, actual);
    }

}
