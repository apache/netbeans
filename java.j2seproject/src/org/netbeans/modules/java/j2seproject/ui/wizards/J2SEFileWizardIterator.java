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
package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import static org.netbeans.modules.java.j2seproject.J2SEProjectUtil.ref;
import org.netbeans.spi.java.project.support.ui.templates.JavaFileWizardIteratorFactory;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class J2SEFileWizardIterator implements JavaFileWizardIteratorFactory {
    
    private static final String MODULE_INFO_JAVA = "module-info.java"; //NOI18N

    public static JavaFileWizardIteratorFactory create() {
        return new J2SEFileWizardIterator();
    }
    
    private J2SEFileWizardIterator() {        
    }
    
    @Override
    public WizardDescriptor.Iterator<WizardDescriptor> createIterator(@NonNull FileObject template) {
        return MODULE_INFO_JAVA.equals(template.getNameExt()) ? new WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor>() { //NOI18N

            private transient WizardDescriptor wiz;
            private WizardDescriptor.Panel<WizardDescriptor> panel = null;

            @Override
            public Set instantiate() throws IOException {
                final J2SEProject project = (J2SEProject) Templates.getProject(wiz);
                final boolean modularSources = J2SEProjectUtil.hasModuleInfo(project.getSourceRoots());
                final boolean modularTests = J2SEProjectUtil.hasModuleInfo(project.getTestSourceRoots());
                final Iterable<ClassPathSupport.Item> toMove = (Iterable<ClassPathSupport.Item>) wiz.getProperty(MoveToModulePathPanel.CP_ITEMS_TO_MOVE);
                ProjectManager.mutex().writeAccess(() -> {
                    final EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    boolean changed = false;
                    if (toMove != null) {
                        for (ClassPathSupport.Item item : toMove) {
                            changed |= removeRef(ep, ProjectProperties.JAVAC_CLASSPATH, item.getReference());
                            changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_MODULEPATH, item.getReference(), null);
                        }
                    }
                    if (modularSources) {
                        if (modularTests) {
                            changed |= removeRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ref(ProjectProperties.BUILD_CLASSES_DIR, true));
                            changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ref(ProjectProperties.BUILD_CLASSES_DIR, true), ref(ProjectProperties.JAVAC_MODULEPATH, true));
                            changed |= removeRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true));
                            changed |= addRefIfAbsent(ep, ProjectProperties.RUN_TEST_MODULEPATH, ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true), ref(ProjectProperties.JAVAC_TEST_MODULEPATH, true));
                        } else {
                            changed |= removeRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ref(ProjectProperties.BUILD_CLASSES_DIR, true));
                            changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ref(ProjectProperties.BUILD_CLASSES_DIR, true), ref(ProjectProperties.JAVAC_MODULEPATH, true));
                            changed |= removeRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true));
                            changed |= removeRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true));
                        }
                    } else {
                        changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ref(ProjectProperties.BUILD_CLASSES_DIR, true), ref(ProjectProperties.JAVAC_CLASSPATH, true));
                        changed |= removeRef(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ref(ProjectProperties.BUILD_CLASSES_DIR, true));
                        changed |= addRefIfAbsent(ep, ProjectProperties.RUN_TEST_CLASSPATH, ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true), ref(ProjectProperties.JAVAC_TEST_CLASSPATH, true));
                        changed |= removeRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true));
                    }
                    if (changed) {
                        try {
                            project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                return null;
            }

            @Override
            public void initialize(WizardDescriptor wizard) {
                this.wiz = wizard;
                final Project project = Templates.getProject(wizard);
                if (project == null) throw new NullPointerException ("No project found for: " + wizard);
                final J2SEProject j2seProject = project.getLookup().lookup(J2SEProject.class);
                if (j2seProject == null) throw new NullPointerException ("No j2seproject found in: " + project);
                final EditableProperties ep = j2seProject.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (hasAnyRef(ep, ProjectProperties.JAVAC_CLASSPATH)
                        && !hasAnyRef(ep, ProjectProperties.JAVAC_MODULEPATH)) {
                    panel = new MoveToModulePathPanel(j2seProject, ep);
                }
            }

            @Override
            public void uninitialize(WizardDescriptor wizard) {
                this.wiz = null;
            }

            @Override
            public WizardDescriptor.Panel<WizardDescriptor> current() {
                return panel;
            }

            @Override
            public String name() {
                return ""; //NOI18N
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public void nextPanel() {
            }

            @Override
            public void previousPanel() {
            }

            private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

            @Override
            public final void addChangeListener(ChangeListener l) {
                synchronized(listeners) {
                    listeners.add(l);
                }
            }

            @Override
            public final void removeChangeListener(ChangeListener l) {
                synchronized(listeners) {
                    listeners.remove(l);
                }
            }

            protected final void fireChangeEvent() {
                ChangeListener[] ls;
                synchronized (listeners) {
                    ls = listeners.toArray(new ChangeListener[listeners.size()]);
                }
                ChangeEvent ev = new ChangeEvent(this);
                for (ChangeListener l : ls) {
                    l.stateChanged(ev);
                }
            }
        } : null;
    }

    private static boolean hasAnyRef(
            @NonNull final EditableProperties ep,
            @NonNull final String pathId) {
        return Optional.ofNullable(ep.getProperty(pathId))
                .map((val) -> {
                    return !val.isEmpty();
                })
                .orElse(Boolean.FALSE);
    }

    private static boolean removeRef(
            @NonNull final EditableProperties ep,
            @NonNull final String pathId,
            @NonNull final String elementToRemove) {
        final boolean[] changed = new boolean[1];
        Optional.ofNullable(ep.getProperty(pathId))
            .map((val)->{
                return Arrays.stream(PropertyUtils.tokenizePath(val))
                        .filter((element) -> {
                            final boolean remove = elementToRemove.equals(element);
                            changed[0] |= remove;
                            return !remove;
                        })
                        .toArray((len) -> new String[len]);
            })
            .ifPresent((val) -> {
                ep.setProperty(pathId, addPathSeparators(val));
            });
        return changed[0];
    }

    private static boolean addRefIfAbsent(
            @NonNull final EditableProperties ep,
            @NonNull final String pathId,
            @NonNull final String elementToAdd,
            @NullAllowed final String insertAfter) {
        final boolean[] changed = new boolean[1];
        Optional.ofNullable(ep.getProperty(pathId))
            .map((val)-> {
                String[] path = PropertyUtils.tokenizePath(val);
                if(!Arrays.stream(path).anyMatch((element) -> elementToAdd.equals(element))) {
                    final List<String> newPath = new ArrayList<>(path.length + 1);
                    boolean added = false;
                    for (int i=0; i< path.length; i++) {
                        newPath.add(path[i]);
                        if (insertAfter != null && insertAfter.equals(path[i])) {
                            added = true;
                            newPath.add(elementToAdd);
                        }
                    }
                    if (!added) {
                        newPath.add(elementToAdd);
                    }
                    path = newPath.toArray(new String[newPath.size()]);
                    changed[0] = true;
                }
                return path;
            })
            .ifPresent((val) -> ep.setProperty(pathId, addPathSeparators(val)));
        return changed[0];
    }

    @NonNull
    private static String[] addPathSeparators(@NonNull final String... path) {
        for (int i = 0; i < path.length; i++) {
            path[i] = i+1 == path.length ?
                    path[i] :
                    String.format("%s:", path[i]);  //NOI18N
        }
        return path;
    }
}
