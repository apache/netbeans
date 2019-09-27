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
package org.netbeans.api.java.source.ui;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.*;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.java.BinaryElementOpen;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.ClassParser;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.ui.ElementOpenAccessor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/** Utility class for opening elements in editor.
 *
 * @author Jan Lahoda
 */
public final class ElementOpen {
    private static Logger log = Logger.getLogger(ElementOpen.class.getName());

    private ElementOpen() {
    }
    
    /**
     * Opens {@link Element} corresponding to the given {@link ElementHandle}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el     declaration to open
     * @return true  if and only if the declaration was correctly opened,
     *                false otherwise
     * @since 1.5
     */
    public static boolean open(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el) {
        final AtomicBoolean cancel = new AtomicBoolean();
        if (SwingUtilities.isEventDispatchThread() && !JavaSourceAccessor.holdsParserLock()) {
            final Object[] openInfo = new Object[3];
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        Object[] info = getOpenInfo(cpInfo, el, cancel);
                        if (info != null) {
                            openInfo[0] = info[0];
                            openInfo[1] = info[1];
                            openInfo[2] = info[2];
                        }
                    }
                },
                NbBundle.getMessage(ElementOpen.class, "TXT_CalculatingDeclPos"),
                cancel,
                false);
            if (cancel.get()) {
                return false;
            }
            if (openInfo[0] instanceof FileObject) {
                return doOpen((FileObject)openInfo[0], (int)openInfo[1], (int)openInfo[2]);                
            }
            return binaryOpen(cpInfo, el, cancel);
        } else {
            return open(cpInfo, el, cancel);
        }
    }
    
    private static boolean open(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el, AtomicBoolean cancel) {
        Object[] openInfo = getOpenInfo(cpInfo, el, cancel);
        if (cancel.get()) return false;
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            return doOpen((FileObject) openInfo[0], (int)openInfo[1], (int)openInfo[2]);
        }
        return binaryOpen(cpInfo, el, cancel);
    }
    
    private static boolean binaryOpen(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el, AtomicBoolean cancel) {
        BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);
        if (beo != null) {
            return beo.open(cpInfo, el, cancel);
        } else {
            return false;
        }        
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el    declaration to open
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(final ClasspathInfo cpInfo, final Element el) {
        return open(cpInfo, ElementHandle.create(el));
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param toSearch fileobject whose {@link ClasspathInfo} will be used
     * @param toOpen   {@link ElementHandle} of the element which should be opened.
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(
            @NonNull final FileObject toSearch,
            @NonNull final ElementHandle<? extends Element> toOpen) {
        final AtomicBoolean cancel = new AtomicBoolean();
        if (SwingUtilities.isEventDispatchThread() && !JavaSourceAccessor.holdsParserLock()) {
            final Object[] openInfo = new Object[3];
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        Object[] info = !isClassFile(toSearch) ? getOpenInfo (toSearch, toOpen, cancel) : null;
                        if (info != null) {
                            openInfo[0] = info[0];
                            openInfo[1] = info[1];
                            openInfo[2] = info[2];
                        }
                    }
                },
                NbBundle.getMessage(ElementOpen.class, "TXT_CalculatingDeclPos"),
                cancel,
                false);
            if (cancel.get()) {
                return false;
            }
            if (openInfo[0] instanceof FileObject) {
                return doOpen((FileObject)openInfo[0],(int)openInfo[1], (int)openInfo[2]);
            }
            return binaryOpen(toSearch, toOpen, cancel);
        } else {
            return open(toSearch, toOpen, cancel);
        }
    }
    
    private static boolean open(
            @NonNull final FileObject toSearch,
            @NonNull final ElementHandle<? extends Element> toOpen,
            @NonNull final AtomicBoolean cancel) {
        Parameters.notNull("toSearch", toSearch);   //NOI18N
        Parameters.notNull("toOpen", toOpen);       //NOI18N

        final Object[] openInfo = !isClassFile(toSearch) ? getOpenInfo (toSearch, toOpen, cancel) : null;
        if (cancel.get()) {
            return false;
        }
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            return doOpen((FileObject)openInfo[0],(int)openInfo[1], (int)openInfo[2]);
        }
        return binaryOpen(toSearch, toOpen, cancel);
    }
    
    private static boolean binaryOpen(
            @NonNull final FileObject toSearch,
            @NonNull final ElementHandle<? extends Element> toOpen,
            @NonNull final AtomicBoolean cancel) {
        boolean res = false;
        final BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);
        if (beo != null) {
            ClassPath bootCp = ClassPath.getClassPath(toSearch, ClassPath.BOOT);
            if (bootCp == null) {
                bootCp = JavaPlatform.getDefault().getBootstrapLibraries();
            }
            ClassPath cp = ClassPath.getClassPath(toSearch, ClassPath.COMPILE);
            if (cp == null || cp.findOwnerRoot(toSearch) == null) {
                cp = ClassPath.getClassPath(toSearch, ClassPath.EXECUTE);
                if (cp == null) {
                    cp = ClassPath.EMPTY;
                }
            }
            final ClassPath src = ClassPath.getClassPath(toSearch, ClassPath.SOURCE);
            res = beo.open(ClasspathInfo.create(bootCp, cp, src), toOpen, cancel);
        }
        return res;
    }

    /**
     * Opens given {@link TreePathHandle}.
     * @param toSearch the {@link FileObject} used to resolve the {@link TreePathHandle} in
     * @param toOpen   {@link TreePathHandle} of the {@link Tree} which should be opened.
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     * @since 1.45
     */
    public static boolean open(
            @NonNull final FileObject toSearch,
            @NonNull final TreePathHandle toOpen) {
        final AtomicBoolean cancel = new AtomicBoolean();
        if (SwingUtilities.isEventDispatchThread() && !JavaSourceAccessor.holdsParserLock()) {
            final boolean[] result = new boolean[1];
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        result[0] = open(toSearch, toOpen, cancel);
                    }
                },
                NbBundle.getMessage(ElementOpen.class, "TXT_CalculatingDeclPos"),
                cancel,
                false);
            return result[0];
        } else {
            return open(toSearch, toOpen, cancel);
        }
    }

    private static boolean open(
            @NonNull final FileObject toSearch,
            @NonNull final TreePathHandle toOpen,
            @NonNull final AtomicBoolean cancel) {
        Parameters.notNull("toSearch", toSearch);   //NOI18N
        Parameters.notNull("toOpen", toOpen);       //NOI18N
        try {
            final long[] pos = {-1, -1};
            final JavaSource js = JavaSource.forFileObject(toSearch);
            if (js != null) {
                js.runUserActionTask(new Task<CompilationController> () {
                    @Override
                    public void run(CompilationController cc) throws Exception {
                        if (cancel.get()) {
                            return;
                        }
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        final TreePath tp = toOpen.resolve(cc);
                        if (tp != null) {
                            final SourcePositions sourcePos = cc.getTrees().getSourcePositions();
                            pos[0] = sourcePos.getStartPosition(cc.getCompilationUnit(), tp.getLeaf());
                            pos[1] = sourcePos.getEndPosition(cc.getCompilationUnit(), tp.getLeaf());
                        }
                    }
                }, true);
            }
            return cancel.get() ? false : doOpen(toSearch, (int) pos[0], (int)pos[1]);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }

    // Private methods ---------------------------------------------------------

    private static boolean isClassFile(@NonNull final FileObject file) {
        return FileObjects.CLASS.equals(file.getExt()) || ClassParser.MIME_TYPE.equals(file.getMIMEType(ClassParser.MIME_TYPE));
    }

    private static Object[] getOpenInfo(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el, AtomicBoolean cancel) {
        FileObject fo = SourceUtils.getFile(el, cpInfo);
        if (fo != null && fo.isFolder()) {
            fo = fo.getFileObject("package-info.java"); // NOI18N
        }
        return fo != null ? getOpenInfo(fo, el, cancel) : null;
    }

    private static Object[] getOpenInfo(final FileObject fo, final ElementHandle<? extends Element> handle, AtomicBoolean cancel) {
        assert fo != null;
        
        try {
            int[] offset = getOffset(fo, handle, cancel);
            return new Object[] {fo, offset[0], offset[1]};
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }


    @SuppressWarnings("deprecation")
    private static boolean doOpen(FileObject fo, final int offsetA, final int offsetB) {
        if (offsetA == -1) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(ElementOpen.class, "WARN_ElementNotFound"), 
                    StatusDisplayer.IMPORTANCE_ANNOTATION);
        }
        boolean success = UiUtils.open(fo, offsetA);
        if (!success) {
            return false;
        }
        final DataObject od;
        try {
            od = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            return success;
        }
        final EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
        if (ec != null && offsetA != -1 && offsetB != -1) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (offsetB >= 0 && panes != null && panes.length > 0) {
                        JEditorPane pane = panes[0];
                        FoldHierarchy fh = FoldHierarchy.get(pane);
                        Fold f = FoldUtilities.findNearestFold(fh, offsetA);
                        // in case a fold already exists ...
                        if (f != null && f.getStartOffset() >= offsetA && f.getEndOffset() <= offsetB) {
                            fh.expand(f);
                        }
                    }
                }
            });
        }
        return success;
    }

    private static final int AWT_TIMEOUT = 1000;
    private static final int NON_AWT_TIMEOUT = 2000;

    private static int[] getOffset(final FileObject fo, final ElementHandle<? extends Element> handle, final AtomicBoolean cancel) throws IOException {
        final int[]  result = new int[] {-1, -1};
        
        final JavaSource js = JavaSource.forFileObject(fo);
        if (js != null) {
            final Task<CompilationController> t = new Task<CompilationController>() {
                public @Override void run(CompilationController info) throws IOException {
                    if (cancel.get()) {
                        return;
                    }
                    try {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                    Element el = handle.resolve(info);
                    if (el == null) {
                        if (!SourceUtils.isScanInProgress()) {
                            log.severe("Cannot resolve " + handle + ". " + info.getClasspathInfo());
                        } else {
                            Level l = Level.FINE;
                            assert (l = Level.INFO) != null;
                            log.log(l, "Cannot resolve {0} ({1})", new Object[]{handle, info.getClasspathInfo()});
                        }
                        return;
                    }
                    
                    if (el.getKind() == ElementKind.PACKAGE) {
                        // FindDeclarationVisitor does not work since there is no visitPackage.
                        // Imprecise but should usually work:
                        Matcher m = Pattern.compile("(?m)^package (.+);$").matcher(fo.asText(/*FileEncodingQuery.getEncoding(fo).name()*/)); // NOI18N
                        if (m.find()) {
                            result[0] = m.start();
                        }
                        return;
                    }

                    FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);

                    CompilationUnitTree cu = info.getCompilationUnit();

                    v.scan(cu, null);
                    Tree elTree = v.declTree;

                    if (elTree != null) {
                        result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
                        result[1] = (int)info.getTrees().getSourcePositions().getEndPosition(cu, elTree);
                    }
                }
            };

            js.runUserActionTask(t, true);
        }
        return result;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class FindDeclarationVisitor extends ErrorAwareTreePathScanner<Void, Void> {
        
        private Element element;
        private Tree declTree;
        private CompilationInfo info;
        
        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }
        
	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }
        
	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }
        
	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }

        @Override
        public Void visitModule(ModuleTree node, Void p) {
            handleDeclaration();
            super.visitModule(node, p);
            return null;
        }
    
        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());
            
            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }
    
    }

    static {
        ElementOpenAccessor.setInstance(new ElementOpenAccessor() {
            @Override
            public Object[] getOpenInfo(ClasspathInfo cpInfo, ElementHandle<? extends Element> el, AtomicBoolean cancel) {
                return ElementOpen.getOpenInfo(cpInfo, el, cancel);
            }
        });
    }

}
