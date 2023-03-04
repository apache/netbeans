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

package org.netbeans.modules.java.completion;

import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

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
                case RECORD:
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
