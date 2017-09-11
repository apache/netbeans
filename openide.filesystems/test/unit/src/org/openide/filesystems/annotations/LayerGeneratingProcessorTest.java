/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
    @SupportedSourceVersion(SourceVersion.RELEASE_7)
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
