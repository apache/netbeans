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
package org.netbeans.modules.jshell.editor;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreeFactory;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.text.BreakIterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/**
 * Filters out REPL generated classes from code completion.
 * @author sdedic
 */
final class CompletionFilter extends DocTrees {

    private static final Logger LOG = Logger.getLogger(CompletionFilter.class.getName());

    private static final MethodHandle MH_GET_TYPE;
    private static final MethodHandle MH_GET_CHARACTERS;

    static {
        MethodHandle getTypeBuilder = null;
        MethodHandle getCharactersBuilder = null;
        Lookup lookup = MethodHandles.lookup();
        try {
            getTypeBuilder = lookup.findVirtual(DocTrees.class, "getType", MethodType.methodType(TypeMirror.class, DocTreePath.class));
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            LOG.log(Level.WARNING, "Failed to lookup MethodHandle for DocTrees.getType", ex);
        }
        try {
            getCharactersBuilder = lookup.findVirtual(DocTrees.class, "getCharacters", MethodType.methodType(String.class, EntityTree.class));
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            LOG.log(Level.WARNING, "Failed to lookup MethodHandle for DocTrees.getCharactersBuilder", ex);
        }
        MH_GET_TYPE = getTypeBuilder;
        MH_GET_CHARACTERS = getCharactersBuilder;
    }

    public CompletionFilter(Trees delegate) {
        this.delegate = (DocTrees)delegate;
    }

    @Override
    public BreakIterator getBreakIterator() {
        return delegate.getBreakIterator();
    }

    @Override
    public DocCommentTree getDocCommentTree(TreePath tp) {
        return delegate.getDocCommentTree(tp);
    }

    @Override
    public DocCommentTree getDocCommentTree(Element elmnt) {
        return delegate.getDocCommentTree(elmnt);
    }

    @Override
    public DocCommentTree getDocCommentTree(FileObject fo) {
        return delegate.getDocCommentTree(fo);
    }

    @Override
    public DocCommentTree getDocCommentTree(Element elmnt, String string) throws IOException {
        return delegate.getDocCommentTree(elmnt, string);
    }

    @Override
    public Element getElement(DocTreePath dtp) {
        return delegate.getElement(dtp);
    }

    @Override
    public List<DocTree> getFirstSentence(List<? extends DocTree> list) {
        return delegate.getFirstSentence(list);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence cs, DocTree dt, DocCommentTree dct, CompilationUnitTree cut) {
        delegate.printMessage(kind, cs, dt, dct, cut);
    }

    @Override
    public void setBreakIterator(BreakIterator bi) {
        delegate.setBreakIterator(bi);
    }

    @Override
    public DocSourcePositions getSourcePositions() {
        return delegate.getSourcePositions();
    }

    @Override
    public Tree getTree(Element elmnt) {
        if (elmnt == null) {
            return null;
        }
        return delegate.getTree(elmnt);
    }

    @Override
    public ClassTree getTree(TypeElement te) {
        if (te == null) {
            return null;
        }
        return delegate.getTree(te);
    }

    @Override
    public MethodTree getTree(ExecutableElement ee) {
        if (ee == null) {
            return null;
        }
        return delegate.getTree(ee);
    }

    @Override
    public Tree getTree(Element elmnt, AnnotationMirror am) {
        if (elmnt == null) {
            return null;
        }
        return delegate.getTree(elmnt, am);
    }

    @Override
    public Tree getTree(Element elmnt, AnnotationMirror am, AnnotationValue av) {
        if (elmnt == null) {
            return null;
        }
        return delegate.getTree(elmnt, am, av);
    }

    @Override
    public TreePath getPath(CompilationUnitTree cut, Tree tree) {
        if (tree == null || cut == null) {
            return null;
        }
        return delegate.getPath(cut, tree);
    }

    @Override
    public TreePath getPath(Element elmnt) {
        if (elmnt == null) {
            return null;
        }
        return delegate.getPath(elmnt);
    }

    @Override
    public TreePath getPath(Element elmnt, AnnotationMirror am) {
        if (elmnt == null) {
            return null;
        }
        return delegate.getPath(elmnt, am);
    }

    @Override
    public TreePath getPath(Element elmnt, AnnotationMirror am, AnnotationValue av) {
        if (elmnt == null) {
            return null;
        }
        return delegate.getPath(elmnt, am, av);
    }

    @Override
    public Element getElement(TreePath tp) {
        if (tp == null) {
            return null;
        }
        return delegate.getElement(tp);
    }

    @Override
    public TypeMirror getTypeMirror(TreePath tp) {
        if (tp == null) {
            return null;
        }
        return delegate.getTypeMirror(tp);
    }

    @Override
    public Scope getScope(TreePath tp) {
        if (tp == null) {
            return null;
        }
        return delegate.getScope(tp);
    }

    @Override
    public String getDocComment(TreePath tp) {
        if (tp == null) {
            return null;
        }
        return delegate.getDocComment(tp);
    }

    @Override
    public boolean isAccessible(Scope scope, TypeElement te) {
        if (te == null || scope == null) {
            return false;
        }
        if (te.getQualifiedName().toString().startsWith("REPL.") && te.getNestingKind() == NestingKind.TOP_LEVEL) {
            return false;
        }
        return delegate.isAccessible(scope, te);
    }

    @Override
    public boolean isAccessible(Scope scope, Element elmnt, DeclaredType dt) {
        return delegate.isAccessible(scope, elmnt, dt);
    }

    @Override
    public TypeMirror getOriginalType(ErrorType et) {
        return delegate.getOriginalType(et);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence cs, Tree tree, CompilationUnitTree cut) {
        delegate.printMessage(kind, cs, tree, cut);
    }

    @Override
    public TypeMirror getLub(CatchTree ct) {
        return delegate.getLub(ct);
    }

    public TypeMirror getType(DocTreePath path) {
        try {
            return (TypeMirror) MH_GET_TYPE.invoke(delegate, path);
        } catch (Throwable ex) {
            if(ex instanceof Error) {
                throw (Error) ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new IllegalStateException(ex);
            }
        }
    }

    public String getCharacters(EntityTree tree) {
        try {
            return (String) MH_GET_CHARACTERS.invoke(delegate, tree);
        } catch (Throwable ex) {
            if(ex instanceof Error) {
                throw (Error) ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new IllegalStateException(ex);
            }
        }
    }

    private DocTrees   delegate;

    @Override
    public DocTreePath getDocTreePath(FileObject fileObject, PackageElement el) {
        if (delegate == null) {
            return null;
        }
        return delegate.getDocTreePath(fileObject, el);
    }

    @Override
    public DocTreeFactory getDocTreeFactory() {
        return delegate.getDocTreeFactory();
    }
}
