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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public final class WorkspaceParserTest extends NbTestCase {
    
    public WorkspaceParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testGetLocation() throws Exception {
        String tempFilePath = new File(System.getProperty("java.io.tmpdir"), "tmp").getAbsolutePath();
        assertRightPath(tempFilePath, tempFilePath);
        assertRightPath("URI//file:" + tempFilePath, tempFilePath);
        assertRightPath("URI//whatever:" + tempFilePath, tempFilePath); 
    }
    
    private void assertRightPath(final String rawPath, final String expectedPath) throws IOException {
        byte[] pathB = rawPath.getBytes();
        byte[] locationContent = new byte[18 + pathB.length];
        locationContent[17] = (byte) pathB.length;
        System.arraycopy(pathB, 0, locationContent, 18, pathB.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(locationContent);
        assertEquals("right path", expectedPath, WorkspaceParser.getLocation(bis).getAbsolutePath());
    }
    
    public void testParseJSFLibraryRegistryV2() throws IOException {
        FileObject fo = FileUtil.toFileObject(new File(getDataDir(), "JSFLibraryRegistryV2.xml"));
        FileObject dest = FileUtil.createFolder(new File(getWorkDir(), "wk/.metadata/.plugins/org.eclipse.jst.jsf.core/"));
        FileUtil.copyFile(fo, dest, fo.getName(), fo.getExt());
        Workspace wk = new Workspace(new File(getWorkDir(), "wk"));
        WorkspaceParser parser = new WorkspaceParser(wk);
        parser.parseJSFLibraryRegistryV2();
        List<String> libContent = wk.getUserLibraries().get("my-jsf");
        assertNotNull(libContent);
        assertEquals(6, libContent.size());
        assertEquals("/home/david/netbeans-6.1/enterprise6/modules/ext/jsf-1_2/commons-beanutils.jar", libContent.get(0));
        assertEquals("/home/david/netbeans-6.1/enterprise6/modules/ext/jsf-1_2/commons-collections.jar", libContent.get(1));
        libContent = wk.getUserLibraries().get("last-one");
        assertNotNull(libContent);
        assertEquals(1, libContent.size());
        assertEquals(new File(wk.getDirectory(), "smth/smthC.jar").getPath(), libContent.get(0));
    }
    
}
