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
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * This class represents Kodo provider.
 *
 * @author Erno Mononen
 */
class KodoProvider extends Provider{
    
    protected KodoProvider(){
        super("kodo.persistence.PersistenceProviderImpl");
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(KodoProvider.class, "LBL_Kodo"); //NOI18N
    }
    
    @Override
    public String getJdbcUrl() {
        return "kodo.ConnectionURL";
    }
    
    @Override
    public String getJdbcDriver() {
        return "kodo.ConnectionDriverName";
    }
    
    @Override
    public String getJdbcUsername() {
        return "kodo.ConnectionUserName";
    }
    
    @Override
    public String getJdbcPassword() {
        return "kodo.ConnectionPassword";
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
        return Collections.emptyMap();
    }
    
}
