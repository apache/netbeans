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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.io.OutputStream;

import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.Utilities.TestLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class SourceLevelQueryImplTest extends NbTestCase {

    public SourceLevelQueryImplTest(String name) {
        super(name);
    }

    private FileObject root;

    @Override
    protected void setUp() throws IOException {
        clearWorkDir();

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(Lookups.metaInfServices(SourceLevelQueryImplTest.class.getClassLoader()));

        root = FileUtil.createFolder(getWorkDir());
    }

    public void DISABLEDtestLegacyProject() throws IOException {
        FileObject jlObject = FileUtil.createData(root, "jdk/src/share/classes/java/lang/Object.java");
        copyString2File(jlObject, "");
        copyString2File(FileUtil.createData(root, ".jcheck/conf"), "project=jdk8\n");

        Project legacyProject = FileOwnerQuery.getOwner(root.getFileObject("jdk"));

        assertNotNull(legacyProject);

        assertEquals("1.8", SourceLevelQuery.getSourceLevel(jlObject));
    }
    
    public void testModuleInfoProject() throws IOException {
        FileObject javaBase = FileUtil.createFolder(root, "jdk/src/java.base");
        FileObject jlObject = FileUtil.createData(javaBase, "share/classes/java/lang/Object.java");
        copyString2File(jlObject, "");
        copyString2File(FileUtil.createData(javaBase, "share/classes/module-info.java"), "module java.base {}");
        copyString2File(FileUtil.createData(root, ".jcheck/conf"), "project=jdk3\n");

        Project javaBaseProject = FileOwnerQuery.getOwner(javaBase);

        assertNotNull(javaBaseProject);

        assertEquals("1.3", SourceLevelQuery.getSourceLevel(jlObject));
    }

    public void testNoJCheck() throws IOException {
        FileObject javaBase = FileUtil.createFolder(root, "jdk/src/java.base");
        FileObject jlObject = FileUtil.createData(javaBase, "share/classes/java/lang/Object.java");
        copyString2File(jlObject, "");
        copyString2File(FileUtil.createData(javaBase, "share/classes/module-info.java"), "module java.base {}");

        Project javaBaseProject = FileOwnerQuery.getOwner(javaBase);

        assertNotNull(javaBaseProject);

        assertEquals("9", SourceLevelQuery.getSourceLevel(jlObject));
    }

    private void copyString2File(FileObject file, String content) throws IOException {
        try (OutputStream out = file.getOutputStream()) {
            out.write(content.getBytes("UTF-8"));
        }
    }

    static {
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final", ""));
    }
}
