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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import junit.framework.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.java.source.indexing.CompileWorker.ParsingOutput;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

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

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig",
                                                       "cache/s1/java/15/classes/test/Test4$1.sig")),
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

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test4.sig",
                                                       "cache/s1/java/15/classes/test/Test4$1.sig")),
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

    public void testArrayType() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 { public void test(Undef[] undef1, Undef undef2) { undef1.invoke(undef2); } }")),
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

    public void testStaticInitializer() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 { static { undef1.invoke(undef2); } }")),
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

    public void testJLObject() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("java/lang/Object.java", "package java.lang; public class Object { }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/java/lang/Object.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testFromOtherSourceRootBroken() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 extends extra.Extra { }")),
                                           Arrays.asList(),
                                           Arrays.asList(compileTuple("extra/Extra.java", "package extra; public class Extra { private void get() { undef(); } }")));

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

    public void testCyclic() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Cyclic.java", "package test; public class Cyclic extends Cyclic { }"),
                                                         compileTuple("test/Test.java", "package test; public class Test { public class Cyclic extends Cyclic {} }"),
                                                         compileTuple("test/Additional.java", "package test; public class Additional { }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Additional.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testAnnotations() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/AnnUse.java", "package test; @Ann1(@Unknown) @Ann2({@Unknown}) @Ann3(Unknown.class) @Ann4(Ann4.E.UNKNOWN) @Ann5(0) public class AnnUse { }"),
                                                         compileTuple("test/FirstAnnBroken.java", "package test; @Ann1(@Unknown) @Ann5(0) public class FirstAnnBroken{ }"),
                                                         compileTuple("test/FirstTwoAnnBroken.java", "package test; @Ann1(@Unknown) @Ann2({@Unknown}) @Ann5(0) public class FirstTwoAnnBroken { }"),
                                                         compileTuple("test/FirstAnnOK.java", "package test; @Ann5(0) @Ann1(@Unknown) @Ann2({@Unknown}) public class FirstAnnOK { }"),
                                                         compileTuple("test/MiddleBroken.java", "package test; @Ann5(0) @Ann4(@Unknown) @Ann6(0) public class MiddleBroken { }"),
                                                         compileTuple("test/WrongType.java", "package test; @Ann3(\"\") public class WrongType { }"),
                                                         compileTuple("test/ManyWrongTrailing.java", "package test; @Ann5(0) @Ann1(@Unknown) @Ann2({@Unknown}) @Ann3(Unknown.class) @Ann4(Ann4.E.UNKNOWN) public class ManyWrongTrailing { }"),
                                                         compileTuple("test/Ann1.java", "package test; public @interface Ann1 { public AnnExtra value(); }"),
                                                         compileTuple("test/Ann2.java", "package test; public @interface Ann2 { public AnnExtra[] value(); }"),
                                                         compileTuple("test/Ann3.java", "package test; public @interface Ann3 { public Class<?> value(); }"),
                                                         compileTuple("test/Ann4.java", "package test; public @interface Ann4 { public EnumExtra value(); }"),
                                                         compileTuple("test/Ann5.java", "package test; public @interface Ann5 { public int other(); }"),
                                                         compileTuple("test/Ann6.java", "package test; public @interface Ann6 { public int other(); }"),
                                                         compileTuple("test/AnnExtra.java", "package test; public @interface AnnExtra { }"),
                                                         compileTuple("test/EnumExtra.java", "package test; public enum EnumExtra { A; }"),
                                                         compileTuple("test/Additional.java", "package test; public class Additional { }"),
                                                         compileTuple("test/WrongDefault.java", "package test; public @interface WrongDefault { public Unknown value() default @Unknown }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Map<String, File> createdFiles = new HashMap<>();

        for (File created : result.createdFiles) {
            createdFiles.put(getWorkDir().toURI().relativize(created.toURI()).getPath(), created);
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/AnnUse.sig",
                                                       "cache/s1/java/15/classes/test/FirstAnnBroken.sig",
                                                       "cache/s1/java/15/classes/test/FirstTwoAnnBroken.sig",
                                                       "cache/s1/java/15/classes/test/FirstAnnOK.sig",
                                                       "cache/s1/java/15/classes/test/Ann1.sig",
                                                       "cache/s1/java/15/classes/test/Ann2.sig",
                                                       "cache/s1/java/15/classes/test/Ann3.sig",
                                                       "cache/s1/java/15/classes/test/Ann4.sig",
                                                       "cache/s1/java/15/classes/test/Ann5.sig",
                                                       "cache/s1/java/15/classes/test/Ann6.sig",
                                                       "cache/s1/java/15/classes/test/AnnExtra.sig",
                                                       "cache/s1/java/15/classes/test/EnumExtra.sig",
                                                       "cache/s1/java/15/classes/test/MiddleBroken.sig",
                                                       "cache/s1/java/15/classes/test/WrongType.sig",
                                                       "cache/s1/java/15/classes/test/Additional.sig",
                                                       "cache/s1/java/15/classes/test/WrongDefault.sig",
                                                       "cache/s1/java/15/classes/test/ManyWrongTrailing.sig")),
                     createdFiles.keySet());
        assertAnnotations("@test.Ann5 runtimeVisible=false",
                          createdFiles.get("cache/s1/java/15/classes/test/FirstAnnBroken.sig"));
        assertAnnotations("@test.Ann5 runtimeVisible=false",
                          createdFiles.get("cache/s1/java/15/classes/test/FirstTwoAnnBroken.sig"));
        assertAnnotations("@test.Ann5 runtimeVisible=false",
                          createdFiles.get("cache/s1/java/15/classes/test/FirstAnnOK.sig"));
        assertAnnotations("@test.Ann5 runtimeVisible=false, @test.Ann6 runtimeVisible=false",
                          createdFiles.get("cache/s1/java/15/classes/test/MiddleBroken.sig"));
    }

    private static void assertAnnotations(String expected, File classfile) throws IOException {
        ClassFile clazz = new ClassFile(classfile, false);
        assertEquals(expected,
                     clazz.getAnnotations().stream().map(ann -> ann.toString()).collect(Collectors.joining(", ")));
    }

    public void testPackageInfo() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/package-info.java", "@Deprecated package test;")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/package-info.sig")),
                     createdFiles);
        //TODO: check file content!!!
    }

    public void testIndexEnumConstants() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test; public enum Test implements Runnable { A() { public void run() {} }; }")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig",
                                                            "cache/s1/java/15/classes/test/Test$1.sig")),
                     createdFiles);
        ClasspathInfo cpInfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(getRoot()));
        Set<ElementHandle<TypeElement>> classIndexResult = cpInfo.getClassIndex().getElements(ElementHandle.createTypeElementHandle(ElementKind.ENUM, "test.Test"), EnumSet.of(SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
        assertEquals(1, classIndexResult.size());
    }

    public void testAnonymousClasses() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    private int i;\n" +
                                                                      "    private final Runnable ri = new Runnable() { void t() { System.err.println(i); } };\n" +
                                                                      "    private static final Runnable rs = new Runnable() { void t() { } };\n" +
                                                                      "    void testI() { System.err.println(new Runnable() { }); }\n" +
                                                                      "    static void testS() { System.err.println(new Runnable() { }); }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig",
                                                            "cache/s1/java/15/classes/test/Test$1.sig",
                                                            "cache/s1/java/15/classes/test/Test$2.sig",
                                                            "cache/s1/java/15/classes/test/Test$3.sig",
                                                            "cache/s1/java/15/classes/test/Test$4.sig")),
                     createdFiles);
        ClasspathInfo cpInfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(getRoot()));
        Set<ElementHandle<TypeElement>> classIndexResult = cpInfo.getClassIndex().getElements(ElementHandle.createTypeElementHandle(ElementKind.INTERFACE, "java.lang.Runnable"), EnumSet.of(SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
        assertEquals(4, classIndexResult.size());

        JavaSource js = JavaSource.create(ClasspathInfo.create(getRoot()));

        js.runUserActionTask(cc -> {
            cc.toPhase(JavaSource.Phase.PARSED);
            verifyAnonymous(cc, "test.Test$1", "test.Test");
            verifyAnonymous(cc, "test.Test$2", "test.Test");
            verifyAnonymous(cc, "test.Test$3", "testI()");
            verifyAnonymous(cc, "test.Test$4", "testS()");
        }, true);
    }

    private void verifyAnonymous(CompilationInfo info, String binaryName, String owner) {
        TypeElement ann = ElementHandle.createTypeElementHandle(ElementKind.CLASS, binaryName).resolve(info);
        assertEquals(binaryName, owner, ann.getEnclosingElement().toString());
    }

    public void testErroneousNewClass() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test; public class Test { void t() { new Undef(); new Undef() { }; } }")),
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

    public void testBasedAnonymous() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    private int i;\n" +
                                                                      "    static void test(Test t) {\n" +
                                                                      "        t.new Inner() {};\n" +
                                                                      "    }\n" +
                                                                      "    class Inner {}\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig",
                                                       "cache/s1/java/15/classes/test/Test$Inner.sig",
                                                       "cache/s1/java/15/classes/test/Test$1.sig")),
                     createdFiles);
    }

    public void testPreserveValidMethods1() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public static void main(String... args) { System.err.println(\"Hello, world!\"); }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public static void main(String... args) {\n" +
                "        System.err.println(\"Hello, world!\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testClearInvalidMethod() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public static void main(String... args) { System.err.println(undefined); }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public static void main(String... args) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testPreserveValidInitializers() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    private static int F1 = 0;\n" +
                                                                      "    private static int F2a = undef;\n" +
                                                                      "    private static int F2b = undef;\n" +
                                                                      "    private int F4 = 0;\n" +
                                                                      "    private int F5a = undef;\n" +
                                                                      "    private int F5b = undef;\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    private static int F1 = 0;\n" +
                "    private static int F2a;\n" +
                "    private static int F2b;\n" +
                "    private int F4 = 0;\n" +
                "    private int F5a;\n" +
                "    private int F5b;\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testBrokenClassHeader1() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test extends Undef {\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test extends Undef {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testNullReturnUnknown() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public Unknown test() {\n" +
                                                                      "        return null;\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public Unknown test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testBrokenNewClass() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test extends Unknown {\n" +
                                                                      "    public Unknown test() {\n" +
                                                                      "        return new Test();\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test extends Unknown {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    \n" +
                "    public Unknown test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testReturnBroken() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test extends Unknown {\n" +
                                                                      "    private Object o;\n" +
                                                                      "    public Unknown test() {\n" +
                                                                      "        return o == null;\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test extends Unknown {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    private Object o;\n" +
                "    \n" +
                "    public Unknown test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testAssertBroken() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    private Unknown o() { return null; }\n" +
                                                                      "    public void test() {\n" +
                                                                      "        assert o() == null;\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    private Unknown o() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    \n" +
                "    public void test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testAnonymousComplex() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test() {\n" +
                                                                      "        Runnable r = new Runnable() {\n" +
                                                                      "            public void run() {\n" +
                                                                      "                System.undefined();\n" +
                                                                      "                String s = null;\n" +
                                                                      "                Runnable r1 = new Runnable() {\n" +
                                                                      "                    public void run() {\n" +
                                                                      "                         System.err.println(s);\n" +
                                                                      "                    }\n" +
                                                                      "                };\n" +
                                                                      "            }\n" +
                                                                      "        };\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$1$1.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test() {\n" +
                "        Runnable r = new Runnable(){\n" +
                "            \n" +
                "            void $$anonymousClasses() {\n" +
                "                new Runnable(){\n" +
                "                    \n" +
                "                    () {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                    \n" +
                "                    public void run() {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                };\n" +
                "            }\n" +
                "            \n" +
                "            () {\n" +
                "                super();\n" +
                "            }\n" +
                "            \n" +
                "            public void run() {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location.args\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testFieldInit() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public enum Test {\n" +
                                                                      "    A(Undefined.S);\n" +
                                                                      "    public Test(String s) {}\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public enum Test {\n" +
                "    /*public static final*/ A /* = new <init>(null) */ /*enum*/ (null);\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.mod.not.allowed.here\");\n" +
                "    }\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test(String s) {\n" +
                "        super();\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testAnonymousComplex2() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test() {\n" +
                                                                      "        System.undefined();\n" +
                                                                      "        new N(Undefined.undefined) { };\n" +
                                                                      "    }\n" +
                                                                      "    public static class N {\n" +
                                                                      "        public N(Undefined u) {}\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$N.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    void $$anonymousClasses() {\n" +
                "        new N(null){\n" +
                "            static {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "            \n" +
                "            (error u) {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location.args\");\n" +
                "    }\n" +
                "    \n" +
                "    public static class N {\n" +
                "        static {\n" +
                "            throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "        }\n" +
                "        \n" +
                "        public N(Undefined u) {\n" +
                "            super();\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testNewClass() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test() {\n" +
                                                                      "        new Object(0) { };\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.apply.symbol\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testUndefNewArray() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public Test(Undef... arr) {}\n" +
                                                                      "    public void test(Undef... arr) {\n" +
                                                                      "        new Test(null, null);\n" +
                                                                      "        test(null, null);\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test(Undef... arr) {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test(Undef... arr) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testUndefAnonymous() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public Undef1 test() {\n" +
                                                                      "        return new Prop(1) {};\n" +
                                                                      "    }\n" +
                                                                      "    static class Prop extends Undef2 {\n" +
                                                                      "        Prop(int i) {}\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$Prop.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    void $$anonymousClasses() {\n" +
                "        new Prop(0){\n" +
                "            static {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "            \n" +
                "            (int i) {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public Undef1 test() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    \n" +
                "    static class Prop extends Undef2 {\n" +
                "        static {\n" +
                "            throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "        }\n" +
                "        \n" +
                "        Prop(int i) {\n" +
                "            throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testWeirdSuperCall() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test extends javax.swing.JPanel {\n" +
                                                                      "    class T extends Undef {\n" +
                                                                      "        public void test() {\n" +
                                                                      "            super.processEvent(null);\n" +
                                                                      "        }\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$T.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test extends javax.swing.JPanel {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    class T extends Undef {\n" +
                "        static {\n" +
                "            throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "        }\n" +
                "        \n" +
                "        T() {\n" +
                "            throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "        }\n" +
                "        \n" +
                "        public void test() {\n" +
                "            throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testAnonymousComplex3() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test(int p) {\n" +
                                                                      "        undefined();\n" +
                                                                      "        int val = 0;\n" +
                                                                      "        new N(val) {\n" +
                                                                      "             int f = p;\n" +
                                                                      "             Object f2 = new Object() {\n" +
                                                                      "                     int f = p;\n" +
                                                                      "                     public void test() {\n" +
                                                                      "                         System.err.println(p);\n" +
                                                                      "                     }\n" +
                                                                      "                 };\n" +
                                                                      "             public void test() {\n" +
                                                                      "                 System.err.println(p);\n" +
                                                                      "                 new Object() {\n" +
                                                                      "                     int f = p;\n" +
                                                                      "                     public void test() {\n" +
                                                                      "                         System.err.println(p);\n" +
                                                                      "                     }\n" +
                                                                      "                 };\n" +
                                                                      "             }\n" +
                                                                      "        };\n" +
                                                                      "    }\n" +
                                                                      "    public static class N {\n" +
                                                                      "        public N(int val) {}\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$N.sig",
                                                       "cache/s1/java/15/classes/test/Test$1$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$1$2.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    void $$anonymousClasses() {\n" +
                "        new N(0){\n" +
                "            \n" +
                "            void $$anonymousClasses() {\n" +
                "                new Object(){\n" +
                "                    \n" +
                "                    () {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                    int f = 0;\n" +
                "                    \n" +
                "                    public void test() {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                };\n" +
                "                new Object(){\n" +
                "                    \n" +
                "                    () {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                    int f = 0;\n" +
                "                    \n" +
                "                    public void test() {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                };\n" +
                "            }\n" +
                "            \n" +
                "            (int val) {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "            int f = 0;\n" +
                "            Object f2 = null;\n" +
                "            \n" +
                "            public void test() {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test(int p) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location.args\");\n" +
                "    }\n" +
                "    \n" +
                "    public static class N {\n" +
                "        \n" +
                "        public N(int val) {\n" +
                "            super();\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testAnonymousComplex4() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test1(int p) {\n" +
                                                                      "        undefined();\n" +
                                                                      "        new Object() { void t() { undefined(); Object o = new Object() { }; } };\n" +
                                                                      "    }\n" +
                                                                      "    public void test2(int p) {\n" +
                                                                      "        undefined();\n" +
                                                                      "        new Object() { undefined(); Object o = new Object() { }; };\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$2.sig",
                                                       "cache/s1/java/15/classes/test/Test$2$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$1$1.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    void $$anonymousClasses() {\n" +
                "        new Object(){\n" +
                "            \n" +
                "            void $$anonymousClasses() {\n" +
                "                new Object(){\n" +
                "                    \n" +
                "                    () {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                };\n" +
                "            }\n" +
                "            static {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.invalid.meth.decl.ret.type.req\");\n" +
                "            }\n" +
                "            \n" +
                "            () {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "            Object o = null;\n" +
                "        };\n" +
                "        new Object(){\n" +
                "            \n" +
                "            void $$anonymousClasses() {\n" +
                "                new Object(){\n" +
                "                    \n" +
                "                    () {\n" +
                "                        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "                    }\n" +
                "                };\n" +
                "            }\n" +
                "            \n" +
                "            () {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "            \n" +
                "            void t() {\n" +
                "                throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test1(int p) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location.args\");\n" +
                "    }\n" +
                "    \n" +
                "    public void test2(int p) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location.args\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testAnonymousComplexCorrect() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test(int p) {\n" +
                                                                      "        int val = 0;\n" +
                                                                      "        new N(val) {\n" +
                                                                      "            public void test() {\n" +
                                                                      "                System.err.println(p);\n" +
                                                                      "            }\n" +
                                                                      "        };\n" +
                                                                      "    }\n" +
                                                                      "    public static class N {\n" +
                                                                      "        public N(int val) {}\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test$1.sig",
                                                       "cache/s1/java/15/classes/test/Test$N.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test(int p) {\n" +
                "        int val = 0;\n" +
                "        new N(val){\n" +
                "            \n" +
                "            (int val) {\n" +
                "                super(val);\n" +
                "            }\n" +
                "            \n" +
                "            public void test() {\n" +
                "                System.err.println(p);\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "    \n" +
                "    public static class N {\n" +
                "        \n" +
                "        public N(int val) {\n" +
                "            super();\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testWarningsAreNotErrors() throws Exception {
        setCompilerOptions(Arrays.asList("-Xlint:rawtypes"));

        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test<T> {\n" +
                                                                      "    Test t;\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test<T> {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    Test t;\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testSuperCall() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test extends SuperClass implements SuperIntf {\n" +
                                                                      "    public void test(int p) {\n" +
                                                                      "        super.test1();\n" +
                                                                      "        SuperIntf.super.test2();\n" +
                                                                      "    }\n" +
                                                                      "}\n" +
                                                                      "class SuperClass {\n" +
                                                                      "    public void test1() {\n" +
                                                                      "    }\n" +
                                                                      "}\n" +
                                                                      "interface SuperIntf {\n" +
                                                                      "    public default void test2() {\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/SuperIntf.sig",
                                                       "cache/s1/java/15/classes/test/SuperClass.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test extends SuperClass implements SuperIntf {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test(int p) {\n" +
                "        super.test1();\n" +
                "        SuperIntf.super.test2();\n" +
                "    }\n" +
                "}\n" +
                "class SuperClass {\n" +
                "    \n" +
                "    SuperClass() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test1() {\n" +
                "    }\n" +
                "}\n" +
                "interface SuperIntf {\n" +
                "    \n" +
                "    public default void test2() {\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testStaticInit() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    static {\n" +
                                                                      "        System.err.println();\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    static {\n" +
                "        System.err.println();\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testRecordPatterns() throws Exception {
        setSourceLevel(SourceVersion.latest().name().substring("RELEASE_".length()));
        setCompilerOptions(Arrays.asList("--enable-preview"));
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test;\n"
                        + "record Rect(ColoredPoint upperLeft) {}\n"
                        + "record ColoredPoint(Point p) {}\n"
                        + "record Point(int x){}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        if (o instanceof Rect(ColoredPoint(Point p) ul)) {\n"
                        + "            int x = p.x();\n"
                        + "            System.out.println(\"Hello\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")), Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Rect.sig",
                                                       "cache/s1/java/15/classes/test/ColoredPoint.sig",
                                                       "cache/s1/java/15/classes/test/Point.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")), createdFiles);
    }

    public void testEnhancedSwitch1() throws Exception {
        setSourceLevel(SourceVersion.latest().name().substring("RELEASE_".length()));
        setCompilerOptions(Arrays.asList("--enable-preview"));
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test;\n"
                        + "record Rect(ColoredPoint upperLeft) {}\n"
                        + "record ColoredPoint(Point p) {}\n"
                        + "record Point(int x){}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        switch (o) {\n"
                        + "            case Rect r: \n"
                        + "                System.out.println(\"Hello\");\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                break;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")), Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Rect.sig",
                                                       "cache/s1/java/15/classes/test/ColoredPoint.sig",
                                                       "cache/s1/java/15/classes/test/Point.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")), createdFiles);
    }

    public void testEnhancedSwitch2() throws Exception {
        setSourceLevel(SourceVersion.latest().name().substring("RELEASE_".length()));
        setCompilerOptions(Arrays.asList("--enable-preview"));
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test;\n"
                        + "record Rect(ColoredPoint upperLeft) {}\n"
                        + "record ColoredPoint(Point p) {}\n"
                        + "record Point(int x){}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        switch (o) {\n"
                        + "            case null: \n"
                        + "                System.out.println(\"Hello\");\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                break;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")), Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Rect.sig",
                                                       "cache/s1/java/15/classes/test/ColoredPoint.sig",
                                                       "cache/s1/java/15/classes/test/Point.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")), createdFiles);
    }

    public void testEnhancedSwitch3() throws Exception {
        setSourceLevel(SourceVersion.latest().name().substring("RELEASE_".length()));
        setCompilerOptions(Arrays.asList("--enable-preview"));
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test;\n"
                        + "record Rect(ColoredPoint upperLeft) {}\n"
                        + "record ColoredPoint(Point p) {}\n"
                        + "record Point(int x){}\n"
                        + "public class Test {\n"
                        + "    private void test(Object o) {\n"
                        + "        switch (o) {\n"
                        + "            case (String s): \n"
                        + "                System.out.println(\"Hello\");\n"
                        + "                break;\n"
                        + "            default:\n"
                        + "                break;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")), Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Rect.sig",
                                                       "cache/s1/java/15/classes/test/ColoredPoint.sig",
                                                       "cache/s1/java/15/classes/test/Point.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")), createdFiles);
    }

    public void testRecordErroneousComponent() throws Exception {
        setSourceLevel(SourceVersion.latest().name().substring("RELEASE_".length()));

        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public record Test(Unknown unknown) {\n" +
                                                                      "}\n")),
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

    public void testMethodWithErroneousInMemberRef() throws Exception {
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void testA() {\n" +
                                                                      "        r().map(this::test1);\n" +
                                                                      "    }\n" +
                                                                      "    public void testB() {\n" +
                                                                      "        r().map(x -> test1(x));\n" +
                                                                      "    }\n" +
                                                                      "    public Object test1(Unknown u) {\n" +
                                                                      "        return null;\n" +
                                                                      "    }\n" +
                                                                      "    public static void doTest(I i) {\n" +
                                                                      "    }\n" +
                                                                      "    private static java.util.Optional<Unknown> r() {\n" +
                                                                      "        return null;\n" +
                                                                      "    }\n" +
                                                                      "}\n" +
                                                                      "interface I {\n" +
                                                                      "    public Object test(Object o);\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/I.sig",
                                                       "cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.cant.resolve.location\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void testA() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    \n" +
                "    public void testB() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    \n" +
                "    public Object test1(Unknown u) {\n" +
                "        return null;\n" +
                "    }\n" +
                "    \n" +
                "    public static void doTest(I i) {\n" +
                "    }\n" +
                "    \n" +
                "    private static java.util.Optional<Unknown> r() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n" +
                "interface I {\n" +
                "    \n" +
                "    public Object test(Object o);\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testComplexDesugaringSupertypes() throws Exception {
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test4.java", "package test; public class Test4 extends extra.Extra { }")),
                                           Arrays.asList(),
                                           Arrays.asList(compileTuple("extra/Extra.java", "package extra; public class Extra { private void get() { Extra2.extra(); } }"),
                                                         compileTuple("extra/Extra2.java", "package extra; public class Extra2 { private void get() { Unknown unknown; } }")));

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

    public void testPatternSwitch() throws Exception {
        setSourceLevel("20");

        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test1(Object o) {\n" +
                                                                      "        switch (o) {\n" +
                                                                      "            case String s -> {}\n" +
                                                                      "            case Object oo -> {}\n" +
                                                                      "        }\n" +
                                                                      "    }\n" +
                                                                      "    public void test2(Object o) {\n" +
                                                                      "        switch (o) {\n" +
                                                                      "            case String s -> {}\n" +
                                                                      "            case Object oo -> {}\n" +
                                                                      "        }\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test1(Object o) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.feature.not.supported.in.source.plural\");\n" +
                "    }\n" +
                "    \n" +
                "    public void test2(Object o) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testTypeTest() throws Exception {
        setSourceLevel("17");

        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public class Test {\n" +
                                                                      "    public void test1(Object o) {\n" +
                                                                      "        if (o instanceof String s) {\n" +
                                                                      "            System.err.println();\n" +
                                                                      "        }\n" +
                                                                      "    }\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public Test() {\n" +
                "        super();\n" +
                "    }\n" +
                "    \n" +
                "    public void test1(Object o) {\n" +
                "        if (o instanceof String s) {\n" +
                "            System.err.println();\n" +
                "        }\n" +
                "    }\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testWrongRecordComponent() throws Exception {
        setSourceLevel("17");

        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java",
                                                                      "package test;\n" +
                                                                      "public record Test(int wait) {\n" +
                                                                      "}\n")),
                                           Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")),
                     createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.illegal.record.component.name\");\n" +
                "    }\n" +
                "    \n" +
                "    public Test(int wait) {\n" +
                "        super();\n" +
                "    }\n" +
                "    private final int wait;\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testRecord1() throws Exception {
        setSourceLevel("11");
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test;\n"
                        + "record Test(int i) {}\n")), Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")), createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "class Test {\n" +
                "    static {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - compiler.err.feature.not.supported.in.source.plural\");\n" +
                "    }\n" +
                "    \n" +
                "    public final java.lang.String toString() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - java.lang.runtime.ObjectMethods does not exist!\");\n" +
                "    }\n" +
                "    \n" +
                "    public final int hashCode() {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - java.lang.runtime.ObjectMethods does not exist!\");\n" +
                "    }\n" +
                "    \n" +
                "    public final boolean equals(java.lang.Object o) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code - java.lang.runtime.ObjectMethods does not exist!\");\n" +
                "    }\n" +
                "    \n" +
                "    Test(int i) {\n" +
                "        throw new java.lang.RuntimeException(\"Uncompilable code\");\n" +
                "    }\n" +
                "    private final int i;\n" +
                "}");
        assertEquals(expected, file2Fixed);
    }

    public void testRecord2() throws Exception {
        setSourceLevel("17");
        Map<String, String> file2Fixed = new HashMap<>();
        VanillaCompileWorker.fixedListener = (file, cut) -> {
            try {
                FileObject source = URLMapper.findFileObject(file.toUri().toURL());
                file2Fixed.put(FileUtil.getRelativePath(getRoot(), source), cut.toString());
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        };
        ParsingOutput result = runIndexing(Arrays.asList(compileTuple("test/Test.java", "package test;\n"
                        + "record Test(int i) {}\n")), Arrays.asList());

        assertFalse(result.lowMemory);
        assertTrue(result.success);

        Set<String> createdFiles = new HashSet<String>();

        for (File created : result.createdFiles) {
            createdFiles.add(getWorkDir().toURI().relativize(created.toURI()).getPath());
        }

        assertEquals(new HashSet<String>(Arrays.asList("cache/s1/java/15/classes/test/Test.sig")), createdFiles);
        Map<String, String> expected = Collections.singletonMap("test/Test.java",
                "package test;\n" +
                "\n" +
                "class Test {\n" +
                "    \n" +
                "    Test(int i) {\n" +
                "        super();\n" +
                "    }\n" +
                "    private final int i;\n" +
                "}");
        assertEquals(expected, file2Fixed);
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

    @Override
    protected void tearDown() throws Exception {
        VanillaCompileWorker.fixedListener = (file, cut) -> {};
    }

    public static Test suite() {
        return new NbTestSuite(VanillaCompileWorkerTest.class);
    }

    static {
        VanillaCompileWorker.DIAGNOSTIC_TO_TEXT = d -> d.getCode();
    }
}
