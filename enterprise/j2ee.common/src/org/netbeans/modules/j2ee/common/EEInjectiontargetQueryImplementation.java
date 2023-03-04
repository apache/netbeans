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

package org.netbeans.modules.j2ee.common;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Realization for all common ee injectable targets
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class EEInjectiontargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    /** Creates a new instance of EEInjectiontargetQueryImplementation */
    public EEInjectiontargetQueryImplementation() {
    }
    
    @Override
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to EEInjectiontargetQueryImplementation.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        FileObject fo = controller.getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        J2eeProjectCapabilities j2eeProjectCapabilities = J2eeProjectCapabilities.forProject(project);
        if (j2eeProjectCapabilities == null) {
            return false;
        }
        boolean ejb31 = j2eeProjectCapabilities.isEjb31Supported() || j2eeProjectCapabilities.isEjb31LiteSupported();//it's foe ee6 only or common annotations 1.1

        if (ejb31 && !(ElementKind.INTERFACE==typeElement.getKind())) {
            
            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
            boolean found = false;

            for (AnnotationMirror m : annotations) {
                Name qualifiedName = ((TypeElement)m.getAnnotationType().asElement()).getQualifiedName();
                if (qualifiedName.contentEquals("javax.annotation.ManagedBean")) { //NOI18N
                    found = true;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }
    
    @Override
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
}
