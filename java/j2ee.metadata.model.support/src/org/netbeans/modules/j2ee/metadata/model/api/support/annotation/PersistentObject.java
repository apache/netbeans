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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author Andrei Badea
 */
public abstract class PersistentObject {

    private final AnnotationModelHelper helper;
    private final ElementHandle<TypeElement> typeElementHandle;

    public PersistentObject(AnnotationModelHelper helper, TypeElement typeElement) {
        this.helper = helper;
        typeElementHandle = ElementHandle.create(typeElement);
    }

    protected final AnnotationModelHelper getHelper() {
        return helper;
    }

    public final ElementHandle<TypeElement> getTypeElementHandle() {
        return typeElementHandle;
    }

    public final TypeElement getTypeElement() {
        TypeElement result = typeElementHandle.resolve(helper.getCompilationController());
        if (result == null) {
            Logger.getLogger(PersistentObject.class.getName()).log(Level.WARNING, "Type {0} has dissapeared", typeElementHandle); // NOI18N
        }
        return result;
    }
}
