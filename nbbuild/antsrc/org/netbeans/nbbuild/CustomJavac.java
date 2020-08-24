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
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.Javac13;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.JavaEnvUtils;

/**
 * Could probably be replaced with a presetdef for javac,
 * and a separate task for {@link #cleanUpStaleClasses}.
 */
public class CustomJavac extends Javac {
    private File backportingCompiler;

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

    public void setBackportingCompiler(File path) {
        this.backportingCompiler = path;
    }

    @Override
    public void execute() throws BuildException {
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

        int src = intVersion(getSource());
        int trgt = intVersion(getTarget());

        if (src > trgt && this.backportingCompiler != null && this.backportingCompiler.isFile()) {
            try {
                Class<?> mainClazz = findMainCompilerClass(this.backportingCompiler);
                super.add(new Javac13() {
                    @Override
                    public boolean execute() throws BuildException {
                        attributes.log("Using modern compiler", Project.MSG_VERBOSE);
                        Commandline cmd = setupModernJavacCommand();
                        cmd.addArguments(new String[] { "--enable-safe-preview" });

                        // Use reflection to be able to build on all JDKs >= 1.1:
                        try {
                            Object compiler = mainClazz.newInstance();
                            Method compile = mainClazz.getMethod("compile", String[].class, PrintWriter.class);
                            int result = (Integer) compile.invoke(compiler,
                                    (Object) cmd.getArguments(), new PrintWriter(System.out));
                            return result == 0;
                        } catch (Exception ex) {
                            if (ex instanceof BuildException) {
                                throw (BuildException) ex;
                            }
                            throw new BuildException("Error starting modern compiler",
                                    ex, location);
                        }
                    }

                });
            } catch (ClassNotFoundException |MalformedURLException ex) {
                throw new BuildException(ex);
            }
        }
        super.compile();
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
            File enclosing = new File(d, clazz.substring(0, i) + ".class");
            if (!enclosing.isFile()) {
                File enclosed = new File(d, clazz);
                log(clazz + " will be deleted since " + enclosing.getName() + " is missing", Project.MSG_VERBOSE);
                if (!enclosed.delete()) {
                    throw new BuildException("could not delete " + enclosed, getLocation());
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

    private static int intVersion(String version) {
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        return Integer.parseInt(version);
    }

    private static Reference<Class<?>> mainCompilerClass;
    private static synchronized Class<?> findMainCompilerClass(File jar) throws MalformedURLException, ClassNotFoundException {
        Class<?> c = mainCompilerClass == null ? null : mainCompilerClass.get();

        URLClassLoader loader = new URLClassLoader(new URL[] { jar.toURI().toURL() }, CustomJavac.class.getClassLoader());
        c = Class.forName("org.frgaal.Main", true, loader);
        mainCompilerClass = new SoftReference<>(c);
        return c;
    }

}
