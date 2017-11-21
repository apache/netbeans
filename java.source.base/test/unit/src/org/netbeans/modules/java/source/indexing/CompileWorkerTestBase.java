/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.CompilationUnitTree;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.CompileWorker.ParsingOutput;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.Factory;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.CancelRequest;
import org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable;
import org.netbeans.modules.parsing.impl.indexing.LogContext;
import org.netbeans.modules.parsing.impl.indexing.LogContext.EventType;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport.SuspendStatusImpl;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public abstract class CompileWorkerTestBase extends NbTestCase {
    
    public CompileWorkerTestBase(String name) {
        super(name);
    }
    
    public void test219787() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test3.java", "package test; public class Test3")),
                                           Arrays.asList(virtualCompileTuple("test/Test1.virtual", "package test; public class Test1 {}"),
                                                         virtualCompileTuple("test/Test2.virtual", "package test; public class Test2 {}")));
        
        assertFalse(result.lowMemory);
        assertTrue(result.success);
        
        Set<String> createdFiles = new HashSet<String>();
        
        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }
        
        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test3.sig")), createdFiles);
    }
    
    private ParsingOutput runIndexing(List<CompileTuple> files, List<CompileTuple> virtualFiles) throws Exception {
        TransactionContext txc = TransactionContext.beginStandardTransaction(src.toURL(), true, false, false);
        Factory f = new JavaCustomIndexer.Factory();
        Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(src.toURL()), src.toURL(), f.getIndexerName(), f.getIndexVersion(), LuceneIndexFactory.getDefault(), false, false, true, SPIAccessor.getInstance().createSuspendStatus(new SuspendStatusImpl() {
            @Override
            public boolean isSuspendSupported() {
                return true;
            }
            @Override public boolean isSuspended() {
                return false;
            }
            @Override public void parkWhileSuspended() throws InterruptedException { }
        }), new CancelRequest() {
            @Override public boolean isRaised() {
                return false;
            }
        }, LogContext.create(EventType.PATH, ""));
        
        JavaParsingContext javaContext = new JavaParsingContext(ctx, ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), ClassPath.EMPTY, ClassPathSupport.createClassPath(new FileObject[0]), ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(new FileObject[] {src}), ClassPath.EMPTY, virtualFiles);
        List<CompileTuple> toIndex = new ArrayList<CompileTuple>();
        
        toIndex.addAll(files);
        toIndex.addAll(virtualFiles);
        
        ParsingOutput result = runCompileWorker(ctx, javaContext, toIndex);
        
        txc.commit();
        
        return result;
    }
    
    protected abstract ParsingOutput runCompileWorker(Context context, JavaParsingContext javaContext, Collection<? extends CompileTuple> files) throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        clearWorkDir();
        File wdFile = getWorkDir();
        FileUtil.refreshFor(wdFile);

        FileObject wd = FileUtil.toFileObject(wdFile);
        assertNotNull(wd);
        src = FileUtil.createFolder(wd, "src");
        FileObject buildRoot = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(src, buildRoot, cache);
    }
    
    private FileObject src;
    
    private FileObject createSrcFile(String pathAndName, String content) throws Exception {
        FileObject testFile = FileUtil.createData(src, pathAndName);
        TestUtilities.copyStringToFile(testFile, content);
        
        return testFile;
    }
    
    private CompileTuple virtualCompileTuple(String relativePath, String content) throws Exception {
        FileObject file = createSrcFile(relativePath, "");
        return new CompileTuple(FileObjects.sourceFileObject(file, src, null, content), SPIAccessor.getInstance().create(new FileObjectIndexable(src, relativePath)), true, true);
    }
    
    private CompileTuple compileTuple(String relativePath, String content) throws Exception {
        FileObject file = createSrcFile(relativePath, content);
        return new CompileTuple(FileObjects.sourceFileObject(file, src), SPIAccessor.getInstance().create(new FileObjectIndexable(src, relativePath)), false, true);
    }
}
