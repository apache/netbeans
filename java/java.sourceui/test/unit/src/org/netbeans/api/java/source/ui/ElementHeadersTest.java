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
package org.netbeans.api.java.source.ui;

import com.sun.source.util.TreePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;

import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author phrebejk
 */
public class ElementHeadersTest extends NbTestCase {
    
    public ElementHeadersTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    Callable<Void> prepareEnvCallback = null;
    FileObject[] classPathElements = new FileObject[0];
    List<Object> extraServicesInLookup = new ArrayList<>();
    
    private void prepareTest(String fileName, String code) throws Exception {
        List<Object> extras = new ArrayList<>();
        extras.add(JavaDataLoader.class);
        extras.addAll(extraServicesInLookup);
        SourceUtilsTestUtil.prepareTest(new String[] { 
                    "org/netbeans/modules/java/platform/resources/layer.xml",
                    "org/netbeans/modules/java/j2seplatform/resources/layer.xml" 
                }, 
                extras.toArray(new Object[0])
        );

        clearWorkDir();
        
        FileUtil.refreshAll();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);
        
        assertNotNull(dataFile);
        
        TestUtilities.copyStringToFile(dataFile, code);
        
        if (prepareEnvCallback != null) {
            prepareEnvCallback.call();
        }
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, classPathElements);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    
    protected void performTest(String fileName, String code, int pos, String format, String golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        assertEquals(golden, ElementHeaders.getHeader(path, info, format));
    }
    
    public void testField() throws Exception {
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
        performTest("test/Test.java", "package test; public class Test { int aa; }", 39, ElementHeaders.NAME, "aa");
    }
    
    public void testConstructor133774() throws Exception {
        performTest("test/Test.java", "package test; public class Test { public Test() {}}", 43, ElementHeaders.NAME, "Test");
    }
    
    public void test134664() throws Exception {
        SourceUtilsTestUtil2.disableConfinementTest();
        performTest("test/Test.java", "package test; public class Test { public Tfst {} }", 43, ElementHeaders.NAME, "Tfst");
    }
    
    private String fileContent;
    
    private TreePath pathInSource(String fragment) throws Exception {
        Path f = getDataDir().toPath().resolve("structureElement/Test.java");
        String content = String.join("\n", Files.readAllLines(f));
        int x = fragment.indexOf('^');
        if (x == -1) {
            x = 0;
        } else {
            fragment = fragment.substring(0, x) + fragment.substring(x + 1);
        }
        int pos = content.indexOf(fragment);
        assertTrue(pos >= 0);
        
        pos += x;
        
        prepareTest("test/Test.java", content);
        fileContent = content;
        TreePath path = info.getTreeUtilities().pathFor(pos);
        return path;
    }

    /**
     * Loads structureElement/Test.java and parses. Creates StructureElement from the position marked by ^ in the passed search fragment.
     * @param fragment piece of text to search for. ^ marks the caret position
     * @param acc element accessor to pass to the API
     * @return StructureElement instance
     * @throws Exception 
     */
    private StructureElement processTestSource(String fragment, ElementAcceptor acc) throws Exception {
        TreePath path = pathInSource(fragment);
        assertNotNull(path);
        Element el = info.getTrees().getElement(path);
        
        return ElementHeaders.toStructureElement(info, el, acc);
    }
    
    private Pattern compileWithSymbols(String pattern) {
        return Pattern.compile(pattern.replace("#", "(?<s>(?=.?))").replace("@", "(?<e>(?=.?))").
                replace("{", Pattern.quote("{")), Pattern.DOTALL | Pattern.MULTILINE);
    }
    /**
     * Checks that the StructureElement has proper ranges. The beginPattern is a regexp that contains # for selection start and @ for 
     * expanded start. Since the end is usually in completely different
     * @param beginPattern
     * @param endSelPattern
     * @param endPattern
     * @param se 
     */
    private void assertRanges(String beginPattern, String endSelPattern, String endPattern, StructureElement se) {
        assertNotNull(se);
        // the zero-width lookahead ".?" will match the position of the marker character. Without lookahead, the preceding character (and its position)
        // could be recorded, i.e. @public void would yield position of the space before `public`, not position of the 'p'.
        Matcher m = compileWithSymbols(beginPattern).matcher(fileContent);
        assertTrue(m.find());
        assertEquals(2, m.groupCount());
        
        int selS = m.start("s");
        int expS = m.start("e");
        
        assertEquals(selS, se.getSelectionStartOffset());
        assertEquals(expS, se.getExpandedStartOffset());

        m = compileWithSymbols(endSelPattern).matcher(fileContent);
        assertTrue(m.find());
        int selE = m.start("s");
        assertEquals(selE, se.getSelectionEndOffset());
        
        m = compileWithSymbols(endPattern).matcher(fileContent);
        assertTrue(m.find());
        int expE = m.start("e");
        assertEquals(expE, se.getExpandedEndOffset());
    }
    
    private void assertTestClass(StructureElement se) {
        assertEquals("Test", se.getName());
        assertRanges("@public class #Test", "class Test#", "}@ // end Test", se);
        assertNotNull(se.getFile());
    }
    
    /**
     * Checks that just class element is returned, checks bounds.
     */
    public void testToStructureClassShallow() throws Exception {
        StructureElement se = processTestSource("class Te^st", null);
        assertNull(se.getChildren());
        assertTestClass(se);
    }

    private void assertTestMethod(StructureElement se) {
        assertEquals("m(int param)", se.getName());
        assertEquals(StructureElement.Kind.Method, se.getKind());
        assertNotNull(se.getFile());
        
        assertRanges("@public void #m", "public void m#", "}@ // end m", se);
    }
    
    /**
     * Checks that just method element is returned, checks bounds.
     */
    public void testToStructureMethodShallow() throws Exception {
        StructureElement se = processTestSource("public void m^", null);
        assertTestMethod(se);
        assertNull(se.getChildren());
    }
    
    private void assertTestField(StructureElement se) {
        assertEquals("a", se.getName());
        assertEquals(StructureElement.Kind.Field, se.getKind());
        assertNotNull(se.getFile());
        
        assertRanges("@public String #a", "public String a#", "public String a;@", se);
    }
    
    /**
     * Checks that field element is returned with proper bounds.
     */
    public void testToStructureField() throws Exception {
        StructureElement se = processTestSource("String ^a", null);
        assertTestField(se);
    }
    
    private ElementAcceptor ALL = new ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror type) {
                // do not accept inner class
                return e.getKind() != ElementKind.CLASS;
            }
    };
    
    private List<StructureElement> sortChildren(StructureElement parent) {
        List<StructureElement> ch = new ArrayList<>(parent.getChildren());
        ch.sort(new Comparator<StructureElement>() {
            @Override
            public int compare(StructureElement o1, StructureElement o2) {
                return o1.getSelectionStartOffset() - o2.getSelectionStartOffset();
            }
        });
        return ch;
    }
    
    public void testSelectedChildren() throws Exception {
        StructureElement se = processTestSource("class Test^", ALL);
        assertTestClass(se);
        assertNotNull(se.getChildren());
        assertEquals(4, se.getChildren().size());
        
        List<StructureElement> sorted = sortChildren(se);
        
        assertTestField(sorted.get(0));
        assertTestMethod(sorted.get(1));
    }
    
    public void testAllChildrenStructure() throws Exception {
        StructureElement se = processTestSource("class Test^", new ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror type) {
                return true;
            }
        });
        assertTestClass(se);
        assertNotNull(se.getChildren());
        assertEquals(5, se.getChildren().size());

        List<StructureElement> sorted = sortChildren(se);
        
        assertTestField(sorted.get(0));
        assertTestMethod(sorted.get(1));
        
        StructureElement ce = sorted.get(2);
        assertEquals(StructureElement.Kind.Class, ce.getKind());
        assertEquals("Inner", ce.getName());
        assertNotNull(ce.getChildren());
        assertEquals(1, ce.getChildren().size());
        
        assertRanges("@static class #Inner {", "class Inner# {", "}@ // end Inner", ce);
    }
    
    public void testStructureNonLocalReference() throws Exception {
        TreePath path = pathInSource("public String ^a;");
        TypeMirror aType = info.getTrees().getTypeMirror(path);
        
        Element stringEl = info.getTypes().asElement(aType);
        StructureElement e = ElementHeaders.toStructureElement(info, stringEl, ALL);
        assertNull(e);
    }

    public void testResolveNonLocalReference() throws Exception {
        TreePath path = pathInSource("public String ^a;");
        TypeMirror aType = info.getTrees().getTypeMirror(path);
        Element stringEl = info.getTypes().asElement(aType);
        ClasspathInfo cpInfo = info.getClasspathInfo();
        final ClassPath cp = ClassPathSupport.createProxyClassPath(
            cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
            cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE),
            cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE));
        final FileObject resource = cp.findResource("java/lang/String.class");
        assertNotNull(resource);
        SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(URLMapper.findURL(cp.findOwnerRoot(resource), URLMapper.INTERNAL));
        if (r == null || r.getRoots().length == 0) {
            // skip the test, no sources for JDK.
            return;
        }
        CompletableFuture<StructureElement> f = ElementHeaders.resolveStructureElement(info, stringEl, true);
        StructureElement se = f.get();
        assertNotNull(se);
    }
    
    FileObject openideBin;
    
    /**
     * Checks that a long-running call that involves SourceJavadocAttacher can be cancelled by the client.
     * @throws Exception 
     */
    public void testUseSourceAttacher() throws Exception {
        SJAImpl sja = new SJAImpl();
        
        extraServicesInLookup.add(sja);
        
        URL cancURL = getClass().getClassLoader().getResource("org/openide/util/Cancellable.class");
        assertNotNull(cancURL);
        
        prepareEnvCallback = () -> {
            openideBin = sourceRoot.getParent().createFolder("openide-bin");
            FileObject utilDir = FileUtil.createFolder(openideBin, "org/openide/util");
            FileObject cf = utilDir.createData("Cancellable", "class");
            try (InputStream is = cancURL.openStream(); OutputStream os = cf.getOutputStream()) {
                FileUtil.copy(is, os);
            }
            classPathElements = new FileObject[] { openideBin };
            return null;
        };

        TreePath path = pathInSource("Cancellable ^cancel;");
        TypeMirror aType = info.getTrees().getTypeMirror(path);
        Element cancelEl = info.getTypes().asElement(aType);
        assertEquals(ElementKind.INTERFACE, cancelEl.getKind());
        
        sja.reset();
        CompletableFuture<StructureElement> f = ElementHeaders.resolveStructureElement(info, cancelEl, true);
        assertFalse(f.isDone());
        StructureElement se;
        
        try {
            se = f.get(500, TimeUnit.MILLISECONDS);
            fail("Should time out");
        } catch (TimeoutException ex) {
            // expected
        }
        sja.sourceLatch.countDown();
        se = f.get();
        assertNotNull(se);
    }
    
    static RequestProcessor RP = new RequestProcessor(ElementHeadersTest.class);
    
    static class SJAImpl implements SourceJavadocAttacherImplementation {
        volatile CountDownLatch sourceLatch;
        
        void reset() {
            sourceLatch = new CountDownLatch(1);
        }
        
        @Override
        public boolean attachSources(URL root, SourceJavadocAttacher.AttachmentListener listener) throws IOException {
            RP.post(() -> {
                if (sourceLatch != null) {
                    try {
                        sourceLatch.await();
                        listener.attachmentSucceeded();
                    } catch (InterruptedException ex) {
                        listener.attachmentFailed();
                    }
                } else {
                    listener.attachmentFailed();
                }
            });
            return true;
        }

        @Override
        public boolean attachJavadoc(URL root, SourceJavadocAttacher.AttachmentListener listener) throws IOException {
            return false;
        }
        
    }
}
