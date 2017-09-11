/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.editor;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
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
import java.text.BreakIterator;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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
        if (te.getQualifiedName().toString().startsWith("REPL.")) {
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
