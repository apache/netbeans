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
package org.netbeans.modules.java.file.launcher.queries;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class MultiSourceRootProviderTest extends NbTestCase {

    public MultiSourceRootProviderTest(String name) {
        super(name);
    }

    public void testFindPackage() {
        assertEquals("test.pack.nested", MultiSourceRootProvider.findPackage("/*package*/package test/**pack*/\n.pack.//package\nnested;"));
        assertEquals(null, MultiSourceRootProvider.findPackage("/*package pack*/"));
    }

    public void testSourcePathFiltering() throws Exception {
        clearWorkDir();

        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject validTest = FileUtil.createData(wd, "valid/pack/Test1.java");
        FileObject invalidTest1 = FileUtil.createData(wd, "valid/pack/Test2.java");
        FileObject invalidTest2 = FileUtil.createData(wd, "valid/pack/Test3.java");

        TestUtilities.copyStringToFile(validTest, "package valid.pack;");
        TestUtilities.copyStringToFile(invalidTest1, "package invalid.pack;");
        TestUtilities.copyStringToFile(invalidTest2, "package invalid;");

        MultiSourceRootProvider provider = new MultiSourceRootProvider();
        ClassPath valid = provider.findClassPath(validTest, ClassPath.SOURCE);

        assertNotNull(valid);
        assertEquals(1, valid.entries().size());
        assertEquals(wd, valid.getRoots()[0]);

        assertNull(provider.findClassPath(invalidTest1, ClassPath.SOURCE));
        assertNull(provider.findClassPath(invalidTest2, ClassPath.SOURCE));
    }
}
