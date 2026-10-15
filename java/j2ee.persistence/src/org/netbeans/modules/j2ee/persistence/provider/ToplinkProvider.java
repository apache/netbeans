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
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.util.NbBundle;

/**
 * This class represents Toplink provider.
 *
 * @author Erno Mononen
 */
class ToplinkProvider extends Provider{
    
    /**
     * There are two valid provider classes for TopLink, i.e. 
     * <code>oracle.toplink.essentials.PersistenceProvider</code> and 
     * <code>oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider</code>. 
     * The former is preferred, whereas the latter is needed for compatibility reasons since
     * it was used in 5.5.
     */ 
    private static final String PREFERRED_PROVIDER_CLASS = "oracle.toplink.essentials.PersistenceProvider"; //NOI18N
    private static final String ALTERNATIVE_PROVIDER_CLASS = "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider";//NOI18N
    private static final String ECLIPSELINK_PROVIDER_CLASS = "org.eclipse.persistence.jpa.PersistenceProvider";//NOI18N

    /**
     * Creates a new instance using the preferred provider class.
     * 
     * @see #PREFERRED_PROVIDER_CLASS
     */ 
    static ToplinkProvider create(String version){
        if(version!=null && !Persistence.VERSION_1_0.equals(version)){
            return new ToplinkProvider(ECLIPSELINK_PROVIDER_CLASS, version);
        }
        else {
            return new ToplinkProvider(PREFERRED_PROVIDER_CLASS, version);
        }
    }
    
    /**
     * Creates a new instance using the provider class used in NetBeans 5.5. Note
     * that this is just for compatiblity, otherwise it is recommended to use 
     * {@link #create()} instead.
     * 
     * @see #ALTERNATIVE_PROVIDER_CLASS
     */ 
    static ToplinkProvider create55Compatible(){
        return new ToplinkProvider(ALTERNATIVE_PROVIDER_CLASS, Persistence.VERSION_1_0);
    }
    
    private ToplinkProvider(String providerClass, String version){
        super(providerClass, version); //NOI18N
    }
    
    @Override
    public String getDisplayName() {
        String name = Persistence.VERSION_1_0.equals(getVersion())?NbBundle.getMessage(ToplinkProvider.class, "LBL_TopLinkEssentials"):NbBundle.getMessage(ToplinkProvider.class, "LBL_TopLink");//NOI18N
        return name + (getVersion()!=null ? " (JPA "+getVersion()+")" : ""); //NOI18N
    }
    
    @Override
    public String getJdbcUrl() {
        return "toplink.jdbc.url";
    }

    @Override
    public String getJdbcDriver() {
        return "toplink.jdbc.driver";
    }

    @Override
    public String getJdbcUsername() {
        return "toplink.jdbc.user";
    }

    @Override
    public String getJdbcPassword() {
        return "toplink.jdbc.password";
    }

    @Override
    public String getTableGenerationPropertyName() {
        return "toplink.ddl-generation";
    }

    @Override
    public String getTableGenerationDropCreateValue() {
        return "drop-and-create-tables";
    }

    @Override
    public String getTableGenerationCreateValue() {
        return "create-tables";
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
