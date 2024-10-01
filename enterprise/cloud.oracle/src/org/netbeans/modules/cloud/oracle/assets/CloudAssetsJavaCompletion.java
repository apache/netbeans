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
package org.netbeans.modules.cloud.oracle.assets;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;

/**
 *
 * @author Dusan Balek
 */
public class CloudAssetsJavaCompletion implements Processor {

    private static final String DATA_SOURCE = "java.sql.DataSource";
    private static final String JAKARTA_NAMED_ANNOTATION = "jakarta.inject.Named";
    private static final String MICRONAUT_JDBC_REPOSITORY_ANNOTATION = "io.micronaut.data.jdbc.annotation.JdbcRepository";
    private static final String MICRONAUT_OBJECT_STORAGE = "io.micronaut.objectstorage.ObjectStorageOperations";
    private static final Set<String> supportedAnnotationTypes = Set.of(JAKARTA_NAMED_ANNOTATION, MICRONAUT_JDBC_REPOSITORY_ANNOTATION);

    private Reference<ProcessingEnvironment> processingEnv;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        switch (((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName().toString()) {
            case JAKARTA_NAMED_ANNOTATION:
                if ("value".contentEquals(member.getSimpleName())) {
                    return CloudAssetsJavaCompletion.completeReferenceNamesByClass(element2ReferenceClass(element));
                }
                break;
            case MICRONAUT_JDBC_REPOSITORY_ANNOTATION:
                if ("dataSource".contentEquals(member.getSimpleName())) {
                    return CloudAssetsJavaCompletion.completeReferenceNamesByClass(DatabaseItem.class);
                }
                break;
        }
        return Collections.emptyList();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = new WeakReference<>(processingEnv);
    }

    private Class<? extends OCIItem> element2ReferenceClass(Element element) {
        TypeMirror tm = element.asType();
        if (tm.getKind() == TypeKind.EXECUTABLE) {
            tm = ((ExecutableType) tm).getReturnType();
        }
        if (tm.getKind() == TypeKind.DECLARED) {
            Elements elements = this.processingEnv.get().getElementUtils();
            Types types = this.processingEnv.get().getTypeUtils();
            tm = types.erasure(tm);
            TypeElement dataSource = elements.getTypeElement(DATA_SOURCE);
            if (dataSource != null && types.isAssignable(tm, dataSource.asType())) {
                return DatabaseItem.class;
            }
            TypeElement objectStorage = elements.getTypeElement(MICRONAUT_OBJECT_STORAGE);
            if (objectStorage != null && types.isAssignable(tm, objectStorage.asType())) {
                return BucketItem.class;
            }
        }
        return null;
    }

    private static Iterable<? extends Completion> completeReferenceNamesByClass(Class<? extends OCIItem> cls) {
        if (cls == null) {
            return Collections.emptyList();
        }
        String format = "\"%s\"";
        return CloudAssets.getDefault().getReferenceNamesByClass(cls).stream().map(name -> new Completion() {
            @Override
            public String getValue() {
                return String.format(format, name);
            }

            @Override
            public String getMessage() {
                return null;
            }
        }).collect(Collectors.toList());
    }
}
