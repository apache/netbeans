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
package org.netbeans.modules.j2ee.sun.dd.api.cmp;

public interface EntityMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String EJB_NAME = "EjbName"; // NOI18N
    public static final String TABLE_NAME = "TableName"; // NOI18N
    public static final String CMP_FIELD_MAPPING = "CmpFieldMapping"; // NOI18N
    public static final String CMR_FIELD_MAPPING = "CmrFieldMapping"; // NOI18N
    public static final String SECONDARY_TABLE = "SecondaryTable"; // NOI18N
    public static final String CONSISTENCY = "Consistency"; // NOI18N

    public void setEjbName(String value);
    public String getEjbName();

    public void setTableName(String value);
    public String getTableName();

    public void setCmpFieldMapping(int index, CmpFieldMapping value);
    public CmpFieldMapping getCmpFieldMapping(int index);
    public int sizeCmpFieldMapping();
    public void setCmpFieldMapping(CmpFieldMapping[] value);
    public CmpFieldMapping[] getCmpFieldMapping();
    public int addCmpFieldMapping(CmpFieldMapping value);
    public int removeCmpFieldMapping(CmpFieldMapping value);
    public CmpFieldMapping newCmpFieldMapping();

    public void setCmrFieldMapping(int index, CmrFieldMapping value);
    public CmrFieldMapping getCmrFieldMapping(int index);
    public int sizeCmrFieldMapping();
    public void setCmrFieldMapping(CmrFieldMapping[] value);
    public CmrFieldMapping[] getCmrFieldMapping();
    public int addCmrFieldMapping(CmrFieldMapping value);
    public int removeCmrFieldMapping(CmrFieldMapping value);
    public CmrFieldMapping newCmrFieldMapping();

    public void setSecondaryTable(int index, SecondaryTable value);
    public SecondaryTable getSecondaryTable(int index);
    public int sizeSecondaryTable();
    public void setSecondaryTable(SecondaryTable[] value);
    public SecondaryTable[] getSecondaryTable();
    public int addSecondaryTable(SecondaryTable value);
    public int removeSecondaryTable(SecondaryTable value);
    public SecondaryTable newSecondaryTable();

    public void setConsistency(Consistency value);
    public Consistency getConsistency();
    public Consistency newConsistency();

}
