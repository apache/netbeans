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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class EmbeddableImpl extends PersistentObject implements Embeddable, JavaContextListener {

    private final EntityMappingsImpl root;

    // persistent
    private String class2;

    // transient: set to null in javaContextLeft()
    private EmbeddableAttributesImpl attributes;

    public EmbeddableImpl(AnnotationModelHelper helper, EntityMappingsImpl root, TypeElement typeElement) {
        super(helper, typeElement);
        this.root = root;
        helper.addJavaContextListener(this);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    boolean refresh(TypeElement typeElement) {
        class2 = typeElement.getQualifiedName().toString();
        AnnotationModelHelper helper = getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror embeddableAnn = annByType.get("jakarta.persistence.Embeddable"); // NOI18N
        if (embeddableAnn == null) {
            embeddableAnn = annByType.get("javax.persistence.Embeddable"); // NOI18N
        }
        return embeddableAnn != null;
    }

    EntityMappingsImpl getRoot() {
        return root;
    }

    public void javaContextLeft() {
        attributes = null;
    }

    public void setClass2(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getClass2() {
        return class2;
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getAccess() {
        return getAttributes().hasFieldAccess() ? FIELD_ACCESS : PROPERTY_ACCESS;
    }

    public void setMetadataComplete(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isMetadataComplete() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAttributes(EmbeddableAttributes value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmbeddableAttributesImpl getAttributes() {
        if (attributes == null) {
            attributes = new EmbeddableAttributesImpl(this);
        }
        return attributes;
    }

    public EmbeddableAttributes newEmbeddableAttributes() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String toString() {
        return "EmbeddableImpl[class2='" + class2 + "']"; // NOI18N
    }
}
