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

public interface CmrFieldMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String CMR_FIELD_NAME = "CmrFieldName"; // NOI18N
    public static final String COLUMN_PAIR = "ColumnPair"; // NOI18N
    public static final String FETCHED_WITH = "FetchedWith"; // NOI18N

    public void setCmrFieldName(String value);
    public String getCmrFieldName();

    public void setColumnPair(int index, ColumnPair value);
    public ColumnPair getColumnPair(int index);
    public int sizeColumnPair();
    public void setColumnPair(ColumnPair[] value);
    public ColumnPair[] getColumnPair();
    public int addColumnPair(ColumnPair value);
    public int removeColumnPair(ColumnPair value);
    public ColumnPair newColumnPair();

    public void setFetchedWith(FetchedWith value);
    public FetchedWith getFetchedWith();
    public FetchedWith newFetchedWith();

}
