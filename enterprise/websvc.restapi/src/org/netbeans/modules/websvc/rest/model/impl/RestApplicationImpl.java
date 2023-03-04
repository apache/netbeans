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

import java.util.Collections;

import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;

/**
 *
 * @author mkuchtiak
 */
public class RestApplicationImpl extends PersistentObject implements RestApplication {
    private String applicationPath;
    private String applicationClass;

    public RestApplicationImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        applicationPath = Utils.getApplicationPath(typeElement);
        applicationClass = typeElement.getQualifiedName().toString();
    }
    
    public String getApplicationPath() {
        return applicationPath;
    }

    public String getApplicationClass() {
        return applicationClass;
    }

    public boolean refresh(TypeElement type) {
        if (!Utils.isRestApplication(type, getHelper())) {
            return false;
        }
        applicationPath = Utils.getApplicationPath(type);
        applicationClass = type.getQualifiedName().toString();
        return true;
    }

}
