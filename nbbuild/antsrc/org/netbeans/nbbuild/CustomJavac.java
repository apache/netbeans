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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.StandardLocation;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.Javac13;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JavaEnvUtils;

/**
 * Could probably be replaced with a presetdef for javac,
 * and a separate task for {@link #cleanUpStaleClasses}.
 */
public class CustomJavac extends Javac {

    public CustomJavac() {}

    private Path processorPath;
    public void addProcessorPath(Path cp) {
        processorPath = cp;
    }

    private boolean usingExplicitIncludes;
    @Override
    public void setIncludes(String includes) {
        super.setIncludes(includes);
        usingExplicitIncludes = true;
    }

    private File generatedClassesDir;

    private String maybeFork;
    @Override public void setFork(boolean f) {
        super.setFork(f);
    }
    @Override public void setExecutable(String forkExec) {
        maybeFork = forkExec;
    }

    @Override
    public void execute() throws BuildException {
        String release = getRelease();
        if (release == null || release.isEmpty()) {
            String tgr = getTarget();
            if (tgr.matches("\\d+")) {
                tgr = "1." + tgr;
            }
            if (!isBootclasspathOptionUsed()) {
                setRelease(tgr.substring(2));
            }
            String src = getSource();
            if (src.matches("\\d+")) {
                src = "1." + src;
            }
            if (!JavaEnvUtils.isAtLeastJavaVersion(src)) {
                log("Cannot handle -source " + src + " from this VM; forking " + maybeFork, Project.MSG_WARN);
                super.setFork(true);
                super.setExecutable(maybeFork);
            }
        } else {
            if (!JavaEnvUtils.isAtLeastJavaVersion(release)) {
                log("Cannot handle -release " + release + " from this VM; forking " + maybeFork, Project.MSG_WARN);
                super.setFork(true);
                super.setExecutable(maybeFork);
            }
        }
        generatedClassesDir = new File(getDestdir().getParentFile(), getDestdir().getName() + "-generated");
        if (!usingExplicitIncludes) {
            cleanUpStaleClasses();
        }
        cleanUpDependDebris();
        super.execute();
    }

