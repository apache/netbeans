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

package org.netbeans.spi.jumpto.file;


import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.jumpto.file.FileProviderAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class FileProviderTest extends NbTestCase {

    private FileObject root;
    private FileObject dataFile;
    private FileObject externalDataFile;

    public FileProviderTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File cwd = getWorkDir();
        root = FileUtil.createFolder(new File(cwd,"test"));     //NOI18N
        dataFile = FileUtil.createData(root, "foo/test.txt");   //NOI18N
        externalDataFile = FileUtil.createData(FileUtil.toFileObject(cwd), "external.txt");   //NOI18N
    }

    public void testFileInSourceGroup() {
        final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext("foo", SearchType.EXACT_NAME, -1, null);  //NOI18N
        final List<FileDescriptor> data = new LinkedList<FileDescriptor>();
        final String[] msg = new String[1];
        final FileProvider.Result result = FileProviderAccessor.getInstance().createResult(data, msg, ctx);
        FileProviderAccessor.getInstance().setRoot(ctx, root);
        final String testMsg = "Test Message";  //NOI18N
        result.addFile(dataFile);
        result.setMessage(testMsg);
        assertEquals(1, data.size());
        assertEquals(dataFile, data.iterator().next().getFileObject());
        assertEquals(dataFile.getNameExt(), data.iterator().next().getFileName());
        assertEquals(FileUtil.getRelativePath(root, dataFile), data.iterator().next().getOwnerPath());
        assertEquals(testMsg, msg[0]);

    }

    public void testExternalFile() {
        final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext("foo", SearchType.EXACT_NAME, -1, null);  //NOI18N
        final List<FileDescriptor> data = new LinkedList<FileDescriptor>();
        final String[] msg = new String[1];
        final FileProvider.Result result = FileProviderAccessor.getInstance().createResult(data, msg, ctx);
        FileProviderAccessor.getInstance().setRoot(ctx, root);
        final String testMsg = "Test Message";  //NOI18N
        result.addFile(externalDataFile);
        result.setMessage(testMsg);
        assertEquals(1, data.size());
        assertEquals(externalDataFile, data.iterator().next().getFileObject());
        assertEquals(externalDataFile.getNameExt(), data.iterator().next().getFileName());
        assertEquals(FileUtil.getFileDisplayName(externalDataFile), data.iterator().next().getOwnerPath());
        assertEquals(testMsg, msg[0]);
    }

    public void testCustomFileDescription() {
        final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext("foo", SearchType.EXACT_NAME, -1, null);  //NOI18N
        final List<FileDescriptor> data = new LinkedList<FileDescriptor>();
        final String[] msg = new String[1];
        final FileProvider.Result result = FileProviderAccessor.getInstance().createResult(data, msg, ctx);
        FileProviderAccessor.getInstance().setRoot(ctx, root);
        final String testMsg = "Test Message";  //NOI18N
        result.addFileDescriptor(new FileDescriptor() {
            @Override
            public String getFileName() {
                return "CUSTOM-NAME";   //NOI18N
            }

            @Override
            public String getOwnerPath() {
                return "CUSTOM-OWNER";  //NOI18N
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public String getProjectName() {
                return null;
            }

            @Override
            public Icon getProjectIcon() {
                return null;
            }

            @Override
            public void open() {
            }

            @Override
            public FileObject getFileObject() {
                return null;
            }
        });
        result.setMessage(testMsg);
        assertEquals(1, data.size());
        assertEquals("CUSTOM-NAME", data.iterator().next().getFileName());  //NOI18N
        assertEquals("CUSTOM-OWNER",data.iterator().next().getOwnerPath()); //NOI18N
        assertEquals(testMsg, msg[0]);
    }

}
