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
package org.netbeans.modules.apisupport.project.ui.wizard.winsys;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;


public class DesignSupportTest extends NbTestCase {
    private FileObject fo;
    
    public DesignSupportTest(String n) {
        super(n);
    }
    
    @Override
    protected void setUp() throws IOException {
        FileSystem ms = FileUtil.createMemoryFileSystem();
        fo = ms.getRoot().createData("my.wsmode");
        OutputStream os = fo.getOutputStream();
        FileUtil.copy(DesignSupportTest.class.getResourceAsStream("testWsmode.xml"), os);
        os.close();
    }
    public void testReadingOfAMode() throws Exception {
        String read = DesignSupport.readMode(fo);
        if (read.contains("active-tc")) {
            fail("No active-tc:\n" + read);
        }
        if (read.indexOf("path orientation") == -1) {
            fail("<path orientation= should be there:\n" + read);
        }
    }
}
