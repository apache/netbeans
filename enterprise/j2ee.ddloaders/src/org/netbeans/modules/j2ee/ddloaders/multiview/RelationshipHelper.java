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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;

/**
 * @author pfiala
 */
public class RelationshipHelper {

    private static final String MULTIPLICITY_MANY = "Many";
    private static final String MULTIPLICITY_ONE = "One";

    private final EjbRelation relation;

    public static class RelationshipRoleHelper {

        private final EjbRelationshipRole role;

        public RelationshipRoleHelper(EjbRelationshipRole role) {
            this.role = role;
        }

        public boolean isMultiple() {
            return MULTIPLICITY_MANY.equals(role.getMultiplicity());
        }

        public void setMultiple(boolean multiple) {
            role.setMultiplicity(multiple ? MULTIPLICITY_MANY : MULTIPLICITY_ONE);
        }

        public String getEjbName() {
            return role.getRelationshipRoleSource().getEjbName();
        }

        public void setEjbName(String ejbName) {
            role.getRelationshipRoleSource().setEjbName(ejbName);
        }

        public String getRoleName() {
            return role.getEjbRelationshipRoleName();
        }

        public void setRoleName(String roleName) {
            role.setEjbRelationshipRoleName(roleName);
        }

        public String getFieldName() {
            CmrField field = role.getCmrField();
            return field == null ? null : field.getCmrFieldName();
        }

        public String getFieldType() {
            CmrField field = role.getCmrField();
            return field == null ? null : field.getCmrFieldType();
        }

        public boolean isCascadeDelete() {
            return role.isCascadeDelete();
        }

        public void setCascadeDelete(boolean cascadeDelete) {
            role.setCascadeDelete(cascadeDelete);
        }

        public CmrField getCmrField() {
            return role.getCmrField();
        }

        public void setCmrField(CmrField cmrField) {
            role.setCmrField(cmrField);
        }

        public void setCmrField(String fieldName, String fieldType) {
            CmrField field = role.getCmrField();
            if (field == null) {
                role.setCmrField(field = role.newCmrField());
            }
            field.setCmrFieldName(fieldName);
            field.setCmrFieldType(fieldType);
        }

    }

    public final RelationshipRoleHelper roleA;
    public final RelationshipRoleHelper roleB;

    public RelationshipHelper(EjbRelation relation) {
        this.relation = relation;
        roleA = new RelationshipRoleHelper(relation.getEjbRelationshipRole());
        roleB = new RelationshipRoleHelper(relation.getEjbRelationshipRole2());
    }

    public RelationshipHelper(Relationships singleRelationships) {
        relation = singleRelationships.newEjbRelation();
        EjbRelationshipRole roleA = newRole();
        relation.setEjbRelationshipRole(roleA);
        EjbRelationshipRole roleB = newRole();
        relation.setEjbRelationshipRole2(roleB);
        singleRelationships.addEjbRelation(relation);
        this.roleA = new RelationshipRoleHelper(roleA);
        this.roleB = new RelationshipRoleHelper(roleB);
    }

    private EjbRelationshipRole newRole() {
        EjbRelationshipRole role = relation.newEjbRelationshipRole();
        role.setRelationshipRoleSource(role.newRelationshipRoleSource());
        return role;
    }

    public String getRelationName() {
        return relation.getEjbRelationName();
    }

    public void setRelationName(String relationName) {
        relation.setEjbRelationName(relationName);
    }

    public String getDescription() {
        return relation.getDefaultDescription();
    }

    public void setDescription(String description) {
        relation.setDescription(description);
    }

}
