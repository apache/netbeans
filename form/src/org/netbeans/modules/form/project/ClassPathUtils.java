/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.form.project;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.api.project.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * Utility methods related to classpath in projects.
 *
 * @author Tomas Pavek
 */

public class ClassPathUtils {

    /**
     * Cache the classloader instance related to the project (shared for all
     * forms from the project).
     */
    private static Map<Project,Reference<FormClassLoader>> loaders = new WeakHashMap<Project,Reference<FormClassLoader>>();

    /**
     * Make sure the project-related classloader is not forgotten by the weak
     * cache during the time a form is opened (same instance used all the time,
     * for the form, if not changed otherwise, e.g. due to project CP change).
     */
    private static Map<FileObject, FormClassLoader> formLoaders = new HashMap<FileObject,FormClassLoader>();

    /**
     * Class loading type for a class to be always loaded by the IDE's system
     * classloader (i.e. from a module). The system classloader is used even if
     * the class was requested by a class that was loaded by the project
     * classloader. This can also be used for user classes from project to link
     * with classes provided by a module for design time. (E.g. used for binding
     * validators.)
     */
    static final ClassLoadingType SYSTEM_CLASS = new ClassLoadingType("system"); // NOI18N

    /**
     * Class loading type for a class to be loaded from a module by a classloader
     * that also includes the project classpath. Can be used for special design
     * module classes that need to access project classpath (classes or resources).
     */
    static final ClassLoadingType SYSTEM_CLASS_WITH_PROJECT = new ClassLoadingType("system_with_project"); // NOI18N

    /**
     * Loads a class with a context of a project in mind (specified by arbitrary
     * file contained in the project). Typically the class is loaded from the
     * project's execution classpath unless it is a basic JDK class, or a class
     * registred as a support (system) class.
     */
    public static Class<?> loadClass(String name, FileObject fileInProject)
        throws ClassNotFoundException
    {
        return Class.forName(name, true, getFormClassLoader(fileInProject));
        // LinkageError left uncaught
    }

    public static boolean checkUserClass(String name, FileObject fileInProject) {
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        if (classPath == null)
            return false;

        String fileName = name.replace('.', '/').concat(".class"); // NOI18N
        return classPath.findResource(fileName) != null;
    }

    public static void resetFormClassLoader(Project p) {
        loaders.remove(p);
    }

    public static void releaseFormClassLoader(FileObject fileInPoject) {
        formLoaders.remove(fileInPoject);
    }

    private static FormClassLoader getFormClassLoader(FileObject fileInProject) {
        Project p = FileOwnerQuery.getOwner(fileInProject);
        Reference<FormClassLoader> ref = loaders.get(p);
        FormClassLoader fcl = ref != null ? ref.get() : null;
        ClassLoader existingProjectCL = fcl != null ? fcl.getProjectClassLoader() : null;
        ClassLoader newProjectCL = ProjectClassLoader.getUpToDateClassLoader(
                                     fileInProject, existingProjectCL);
        if (fcl == null || newProjectCL != existingProjectCL) {
            fcl = new FormClassLoader(newProjectCL);
            formLoaders.put(fileInProject, fcl);
            loaders.put(p, new WeakReference<FormClassLoader>(fcl));
        }
        return fcl;
    }
    
    // Don't use - public only because of FormLAF
    public static ClassLoader getProjectClassLoader(FileObject fileInProject) {
        return getFormClassLoader(fileInProject).getProjectClassLoader();
    }

    /**
     * @return ClassLoadingType if the class should be loaded in a special way,
     *         or null to do default loading (from project classpath)
     */
    static ClassLoadingType getClassLoadingType(String className) {
        int i = className.lastIndexOf("[L"); // NOI18N
        if (i != -1) {
            className = className.substring(i+2, className.length()-1);
        }
        for (ClassLoadingType clType : CLASS_LOADING_TYPES) {
            if (isClassLoadingType(className, clType)) {
                return clType;
            }
        }
        return null;
    }

