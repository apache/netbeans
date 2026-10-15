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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.AttributesHelper.PropertyHandler;

public class EmbeddableAttributesImpl implements EmbeddableAttributes, PropertyHandler {

    private final EmbeddableImpl embeddable;
    private final AttributesHelper attrHelper;

    private final List<Basic> basicList = new ArrayList<Basic>();

    public EmbeddableAttributesImpl(EmbeddableImpl embeddable) {
        this.embeddable = embeddable;
        attrHelper = new AttributesHelper(embeddable.getRoot().getHelper(), embeddable.getTypeElement(), this);
        attrHelper.parse();
    }

    public boolean hasFieldAccess() {
        return attrHelper.hasFieldAccess();
    }

    public void handleProperty(Element element, String propertyName) {
        AnnotationModelHelper helper = embeddable.getRoot().getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(element.getAnnotationMirrors());
        if (EntityMappingsUtilities.isTransient(annByType, element.getModifiers())) {
            return;
        }

        AnnotationMirror columnAnnotation = annByType.get("jakarta.persistence.Column"); // NOI18N
        if (columnAnnotation == null) {
            columnAnnotation = annByType.get("javax.persistence.Column"); // NOI18N
        }
        AnnotationMirror temporalAnnotation = annByType.get("jakarta.persistence.Temporal"); // NOI18N
        if (temporalAnnotation == null) {
            temporalAnnotation = annByType.get("javax.persistence.Temporal"); // NOI18N
        }
        String temporal = temporalAnnotation != null ? EntityMappingsUtilities.getTemporalType(helper, temporalAnnotation) : null;
        Column column = new ColumnImpl(helper, columnAnnotation, propertyName.toUpperCase()); // NOI18N
        AnnotationMirror basicAnnotation = annByType.get("jakarta.persistence.Basic"); // NOI18N
        if (basicAnnotation == null) {
            basicAnnotation = annByType.get("javax.persistence.Basic"); // NOI18N
        }
        basicList.add(new BasicImpl(helper, basicAnnotation, propertyName, column, temporal));
    }

    public void setBasic(int index, Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic getBasic(int index) {
        return basicList.get(index);
    }

    public int sizeBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setBasic(Basic[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic[] getBasic() {
        return basicList.toArray(new Basic[0]);
    }

    public int addBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic newBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTransient(int index, Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient getTransient(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTransient(Transient[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient[] getTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient newTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
