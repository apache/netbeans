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
package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Scope;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.base.imports.UnusedImports;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixAllImports {
    
    //-J-Dorg.netbeans.modules.java.editor.imports.JavaFixAllImports.invalid_import_html="<html><font color='#808080'>"
    public static final String NOT_VALID_IMPORT_HTML = System.getProperty(JavaFixAllImports.class.getName() + ".invalid_import_html", "");
    
    private static final String PREFS_KEY = JavaFixAllImports.class.getName();
    private static final String KEY_REMOVE_UNUSED_IMPORTS = "removeUnusedImports"; // NOI18N
    private static final JavaFixAllImports INSTANCE = new JavaFixAllImports();
    
    public static JavaFixAllImports getDefault() {
        return INSTANCE;
    }
    
    /** Creates a new instance of JavaFixAllImports */
    private JavaFixAllImports() {
    }
    
    public void fixAllImports(final FileObject fo, final JTextComponent target) {
        final AtomicBoolean cancel = new AtomicBoolean();
        final JavaSource javaSource = JavaSource.forFileObject(fo);
        final AtomicReference<ImportData> id = new AtomicReference<ImportData>();
        final Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) {
                boolean removeUnusedImports;
                try {
                    wc.toPhase(Phase.RESOLVED);
                    if (cancel.get()) {
                        return;
                    }

                    final ImportData data = computeImports(wc);

                    if (cancel.get()) {
                        return;
                    }

                    if (data.shouldShowImportsPanel) {
                        if (!cancel.get()) {
                            id.set(data);
                        }
                    } else {
                        Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);
                        
                        removeUnusedImports = prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true);
                        performFixImports(wc, data, data.defaults, removeUnusedImports);
                    }
                } catch (IOException ex) {
                    //TODO: ErrorManager
                    ex.printStackTrace();
                }
            }
        };

        if (javaSource == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaFixAllImports.class, "MSG_CannotFixImports"));
        } else {
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {

                public void run() {
                    try {
                        ModificationResult mr = javaSource.runModificationTask(task);
                        GeneratorUtils.guardedCommit(target, mr);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, "Fix All Imports", cancel, false);

            if (id.get() != null && !cancel.get()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showFixImportsDialog(javaSource, target, id.get());
                    }
                });
            }
        }
    }
    
    private static List<TreePathHandle> getImportsFromSamePackage(WorkingCopy wc) {
        ImportVisitor v = new ImportVisitor(wc);
        v.scan(wc.getCompilationUnit(), null);
        return v.getImports();
    }

    private static class ImportVisitor extends ErrorAwareTreePathScanner {
        private CompilationInfo info;
        private String currentPackage;
        private List<TreePathHandle> imports;

        private ImportVisitor (CompilationInfo info) {
            this.info = info;
            ExpressionTree pkg = info.getCompilationUnit().getPackageName();
            currentPackage = pkg != null ? pkg.toString() : "";
            imports = new ArrayList<TreePathHandle>();
        }

        @Override
        public Object visitImport(ImportTree node, Object d) {
            if (node.getQualifiedIdentifier().getKind() == Kind.MEMBER_SELECT) {
                ExpressionTree exp = ((MemberSelectTree) node.getQualifiedIdentifier()).getExpression();
                if (exp.toString().equals(currentPackage)) {
                    imports.add(TreePathHandle.create(getCurrentPath(), info));
                }
            }

            super.visitImport(node, null);
            return null;
        }

        List<TreePathHandle> getImports() {
            return imports;
        }
    }

    private static void performFixImports(WorkingCopy wc, ImportData data, CandidateDescription[] selections, boolean removeUnusedImports) throws IOException {
        //do imports:
        Set<Element> toImport = new HashSet<Element>();
        Map<Name, Element> useFQNsFor = new HashMap<Name, Element>();

        CodeStyle cs = CodeStyle.getDefault(wc.getDocument());
        for (CandidateDescription cd : selections) {
            Element el = cd.toImport != null ? cd.toImport.resolve(wc) : null;

            if (el != null) {
                if (cs.useFQNs()) {
                    useFQNsFor.put(el.getSimpleName(), el);
                } else {
                    toImport.add(el);
                }
            }
        }

        CompilationUnitTree cut = wc.getCompilationUnit();

        if (!toImport.isEmpty()) {
            cut = GeneratorUtilities.get(wc).addImports(cut, toImport);
        }
        if (!useFQNsFor.isEmpty()) {
            new TreeVisitorImpl(wc, useFQNsFor).scan(cut, null);
        }
        
        boolean someImportsWereRemoved = false;
        
        if (removeUnusedImports) {
            //compute imports to remove:
            List<TreePathHandle> unusedImports = UnusedImports.computeUnusedImports(wc);
            unusedImports.addAll(getImportsFromSamePackage(wc));
            someImportsWereRemoved = !unusedImports.isEmpty();

            // make the changes to the source
            for (TreePathHandle handle : unusedImports) {
                TreePath path = handle.resolve(wc);

                assert path != null;

                cut = wc.getTreeMaker().removeCompUnitImport(cut, (ImportTree) path.getLeaf());
            }
        }

        wc.rewrite(wc.getCompilationUnit(), cut);

        if( !data.shouldShowImportsPanel ) {
            String statusText;
            if( toImport.isEmpty() && useFQNsFor.isEmpty() && !someImportsWereRemoved ) {
                Toolkit.getDefaultToolkit().beep();
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_NothingToFix" ); //NOI18N
            } else if( toImport.isEmpty() && someImportsWereRemoved ) {
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_UnusedImportsRemoved" ); //NOI18N
            } else {
                statusText = NbBundle.getMessage( JavaFixAllImports.class, "MSG_ImportsFixed" ); //NOI18N
            }
            StatusDisplayer.getDefault().setStatusText( statusText );
        }
    }

    private static ImportData computeImports(CompilationInfo info) {
        ComputeImports imps = new ComputeImports(info);
        Pair<Map<String, List<Element>>, Map<String, List<Element>>> candidates = imps.computeCandidates();

        Map<String, List<Element>> filteredCandidates = candidates.a;
        Map<String, List<Element>> notFilteredCandidates = candidates.b;

        int size = notFilteredCandidates.size();
        ImportData data = new ImportData(size);

        ReferencesCount referencesCount = ReferencesCount.get(info.getClasspathInfo());
        
        int index = 0;

        boolean shouldShowImportsPanel = false;

        for (String key : notFilteredCandidates.keySet()) {
            data.simpleNames[index] = key;

            List<Element> unfilteredVars = notFilteredCandidates.get(key);
            List<Element> filteredVars = filteredCandidates.get(key);


            shouldShowImportsPanel |= unfilteredVars.size() > 1;

            if (!unfilteredVars.isEmpty()) {
                boolean staticImports = true;
                for (Element e : unfilteredVars) {
                    if (e.getKind().isClass() || e.getKind().isInterface()) {
                        staticImports = false;
                    }
                }
                shouldShowImportsPanel |= staticImports;
                
                data.variants[index] = new CandidateDescription[staticImports ? unfilteredVars.size() + 1 : unfilteredVars.size()];

                int i = -1;
                int minImportanceLevel = Integer.MAX_VALUE;

                for (Element e : filteredVars) {
                    String displayName = imps.displayNameForImport(e);
                    Icon icon = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                    data.variants[index][++i] = new CandidateDescription(displayName, icon, ElementHandle.create(e));
                    int level = Utilities.getImportanceLevel(info, referencesCount, e);
                    if (level < minImportanceLevel) {
                        data.defaults[index] = data.variants[index][i];
                        minImportanceLevel = level;
                    }
                }
                
                if (data.defaults[index] != null)
                    minImportanceLevel = Integer.MIN_VALUE;

                for (Element e : unfilteredVars) {
                    if (filteredVars.contains(e))
                        continue;

                    String displayName = NOT_VALID_IMPORT_HTML + imps.displayNameForImport(e);
                    Icon icon = ElementIcons.getElementIcon(e.getKind(), e.getModifiers());
                    data.variants[index][++i] = new CandidateDescription(displayName, icon, ElementHandle.create(e));
                    int level = Utilities.getImportanceLevel(info, referencesCount, e);
                    if (level < minImportanceLevel) {
                        data.defaults[index] = data.variants[index][i];
                        minImportanceLevel = level;
                    }
                }

                if (staticImports) {
                    data.variants[index][++i] = new CandidateDescription(NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_DoNotImport"), //NOI18N
                                                                         ImageUtilities.loadImageIcon("org/netbeans/modules/java/editor/resources/error-glyph.gif", false), //NOI18N
                                                                         null);
                }
            } else {
                data.variants[index] = new CandidateDescription[1];
                data.variants[index][0] = new CandidateDescription(NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_CannotResolve"), //NOI18N
                                                                   ImageUtilities.loadImageIcon("org/netbeans/modules/java/editor/resources/error-glyph.gif", false), //NOI18N
                                                                   null);
                data.defaults[index] = data.variants[index][0];
            }

            index++;
        }

        data.shouldShowImportsPanel = shouldShowImportsPanel;

        return data;
    }

    static final class ImportData {
        public final String[] simpleNames;
        public final CandidateDescription[][] variants;
        public final CandidateDescription[] defaults;
        public       boolean shouldShowImportsPanel;

        public ImportData(int size) {
            simpleNames = new String[size];
            variants = new CandidateDescription[size][];
            defaults = new CandidateDescription[size];
        }
    }

    private static final RequestProcessor WORKER = new RequestProcessor(JavaFixAllImports.class.getName(), 1);
    
    private static void showFixImportsDialog(final JavaSource js, final JTextComponent target, final ImportData data) {
        final Preferences prefs = NbPreferences.forModule(JavaFixAllImports.class).node(PREFS_KEY);
        final FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

        panel.initPanel(data, prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true));

        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");
        final AtomicBoolean stop = new AtomicBoolean();
        DialogDescriptor dd = new DialogDescriptor(panel,
                                                   NbBundle.getMessage(JavaFixAllImports.class, "FixDupImportStmts_Title"), //NOI18N
                                                   true,
                                                   new Object[] {ok, cancel},
                                                   ok,
                                                   DialogDescriptor.DEFAULT_ALIGN,
                                                   HelpCtx.DEFAULT_HELP,
                                                   new ActionListener() {
                                                       public void actionPerformed(ActionEvent e) {}
                                                   },
                                                   true
                                                   );

        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                final CandidateDescription[] selections = panel.getSelections();
                final boolean removeUnusedImports = panel.getRemoveUnusedImports();
                WORKER.post(new Runnable() {
                    public void run() {
                        try {
                            ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                                public void run(WorkingCopy wc) throws Exception {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            cancel.setEnabled(false);
                                            ((JDialog)d).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                        }
                                    });                                    
                                    wc.toPhase(Phase.RESOLVED);
                                    if (stop.get()) return;
                                    performFixImports(wc, data, selections, removeUnusedImports);
                                }
                            });
                            GeneratorUtils.guardedCommit(target, mr);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        prefs.putBoolean(KEY_REMOVE_UNUSED_IMPORTS, removeUnusedImports);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                d.setVisible(false);
                            }
                        });
                    }
                });
            }
        });

        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stop.set(true);
                d.setVisible(false);
            }
        });

        d.setVisible(true);

        d.dispose();
    }

    static final class CandidateDescription {
        public final String displayName;
        public final Icon icon;
        public final ElementHandle<Element> toImport;
        public CandidateDescription(String displayName, Icon icon, ElementHandle<Element> toImport) {
            this.displayName = displayName;
            this.icon = icon;
            this.toImport = toImport;
        }
    }
    
    private static class TreeVisitorImpl extends CancellableTreePathScanner<Void, Void> {

        private WorkingCopy wc;
        private Map<Name, Element> name2Element;

        public TreeVisitorImpl(WorkingCopy wc, Map<Name, Element> name2Element) {
            this.wc = wc;
            this.name2Element = name2Element;
        }        

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            Void ret = super.visitIdentifier(node, p);
            final Element el = wc.getTrees().getElement(getCurrentPath());
            if (el != null && (el.getKind().isClass() || el.getKind().isInterface() || el.getKind() == ElementKind.PACKAGE)) {
                TypeMirror type = el.asType();
                if (type != null) {
                    if (type.getKind() == TypeKind.ERROR) {
                        boolean allowImport = true;
                        if (getCurrentPath().getParentPath() != null) {
                            if (getCurrentPath().getParentPath().getLeaf().getKind() == Kind.ASSIGNMENT) {
                                AssignmentTree at = (AssignmentTree) getCurrentPath().getParentPath().getLeaf();
                                allowImport = at.getVariable() != node;
                            } else if (getCurrentPath().getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                                Scope s = wc.getTrees().getScope(getCurrentPath());
                                while (s != null) {
                                    allowImport &= !wc.getElementUtilities().getLocalMembersAndVars(s, new ElementUtilities.ElementAcceptor() {
                                        @Override public boolean accept(Element e, TypeMirror type) {
                                            return e.getSimpleName().contentEquals(el.getSimpleName());
                                        }
                                    }).iterator().hasNext();
                                    s = s.getEnclosingScope();
                                }
                            }
                        }
                        if (allowImport) {
                            Element e = name2Element.get(node.getName());
                            if (e != null) {
                                wc.rewrite(node, wc.getTreeMaker().QualIdent(e));
                            }
                        }
                    } else if (type.getKind() == TypeKind.PACKAGE) {
                        String s = ((PackageElement) el).getQualifiedName().toString();
                        if (wc.getElements().getPackageElement(s) == null) {
                            Element e = name2Element.get(node.getName());
                            if (e != null) {
                                wc.rewrite(node, wc.getTreeMaker().QualIdent(e));
                            }
                        }
                    }
                }
            }
            return ret;
        }
    }
}
