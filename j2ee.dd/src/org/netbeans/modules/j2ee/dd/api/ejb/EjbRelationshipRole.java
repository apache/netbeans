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
 