    @Override
    protected void compile() {
        if (processorPath != null && processorPath.size() > 0) {
            createCompilerArg().setValue("-processorpath");
            createCompilerArg().setPath(processorPath);
        }
        createCompilerArg().setValue("-implicit:class");
        if (generatedClassesDir.isDirectory() || generatedClassesDir.mkdirs()) {
            createCompilerArg().setValue("-s");
            createCompilerArg().setFile(generatedClassesDir);
            if (generatedClassesDir.isDirectory()) {
                createSrc().setLocation(generatedClassesDir);
            }
        } else {
            log("Warning: could not create " + generatedClassesDir, Project.MSG_WARN);
        }
        try {
            Class<?> mainClazz = NbJavacLoader.findMainCompilerClass(getProject());
            if (mainClazz != null) {
                super.add(new NbJavacCompiler(mainClazz));
            }
        } catch (ClassNotFoundException | MalformedURLException | URISyntaxException ex) {
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
            }
            throw new BuildException(ex);
        }
        super.compile();
    }

    private boolean isBootclasspathOptionUsed() {
        for (String arg : getCurrentCompilerArgs()) {
            if (arg.contains("-Xbootclasspath")) {
                return true;
            }
        }
        return false;
    }

    /**
     * See issue #166888. If there are any existing class files with no matching
     * source file, assume this is an incremental build and the source file has
     * since been deleted. In that case, delete the whole classes dir. (Could
     * merely delete the stale class file, but if an annotation processor also
     * created associated resources, these may be stale too. Kill them all and
     * let JSR 269 sort it out.)
     */
    private void cleanUpStaleClasses() {
        File d = getDestdir();
        if (!d.isDirectory()) {
            return;
        }
        List<File> sources = new ArrayList<>();
        for (String s : getSrcdir().list()) {
            sources.add(new File(s));
        }
        if (generatedClassesDir.isDirectory()) {
            sources.add(generatedClassesDir);
        }
        FileSet classes = new FileSet();
        classes.setDir(d);
        classes.setIncludes("**/*.class");
        classes.setExcludes("**/*$*.class");
        String startTimeProp = getProject().getProperty("module.build.started.time");
        Date startTime;
        try {
            startTime = startTimeProp != null ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(startTimeProp) : null;
        } catch (ParseException x) {
            throw new BuildException(x);
        }
        for (String clazz : classes.getDirectoryScanner(getProject()).getIncludedFiles()) {
            if (startTime != null && new File(d, clazz).lastModified() > startTime.getTime()) {
                // Ignore recently created classes. Hack to get contrib/j2ee.blueprints and the like
                // to build; these generate classes and resources before main compilation step.
                continue;
            }
            String java = clazz.substring(0, clazz.length() - ".class".length()) + ".java";
            boolean found = false;
            for (File source : sources) {
                if (new File(source, java).isFile()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // XXX might be a false negative in case this was a nonpublic outer class
                // (could check for "ClassName.java" in bytecode to see)
                log(new File(d, clazz) + " appears to be stale, rebuilding all module sources", Project.MSG_WARN);
                Delete delete = new Delete();
                delete.setProject(getProject());
                delete.setOwningTarget(getOwningTarget());
                delete.setLocation(getLocation());
                FileSet deletables = new FileSet();
                deletables.setDir(d);
                delete.addFileset(deletables);
                delete.init();
                delete.execute();
                break;
            }
        }
    }

    @Override
    public void setErrorProperty(String errorProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUpdatedProperty(String updatedProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFailonerror(boolean fail) {
        throw new UnsupportedOperationException();
    }

    /**
     * See issue #196556. Sometimes {@code <depend>} leaves behind individual
     * class files for nested classes when their enclosing classes do not exist.
     * This can cause javac to try to read the nested classes and fail.
     */
    private void cleanUpDependDebris() {
        File d = getDestdir();
        if (!d.isDirectory()) {
            return;
        }
        FileSet classes = new FileSet();
        classes.setDir(d);
        classes.setIncludes("**/*$*.class");
        final String whiteListRaw = getProject().getProperty("nbjavac.ignore.missing.enclosing"); //NOI18N
        final String[] whiteList = whiteListRaw == null ? new String[0] : whiteListRaw.split("\\s*,\\s*");  //NOI18N
        for (String clazz : classes.getDirectoryScanner(getProject()).getIncludedFiles()) {
            if (isIgnored(whiteList, clazz)) {
                log(clazz + " ignored from the enclosing check due to ignore list", Project.MSG_VERBOSE);
                continue;
            }
            int i = clazz.indexOf('$');
            // ignore filenames that start right with '$' (separatorChar preceded), these could not be inner classes.
            if (i > 0 && clazz.charAt(i - 1) != File.separatorChar) {
                File enclosing = new File(d, clazz.substring(0, i) + ".class");
                // no inner class' filename may begin directly with '$', it must be preceded by an outer class' name.
                if (!enclosing.isFile()) {
                    File enclosed = new File(d, clazz);
                    log(clazz + " will be deleted since " + enclosing.getName() + " is missing", Project.MSG_VERBOSE);
                    if (!enclosed.delete()) {
                        throw new BuildException("could not delete " + enclosed, getLocation());
                    }
                }
            }
        }
    }

    private static boolean isIgnored(
            final String[] patterns,
            final String resource) {
        for (String pattern : patterns) {
            if (SelectorUtils.match(pattern, resource)) {
                return true;
            }
        }
        return false;
    }

    private static final class NbJavacLoader extends URLClassLoader {
        private static final String MAIN_COMPILER_CP = "nbjavac.class.path";
        private static final String MAIN_COMPILER_CLASS = "com.sun.tools.javac.Main";
        private final Map<String, Class<?>> priorityLoaded;

        private NbJavacLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.priorityLoaded = new HashMap<>();
        }

        private static synchronized Class<?> findMainCompilerClass(
                Project prj
        ) throws MalformedURLException, ClassNotFoundException, URISyntaxException {
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
                URLClassLoader loader = new NbJavacLoader(urls.toArray(new URL[0]), CustomJavac.class.getClassLoader().getParent());
                final Class<?> newCompilerClass = Class.forName(MAIN_COMPILER_CLASS, true, loader);
                assertIsolatedClassLoader(newCompilerClass, loader);
                System.getProperties().put(MAIN_COMPILER_CLASS, newCompilerClass);
                c = newCompilerClass;
            }
            return (Class<?>) c;
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
            return name.startsWith("javax.annotation.")
                    || name.startsWith("javax.tools.")
                    || name.startsWith("javax.lang.model.")
                    || name.startsWith("com.sun.source.")
                    || name.startsWith("com.sun.tools.");
        }
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
                if ("-Werror".equals(args[i])) {
                    args[i] = "-Xlint:none";
                }
            }
            try {
                Method compile = mainClazz.getMethod("compile", String[].class);
                int result = (Integer) compile.invoke(null, (Object) args);
                return result == 0;
            } catch (Exception ex) {
                attributes.log("Compiler arguments: " + Arrays.toString(args), Project.MSG_ERR);
                if (ex instanceof BuildException) {
                    throw (BuildException) ex;
                }
                throw new BuildException("Error starting modern compiler",
                        ex, location);
            }
        }
    }
}
