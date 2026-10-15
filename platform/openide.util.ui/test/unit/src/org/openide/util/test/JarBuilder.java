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

package org.openide.util.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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
        jar = Files.createTempFile(workdir.toPath(), "test", ".jar").toFile();
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
