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

package org.netbeans.modules.maven.jaxws;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class _RetoucheUtil {

    private _RetoucheUtil() { }

    /** Get Main Class Name.
     * never call this from javac task.
     * @param classFO file object
     * @exception IOException throws when runUserActionTask fails
     * @return class name
     */
    public static String getMainClassName(final FileObject classFO) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(classFO);
        final String[] result = new String[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                result[0] = SourceUtils.getPublicTopLevelElement(controller).getQualifiedName().toString();
            }
        }, true);
        return result[0];
    }

    public static AnnotationMirror getAnnotation(CompilationController controller, 
            Element subject, String annotationType) 
    {
        List<? extends AnnotationMirror> annotations = subject
                .getAnnotationMirrors();
        for (AnnotationMirror annotation : annotations) {
            Element element = annotation.getAnnotationType().asElement();
            String fqn = null;
            if (element instanceof TypeElement) {
                fqn = ((TypeElement) element).getQualifiedName().toString();
            }
            if (annotationType.equals(fqn)) {
                return annotation;
            }
        }
        return null;
    }

}
