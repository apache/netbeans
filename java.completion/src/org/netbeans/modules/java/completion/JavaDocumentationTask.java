/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.completion;

import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.lang.model.element.Element;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;

/**
 *
 * @author Dusan Balek
 */
public final class JavaDocumentationTask<T> extends BaseTask {

    public static <I> JavaDocumentationTask<I> create(final int caretOffset, @NullAllowed final ElementHandle<Element> element, @NonNull final DocumentationFactory<I> factory, @NullAllowed final Callable<Boolean> cancel) {
        return new JavaDocumentationTask<>(caretOffset, element, factory, cancel);
    }

    public static interface DocumentationFactory<T> {

        T create(CompilationInfo compilationInfo, Element element, final Callable<Boolean> cancel);
    }

    private final ElementHandle<Element> element;
    private final DocumentationFactory factory;
    private T documentation;

    private JavaDocumentationTask(final int caretOffset, final ElementHandle<Element> element, final DocumentationFactory factory, final Callable<Boolean> cancel) {
        super(caretOffset, cancel);
        this.element = element;        
        this.factory = factory;
    }

    public T getDocumentation() {
        return documentation;
    }
    
    @Override
    protected void resolve(CompilationController controller) throws IOException {
        controller.toPhase(JavaSource.Phase.RESOLVED);
        Element el = null;
        if (element != null) {
            el = element.resolve(controller);
        } else {
            Env e = getCompletionEnvironment(controller, false);
            if (e != null) {
                el = controller.getTrees().getElement(refinePath(e.getPath()));
            }
        }
        if (!controller.getElementUtilities().isErroneous(el)) {
            switch (el.getKind()) {
                case MODULE:
                case PACKAGE:
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case CONSTRUCTOR:
                case ENUM_CONSTANT:
                case FIELD:
                case METHOD:
                    documentation = (T)factory.create(controller, el, cancel);
            }
        }
    }

    private TreePath refinePath(final TreePath path) {
        TreePath tp = path;
        Tree last = null;
        while(tp != null) {
            if (tp.getLeaf().getKind() == Tree.Kind.MODULE && ((ModuleTree)tp.getLeaf()).getName() == last) {
                return tp;
            }
            last = tp.getLeaf();
            tp = tp.getParentPath();
        }
        return path;
    }    
}
