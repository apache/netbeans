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
package org.netbeans.modules.j2ee.ejbrefactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * A plugin for EJB refactorings, only displays a warning message.
 *
 * @author Erno Mononen
 */
public class EjbRefactoringPlugin implements RefactoringPlugin {

    private static final Logger LOG = Logger.getLogger(EjbRefactoringPlugin.class.getName());
    /**
     * The localized message to be displayed.
     */
    private AbstractRefactoring refactoring;

    public EjbRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        final Problem[] result = new Problem[1];
        final TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            FileObject fo = tph.getFileObject();
            if (fo != null) {
                try {
                    JavaSource js = JavaSource.forFileObject(fo);
                    if (js != null) {
                        js.runUserActionTask(new CancellableTask<CompilationController>() {
                            @Override
                            public void run(CompilationController info) throws Exception {
                                info.toPhase(JavaSource.Phase.RESOLVED);
                                Element el = tph.resolveElement(info);
                                if (el == null) {
                                    return;
                                }

                                if (el.getModifiers().contains(Modifier.PRIVATE)) {
                                    result[0] = null;
                                } else {
                                    String refactoringWarning = getRefactoringWarning();
                                    if (refactoringWarning != null) {
                                        result[0] = new Problem(false, refactoringWarning);
                                    }
                                }
                            }

                            @Override
                            public void cancel() {
                                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                            }
                        }, true);
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return result[0];
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        return null;
    }

    private String getRefactoringWarning() {
        String msg;
        if (refactoring instanceof RenameRefactoring) {
            msg = "TXT_EjbJarRenameWarning";        //NO18N
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            msg = "TXT_EjbJarSafeDeleteWarning";    //NO18N
        } else if (refactoring instanceof MoveRefactoring) {
            msg = "TXT_EjbJarMoveClassWarning";     //NO18N
        } else if (refactoring instanceof WhereUsedQuery) {
            msg = "TXT_EjbJarWhereUsedWarning";     //NO18N
        } else {
            msg = "TXT_EjbJarGeneralWarning";       //NO18N
        }

        FileObject source = getRefactoringSource(refactoring);
        if (source == null) {
            return null;
        }
        List<EjbJar> ejbJars = getEjbJars(source);
        if (ejbJars.isEmpty()) {
            return null;
        }
        String ejbJarPaths = getEjbJarPaths(ejbJars);

        return NbBundle.getMessage(EjbRefactoringFactory.class, msg, ejbJarPaths);
    }

    /**
     * @return a comma separated string representing the locations of the ejb-jar.xml files of the
     * given <code>ejbJars</code>.
     */
    private String getEjbJarPaths(List<EjbJar> ejbJars) {
        // TODO: it would be probably better to display the project names instead
        StringBuilder ejbJarPaths = new StringBuilder();
        for (Iterator<EjbJar> it = ejbJars.iterator(); it.hasNext();) {
            EjbJar ejbJar = it.next();
            String path = FileUtil.getFileDisplayName(ejbJar.getDeploymentDescriptor());
            ejbJarPaths.append(path);
            if (it.hasNext()) {
                ejbJarPaths.append(", ");
            }
        }
        return ejbJarPaths.toString();
    }

    private FileObject getRefactoringSource(AbstractRefactoring refactoring) {
        FileObject source = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (source != null) {
            return source;
        }
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph.getFileObject();
        }
        NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        if (folder != null) {
            return folder.getFolder();
        }
        return null;
    }

    /**
     * @return the <code>EjbJar</code>s representing the EJB Modules that are relevant to the given <code>source</code>,
     * i.e. the ones that depend on the project owning the <code>source</code>.
     */
    private List<EjbJar> getEjbJars(FileObject source) {
        List<EjbJar> result = new ArrayList<EjbJar>();
        for (EjbJar each : getRelevantEjbModules(source)) {
            FileObject ejbJarFO = each.getDeploymentDescriptor();
            if (ejbJarFO != null) {
                result.add(each);
            }
        }
        return result;
    }

    /**
     * Finds all ejb projects that depend on a project which is owner of FileObject 'fo'
     */
    private static Collection<EjbJar> getRelevantEjbModules(FileObject fo) {
        Project affectedProject = FileOwnerQuery.getOwner(fo);
        List<EjbJar> ejbmodules = new ArrayList<EjbJar>();
        List<Project> projects = new ArrayList<Project>();

        if (affectedProject != null) {
            // first check if the project which directly contains fo is relevant
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar emod =
                    org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(affectedProject.getProjectDirectory());
            if (emod != null) {
                projects.add(affectedProject);
            } else {
                return Collections.EMPTY_SET;
            }
            for (Project project : OpenProjects.getDefault().getOpenProjects()) {
                Object isJ2eeApp = project.getLookup().lookup(J2eeApplicationProvider.class);
                if (isJ2eeApp != null) {
                    J2eeApplicationProvider j2eeApp = (J2eeApplicationProvider) isJ2eeApp;
                    J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();

                    if (j2eeModules != null) {
                        J2eeModuleProvider affectedPrjProvider = affectedProject.getLookup().lookup(J2eeModuleProvider.class);

                        if (affectedPrjProvider != null) {
                            if (Arrays.asList(j2eeModules).contains(affectedPrjProvider)) {
                                for (int k = 0; k < j2eeModules.length; k++) {
                                    FileObject[] sourceRoots = j2eeModules[k].getSourceRoots();
                                    if (sourceRoots != null && sourceRoots.length > 0) {
                                        FileObject srcRoot = sourceRoots[0];
                                        Project p = FileOwnerQuery.getOwner(srcRoot);
                                        if ((p != null) && (!projects.contains(p))) {
                                            projects.add(p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //mkleint: see subprojectprovider for official contract, maybe classpath should be checked instead? see #210465
                //in this case J2eeApplicationprovider might provide the same results though.
                Object obj = project.getLookup().lookup(SubprojectProvider.class);
                if ((obj != null) && (obj instanceof SubprojectProvider)) {
                    Set subprojects = ((SubprojectProvider) obj).getSubprojects();
                    if (subprojects.contains(affectedProject)) {
                        org.netbeans.modules.j2ee.api.ejbjar.EjbJar em = org.netbeans.modules.j2ee.api.ejbjar.EjbJar
                                .getEjbJar(project.getProjectDirectory());
                        if (em != null) {
                            if (!projects.contains(project)) { // include each project only once
                                projects.add(project);
                            }
                        }
                    }
                }
            }
        }

        for (int j = 0; j < projects.size(); j++) {
            Project prj = (Project) ((ArrayList) projects).get(j);
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejb =
                    org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(prj.getProjectDirectory());
            if (ejb != null) {
                ejbmodules.add(ejb);
            }
        }

        return ejbmodules;
    }
}
