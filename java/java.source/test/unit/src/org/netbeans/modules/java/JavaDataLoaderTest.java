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

package org.netbeans.modules.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author lahvac
 */
public class JavaDataLoaderTest extends NbTestCase {

    public JavaDataLoaderTest(String name) {
        super(name);
    }

    public void testMimeTypeBasedRecognition180478() throws Exception {
        clearWorkDir();
        FileUtil.refreshAll();
        
        MockServices.setServices(JavaDataLoader.class);
        FileUtil.setMIMEType("bbb", "text/x-java");

        File wd = getWorkDir();

        new FileOutputStream(new File(wd, "Test.java")).close();
        new FileOutputStream(new File(wd, "Test.bbb")).close();

        FileUtil.refreshAll();

        FileObject f = FileUtil.toFileObject(wd);
        DataFolder df = DataFolder.findFolder(f);
        DataObject[] children = df.getChildren();

        assertEquals(2, children.length);
        assertEquals(JavaDataObject.class, children[0].getClass());
        assertEquals(JavaDataObject.class, children[1].getClass());
    }

    public void XtestPerformance() throws Exception {
        MockServices.setServices(JavaDataLoader.class);
        FileUtil.setMIMEType("bbb", "text/x-java");
        recognize(1000);
        recognize(1000);
    }

    private void recognize(int count) throws IOException {
        clearWorkDir();
        FileUtil.refreshAll();

        File wd = getWorkDir();

        while (count-- > 0) {
            new FileOutputStream(new File(wd, "f" + count + ".java")).close();
            new FileOutputStream(new File(wd, "f" + count + ".bbb")).close();
        }

        long s = System.currentTimeMillis();
        FileUtil.refreshAll();

        FileObject f = FileUtil.toFileObject(wd);
        DataFolder df = DataFolder.findFolder(f);

        System.err.println("preparation took: " + (System.currentTimeMillis() - s));
        System.err.println(df.getChildren().length);
        System.err.println("recognition took:" + (System.currentTimeMillis() - s));
    }

}