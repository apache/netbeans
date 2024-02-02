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
package org.netbeans.modules.java.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class JavaSourceUtilImplTest extends NbTestCase {
    
    private FileObject wd;
    private FileObject root;
    private FileObject java;
    private FileObject cache;
    private FileObject cacheSrc;
    private FileObject ap;
    
    public JavaSourceUtilImplTest(String name) {
        super(name);
    }
    
    
    @Before
    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil2.disableMultiFileSourceRoots();
        wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        root = FileUtil.createFolder(wd, "src");    //NOI18N
        java = createFile(root, "org/nb/A.java","package nb;\n class A {}");    //NOI18N
        cache = FileUtil.createFolder(wd, "cache");    //NOI18N
        cacheSrc = FileUtil.createFolder(wd, "cacheSrc");    //NOI18N
        ap = createFile(cacheSrc, "test/AP.java","");    //NOI18N
    }
    
    @Test
    public void testGenerate() throws Exception {
        MockLookup.setInstances(new SourceLevelQueryImplementation() {
            @Override
            public String getSourceLevel(FileObject javaFile) {
                return "11";
            }
        });
        assertNotNull(root);
        assertNotNull(java);
        DiagnosticListener<JavaFileObject> noErrors = d -> {
            fail(d.getMessage(null));
        };
        final Map<String, byte[]> res = new JavaSourceUtilImpl().generate(root, java, "package nb;\n class A { void foo(){}}", noErrors);   //NOI18N
        assertNotNull(res);
        assertEquals(1, res.size());
        Map.Entry<String,byte[]> e = res.entrySet().iterator().next();
        assertEquals("nb.A", e.getKey());   //NOI18N
        final ClassFile cf = new ClassFile(new ByteArrayInputStream(e.getValue()));
        assertEquals(2, cf.getMethodCount());
        final Set<String> methods = cf.getMethods().stream()
                .map((m) -> m.getName())
                .collect(Collectors.toSet());
        assertEquals(
                new HashSet<>(Arrays.asList(new String[]{
                    "<init>",   //NOI18N
                    "foo"       //NOI18N
                })),
                methods);
    }

    @Test
    public void testGenerateWithAP() throws Exception {
        MockLookup.setInstances(new AnnotationProcessingQueryImplementation() {
            @Override
            public AnnotationProcessingQuery.Result getAnnotationProcessingOptions(FileObject file) {
                if (file != java && file != root) return null;
                return new AnnotationProcessingQuery.Result() {
                    @Override
                    public Set<? extends AnnotationProcessingQuery.Trigger> annotationProcessingEnabled() {
                        return EnumSet.allOf(AnnotationProcessingQuery.Trigger.class);
                    }

                    @Override
                    public Iterable<? extends String> annotationProcessorsToRun() {
                        return Arrays.asList("test.AP");
                    }

                    @Override
                    public URL sourceOutputDirectory() {
                        return cache.toURL();
                    }

                    @Override
                    public Map<? extends String, ? extends String> processorOptions() {
                        return Collections.emptyMap();
                    }

                    @Override
                    public void addChangeListener(ChangeListener l) {
                    }

                    @Override
                    public void removeChangeListener(ChangeListener l) {
                    }
                };
            }
        }, new ClassPathProvider() {
            @Override
            public ClassPath findClassPath(FileObject file, String type) {
                if (file != java && file != root) return null;
                if (type == JavaClassPathConstants.PROCESSOR_PATH) {
                    return ClassPathSupport.createClassPath(cache);
                } else if (type == ClassPath.SOURCE) {
                    return ClassPathSupport.createClassPath(root);
                }
                return null;
            }
        }, new SourceLevelQueryImplementation() {
            @Override
            public String getSourceLevel(FileObject javaFile) {
                return "11";
            }
        });

        String apCode = "package test;\n" +
                        "\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.Writer;\n" +
                        "import java.util.Set;\n" +
                        "import javax.annotation.processing.AbstractProcessor;\n" +
                        "import javax.annotation.processing.RoundEnvironment;\n" +
                        "import javax.annotation.processing.SupportedAnnotationTypes;\n" +
                        "import javax.annotation.processing.SupportedSourceVersion;\n" +
                        "import javax.lang.model.element.TypeElement;\n" +
                        "import javax.lang.model.SourceVersion;\n" +
                        "\n" +
                        "@SupportedAnnotationTypes(\"*\") @SupportedSourceVersion(SourceVersion.RELEASE_11)\n" +
                        "public class AP extends AbstractProcessor {\n" +
                        "    int round;\n" +
                        "    @Override\n" +
                        "    public boolean process(Set<? extends TypeElement> arg0, RoundEnvironment arg1) {\n" +
                        "        if (round++ == 0) {\n" +
                        "            try (Writer w = processingEnv.getFiler().createSourceFile(\"nb.Dep\").openWriter()) {\n" +
                        "                w.write(\"package nb; class Dep { }\");\n" +
                        "            } catch (IOException ex) {\n" +
                        "                ex.printStackTrace();\n" +
                        "                throw new IllegalStateException(ex);\n" +
                        "            }\n" +
                        "        }\n" +
                        "        return false;\n" +
                        "    }\n" +
                        "    \n" +
                        "}\n";
        DiagnosticListener<JavaFileObject> noErrors = d -> {
            fail(d.getMessage(null));
        };
        for (Entry<String, byte[]> e : new JavaSourceUtilImpl().generate(cacheSrc, ap, apCode, noErrors).entrySet()) {
            try (OutputStream out = FileUtil.createData(cache, e.getKey().replace(".", "/") + ".class").getOutputStream()) {
                out.write(e.getValue());
            }
        }
        assertNotNull(root);
        assertNotNull(java);
        final Map<String, byte[]> res = new JavaSourceUtilImpl().generate(root, java, "package nb;\n class A { Dep dep; void foo(){}}", noErrors);   //NOI18N
        assertNotNull(res);
        assertEquals(1, res.size());
        Map.Entry<String,byte[]> e = res.entrySet().iterator().next();
        assertEquals("nb.A", e.getKey());   //NOI18N
        final ClassFile cf = new ClassFile(new ByteArrayInputStream(e.getValue()));
        assertEquals(2, cf.getMethodCount());
        final Set<String> methods = cf.getMethods().stream()
                .map((m) -> m.getName())
                .collect(Collectors.toSet());
        assertEquals(
                new HashSet<>(Arrays.asList(new String[]{
                    "<init>",   //NOI18N
                    "foo"       //NOI18N
                })),
                methods);
    }

    @Test
    public void testGenerateWrongContent() throws Exception {
        MockLookup.setInstances(new SourceLevelQueryImplementation() {
            @Override
            public String getSourceLevel(FileObject javaFile) {
                return "11";
            }
        });
        assertNotNull(root);
        assertNotNull(java);
        int[] errorCount = new int[1];
        DiagnosticListener<JavaFileObject> errors = d -> {
            if (d.getKind() == Kind.ERROR) {
                errorCount[0]++;
            }
        };
        final Map<String, byte[]> res = new JavaSourceUtilImpl().generate(root, java, "package nb;\n class A { void foo(){ Unknown unknown;}}", errors);   //NOI18N
        assertNotNull(res);
        assertEquals(0, res.size());
        assertEquals(1, errorCount[0]);
    }

    private static FileObject createFile(
            final FileObject root,
            final String path,
            final String content) throws Exception {
        FileObject file = FileUtil.createData(root, path);
        TestUtilities.copyStringToFile(file, content);
        return file;
    }
    
    private static void dump(
            final FileObject wd,
            final Map<String,byte[]> clzs) throws IOException {
        for (Map.Entry<String,byte[]> clz : clzs.entrySet()) {
            final String extName = FileObjects.convertPackage2Folder(clz.getKey());
            final FileObject data = FileUtil.createData(wd, String.format(
                    "%s.class", //NOI18N
                    extName));
            FileLock l = data.lock();
            try (final OutputStream out = data.getOutputStream(l)) {
                out.write(clz.getValue());
            }finally {
                l.releaseLock();
            }
        }
        System.out.printf("Dumped into: %s%n", FileUtil.getFileDisplayName(wd));
    }

    static {
        System.setProperty("SourcePath.no.source.filter", "true");
    }
}
