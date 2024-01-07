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
package org.netbeans.modules.micronaut.db;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lsp.CodeAction;
import org.netbeans.api.lsp.LazyCodeAction;
import org.netbeans.api.lsp.Range;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.lsp.CodeActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Union2;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionProvider.class)
public class MicronautDataEndpointGenerator implements CodeActionProvider {

    private static final String SOURCE = "source";
    private static final String CONTROLLER_ANNOTATION_NAME = "io.micronaut.http.annotation.Controller";

    @Override
    @NbBundle.Messages({
        "DN_GenerateDataEndpoint=Generate Data Endpoint...",
        "DN_SelectEndpoints=Select endpoints to generate",
    })
    public List<CodeAction> getCodeActions(Document doc, Range range, Lookup context) {
        try {
            List<String> only = context.lookup(List.class);
            if (only == null || !only.contains(SOURCE)) {
                return Collections.emptyList();
            }
            ResultIterator resultIterator = context.lookup(ResultIterator.class);
            CompilationController cc = resultIterator != null && resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
            if (cc == null) {
                return Collections.emptyList();
            }
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            int offset = range.getStartOffset();
            TreePath path = cc.getTreeUtilities().pathFor(offset);
            path = cc.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
            if (path == null) {
                return Collections.emptyList();
            }
            TypeElement te = (TypeElement) cc.getTrees().getElement(path);
            if (te == null || !te.getKind().isClass()) {
                return Collections.emptyList();
            }
            AnnotationMirror controllerAnn = Utils.getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
            if (controllerAnn == null) {
                return Collections.emptyList();
            }
            List<VariableElement> repositories = Utils.getRepositoriesFor(cc, te);
            if (repositories.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> endpoints = new ArrayList<>();
            Utils.collectMissingDataEndpoints(cc, te, null, (repository, delegateMethod, id) -> {
                switch (delegateMethod.getSimpleName().toString()) {
                    case "findAll":
                        endpoints.add(id != null ? id + "/ -- GET" : "/ -- GET");
                        break;
                    case "findById":
                        endpoints.add(id != null ? id + "/{id} -- GET" : "/{id} -- GET");
                        break;
                    case "deleteById":
                        endpoints.add(id != null ? id + "/{id} -- DELETE" : "/{id} -- DELETE");
                        break;
                }
            });
            if (!endpoints.isEmpty()) {
                FileObject fo = cc.getFileObject();
                List<ElementHandle<VariableElement>> repositoryHandles = repositories.stream().map(repository -> ElementHandle.create(repository)).collect(Collectors.toList());
                return Collections.singletonList(new LazyCodeAction(Bundle.DN_GenerateDataEndpoint(), SOURCE, null, () -> {
                    try {
                        List<NotifyDescriptor.QuickPick.Item> items = endpoints.stream().map(endpoint -> new NotifyDescriptor.QuickPick.Item(endpoint, null)).collect(Collectors.toList());
                        NotifyDescriptor.QuickPick pick = new NotifyDescriptor.QuickPick(Bundle.DN_GenerateDataEndpoint(), Bundle.DN_SelectEndpoints(), items, true);
                        if (DialogDescriptor.OK_OPTION != DialogDisplayer.getDefault().notify(pick)) {
                            return null;
                        }
                        List<String> selectedIds = new ArrayList<>();
                        for (NotifyDescriptor.QuickPick.Item item : pick.getItems()) {
                            if (item.isSelected()) {
                                selectedIds.add(item.getLabel());
                            }
                        }
                        if (selectedIds.isEmpty()) {
                            return null;
                        }
                        JavaSource js = JavaSource.forFileObject(fo);
                        if (js == null) {
                            throw new IOException("Cannot get JavaSource for: " + fo.toURL().toString());
                        }
                        return modify2Edit(js, getTask(offset, repositoryHandles, selectedIds));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return null;
                }));
            }
        } catch (IOException | ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }

    private static Task<WorkingCopy> getTask(int offset, List<ElementHandle<VariableElement>> repositoryHandles, List<String> endpointIds) {
        return copy -> {
            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            tp = copy.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
            if (tp != null) {
                ClassTree clazz = (ClassTree) tp.getLeaf();
                List<Tree> members = new ArrayList<>();
                for (ElementHandle<VariableElement> repositoryHandle : repositoryHandles) {
                    VariableElement repository = repositoryHandle.resolve(copy);
                    if (repository != null) {
                        TypeMirror repositoryType = repository.asType();
                        if (repositoryType.getKind() == TypeKind.DECLARED) {
                            TypeElement repositoryTypeElement = (TypeElement) ((DeclaredType) repositoryType).asElement();
                            String id = null;
                            if (repositoryHandles.size() > 1) {
                                id = '/' + repositoryTypeElement.getSimpleName().toString().toLowerCase();
                                if (id.endsWith("repository")) {
                                    id = id.substring(0, id.length() - 10);
                                }
                            }
                            for (String endpointId : endpointIds) {
                                String delegateMethodName = null;
                                if (endpointId.equals(id != null ? id + "/ -- GET" : "/ -- GET")) {
                                    delegateMethodName = "findAll";
                                } else if (endpointId.equals(id != null ? id + "/{id} -- GET" : "/{id} -- GET")) {
                                    delegateMethodName = "findById";
                                } else if (endpointId.equals(id != null ? id + "/{id} -- DELETE" : "/{id} -- DELETE")) {
                                    delegateMethodName = "deleteById";
                                }
                                if (delegateMethodName != null) {
                                    members.add(Utils.createControllerDataEndpointMethod(copy, repositoryTypeElement, repository.getSimpleName().toString(), delegateMethodName, id));
                                }
                            }
                        }
                    }
                }
                copy.rewrite(clazz, GeneratorUtilities.get(copy).insertClassMembers(clazz, members, offset));
            }
        };
    }

    private static WorkspaceEdit modify2Edit(JavaSource js, Task<WorkingCopy> task) throws IOException {
        FileObject[] file = new FileObject[1];
        ModificationResult changes = js.runModificationTask(wc -> {
            task.run(wc);
            file[0] = wc.getFileObject();
        });
        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(file[0]);
        if (diffs != null) {
            List<TextEdit> edits = new ArrayList<>();
            for (ModificationResult.Difference diff : diffs) {
                edits.add(new TextEdit(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset(), diff.getNewText()));
            }
            return new WorkspaceEdit(Collections.singletonList(Union2.createFirst(new TextDocumentEdit(file[0].toURI().toString(), edits))));
        }
        return null;
    }

    @NbBundle.Messages({
        "LBL_GenerateButton=Generate...",
        "LBL_CancelButton=Cancel",
    })
    private static DialogDescriptor createDialogDescriptor( JComponent content, String label ) {
        final JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(Bundle.LBL_GenerateButton());
        buttons[1] = new JButton(Bundle.LBL_CancelButton());
        final DialogDescriptor dd = new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        dd.addPropertyChangeListener(evt -> {
            if (DialogDescriptor.PROP_VALID.equals(evt.getPropertyName())) {
                buttons[0].setEnabled(dd.isValid());
            }
        });
        return dd;
    }

    @MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
    public static class Factory implements CodeGenerator.Factory {

        @Override
        @NbBundle.Messages({
            "DN_DataEndpoint=Data Endpoint...",
            "LBL_GenerateDataEndpoint=Generate Data Endpoint...",
        })
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent comp = context.lookup(JTextComponent.class);
            CompilationController cc = context.lookup(CompilationController.class);
            if (comp == null || cc == null) {
                return ret;
            }
            TreePath path = context.lookup(TreePath.class);
            path = cc.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
            if (path == null) {
                return ret;
            }
            try {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            TypeElement te = (TypeElement) cc.getTrees().getElement(path);
            if (te == null || !te.getKind().isClass()) {
                return ret;
            }
            AnnotationMirror controllerAnn = Utils.getAnnotation(te.getAnnotationMirrors(), CONTROLLER_ANNOTATION_NAME);
            if (controllerAnn == null) {
                return Collections.emptyList();
            }
            List<VariableElement> repositories = Utils.getRepositoriesFor(cc, te);
            if (repositories.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> endpoints = new ArrayList<>();
            Utils.collectMissingDataEndpoints(cc, te, null, (repository, delegateMethod, id) -> {
                switch (delegateMethod.getSimpleName().toString()) {
                    case "findAll":
                        endpoints.add(id != null ? id + "/ -- GET" : "/ -- GET");
                        break;
                    case "findById":
                        endpoints.add(id != null ? id + "/{id} -- GET" : "/{id} -- GET");
                        break;
                    case "deleteById":
                        endpoints.add(id != null ? id + "/{id} -- DELETE" : "/{id} -- DELETE");
                        break;
                }
            });
            if (!endpoints.isEmpty()) {
                int offset = comp.getCaretPosition();
                FileObject fo = cc.getFileObject();
                List<ElementHandle<VariableElement>> repositoryHandles = repositories.stream().map(repository -> ElementHandle.create(repository)).collect(Collectors.toList());
                ret.add(new CodeGenerator() {
                    @Override
                    public String getDisplayName() {
                        return Bundle.DN_DataEndpoint();
                    }

                    @Override
                    public void invoke() {
                        EndpointSelectorPanel panel = new EndpointSelectorPanel(endpoints);
                        DialogDescriptor dialogDescriptor = createDialogDescriptor(panel, Bundle.LBL_GenerateDataEndpoint());
                        panel.addPropertyChangeListener(evt -> {
                            List<String> selected = panel.getSelectedEndpoints();
                            dialogDescriptor.setValid(selected != null && !selected.isEmpty());
                        });
                        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                        dialog.setVisible(true);
                        if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
                            return;
                        }
                        List<String> selectedEndpoints = panel.getSelectedEndpoints();
                        if (selectedEndpoints.isEmpty()) {
                            return;
                        }
                        try {
                            JavaSource js = JavaSource.forFileObject(fo);
                            if (js == null) {
                                throw new IOException("Cannot get JavaSource for: " + fo.toURL().toString());
                            }
                            js.runModificationTask(getTask(offset, repositoryHandles, selectedEndpoints)).commit();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            return ret;
        }
    }
}
