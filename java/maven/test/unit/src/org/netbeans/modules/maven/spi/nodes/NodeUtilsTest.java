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

package org.netbeans.modules.maven.spi.nodes;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.test.TestFileUtils;

public class NodeUtilsTest extends NbTestCase {

    public NodeUtilsTest(String n) {
        super(n);
    }

    public void testReadOnlyLocalRepositoryFile() throws Exception {
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File f = TestFileUtils.writeFile(new File(repo, "ant/ant/1.5.1/ant-1.5.1.jar.sha1"), "a50e3e050a6e78e0656edc183435b9773f53ce78");
        FileObject rw = FileUtil.toFileObject(f);
        FileObject ro = NodeUtils.readOnlyLocalRepositoryFile(rw);
        assertNotSame(rw, ro);
        assertFalse(ro.canWrite());
        assertNotNull(DataObject.find(ro).getLookup().lookup(OpenCookie.class));
        f = TestFileUtils.writeFile(new File(repo, "ant/ant/1.5.1/ant-1.5.1.pom.sha1"), "0ffdb41f140a621beeec4dc81b3d3ecaee085d28");
        rw = FileUtil.toFileObject(f);
        ro = NodeUtils.readOnlyLocalRepositoryFile(rw);
        assertNotSame(rw, ro);
        assertFalse(ro.canWrite());
        assertNotNull(DataObject.find(ro).getLookup().lookup(OpenCookie.class));
        assertSame(ro, NodeUtils.readOnlyLocalRepositoryFile(rw));
        FileObject skip = FileUtil.toFileObject(new File(repo, "ant/ant/1.5.1"));
        assertNotNull(skip);
        assertSame(skip, NodeUtils.readOnlyLocalRepositoryFile(skip));
        File stuff = TestFileUtils.writeFile(new File(getWorkDir(), "stuff"), "stuff");
        skip = FileUtil.toFileObject(stuff);
        assertNotNull(skip);
        assertSame(skip, NodeUtils.readOnlyLocalRepositoryFile(skip));
        Reference<?> r = new WeakReference<Object>(ro.getFileSystem());
        ro = null;
        assertGC("can collect FS", r);
    }

}
