/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.source.classpath;

import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.ClassIndexTestCase;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class CacheSourceForBinaryQueryImplTest extends ClassIndexTestCase {
    
    FileObject[] srcRoots;
    ClasspathInfo cpInfo;
    CacheSourceForBinaryQueryImpl sfbq;

    public CacheSourceForBinaryQueryImplTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        this.clearWorkDir();
        File fwd = this.getWorkDir();
        FileObject wd = FileUtil.toFileObject(fwd);
        assertNotNull(wd);
        File cacheFolder = new File (fwd,"cache");  //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder (cacheFolder);
        this.srcRoots = new FileObject [2];
        this.srcRoots[0] = wd.createFolder("src1"); //NOI18N
        this.srcRoots[1] = wd.createFolder("src2"); //NOI18N
        ClassPath bootPath = ClassPathSupport.createClassPath(new URL[0]);
        ClassPath compilePath = ClassPathSupport.createClassPath(new URL[0]);
        ClassPath srcPath = ClassPathSupport.createClassPath(srcRoots);
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        gpr.register(ClassPath.SOURCE, new ClassPath[]{srcPath});
        gpr.register(ClassPath.BOOT, new ClassPath[] {bootPath});
        gpr.register(ClassPath.COMPILE, new ClassPath[] {compilePath});

        beginTx();

        this.cpInfo = ClasspathInfoAccessor.getINSTANCE().create(bootPath,ClassPath.EMPTY,compilePath,ClassPath.EMPTY,ClassPath.EMPTY,srcPath,ClassPath.EMPTY,null,true,false,false,false,null);
        this.sfbq = new CacheSourceForBinaryQueryImpl ();
    }

    protected @Override void tearDown() throws Exception {
        this.cpInfo = null;
    }

    public void testFindSourceRoots() throws Exception {
        ClassPath outCp = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(this.cpInfo,ClasspathInfo.PathKind.OUTPUT);
        assertNotNull(outCp);
        assertEquals(srcRoots.length,outCp.entries().size());
        Iterator<ClassPath.Entry> it = ((List<ClassPath.Entry>)outCp.entries()).iterator();
        for (int i=0; it.hasNext(); i++) {
            ClassPath.Entry entry = it.next();
            URL url = entry.getURL();
            SourceForBinaryQuery.Result result = this.sfbq.findSourceRoots(url);
            FileObject[] sourceRoots = result.getRoots();
            assertNotNull(sourceRoots);
            assertEquals(1,sourceRoots.length);
            assertEquals(srcRoots[i],sourceRoots[0]);
        }
    }

    public void testFindSourceRootsWithAptRoot() throws Exception {
        //Force Apt Source cache creation
        final FileObject[] aptSrcRoots = new FileObject[srcRoots.length];
        for (int i=0; i<srcRoots.length; i++) {
            aptSrcRoots[i]=URLMapper.findFileObject(AptCacheForSourceQuery.getAptFolder(srcRoots[i].getURL()));
        }
        ClassPath outCp = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(this.cpInfo,ClasspathInfo.PathKind.OUTPUT);
        assertNotNull(outCp);
        assertEquals(srcRoots.length,outCp.entries().size());
        Iterator<ClassPath.Entry> it = ((List<ClassPath.Entry>)outCp.entries()).iterator();
        for (int i=0; it.hasNext(); i++) {
            ClassPath.Entry entry = it.next();
            URL url = entry.getURL();
            SourceForBinaryQuery.Result result = this.sfbq.findSourceRoots(url);
            FileObject[] sourceRoots = result.getRoots();
            assertNotNull(sourceRoots);
            assertEquals(2,sourceRoots.length);
            assertEquals(srcRoots[i],sourceRoots[0]);
            assertEquals(aptSrcRoots[i], sourceRoots[1]);
        }
    }
}
