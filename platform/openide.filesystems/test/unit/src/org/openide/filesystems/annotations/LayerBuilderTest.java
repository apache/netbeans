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

package org.openide.filesystems.annotations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.BaseUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.TestFileUtils;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

public class LayerBuilderTest extends NbTestCase {

    public LayerBuilderTest(String n) {
        super(n);
    }

    private Document doc;
    private LayerBuilder b;
    private File src;
    private File dest;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        doc = XMLUtil.createDocument("filesystem", null, null, null);
        b = new LayerBuilder(doc, null, null);
        assertEquals("<filesystem/>", dump());
        src = new File(getWorkDir(), "src");
        dest = new File(getWorkDir(), "dest");
    }

    private String dump() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        return clean(baos.toString("UTF-8"));
    }

    private static String clean(String layer) {
        return layer.
                replace('"', '\'').
                 // can be on same line try to remove only the element
                replaceFirst("<\\?xml version='1\\.0' encoding='UTF-8'\\?>", "").
                replaceFirst("<!DOCTYPE [^>]+>", "").
                replaceAll("\r?\n *", "");
    }

    public void testBasicFiles() throws Exception {
        b.file("Menu/File/x.instance").stringvalue("instanceClass", "some.X").write().
                file("Menu/Edit/y.instance").stringvalue("instanceClass", "some.Y").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'><attr name='instanceClass' stringvalue='some.X'/></file></folder>" +
                "<folder name='Edit'><file name='y.instance'><attr name='instanceClass' stringvalue='some.Y'/></file></folder>" +
                "</folder></filesystem>", dump());
    }

    public void testContent() throws Exception {
        b.file("a.txt").contents("some text here...").write().
                file("b.xml").url("/resources/b.xml").write();
        assertEquals("<filesystem><file name='a.txt'><![CDATA[some text here...]]></file>" +
                "<file name='b.xml' url='/resources/b.xml'/></filesystem>", dump());
    }

    public void testOverwritingAttribute() throws Exception {
        b.file("Menu/File/x.instance").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'/></folder>" +
                "</folder></filesystem>", dump());
        b.file("Menu/File/x.instance").stringvalue("instanceClass", "some.X").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'><attr name='instanceClass' stringvalue='some.X'/></file></folder>" +
                "</folder></filesystem>", dump());
        b.file("Menu/File/x.instance").stringvalue("instanceClass", "another.X").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'><attr name='instanceClass' stringvalue='another.X'/></file></folder>" +
                "</folder></filesystem>", dump());
        b.file("Menu/File/x.instance").stringvalue("displayName", "Hello").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'><attr name='instanceClass' stringvalue='another.X'/><attr name='displayName' stringvalue='Hello'/></file></folder>" +
                "</folder></filesystem>", dump());
    }

    public void testOverwritingURL() throws Exception {
        b.file("f.xml").url("/f1.xml").write();
        assertEquals("<filesystem><file name='f.xml' url='/f1.xml'/></filesystem>", dump());
        b.file("f.xml").url("/f2.xml").write();
        assertEquals("<filesystem><file name='f.xml' url='/f2.xml'/></filesystem>", dump());
    }

    public void testOverwritingContents() throws Exception {
        b.file("f.txt").contents("hello").write();
        assertEquals("<filesystem><file name='f.txt'><![CDATA[hello]]></file></filesystem>", dump());
        b.file("f.txt").contents("goodbye").write();
        assertEquals("<filesystem><file name='f.txt'><![CDATA[goodbye]]></file></filesystem>", dump());
    }

    public void testShadows() throws Exception {
        LayerBuilder.File orig = b.file("Actions/System/some-Action.instance");
        orig.write();
        b.shadowFile(orig.getPath(), "Menu/File", null).write();
        b.shadowFile(orig.getPath(), "Shortcuts", "C-F6").write();
        assertEquals("<filesystem>" +
                "<folder name='Actions'><folder name='System'><file name='some-Action.instance'/></folder></folder>" +
                "<folder name='Menu'><folder name='File'><file name='some-Action.shadow'>" +
                "<attr name='originalFile' stringvalue='Actions/System/some-Action.instance'/></file></folder></folder>" +
                "<folder name='Shortcuts'><file name='C-F6.shadow'>" +
                "<attr name='originalFile' stringvalue='Actions/System/some-Action.instance'/></file></folder>" +
                "</filesystem>", dump());
    }

    public void testSerialValue() throws Exception {
        b.file("x").serialvalue("a", new byte[] {0, 10, 100, (byte) 200}).write();
        assertEquals("<filesystem><file name='x'><attr name='a' serialvalue='000A64C8'/></file></filesystem>", dump());
    }

    public void testURIs() throws Exception {
        LayerBuilder.File f = b.file("x").urlvalue("a", "../rel").urlvalue("b", "/abs").urlvalue("c", "nbresloc:/proto");
        try {
            f.urlvalue("bogus", ":not:a:URI");
            fail();
        } catch (LayerGenerationException x) {/* right */}
        try {
            f.urlvalue("bogus", "something:opaque");
            fail();
        } catch (LayerGenerationException x) {/* right */}
        f.write();
        assertEquals("<filesystem><file name='x'>" +
                "<attr name='a' urlvalue='../rel'/>" +
                "<attr name='b' urlvalue='/abs'/>" +
                "<attr name='c' urlvalue='nbresloc:/proto'/>" +
                "</file></filesystem>", dump());
    }

    public void testFolders() throws Exception {
        b.file("x/y").write();
        b.folder("x/z").stringvalue("a", "v").write();
        b.folder("x").write();
        assertEquals("<filesystem><folder name='x'>" +
                "<file name='y'/>" +
                "<folder name='z'><attr name='a' stringvalue='v'/></folder>" +
                "</folder></filesystem>", dump());
    }

    public void testRedundantNames() throws Exception {
        b.folder("f1/f2").write();
        b.folder("f2").write();
        assertEquals("<filesystem><folder name='f1'>" +
                "<folder name='f2'/>" +
                "</folder><folder name='f2'/></filesystem>", dump());
    }

    public void testFolderAttributes() throws Exception {
        b.folder("f").intvalue("a", 7).write();
        b.file("f/x").write();
        b.folder("f").stringvalue("a", "v").write();
        assertEquals("<filesystem><folder name='f'>" +
                "<file name='x'/>" +
                "<attr name='a' stringvalue='v'/>" +
                "</folder></filesystem>", dump());
    }

    public void testOriginatingElementComments() throws Exception {
        b = new LayerBuilder(doc, new Element() {
            public @Override ElementKind getKind() {
                return ElementKind.OTHER;
            }
            public @Override String toString() {
                return "originating.Type";
            }
            public @Override TypeMirror asType() {return null;}
            public @Override List<? extends AnnotationMirror> getAnnotationMirrors() {return null;}
            public @Override <A extends Annotation> A getAnnotation(Class<A> annotationType) {return null;}
            public @Override Set<Modifier> getModifiers() {return null;}
            public @Override Name getSimpleName() {return null;}
            public @Override Element getEnclosingElement() {return null;}
            public @Override List<? extends Element> getEnclosedElements() {return null;}
            public @Override <R, P> R accept(ElementVisitor<R, P> v, P p) {return null;}
            @Override
            public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
                return (A[]) Array.newInstance(annotationType, 0);
            }
        }, null);
        b.folder("f").write();
        assertEquals("<filesystem><folder name='f'><!--originating.Type--></folder></filesystem>", dump());
        // #180154: do not repeat after an incremental build
        b.folder("f").write();
        assertEquals("<filesystem><folder name='f'><!--originating.Type--></folder></filesystem>", dump());
    }

    public void testSourcePath() throws Exception { // #181355
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + "(displayName=\"#label\") public class C {}");
        TestFileUtils.writeFile(new File(dest, "p/Bundle.properties"), "label=hello");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        File layer = new File(dest, "META-INF/generated-layer.xml");
        assertEquals("<filesystem><file name='whatever'>" +
                "<!--p.C--><attr bundlevalue='p.Bundle#label' name='displayName'/>" +
                "</file></filesystem>",
                clean(TestFileUtils.readFile(layer)));
    }

    public void testMissingBundleError() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + "(displayName=\"#nonexistent\") public class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("p/Bundle.properties"));
    }

    public void testMissingBundleKeyError() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + "(displayName=\"#nonexistent\") public class C {}");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "label=hello");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("nonexistent"));
    }

    public void testBundleKeyDefinedUsingMessages() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + "(displayName=\"#k\") @org.openide.util.NbBundle.Messages(\"k=v\") public class C {}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "public class C2 {@" + A.class.getCanonicalName() + "(displayName=\"#k2\") @org.openide.util.NbBundle.Messages(\"k2=v\") String f = null;}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, null));
    }

    /**
     * Checks behaviour of annotations on {@link LayerGeneratingProcessor} subclasses.
     * When not annnotated, the compilation should not produce a warning, as the base class
     * report {@link SourceVersion#latest}. VP processor is annotated by an obsolete version,
     * so a warning will be printed for it.
     * 
     * @throws Exception 
     */
    public void testWarningsFromProcessors() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + "(displayName=\"#k\") @org.openide.util.NbBundle.Messages(\"k=v\") public class C {}");
        File j = TestFileUtils.writeZipFile(new File(getWorkDir(), "cp.jar"), "other/x1:x1");
        TestFileUtils.writeFile(new File(src, "p/resources/x2"), "x2");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        AnnotationProcessorTestUtils.runJavac(src, null, dest, 
                new File[] {j, BaseUtilities.toFile(LayerBuilderTest.class.getProtectionDomain().getCodeSource().getLocation().toURI())}, 
                err,
                "8");
        String msgs = err.toString();
        boolean vpProcessorWarned = false;
        for (String m : msgs.split("\n")) {
            if (m.startsWith("warning: Supported source version ")) {
                int procIndex = m.indexOf("LayerBuilderTest$");
                if (procIndex == -1) {
                    // perhaps some other processor
                    continue;
                }
                assertTrue("Unexpected warning: " + m, m.contains("LayerBuilderTest$VP'"));
                vpProcessorWarned = true;
            }
        }
        assertTrue(vpProcessorWarned);
    }

    public @interface A {String displayName();}

    @ServiceProvider(service=Processor.class)
    public static class AP extends LayerGeneratingProcessor {
        public @Override Set<String> getSupportedAnnotationTypes() {
            return Collections.singleton(A.class.getCanonicalName());
        }
        protected @Override boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
            if (roundEnv.processingOver()) {
                return false;
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(A.class)) {
                A a = e.getAnnotation(A.class);
                layer(e).file("whatever").bundlevalue("displayName", a.displayName()).write();
            }
            return true;
        }
    }

    public void testAbsolutizeAndValidateResourcesExistent() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + V.class.getCanonicalName() + "(r1=\"other/x1\", r2=\"resources/x2\") public class C {}");
        File j = TestFileUtils.writeZipFile(new File(getWorkDir(), "cp.jar"), "other/x1:x1");
        TestFileUtils.writeFile(new File(src, "p/resources/x2"), "x2");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean status = AnnotationProcessorTestUtils.runJavac(src, null, dest, new File[] {j, BaseUtilities.toFile(LayerBuilderTest.class.getProtectionDomain().getCodeSource().getLocation().toURI())}, err);
        String msgs = err.toString();
        assertTrue(msgs, status);
        assertTrue(msgs, msgs.contains("r1=x1"));
        assertTrue(msgs, msgs.contains("r2=x2"));
        FileObject f = new XMLFileSystem(BaseUtilities.toURI(new File(dest, "META-INF/generated-layer.xml")).toURL()).findResource("f");
        assertNotNull(f);
        assertEquals("other/x1", f.getAttribute("r1"));
        assertEquals("p/resources/x2", f.getAttribute("r2"));
    }

    public void testValidateResourceNonexistent() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + V.class.getCanonicalName() + "(r1=\"other/x1\", r2=\"resourcez/x2\") public class C {}");
        File j = TestFileUtils.writeZipFile(new File(getWorkDir(), "cp.jar"), "other/x1:x1");
        TestFileUtils.writeFile(new File(src, "p/resources/x2"), "x2");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean status = AnnotationProcessorTestUtils.runJavac(src, null, dest, new File[] {j, BaseUtilities.toFile(LayerBuilderTest.class.getProtectionDomain().getCodeSource().getLocation().toURI())}, err);
        String msgs = err.toString();
        assertFalse(msgs, status);
        assertTrue(msgs, msgs.contains("resourcez"));
        assertTrue(msgs, msgs.contains("r1=x1"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + V.class.getCanonicalName() + "(r1=\"othr/x1\", r2=\"resources/x2\") public class C {}");
        err = new ByteArrayOutputStream();
        status = AnnotationProcessorTestUtils.runJavac(src, null, dest, new File[] {j, BaseUtilities.toFile(LayerBuilderTest.class.getProtectionDomain().getCodeSource().getLocation().toURI())}, err);
        msgs = err.toString();
        assertFalse(msgs, status);
        assertTrue(msgs, msgs.contains("othr"));
    }

    // XXX verify that CLASS_OUTPUT may be used as well

    public @interface V {
        /** absolute, may be in classpath */ String r1();
        /** relative, must be in sourcepath */ String r2();
    }
    @ServiceProvider(service=Processor.class)
    // this processor has deliberately @SupportedSourceVersion left and obsolete, a test checks this
    @SupportedSourceVersion(SourceVersion.RELEASE_7)
    public static class VP extends LayerGeneratingProcessor {
        public @Override Set<String> getSupportedAnnotationTypes() {
            return Collections.singleton(V.class.getCanonicalName());
        }
        protected @Override boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
            if (roundEnv.processingOver()) {
                return false;
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(V.class)) {
                V v = e.getAnnotation(V.class);
                LayerBuilder b = layer(e);
                LayerBuilder.File f = b.file("f");
                String r2 = LayerBuilder.absolutizeResource(e, v.r2());
                try {
                    try {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "r1=" + b.validateResource(v.r1(), e, v, "r1", true).getCharContent(true));
                    } catch (FileNotFoundException x) {
                        // OK, JDK 6, ignore
                    }
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "r2=" + b.validateResource(r2, e, v, "r2", false).getCharContent(true));
                } catch (IOException x) {
                    throw new LayerGenerationException(x.toString(), e, processingEnv, v);
                }
                f.stringvalue("r1", v.r1());
                f.stringvalue("r2", r2);
                f.write();
            }
            return true;
        }
    }

    public void testInstantiableClassOrMethod() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + I.class.getCanonicalName() + " public class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("Serializable"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + I.class.getCanonicalName() + " class C implements java.io.Serializable {}");
        err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("public"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + I.class.getCanonicalName() + " public class C implements java.io.Serializable {public C(int x) {}}");
        err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("constructor"));
        // XXX no-arg ctor must be public
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + I.class.getCanonicalName() + " public abstract class C implements java.io.Serializable {}");
        err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("abstract"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + I.class.getCanonicalName() + " public interface C extends java.io.Serializable {}");
        err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("instance"));
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "public class C {@" + I.class.getCanonicalName() + " public class N implements java.io.Serializable {}}");
        err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("static"));
        // XXX test factory methods
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + I.class.getCanonicalName() + " public class C implements java.io.Serializable {}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "public class C {@" + I.class.getCanonicalName() + " public static class N implements java.io.Serializable {}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
    }

    public @interface I {}
    @ServiceProvider(service=Processor.class)
    public static class IP extends LayerGeneratingProcessor {
        public @Override Set<String> getSupportedAnnotationTypes() {
            return Collections.singleton(I.class.getCanonicalName());
        }
        protected @Override boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
            if (roundEnv.processingOver()) {
                return false;
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(I.class)) {
                layer(e).instanceFile("stuff", null, Serializable.class).write();
            }
            return true;
        }
    }

}
