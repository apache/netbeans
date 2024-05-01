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

package org.netbeans.modules.beans.addproperty;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.GuardedException;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class AddPropertyCodeGenerator implements CodeGenerator {

    private JTextComponent component;
    private String className;
    private List<String> existingFields;
    private String[] pcsName;
    private String[] vcsName;

    public AddPropertyCodeGenerator(JTextComponent component, String className, List<String> existingFields, String[] pcsName, String[] vcsName) {
        this.component = component;
        this.className = className;
        this.existingFields = existingFields;
        this.pcsName = pcsName;
        this.vcsName = vcsName;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AddPropertyCodeGenerator.class, "DN_AddProperty");
    }

    @Override
    public void invoke() {
        Object o = component.getDocument().getProperty(Document.StreamDescriptionProperty);

        if (o instanceof DataObject) {
            DataObject d = (DataObject) o;

            perform(d.getPrimaryFile(), component);
        }
    }

    public void perform(FileObject file, JTextComponent pane) {
        JButton ok = new JButton(NbBundle.getMessage(AddPropertyCodeGenerator.class, "LBL_ButtonOK"));
        CodeStyle cs = CodeStyle.getDefault(pane.getDocument());
        if(cs == null) {
            cs = CodeStyle.getDefault(file);
        }
        final AddPropertyPanel addPropertyPanel = new AddPropertyPanel(file, className, cs, existingFields, pcsName, vcsName, ok);
        String caption = NbBundle.getMessage(AddPropertyCodeGenerator.class, "CAP_AddProperty");
        String cancel = NbBundle.getMessage(AddPropertyCodeGenerator.class, "LBL_ButtonCancel");
        DialogDescriptor dd = new DialogDescriptor(addPropertyPanel,caption, true, new Object[] {ok,cancel}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) == ok) {
            insertCode2(file, pane, addPropertyPanel.getAddPropertyConfig(), cs);
        }
    }

    /**
     * work around for {@link #insertCode}.
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=162853">162853</a>
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=162630">162630</a>
     */
    static void insertCode2(final FileObject file, final JTextComponent pane, final AddPropertyConfig config, CodeStyle cs) {
            final Document doc = pane.getDocument();
            final Reformat r = Reformat.get(pane.getDocument());
            final String code = new AddPropertyGenerator().generate(config, cs);
            final Position[] bounds = new Position[2];
            
            final int offset[] = new int[2];
            
        try {
            JavaSource.forFileObject(file).runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.PARSED);
                    Trees trees = parameter.getTrees();
                    TreeUtilities treeUtils = parameter.getTreeUtilities();
                    offset[0] = pane.getCaretPosition();
                    offset[1] = -1;
                    TreePath path = parameter.getTreeUtilities().pathFor(offset[0]);
                    
                    if (path==null || path.getLeaf().getKind() == Tree.Kind.CLASS) {
                        return;
                    }
                    CompilationUnitTree cut = path.getCompilationUnit();
                    if(path.getLeaf().getKind() != Tree.Kind.ENUM) {
                        while (path != null && path.getParentPath()!=null &&
                                (path.getParentPath().getLeaf().getKind() != Tree.Kind.CLASS &&
                                path.getParentPath().getLeaf().getKind() != Tree.Kind.ENUM)) {
                            path = path.getParentPath();
                        }
                    }
                    
                    int enumconstantEnd = -1;
                    int otherStart = -1;
                    if(path.getLeaf().getKind() == Tree.Kind.ENUM ||
                            (path.getParentPath() != null &&
                            path.getParentPath().getLeaf().getKind() == Tree.Kind.ENUM)) {
                        TreePath clazzPath = path.getLeaf().getKind() != Tree.Kind.ENUM ? path.getParentPath() : path;
                        ClassTree clazz = (ClassTree) clazzPath.getLeaf();
                        for (Tree tree : clazz.getMembers()) {
                            TreePath treePath = new TreePath(clazzPath, tree);
                            if(treeUtils.isSynthetic(treePath)) {
                                continue;
                            }
                            Element element;
                            if(tree.getKind() == Tree.Kind.VARIABLE && (element = trees.getElement(treePath)) != null &&
                                   element.getKind() == ElementKind.ENUM_CONSTANT) {
                                int endPosition = (int) trees.getSourcePositions().getEndPosition(cut, tree);
                                enumconstantEnd = Math.max(enumconstantEnd, endPosition);
                            } else if(otherStart == -1) {
                                otherStart = (int) trees.getSourcePositions().getStartPosition(cut, tree);
                            }
                        }
                        
                        if(enumconstantEnd == -1) {
                            enumconstantEnd = treeUtils.findBodySpan(clazz)[0] + 1;
                        }
                        
                        if(otherStart == -1) {
                            otherStart = (int) trees.getSourcePositions().getEndPosition(cut, clazz);
                        }
                        
                        int semicolon = scanForSemicolon(doc, offset, enumconstantEnd, otherStart);

                        if (semicolon == -1) {
                            offset[1] = enumconstantEnd;
                            if (offset[0] <= enumconstantEnd) {
                                offset[0] = enumconstantEnd + 1;
                            }
                        } else {
                            if (offset[0] <= semicolon) {
                                offset[0] = semicolon + 1;
                            } else if(path.getLeaf().getKind() != Tree.Kind.ENUM) {
                                Tree current = path.getLeaf();
                                offset[0] = (int) trees.getSourcePositions().getEndPosition(cut, current);
                            }
                        }
                        return;
                    }
                    
                    Tree current = path.getLeaf();
                    offset[0] = (int) trees.getSourcePositions().getEndPosition(cut, current);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
            
            r.lock();
            try {
                NbDocument.runAtomicAsUser((StyledDocument) doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GuardedSectionManager manager = GuardedSectionManager.getInstance((StyledDocument) doc);
                            if (manager != null) {
                                for (GuardedSection guard : manager.getGuardedSections()) {
                                    if (guard.contains(doc.createPosition(offset[0]), true)) {
                                        offset[0] = guard.getEndPosition().getOffset() + 1;
                                        break;
                                    }
                                }
                            }
                            
                            doc.insertString(offset[0], code, null);
                            if(offset[1] != -1) {
                                doc.insertString(offset[1], ";", null);
                            }
                            Position start = doc.createPosition(offset[0]);
                            Position end = doc.createPosition(offset[0] + code.length());
//                            r.reformat(Utilities.getRowStart(pane, start.getOffset()), Utilities.getRowEnd(pane, end.getOffset()));
                            r.reformat(start.getOffset(), end.getOffset());
                            bounds[0] = start;
                            bounds[1] = end;
                        } catch (GuardedException ex) {
                            //workaround for bug 205193
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                r.unlock();
            }

            if (bounds[0] != null) {
                // code insertion to document passed
                try {
                    JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {
                        @Override
                        public void run(WorkingCopy workingCopy) throws Exception {
                            workingCopy.toPhase(Phase.RESOLVED);

                            Position start = bounds[0];
                            Position end = bounds[1];
                            
                            new ImportFQNsHack(workingCopy, start.getOffset(), end.getOffset()).scan(workingCopy.getCompilationUnit(), null);

                            CompilationUnitTree cut = workingCopy.getCompilationUnit();

                            workingCopy.rewrite(cut, workingCopy.getTreeMaker().CompilationUnit(cut.getPackageAnnotations(), cut.getPackageName(), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                        }
                    }).commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
    }
    
    private static int scanForSemicolon(Document doc, int[] offset, int start, int end) throws BadLocationException {
        TokenHierarchy<Document> th = doc != null ? TokenHierarchy.get(doc) : null;
        List<TokenSequence<?>> embeddedSequences = th != null ? th.embeddedTokenSequences(offset[0], false) : null;
        TokenSequence<?> seq = embeddedSequences != null ? embeddedSequences.get(embeddedSequences.size() - 1) : null;

        if (seq == null) {
            return offset[0];
        }   

        seq.move(start);

        int semicolon = -1;
        while(seq.moveNext()) {
            int tokenOffset = seq.offset();
            if(tokenOffset > end) {
                break;
            }
            Token<?> t = seq.token();
            if(t != null && t.id() == JavaTokenId.SEMICOLON ) {
                semicolon = tokenOffset;
                break;
            }
        }
        return semicolon;
    }

    private static final class ImportFQNsHack extends ErrorAwareTreePathScanner<Void, Void> {

        private WorkingCopy wc;
        private int start;
        private int end;

        public ImportFQNsHack(WorkingCopy wc, int start, int end) {
            this.wc = wc;
            this.start = start;
            this.end = end;
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void p) {
            int s = (int) wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), node);
            int e = (int) wc.getTrees().getSourcePositions().getEndPosition(wc.getCompilationUnit(), node);

            if (s >= start && e <= end) {
                Element el = wc.getTrees().getElement(getCurrentPath());

                if (el != null && (el.getKind().isClass() || el.getKind().isInterface()) && ((TypeElement) el).asType().getKind() != TypeKind.ERROR) {
                    wc.rewrite(node, wc.getTreeMaker().QualIdent(el));
                    return null;
                }
            }

            return super.visitMemberSelect(node, p);
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            final SourcePositions sourcePositions = wc.getTrees().getSourcePositions();
            final TreeMaker make = wc.getTreeMaker();
            List<Tree> members = new LinkedList<Tree>();
            ClassTree classTree = node;
            for (Tree member : node.getMembers()) {
                int s = (int) sourcePositions.getStartPosition(wc.getCompilationUnit(), member);
                int e = (int) sourcePositions.getEndPosition(wc.getCompilationUnit(), member);
                if (s >= start && e <= end) {
                    classTree = make.removeClassMember(classTree, member);
                    members.add(member);
                }
            }
            classTree = GeneratorUtils.insertClassMembers(wc, classTree, members, start);
            wc.rewrite(node, classTree);
            return super.visitClass(classTree, p);
        }
    }

    public static final class Factory implements CodeGenerator.Factory {

        private static final EnumSet<Tree.Kind> TREE_KINDS = EnumSet.copyOf(TreeUtilities.CLASS_TREE_KINDS);
        static {
            TREE_KINDS.remove(Tree.Kind.RECORD); // no fields in records
        }

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController cc = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            while (path != null && !TREE_KINDS.contains(path.getLeaf().getKind())) {
                path = path.getParentPath();
            }

            if (component == null || cc == null || path == null) {
                return Collections.emptyList();
            }
            
            //find PropertyChangeSupport, VetoableChangeSupport
            //list all fields to detect collisions
            Element e = cc.getTrees().getElement(path);
            
            if (e == null || !e.getKind().isClass()) {
                return Collections.emptyList();
        }
        
            TypeMirror pcs = resolve(cc, "java.beans.PropertyChangeSupport"); //NOI18N
            TypeMirror vcs = resolve(cc, "java.beans.VetoableChangeSupport"); //NOI18N
            
            if (pcs == null || vcs == null) {
                return Collections.emptyList();
            }
            
            List<String> existingFields = new LinkedList<String>();
            String[] pcsName = new String[2];
            String[] vcsName = new String[2];
            
            for (VariableElement field : ElementFilter.fieldsIn(e.getEnclosedElements())) {
                existingFields.add(field.getSimpleName().toString());
                
                if (field.asType().equals(pcs)) {
                    int i = field.getModifiers().contains(Modifier.STATIC) ? 1 : 0;
                    
                    pcsName[i] = field.getSimpleName().toString();
                }
                
                if (field.asType().equals(vcs)) {
                    int i = field.getModifiers().contains(Modifier.STATIC) ? 1 : 0;

                    vcsName[i] = field.getSimpleName().toString();
                }
            }
            
            String className = ((TypeElement) e).getQualifiedName().toString();
            
            return Collections.singletonList(new AddPropertyCodeGenerator(component, className, existingFields, pcsName,vcsName));
        }
        
        private static TypeMirror resolve(CompilationInfo info, String s) {
            TypeElement te = info.getElements().getTypeElement(s);
            
            if (te == null) return null;
            
            return te.asType();
        }
    }
};
