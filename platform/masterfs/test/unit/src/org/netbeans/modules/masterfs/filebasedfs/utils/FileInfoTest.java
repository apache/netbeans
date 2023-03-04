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
package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author Radek Matous
 */
public class FileInfoTest extends NbTestCase {

    public FileInfoTest(String testName) {
        super(testName);
    }

    /** Test getRoot() method. */
    public void testGetRoot() {
        if(!Utilities.isWindows()) {
            return;
        }
        String[][] files = {
            // filename, expected root
            {"\\\\computerName\\sharedFolder\\a\\b\\c\\d.txt", "\\\\computerName\\sharedFolder"},
            {"\\\\computerName\\sharedFolder", "\\\\computerName\\sharedFolder"},
            {"\\\\computerName", "\\\\computerName"},
            {"\\\\", "\\\\"},
            {"D:\\a\\b\\c\\a.txt", "D:\\"},
            {"D:\\a.txt", "D:\\"},
            {"D:\\", "D:\\"}
        };
        for (int i = 0; i < files.length; i++) {
            assertEquals("Wrong root for file "+files[i][0]+".", files[i][1], new FileInfo(new File(files[i][0])).getRoot().toString());
        }
    }

    public void testComposeName() {
        testComposeNameImpl("a.b");
        testComposeNameImpl(".b");
        testComposeNameImpl("a.");
    }

    private void testComposeNameImpl(final String fullName) {
        String ext = FileInfo.getExt(fullName);
        String name = FileInfo.getName(fullName);

        assertEquals(fullName, FileInfo.composeName(name, ext));
    }
}
