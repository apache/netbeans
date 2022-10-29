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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.junit.NbTestCase;

import static org.netbeans.lib.nbjavac.services.Utilities.OUT;

/**
 *
 * @author lahvac
 */
public class NBClassWriterTest extends NbTestCase {

    private final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();

    public NBClassWriterTest(String testName) {
        super(testName);
    }

    public void test209734() throws Exception {
        String code = "package test; public class Test { void t() { new Runnable() { public void run() {} }; } }";
        compile(code);
        testEnclosedByPackage("test", "test.Test");
    }

    public void testSourceRetentionAnnotations() throws Exception {
        String code = "package test;\n" +
                      "import java.lang.annotation.ElementType;\n" +
                      "import java.lang.annotation.Retention;\n" +
                      "import java.lang.annotation.RetentionPolicy;\n" +
                      "import java.lang.annotation.Target;\n" +
                      "import java.util.List;\n" +
                      "@SourceRetention\n" +
                      "public class Test {\n" +
                      "    @SourceRetention\n" +
                      "    int testField;\n" +
                      "    @SourceRetention\n" +
                      "    <@SourceRetentionTypeAnnotation T> void testMethod(@SourceRetention int testParam) {\n" +
                      "    }\n" +
                      "}\n" +
                      "@Retention(RetentionPolicy.SOURCE)\n" +
                      "@interface SourceRetention {}\n"+
                      "@Retention(RetentionPolicy.SOURCE)\n" +
                      "@Target(ElementType.TYPE_USE)\n" +
                      "@interface SourceRetentionTypeAnnotation {}\n";
        compile(code);

        Context context = new Context();
        NBLog.preRegister(context, OUT);
        JavacTaskImpl ct = (JavacTaskImpl) ((JavacTool)tool).getTask(null, null, null, Arrays.asList("-source", "1.7", "-target", "1.7", "-classpath", workingDir.toString()), null, Arrays.asList(new MyFileObject("")), context);

        NBClassReader.preRegister(ct.getContext());
        NBClassWriter.preRegister(ct.getContext());

        ct.enter();

        TypeElement test = ct.getElements().getTypeElement("test.Test");

        assertNotNull(test);

        checkHasAnnotation(test, "test.SourceRetention");
        checkHasAnnotation(ElementFilter.fieldsIn(test.getEnclosedElements()).get(0), "test.SourceRetention");
        ExecutableElement method = ElementFilter.methodsIn(test.getEnclosedElements()).get(0);
        checkHasAnnotation(method, "test.SourceRetention");
        checkHasAnnotation(method.getParameters().get(0), "test.SourceRetention");
        checkHasAnnotation(method.getTypeParameters().get(0).getAnnotationMirrors(), "test.SourceRetentionTypeAnnotation");
    }

    private void checkHasAnnotation(Element element, String annotationType) {
        checkHasAnnotation(element.getAnnotationMirrors(), annotationType);
    }

    private void checkHasAnnotation(List<? extends AnnotationMirror> annotations, String annotationType) {
        boolean seenAnnotation = false;

        for (AnnotationMirror am : annotations) {
            Name actualAnnotation = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
            if (actualAnnotation.contentEquals(annotationType)) {
                seenAnnotation = true;
                break;
            }
        }

        if (!seenAnnotation) {
            fail();
        }
    }

    //<editor-fold defaultstate="collapsed" desc=" Test Infrastructure ">
    private static class MyFileObject extends SimpleJavaFileObject {
        private String text;

        public MyFileObject(String text) {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
            this.text = text;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    private File workingDir;

    @Override
    protected void setUp() throws Exception {
        workingDir = getWorkDir();
    }

    private void compile(String code) throws Exception {
        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));

        Context context = new Context();
        NBLog.preRegister(context, OUT);
        final JavacTaskImpl ct = (JavacTaskImpl) ((JavacTool)tool).getTask(null, std, null, Arrays.asList("-source", "1.8", "-target", "1.8"), null, Arrays.asList(new MyFileObject(code)), context);

        NBClassReader.preRegister(ct.getContext());
        NBClassWriter.preRegister(ct.getContext());

        assertTrue(ct.call());
    }

    private void testEnclosedByPackage(String packageName, String... expectedClassNames) throws IOException {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));
        std.setLocation(StandardLocation.CLASS_PATH, Collections.singleton(workingDir));

        Context context = new Context();
        NBLog.preRegister(context, OUT);
        JavacTaskImpl ct = (JavacTaskImpl)((JavacTool)tool).getTask(null, std, null, Arrays.asList("-source", "1.8", "-target", "1.8"), null, Arrays.<JavaFileObject>asList(), context);

        NBClassReader.preRegister(ct.getContext());
        NBClassWriter.preRegister(ct.getContext());
        
        PackageElement pack = ct.getElements().getPackageElement(packageName);
        Set<String> actualClassNames = new HashSet<String>();

        for (TypeElement te : ElementFilter.typesIn(pack.getEnclosedElements())) {
            actualClassNames.add(ct.getElements().getBinaryName(te).toString());
        }

        if (!new HashSet<String>(Arrays.asList(expectedClassNames)).equals(actualClassNames)) {
            throw new AssertionError(actualClassNames.toString());
        }
    }
    //</editor-fold>
}
