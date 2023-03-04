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

package org.netbeans.modules.j2ee.deployment.common.api;

import org.openide.filesystems.FileObject;

/**
 * This interface provides an abstraction for the original relational database
 * information used to generate an Entity container managed persistence bean.
 * Changes to cmp or cmr fields made after generation will not be reflected in
 * this interface and may invalidation the original mappings. Thus, this
 * information is intended to serve only as a hint for application server
 * mapping.
 * @author  Chris Webster
 */
public interface OriginalCMPMapping {

    /**
     * @return file reference to dbschema where table and column references
     * refer. 
     */
    FileObject getSchema();
    
    /**
     * @return ejb name. 
     */
    String getEjbName();
    
    /**
     * @return table name containing all column references.
     */
    String getTableName();
    
    /**
     * @param cmpFieldName to use for locating column mapping.
     * @return name of column which represents the cmp field in #getTable() or
     * null if this cmp field does not have a mapping (this could happen because
     * of a change to the set of cmp fields or changes to the specific cmp field).
     */
    String getFieldColumn(String cmpFieldName);
    
    /**
     * Obtain the foreign key columns used to express a 1:N relationship. This
     * method will return the mapping only from the dependant EJB. Consider a
     * an Order to LineItem relationship (Order contains many line items)
     * where Order has a cmr field called lineItems and LineItem has a cmr field
     * named order. Invoking <code>getRelationshipColumn("lineItems")</code> on
     * Order would return null; however, the same invocation on LineItem would
     * return the foreign keys which reference Order. Mapping information for
     * M:N relationships is obtain via the #getRelationshipTable(java.lang.String)
     * method.
     * @param cmrFieldName to use for locating column mapping.
     * @return name of columns representing the cmr field in #getTable() or null
     * if the field was not present during creation or is participating in a
     * many to many relationship. 
     */
    String[] getRelationshipColumn(String cmrFieldName);
    
    /** 
     * Obtain the name of the join table a cmr field is based on. Both sides
     * of the relationship will return the same table name.
     * @param cmrFieldName to use in decision.
     * @return name of join table if cmrFieldName is based on a join table, 
     * null otherwise. 
     */
    String getRelationshipJoinTable(String cmrFieldName);
}
