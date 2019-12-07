/**
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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.Test;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.NoJavacHelper;
import org.netbeans.modules.java.source.indexing.CompileWorker.ParsingOutput;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.parsing.spi.indexing.Context;

/**
 *
 * @author lahvac
 */
public class VanillaCompileWorkerTest extends CompileWorkerTestBase {
    
    public VanillaCompileWorkerTest(String name) {
        super(name);
    }
    
    @Override
    protected ParsingOutput runCompileWorker(Context context, JavaParsingContext javaContext, Collection<? extends CompileTuple> files) {
        return new VanillaCompileWorker().compile(null, context, javaContext, files);
    }
    
    public void testVanillaWorker() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test3.java", "package test; public class Test3"),
                                                         compileTuple("test/Test4.java", "package test; public class Test4 { Undef undef; }")),
                                           Arrays.asList(virtualCompileTuple("test/Test1.virtual", "package test; public class Test1 {}"),
                                                         virtualCompileTuple("test/Test2.virtual", "package test; public class Test2 {}")));

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test3.sig",
                                                       "cache/s1/java/15/classes/test/Test4.sig")), createdFiles);
        result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 { void t() { Undef undef; } }")),
                             Collections.emptyList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);
    }

    public void testRepair1() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 { public void test() { Undef undef; } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")), createdFiles);
    }

    public void testRepair2() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; @Undef public class Test4 { @Undef public void test1() { } @Deprecated @Undef public void test2() { } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")), createdFiles);
        //TODO: check file content!!!
    }

    public void testRepair3() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 { public <T> void test1(T t) { } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")), createdFiles);
        //TODO: check file content!!!
    }

    public void testRepair4() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; import java.util.List; public class Test4 { public List<Undef> test() { } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")), createdFiles);
        //TODO: check file content!!!
    }

    public void testRepairEnum() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public enum Test4 { A {} }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testRepairEnum2() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public enum Test4 { B(Unknown.unknownO(), Unknown.unknownB(), Unknown.unknownI()) {}; private Test4(String str, boolean b, int i) {} }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testRepairWildcard() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; import java.util.*; public class Test4 { void test(List<? extends Undef> l1, List<? super Undef> l2) { } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testErasureField() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4<T> { void test(Test4<Undef> t2, Undef t1) { } static void t(Test4 raw) { raw.test(null, null); } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testModuleInfoAndSourceLevel8() throws Exception {
        setSourceLevel("8");

        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("module-info.java", "module m {}"),
                                                         compileTuple("test/Test.java", "package test; public class Test { }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
    }

    public void testErroneousMethodClassNETBEANS_224() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test1.java", "package test; public class Test1 { public abstract void }"),
                                                         compileTuple("test/Test2.java", "package test; public class Test2 { public abstract Object }"),
                                                         compileTuple("test/Test3.java", "package test; public class Test3 { public abstract class }"),
                                                         compileTuple("test/Test4.java", "package test; public class ")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test1.sig",
                                                       "cache/s1/java/15/classes/test/Test2.sig",
                                                       "cache/s1/java/15/classes/test/Test3.sig")),
                     createdFiles);
    }

    public void testRepairFieldBrokenGenerics() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; import java.util.List; public class Test4 { public List<Undef> test; }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")), createdFiles);
        //TODO: check file content!!!
    }

    public void testTypeParameter() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; import java.util.*; public class Test4 { <T extends Undef> T test() { return null; } }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testDuplicate() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java",
                                                                      "package test;\n" +
                                                                      "@interface Test4 { private int t(int i) { return i; } }\n" +
                                                                      "@interface Test4 { private int t(int i) { return i; } }\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testAnnotationMethodWithBody() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java",
                                                                      "package test;\n" +
                                                                      "@interface Test4 { private int t(int i) { return i; } }\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public static void noop() {}

    @Override
    public void runTest() throws Throwable {
        AtomicBoolean wasException = new AtomicBoolean();
        Handler logHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getThrown() != null) {
                    wasException.set(true);
                }
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
        };
        Level origLevel = JavaIndex.LOG.getLevel();
        JavaIndex.LOG.setLevel(Level.WARNING);
        JavaIndex.LOG.addHandler(logHandler);
        try {
            super.runTest();
            assertFalse(wasException.get());
        } finally {
            JavaIndex.LOG.setLevel(origLevel);
            JavaIndex.LOG.removeHandler(logHandler);
        }
    }

    public static Test suite() {
        if (NoJavacHelper.hasNbJavac()) {
            return new VanillaCompileWorkerTest("noop");
        } else {
            return new NbTestSuite(VanillaCompileWorkerTest.class);
        }
    }
}
