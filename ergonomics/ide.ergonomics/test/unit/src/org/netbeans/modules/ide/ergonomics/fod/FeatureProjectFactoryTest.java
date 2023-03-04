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
package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class FeatureProjectFactoryTest {

    public FeatureProjectFactoryTest() {
    }

    @Test
    public void recognizeWildCard() throws IOException {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject no1 = prj(root, "no1", "mx.false", "anything");
        FileObject yes1 = prj(root, "yes1", "mx.true", "suite.py");
        FileObject no2 = prj(root, "no2", "mxnot", "suite.py");

        final String relative = "mx.*/suite.py";

        assertFalse(isProject(no1, relative));
        assertFalse(isProject(no2, relative));
        assertTrue(isProject(yes1, relative));
    }

    @Test
    public void recognizeParentPath() throws IOException {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject src = FileUtil.createFolder(root, "src");
        FileObject marker = FileUtil.createFolder(src, "marker");
        FileObject other = FileUtil.createFolder(src, "other");

        final String relative = "../marker";

        assertFalse(isProject(src, relative));
        assertTrue(isProject(marker, relative));
        assertTrue(isProject(other, relative));
    }

    private static FileObject prj(FileObject root, String base, String mx, String file) throws IOException {
        return root.createFolder(base).createFolder(mx).createData(file).getParent().getParent();
    }

    private static boolean isProject(FileObject dir, String relative) {
        FeatureProjectFactory.Data d = new FeatureProjectFactory.Data(dir, true);
        return d.hasFile(relative);
    }
}
