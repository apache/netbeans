/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.SelectorUtils;
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
        String src = getSource();
        if (src.matches("\\d+")) {
            src = "1." + src;
        }
        if (!JavaEnvUtils.isAtLeastJavaVersion(src)) {
            log("Cannot handle -source " + src + " from this VM; forking " + maybeFork, Project.MSG_WARN);
            super.setFork(true);
            super.setExecutable(maybeFork);
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
        List<File> sources = new ArrayList<File>();
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

}
