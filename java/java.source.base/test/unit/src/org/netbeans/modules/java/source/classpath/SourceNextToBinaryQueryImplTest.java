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
package org.netbeans.modules.java.source.classpath;

import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

public final class SourceNextToBinaryQueryImplTest extends NbTestCase {
    public SourceNextToBinaryQueryImplTest(String n) {
        super(n);
    }

    public void testFindSourceNextToBinary() throws Exception {
        clearWorkDir();
        FileObject root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("Root for testing found", root);
        FileObject dir = root.createFolder("testFolder");
        FileObject jar = dir.createData("junit-4.12", "jar");
        FileObject src = dir.createData("junit-4.12-sources", "jar");

        URL jarRoot = FileUtil.getArchiveRoot(URLMapper.findURL(jar, URLMapper.INTERNAL));
        SourceNextToBinaryQueryImpl instance = new SourceNextToBinaryQueryImpl();
        SourceForBinaryQuery.Result result = instance.findSourceRoots(jarRoot);
        assertNotNull("result is found", result);
        assertEquals("ONe root", 1, result.getRoots().length);
        FileObject found = FileUtil.getArchiveFile(result.getRoots()[0]);
        assertEquals("The right source file found", src, found);
    }
}
