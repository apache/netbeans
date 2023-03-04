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

import java.util.List;

/**
 * This interface represents a data source provider. Should
 * be implemented by projects where it is possible to use data sources.
 * 
 * @author Erno Mononen
 */
public interface JPADataSourceProvider {

    /**
     * Gets all registered data sources. 
     * 
     * @return a list of <code>JPADataSource</code>s representing
     * the available data sources.
     */ 
    List<JPADataSource> getDataSources();
    
    /**
     * Converts the given <code>dataSource</code> to a <code>JPADataSource</code> if possible. 
     * 
     * @return the given <code>dataSource</code> as a <code>JPADataSource</code> or 
     * <code>null</code> if it could not be converted. 
     */ 
    JPADataSource toJPADataSource(Object dataSource);
}
