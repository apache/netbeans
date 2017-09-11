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

public interface EntityMappings {

    public void setVersion(String value);

    public String getVersion();
    
    public void setDescription(String value);
    
    public String getDescription();
    
    public void setPersistenceUnitMetadata(PersistenceUnitMetadata value);
    
    public PersistenceUnitMetadata getPersistenceUnitMetadata();
    
    public PersistenceUnitMetadata newPersistenceUnitMetadata();
    
    public void setPackage(String value);
    
    public String getPackage();
    
    public void setSchema(String value);
    
    public String getSchema();
    
    public void setCatalog(String value);
    
    public String getCatalog();
    
    public void setAccess(String value);
    
    public String getAccess();
    
    public void setSequenceGenerator(int index, SequenceGenerator value);
    
    public SequenceGenerator getSequenceGenerator(int index);
    
    public int sizeSequenceGenerator();
    
    public void setSequenceGenerator(SequenceGenerator[] value);
    
    public SequenceGenerator[] getSequenceGenerator();
    
    public int addSequenceGenerator(SequenceGenerator value);
    
    public int removeSequenceGenerator(SequenceGenerator value);
    
    public SequenceGenerator newSequenceGenerator();
    
    public void setTableGenerator(int index, TableGenerator value);
    
    public TableGenerator getTableGenerator(int index);
    
    public int sizeTableGenerator();
    
    public void setTableGenerator(TableGenerator[] value);
    
    public TableGenerator[] getTableGenerator();
    
    public int addTableGenerator(TableGenerator value);
    
    public int removeTableGenerator(TableGenerator value);
    
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
    
    public void setMappedSuperclass(int index, MappedSuperclass value);
    
    public MappedSuperclass getMappedSuperclass(int index);
    
    public int sizeMappedSuperclass();
    
    public void setMappedSuperclass(MappedSuperclass[] value);
    
    public MappedSuperclass[] getMappedSuperclass();
    
    public int addMappedSuperclass(MappedSuperclass value);
    
    public int removeMappedSuperclass(MappedSuperclass value);
    
    public MappedSuperclass newMappedSuperclass();
    
    public void setEntity(int index, Entity value);
    
    public Entity getEntity(int index);
    
    public int sizeEntity();
    
    public void setEntity(Entity[] value);
    
    public Entity[] getEntity();
    
    public int addEntity(Entity value);
    
    public int removeEntity(Entity value);
    
    public Entity newEntity();
    
    public void setEmbeddable(int index, Embeddable value);
    
    public Embeddable getEmbeddable(int index);
    
    public int sizeEmbeddable();
    
    public void setEmbeddable(Embeddable[] value);
    
    public Embeddable[] getEmbeddable();
    
    public int addEmbeddable(Embeddable value);
    
    public int removeEmbeddable(Embeddable value);
    
    public Embeddable newEmbeddable();
    
}
