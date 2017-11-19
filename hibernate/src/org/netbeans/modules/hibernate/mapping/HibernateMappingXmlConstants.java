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

package org.netbeans.modules.hibernate.mapping;

/**
 * Constants for Hibernate mapping file tags and attribute names
 * 
 * @author Dongmei
 */
public class HibernateMappingXmlConstants {
    
    public static final String MAPPING_TAG = "hibernate-mapping";
    public static final String CLASS_TAG = "class";
    public static final String ID_TAG = "id";
    public static final String GENERATOR_TAG = "generator";
    public static final String PROPERTY_TAG = "property";
    public static final String SET_TAG = "set";
    public static final String KEY_TAG = "key";
    public static final String ONE_TO_MANY_TAG = "one-to-many";
    public static final String DISCRIMINATOR_TAG = "discriminator";
    public static final String COMPOSITE_ID_TAG = "composite-id";
    public static final String KEY_PROPERTY_TAG = "key-property";
    public static final String KEY_MANY_TO_ONE_TAG = "key-many-to-one";
    public static final String VERSION_TAG = "version";
    public static final String TIMESTAMP_TAG = "timestamp";
    public static final String MANY_TO_ONE_TAG = "many-to-one";
    public static final String ONE_TO_ONE_TAG = "one-to-one";
    public static final String COMPONENT_TAG = "component";
    public static final String SUBCLASS_TAG = "subclass";
    public static final String JOINED_SUBCLASS_TAG = "joined-subclass";
    public static final String UNION_SUBCLASS_TAG = "union-subclass";
    public static final String JOIN_TAG = "join";
    public static final String COLUMN_TAG = "column";
    public static final String IMPORT_TAG = "import";
    public static final String ANY_TAG = "any";
    public static final String MAP_TAG = "map";
    public static final String LIST_TAG = "list";
    public static final String LIST_INDEX_TAG = "list-index";
    public static final String INDEX_TAG = "index";
    public static final String MAP_KEY_TAG = "map-key";
    public static final String ELEMENT_TAG = "element";
    public static final String MANY_TO_MANY_TAG = "many-to-many";
    
    public static final String TABLE_ATTRIB = "table"; // table name
    public static final String PACKAGE_ATTRIB = "package";
    public static final String CLASS_ATTRIB = "class";
    public static final String NAME_ATTRIB = "name";
    public static final String TYPE_ATTRIB = "type";
    public static final String COLUMN_ATTRIB = "column";
    public static final String EXTENDS_ATTRIB = "extends";
    public static final String PERSISTER_ATTRIB = "persister";
    public static final String CASCADE_ATTRIB = "cascade";
    public static final String ID_TYPE_ATTRIB = "id-type";
}
