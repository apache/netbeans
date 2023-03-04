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
package org.netbeans.modules.editor.java;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class SupportedAnnotationTypesCompletion implements Processor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        ProcessingEnvironment processingEnv = this.processingEnv.get();

        if (processingEnv == null)
            return Collections.emptyList();

        TypeElement annotationObj = processingEnv.getElementUtils().getTypeElement("java.lang.annotation.Annotation");

        if (annotationObj == null)
            return Collections.emptyList();

        Trees trees = Trees.instance(processingEnv);
        TreePath path = trees.getPath(element);

        if (path == null)
            return Collections.emptyList();

        FileObject owner;

        try {
            owner = URLMapper.findFileObject(path.getCompilationUnit().getSourceFile().toUri().toURL());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }

        ClassIndex ci = ClasspathInfo.create(owner).getClassIndex();

        if (ci == null)
            return Collections.emptyList();

        List<Completion> result = new LinkedList<Completion>();

//        for (ElementHandle<TypeElement> eh : ci.getElements(ElementHandle.create(annotationObj), EnumSet.of(SearchKind.IMPLEMENTORS), EnumSet.of(SearchScope.DEPENDENCIES, SearchScope.SOURCE))) {
//            result.add(new CompletionImpl(eh.getQualifiedName()));
//        }

        for (ElementHandle<TypeElement> eh : ci.getDeclaredTypes("", ClassIndex.NameKind.PREFIX, EnumSet.of(SearchScope.DEPENDENCIES, SearchScope.SOURCE))) {
            if (eh.getKind() != ElementKind.ANNOTATION_TYPE) continue;
            
            result.add(new CompletionImpl('\"' + eh.getQualifiedName() + '\"'));
        }

        return result;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    private static final Set<String> supportedAnnotationTypes = new HashSet<String>(Arrays.asList(SupportedAnnotationTypes.class.getName()));
    
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private Reference<ProcessingEnvironment> processingEnv;
    
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = new WeakReference<ProcessingEnvironment>(processingEnv);
    }

    private final class CompletionImpl implements Completion {

        private final String value;

        public CompletionImpl(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getMessage() {
            return null;
        }
    }

}
