/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