    /** Loads class from classpath described by ClassSource object.
     * @return loaded class, null if class name in ClassSource is null
     */
    public static Class loadClass(ClassSource classSource)
        throws ClassNotFoundException
    {
        String className = classSource.getClassName();
        if (className == null)
            return null;

        ClassLoader loader = null;

        if (!classSource.hasEntries()) {
            // for loading JDK classes
            loader = Lookup.getDefault().lookup(ClassLoader.class);
        }
        else try {
            List<URL> urlList = classSource.getClasspath();
            if (urlList.size() > 0) {
                URL[] roots = new URL[urlList.size()];
                urlList.toArray(roots);
                loader = ClassPathSupport.createClassPath(roots).getClassLoader(true);
            }
            else return null;
        }
        catch (Exception ex) { // could not construct the classpath
            IllegalArgumentException iae = new IllegalArgumentException();
            ErrorManager.getDefault().annotate(iae, ex);
            throw iae;
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return loader.loadClass(classSource.getClassName());
    }
    
    public static boolean isOnClassPath(FileObject fileInProject, String className) {
        String resourceName = className.replace('.', '/') + ".class"; // NOI18N
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.COMPILE);
        if (classPath == null)
            return false;

        return classPath.findResource(resourceName) != null;
    }

    public static boolean isJava6ProjectPlatform(FileObject fileInProject) {
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.BOOT);
        if (classPath == null)
            return false;

