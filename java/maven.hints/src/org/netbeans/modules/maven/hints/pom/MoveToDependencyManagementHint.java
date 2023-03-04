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
package org.netbeans.modules.maven.hints.pom;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.SelectionPOMFixProvider;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class MoveToDependencyManagementHint implements SelectionPOMFixProvider {
    private static final Logger LOG = Logger.getLogger(MoveToDependencyManagementHint.class.getName());

    private final Configuration configuration;

    public MoveToDependencyManagementHint() {
        configuration = new Configuration("MoveToDependencyManagementHint", //NOI18N
                NbBundle.getMessage(MoveToDependencyManagementHint.class, "TIT_MoveToDependencyManagementHint"),
                NbBundle.getMessage(MoveToDependencyManagementHint.class, "DESC_MoveToDependencyManagementHint"),
                true, Configuration.HintSeverity.WARNING);
    }

    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj,
            int selectionStart, int selectionEnd, int caretPosition) {
        List<ErrorDescription> err = new ArrayList<ErrorDescription>();
        if (prj == null) {
            return err;
        }
        DocumentComponent comp1 = model.findComponent(selectionStart);
        DocumentComponent comp2 = model.findComponent(selectionEnd);
        if (comp1 == null || comp2 == null) { //#157213
            return err;
        }
 
        List<Dependency> deps = getSelectedDependencies(model, selectionStart, selectionEnd);
        if (deps != null && !deps.isEmpty()) { //NOI18N
            try {
                Line line = NbEditorUtilities.getLine(model.getBaseDocument(), caretPosition, false);
                err.add(ErrorDescriptionFactory.createErrorDescription(
                        Severity.HINT,
                        NbBundle.getMessage(MoveToDependencyManagementHint.class, "TEXT_MoveToDependencyManagementHint"),
                        Collections.<Fix>singletonList(new MoveFix(selectionStart, selectionEnd, model, prj)),
                        model.getBaseDocument(), line.getLineNumber() + 1));
            } catch (IndexOutOfBoundsException ioob) {
                //#214527
                LOG.log(Level.FINE, "document changed", ioob);
            }
        }
        return err;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private static class MoveFix implements Fix {
        private final POMModel mdl;
        private final int start;
        private final int end;
        private final Project project;

        MoveFix(int selectionStart, int selectionEnd, POMModel model, Project prj) {
            mdl = model;
            start = selectionStart;
            end = selectionEnd;
	    project = prj;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(OverrideDependencyManagementError.class, "TIT_MoveToDependencyManagementHint");
        }

        @Override
        public ChangeInfo implement() throws Exception {

            ChangeInfo info = new ChangeInfo();
            if (!mdl.getState().equals(Model.State.VALID)) {
                return info;
            }
            
            PomModelUtils.implementInTransaction(mdl, new Runnable() {
                @Override
                public void run() {
                    File fl = mdl.getModelSource().getLookup().lookup(File.class);
                    if (fl == null) {
                        FileObject obj = mdl.getModelSource().getLookup().lookup(FileObject.class);
                        fl = FileUtil.toFile(obj);
                    }
                    assert fl != null;
                    List<Dependency> deps = getSelectedDependencies(mdl, start, end);
                    if (deps == null || deps.isEmpty()) {
                        return;
                    }
                    MoveToDependencyManagementPanel pnl= createPanel(fl);
                    DialogDescriptor dd = new DialogDescriptor(pnl, NbBundle.getMessage(MoveToDependencyManagementHint.class, "TIT_MoveDepMan"));
                    Object ret = DialogDisplayer.getDefault().notify(dd);
                    if (ret == DialogDescriptor.OK_OPTION) {
                        //TODO add the versions to the selected DM section.
                        FileObject fo = pnl.getSelectedPomFile();
                        if (fo == null) {
                            return;
                        }
                        FileObject current = mdl.getModelSource().getLookup().lookup(FileObject.class);
                        POMModel depMdl;
                        if (fo.equals(current)) {
                            depMdl = mdl;
                        } else {
                            ModelSource depSource = Utilities.createModelSource(fo);
                            depMdl = POMModelFactory.getDefault().getModel(depSource);
                        }
                        int oldpos = -1;
                        try {
                            if (depMdl != mdl) {
                                depMdl.startTransaction();
                            }
                            DependencyManagement dm = depMdl.getProject().getDependencyManagement();
                            if (dm == null) {
                                dm = depMdl.getFactory().createDependencyManagement();
                                depMdl.getProject().setDependencyManagement(dm);
                            }
                            for (Dependency d : deps) {
                                Dependency old = dm.findDependencyById(d.getGroupId(), d.getArtifactId(), d.getScope());
                                if (old == null) {
                                    old = depMdl.getFactory().createDependency();
                                    old.setGroupId(d.getGroupId());
                                    old.setArtifactId(d.getArtifactId());
                                    old.setVersion(d.getVersion());
                                    old.setClassifier(d.getClassifier());
                                    old.setType(d.getType());
                                    old.setScope(d.getScope());
                                    dm.addDependency(old);
                                } else {
                                    //TODO shall be copy over values from the current pom to the dm?
                                    old.setVersion(d.getVersion());
                                }
                                oldpos = old.findPosition();

                            }
                        } finally {
                            if (depMdl != mdl) {
                                try {
                                    depMdl.endTransaction();
                                } catch (IllegalStateException ex) {
                                    StatusDisplayer.getDefault().setStatusText(
                                            NbBundle.getMessage(PomModelUtils.class, "ERR_UpdatePomModel",
                                            Exceptions.findLocalizedMessage(ex)));
                                }
                            }
                        }
                        for (Dependency d : deps) {
                            d.setVersion(null);
                        }
                        if (oldpos != -1) {
                            openParent(oldpos, depMdl);
                        }
                    }
                }

                private MoveToDependencyManagementPanel createPanel(File fl) {
                    final MoveToDependencyManagementPanel[] pnl= new MoveToDependencyManagementPanel[1];
                    final File ffl = fl;
                    try {
                        EventQueue.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                pnl[0] = new MoveToDependencyManagementPanel(ffl, project);
                            }
                        });
                    } catch (InterruptedException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    } catch (InvocationTargetException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                    return pnl[0];
                }
            });
            return info;
        }
    }

    private static List<Dependency> getSelectedDependencies(POMModel mdl, int start, int end) {
        DocumentComponent comp1 = mdl.findComponent(start);

        POMComponent pc = findEnclosing(comp1);
        List<Dependency> dps = null;
        if (pc instanceof org.netbeans.modules.maven.model.pom.Project) {
            org.netbeans.modules.maven.model.pom.Project modprj = (org.netbeans.modules.maven.model.pom.Project) pc;
            dps = modprj.getDependencies();
        } else if (pc instanceof Profile) {
            Profile prf = (Profile) pc;
            dps = prf.getDependencies();
        }
        if (dps == null) {
            return null;
        }
        return extractSelectedDeps(dps, start, end);
    }

    private static void openParent(final int offset, final POMModel model) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Line line = NbEditorUtilities.getLine(model.getBaseDocument(), offset, false);
                    line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                } catch (IndexOutOfBoundsException e) {
                    //#214527
                    LOG.log(Level.FINE, "document changed", e);
                }
            }
        });
    }

    private static List<Dependency> extractSelectedDeps(List<Dependency> dps, int selectionStart, int selectionEnd) {
        List<Dependency> toRet = new ArrayList<Dependency>();
        for (Dependency d : dps) {
            int pos = d.findPosition();
            int endPos = d.findEndPosition();
            if (pos >= selectionStart && endPos <= selectionEnd) {
                if (d.getVersion() != null) {
                    toRet.add(d);
                }
            }
        }
        return toRet;
    }

    private static POMComponent findEnclosing(DocumentComponent comp1) {
        if (comp1 instanceof ModelList) {
            return (POMComponent)comp1.getParent();
        }
        Component parent = comp1.getParent();
        if (parent == null || !(parent instanceof POMComponent)) {
            return null;
        }
        return findEnclosing((DocumentComponent)parent);
    }

}
