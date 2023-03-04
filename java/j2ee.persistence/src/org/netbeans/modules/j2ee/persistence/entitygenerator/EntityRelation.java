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

package org.netbeans.modules.j2ee.persistence.entitygenerator;

/**
 * Abstract information about relationship between 2 entity classes
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Pavel Buzek
 */
public class EntityRelation {
    
    private RelationshipRole[] roles;
    private String relationName;
    
    public EntityRelation(RelationshipRole roleA, RelationshipRole roleB) {
        roles = new RelationshipRole[] {roleA, roleB};
        roleA.setParent(this);
        roleB.setParent(this);
    }
    
    public void makeRoleNamesUnique() {
        if (getRoleA().getRoleName().equals(
                getRoleB().getRoleName())) {
            
            String roleBName = getRoleB().getRoleName() + '1';
            getRoleB().setRoleName(roleBName);
        }
    }
    
    public RelationshipRole getRoleA() {
        return roles[0];
    }
    
    public RelationshipRole getRoleB() {
        return roles[1];
    }
    
    public void setRoleA(RelationshipRole roleA) {
        roles[0] = roleA;
    }
    
    public void setRoleB(RelationshipRole roleB) {
        roles[1] = roleB;
    }
    
    @Override
    public String toString() {
        return "\nrelation name " + getRelationName() + // NOI18N
                "\nroleA = \n\t" + getRoleA() + // NOI18N
                "\nroleB = \n\t" + getRoleB(); // NOI18N
    }
    
    public String getRelationName() {
        return relationName;
    }
    
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }
    
    public RelationshipRole[] getRoles() {
        return roles;
    }
    
    public static enum FetchType {
        DEFAULT, EAGER, LAZY;
    }
    
    public static enum CollectionType {
        COLLECTION("java.util.Collection"), //NOI18N
        LIST("java.util.List"), //NOI18N
        SET("java.util.Set"); //NOI18N
        
        private final String classStr;
        
        private CollectionType(String classStr) {
            this.classStr = classStr;
        }
        
        public String className() {
            return this.classStr;
        }
        
        public String getShortName() {
            return this.classStr.replace("java.util.", "").trim(); //NOI18N
        }
    }
}
