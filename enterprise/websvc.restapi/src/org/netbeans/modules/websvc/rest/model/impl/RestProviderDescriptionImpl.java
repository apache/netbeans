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
package org.netbeans.modules.websvc.rest.model.impl;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.websvc.rest.model.api.RestProviderDescription;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;
import org.openide.filesystems.FileObject;

public class RestProviderDescriptionImpl extends PersistentObject implements RestProviderDescription {

    private String className;

    RestProviderDescriptionImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        this.className = typeElement.getQualifiedName().toString();
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public FileObject getFile(){
        return SourceUtils.getFile(getTypeElementHandle(), getHelper().getClasspathInfo());
    }
    
    public Status refresh(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            return Status.REMOVED;
        }

        if (!Utils.isProvider(typeElement, getHelper())) {
            return Status.REMOVED;
        }

        String newValue = typeElement.getQualifiedName().toString();
        if (!this.className.equals(newValue)) {
            this.className = newValue;
            return Status.MODIFIED;
        }

        return Status.UNMODIFIED;
    }

}
