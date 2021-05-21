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

package org.netbeans.modules.maven.j2ee;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.j2ee.Bundle.*;


/**
 * Warn user when Java EE APIs are missing.
 */
public class MissingJavaEEForUnitTestExecutionHint extends AbstractHint {

    private static final Set<Tree.Kind> TREE_KINDS =
            EnumSet.<Tree.Kind>of(Kind.MEMBER_SELECT, Kind.IDENTIFIER);
    
    public MissingJavaEEForUnitTestExecutionHint() {
        super(true, true, AbstractHint.HintSeverity.ERROR);
    }
    
    @Override
    @NbBundle.Messages("MissingJavaEEForUnitTestExecutionHint_Description=Find out whether Java EE API is available on project classpath. Java EE API is necessary for successful test execution. This hint also warns about incorrect usage of javaee-web-api artifact - that artifact is suitable only for compilation but not for test execution because body of all API methods was removed from the bytecode.")
    public String getDescription() {
        return MissingJavaEEForUnitTestExecutionHint_Description();
    }

    @Override
    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    @Override
    public List<org.netbeans.spi.editor.hints.ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        Element el = info.getTrees().getElement(treePath);
        if (el == null) {
            return null;
        }
        //Logger.getAnonymousLogger().log(Level.SEVERE, "---"+el+"  "+(treePath.getLeaf() != null ? treePath.getLeaf().getKind() : "no kind"));
        if (el.asType() == null || !el.asType().getKind().equals(TypeKind.DECLARED)) {
            return null;
        }
        if (!isEEType(info, el.asType())) {
            return null;
        }
        
        String name = el.asType().toString();
        FileObject testFile = info.getFileObject();
        Project prj = FileOwnerQuery.getOwner(testFile);
        if (prj == null) {
            return null;
        }
        NbMavenProject mp = prj.getLookup().lookup(NbMavenProject.class);
        if (mp == null) {
            // handles only Maven projects; Ant projects solves this issue differently
            return null;
        }

        List<String> testRoots = mp.getMavenProject().getTestCompileSourceRoots();
        String path = FileUtil.getFileDisplayName(testFile);
        boolean unitTest = false;
        for (String testRoot : testRoots) {
            if (path.startsWith(testRoot)) {
                unitTest = true;
                break;
            }
        }
        if (!unitTest) {
            // relevant only for unit tests which are going to be executed
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(testFile, ClassPath.EXECUTE);
        if (cp == null) {
            return null;
        }
        boolean javaeeJar = false;
        boolean gfServer = false;
        for (FileObject cpRoot : cp.getRoots()) {
            FileObject fo = FileUtil.getArchiveFile(cpRoot);
            if (fo == null) {
                continue;
            }
            if (fo.getNameExt().toLowerCase().contains("javaee-web-api-7.0") || // NOI18N
                    fo.getNameExt().toLowerCase().contains("javaee-api-7.0") ||
                    fo.getNameExt().toLowerCase().contains("javaee-web-api-8.0") ||
                    fo.getNameExt().toLowerCase().contains("javaee-api-8.0")) { // NOI18N
                javaeeJar = true;
            }
            
            if (fo.getNameExt().toLowerCase().contains("glassfish-embedded-static-shell") || // NOI18N
                    fo.getNameExt().toLowerCase().contains("glassfish-embedded-all")) { // NOI18N
                // GF is on project classpath; everything should be OK
                return null;
            }
        }
        try {
            cp.getClassLoader(true).loadClass(name); // NOI18N
            return null;
        } catch (ClassFormatError tt) {
            // OK, show hint to add JavaEE API
        } catch (ClassNotFoundException tt) {
            // #196713 - ignore this exception; it can happen for example when project classes are not compiled
            return null;
        }
        
        Tree t = treePath.getLeaf();
        return Collections.<ErrorDescription>singletonList(
                ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(),
                getDisplayName(javaeeJar),
                new ArrayList<Fix>(),
                info.getFileObject(),
                (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)));
    }

    private boolean isEEType(CompilationInfo info, TypeMirror type) {
        if (type == null) {
            return false;
        }
        if (isEEType(type)) {
            return true;
        }
        List<? extends TypeMirror> l = info.getTypes().directSupertypes(type);
        for (TypeMirror m : l) {
            if (isEEType(info, m)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isEEType(TypeMirror type) {
        if (type == null) {
            return false;
        }
        String name = type.toString();
        return (name.startsWith("javax.")); // NOI18N
    }

    @Override
    public String getId() {
        return "MissingJavaEEForUnitTestExecutionHint"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return getDisplayName(false);
    }

    @NbBundle.Messages({
        "MissingJavaEEForUnitTestExecutionHint_DisplayName2=Java EE API is missing on project classpath (javaee-web-api artifact cannot be used for test execution).",
        "MissingJavaEEForUnitTestExecutionHint_DisplayName=Java EE API is missing on project classpath."
    })
    public String getDisplayName(boolean javaeeJar) {
        if (javaeeJar) {
            return MissingJavaEEForUnitTestExecutionHint_DisplayName2();
        } else {
            return MissingJavaEEForUnitTestExecutionHint_DisplayName();
        }
    }
    
    @Override
    public void cancel() {
    }

}
