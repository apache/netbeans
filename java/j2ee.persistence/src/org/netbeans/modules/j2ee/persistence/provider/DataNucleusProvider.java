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

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 *
 * @author pblaha
 */

/**
 * This class represents DataNucleus provider.
 *
 * @author pblaha
 */
class DataNucleusProvider extends Provider{

    protected DataNucleusProvider(){
        super("org.datanucleus.store.appengine.jpa.DatastorePersistenceProvider"); //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DataNucleusProvider.class, "LBL_DataNucleus"); //NOI18N
    }

    @Override
    public String getJdbcUrl() {
        return "";
    }

    @Override
    public String getJdbcDriver() {
        return "";
    }

    @Override
    public String getJdbcUsername() {
        return "";
    }

    @Override
    public String getJdbcPassword() {
        return "";
    }

    @Override
    public String getTableGenerationPropertyName() {
        return "";
    }

    @Override
    public String getTableGenerationDropCreateValue() {
        return "";
    }

    @Override
    public String getTableGenerationCreateValue() {
        return "";
    }

    @Override
    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Map getDefaultVendorSpecificProperties() {
        Hashtable<String,String> properties = new Hashtable();
        properties.put("datanucleus.NontransactionalRead", "true"); //NOI18N
        properties.put("datanucleus.NontransactionalWrite", "true"); //NOI18N
        properties.put("datanucleus.ConnectionURL", "appengine"); //NOI18N
        return properties;
    }

}
