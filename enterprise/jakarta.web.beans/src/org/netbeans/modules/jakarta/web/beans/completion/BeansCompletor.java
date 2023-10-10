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
package org.netbeans.modules.jakarta.web.beans.completion;

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
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;
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
