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
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.util.ProxyURLStreamHandlerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class FolderCopyProblemTest extends NbTestCase {

    public FolderCopyProblemTest(String name) {
        super(name);
    }
    
    public void testTheCopyProblem() throws Exception {
        clearWorkDir();
        
        URL.setURLStreamHandlerFactory(new ProxyURLStreamHandlerFactory());

        File tempFileSource = new File(getWorkDir(), "source");
        tempFileSource.mkdirs();
        File tempFile1 = File.createTempFile("test.test1", "", tempFileSource);
        tempFile1.delete();
        tempFile1.mkdir();
        File tempFile2 = File.createTempFile("test.test2", "", tempFileSource);
        tempFile2.delete();
        tempFile2.mkdir();

        File tempFileTarget = new File(getWorkDir(), "target");
        tempFileTarget.mkdir();

        FileObject sourceFo = FileUtil.toFileObject(tempFileSource);
        FileObject targetFo = FileUtil.toFileObject(tempFileTarget);

        FileUtil.copyFile(sourceFo, targetFo, "source");       

        assertNotNull(targetFo.getFileObject("source/" + tempFile1.getName()));
        assertNotNull(targetFo.getFileObject("source/" + tempFile2.getName()));
    }
}
