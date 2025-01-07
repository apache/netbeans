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
                                                                                                                                                                                                                               
package org.netbeans.api.java.source;                                                                                                                                                                                          
                                                                                                                                                                                                                               
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;                                                                                                                                                                                               
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;                                                                                                                                                                                           
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;                                                                                                                                                                                        

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.lang.model.element.Element;                                                                                                                                                                                       
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;                                                                                                                                                                                         
import javax.tools.JavaFileObject;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.PositionRefProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;                                                                                                                                                                                     
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
                                                                                                                                                                                                                               
/**                                                                                                                                                                                                                            
 * Represents a handle for {@link TreePath} which can be kept and later resolved                                                                                                                                               
 * by another javac. The Javac {@link Element}s are valid only in the single                                                                                                                                                   
 * {@link javax.tools.JavaCompiler.CompilationTask} or single run of the                                                                                                                                                                    
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to                                                                                                                                                
 * keep a reference to the {@link TreePath} and use it in the other CancellableTask                                                                                                                                            
 * he has to serialize it into the {@link TreePathHandle}.                                                                                                                                                                     
 * <div class="nonnormative">                                                                                                                                                                                                  
 * <p>                                                                                                                                                                                                                         
 * Typical usage of TreePathHandle enclElIsCorrespondingEl:                                                                                                                                                                    
 * </p>                                                                                                                                                                                                                        
 * <pre>{@code                                                                                                                                                                                                       
 * final TreePathHandle[] tpHandle = new TreePathHandle[1];                                                                                                                                                                    
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {                                                                                                                                             
 *     public void run(CompilationController compilationController) {                                                                                                                                                          
 *         parameter.toPhase(Phase.RESOLVED);                                                                                                                                                                                  
 *         CompilationUnitTree cu = compilationController.getTree ();                                                                                                                                                          
 *         TreePath treePath = getInterestingTreePath (cu);                                                                                                                                                                    
 *         treePathHandle[0] = TreePathHandle.create (element, compilationController);                                                                                                                                         
 *    }                                                                                                                                                                                                                        
 * },priority);                                                                                                                                                                                                                
 *                                                                                                                                                                                        
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {                                                                                                                                        
 *     public void run(CompilationController compilationController) {                                                                                                                                                          
 *         parameter.toPhase(Phase.RESOLVED);                                                                                                                                                                                  
 *         TreePath treePath = treePathHanlde[0].resolve (compilationController);                                                                                                                                              
 *         ....                                                                                                                                                                                                                
 *    }                                                                                                                                                                                                                        
 * },priority);                                                                                                                                                                                                                
 * }</pre>                                                                                                                                                                                                                      
 * </div>                                                                                                                                                                                                                      
 *                                                                                                                                                                                                                             
 *                                                                                                                                                                                                                             
 * @author Jan Becicka                                                                                                                                                                                                         
 */                                                                                                                                                                                                                            
public final class TreePathHandle {
    private static Logger log = Logger.getLogger(TreePathHandle.class.getName());

    private final Delegate delegate;
    
    private TreePathHandle(Delegate d) {
        if (d == null) {
            throw new IllegalArgumentException();
        }
        
        this.delegate = d;
    }
    
    /**                                                                                                                                                                                                                        
     * getter for FileObject from give TreePathHandle                                                                                                                                                                          
     * @return FileObject for which was this handle created,
     *         or <code>null</code> if not available
     */                                                                                                                                                                                                                        
    public @CheckForNull FileObject getFileObject() {
        return this.delegate.getFileObject();
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Resolves an {@link TreePath} from the {@link TreePathHandle}.                                                                                                                                                           
     * @param compilationInfo representing the {@link javax.tools.JavaCompiler.CompilationTask}                                                                                                                                             
     * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.JavaCompiler.CompilationTask}.
     * @throws IllegalArgumentException when this {@link TreePathHandle} is not created for a source
     * represented by the compilationInfo.
     */                                                                                                                                                                                                                        
    public TreePath resolve (final CompilationInfo compilationInfo) throws IllegalArgumentException {
        final TreePath result = this.delegate.resolve(compilationInfo);
        if (result == null) {
            Logger.getLogger(TreePathHandle.class.getName()).info("Cannot resolve: "+toString());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreePathHandle)) {
            return false;
        }
        
