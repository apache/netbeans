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

        this.cpInfo = ClasspathInfoAccessor.getINSTANCE().create(bootPath,ClassPath.EMPTY,compilePath,ClassPath.EMPTY,ClassPath.EMPTY,srcPath,ClassPath.EMPTY,null,true,false,false,false,false,null);
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
