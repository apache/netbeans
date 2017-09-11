/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.project;

import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.jshell.launch.PropertyNames;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public final class ShellProjectUtils {
    /**
     * Determines if the project wants to launch a JShell. 
     * @param p the project
     * @return true, if JShell support is enabled in the active configuration.
     */
    public static boolean isJShellRunEnabled(Project p) {
        J2SEPropertyEvaluator  prjEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (prjEval != null) {
            return Boolean.parseBoolean(prjEval.evaluator().evaluate("${" + PropertyNames.JSHELL_ENABLED +"}"));
        }
        return false;
    }
    
    /**
     * Determines a Project given a debugger session. Acquires a baseDir from the
     * debugger and attempts to find a project which owns it. May return {@code null{
     * @param s
     * @return project or {@code null}.
     */
    public static Project getSessionProject(Session s) {
        Map m = s.lookupFirst(null, Map.class);
        if (m == null) {
            return null;
        }
        Object bd = m.get("baseDir"); // NOI18N
        if (bd instanceof File) {
            FileObject fob = FileUtil.toFileObject((File)bd);
            if (fob == null || !fob.isFolder()) {
                return null;
            }
            try {
                Project p = ProjectManager.getDefault().findProject(fob);
                return p;
            } catch (IOException | IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return null;
    }

    public static boolean isNormalRoot(SourceGroup sg) {
        return UnitTestForSourceQuery.findSources(sg.getRootFolder()).length == 0;
    }

    public static JavaPlatform findPlatform(ClassPath bootCP) {
        Set<URL> roots = to2Roots(bootCP);
        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            Set<URL> platformRoots = to2Roots(platform.getBootstrapLibraries());
            if (platformRoots.containsAll(roots)) {
                return platform;
            }
        }
        return null;
    }

    public static Set<URL> to2Roots(ClassPath bootCP) {
        Set<URL> roots = new HashSet<>();
        for (ClassPath.Entry e : bootCP.entries()) {
            roots.add(e.getURL());
        }
        return roots;
    }
    
    /**
     * Returns set of modules imported by the project. Adds to the passed collection
     * if not null. Module names from `required' clause will be returned
     * 
     * @param project the project
     * @param in optional; the collection
     * @return original collection or a new one with imported modules added
     */
    public static Collection<String> findProjectImportedModules(Project project, Collection<String> in) {
        Collection<String> result = in != null ? in : new HashSet<>();
        if (project == null) {
            return result;
        }
        
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                ClasspathInfo cpi = ClasspathInfo.create(sg.getRootFolder());
                ClassPath mcp = cpi.getClassPath(PathKind.COMPILE);
                
                for (FileObject r : mcp.getRoots()) {
                    URL u = URLMapper.findURL(r, URLMapper.INTERNAL);
                    String modName = SourceUtils.getModuleName(u);
                    if (modName != null) {
                        result.add(modName);
                    }
                }
            }
        }
        
        return result;
    }
    
    public static Set<String>   findProjectModules(Project project, Set<String> in) {
        Set<String> result = in != null ? in : new HashSet<>();
        if (project == null) {
            return result;
        }
        
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                URL u = URLMapper.findURL(sg.getRootFolder(), URLMapper.INTERNAL);
                BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                for (URL u2 : r.getRoots()) {
                    String modName = SourceUtils.getModuleName(u2, true);
                    if (modName != null) {
                        result.add(modName);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Collects project modules and packages from them.
     * For each modules, provides a list of (non-empty) packages from that module.
     * @param project
     * @return 
     */
    public static Map<String, Collection<String>>   findProjectModulesAndPackages(Project project) {
        Map<String, Collection<String>> result = new HashMap<>();
        if (project == null) {
            return result;
        }
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                URL u = URLMapper.findURL(sg.getRootFolder(), URLMapper.INTERNAL);
                BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                for (URL u2 : r.getRoots()) {
                    String modName = SourceUtils.getModuleName(u2, true);
                    if (modName != null) {
                        FileObject root = URLMapper.findFileObject(u);
                        Collection<String> pkgs = getPackages(root); //new HashSet<>();
                        if (!pkgs.isEmpty()) {
                            Collection<String> oldPkgs = result.get(modName);
                            if (oldPkgs != null) {
                                oldPkgs.addAll(pkgs);
                            } else {
                                result.put(modName, pkgs);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private static Collection<String> getPackages(FileObject root) {
        ClasspathInfo cpi = ClasspathInfo.create(root);
        // create CPI from just the single source root, to avoid packages from other
        // modules
        ClasspathInfo rootCpi = new ClasspathInfo.Builder(
                cpi.getClassPath(PathKind.BOOT)).
                setClassPath(cpi.getClassPath(PathKind.COMPILE)).
                setModuleSourcePath(cpi.getClassPath(PathKind.MODULE_SOURCE)).
                setModuleCompilePath(cpi.getClassPath(PathKind.MODULE_COMPILE)).
                setSourcePath(
                    ClassPathSupport.createClassPath(root)
                ).build();
        
        Collection<String> pkgs = new HashSet<>(rootCpi.getClassIndex().getPackageNames("", false, 
                Collections.singleton(SearchScope.SOURCE)));
        pkgs.remove(""); // NOI18N
        return pkgs;
    }
    
    public static boolean isModularProject(Project project) {
        if (project == null) { 
            return false;
        }
        JavaPlatform platform = findPlatform(project);
        if (platform == null || !isModularJDK(platform)) {
            return false;
        }
        String s = SourceLevelQuery.getSourceLevel(project.getProjectDirectory());
        if (!(s != null && new SpecificationVersion("9").compareTo(new SpecificationVersion(s)) <= 0)) {
            return false;
        }
        // find module-info.java
        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                if (sg.getRootFolder().getFileObject("module-info.java") != null) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isModularJDK(JavaPlatform pl) {
        if (pl != null) {
            Specification plSpec = pl.getSpecification();
            SpecificationVersion jvmversion = plSpec.getVersion();
            if (jvmversion.compareTo(new SpecificationVersion("9")) >= 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns classpath to execute the application merged from all source roots of a project.
     * 
     * @param project
     * @return 
     */
    public static ClassPath projectRuntimeModulePath(Project project) {
        List<ClassPath> delegates = new ArrayList<>();
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            ClassPath del = ClassPath.getClassPath(sg.getRootFolder(), JavaClassPathConstants.MODULE_EXECUTE_PATH); 
            if (del != null && !del.entries().isEmpty()) {
                delegates.add(del);
            }
        }
        return ClassPathSupport.createProxyClassPath(delegates.toArray(new ClassPath[delegates.size()]));
    }
    
    public static ClassPath projecRuntimeClassPath(Project project) {
        if (project == null) {
            return null;
        }
        boolean modular = isModularProject(project);
        List<ClassPath> delegates = new ArrayList<>();
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (!isNormalRoot(sg)) {
                continue;
            }
            ClassPath del = null; 
            if (modular) {
                del = ClassPath.getClassPath(sg.getRootFolder(), JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH);
            } else {
                del = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE); 
            }
            if (del != null && !del.entries().isEmpty()) {
                delegates.add(del);
            }
        }
        return ClassPathSupport.createProxyClassPath(delegates.toArray(new ClassPath[delegates.size()]));
    }
    
    public static List<String[]> compilerPathOptions(Project project) {
        List<String[]> result = new ArrayList<>();
        List<String> exportMods = new ArrayList<>(
                ShellProjectUtils.findProjectImportedModules(project, 
                    ShellProjectUtils.findProjectModules(project, null))
        );
        ShellProjectUtils.findProjectModules(project, null);
        boolean modular = isModularJDK(findPlatform(project));
        if (exportMods.isEmpty() || !modular) {
            return null;
        }
        List<String> addReads = new ArrayList<>();
        Collections.sort(exportMods);
        result.add(new String[] { "--add-modules", String.join(",", exportMods) });
        
        // now export everything from the project:
        Map<String, Collection<String>> packages = ShellProjectUtils.findProjectModulesAndPackages(project);
        for (Map.Entry<String, Collection<String>> en : packages.entrySet()) {
            String p = en.getKey();
            Collection<String> vals = en.getValue();

            for (String v : vals) {
                result.add(new String[] { "--add-exports", String.format("%s/%s=ALL-UNNAMED", p, v)});
            }
        }
        return result;
    }
    
    public static List<String> launchVMOptions(Project project) {
        List<String> exportMods = new ArrayList<>(
                ShellProjectUtils.findProjectImportedModules(project, 
                    ShellProjectUtils.findProjectModules(project, null))
        );
        ShellProjectUtils.findProjectModules(project, null);
        boolean modular = isModularJDK(findPlatform(project));
        if (exportMods.isEmpty() || !modular) {
            return null;
        }
        List<String> addReads = new ArrayList<>();
        exportMods.add("jdk.jshell");
        Collections.sort(exportMods);
        addReads.add("--add-modules " + String.join(",", exportMods));
        addReads.add("--add-reads jdk.jshell=ALL-UNNAMED"); // NOI18N
        
        // now export everything from the project:
        Map<String, Collection<String>> packages = ShellProjectUtils.findProjectModulesAndPackages(project);
        for (Map.Entry<String, Collection<String>> en : packages.entrySet()) {
            String p = en.getKey();
            Collection<String> vals = en.getValue();

            for (String v : vals) {
                addReads.add(String.format("--add-exports %s/%s=ALL-UNNAMED", 
                        p, v));
            }
        }
        return addReads;
    }
    
    public static FileObject findProjectRoots(Project project, List<URL> urls) {
        if (project == null) {
            return null;
        }
        FileObject ret = null;
        Set<URL> knownURLs = new HashSet<>();
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (org.netbeans.modules.jshell.project.ShellProjectUtils.isNormalRoot(sg)) {
                if (urls != null) {
                    URL u = URLMapper.findURL(sg.getRootFolder(), URLMapper.INTERNAL);
                    BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                    for (URL ru : r.getRoots()) {
                        if (knownURLs.add(ru)) {
                            urls.add(ru);
                        }
                    }
                }
                if (ret == null) {
                    ret = sg.getRootFolder();
                }
            }
        }
        return ret;
    }
    
    public static JavaPlatform findPlatform(Project project) {
        FileObject ref = findProjectRoots(project, null);
        if (ref == null) {
            return null;
        }
        JavaPlatform platform = findPlatform(ClassPath.getClassPath(ref, ClassPath.BOOT));
        return platform != null ? platform : JavaPlatform.getDefault();
    }
}
