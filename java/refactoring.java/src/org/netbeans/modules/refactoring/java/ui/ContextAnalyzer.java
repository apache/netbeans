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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * TODO: should be API
 * @author Jan Becicka
 */
public final class ContextAnalyzer {

    /**
     * create analyze task.
     * @param context context of refactoring
     * @param factory for creating of RefactoringUI
     * @return
     */
    public static Runnable createTask(Lookup context, final JavaRefactoringUIFactory factory) {

        Runnable task;
        EditorCookie ec = context.lookup(EditorCookie.class);
        if (RefactoringUtils.isFromEditor(ec)) {
            task = new TextComponentTask(ec) {

                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement, int startOffset, int endOffset, final CompilationInfo info) {
                    TreePathHandle[] handles;
                    FileObject[] file;
                    if(selectedElement != null) {
                        handles = new TreePathHandle[]{selectedElement};
                        file = new FileObject[]{selectedElement.getFileObject()};
                    } else {
                        handles = new TreePathHandle[0];
                        file = new FileObject[]{info.getFileObject()};
                    }
                    return factory.create(info, handles, file, new NonRecursiveFolder[0]);
                }
            };
        } else if (nodeHandle(context)) {
            task = new TreePathHandleTask(new HashSet<Node>(context.lookupAll(Node.class)), false) {

                RefactoringUI ui;
                private boolean created;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    if (size()==1) {
                        ui = factory.create(javac, new TreePathHandle[]{handle}, null, new NonRecursiveFolder[0]);
                        created = true;
                    }
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    if (!created) {
                        ui = factory.create(null, handles.toArray(new TreePathHandle[0]), null,  new NonRecursiveFolder[0]);
                    }
                    return ui;
                }
            };
        } else {
             task = new NodeToFileObjectTask(new HashSet<Node>(context.lookupAll(Node.class))) {

                RefactoringUI ui;
                private boolean created = false;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    if (size()==1) {
                        ui = factory.create(javac, handles.toArray(new TreePathHandle[1]), new FileObject[]{handles.iterator().next().getFileObject()}, new NonRecursiveFolder[0]);
                        created = true;
                    }
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    if (!created) {
                        if(handles.isEmpty() && selectedElements.length > 0 && "package-info".equals(selectedElements[0].getName())) {
                            ui = factory.create(null, new TreePathHandle[]{}, selectedElements, new NonRecursiveFolder[0]);
                        } else {
                            ui = factory.create(null, handles.toArray(new TreePathHandle[0]), selectedElements, pkg.toArray(new NonRecursiveFolder[0]));
                        }
                    }
                    return ui;
                }
            };
        }
        return task;
    }
    
    /**
     * utility method to perform enable/disable logic for refactoring actions
     * @param lookup
     * @param notOnlyFile action is disabled when the selection is on File
     * @param onlyFromEditor action is enabled only in editor
     * @return 
     */
    public static boolean canRefactorSingle(Lookup lookup, boolean notOnlyFile, boolean onlyFromEditor) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1) {
            return false;
        }
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            if(JavaRefactoringUtils.isRefactorable(tph.getFileObject())) {
                return !onlyFromEditor || RefactoringUtils.isFromEditor(lookup.lookup(EditorCookie.class));
            } else {
                return false;
            }
        }
        DataObject dObj = node.getLookup().lookup(DataObject.class);
        if(null == dObj) {
            return false;
        }
        FileObject fileObj = dObj.getPrimaryFile();
        if(null == fileObj || !JavaRefactoringUtils.isRefactorable(fileObj)) {
            return false;
        }

        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringUtils.isFromEditor(ec)) {
            return true;
        }
        return !notOnlyFile;
    }
    
    private abstract static class TreePathHandleTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
        private TreePathHandle current;
        private FileObject file;
        boolean renameFile;
     
        public TreePathHandleTask(Collection<? extends Node> nodes) {
            this(nodes, false);
        }
        
        public int size() {
            return handles.size();
        }
        public TreePathHandleTask(Collection<? extends Node> nodes, boolean useFirstHandle) {
            for (Node n:nodes) {
                TreePathHandle temp = n.getLookup().lookup(TreePathHandle.class);
                if (temp!=null) {
                    handles.add(temp);
                    if (useFirstHandle) {
                        break;
                    }
                }
            }
            Node n = nodes.iterator().next();
            DataObject dob = n.getLookup().lookup(DataObject.class);
            file = dob != null ? dob.getPrimaryFile() : null;
        }
        
        public TreePathHandleTask(TreePathHandle tph) {
            handles.add(tph);
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Element el = current.resolveElement(info);
            if (el!=null && el instanceof TypeElement && !((TypeElement)el).getNestingKind().isNested()) {
                if (info.getFileObject().getName().equals(el.getSimpleName().toString())) {
                    renameFile = true;
                }
            }
            treePathHandleResolved(current, info);
        }
        
        @Override
        public void run() {
            for (TreePathHandle handle:handles) {
                FileObject f = handle.getFileObject();
                if (f==null) {
                    if(file != null) {
                        f = file;
                    } else {
                        //ugly workaround for #205142
                        TopComponent top = (TopComponent) EditorRegistry.lastFocusedComponent().getParent().getParent().getParent().getParent();
                        f = top.getLookup().lookup(FileObject.class);
                    }
                }
                current = handle;
                JavaSource source = JavaSource.forFileObject(f);
                assert source != null;
                try {
                    source.runUserActionTask(this, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            TopComponent activetc = TopComponent.getRegistry().getActivated();

            RefactoringUI ui = createRefactoringUI(handles);
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
            }
        }

        /**
         * This is the place where subclasses may collect info about handles.
         * @param handle handle
         * @param javac context of running transaction
         */
        protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
        }

        protected abstract RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles);
    }    
    
    private abstract static class TextComponentTask implements Runnable, CancellableTask<CompilationController> {
        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        private boolean selection;
        
        public TextComponentTask(EditorCookie ec) {
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            this.selection = start != end && (start != -1 || end != -1);
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(final CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            final int c = selection?start:this.caret;
            final TreePath[] selectedElement = new TreePath[] {null};
//            final boolean[] insideJavadoc = {false};
            final Document doc = cc.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    selectedElement[0] = validateSelection(cc, start, end);

                    if (selectedElement[0] == null) {
                        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(cc.getTokenHierarchy(), c);
                        int adjustedCaret = c;
                        ts.move(c);
                        if (ts.moveNext() && ts.token() != null) {
                            if (ts.token().id() == JavaTokenId.IDENTIFIER) {
                                adjustedCaret = ts.offset() + ts.token().length() / 2 + 1;
                            } /*else if (ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                             TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
                             if (jdts != null && JavadocImports.isInsideReference(jdts, caret)) {
                             jdts.move(caret);
                             if (jdts.moveNext() && jdts.token().id() == JavadocTokenId.IDENT) {
                             adjustedCaret[0] = jdts.offset();
                             insideJavadoc[0] = true;
                             }
                             } else if (jdts != null && JavadocImports.isInsideParamName(jdts, caret)) {
                             jdts.move(caret);
                             if (jdts.moveNext()) {
                             adjustedCaret[0] = jdts.offset();
                             insideJavadoc[0] = true;
                             }
                             }
                             }*/
                        }
                        selectedElement[0] = cc.getTreeUtilities().pathFor(adjustedCaret);
                    }
                }
            });
            //workaround for issue 89064
            if (selectedElement[0].getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                if (!decls.isEmpty()) {
                    TreePath path = TreePath.getPath(cc.getCompilationUnit(), decls.get(0));
                    if (path!=null && cc.getTrees().getElement(path)!=null) {
                        selectedElement[0] = path;
                    }
                } else {
                    selectedElement[0] = null;
                }
            }
            ui = createRefactoringUI(selectedElement[0] != null ? TreePathHandle.create(selectedElement[0], cc) : null, start, end, cc);
        }
        
        @Override
        public final void run() {
            try {
                JavaSource source = JavaSource.forDocument(textC.getDocument());
                source.runUserActionTask(this, true);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return ;
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();

            SHOW.show(ui, activetc);
        }
        
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info);
    }

    private static final Set<TypeKind> NOT_ACCEPTED_TYPES = EnumSet.of(TypeKind.ERROR, TypeKind.NONE, TypeKind.OTHER, TypeKind.VOID);
    
    static TreePath validateSelection(CompilationInfo ci, int start, int end) {
        return validateSelection(ci, start, end, NOT_ACCEPTED_TYPES);
    }

    public static TreePath validateSelection(CompilationInfo ci, int start, int end, Set<TypeKind> ignoredTypes) {
        if(start == end) {
            TokenSequence<JavaTokenId> cts = ci.getTokenHierarchy().tokenSequence(JavaTokenId.language());

            if (cts != null) {
                cts.move(start);

                if (cts.moveNext() && cts.token().id() != JavaTokenId.WHITESPACE && cts.offset() == start) {
                    start = end += 1;
                }
            }
        }
        
        TreePath tp = ci.getTreeUtilities().pathFor(start == end? start : (start + end) / 2 + 1);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            if (   !ExpressionTree.class.isAssignableFrom(leaf.getKind().asInterface())
                && (leaf.getKind() != Tree.Kind.VARIABLE || ((VariableTree) leaf).getInitializer() == null)) {
                continue;
            }

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (start != end) {
                if (treeStart != start || treeEnd != end) {
                    continue;
                }
            } else {
                if (treeStart != start && treeEnd != end) {
                    continue;
                } 
            }

            TypeMirror type = ci.getTrees().getTypeMirror(tp);

            if (type != null && type.getKind() == TypeKind.ERROR) {
                type = ci.getTrees().getOriginalType((ErrorType) type);
            }

            if (type == null || ignoredTypes.contains(type.getKind()))
                continue;

            if(tp.getLeaf().getKind() == Tree.Kind.ASSIGNMENT)
                continue;

            if (tp.getLeaf().getKind() == Tree.Kind.ANNOTATION)
                continue;

            if (!isInsideClass(tp))
                return null;

            TreePath candidate = tp;

            tp = tp.getParentPath();

            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case VARIABLE:
                        VariableTree vt = (VariableTree) tp.getLeaf();
                        if (vt.getInitializer() == leaf) {
                            return candidate;
                        } else {
                            return null;
                        }
                    case NEW_CLASS:
                        NewClassTree nct = (NewClassTree) tp.getLeaf();
                        
                        if (nct.getIdentifier().equals(candidate.getLeaf())) { //avoid disabling hint ie inside of anonymous class higher in treepath
                            for (Tree p : nct.getArguments()) {
                                if (p == leaf) {
                                    return candidate;
                                }
                            }

                            return null;
                        }
                }

                leaf = tp.getLeaf();
                tp = tp.getParentPath();
            }

            return candidate;
        }

        return null;
    }
    
    private static boolean isInsideClass(TreePath tp) {
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()))
                return true;

            tp = tp.getParentPath();
        }

        return false;
    }
    
    private abstract static class NodeToElementTask implements Runnable, CancellableTask<CompilationController>  {
        private Node node;
        private RefactoringUI ui;
        
        public NodeToElementTask(Collection<? extends Node> nodes) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            CompilationUnitTree unit = info.getCompilationUnit();
            if (unit.getTypeDecls().isEmpty()) {
                ui = createRefactoringUI(null, info);
            } else {
                TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit, unit.getTypeDecls().get(0)),info);
                ui = createRefactoringUI(representedObject, info);
            }
        }
        
        @Override
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
            JavaSource source = JavaSource.forFileObject(o.getPrimaryFile());
            assert source != null;
            try {
                source.runUserActionTask(this, true);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (ui!=null) {
                UI.openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
            }
        }
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement, CompilationInfo info);
    }
    
    private abstract static class NodeToFileObjectTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<? extends Node> nodes;
        public ArrayList<NonRecursiveFolder> pkg;
        Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
        private Node currentNode;
     
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        protected int size() {
            return nodes.size();
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            Collection<TreePathHandle> handlesPerNode = new ArrayList<TreePathHandle>();
            CompilationUnitTree unit = info.getCompilationUnit();
            Collection<TreePathHandle> publicHandles = new ArrayList<TreePathHandle>();
            Collection<TreePathHandle> sameNameHandles = new ArrayList<TreePathHandle>();
            for (Tree t: unit.getTypeDecls()) {
                Element e = info.getTrees().getElement(TreePath.getPath(unit, t));
                if (e == null || !(e.getKind().isClass() || e.getKind().isInterface())) {
                    // syntax errors #111195
                    continue;
                }
                if (e.getSimpleName().toString().equals(info.getFileObject().getName())) {
                    TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit,t),info);
                    sameNameHandles.add(representedObject);
                }
                if (e.getModifiers().contains(Modifier.PUBLIC)) {
                    TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit,t),info);
                    publicHandles.add(representedObject);
                }
            }
            if (!publicHandles.isEmpty()) {
                handlesPerNode.addAll(publicHandles);
            } else {
                handlesPerNode.addAll(sameNameHandles);
            }

            if (!handlesPerNode.isEmpty()) {
                handles.addAll(handlesPerNode);
                nodeTranslated(currentNode, handlesPerNode, info);
            }
        }
        
        @Override
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new ArrayList<NonRecursiveFolder>();
            int i = 0;
            for (Node node:nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob!=null) {
                    fobs[i] = dob.getPrimaryFile();
                    if (RefactoringUtils.isJavaFile(fobs[i])) {
                        JavaSource source = JavaSource.forFileObject(fobs[i]);
                        assert source != null;
                        try {
                            currentNode = node;
                            // XXX this could be optimize by ClasspasthInfo in case of more than one file
                            source.runUserActionTask(this, true);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            currentNode = null;
                        }
                    }
                    final NonRecursiveFolder nonrecursivefolder = node.getLookup().lookup(NonRecursiveFolder.class);
                    if (nonrecursivefolder !=null) {
                        pkg.add(nonrecursivefolder);
                    }
                    i++;
                }
            }
            RefactoringUI ui = createRefactoringUI(fobs, handles);
            if (ui!=null) {
                UI.openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
            }
        }

        /**
         * Notifies subclasses about the translation.
         * This is the place where subclasses may collect info about handles.
         * @param node node that is translated
         * @param handles handles translated from the node
         * @param javac context of running translation
         */
        protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<TreePathHandle> handles);
    }    

    
    private static boolean nodeHandle(Lookup lookup) {
        Node n = lookup.lookup(Node.class);
        if (n!=null) {
            if (n.getLookup().lookup(TreePathHandle.class)!=null) {
                return true;
            }
        }
        return false;
    }

    public static ShowUI SHOW = new ShowUI() {
        @Override
        public void show(RefactoringUI ui, TopComponent activetc) {
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
            }
        }
    };
    public interface ShowUI {
        public void show(RefactoringUI ui, TopComponent activetc);
    }    
}
