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

import java.util.LinkedList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * Indicates conflict between the data sources being deployed/saved and the existing ones.
 *
 * @author Libor Kotouc
 *
 * @since 1.15
 */
public final class DatasourceAlreadyExistsException extends Exception {
    
    private List<Datasource> datasources;
    
    /**
     * Creates new DatasourceAlreadyExistsException with the list of conflicting data sources
     *
     * @param datasources the list of conflicting data sources
     *
     * @exception NullPointerException if the <code>datasources</code> argument is <code>null</code>
     */
    public DatasourceAlreadyExistsException(List<Datasource> datasources) {
        if (datasources == null) {
            throw new NullPointerException(NbBundle.getMessage(getClass(), "ERR_CannotPassNullDatasources")); // NOI18N
        }
        this.datasources = datasources;
    }
    
    /**
     * Creates new DatasourceAlreadyExistsException with the conflicting data source
     *
     * @param datasource the conflicting data source
     */
    public DatasourceAlreadyExistsException(Datasource datasource) {
        datasources = new LinkedList<Datasource>();
        datasources.add(datasource);
    }
    
    /**
     * Returns list of conflicting data sources
     *
     * @return list of conflicting data sources
     */
    public List<Datasource> getDatasources() {
        return datasources;
    }
    
}