        if (delegate.getClass() != ((TreePathHandle) obj).delegate.getClass()) {
            return false;
        }
        
        return delegate.equalsHandle(((TreePathHandle) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Resolves an {@link Element} from the {@link TreePathHandle}.                                                                                                                                                            
     * @param info representing the {@link javax.tools.JavaCompiler.CompilationTask}                                                                                                                                             
     * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.JavaCompiler.CompilationTask}.                                                                                                                                                        
     */                                                                                                                                                                                                                        
    public Element resolveElement(final CompilationInfo info) {
        Parameters.notNull("info", info);
        
        final Element result = this.delegate.resolveElement(info);
        if (result == null) {
            Logger.getLogger(TreePathHandle.class.getName()).info("Cannot resolve: "+toString());
        }
        return result;
    }
    
    /**
     * returns {@link ElementHandle} corresponding to this {@link TreePathHandle}
     * @return {@link ElementHandle} or null if this {@link TreePathHandle} does
     * not represent any {@link Element}
     * @since 0.93
     */
    @CheckForNull
    public ElementHandle getElementHandle() {
        return this.delegate.getElementHandle();
    }
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Returns the {@link com.sun.source.tree.Tree.Kind} of this TreePathHandle,                                                                                                                                                                   
     * it returns the kind of the {@link com.sun.source.tree.Tree} from which the handle                                                                                                                                           
     * was created.                                                                                                                                                                                                            
     *                                                                                                                                                                                                                         
     * @return {@link com.sun.source.tree.Tree.Kind}                                                                                                                                                                                               
     */                                                                                                                                                                                                                        
    public Tree.Kind getKind() {
        return this.delegate.getKind();
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Factory method for creating {@link TreePathHandle}.                                                                                                                                                                     
     *                                                                                                                                                                                                                         
     * @param treePath for which the {@link TreePathHandle} should be created.                                                                                                                                                  
     * @param info 
     * @return a new {@link TreePathHandle}                                                                                                                                                                                    
     * @throws java.lang.IllegalArgumentException if arguments are not supported
     */
    public static TreePathHandle create(final TreePath treePath, CompilationInfo info) throws IllegalArgumentException {
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("info", info);
        
        FileObject file;
        try {
            URL url = treePath.getCompilationUnit().getSourceFile().toUri().toURL();
            file = URLMapper.findFileObject(url);
            if (file == null) {
                //#155161:
                log.log(Level.INFO, "There is no fileobject for source: " + url + ". Was this file removed?");
                return new TreePathHandle(new EmptyDelegate(url, treePath.getLeaf().getKind()));
            }
        } catch (MalformedURLException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        int position = ((JCTree) treePath.getLeaf()).pos;
        if (position == (-1)) {
            int index = listChildren(treePath.getParentPath().getLeaf()).indexOf(treePath.getLeaf());
            assert index != (-1);
            return new TreePathHandle(new CountingDelegate(TreePathHandle.create(treePath.getParentPath(), info), index, treePath.getLeaf().getKind()));
        }
        Position pos = createPositionRef(file, position, Bias.Forward);
        TreePath current = treePath;
        Element correspondingElement = info.getTrees().getElement(current);
        Element element = null;
        boolean child = false;
        while (current != null) {
            if (   TreeUtilities.CLASS_TREE_KINDS.contains(current.getLeaf().getKind())
                || current.getLeaf().getKind() == Kind.VARIABLE
                || current.getLeaf().getKind() == Kind.METHOD) {
                Element loc = info.getTrees().getElement(current);
                if (isSupported(loc)) {
                    if (child && info.getTreeUtilities().isSynthetic(info.getCompilationUnit(), current.getLeaf())) {
                        // we do not support handles to statements in synth members
                        throw new IllegalArgumentException("Handle for synthetic path");
                    }
                    element = loc;
                    break;
                }
            }
            child = true;
            current = current.getParentPath();
        }
        ElementHandle<?> elementHandle = null;
        //Do not create ElementHandle for OTHER (<any>,<none>).
        if (element != null && element.getKind() != ElementKind.OTHER) {
            elementHandle = createHandle(element);
        }
        ElementHandle<?> correspondingElementHandle = null;
        if (correspondingElement != null && isSupported(correspondingElement)) {
            correspondingElementHandle = createHandle(correspondingElement);
        }
        return new TreePathHandle(new TreeDelegate(pos, new TreeDelegate.KindPath(treePath), file, elementHandle, correspondingElementHandle));
    }

    /**                                                                                                                                                                                                                        
     * Factory method for creating {@link TreePathHandle}.                                                                                                                                                                     
     *                                                                                                                                                                                                                         
     * @param element for which the {@link TreePathHandle} should be created.                                                                                                                                                  
     * @param info 
     * @return a new {@link TreePathHandle}                                                                                                                                                                                    
     * @throws java.lang.IllegalArgumentException if arguments are not supported
     */
    public static TreePathHandle create(Element element, CompilationInfo info) throws IllegalArgumentException {
        URL u = null;
        String qualName = null;
        Symbol.ClassSymbol clsSym;
        if (element instanceof Symbol.ClassSymbol) {
            clsSym = (Symbol.ClassSymbol) element;
        } else {
            clsSym = (Symbol.ClassSymbol) SourceUtils.getEnclosingTypeElement(element);
        }
        if (clsSym != null && (clsSym.classfile != null || clsSym.sourcefile != null)) {
            try {
                if (   clsSym.sourcefile != null
                    && clsSym.sourcefile.getKind() == JavaFileObject.Kind.SOURCE
                    && clsSym.sourcefile.toUri().isAbsolute()) {
                    u = clsSym.sourcefile.toUri().toURL();
                } else if (clsSym.classfile != null) {
                    u = clsSym.classfile.toUri().toURL();
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            qualName = clsSym.getEnclosingElement().getQualifiedName().toString();
        }
            
        return new TreePathHandle(new ElementDelegate(ElementHandle.create(element), u, qualName, info.getClasspathInfo()));
    }

    @CheckForNull
    private static <T extends Element> ElementHandle<T> createHandle (@NonNull final T element) {
        try {
            return ElementHandle.create(element);
        } catch (IllegalArgumentException e) {
            log.log(
                Level.INFO,
                "Unresolvable element: {0}, reason: {1}",    //NOI18N
                new Object[]{
                    element,
                    e.getMessage()
                });
            return null;
        }
    }

    private static boolean isSupported(Element el) {
        if (el == null) {
            return false;
        }
        switch (el.getKind()) {
            case PACKAGE:
            case CLASS:
            case INTERFACE:
            case ENUM:
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
            case FIELD:
            case ANNOTATION_TYPE:
            case ENUM_CONSTANT:
            case RECORD:
                //TODO: record component
            case RECORD_COMPONENT:
                return true;
            case PARAMETER:
                //only method and constructor parameters supported (not lambda):
                if (el.getEnclosingElement().getKind() == ElementKind.METHOD ||
                    el.getEnclosingElement().getKind() == ElementKind.CONSTRUCTOR) {
                    return ((ExecutableElement) el.getEnclosingElement()).getParameters().contains(el);
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    private static Position createPositionRef(FileObject file, int position, Position.Bias bias) {
        try {
            PositionRefProvider prp = PositionRefProvider.get(file);
            Position positionRef = prp != null ? prp.createPosition(position, bias) : null;
            if (positionRef != null) {
                return positionRef;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        throw new IllegalStateException("Cannot create PositionRef for file " + file.getPath() + ". CloneableEditorSupport not found");
    }

    /**Constructs a <code>TreePathHandle</code> that corresponds to the given <code>ElementHandle</code>.
     *
     * @param handle an <code>ElementHandle</code> for which the <code>TreePathHandle</code> should be constructed
     * @param cpInfo a classpath which is supposed to contain the element described by given the <code>ElementHandle</code>
     * @return a newly constructed <code>TreePathHandle</code>
     * @since 0.79
     */
    public static @NonNull TreePathHandle from(@NonNull ElementHandle<?> handle, @NonNull ClasspathInfo cpInfo) {
        Parameters.notNull("handle", handle);
        Parameters.notNull("cpInfo", cpInfo);

        return new TreePathHandle(new ElementDelegate(handle, null, null, cpInfo));
    }
    
    @Override
    public String toString() {
        return "TreePathHandle[delegate:"+delegate+"]";
    }

    static interface Delegate {
        public FileObject getFileObject();

        public TreePath resolve(final CompilationInfo compilationInfo) throws IllegalArgumentException;

        public boolean equalsHandle(Delegate obj);

        public int hashCode();

        public Element resolveElement(final CompilationInfo info);

        public Tree.Kind getKind();

        public ElementHandle getElementHandle();
    }

    private static final class TreeDelegate implements Delegate {
        
        private final Position position;
        private final KindPath kindPath;
        private final FileObject file;
        private final ElementHandle enclosingElement;
        private final ElementHandle correspondingEl;
        private final Tree.Kind kind;

        private TreeDelegate(Position position, KindPath kindPath, FileObject file, ElementHandle element, ElementHandle correspondingEl) {
            this.kindPath = kindPath;
            this.position = position;
            this.file = file;
            this.enclosingElement = element;
            this.correspondingEl = correspondingEl;
            if (kindPath != null) {
                this.kind = kindPath.kindPath.get(0);
            } else {
                if (correspondingEl != null) {
                    ElementKind k = correspondingEl.getKind();
                    switch (k) {
                        case ANNOTATION_TYPE: kind = Tree.Kind.ANNOTATION_TYPE; break;
                        case CLASS: kind = Tree.Kind.CLASS; break;
                        case ENUM: kind = Tree.Kind.ENUM; break;
                        case INTERFACE: kind = Tree.Kind.INTERFACE; break;
                        case RECORD: kind = Tree.Kind.RECORD; break;
                        case ENUM_CONSTANT: case FIELD: case RECORD_COMPONENT: kind = Tree.Kind.VARIABLE; break;
                        case METHOD: case CONSTRUCTOR: kind = Tree.Kind.METHOD; break;
                        default: kind = null; break;
                    }
                } else {
                    kind = null;
                }
            }
        }

        /**                                                                                                                                                                                                                        
         * getter for FileObject from give TreePathHandle                                                                                                                                                                          
         * @return FileObject for which was this handle created                                                                                                                                                                    
         */
        public FileObject getFileObject() {
            return file;
        }

        /**                                                                                                                                                                                                                        
         * Resolves an {@link TreePath} from the {@link TreePathHandle}.                                                                                                                                                           
         * @param compilationInfo representing the {@link javax.tools.JavaCompiler.CompilationTask}                                                                                                                                             
         * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
         * the classpath/sourcepath of {@link javax.tools.JavaCompiler.CompilationTask}.
         * @throws IllegalArgumentException when this {@link TreePathHandle} is not created for a source
         * represented by the compilationInfo.
         */
        public TreePath resolve(final CompilationInfo compilationInfo) throws IllegalArgumentException {
            assert compilationInfo != null;
            if (!compilationInfo.getFileObject().equals(getFileObject())) {
                StringBuilder debug  = new StringBuilder();
                FileObject    mine   = getFileObject();
                FileObject    remote = compilationInfo.getFileObject();
                
                debug.append("TreePathHandle [" + FileUtil.getFileDisplayName(mine) + "] was not created from " + FileUtil.getFileDisplayName(remote));
                debug.append("\n");
                debug.append("mine: id=").append(mine).append(", url=").append(mine.toURL().toExternalForm());
                debug.append("\n");
                debug.append("remote: id=").append(remote).append(", url=").append(remote.toURL().toExternalForm());

                throw new IllegalArgumentException(debug.toString());
            }
            Element element = enclosingElement != null ? enclosingElement.resolve(compilationInfo) : null;
            TreePath tp = null;
            if (element != null) {
                TreePath startPath = compilationInfo.getTrees().getPath(element);
                if (startPath == null) {
                    Logger.getLogger(TreePathHandle.class.getName()).fine("compilationInfo.getTrees().getPath(element) returned null for element %s " + element + "(" + file.getPath() + ")");    //NOI18N
                } else {
                    tp = compilationInfo.getTreeUtilities().pathFor(startPath, position.getOffset() + 1);
                }
            }
            if (tp != null && new KindPath(tp).equals(kindPath)) {
                return tp;
            }
            int pos = position.getOffset();
            tp = resolvePathForPos(compilationInfo, pos + 1);
            if (tp != null) return tp;
            tp = resolvePathForPos(compilationInfo, pos);
            return tp;
        }

        private TreePath resolvePathForPos(final CompilationInfo compilationInfo, int pos) {
            TreePath tp = compilationInfo.getTreeUtilities().pathFor(pos);
            while (tp != null) {
                KindPath kindPath1 = new KindPath(tp);
                kindPath.getList().remove(Tree.Kind.ERRONEOUS);
                if (kindPath1.equals(kindPath)) {
                    return tp;
                }
                tp = tp.getParentPath();
            }
            return null;
        }

        public boolean equalsHandle(Delegate obj) {
            TreeDelegate other = (TreeDelegate) obj;

            if (this.correspondingEl != other.correspondingEl && (this.correspondingEl == null || !this.correspondingEl.equals(other.correspondingEl))) {
                return false;
            }
            if (this.enclosingElement != other.enclosingElement && (this.enclosingElement == null || !this.enclosingElement.equals(other.enclosingElement))) {
                return false;
            }
            if (this.position == null && other.position == null) {
                return true;
            }
            // TODO: swap file and position test ?
            if (this.position.getOffset() != other.position.getOffset()) {
                return false;
            }
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            if (this.position == null) {
                return 553 + enclosingElement.hashCode();
            }
            int hash = 7;
            hash = 79 * hash + this.position.getOffset();
            hash = 79 * hash + (this.file != null ? this.file.hashCode() : 0);
            return hash;
        }

        /**                                                                                                                                                                                                                        
         * Resolves an {@link Element} from the {@link TreePathHandle}.                                                                                                                                                            
         * @param compilationInfo representing the {@link javax.tools.JavaCompiler.CompilationTask}                                                                                                                                             
         * @return resolved subclass of {@link Element} or null if the element does not exist on                                                                                                                                    
         * the classpath/sourcepath of {@link javax.tools.JavaCompiler.CompilationTask}.                                                                                                                                                        
         */
        public Element resolveElement(final CompilationInfo info) {
            if (correspondingEl != null) {
                return correspondingEl.resolve(info);
            }
            if ((this.file != null && info.getFileObject() != null) && info.getFileObject().equals(this.file) && this.position != null) {
                TreePath tp = this.resolve(info);
                if (tp == null) {
                    return null;
                }
                Element el = info.getTrees().getElement(tp);
                if (el == null) {
                    Logger.getLogger(TreePathHandle.class.toString()).fine("info.getTrees().getElement(tp) returned null for " + tp);
                    Element staticallyImported = getStaticallyImportedElement(tp, info);
                    if (staticallyImported!=null) {
                        return staticallyImported;
                    }
                } else {
                    return el;
                }
            }
            
            return null;
        }
        
        /**
         * special handling of static imports
         * see #196685
         * 
         */
        private Element getStaticallyImportedElement(TreePath treePath, CompilationInfo info) {
            if (treePath.getLeaf().getKind() != Tree.Kind.MEMBER_SELECT) 
                return null;
                
            MemberSelectTree memberSelectTree = (MemberSelectTree) treePath.getLeaf();
            TreePath tp = treePath; 
            while (tp!=null) {
                Kind treeKind = tp.getLeaf().getKind();
                if (treeKind == Tree.Kind.IMPORT) {
                    if (!((ImportTree) tp.getLeaf()).isStatic()) {
                        return null;
                    }
                    break;    
                } else if (treeKind == Tree.Kind.MEMBER_SELECT || treeKind == Tree.Kind.IDENTIFIER) {
                    tp = tp.getParentPath();
                    continue;
                }
                return null;
            }
            
            Name simpleName = memberSelectTree.getIdentifier();
            if (simpleName == null) {
                return null;
            }
            TreePath declPath  = new TreePath(new TreePath(treePath, memberSelectTree), memberSelectTree.getExpression());
            TypeElement decl = (TypeElement) info.getTrees().getElement(declPath);
            if (decl==null) {
                return null;
            }
            
            for (Element e : info.getElements().getAllMembers((TypeElement) decl)) {
                if (!e.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }
                if (!e.getSimpleName().equals(simpleName)) {
                    continue;
                }
                return e;
            }
            return null;
        }


        /**                                                                                                                                                                                                                        
         * Returns the {@link Tree.Kind} of this TreePathHandle,                                                                                                                                                                   
         * it returns the kind of the {@link Tree} from which the handle                                                                                                                                           
         * was created.                                                                                                                                                                                                            
         *                                                                                                                                                                                                                         
         * @return {@link Tree.Kind}                                                                                                                                                                                               
         */
        public Tree.Kind getKind() {
            return kind;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName()+"[kind:" + kind + ", enclosingElement:" + enclosingElement +", file:" + file + "]";
        }

        @Override
        public ElementHandle getElementHandle() {
            return correspondingEl;
        }

        static class KindPath {
            private ArrayList<Tree.Kind> kindPath = new ArrayList<>();

            KindPath(TreePath treePath) {
                while (treePath != null) {
                    kindPath.add(treePath.getLeaf().getKind());
                    treePath = treePath.getParentPath();
                }
            }

            public int hashCode() {
                return kindPath.hashCode();
            }

            public boolean equals(Object object) {
                if (object instanceof KindPath) {
                    return kindPath.equals(((KindPath) object).kindPath);
                }
                return false;
            }

            public ArrayList<Tree.Kind> getList() {
                return kindPath;
            }
        }

    }
    
    private static final class ElementDelegate implements Delegate {

        private final ElementHandle<? extends Element> el;
        private final URL source;
        private final String qualName;
        private final ClasspathInfo cpInfo;

        public ElementDelegate(ElementHandle<? extends Element> el, URL source, String qualName, ClasspathInfo cpInfo) {
            this.el = el;
            this.source = source;
            this.qualName = qualName;
            this.cpInfo = cpInfo;
        }

        public FileObject getFileObject() {
            //source does not exist
            FileObject file = SourceUtils.getFile(el, cpInfo);
            //tzezula: Very strange and probably useless
            if (file == null && source != null) {
                FileObject fo = URLMapper.findFileObject(source);
                if (fo == null) {
                    log.log(Level.INFO, "There is no fileobject for source: " +source + ". Was this file removed?");
                    return file;
                }
                file = fo;
                if (fo.getNameExt().endsWith(FileObjects.SIG)) {
                    //NOI18N
                    //conversion sig -> class
                    String pkgName = FileObjects.convertPackage2Folder(qualName);
                    StringTokenizer tk = new StringTokenizer(pkgName, "/"); //NOI18N
                    for (int i = 0; fo != null && i <= tk.countTokens(); i++) {
                        fo = fo.getParent();
                    }
                    if (fo != null) {
                        URL url = fo.toURL();
                        URL sourceRoot = null;//XXX: Index.getSourceRootForClassFolder(url);
                        if (sourceRoot != null) {
                            FileObject root = URLMapper.findFileObject(sourceRoot);
                            String resourceName = FileUtil.getRelativePath(fo, URLMapper.findFileObject(source));
                            file = root.getFileObject(resourceName.replace('.'+FileObjects.SIG, '.'+FileObjects.CLASS)); //NOI18N
                        } else {
                            Logger.getLogger(TreePathHandle.class.getName()).fine("Index.getSourceRootForClassFolder(url) returned null for url=" + url); //NOI18N
                        }
                    }
                }
            }
            
            return file;
        }

        public TreePath resolve(CompilationInfo compilationInfo) throws IllegalArgumentException {
            Element e = resolveElement(compilationInfo);
            
            if (e == null) {
                return null;
            }
            return compilationInfo.getTrees().getPath(e);
        }

        public Element resolveElement(CompilationInfo info) {
            return el.resolve(info);
        }

        public Kind getKind() {
            switch (el.getKind()) {
                case PACKAGE:
                    return Kind.COMPILATION_UNIT;
                    
                case ENUM:
                case CLASS:
                case RECORD:
                case ANNOTATION_TYPE:
                case INTERFACE:
                    return Kind.CLASS;
                    
                case ENUM_CONSTANT:
                case FIELD:
                case PARAMETER:
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case EXCEPTION_PARAMETER:
                case RECORD_COMPONENT:
                    return Kind.VARIABLE;
                    
                case METHOD:
                case CONSTRUCTOR:
                    return Kind.METHOD;
                    
                case STATIC_INIT:
                case INSTANCE_INIT:
                    return Kind.BLOCK;
                    
                case TYPE_PARAMETER:
                    return Kind.TYPE_PARAMETER;
                    
                case OTHER:
                default:
                    return Kind.OTHER;
            }
        }

        public boolean equalsHandle(Delegate obj) {
            ElementDelegate other = (ElementDelegate) obj;
            
            return el.signatureEquals(other.el) && cpInfo.equals(other.cpInfo);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(el.getSignature());
        }
        
        @Override
        public String toString() {
            return this.getClass().getSimpleName()+"[elementHandle:"+el+", url:"+source+"]";
        }

        @Override
        public ElementHandle getElementHandle() {
            return el;
        }
    }

    private static final class EmptyDelegate implements Delegate {

        private final URL source;
        private final Tree.Kind kind;

        public EmptyDelegate(URL source, Kind kind) {
            this.source = source;
            this.kind = kind;
        }

        public FileObject getFileObject() {
            return URLMapper.findFileObject(source);
        }

        public TreePath resolve(CompilationInfo compilationInfo) throws IllegalArgumentException {
            return null;
        }

        public boolean equalsHandle(Delegate obj) {
            return this == obj;
        }

        public Element resolveElement(CompilationInfo info) {
            return null;
        }

        public Kind getKind() {
            return kind;
        }

        @Override
        public ElementHandle getElementHandle() {
            return null;
        }
        
    }

    private static final class CountingDelegate implements Delegate {

        private final TreePathHandle parent;
        private final int index;
        private final Tree.Kind kind;

        public CountingDelegate(TreePathHandle parent, int index, Kind kind) {
            this.parent = parent;
            this.index = index;
            this.kind = kind;
        }

        public FileObject getFileObject() {
            return parent.getFileObject();
        }

        public TreePath resolve(CompilationInfo compilationInfo) throws IllegalArgumentException {
            TreePath p = parent.resolve(compilationInfo);

            if (p == null) return null;

            List<Tree> children = listChildren(p.getLeaf());

            if (index < children.size()) {
                Tree t = children.get(index);

                if (t.getKind() == kind) {
                    return new TreePath(p, t);
                }
            }

            return null;
        }

        public boolean equalsHandle(Delegate obj) {
            return this == obj;//XXX
        }

        public Element resolveElement(CompilationInfo info) {
            return parent.resolveElement(info);
        }

        public Kind getKind() {
            return kind;
        }

        @Override
        public ElementHandle getElementHandle() {
            return parent.getElementHandle();
        }

    }

    private static List<Tree> listChildren(@NonNull Tree t) {
        final List<Tree> result = new LinkedList<Tree>();

        t.accept(new ErrorAwareTreeScanner<Void, Void>() {
            @Override
            public Void scan(Tree node, Void p) {
                result.add(node);
                return null;
            }
        }, null);

        return result;
    }
    
}