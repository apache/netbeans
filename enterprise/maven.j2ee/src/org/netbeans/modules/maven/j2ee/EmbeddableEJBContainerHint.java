/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Warn user when implementation of EJBContainer is missing and offers him Embeddable EJB Container to use .
 */
public class EmbeddableEJBContainerHint extends AbstractHint {

    private static final Set<Tree.Kind> TREE_KINDS =
            EnumSet.<Tree.Kind>of(Tree.Kind.METHOD_INVOCATION);
    
    private static final String PROP_GF_EMBEDDED_JAR = "glassfish.embedded-static-shell.jar";
    
    public EmbeddableEJBContainerHint() {
        super(true, true, AbstractHint.HintSeverity.ERROR);
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(EmbeddableEJBContainerHint.class, "EmbeddableEJBContainer_Description"); // NOI18N
    }

    @Override
    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    @Override
    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        Tree t = treePath.getLeaf();

        Element el = info.getTrees().getElement(treePath);

        if (!(el != null && el.getEnclosingElement().getSimpleName().toString().equals("EJBContainer") &&  // NOI18N
            el.getSimpleName().toString().equals("createEJBContainer"))) { // NOI18N
            return null;
        }
        
        FileObject testFile = info.getFileObject();
        Project prj = FileOwnerQuery.getOwner(testFile);
        if (prj == null) {
            return null;
        }
        if (prj.getLookup().lookup(NbMavenProject.class) == null) {
            // handles only Maven projects; Ant projects solves this issue differently
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(testFile, ClassPath.EXECUTE);
        if (cp == null) {
            return null;
        }
        for (FileObject cpRoot : cp.getRoots()) {
            FileObject fo = FileUtil.getArchiveFile(cpRoot);
            if (fo != null && fo.getNameExt().toLowerCase().contains("glassfish-embedded-static-shell")) {
                return null;
            }
        }
        try {
            cp.getClassLoader(true).loadClass("javax.ejb.embeddable.EJBContainer"); // NOI18N
            return null;
        } catch (ClassFormatError tt) {
            // OK, show hint to add GF
        } catch (ClassNotFoundException tt) {
            // OK, show hint to add GF
        }
        
        List<Fix> fixes = new ArrayList<Fix>();
        J2eeModuleProvider provider = prj.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            fixes = FixEjbContainerAction.createGF3SystemScope(prj);
        }
                
        return Collections.<ErrorDescription>singletonList(
                ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(),
                getDisplayName(),
                fixes,
                info.getFileObject(),
                (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)));
    }

    @Override
    public String getId() {
        return "EmbeddableEJBContainer"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EmbeddableEJBContainerHint.class, "EmbeddableEJBContainer_DisplayName"); // NOI18N
    }

    @Override
    public void cancel() {
    }

    private static final class FixEjbContainerAction implements Fix {

        private static final String GF_EMBEDDED_STATIC_SHELL_POM_LATEST = "https://maven.java.net/content/repositories/releases/org/glassfish/main/extras/glassfish-embedded-static-shell/3.1.2.2/glassfish-embedded-static-shell-3.1.2.2.pom";
        private static final String GF_EMBEDDED_STATIC_SHELL_POM_OLD = "http://repo2.maven.org/maven2/org/glassfish/extras/glassfish-embedded-static-shell/3.0.1/glassfish-embedded-static-shell-3.0.1.pom";
        private final String fixDesc;
        private final URL pomUrl;
        private final Project project;
        private final File file;


        private FixEjbContainerAction(String fixDesc, URL pomUrl, File f, Project prj) {
            this.fixDesc = fixDesc;
            this.pomUrl = pomUrl;
            this.project = prj;
            this.file = FileUtil.normalizeFile(f);
        }

        public static List<Fix> createGF3SystemScope(Project prj) {
            List<Fix> fixes = new ArrayList<Fix>();
            String usedServer = JavaEEProjectSettings.getServerInstanceID(prj);
            for (String serId : Deployment.getDefault().getServerInstanceIDs()) {
                ServerInstance server = Deployment.getDefault().getServerInstance(serId);
                try {
                    if (!"gfv3ee6".equals(server.getServerID())) {
                        continue;
                    }
                } catch (InstanceRemovedException ex) {
                    continue;
                }
                try {
                    File[] files = server.getJ2eePlatform().getToolClasspathEntries(J2eePlatform.TOOL_EMBEDDABLE_EJB);
                    assert files.length == 1 : "expecting one item: " + Arrays.asList(files);
                    
                    try {
                        URL pomUrl;
                        if (serId.indexOf("gfv3ee6wc") != -1) {
                            pomUrl = new URL(GF_EMBEDDED_STATIC_SHELL_POM_LATEST);
                        } else {
                            pomUrl = new URL(GF_EMBEDDED_STATIC_SHELL_POM_OLD);
                        }
                        if (serId.equals(usedServer)) {
                            fixes.clear();
                        }
                        fixes.add(new FixEjbContainerAction(
                                NbBundle.getMessage(EmbeddableEJBContainerHint.class, "EmbeddableEJBContainer_FixGFStatic", server.getDisplayName()),
                                pomUrl, files[0], prj));
                        if (serId.equals(usedServer)) {
                            return fixes;
                        }
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } catch (InstanceRemovedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return fixes;
        }
        
        @Override
        public ChangeInfo implement() throws Exception {
            final Boolean[] added = new Boolean[1];
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    added[0] = checkAndAddPom(pomUrl, model, file);
                }
            };
            FileObject pom = project.getProjectDirectory().getFileObject("pom.xml");//NOI18N
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
            //TODO is the manual reload necessary if pom.xml file is being saved?
    //                NbMavenProject.fireMavenProjectReload(project);
            if (added[0]) {
                project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
            }
            return null;
        }

        private boolean checkAndAddPom(URL pom, POMModel model, File systemDep) {
            ModelUtils.LibraryDescriptor result = ModelUtils.checkLibrary(pom);
            if (result != null) {
                //set dependency
                Dependency dep = ModelUtils.checkModelDependency(model, result.getGroupId(), result.getArtifactId(), true);
                dep.setVersion(result.getVersion());
                dep.setScope("system"); //NOI18N
                
                if (result.getClassifier() != null) {
                    dep.setClassifier(result.getClassifier());
                }
                if (systemDep != null) {
                    Properties props = model.getProject().getProperties();
                    if (props != null && props.getProperty(PROP_GF_EMBEDDED_JAR) == null) {
                        props.setProperty(PROP_GF_EMBEDDED_JAR, systemDep.getAbsolutePath());
                    }
                    dep.setSystemPath("${"+PROP_GF_EMBEDDED_JAR+ "}");
                }
                return true;
            }
            return false;
        }

        @Override
        public String getText() {
            return fixDesc;
        }
     }
}
