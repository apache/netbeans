/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.hibernate.reveng;

/**
 * Constants for Hibernate Reverse Engineering file tags and attribute names
 * 
 * @author gowri
 */
public class HibernateRevengXmlConstants {
    
    public static final String REVENG_TAG = "hibernate-reverse-engineering";
    public static final String SCHEMA_SELECTION_TAG = "schema-selection";
    public static final String TYPE_MAPPING_TAG = "type-mapping";
    public static final String TABLE_TAG = "table";
    public static final String TABLE_FILTER_TAG = "table-filter";   
    public static final String GENERATOR_TAG = "generator";    
    public static final String COLUMN_TAG = "column";   
    public static final String SQL_TYPE_TAG = "sql-type";
    
    
    public static final String MATCH_NAME_ATTRIB = "match-name"; // table name
    public static final String PACKAGE_ATTRIB = "package";
    public static final String CLASS_ATTRIB = "class";
    public static final String NAME_ATTRIB = "name";    
    public static final String COLUMN_ATTRIB = "column";  
    public static final String HIBERNATE_TYPE_ATTRIB = "hibernate-type";
}
