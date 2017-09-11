/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
