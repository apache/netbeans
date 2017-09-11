/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * <p/>
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 * <p/>
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 * <p/>
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * <p/>
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 * <p/>
 * Contributor(s):
 * <p/>
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.openide.filesystems.annotations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
    @SupportedSourceVersion(SourceVersion.RELEASE_7)
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
