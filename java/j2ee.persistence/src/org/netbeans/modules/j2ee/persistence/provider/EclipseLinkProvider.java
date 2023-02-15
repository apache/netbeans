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
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.util.NbBundle;

/**
 * This class represents the EclipseLink provider.
 *
 * @author Andrei Badea
 */
class EclipseLinkProvider extends Provider {

    public EclipseLinkProvider(String version){
        super(PersistenceProvider.class.getName(), version); //NOI18N
    }

    public EclipseLinkProvider(){
        this(null); //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EclipseLinkProvider.class, "LBL_EclipseLink") + (getVersion()!=null ? " (JPA "+getVersion()+")" : ""); //NOI18N
    }

    @Override
    public String getJdbcUrl() {
        return   Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.url" : super.getJdbcUrl();
    }

    @Override
    public String getJdbcDriver() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.driver" : super.getJdbcDriver();
    }

    @Override
    public String getJdbcUsername() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.user" : super.getJdbcUsername();
    }

    @Override
    public String getJdbcPassword() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "eclipselink.jdbc.password" : super.getJdbcPassword();
    }

    @Override
    public String getAnnotationProcessor() {
        return (getVersion()!=null && !Persistence.VERSION_1_0.equals(getVersion())) ? "org.eclipse.persistence.internal.jpa.modelgen.CanonicalModelProcessor" : super.getAnnotationProcessor();
    }

    @Override
    public String getAnnotationSubPackageProperty() {
        return PersistenceUnitProperties.CANONICAL_MODEL_SUB_PACKAGE;//NOI18N
    }
    
    @Override
    public String getTableGenerationPropertyName() {
        return getVersion() != null && 
                (Persistence.VERSION_2_1.equals(getVersion()) || Persistence.VERSION_2_2.equals(getVersion()))
                ?  super.getTableGenerationPropertyName()
                : PersistenceUnitProperties.DDL_GENERATION;
    }

    @Override
    public String getTableGenerationDropCreateValue() {
        return getVersion() != null && 
                (Persistence.VERSION_2_1.equals(getVersion()) || Persistence.VERSION_2_2.equals(getVersion()))
                ? super.getTableGenerationDropCreateValue()
                : PersistenceUnitProperties.DROP_AND_CREATE;
    }

    @Override
    public String getTableGenerationCreateValue() {
        return getVersion() != null && 
                (Persistence.VERSION_2_1.equals(getVersion()) || Persistence.VERSION_2_2.equals(getVersion()))
                ? super.getTableGenerationCreateValue()
                : PersistenceUnitProperties.CREATE_ONLY;
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
