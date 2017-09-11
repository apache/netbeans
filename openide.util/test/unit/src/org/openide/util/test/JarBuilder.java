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

package org.openide.util.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.junit.NbTestCase;

// XXX could add methods to add a JAR manifest, include text or binary resources, ...

/**
 * Builder for compiling some source files and packing the result into a JAR.
 */
public final class JarBuilder {

    private final File src, dest, jar;
    private List<File> classpath;

    /**
     * @param workdir as in {@link NbTestCase#getWorkDir}
     */
    public JarBuilder(File workdir) throws Exception {
        jar = File.createTempFile("test", ".jar", workdir);
        String n = jar.getName().replaceAll("^test|[.]jar$", "");
        src = new File(workdir, "src" + n);
        dest = new File(workdir, "classes" + n);
    }

    /**
     * Optional classpath for compiling sources.
     * If unspecified, use Java classpath of test.)
     */
    public JarBuilder classpath(File... cp) {
        if (classpath == null) {
            classpath = new ArrayList<File>();
        }
        classpath.addAll(Arrays.asList(cp));
        return this;
    }

    /**
     * Adds a source file to compile.
     * @see AnnotationProcessorTestUtils#makeSource
     */
    public JarBuilder source(String clazz, String... content) throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, clazz, content);
        return this;
    }

    /**
     * Compiles sources and creates resulting JAR.
     * @return the created JAR file
     */
    public File build() throws Exception {
        if (!AnnotationProcessorTestUtils.runJavac(src, null, dest, classpath != null ? classpath.toArray(new File[0]) : null, null)) {
            throw new Exception("compilation failed");
        }
        Map<String,byte[]> data = new TreeMap<String,byte[]>();
        scan(data, dest, "");
        OutputStream os = new FileOutputStream(jar);
        try {
            TestFileUtils.writeZipFile(os, data);
        } finally {
            os.close();
        }
        return jar;
    }

    private static void scan(Map<String,byte[]> data, File d, String prefix) throws Exception {
        for (File kid : d.listFiles()) {
            String prefixName = prefix + kid.getName();
            if (kid.isDirectory()) {
                scan(data, kid, prefixName + '/');
            } else {
                data.put(prefixName, TestFileUtils.readFileBin(kid));
            }
        }
    }

}
