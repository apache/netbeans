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
package org.netbeans.modules.javafx2.editor.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JButton;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.javafx2.editor.codegen.Bundle.*;
import org.netbeans.modules.javafx2.project.api.JavaFXProjectUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@NbBundle.Messages({
    "DN_AddFxProperty=Add Java FX Property...",
    "LBL_ButtonOK=OK",
    "LBL_ButtonCancel=Cancel",
    "CAP_AddProperty=Add Property",
    "ERR_CannotFindOriginalClass=Cannot find original class"})
public class AddJavaFXPropertyCodeGenerator implements CodeGenerator {

    //<editor-fold defaultstate="collapsed" desc="Factory">
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            List<CodeGenerator> ret = new LinkedList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController javac = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || javac == null || path == null) {
                return ret;
            }
            Project p = FileOwnerQuery.getOwner(javac.getFileObject());
            if (p != null) {
                if (JavaFXProjectUtils.isJavaFxEnabled(p) || JavaFXProjectUtils.isMavenFxProject(p)) {
                    //list all fields to detect collisions
                    Element e = javac.getTrees().getElement(path);
                    
                    if (e == null || !e.getKind().isClass()) {
                        return Collections.<CodeGenerator>emptyList();
                    }
                    
                    List<String> existingFields = new LinkedList<String>();
                    
                    for (VariableElement field : ElementFilter.fieldsIn(e.getEnclosedElements())) {
                        existingFields.add(field.getSimpleName().toString());
                    }
                    
                    ret.add(new AddJavaFXPropertyCodeGenerator(javac, component, ElementHandle.create((TypeElement)e), existingFields));
                }
            }
            return ret;
        }
    }
    //</editor-fold>

    private final CompilationController javac;
    private JTextComponent component;
    private ElementHandle<TypeElement> handle;
    private List<String> existingFields;

    AddJavaFXPropertyCodeGenerator(CompilationController javac, JTextComponent component, ElementHandle<TypeElement> handle, List<String> existingFields) {
        this.javac = javac;
        this.component = component;
        this.handle = handle;
        this.existingFields = existingFields;
    }

    @Override
    public String getDisplayName() {
        return DN_AddFxProperty();
    }
    
    @Override
    public void invoke() {
        Object o = component.getDocument().getProperty(Document.StreamDescriptionProperty);
        if (o instanceof DataObject) {
            DataObject d = (DataObject) o;
            JButton ok = new JButton(LBL_ButtonOK());
            FileObject primaryFile = d.getPrimaryFile();
            TypeElement element = handle.resolve(javac);
            TreePath path = javac.getTrees().getPath(element);
            Scope scope = javac.getTrees().getScope(path);
            final AddPropertyPanel addPropertyPanel = new AddPropertyPanel(javac, scope, existingFields, ok);
            DialogDescriptor dd = new DialogDescriptor(addPropertyPanel, CAP_AddProperty(), true, new Object[] {ok, LBL_ButtonCancel()}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) == ok) {
                perform(primaryFile, component, addPropertyPanel.getAddPropertyConfig(), scope);
            }
        }
    }
    
    public void perform(FileObject file, JTextComponent pane, final AddFxPropertyConfig config, final Scope scope) {
        final int caretOffset = component.getCaretPosition();
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                    @Override
                    public void run(WorkingCopy javac) throws IOException {
                        javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Element e = handle.resolve(javac);
                        TreePath path = e != null ? javac.getTrees().getPath(e) : javac.getTreeUtilities().pathFor(caretOffset);
                        path = getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                        if (path == null) {
                            org.netbeans.editor.Utilities.setStatusBoldText(component, ERR_CannotFindOriginalClass());
                        } else {
                            ClassTree cls = (ClassTree) path.getLeaf();
                            AddJavaFXPropertyMaker maker = new AddJavaFXPropertyMaker(javac, scope, javac.getTreeMaker(), config);
                            List<Tree> members = maker.createMembers();
                            if(members != null) {
                                javac.rewrite(cls, GeneratorUtils.insertClassMembers(javac, cls, members, caretOffset));
                            }
                        }
                    }
                });
                GeneratorUtils.guardedCommit(component, mr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static TreePath getPathElementOfKind(Set<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind())) {
                return path;
            }
            path = path.getParentPath();
        }
        return null;
    }
}
