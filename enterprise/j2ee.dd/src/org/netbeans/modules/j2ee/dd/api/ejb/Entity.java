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

public interface Entity extends EntityAndSession {

    public static final String PERSISTENCE_TYPE = "PersistenceType";	// NOI18N
    public static final String PRIM_KEY_CLASS = "PrimKeyClass";	// NOI18N
    public static final String REENTRANT = "Reentrant";	// NOI18N
    public static final String CMP_VERSION = "CmpVersion";	// NOI18N
    public static final String ABSTRACT_SCHEMA_NAME = "AbstractSchemaName";	// NOI18N
    public static final String CMP_FIELD = "CmpField";	// NOI18N
    public static final String PRIMKEY_FIELD = "PrimkeyField";	// NOI18N
    public static final String PRIMKEYFIELDID = "PrimkeyFieldId";	// NOI18N
    public static final String QUERY = "Query";	// NOI18N
    public static final String PERSISTENCE_TYPE_BEAN = "Bean"; // NOI18N
    public static final String PERSISTENCE_TYPE_CONTAINER = "Container"; // NOI18N
    public static final String CMP_VERSION_ONE = "1.x"; // NOI18N
    public static final String CMP_VERSION_TWO = "2.x"; // NOI18N
        
    public void setPersistenceType(String value);
    
    public String getPersistenceType();
    
    public void setPrimKeyClass(String value);
    
    public String getPrimKeyClass();
    
    public void setReentrant(boolean value);
    
    public boolean isReentrant();
    
    public void setCmpVersion(String value);
    
    public String getCmpVersion();
    
    public void setAbstractSchemaName(String value);
    
    public String getAbstractSchemaName();
    
    public void setCmpField(int index, CmpField value);
    
    public CmpField getCmpField(int index);
    
    public void setCmpField(CmpField[] value);
    
    public CmpField[] getCmpField();
    
    public int sizeCmpField();
    
    public int addCmpField(org.netbeans.modules.j2ee.dd.api.ejb.CmpField value);
    
    public int removeCmpField(org.netbeans.modules.j2ee.dd.api.ejb.CmpField value);
    
    public CmpField newCmpField();
    
    public void setPrimkeyField(String value);
    
    public String getPrimkeyField();
    
    public void setPrimkeyFieldId(java.lang.String value);
    
    public java.lang.String getPrimkeyFieldId();
              
    public void setQuery(int index, Query value);
    
    public Query getQuery(int index);
    
    public void setQuery(Query[] value);
    
    public Query[] getQuery();
    
    public int sizeQuery();
    
    public int removeQuery(org.netbeans.modules.j2ee.dd.api.ejb.Query value);
    
    public int addQuery(org.netbeans.modules.j2ee.dd.api.ejb.Query value);
    
    public Query newQuery();
        
        
        
}

