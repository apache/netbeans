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
package org.netbeans.nbbuild;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.tools.StandardLocation;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.Javac13;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * Helper class to allow using derivates of the JDK javac to be used to compile
 * modules. The primary use-case is to test building with nbjavac. It setups a
 * special classloader, that loads the compiler (and related) classes from the
 * classpath defined by the system property {@code nbjavac.class.path}.
 *
 * <p>
 * <em>Warning/implementation note:</em> The compiler class is cached in the
 * system properties to improve performance. This results in system properties,
 * that can't be serialized anymore and might be problematic for code not
 * prepared for non-string values in Properties.</p>
 *
 * <p>
 * A build failure with {@code nbjavac.class.path} is only a real problem if it
 * can be reproduced with the JDK itself.</p>
 */
final class CustomJavacClassLoader extends URLClassLoader {

    private static final String MAIN_COMPILER_CP = "nbjavac.class.path";
    private static final String MAIN_COMPILER_CLASS = "com.sun.tools.javac.Main";
    private final Map<String, Class<?>> priorityLoaded;

    private CustomJavacClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.priorityLoaded = new ConcurrentHashMap<>();
    }

    static synchronized Class<?> findMainCompilerClass(Project prj) throws MalformedURLException, ClassNotFoundException, URISyntaxException {
        String cp = prj.getProperty(MAIN_COMPILER_CP);
        if (cp == null) {
            return null;
        }
        Object c = System.getProperties().get(MAIN_COMPILER_CLASS);
        if (!(c instanceof Class<?>)) {
            FileSet fs = new FileSet();
            final File cpPath = new File(cp);
            if (cpPath.isAbsolute()) {
                fs.setDir(cpPath.getParentFile());
                fs.setIncludes(cpPath.getName());
            } else {
                String nball = prj.getProperty("nb_all");
                if (nball != null) {
                    fs.setDir(new File(nball));
                } else {
                    fs.setDir(prj.getBaseDir());
                }
                fs.setIncludes(cp);
            }
            List<URL> urls = new ArrayList<>();
            final DirectoryScanner scan = fs.getDirectoryScanner(prj);
            File base = scan.getBasedir();
            for (String relative : scan.getIncludedFiles()) {
                File file = new File(base, relative);
                URL url = FileUtils.getFileUtils().getFileURL(file);
                urls.add(url);
            }
            if (urls.isEmpty()) {
                throw new BuildException("Cannot find nb-javac JAR libraries in " + base + " and " + cp);
            }
            URLClassLoader loader = new CustomJavacClassLoader(urls.toArray(new URL[0]), CustomJavac.class.getClassLoader().getParent());
            final Class<?> newCompilerClass = Class.forName(MAIN_COMPILER_CLASS, true, loader);
            assertIsolatedClassLoader(newCompilerClass, loader);
            System.getProperties().put(MAIN_COMPILER_CLASS, newCompilerClass);
            c = newCompilerClass;
        }
        return (Class<?>) c;
    }

    /**
     * Creates an instance of Ant's Javac13 compiler using a class returned from
     * the {@link #findMainCompilerClass(org.apache.tools.ant.Project)}.
     *
     * @param mainClass the class previously found by the
     * {@link #findMainCompilerClass(org.apache.tools.ant.Project)} method
     * @return instance of Ant Javac compiler using the {@code mainClass}
     */
    static Javac13 createCompiler(Class<?> mainClass) {
        return new NbJavacCompiler(mainClass);
    }

    private static void assertIsolatedClassLoader(Class<?> c, URLClassLoader loader) throws ClassNotFoundException, BuildException {
        if (c.getClassLoader() != loader) {
            throw new BuildException("Class " + c + " loaded by " + c.getClassLoader() + " and not " + loader);
        }
        Class<?> stdLoc = c.getClassLoader().loadClass(StandardLocation.class.getName());
        if (stdLoc.getClassLoader() != loader) {
            throw new BuildException("Class " + stdLoc + " loaded by " + stdLoc.getClassLoader() + " and not " + loader);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isNbJavacClass(name)) {
            Class<?> clazz = priorityLoaded.get(name);
            if (clazz == null) {
                clazz = findClass(name);
                priorityLoaded.put(name, clazz);
            }
            return clazz;
        }
        return super.loadClass(name, resolve);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    private static boolean isNbJavacClass(String name) {
        return name.startsWith("javax.annotation.") || name.startsWith("javax.tools.") || name.startsWith("javax.lang.model.") || name.startsWith("com.sun.source.") || name.startsWith("com.sun.tools.");
    }

    private static final class NbJavacCompiler extends Javac13 {

        private final Class<?> mainClazz;

        NbJavacCompiler(Class<?> mainClazz) {
            this.mainClazz = mainClazz;
        }

        @Override
        public boolean execute() throws BuildException {
            attributes.log("Using modern compiler", Project.MSG_VERBOSE);
            Commandline cmd = setupModernJavacCommand();
            final String[] args = cmd.getArguments();
            boolean bootClasspath = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-Xbootclasspath/p:")) { // ide/html
                    bootClasspath = true;
                }
            }
            for (int i = 0; i < args.length; i++) {
                if (!bootClasspath) {
                    if ("-target".equals(args[i]) || "-source".equals(args[i])) {
                        args[i] = "--release";
                        if (args[i + 1].startsWith("1.")) {
                            args[i + 1] = "8";
                        }
                    }
                }
            }
            // nbjavac in version 20 contains invalid ct.sym files, which cause
            // warnings from build. Some of the modules are compiled with
            // -Werror and thus this breaks the build
            // Ater the update to version 20+ this should be removed.
            String[] args2 = new String[args.length + 1];
            args2[0] = "-Xlint:-classfile";
            System.arraycopy(args, 0, args2, 1, args.length);
            try {
                Method compile = mainClazz.getMethod("compile", String[].class);
                int result = (Integer) compile.invoke(null, (Object) args2);
                return result == 0;
            } catch (Exception ex) {
                attributes.log("Compiler arguments: " + Arrays.toString(args2), Project.MSG_ERR);
                if (ex instanceof BuildException) {
                    throw (BuildException) ex;
                }
                throw new BuildException("Error starting modern compiler",
                        ex, location);
            }
        }
    }
}
