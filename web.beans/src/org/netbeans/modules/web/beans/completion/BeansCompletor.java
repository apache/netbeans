/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.beans.completion;

import java.io.IOException;
import java.util.*;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Various completor for code completing XML tags and attributes 
 * 
 */
public abstract class BeansCompletor {

    private int anchorOffset = -1;

    public enum TAG {

        CLASS, STEREOTYPE
    };

    TAG tag;

    public abstract List<BeansCompletionItem> doCompletion(CompletionContext context);

    BeansCompletor(TAG tag) {
        this.tag = tag;
    }

    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * A completor for completing class tag
     */
    public static class JavaClassesCompletor extends BeansCompletor {

        JavaClassesCompletor(TAG tag) {
            super(tag);
        }

        @Override
        public List<BeansCompletionItem> doCompletion(final CompletionContext context) {
            final List<BeansCompletionItem> results = new ArrayList<>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = Utils.getJavaSource(doc);
                if (js == null) {
                    return Collections.emptyList();
                }
                FileObject fo = NbEditorUtilities.getFileObject(context.getDocument());
                doJavaCompletion(fo, js, results, typedChars, context.getCurrentTokenOffset());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return results;
        }

        private void doJavaCompletion(final FileObject fo, final JavaSource js, final List<BeansCompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    Set<ElementHandle<TypeElement>> declaredTypes = null;
                    declaredTypes = cc.getClasspathInfo().getClassIndex().getDeclaredTypes(typedPrefix, ClassIndex.NameKind.PREFIX, Collections.singleton(ClassIndex.SearchScope.SOURCE));//to have dependencies: EnumSet.allOf(ClassIndex.SearchScope.class)

                    // add classes 
                    if (declaredTypes != null && declaredTypes.size() > 0) {
                        for (ElementHandle<TypeElement> cl : declaredTypes) {
                            ElementKind kind = cl.getKind();
                            switch (tag) {
                                case CLASS: {

                                    if (kind == ElementKind.CLASS) {
                                        TypeElement te = cl.resolve(cc);
                                        if (isAlternative(te)) {

                                            BeansCompletionItem item = BeansCompletionItem.createBeansTagValueItem(substitutionOffset-typedPrefix.length(), cl.getQualifiedName(), te.getSimpleName().toString());
                                            results.add(item);
                                        }
                                    }

                                }
                                break;
                                case STEREOTYPE: {
                                    if (kind == ElementKind.ANNOTATION_TYPE) {
                                        TypeElement te = cl.resolve(cc);
                                        if (isAlternative(te)) {

                                            BeansCompletionItem item = BeansCompletionItem.createBeansTagValueItem(substitutionOffset-typedPrefix.length(), cl.getQualifiedName(), te.getSimpleName().toString());
                                            results.add(item);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }, true);

            setAnchorOffset(substitutionOffset);
        }
    }

    private static boolean isAlternative(TypeElement te) {
        List<? extends AnnotationMirror> annotationMirrors = te.getAnnotationMirrors();
        for (AnnotationMirror annotation : annotationMirrors) {
            if (annotation.getAnnotationType().asElement() instanceof TypeElement) {
                String typeName = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName().toString();
                if (AnnotationUtil.ALTERNATVE.equals(typeName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
