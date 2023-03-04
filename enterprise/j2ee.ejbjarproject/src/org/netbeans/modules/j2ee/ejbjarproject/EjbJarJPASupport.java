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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;

/**
 * Implements the SPI interfaces providing support for data source handling.
 *
 * @author Erno Mononen
 */
public class EjbJarJPASupport implements JPADataSourcePopulator, JPADataSourceProvider{
    
    private final EjbJarProject project;
    
    /** Creates a new instance of EjbJarJPASupport */
    public EjbJarJPASupport(EjbJarProject project) {
        this.project = project;
    }
    
    public void connect(JComboBox comboBox) {
        DatasourceUIHelper.connect(project.getEjbModule(), comboBox);
    }
    
    public List<JPADataSource> getDataSources() {
        
        List<Datasource> datasources = new ArrayList<Datasource>();
        try {
            datasources.addAll(project.getEjbModule().getModuleDatasources());
        } catch (ConfigurationException e) {
            // TODO: it would be reasonable to rethrow this exception, see #96791
        }
        try {
            datasources.addAll(project.getEjbModule().getServerDatasources());
        } catch (ConfigurationException e) {
            // TODO: it would be reasonable to rethrow this exception, see #96791
        }

        List<JPADataSource> result = new ArrayList<JPADataSource>(datasources.size());
        for(Datasource each : datasources){
            result.add(new DatasourceWrapper(each));
        }
        return result;
    }

    public JPADataSource toJPADataSource(Object dataSource) {
        if (dataSource instanceof JPADataSource){
            return (JPADataSource) dataSource;
        } else if (dataSource instanceof Datasource){
            return new DatasourceWrapper((Datasource) dataSource);
        }
        return null;
    }
    

    /**
     * Provides <code>JPADataSource</code> interface for <code>Datasource</code>s.
     */ 
    private static class DatasourceWrapper implements Datasource, JPADataSource{
        private Datasource delegate;

        DatasourceWrapper(Datasource datasource){
            this.delegate = datasource;
        }

        public String getJndiName() {
            return delegate.getJndiName();
        }

        public String getUrl() {
            return delegate.getUrl();
        }

        public String getUsername() {
            return delegate.getUsername();
        }

        public String getPassword() {
            return delegate.getPassword();
        }

        public String getDriverClassName() {
            return delegate.getDriverClassName();
        }

        public String getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
