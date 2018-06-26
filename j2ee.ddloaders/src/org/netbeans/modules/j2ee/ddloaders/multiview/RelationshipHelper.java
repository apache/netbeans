/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
