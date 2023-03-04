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
package org.netbeans.modules.j2ee.jpa.verification.fixes;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CreateOneToManyRelationshipHint extends AbstractCreateRelationshipHint {
    
    public CreateOneToManyRelationshipHint(FileObject fileObject,
            ElementHandle<TypeElement> classHandle,
            AccessType accessType,
            String localAttrName,
            String targetEntityClassName) {
        super(fileObject, classHandle, accessType, localAttrName, targetEntityClassName,
                JPAAnnotations.ONE_TO_MANY, JPAAnnotations.MANY_TO_ONE);
    }
    
    @Override protected CreateRelationshipPanel.AvailableSelection getAvailableRelationTypeSelection() {
        return CreateRelationshipPanel.AvailableSelection.INVERSE_ONLY;
    }
}