        return classPath.findResource("javax/swing/GroupLayout.class") != null; // NOI18N
    }

    public static boolean isJava7ProjectPlatform(FileObject fileInProject) {
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.BOOT);
        if (classPath == null)
            return false;

        return classPath.findResource("javax/swing/JLayer.class") != null; // NOI18N
    }

    /** Updates project'c classpath with entries from ClassSource object.
     * @return null if operation was cancelled by user otherwise true or false
     *  if project classpath was changed or not
     */
    public static Boolean updateProject(FileObject fileInProject, ClassSource classSource)
            throws IOException {
        return updateProject(fileInProject, classSource, false);
    }

    public static Boolean updateProject(final FileObject fileInProject,
            final ClassSource classSource, boolean asynchronous)
            throws IOException {

        if (!classSource.hasEntries())
            return Boolean.FALSE; // nothing to add to project

        Project project = FileOwnerQuery.getOwner(fileInProject);
	if(project==null)
	    return Boolean.FALSE;

        Boolean updated = null;
        if (asynchronous) {
            FormUtils.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        classSource.addToProjectClassPath(fileInProject, ClassPath.COMPILE);
                    } catch (IOException ioex) {
                        FormUtils.LOGGER.log(Level.INFO, null, ioex);
                    }
                }
            });
        } else {
            String msg = getBundleString("MSG_UpdatingClassPath"); // NOI18N
            Object retVal = ProgressUtils.showProgressDialogAndRun(new ProgressRunnable<Object>() {
                @Override
                public Object run(ProgressHandle handle) {
                    try {
                        return classSource.addToProjectClassPath(fileInProject, ClassPath.COMPILE);
                    } catch (IOException ioex) {
                        return ioex;
                    }
                }
            }, msg, false);
            if (retVal instanceof IOException) {
                throw (IOException)retVal;
            } else {
                updated = (Boolean)retVal;
            }
        }
        return updated;
    }

    /** Provides description for ClassSource object usable e.g. for error
     * messages.
     */
    public static String getClassSourceDescription(ClassSource classSource) {
        if (classSource == null || !classSource.hasEntries()) {
            String className = classSource.getClassName();
            if (className != null) {
                if (className.startsWith("javax.") // NOI18N
                        || className.startsWith("java.")) // NOI18N
                    return getBundleString("MSG_StandardJDKSource"); // NOI18N
                if (className.startsWith("org.netbeans.")) // NOI18N
                    return getBundleString("MSG_NetBeansSource"); // NOI18N
            }
            return NbBundle.getMessage(ClassPathUtils.class, "MSG_UnspecifiedSource");
        }
        else {
            return classSource.getEntries().iterator().next().getDisplayName();
        }
    }

    static String getBundleString(String key) {
        return NbBundle.getBundle(ClassPathUtils.class).getString(key);
    }

    // -----
    // Registered class patterns for class loader type

    static class ClassLoadingType {
        private String folderName;
        private FileObject folder;
        private List<ClassPattern> patterns;

        private ClassLoadingType(String folderName) {
            this.folderName = folderName;
        }
    }

    private static final ClassLoadingType[] CLASS_LOADING_TYPES = {
        SYSTEM_CLASS, SYSTEM_CLASS_WITH_PROJECT
    };

    private static final String CL_LAYER_BASE = "org-netbeans-modules-form/classloader/"; // NOI18N

    private static boolean isClassLoadingType(String className, ClassLoadingType clType) {
        List<ClassPattern> list = getClassPatterns(clType);
        if (list == null) {
            return false;
        }
        for (ClassPattern cp : list) {
            switch (cp.type) {
                case (ClassPattern.CLASS):
                    if (className.equals(cp.name)) {
                        return !cp.exclude;
                    }
                    break;
                case (ClassPattern.PACKAGE):
                    if (className.startsWith(cp.name) && (className.lastIndexOf('.') <= cp.name.length())) {
                        return !cp.exclude;
                    }
                    break;
                case (ClassPattern.PACKAGE_AND_SUBPACKAGES):
                    if (className.startsWith(cp.name)) {
                        return !cp.exclude;
                    }
                    break;
            }
        }
        return false;
    }

    private static List<ClassPattern> getClassPatterns(ClassLoadingType clType) {
        List<ClassPattern> list = clType.patterns;
        if (list == null) {
            list = loadClassPatterns(getClassPatternsFolder(clType));
            clType.patterns = list;
        }
        return list;
    }

    private static FileObject getClassPatternsFolder(final ClassLoadingType clType) {
        FileObject folder = clType.folder;
        if (folder == null) {
            folder = getClassPatternsFolder(clType.folderName);
            if (folder == null) {
                return null;
            }
            clType.folder = folder;
            // in case of any change in files make all the patterns reload
            folder.addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileDataCreated(FileEvent ev) {
                    clType.patterns = null;
                    loaders.clear();
                }
                @Override
                public void fileDeleted(FileEvent ev) {
                    clType.patterns = null;
                    if (ev.getFile() == clType.folder) {
                        clType.folder.removeFileChangeListener(this);
                        clType.folder = null;
                    }
                    loaders.clear();
                }
            });
        }
        return folder;
    }

    private static FileObject getClassPatternsFolder(String folderName) {
        FileObject folder = null;
        if (folderName != null) {
            try {
                folder = FileUtil.getConfigFile(CL_LAYER_BASE + folderName);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return folder;
    }

    private static List<ClassPattern> loadClassPatterns(FileObject folder) {
        List<ClassPattern> list = new ArrayList<ClassPattern>();
        if (folder == null)
            return list;

        FileObject[] files = folder.getChildren();
        for (int i=0; i < files.length; i++) {
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(files[i].getInputStream()));
                String line = r.readLine();
                while (line != null) {
                    line = line.trim();
                    if (!line.equals("")) { // NOI18N
                        ClassPattern cp;
                        if (line.endsWith("**")) { // NOI18N
                            cp = new ClassPattern(line.substring(0, line.length()-2),
                                                  ClassPattern.PACKAGE_AND_SUBPACKAGES);
                        }
                        else if (line.endsWith("*")) { // NOI18N
                            cp = new ClassPattern(line.substring(0, line.length()-1),
                                                  ClassPattern.PACKAGE);
                        }
                        else {
                            cp = new ClassPattern(line, ClassPattern.CLASS);
                        }
                        list.add(cp);
                    }
                    line = r.readLine();
                }
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return list;
    }

    private static class ClassPattern {
        static final int CLASS = 0;
        static final int PACKAGE = 1;
        static final int PACKAGE_AND_SUBPACKAGES = 2;
        String name;
        int type;
        boolean exclude;
        
        ClassPattern(String name, int type) {
            if (name.startsWith("-")) { // NOI18N
                exclude = true;
                name = name.substring(1);
            }
            this.name = name;
            this.type = type;
        }
    }
}
