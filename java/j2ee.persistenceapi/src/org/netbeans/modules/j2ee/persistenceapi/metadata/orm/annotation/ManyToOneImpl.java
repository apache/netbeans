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

import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class ManyToOneImpl implements ManyToOne {

    private final String name;
    private final ParseResult parseResult;
    private final JoinTable joinTable;
    private final List<JoinColumn> joinColumnList;

    public ManyToOneImpl(final AnnotationModelHelper helper, final Element element, AnnotationMirror manyToOneAnnotation, String name, Map<String, ? extends AnnotationMirror> annByType) {
        this.name = name;

        String cascadeTypeName;
        String fetchTypeName;
        String joinTableName;

        if (((TypeElement) manyToOneAnnotation.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("jakarta.")) {
            cascadeTypeName = "jakarta.persistence.CascadeType";
            fetchTypeName = "jakarta.persistence.FetchType";
            joinTableName = "jakarta.persistence.JoinTable";
        } else {
            cascadeTypeName = "javax.persistence.CascadeType";
            fetchTypeName = "javax.persistence.FetchType";
            joinTableName = "javax.persistence.JoinTable";
        }

        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectClass("targetEntity", new DefaultProvider() { // NOI18N
            public Object getDefaultValue() {
                return EntityMappingsUtilities.getElementTypeName(element);
            }
        });
        parser.expectEnumConstantArray("cascade", helper.resolveType(cascadeTypeName), new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                return new CascadeTypeImpl(arrayMembers);
            }
        }, parser.defaultValue(new CascadeTypeImpl()));
        parser.expectEnumConstant("fetch", helper.resolveType(fetchTypeName), parser.defaultValue("EAGER")); // NOI18N
        parser.expectPrimitive("optional", Boolean.class, parser.defaultValue(true)); // NOI18N
        parseResult = parser.parse(manyToOneAnnotation);

        joinTable = new JoinTableImpl(helper, annByType.get(joinTableName)); // NOI18N
        joinColumnList = EntityMappingsUtilities.getJoinColumns(helper, annByType);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return name;
    }

    public void setTargetEntity(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getTargetEntity() {
        return parseResult.get("targetEntity", String.class); // NOI18N
    }

    public void setFetch(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getFetch() {
        return parseResult.get("fetch", String.class); // NOI18N
    }

    public void setOptional(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isOptional() {
        return parseResult.get("optional", Boolean.class); // NOI18N
    }

    public void setJoinColumn(int index, JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn getJoinColumn(int index) {
        return joinColumnList.get(index);
    }

    public int sizeJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setJoinColumn(JoinColumn[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn[] getJoinColumn() {
        return joinColumnList.toArray(new JoinColumn[0]);
    }

    public int addJoinColumn(JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeJoinColumn(JoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinColumn newJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setJoinTable(JoinTable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public JoinTable getJoinTable() {
        return joinTable;
    }

    public JoinTable newJoinTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCascade(CascadeType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public CascadeType getCascade() {
        return parseResult.get("cascade", CascadeType.class); // NOI18N
    }

    public CascadeType newCascadeType() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
