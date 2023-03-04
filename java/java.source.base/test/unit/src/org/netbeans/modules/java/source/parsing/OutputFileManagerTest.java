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
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class OutputFileManagerTest extends NbTestCase {

    private ClassPath outCp;
    private ClassPath srcCp;
    private SiblingSource sibling;
    private WriteBackTransaction wbTx;
    private OutputFileManager fm;


    public OutputFileManagerTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject src = FileUtil.createFolder(wd, "src");    //NOI18N
        final FileObject cache = FileUtil.createFolder(wd, "cache");    //NOI18N
        CacheFolder.setCacheFolder(cache);
        final File index = JavaIndex.getClassFolder(src.toURL(), false, true);
        srcCp = ClassPathSupport.createClassPath(src);
        outCp = ClassPathSupport.createClassPath(Utilities.toURI(index).toURL());
        sibling = SiblingSupport.create();
        wbTx = (WriteBackTransaction) FileManagerTransaction.writeBack(src.toURL());
        fm = new OutputFileManager(
                CachingArchiveProvider.getDefault(),
                outCp,
                srcCp,
                ClassPath.EMPTY,
                sibling.getProvider(),
                wbTx,
                null);
    }

    public void testValidClassName() throws IOException {
        assertNotNull(fm);
        final JavaFileObject fobj = fm.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, "org.netbeans.java.Test", JavaFileObject.Kind.CLASS, null);
        assertNotNull(fobj);
        try (OutputStream out = fobj.openOutputStream()) {
            out.write(new byte[]{(byte)0xca,(byte)0xfe,(byte)0xba, (byte) 0xbe});
        }
        wbTx.commit();
        FileUtil.refreshFor(FileUtil.toFile(outCp.getRoots()[0]));
        assertNotNull(outCp.findResource("org/netbeans/java/Test.sig"));
    }

    public void testInvalidClassName() throws IOException {
        assertNotNull(fm);
        final JavaFileObject fobj = fm.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, "org.netbeans.java.<any>", JavaFileObject.Kind.CLASS, null);
        assertNotNull(fobj);
        try (OutputStream out = fobj.openOutputStream()) {
            out.write(new byte[]{(byte)0xca,(byte)0xfe,(byte)0xba, (byte) 0xbe});
        }
        wbTx.commit();
        FileUtil.refreshFor(FileUtil.toFile(outCp.getRoots()[0]));
        assertNull(outCp.findResource("org/netbeans/java/<any>.sig"));
    }

    public void testInvalidPackage() throws IOException {
        assertNotNull(fm);
        final JavaFileObject fobj = fm.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, "org.netbeans.<error>.Test", JavaFileObject.Kind.CLASS, null);
        assertNotNull(fobj);
        try (OutputStream out = fobj.openOutputStream()) {
            out.write(new byte[]{(byte)0xca,(byte)0xfe,(byte)0xba, (byte) 0xbe});
        }
        wbTx.commit();
        FileUtil.refreshFor(FileUtil.toFile(outCp.getRoots()[0]));
        assertNull(outCp.findResource("org/netbeans/<error>/Test.sig"));
    }

}
