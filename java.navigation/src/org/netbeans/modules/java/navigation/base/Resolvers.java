/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.base;

import com.sun.source.util.TreePath;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class Resolvers {


    private Resolvers() {
        throw new IllegalStateException();
    }

    public static Callable<Pair<URI,ElementHandle<TypeElement>>> createFileResolver(
            @NonNull final JavaSource js) {
        return new FileResolver(js);
    }

    public static Callable<Pair<URI,ElementHandle<TypeElement>>> createEditorResolver(
            @NonNull final JavaSource js,
            final int dot) {
        return new EditorResolver(js, dot);
    }

    private static FileObject getFileForJavaSource(@NonNull final JavaSource js) {
        final Collection<FileObject> fos = js.getFileObjects();
        if (fos.size() != 1) {
            throw new IllegalArgumentException(
                String.format("Expecting 1 file, got: %d files.",fos.size())); //NOI18N
        }
        return fos.iterator().next();
    }
    
    private static final class FileResolver implements Callable<Pair<URI,ElementHandle<TypeElement>>>{

        private final JavaSource js;
        private final FileObject fo;

        public FileResolver(
                @NonNull final JavaSource js) {
            Parameters.notNull("js", js);   //NOI18N
            this.js = js;            
            this.fo = getFileForJavaSource(js);
        }

        @Override
        public Pair<URI,ElementHandle<TypeElement>> call() throws Exception {
            final List<ElementHandle<TypeElement>> ret = new ArrayList<ElementHandle<TypeElement>>(1);
            ret.add(null);
            js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase (JavaSource.Phase.ELEMENTS_RESOLVED);
                            ret.set(0,findMainElement(cc,fo.getName()));
                        }
                    },
                    true);
            final ElementHandle<TypeElement> handle = ret.get(0);
            if (handle == null) {
                return null;
            }            
            return Pair.<URI,ElementHandle<TypeElement>>of(fo.toURI(),handle);
        }

        @CheckForNull
        static ElementHandle<TypeElement> findMainElement(
                @NonNull final CompilationController cc,
                @NonNull final String fileName) {
            final List<? extends TypeElement> topLevels = cc.getTopLevelElements();
            if (topLevels.isEmpty()) {
                return null;
            }
            TypeElement candidate = topLevels.get(0);
            for (int i = 1; i< topLevels.size(); i++) {
                if (fileName.contentEquals(topLevels.get(i).getSimpleName())) {
                    candidate = topLevels.get(i);
                    break;
                }
            }
            return ElementHandle.create(candidate);
        }
    }

    private static final class EditorResolver implements Callable<Pair<URI,ElementHandle<TypeElement>>> {

        private final JavaSource js;
        private final FileObject fo;
        private final int dot;

        public EditorResolver(
                @NonNull final JavaSource js,
                final int dot) {
            Parameters.notNull("js", js);   //NOI18N
            this.js = js;
            this.fo = getFileForJavaSource(js);
            this.dot = dot;
        }

        @Override
        public Pair<URI,ElementHandle<TypeElement>> call() throws Exception {
            final List<ElementHandle<TypeElement>> ret = new ArrayList<ElementHandle<TypeElement>>();
            ret.add(null);
            js.runUserActionTask(
                    new Task<CompilationController>(){
                        @Override
                        public void run(CompilationController cc) throws Exception {
                            cc.toPhase (JavaSource.Phase.RESOLVED);
                            Document document = cc.getDocument ();
                            if (document != null) {
                                // Find the TreePath for the caret position
                                final TreePath tp = cc.getTreeUtilities ().pathFor(dot);
                                // Get Element
                                Element element = cc.getTrees().getElement(tp);
                                if (element instanceof TypeElement) {
                                    ret.set(0, ElementHandle.create((TypeElement) element));
                                } else if (element instanceof VariableElement) {
                                    TypeMirror typeMirror = ((VariableElement) element).asType();
                                    if (typeMirror.getKind() == TypeKind.DECLARED) {
                                        element = ((DeclaredType) typeMirror).asElement();
                                        if (element != null) {
                                            ret.set(0, ElementHandle.create((TypeElement) element));
                                        }
                                    }
                                } else if (element instanceof ExecutableElement) {
                                    if (element.getKind() == ElementKind.METHOD) {
                                        TypeMirror typeMirror = ((ExecutableElement) element).getReturnType();
                                        if (typeMirror.getKind() == TypeKind.DECLARED) {
                                            element = ((DeclaredType) typeMirror).asElement();
                                            if (element != null) {
                                                ret.set(0, ElementHandle.create((TypeElement) element));
                                            }
                                        }
                                    } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                                        element = element.getEnclosingElement();
                                        if (element != null) {
                                            ret.set(0, ElementHandle.create((TypeElement) element));
                                        }
                                    }
                                } else {
                                    ret.set(0,FileResolver.findMainElement(cc, fo.getName()));
                                }
                            }
                        }
                    },
                    true);
            final ElementHandle<TypeElement> handle = ret.get(0);
            if (handle == null) {
                return null;
            }
            final FileObject file = Utils.getFile(
                handle,
                js.getClasspathInfo());
            if (file == null) {
                return null;
            }
            return Pair.<URI,ElementHandle<TypeElement>>of(file.toURI(),handle);
        }

    }    
}
