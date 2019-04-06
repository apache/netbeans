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

package org.netbeans.modules.gradle.java;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ReplaceTokenProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public class GradleJavaTokenProvider implements ReplaceTokenProvider {

    private static final Set<String> SUPPORTED = Collections.unmodifiableSet(new HashSet(Arrays.asList(
            "selectedClass",       //NOI18N
            "selectedMethod",      //NOI18N
            "selectedPackage",     //NOI18N
            "affectedBuildTasks"   //NOI18N
    )));

    final Project project;

    public GradleJavaTokenProvider(Project project) {
        this.project = project;
    }

    @Override
    public Set<String> getSupportedTokens() {
        return SUPPORTED;
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        Map<String, String> ret = new HashMap<>();
        processSelectedPackageAndClass(ret, context);
        processSelectedMethod(ret, context);
        processSourceSets(ret, context);
        return ret;
    }

    private void processSelectedPackageAndClass(final Map<String, String> map, Lookup context) {
        FileObject fo = RunUtils.extractFileObjectfromLookup(context);
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if ((gjp != null) && (fo != null)) {
            File f = FileUtil.toFile(fo);
            GradleJavaSourceSet sourceSet = gjp.containingSourceSet(f);
            if (sourceSet != null)  {
                if (f.isFile()) {
                    String relPath = sourceSet.relativePath(f);
                    String className = (relPath.lastIndexOf('.') > 0 ?
                            relPath.substring(0, relPath.lastIndexOf('.')) :
                            relPath).replace('/', '.');
                    map.put("selectedClass", className);  //NOI18N
                    f = f.getParentFile();
                }
                String pkg = sourceSet.relativePath(f).replace('/', '.');
                map.put("selectedPackage", pkg); //NOI18N
            }
        }
    }

    private static void processSelectedMethod(final Map<String, String> map, Lookup context) {
        SingleMethod method = context.lookup(SingleMethod.class);
        FileObject fo = method != null ? method.getFile() : RunUtils.extractFileObjectfromLookup(context);
        String methodName = method != null ? method.getMethodName() : null;
        if (fo != null) {
             String selectedMethod = evaluateSingleMethod(fo, methodName);
             map.put("selectedMethod", selectedMethod); //NOI18N
        }
    }

    private void processSourceSets(final Map<String, String> map, Lookup context) {
        FileObject[] fos = RunUtils.extractFileObjectsfromLookup(context);
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if ((fos.length > 0) && (gjp != null)) {
            Set<String> buildTasks = new HashSet<>();
            for (FileObject fo : fos) {
                File f = FileUtil.toFile(fo);
                GradleJavaSourceSet ss = gjp.containingSourceSet(f);
                if (ss != null) {
                    Set<GradleJavaSourceSet.SourceType> types = ss.getSourceTypes(f);
                    for (GradleJavaSourceSet.SourceType type : types) {
                        buildTasks.add(ss.getBuildTaskName(type));
                    }
                }
            }
            StringBuilder tasks = new StringBuilder();
            for (String task : buildTasks) {
                tasks.append(task).append(' ');
            }
            map.put("affectedBuildTasks", tasks.toString()); //NOI18N
        }
    }

    private static String evaluateSingleMethod(final FileObject fo, final String method) {
        final Object[] ret = new Object[1];
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource != null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Trees trees = compilationController.getTrees();
                        CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                        List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                        for (Tree tree : typeDecls) {
                            Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                            if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo.getName())) {
                                TypeElement type = (TypeElement) element;
                                StringBuilder sb = new StringBuilder(type.getQualifiedName());
                                if (method != null) {
                                    sb.append('.').append(method);
                                }
                                ret[0] = sb.toString();
                                break;
                            }
                        }
                    }
                }, true);
                return ret[0].toString();
            } catch (IOException ioe) {
                //TODO: Do nothing?
            }
        }
        return null;
    }
}
