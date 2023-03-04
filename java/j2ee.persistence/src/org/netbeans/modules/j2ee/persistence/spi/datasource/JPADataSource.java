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

package org.netbeans.modules.j2ee.persistence.spi.datasource;

/**
 * This interface reprsents a data source.
 * <i>Currently the interface mirrors the 
 * <code>org.netbeans.modules.j2ee.deployment.common.api.Datasource</code>
 * which can't be directly used because of the dependencies.</i>
 * 
 * @author Erno Mononen
 */
public interface JPADataSource {
 
    /**
     * Returns the JNDI name
     *
     * @return the JNDI name
     */
    String getJndiName();
    
    /**
     * Returns the database URL
     *
     * @return the database URL
     */
    String getUrl();
    
    /**
     * Returns the database user
     *
     * @return the database user
     */
    String getUsername();
    
    /**
     * Returns the password
     *
     * @return the the password
     */
    String getPassword();
    
    /**
     * Returns the fully qualified name of the database driver class
     *
     * @return the fully qualified name of the database driver class
     */
    String getDriverClassName();
    
    /**
     * Returns the data source display name
     *
     * @return the data source display name
     */
    String getDisplayName();
    
}
