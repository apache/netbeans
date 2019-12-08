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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.netbeans.junit.NbTestCase;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.AnnotationProcessorTestUtils;

public class LayerGenerationExceptionTest extends NbTestCase {

    public LayerGenerationExceptionTest(String name) {
        super(name);
    }

    public void testFindAnnotationMirror() throws Exception {
        File src = new File(getWorkDir(), "src");
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + "(attr1=\"one\", attr2=\"two\") public class C {}");
        File dest = new File(getWorkDir(), "dest");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("p.C two"));
    }

    /* XXX not yet implemented:
    public void testFindAnnotationMirrorNested() throws Exception {
        File src = new File(getWorkDir(), "src");
        AnnotationProcessorTestUtils.makeSource(src, "p.C",
                "@" + AS.class.getCanonicalName() + "({",
                "@" + A.class.getCanonicalName() + "(attr1=\"one\", attr2=\"two\"),",
                "@" + A.class.getCanonicalName() + "(attr1=\"three\", attr2=\"four\")",
                "})",
                "public class C {}");
        File dest = new File(getWorkDir(), "dest");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err);
        String out = err.toString();
        assertTrue(out, r);
        assertTrue(out,out.contains("p.C two"));
        assertTrue(out,out.contains("p.C four"));
    }
    */

    public @interface A {
        String attr1();
        String attr2();
    }

    public @interface AS {
        A[] value();
    }

    @ServiceProvider(service=Processor.class)
    public static class AP extends LayerGeneratingProcessor {
        public @Override Set<String> getSupportedAnnotationTypes() {
            return new HashSet<String>(Arrays.asList(A.class.getCanonicalName(), AS.class.getCanonicalName()));
        }
        protected @Override boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
            if (roundEnv.processingOver()) {
                return false;
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(A.class)) {
                handle(e, e.getAnnotation(A.class));
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(AS.class)) {
                for (A a : e.getAnnotation(AS.class).value()) {
                    handle(e, a);
                }
            }
            return true;
        }
        private void handle(Element e, A a) {
            LayerGenerationException lge = new LayerGenerationException("msg", e, processingEnv, a, "attr2");
            processingEnv.getMessager().printMessage(Kind.NOTE, lge.erroneousElement + " " + (lge.erroneousAnnotationValue != null ? lge.erroneousAnnotationValue.getValue() : null));
        }
    }

}
