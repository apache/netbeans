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
package org.netbeans.modules.java.source.indexing;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.event.ChangeListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
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
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
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
    
    public void testClassesLivingElsewhere() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test1.java", "package test; public class Test1 { Test2a t; } class Test1a { }"),
                                                         compileTuple("test/Test2.java", "package test; public class Test2 { Test1a t; } class Test2a { }")),
                                           Arrays.asList());
        
        assertFalse(result.lowMemory);
        assertTrue(result.success);
        
        Set<String> createdFiles = new HashSet<String>();
        
        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }
        
        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test1.sig",
                                                       "cache/s1/java/15/classes/test/Test1a.sig",
                                                       "cache/s1/java/15/classes/test/Test2.sig",
                                                       "cache/s1/java/15/classes/test/Test2a.sig")),
                     createdFiles);
        assertFalse(ErrorsCache.isInError(getRoot(), true));
    }

    public void testStoreAndReadParameterNames() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test; public class Test { public void test(int parameter) { } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        assertFalse(ErrorsCache.isInError(getRoot(), false));

        JavaSource js = JavaSource.forFileObject(src.getFileObject("test/Test.java"));

        js = JavaSource.create(js.getClasspathInfo());
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement clazz = cc.getElements().getTypeElement("test.Test");
                assertEquals(JavaFileObject.Kind.CLASS, ((ClassSymbol) clazz).classfile.getKind());
                assertEquals("parameter", ElementFilter.methodsIn(clazz.getEnclosedElements())
                                                       .iterator()
                                                       .next()
                                                       .getParameters()
                                                       .get(0)
                                                       .getSimpleName()
                                                       .toString());
            }
        }, true);
    }

    protected ParsingOutput runIndexing(List<CompileTuple> files, List<CompileTuple> virtualFiles) throws Exception {
        return runIndexing(files, virtualFiles, Collections.emptyList());
    }

    protected ParsingOutput runIndexing(List<CompileTuple> files, List<CompileTuple> virtualFiles, List<CompileTuple> extraSourceFiles) throws Exception {
        TransactionContext txc = TransactionContext.beginStandardTransaction(src.toURL(), true, ()->false, false);
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
        
        for (CompileTuple extra : extraSourceFiles) {
            try (OutputStream out = FileUtil.createData(extraSrc, extra.indexable.getRelativePath()).getOutputStream();
                 Writer w = new OutputStreamWriter(out)) {
                w.append(extra.jfo.getCharContent(true));
            }
        }

        ParsingOutput result = runCompileWorker(ctx, javaContext, toIndex);
        
        javaContext.finish();
        javaContext.store();
        txc.commit();
        
        return result;
    }
    
    protected abstract ParsingOutput runCompileWorker(Context context, JavaParsingContext javaContext, Collection<? extends CompileTuple> files) throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {new SourceLevelQueryImpl(), new CompilerOptionsQueryImpl()});
        
        clearWorkDir();
        File wdFile = getWorkDir();
        FileUtil.refreshFor(wdFile);

        FileObject wd = FileUtil.toFileObject(wdFile);
        assertNotNull(wd);
        src = FileUtil.createFolder(wd, "src");
        extraSrc = FileUtil.createFolder(wd, "extraSrc");
        FileObject buildRoot = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");
        ClassPath sourcePath = ClassPathSupport.createClassPath(src, extraSrc);

        SourceUtilsTestUtil.prepareTest(sourcePath, buildRoot, cache, new FileObject[0]);
    }
    
    private FileObject src;
    private FileObject extraSrc;
    private String sourceLevel;
    private List<String> compilerOptions = Collections.emptyList();
    
    private FileObject createSrcFile(String pathAndName, String content) throws Exception {
        FileObject testFile = FileUtil.createData(src, pathAndName);
        TestUtilities.copyStringToFile(testFile, content);
        
        return testFile;
    }
    
    protected CompileTuple virtualCompileTuple(String relativePath, String content) throws Exception {
        FileObject file = createSrcFile(relativePath, "");
        return new CompileTuple(FileObjects.sourceFileObject(file, src, null, content), SPIAccessor.getInstance().create(new FileObjectIndexable(src, relativePath)), true, true);
    }
    
    protected CompileTuple compileTuple(String relativePath, String content) throws Exception {
        FileObject file = createSrcFile(relativePath, content);
        return new CompileTuple(FileObjects.sourceFileObject(file, src), SPIAccessor.getInstance().create(new FileObjectIndexable(src, relativePath)), false, true);
    }
    
    protected FileObject getRoot() {
        return src;
    }

    protected void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    protected void setCompilerOptions(List<String> compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    private final class SourceLevelQueryImpl implements SourceLevelQueryImplementation2 {

        @Override
        public Result getSourceLevel(FileObject file) {
            return new Result() {
                @Override
                public String getSourceLevel() {
                    return sourceLevel;
                }

                @Override
                public void addChangeListener(ChangeListener l) {}

                @Override
                public void removeChangeListener(ChangeListener l) {}
            };
        }

    }

    private final class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

        private final Result result = new Result() {
            @Override
            public List<? extends String> getArguments() {
                return compilerOptions;
            }

            @Override
            public void addChangeListener(ChangeListener listener) {}

            @Override
            public void removeChangeListener(ChangeListener listener) {}
        };

        @Override
        public Result getOptions(FileObject file) {
            return result;
        }

    }
}
