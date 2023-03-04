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
package org.netbeans.modules.masterfs;

import java.io.File;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class CreateFileOnWindowsTest extends NbTestCase {
    private final File root;
    private static FileSystem masterfs;

    public CreateFileOnWindowsTest(File root) {
        super(root.toString());
        this.root = root;
    }
    
    public static Test suite() {
        NbTestSuite ts = new NbTestSuite();
        if (Utilities.isWindows()) {
            for (File root : File.listRoots()) {
                ts.addTest(new CreateFileOnWindowsTest(root));
            }
        }
        return ts;
    }

    @Override
    protected void runTest() throws Throwable {
        if (!root.exists()) {
            return;
        }
        final File tmp = new File(root, "tmp");
        if (!tmp.mkdirs()) {
            System.err.println("Cannot create " + tmp);
            return;
        }
        if (masterfs == null) {
            final FileObject fo = FileUtil.toFileObject(root);
            if (fo == null) {
                return;
            }
            masterfs = fo.getFileSystem();
        }
        FileObject fo = FileUtil.createData(masterfs.getRoot(), root.getPath().substring(0, 2) + "/tmp/tst.txt");
        System.err.println("ok: " + fo);
        fo.delete();
    }
}
