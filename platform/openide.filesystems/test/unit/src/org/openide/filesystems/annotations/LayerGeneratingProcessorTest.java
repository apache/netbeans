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

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.ToolProvider;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.AnnotationProcessorTestUtils;

public class LayerGeneratingProcessorTest extends NbTestCase {

    public LayerGeneratingProcessorTest(String n) {
        super(n);
    }

    public void testProcessingEnvironmentLeak() throws Exception { // #198604
        try {
            if (Modifier.isStatic(Class.forName("com.sun.tools.javac.code.Symtab").getField("byteType").getModifiers())) {
                System.err.println("Skipping testProcessingEnvironmentLeak due to old buggy version of javac");
                return;
            }
        } catch (Exception x) {
            System.err.println("Note: perhaps using non-javac compiler? " + x);
        }
        clearWorkDir();
        File src = new File(getWorkDir(), "src");
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@" + A.class.getCanonicalName() + " public class C {}");
        File dest = new File(getWorkDir(), "dest");
        assertTrue(dest.mkdirs());
        List<String> args = new ArrayList<String>();
        args.add("-classpath");
        args.add(dest + File.pathSeparator + System.getProperty("java.class.path"));
        args.add("-d");
        args.add(dest.getAbsolutePath());
        args.add("-sourcepath");
        args.add(src.getAbsolutePath());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager fm = compiler.getStandardFileManager(null, null, null);
        CompilationTask task = compiler.getTask(null, fm, null, args, Collections.singleton("p.C"), null);
        P p = new P();
        task.setProcessors(Collections.singleton(p));
        assertFalse(task.call());
        assertNotNull(p.env);
        Reference<?> r = new WeakReference<Object>(p.env);
        compiler = null;
        fm = null;
        task = null;
        p = null;
        assertGC("can collect ProcessingEnvironment", r);
    }
    public @interface A {}
    static class P extends LayerGeneratingProcessor {
        ProcessingEnvironment env;
        public @Override Set<String> getSupportedAnnotationTypes() {
            return Collections.singleton(A.class.getCanonicalName());
        }
        protected @Override boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
            env = processingEnv;
            if (roundEnv.processingOver()) {
                return false;
            }
            for (Element e : roundEnv.getElementsAnnotatedWith(A.class)) {
                layer(e).file("whatever").write();
                throw new LayerGenerationException("oops");
            }
            return true;
        }

    }

}
