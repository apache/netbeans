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
package org.netbeans.modules.javafx2.editor.completion.beans;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author sdedic
 */
public final class SimpleBuilderResolver implements BuilderResolver {
    @Override
    public String findBuilderClass(CompilationInfo cinfo, Source source, String beanClassName) {
        TypeElement classElement = cinfo.getElements().getTypeElement(beanClassName);
        if (classElement == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(((PackageElement)classElement.getEnclosingElement()).getQualifiedName().toString());
        if (sb.length() > 0) {
            sb.append("."); // NOI18N
        }
        sb.append(classElement.getSimpleName().toString()).append("Builder"); // NOI18N
        
        TypeElement builderEl = cinfo.getElements().getTypeElement(sb.toString());
        return builderEl != null ?
                sb.toString() :
                null;
    }
}
