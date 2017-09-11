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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface Entity {

    public final String FIELD_ACCESS = "FIELD"; // NOI18N
    public final String PROPERTY_ACCESS = "PROPERTY"; // NOI18N
    
    public void setName(String value);
    
    public String getName();
    
    public void setClass2(String value);
    
    public String getClass2();
    
    public void setAccess(String value);
    
    public String getAccess();
    
    public void setMetadataComplete(boolean value);
    
    public boolean isMetadataComplete();
    
    public void setDescription(String value);
    
    public String getDescription();
    
    public void setTable(Table value);
    
    public Table getTable();
    
    public Table newTable();
    
    public void setSecondaryTable(int index, SecondaryTable value);
    
    public SecondaryTable getSecondaryTable(int index);
    
    public int sizeSecondaryTable();
    
    public void setSecondaryTable(SecondaryTable[] value);
    
    public SecondaryTable[] getSecondaryTable();
    
    public int addSecondaryTable(SecondaryTable value);
    
    public int removeSecondaryTable(SecondaryTable value);
    
    public SecondaryTable newSecondaryTable();
    
    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index);
    
    public int sizePrimaryKeyJoinColumn();
    
    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value);
    
    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn();
    
    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn();
    
    public void setIdClass(IdClass value);
    
    public IdClass getIdClass();
    
    public IdClass newIdClass();
    
    public void setInheritance(Inheritance value);
    
    public Inheritance getInheritance();
    
    public Inheritance newInheritance();
    
    public void setDiscriminatorValue(String value);
    
    public String getDiscriminatorValue();
    
    public void setDiscriminatorColumn(DiscriminatorColumn value);
    
    public DiscriminatorColumn getDiscriminatorColumn();
    
    public DiscriminatorColumn newDiscriminatorColumn();
    
    public void setSequenceGenerator(SequenceGenerator value);
    
    public SequenceGenerator getSequenceGenerator();
    
    public SequenceGenerator newSequenceGenerator();
    
    public void setTableGenerator(TableGenerator value);
    
    public TableGenerator getTableGenerator();
    
    public TableGenerator newTableGenerator();
    
    public void setNamedQuery(int index, NamedQuery value);
    
    public NamedQuery getNamedQuery(int index);
    
    public int sizeNamedQuery();
    
    public void setNamedQuery(NamedQuery[] value);
    
    public NamedQuery[] getNamedQuery();
    
    public int addNamedQuery(NamedQuery value);
    
    public int removeNamedQuery(NamedQuery value);
    
    public NamedQuery newNamedQuery();
    
    public void setNamedNativeQuery(int index, NamedNativeQuery value);
    
    public NamedNativeQuery getNamedNativeQuery(int index);
    
    public int sizeNamedNativeQuery();
    
    public void setNamedNativeQuery(NamedNativeQuery[] value);
    
    public NamedNativeQuery[] getNamedNativeQuery();
    
    public int addNamedNativeQuery(NamedNativeQuery value);
    
    public int removeNamedNativeQuery(NamedNativeQuery value);
    
    public NamedNativeQuery newNamedNativeQuery();
    
    public void setSqlResultSetMapping(int index, SqlResultSetMapping value);
    
    public SqlResultSetMapping getSqlResultSetMapping(int index);
    
    public int sizeSqlResultSetMapping();
    
    public void setSqlResultSetMapping(SqlResultSetMapping[] value);
    
    public SqlResultSetMapping[] getSqlResultSetMapping();
    
    public int addSqlResultSetMapping(SqlResultSetMapping value);
    
    public int removeSqlResultSetMapping(SqlResultSetMapping value);
    
    public SqlResultSetMapping newSqlResultSetMapping();
    
    public void setExcludeDefaultListeners(EmptyType value);
    
    public EmptyType getExcludeDefaultListeners();
    
    public EmptyType newEmptyType();
    
    public void setExcludeSuperclassListeners(EmptyType value);
    
    public EmptyType getExcludeSuperclassListeners();
    
    public void setEntityListeners(EntityListeners value);
    
    public EntityListeners getEntityListeners();
    
    public EntityListeners newEntityListeners();
    
    public void setPrePersist(PrePersist value);
    
    public PrePersist getPrePersist();
    
    public PrePersist newPrePersist();
    
    public void setPostPersist(PostPersist value);
    
    public PostPersist getPostPersist();
    
    public PostPersist newPostPersist();
    
    public void setPreRemove(PreRemove value);
    
    public PreRemove getPreRemove();
    
    public PreRemove newPreRemove();
    
    public void setPostRemove(PostRemove value);
    
    public PostRemove getPostRemove();
    
    public PostRemove newPostRemove();
    
    public void setPreUpdate(PreUpdate value);
    
    public PreUpdate getPreUpdate();
    
    public PreUpdate newPreUpdate();
    
    public void setPostUpdate(PostUpdate value);
    
    public PostUpdate getPostUpdate();
    
    public PostUpdate newPostUpdate();
    
    public void setPostLoad(PostLoad value);
    
    public PostLoad getPostLoad();
    
    public PostLoad newPostLoad();
    
    public void setAttributeOverride(int index, AttributeOverride value);
    
    public AttributeOverride getAttributeOverride(int index);
    
    public int sizeAttributeOverride();
    
    public void setAttributeOverride(AttributeOverride[] value);
    
    public AttributeOverride[] getAttributeOverride();
    
    public int addAttributeOverride(AttributeOverride value);
    
    public int removeAttributeOverride(AttributeOverride value);
    
    public AttributeOverride newAttributeOverride();
    
    public void setAssociationOverride(int index, AssociationOverride value);
    
    public AssociationOverride getAssociationOverride(int index);
    
    public int sizeAssociationOverride();
    
    public void setAssociationOverride(AssociationOverride[] value);
    
    public AssociationOverride[] getAssociationOverride();
    
    public int addAssociationOverride(AssociationOverride value);
    
    public int removeAssociationOverride(AssociationOverride value);
    
    public AssociationOverride newAssociationOverride();
    
    public void setAttributes(Attributes value);
    
    public Attributes getAttributes();
    
    public Attributes newAttributes();
    
}
