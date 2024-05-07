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
package org.netbeans.modules.java.editor.imports;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.test.support.MemoryValidator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeImportsTest extends NbTestCase {
    
    private static final Set<String> IGNORE_CLASSES = new HashSet<String>(Arrays.asList(new String[] {
        "com.sun.tools.javac.util.List",
        "com.sun.tools.javac.code.Attribute.RetentionPolicy",
        "com.sun.tools.classfile.Opcode.Set",
        "com.sun.xml.bind.v2.schemagen.xmlschema.List",
        "com.sun.xml.txw2.Document",
        "com.sun.xml.internal.txw2.Document",
        "com.sun.xml.internal.bind.v2.schemagen.xmlschema.List",
        "com.sun.xml.internal.ws.wsdl.writer.document.Documented",
        "com.sun.xml.internal.bind.v2.model.core.Element",
        "com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl.Element",
        "com.sun.xml.internal.bind.v2.schemagen.xmlschema.Element",
        "sun.text.normalizer.RangeValueIterator.Element",
        "javax.xml.bind.Element",
        "javax.lang.model.element.Element",
        "com.sun.org.apache.xalan.internal.xsltc.runtime.AttributeList",
        "com.sun.xml.internal.ws.api.server.Adapter.Toolkit",
        "sunw.io.Serializable",
        "sun.rmi.transport.Target",
        "com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Element",
        "com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections",
        "com.azul.crs.client.service.JarLoadMonitor.ProcessedJarFiles.LRU.Element"
    }));
    
    private static final List<Pattern> IGNORE_PATTERNS = Collections.unmodifiableList(Arrays.asList(
        Pattern.compile("jdk\\..*\\.internal\\..*"),
        Pattern.compile("org\\.graalvm\\..*"),
        Pattern.compile("com\\.azul\\.crs\\..*") // https://docs.azul.com/vulnerability-detection/detailed-information/configuration-options
    ));

    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    public ComputeImportsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        
        clearWorkDir();
        
        if (cache == null) {
            cache = new File(getWorkDir(), "cache");
            cacheFO = FileUtil.createFolder(cache);

            IndexUtil.setCacheFolder(cache);

            TestUtilities.analyzeBinaries(SourceUtilsTestUtil.getBootClassPath());
        }
    }
    
    public static Test suite() {
        return MemoryValidator.wrap(new TestSuite(ComputeImportsTest.class));
    }
    
    public void testSimple() throws Exception {
        doTest("TestSimple");
    }
    
    public void testFilterDeclaration() throws Exception {
        doTest("TestFilterDeclaration");
    }
    
    public void testFilterTypedInitializator() throws Exception {
        doTest("TestFilterTypedInitializator");
    }
    
    public void testFilterWithMethods() throws Exception {
        doTest("TestFilterWithMethods");
    }
    
    public void testGetCookie() throws Exception {
        doTest("TestGetCookie");
    }
    
    public void testNew() throws Exception {
        doTest("TestNew");
    }
    
    public void testException() throws Exception {
        doTest("TestException");
    }
    
    public void testEmptyCatch() throws Exception {
        doTest("TestEmptyCatch");
    }
    
    public void testUnfinishedMethod() throws Exception {
        doTest("TestUnfinishedMethod");
    }
    
    public void testUnsupportedOperation1() throws Exception {
        doTest("TestUnsupportedOperation1");
    }
    
    public void testPackageDoesNotExist() throws Exception {
        doTest("TestPackageDoesNotExist");
    }

    public void testUnfinishedMethod2() throws Exception {
        doTest("TestUnfinishedMethod2");
    }
    
    public void testAnnotation() throws Exception {
        doTest("TestAnnotation");
    }
    
    public void testAnnotation2() throws Exception {
        doTest("TestAnnotation2");
    }
    
    public void test90743() throws Exception {
        doTest("Test90743");
    }
    
    public void test97420() throws Exception {
        doTest("Test97420");
    }
    
    public void test102613() throws Exception {
        doTest("Test102613");
    }
    
    public void testFilterByKind() throws Exception {
        doTest("TestFilterByKind");
    }

    public void test202604() throws Exception {
        doTest("Test202604");
    }

    public void testBrokenLambdaParameter() throws Exception {
        doTest("TestBrokenLambdaParameter");
    }
    
    public void testStaticImports1() throws Exception {
        doTest("StaticImports1");
    }
    
    public void testStaticImportsArrays1() throws Exception {
        doTest("StaticImportsArrays1");
    }
    
    public void testStaticImportsArrays2() throws Exception {
        doTest("StaticImportsArrays2");
    }
    
    public void test232647() throws Exception {
        doTest("Test232647");
    }
    
    public void testStaticImports233117() throws Exception {
        doTest("StaticImports233117", "1.8");
    }
    
    public void testNotImportFieldAsClass() throws Exception {
        doTest("TestNotImportFieldAsClass");
    }

    public void testJIRA2914a() throws Exception {
        doTest("test/Test",
               "11",
               Arrays.asList(
                   new FileData("test/Test.java",
                                "package test;\n" +
                                "import io.test.IO;\n" +
                                "public class Test {\n" +
                                "    IO i1;\n" +
                                "    io.test.IO i2;\n" +
                                "}\n"),
                   new FileData("io/test/IO.java",
                                "package io.test;\n" +
                                "public class IO {\n" +
                                "    public static IO io() { return null; }\n" +
                                "}\n")
               ),
               "",
               "");
    }

    public void testJIRA2914b() throws Exception {
        doTest("test/Test",
               "11",
               Arrays.asList(
                   new FileData("test/Test.java",
                                "package test;\n" +
                                "import io.test.IO;\n"),
                   new FileData("io/test/IO.java",
                                "package io.test;\n" +
                                "public class IO {\n" +
                                "    public static IO io() { return null; }\n" +
                                "}\n")
               ),
               "",
               "");
    }

    public void testJIRA2914c() throws Exception {
        doTest("test/Test",
               "11",
               Arrays.asList(
                   new FileData("test/Test.java",
                                "import io.test.IO;\n"),
                   new FileData("io/test/IO.java",
                                "package io.test;\n" +
                                "public class IO {\n" +
                                "    public static IO io() { return null; }\n" +
                                "}\n")
               ),
               "",
               "");
    }
    
    public void testRecordImport() throws Exception {
        doTest("test/Test",
                "14",
                Arrays.asList(
                        new FileData("test/Test.java",
                                "package test;\n"
                                + "import mytest.test.MyRecord;\n"
                                + "public class Test {\n"
                                + "    MyRecord rec;\n"
                                + "}\n"),
                        new FileData("mytest/test/MyRecord.java",
                                "package mytest.test;\n"
                                + "public record MyRecord() {\n"
                                + "}\n")
                ),
                "",
                "");
    }

    // https://github.com/apache/netbeans/issues/7073
    public void testDontImportRootPackageMatchingMember() throws Exception {
        doTest("test/Test",
                "11",
                Arrays.asList(
                        new FileData("test/Test.java",
                                "package test;\n" +
                                "import java.util.List;\n" +
                                "public class Test {\n" +
                                "    public static class SomeClass {\n" +
                                "        public static Object java(java.util.Map<String, String> value) {\n" +
                                "            return value;\n" +
                                "        }\n" +
                                "    }\n" +
                                "}")
                ),
                "",
                "");
    }

    private void prepareTest(String capitalizedName, String sourceLevel, Iterable<FileData> files) throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        for (FileData fd : files) {
            FileObject target = FileUtil.createData(sourceRoot, fd.fileName);
            try (OutputStream out = target.getOutputStream();
                 Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                w.write(fd.content);
            }
        }
        
        FileUtil.refreshAll();
        
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        testSource = sourceRoot.getFileObject(capitalizedName + ".java");
        
        assertNotNull(testSource);
        
        SourceUtilsTestUtil.setSourceLevel(testSource, sourceLevel);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private String dump(Map<String, List<Element>> set) {
        StringWriter out = new StringWriter();
        List<String> keys = new LinkedList<String>(set.keySet());
        
        Collections.sort(keys);
        
        for (String key : keys) {
            List<String> fqns = new ArrayList<String>();
            
            for (Element t : set.get(key)) {
                String fqn;
                
                if (t.getKind().isClass() || t.getKind().isInterface()) {
                    fqn = ((TypeElement) t).getQualifiedName().toString();
                } else {
                    StringBuilder fqnSB = new StringBuilder();
                    
                    fqnSB.append(((TypeElement) t.getEnclosingElement()).getQualifiedName());
                    fqnSB.append('.');
                    fqnSB.append(t.getSimpleName());
                    
                    if (t.getKind() == ElementKind.METHOD) {
                        fqnSB.append('(');
                        boolean first = true;
                        for (VariableElement var : ((ExecutableElement) t).getParameters()) {
                            if (!first) {
                                fqnSB.append(", ");
                            }
                            fqnSB.append(info.getTypes().erasure(var.asType()).toString());
                            first = false;
                        }
                        fqnSB.append(')');
                    }
                    
                    fqn = fqnSB.toString();
                }
                
                if (!IGNORE_CLASSES.contains(fqn) && IGNORE_PATTERNS.stream().noneMatch(p -> p.matcher(fqn).matches())) {
                    fqns.add(fqn);
                }
            }
            
            Collections.sort(fqns);
            
            out.write(key + ":" + fqns.toString() + "\n");
        }

        return out.toString();
    }
    
    private void doTest(String name) throws Exception {
        doTest(name, "1.5");
    }
    
    private void doTest(String name, String sourceLevel) throws Exception {
        String testPackagePath = "org/netbeans/modules/java/editor/imports/data/";
        File   testPackageFile = new File(getDataDir(), testPackagePath);
        List<FileData> files = new ArrayList<>();
        
        String[] names = testPackageFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".java"))
                    return true;

                return false;
            }
        });

        for (int cntr = 0; cntr < names.length; cntr++) {
            files.add(new FileData(testPackagePath + names[cntr],
                                   readContent(new File(testPackageFile, names[cntr]))));
        }


        doTest(testPackagePath + name,
               sourceLevel,
               files,
               readContent(getGoldenFile(getName() + "-unfiltered.pass")),
               readContent(getGoldenFile(getName() + "-filtered.pass")));
    }

    private String readContent(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            StringWriter sw = new StringWriter();
            int read;

            while ((read = r.read()) != (-1)) {
                sw.write(read);
            }
            return sw.toString();
        }
    }

    private void doTest(String name, String sourceLevel, Iterable<FileData> files, String unfilteredPass, String filteredPass) throws Exception {
        prepareTest(name, sourceLevel, files);

        DataObject testDO = DataObject.find(testSource);
        EditorCookie ec = testDO.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        Pair<Map<String, List<Element>>, Map<String, List<Element>>> candidates = new ComputeImports(info).computeCandidates();
        
        assertEquals(unfilteredPass, dump(candidates.b));
        assertEquals(filteredPass, dump(candidates.a));
    }
    
    private static final class FileData {
        public final String fileName;
        public final String content;

        public FileData(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
        }

    }

    //from CompletionTestBaseBase:
    private   final String goldenFilePath = "org/netbeans/modules/java/editor/imports/ComputeImportsTest";
    public File getGoldenFile(String goldenFileName) {
        File goldenFile = null;
        String version = System.getProperty("java.specification.version");
        for (String variant : computeVersionVariantsFor(version)) {
            goldenFile = new File(getDataDir(), "/goldenfiles/" + goldenFilePath + "/" + variant + "/" + goldenFileName);
            if (goldenFile.exists())
                break;
        }
        assertNotNull(goldenFile);
        return goldenFile;
    }

    private List<String> computeVersionVariantsFor(String version) {
        int dot = version.indexOf('.');
        version = version.substring(dot + 1);
        int versionNum = Integer.parseInt(version);
        List<String> versions = new ArrayList<>();

        for (int v = versionNum; v >= 8; v--) {
            versions.add(v != 8 ? "" + v : "1." + v);
        }

        return versions;
    }
}
