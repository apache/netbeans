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
package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.hints.pom.spi.SelectionPOMFixProvider;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A ParserResult task that should be run on caret/selection changes. It runs 
 * {@link SelectionPOMFixProvider}s to provide suggestions appropriate for the
 * current caret location.
 * It also adds gutter annotation for parent POM reference.
 *
 * @author sdedic
 */
public class MavenSelectionHintsTask extends ParserResultTask<MavenResult> {

    @Override
    public void run(MavenResult result, SchedulerEvent event) {
        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }
        CursorMovedSchedulerEvent cursorEvent = (CursorMovedSchedulerEvent)event;
        int ss = cursorEvent.getCaretOffset();
        int se = cursorEvent.getMarkOffset();
        if (ss > se) {
            // swap min/max
            int x = se;
            se = ss;
            ss = x;
        }
        List<ErrorDescription> errors = computeErrors(result, ss, se, cursorEvent.getCaretOffset());
        HintsController.setErrors(result.getPomFile(), PomModelUtils.LAYER_POM_SELECTION, errors);
    }

    @NonNull
    static List<ErrorDescription> computeErrors(MavenResult result, int ss, int se, int co) {
        final List<ErrorDescription> errors = new ArrayList<>();
        FileObject fo = result.getPomFile();
        Project project = FileOwnerQuery.getOwner(fo);
        Document document = result.getSnapshot().getSource().getDocument(false);
        if (fo == null || project == null || project.getProjectDirectory() != fo.getParent()) {
            // ?? pom file ought to form a project!
            return errors;
        }
        final POMModel model = result.getProjectModel();
        // clear selection hints in case of an error; validation errors are handled by 
        // MavenFileHintsTask.
        StyledDocument styled = null;
        Annotation[] old = null;
        
        if (document instanceof StyledDocument) {
            styled = (StyledDocument)document;
            old = (Annotation[]) styled.getProperty("maven_annot");
        }
        if (PomModelUtils.checkModelValid(model)) {
            for (SelectionPOMFixProvider prov : PomModelUtils.hintProviders(project, SelectionPOMFixProvider.class)) {
                List<ErrorDescription> lst = prov.getErrorsForDocument(model, project, 
                        ss, se, co);
                if (lst != null) {
                    errors.addAll(lst);
                }
            }
            if (styled != null) {
                List<Annotation> anns = new ArrayList<Annotation>();
                try {
                    Parent p = model.findComponent(ss, Parent.class, true);
                    if (p != null && p.getArtifactId() != null && p.getGroupId() != null && p.getVersion() != null) { //217741
                        Annotation ann = new ParentPomAnnotation();
                        anns.add(ann);
                        Position position = NbDocument.createPosition(document, ss, Position.Bias.Forward);
                        NbDocument.addAnnotation(styled, position, se - ss, ann);
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                if (old != null) {
                }

                styled.putProperty("maven_annot", anns.toArray(Annotation[]::new));
            } else {
                // clear on error
                if (styled != null) {
                    styled.putProperty("maven_annot", null); //217741
                }
            }
            // remove old annotations
            if (styled != null && old != null) {
                for (Annotation ann : old) {
                    NbDocument.removeAnnotation(styled, ann);
                }
            }
        }
        return errors;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        // 
    }

    public static class ParentPomAnnotation extends Annotation {

        @Override
        public String getAnnotationType() {
            return "org-netbeans-modules-editor-annotations-implements";
        }

        @NbBundle.Messages({
            "ParentPOMAnnotation_Description=Go to parent POM declaration"
        })
        @Override
        public String getShortDescription() {
            return Bundle.ParentPOMAnnotation_Description();
        }
    }
}
