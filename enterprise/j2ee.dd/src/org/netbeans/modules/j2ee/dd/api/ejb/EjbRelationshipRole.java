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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public interface EjbRelationshipRole extends CommonDDBean, DescriptionInterface {

    public static final String EJB_RELATIONSHIP_ROLE_NAME = "EjbRelationshipRoleName";	// NOI18N
    public static final String EJBRELATIONSHIPROLENAMEID = "EjbRelationshipRoleNameId";	// NOI18N
    public static final String MULTIPLICITY = "Multiplicity";	// NOI18N
    public static final String CASCADE_DELETE = "CascadeDelete";	// NOI18N
    public static final String CASCADEDELETEID = "CascadeDeleteId";	// NOI18N
    public static final String RELATIONSHIP_ROLE_SOURCE = "RelationshipRoleSource";	// NOI18N
    public static final String CMR_FIELD = "CmrField";	// NOI18N   
    public static final String MULTIPLICITY_ONE = "One"; // NOI18N  
    public static final String MULTIPLICITY_MANY = "Many"; // NOI18N  
        
    public void setEjbRelationshipRoleName(String value);

    public String getEjbRelationshipRoleName();
        
    public void setEjbRelationshipRoleNameId(java.lang.String value);

    public java.lang.String getEjbRelationshipRoleNameId();
        
    public void setMultiplicity(String value);
    
    public String getMultiplicity();
    
    public void setCascadeDelete(boolean value);
    
    public boolean isCascadeDelete();
        
    public void setRelationshipRoleSource(RelationshipRoleSource value);
    
    public RelationshipRoleSource getRelationshipRoleSource();
    
    public RelationshipRoleSource newRelationshipRoleSource();
        
    public void setCmrField(CmrField value);
    
    public CmrField getCmrField();
        
    public CmrField newCmrField();
    
}
 

