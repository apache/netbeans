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
package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.util.NbBundle;

/**
 *
 * @author Erno Mononen
 */
class OpenJPAProvider extends Provider{


    public OpenJPAProvider() {
        this(null);
    }
    public OpenJPAProvider(String version) {
        super("org.apache.openjpa.persistence.PersistenceProviderImpl", version); //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(KodoProvider.class, "LBL_OpenJPA") + (getVersion()!=null ? " (JPA "+getVersion()+")" : ""); //NOI18N
    }
    
    @Override
    public String getJdbcUrl() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionURL" : super.getJdbcUrl();//NOI18N
    }
    
    @Override
    public String getJdbcDriver() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionDriverName" : super.getJdbcDriver();//NOI18N
    }
    
    @Override
    public String getJdbcUsername() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionUserName" : super.getJdbcUsername();//NOI18N
    }
    
    @Override
    public String getJdbcPassword() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionPassword" : super.getJdbcPassword();//NOI18N
    }

    @Override
    public String getAnnotationProcessor() {
        return (getVersion()!=null && !Persistence.VERSION_1_0.equals(getVersion())) ? "org.apache.openjpa.persistence.meta.AnnotationProcessor6" : super.getAnnotationProcessor();
    }
    
    @Override
    public String getTableGenerationPropertyName() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationPropertyName() : "openjpa.jdbc.SynchronizeMappings";//NOI18N
    }

    @Override
    public String getTableGenerationDropCreateValue() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationDropCreateValue() : "buildSchema(SchemaAction='add,deleteTableContents',ForeignKeys=true)";//NOI18N
    }

    @Override
    public String getTableGenerationCreateValue() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationCreateValue() : "buildSchema(ForeignKeys=true)";//NOI18N
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
