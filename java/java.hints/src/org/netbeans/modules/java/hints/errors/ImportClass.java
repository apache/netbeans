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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSourcePath;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.imports.ComputeImports;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports;
import org.netbeans.modules.java.hints.OrganizeImports;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public final class ImportClass implements ErrorRule<Void>{
    
    static RequestProcessor WORKER = new RequestProcessor("ImportClassEnabler", 1);
    
    public ImportClass() {
    }
    
    @Override
    public Set<String> getCodes() {
        return new HashSet<>(Arrays.asList(
                "compiler.err.cant.resolve",
                "compiler.err.cant.resolve.location",
                "compiler.err.cant.resolve.location.args",
                "compiler.err.doesnt.exist",
                "compiler.err.not.stmt", 
                "compiler.err.not.def.public.cant.access",
                "compiler.err.expected"
        ));
    }

    @Override
    public List<Fix> run(final CompilationInfo info, String diagnosticKey, final int offset, TreePath treePath, Data<Void> data) {
        resume();
        int errorPosition = offset;
        boolean expectedErr = "compiler.err.expected".equals(diagnosticKey);
        if (!expectedErr) {
            errorPosition++; //TODO: +1 required to work OK, rethink
        } 
        if (!ErrorFixesFakeHint.enabled(info.getFileObject(), ErrorFixesFakeHint.FixKind.IMPORT_CLASS)) {
            return Collections.<Fix>emptyList();
        }
        if (errorPosition == (-1)) {
            ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create errorPosition=-1"); //NOI18N
            
            return Collections.<Fix>emptyList();
        }

        TreePath path = info.getTreeUtilities().pathFor(errorPosition);
        
        Token ident = null;
        
        try {
            ident = ErrorHintsProvider.findUnresolvedElementToken(info, offset);
            // handle error 'expected identifier' after UnresolvedIdentifier.|
            if (ident == null && expectedErr) {
                TokenHierarchy<?> th = info.getTokenHierarchy();
                TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
                if (ts != null) {
                    ts.move(offset);
                    if (ts.movePrevious()) {
                        Token<JavaTokenId> check = ts.token();
                        if (check.id() == JavaTokenId.DOT) {
                            // backward skip all whitespaces
                            boolean hasToken = false;
                            while ((hasToken = ts.movePrevious()) && ts.token().id() == JavaTokenId.WHITESPACE)
                                ;
                            if (hasToken && ts.token().id() == JavaTokenId.IDENTIFIER) {
                                ident = ts.token();
                            }
                        }
                    }
                    
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create ident={0}", ident); //NOI18N
        
        if (ident == null) {
            return Collections.<Fix>emptyList();
        }
        
        FileObject file = info.getFileObject();
        boolean useFQN = false;
        if (file != null) {
                CodeStyle cs = CodeStyle.getDefault(file);
                useFQN = cs.useFQNs();
        }
        String simpleName = ident.text().toString();
        ComputeImports imps = getCandidateFQNs(info, file, simpleName, data);
        if (imps == null) {
            return Collections.<Fix>emptyList();
        }
        List<Element> cands = imps.getCandidates(simpleName);

        //workaround for #118714 -- neverending import
        List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
        for (ImportTree it : imports) {
            Element el = info.getTrees().getElement(new TreePath(new TreePath(new TreePath(info.getCompilationUnit()), it), it.getQualifiedIdentifier()));

            if (el != null && cands != null) {
                List<Element> a = cands;
                if (a != null && a.contains(el)) {
                    return Collections.<Fix>emptyList();
                }
            }
        }

        if (isCancelled()) {
            ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.cancelled."); //NOI18N
            
            return CreatorBasedLazyFixList.CANCELLED;
        }

        String replaceSuffix = null;
        TreePath changePath = null;

        if (useFQN) {
            changePath = path;
            replaceSuffix = ""; // NOI18N
        } else if (path.getLeaf().getKind() == Kind.IMPORT) {
            //for import package.*;, the error points to the import tree:
            Tree star = ((ImportTree) path.getLeaf()).getQualifiedIdentifier();

            if (star.getKind() == Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree) star;
                if (mst.getIdentifier().contentEquals("*")) {
                    replaceSuffix = ".*";
                    changePath = new TreePath(new TreePath(path, star), mst.getExpression());
                }
            }
        } else {
            StringBuilder replaceSuffixBuilder = new StringBuilder();
            TreePath imp = path.getParentPath();

            while (imp != null && imp.getLeaf().getKind() == Kind.MEMBER_SELECT) {
                replaceSuffixBuilder.append(".");
                replaceSuffixBuilder.append(((MemberSelectTree) imp.getLeaf()).getIdentifier());
                imp = imp.getParentPath();
            }

            if (imp != null && imp.getLeaf().getKind() == Kind.IMPORT) {
                replaceSuffix = replaceSuffixBuilder.toString();
                changePath = path;
            }
        }
        
        Preferences prefs = ErrorFixesFakeHint.getPreferences(info.getFileObject(), 
                ErrorFixesFakeHint.FixKind.IMPORT_CLASS);
        boolean doOrganize = ErrorFixesFakeHint.isOrganizeAfterImportClass(prefs);

        List<Element> filtered = cands;
        List<Element> unfiltered = imps.getRawCandidates(simpleName);
        List<Fix> fixes = new ArrayList<>();
        
        if (unfiltered != null && filtered != null) {
            ReferencesCount referencesCount = ReferencesCount.get(info.getClasspathInfo());
            Set<String> uniq = new HashSet<>();
            
            for (Element element : unfiltered) {
                String fqn = imps.displayNameForImport(element);
                if (!uniq.add(fqn)) {
                    continue;
                }
                if (org.netbeans.modules.java.completion.Utilities.isExcluded(fqn)) {
                    continue;
                }
                StringBuilder sort = new StringBuilder();
                
                sort.append("0001#");
                
                boolean prefered = filtered.contains(element);
                
                if (prefered) {
                    sort.append("A#");
                } else {
                    sort.append("Z#");
                }
                
                int order = Utilities.getImportanceLevel(info, referencesCount, element);
                String orderString = Integer.toHexString(order);
                
                sort.append("00000000".substring(0, 8 - orderString.length()));
                sort.append(orderString);
                sort.append('#');
                sort.append(fqn);

                ElementHandle<Element> eh = ElementHandle.create(element);
                if (useFQN) {
                    fixes.add(new UseFQN(info, file, fqn, eh, "Z#" + fqn, treePath, prefered, false));
                    fixes.add(UseFQN.createShared(info, file, fqn, eh, fqn, treePath, prefered));
                } else {
                    fixes.add(new FixImport(file, info.getJavaSourcePath(), fqn, eh, sort.toString(),
                            prefered, info, changePath, replaceSuffix, doOrganize));
                }
            }
        }
        
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.create finished."); //NOI18N

        return fixes;
    }
    
    @Override
    public synchronized void cancel() {
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.cancel called."); //NOI18N
        
        cancelled = true;
        
        if (compImports != null) {
            compImports.cancel();
        }
    }
    
    @Override
    public String getId() {
        return ImportClass.class.getName();
    }
    
    @Override
    public String getDisplayName() {
        return "Add Import Fix";
    }
    
    public String getDescription() {
        return "Add Import Fix";
    }
    
    private synchronized void resume() {
        ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.resume called."); //NOI18N
        
        cancelled = false;
    }
    
    private synchronized boolean isCancelled() {
        return cancelled;
    }
    
    private boolean cancelled;
    private ComputeImports compImports;
    
    private synchronized void setComputeImports(ComputeImports compImports) {
        this.compImports = compImports;
    }
    
    public ComputeImports getCandidateFQNs(CompilationInfo info, FileObject file, String simpleName, Data<Void> data) {
            //compute imports:
            ComputeImports imp = new ComputeImports(info);
            setComputeImports(imp);
            
            ComputeImports.Pair<Map<String, List<Element>>, Map<String, List<Element>>> rawCandidates;
            try {
                imp = imp.computeCandidatesEx();
            } finally {
                setComputeImports(null);
            }
            if (isCancelled()) {
                ErrorHintsProvider.LOG.log(Level.FINE, "ImportClassEnabler.getCandidateFQNs cancelled, returning."); //NOI18N
                return null;
            }
            return imp;
    }
    
    @Messages("WRN_FileInvalid=Cannot resolve file - already deleted?")
    abstract static class FixBase implements EnhancedFix {
        static final Logger LOG = Logger.getLogger(FixImport.class.getName());
        protected final String fqn;
        protected final ElementHandle<Element> toImport;
        protected final boolean isValid;
        private final FileObject file;
        private final JavaSourcePath path;
        private final String sortText;

        protected WorkingCopy copy;
        
        private FixBase(FileObject file, JavaSourcePath path, String fqn, ElementHandle<Element> toImport, String sortText, boolean isValid) {
            this.isValid = isValid;
            this.file = file;
            this.path = path;
            this.fqn = fqn;
            this.toImport = toImport;
            this.sortText = sortText;
        }
        
        protected abstract boolean perform();
        protected abstract void performDoc(Document doc);
        
        private boolean needsChange;
        
        public ChangeInfo implement() throws IOException, ParseException {
            Task task = new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws Exception {
                    if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }
                    FixBase.this.copy = copy;
                    try {
                        needsChange = false; // for case of an error
                        needsChange = perform();
                    } finally {
                        FixBase.this.copy = null;
                    }
                }
            };
            if (path != null) {
                do {
                    ModificationResult.runModificationTask(path, task).commit();
                } while (needsChange);
            } else {
                DataObject od;
                
                try {
                    od = DataObject.find(file);
                } catch (DataObjectNotFoundException donfe) {
                    LOG.log(Level.INFO, null, donfe);
                    StatusDisplayer.getDefault().setStatusText(Bundle.WRN_FileInvalid());
                    return null;
                }
                
                EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
                Document doc = ec != null ? ec.openDocument() : null;
                if (doc != null) {
                    performDoc(doc);
                }
            }
            return null;
        }

        @Override
        public int hashCode() {
            return fqn.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (getClass().isInstance(o)) {
                return fqn.equals(((FixBase) o).fqn);
            }
            return false;
        }

        @Override
        public CharSequence getSortText() {
            return sortText;
        }
    }
    
    static final class UseFQN extends FixBase {
        private final boolean all;
        
        static UseFQN createShared(CompilationInfo info, 
                FileObject file, 
                String fqn, 
                ElementHandle<Element> toImport, 
                String sortText, TreePath replacePath, boolean isValid) {
            
            String k = UseFQN.class.getName() + "#" + fqn; // NOI18N
            Object o = info.getCachedValue(k);
            UseFQN inst;
            if (o instanceof UseFQN) {
                inst = (UseFQN)o;
                inst.addTreePath(TreePathHandle.create(replacePath, info));
            } else {
                inst = new UseFQN(info, file, fqn, toImport, sortText, replacePath, isValid, true);
                info.putCachedValue(k, inst, CompilationInfo.CacheClearPolicy.ON_TASK_END);
            }
            return inst;
        }
        
        private final TreePathHandle replacePathHandle;
        private final Collection<TreePathHandle> additionalLocations = new ArrayList<>();
        private final String sn;
        
        public UseFQN(CompilationInfo info, FileObject file, String fqn, ElementHandle<Element> toImport, String sortText, TreePath replacePath, boolean isValid, 
                boolean all) {
            super(file, info.getJavaSourcePath(), fqn, toImport, sortText, isValid);
            this.sn = replacePath.getLeaf().toString();
            this.replacePathHandle = TreePathHandle.create(replacePath, info);
            this.all = all;
        }
        
        void addTreePath(TreePathHandle toReplace) {
            this.additionalLocations.add(toReplace);
        }

        @Override
        protected boolean perform() {
            TreePath replacePath = replacePathHandle.resolve(copy);

            if (replacePath == null) {
                Logger.getAnonymousLogger().warning(String.format("Attempt to change import for FQN: %s, but the import cannot be resolved in the current context", fqn));
                return false;
            }
            Element el = toImport.resolve(copy);
            if (el == null) {
                return false;
            }
            CharSequence elFQN = copy.getElementUtilities().getElementName(el, true);
            IdentifierTree id = copy.getTreeMaker().Identifier(elFQN);
            copy.rewrite(replacePath.getLeaf(), id);
            
            for (TreePathHandle tph : additionalLocations) {
                replacePath = tph.resolve(copy);
                if (replacePath == null) {
                    continue;
                }
                copy.rewrite(replacePath.getLeaf(), id);
            }
            return false;
        }

        @Override
        protected void performDoc(Document doc) {
            // ???
        }
        
        public String getText() {
            String displayName = all ? 
                    NbBundle.getMessage(ImportClass.class, "Use_FQN_for_All_X", fqn, sn):
                    NbBundle.getMessage(ImportClass.class, "Use_FQN_for_X", fqn);
            if (isValid) {
                return displayName;
            } else {
                return JavaFixAllImports.NOT_VALID_IMPORT_HTML + displayName;
            }
        }

        @Override
        public boolean equals(Object o) {
            boolean x = super.equals(o);
            if (x) {
                x = ((UseFQN)o).all == all;
            }
            return x;
        }
        
        
    }
    
    public static final class FixImport extends FixBase {
        private final @NullAllowed TreePathHandle replacePathHandle;
        private final @NullAllowed String suffix;
        private final boolean statik;
        private final boolean doOrganize;
        String requiresModName;
        boolean moduleAdded;
        int round;
        
        public FixImport(FileObject file, JavaSourcePath path, String fqn, ElementHandle<Element> toImport, String sortText, boolean isValid, CompilationInfo info, @NullAllowed TreePath replacePath, @NullAllowed String replaceSuffix,
                boolean doOrganize) {
            super(file, path, fqn, toImport, sortText, isValid);
            if (replacePath != null) {
                this.replacePathHandle = TreePathHandle.create(replacePath, info);
                this.suffix = replaceSuffix;
                while (replacePath != null && replacePath.getLeaf().getKind() != Kind.IMPORT) {
                    replacePath = replacePath.getParentPath();
                }
                this.statik = replacePath != null ? ((ImportTree) replacePath.getLeaf()).isStatic() : false;
            } else {
                this.replacePathHandle = null;
                this.suffix = null;
                this.statik = false;
            }
            this.doOrganize = doOrganize;
        }

        @Messages("Change_to_import_X=Change to import {1}{0}")
        @Override
        public String getText() {
            String displayName = replacePathHandle == null ? NbBundle.getMessage(ImportClass.class, "Add_import_for_X", new Object[] {fqn}) : Bundle.Change_to_import_X(fqn + suffix, statik ? "static " : "");
            if (isValid) {
                return displayName;
            } else {
                return JavaFixAllImports.NOT_VALID_IMPORT_HTML + displayName;
            }
        }

        @Override
        protected void performDoc(Document doc) {
            String topLevelLanguageMIMEType = doc != null ? NbEditorUtilities.getMimeType(doc) : null;
            if (topLevelLanguageMIMEType != null) {
                Lookup lookup = MimeLookup.getLookup(MimePath.get(topLevelLanguageMIMEType));
                Collection<? extends ImportProcessor> instances = lookup.lookupAll(ImportProcessor.class);

                for (ImportProcessor importsProcesor : instances) {
                    importsProcesor.addImport(doc, fqn);
                }
            }
        }

        @Override
        protected boolean perform() {
            round++;
            if (replacePathHandle != null) {
                TreePath replacePath = replacePathHandle.resolve(copy);

                if (replacePath == null) {
                    Logger.getAnonymousLogger().warning(String.format("Attempt to change import for FQN: %s, but the import cannot be resolved in the current context", fqn));
                    return false;
                }

                copy.rewrite(replacePath.getLeaf(), copy.getTreeMaker().Identifier(fqn));
                return false;
            }

            Element te = toImport.resolve(copy);
            JavaSource modInfoSrc = null;
            if (te == null && round < 2) {
                // check if the project is Source Level 9 and the sources indeed contain module-info.java
                FileObject modInfo = org.netbeans.modules.java.hints.errors.Utilities.getModuleInfo(copy);
                if (modInfo != null) {
                    // attempt to resolve/find the elememt in the dependencies, then add the correct module.
                    ClasspathInfo cpInfo = copy.getClasspathInfo();
                    ClasspathInfo extraInfo = ClasspathInfo.create(
                            ClassPathSupport.createProxyClassPath(
                                    cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                                    cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_BOOT)),
                            ClassPathSupport.createProxyClassPath(
                                    cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE),
                                    cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_COMPILE),
                                    cpInfo.getClassPath(ClasspathInfo.PathKind.MODULE_CLASS)),
                            cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE));
                    modInfoSrc = JavaSource.create(extraInfo, modInfo);
                    try {
                        modInfoSrc.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(CompilationController parameter) throws Exception {
                                parameter.toPhase(Phase.RESOLVED);
                                Element x = toImport.resolve(parameter);
                                if (x == null) {
                                    return;
                                }
                                // find the toplevel class:
                                
                                while (x.getEnclosingElement() != null && x.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                                    x = x.getEnclosingElement();
                                }
                                if (x == null || !(x.getKind().isClass() || x.getKind().isInterface())) {
                                    return;
                                }
                                TypeElement tel = (TypeElement)x;
                                String res = tel.getQualifiedName().toString().replace('.', '/');
                                ClassPath compPath = ClassPathSupport.createProxyClassPath(
                                        extraInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                                        extraInfo.getClassPath(ClasspathInfo.PathKind.COMPILE));
                                FileObject f = compPath.findResource(res + ".class");
                                if (f == null) {
                                    return;
                                }
                                FileObject rf = compPath.findOwnerRoot(f);
                                URL u = URLMapper.findURL(rf, URLMapper.INTERNAL);
                                if (u == null) {
                                    return;
                                }
                                requiresModName = SourceUtils.getModuleName(u);
                            }
                        }, true);
                        if (requiresModName == null) {
                            Logger.getAnonymousLogger().warning(String.format("Attempt to fix import for FQN: %s, which does not have a TypeElement in currect context", fqn));
                            return false;
                        }
                        Set<String> modules = new HashSet<>();
                        modules.add(requiresModName);
                        addRequiredModules(modInfo, modInfoSrc, modules);
                        if (moduleAdded) {
                            te = toImport.resolve(copy);
                            // make the actual import in the next round
                            return true;
                        } else {
                            Logger.getAnonymousLogger().warning(String.format("Could not add module %s for fqn %s", requiresModName, fqn));
                            return false;
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            if (te == null) {
                Logger.getAnonymousLogger().warning(String.format("Attempt to fix import for FQN: %s, which does not have a TypeElement in currect context", fqn));
                return false;
            }

            if (doOrganize) {
                OrganizeImports.doOrganizeImports(copy, Collections.singleton(te), false);
            } else {
                CompilationUnitTree cut = GeneratorUtilities.get(copy).addImports(
                    copy.getCompilationUnit(),
                    Collections.singleton(te)
                );                        
                copy.rewrite(copy.getCompilationUnit(), cut);
            }
            return false;
        }
        
        private void addRequiredModules(FileObject fo, JavaSource js, Set<String> moduleNames) throws IOException {
            js.runModificationTask((wc) -> {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final CompilationUnitTree cu = wc.getCompilationUnit();
                final Set<String> knownModules = new HashSet<>();
                final ModuleTree[] module = new ModuleTree[1];
                final RequiresTree[] lastRequires = new RequiresTree[1];
                cu.accept(new TreeScanner<Void, Void>() {
                            @Override
                            public Void visitModule(ModuleTree m, Void p) {
                                module[0] = m;
                                return super.visitModule(m, p);
                            }
                            @Override
                            public Void visitRequires(RequiresTree r, Void p) {
                                lastRequires[0] = r;
                                knownModules.add(r.getModuleName().toString());
                                return super.visitRequires(r, p);
                            }
                        },
                        null);
                if (module[0] != null) {
                    moduleNames.removeAll(knownModules);
                    moduleAdded = !moduleNames.isEmpty();
                    final TreeMaker tm = wc.getTreeMaker();
                    final List<RequiresTree> newRequires = moduleNames.stream()
                            .map((name) -> tm.Requires(false, false, tm.QualIdent(name)))
                            .collect(Collectors.toList());

                    final List<DirectiveTree> newDirectives = new ArrayList<>(
                            module[0].getDirectives().size() + newRequires.size());
                    if (lastRequires[0] == null) {
                        newDirectives.addAll(newRequires);
                    }
                    for (DirectiveTree dt : module[0].getDirectives()) {
                        newDirectives.add(dt);
                        if (dt == lastRequires[0]) {
                            newDirectives.addAll(newRequires);
                        }
                    }
                    final ModuleTree newModule = tm.Module(
                            tm.Modifiers(0, module[0].getAnnotations()),
                            module[0].getModuleType(),
                            module[0].getName(),
                            newDirectives);
                    wc.rewrite(module[0], newModule);
                }                    
            }).commit();
            // needs to be saved so that resolution will consider the added module
            SaveCookie s = fo.getLookup().lookup(SaveCookie.class);
            if (s != null) {
                s.save();
            }
        }

        public ElementHandle<Element> getToImport() {
            return toImport;
        }

    }
}
