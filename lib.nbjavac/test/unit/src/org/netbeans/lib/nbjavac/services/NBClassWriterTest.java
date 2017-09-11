/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javadoc.main.JavadocClassFinder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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

import static org.netbeans.lib.nbjavac.services.Utilities.DEV_NULL;

/**
 *
 * @author lahvac
 */
public class NBClassWriterTest extends NbTestCase {

    public NBClassWriterTest(String testName) {
        super(testName);
    }

    public void test209734() throws Exception {
        String code = "package test; public class Test { void t() { new Runnable() { public void run() {} }; } }";
        compile(code);
        testEnclosedByPackage("test", "test.Test");
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
        final String bootPath = System.getProperty("sun.boot.class.path"); //NOI18N
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));

        Context context = new Context();
        NBMessager.preRegister(context, null, DEV_NULL, DEV_NULL, DEV_NULL);
        final JavacTaskImpl ct = (JavacTaskImpl) ((JavacTool)tool).getTask(null, std, null, Arrays.asList("-bootclasspath",  bootPath, "-source", "1.6", "-target", "1.6"), null, Arrays.asList(new MyFileObject(code)), context);

        NBClassReader.preRegister(ct.getContext());
        JavadocClassFinder.preRegister(ct.getContext(), false);
        NBClassWriter.preRegister(ct.getContext());

        ct.call();
    }

    private void testEnclosedByPackage(String packageName, String... expectedClassNames) throws IOException {
        final String bootPath = System.getProperty("sun.boot.class.path"); //NOI18N
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));
        std.setLocation(StandardLocation.CLASS_PATH, Collections.singleton(workingDir));

        Context context = new Context();
        NBMessager.preRegister(context, null, DEV_NULL, DEV_NULL, DEV_NULL);
        JavacTaskImpl ct = (JavacTaskImpl)((JavacTool)tool).getTask(null, std, null, Arrays.asList("-bootclasspath",  bootPath), null, Arrays.<JavaFileObject>asList(), context);

        NBClassReader.preRegister(ct.getContext());
        JavadocClassFinder.preRegister(ct.getContext(), false);
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
