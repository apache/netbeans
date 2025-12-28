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

package org.netbeans.modules.maven.j2ee;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.j2ee.ejb.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider2;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.ui.SelectAppServerPanel;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ChangeSupport;

/**
 * An implementation of the <code>JPAModuleInfo, JPADataSourcePopulator, JPADataSourceProvider, ServerStatusProvider</code>
 * for maven projects.
 * 
 * @author Milos Kleint
 */
@ProjectServiceProvider(service = {JPAModuleInfo.class, JPADataSourcePopulator.class, JPADataSourceProvider.class, ServerStatusProvider2.class}, projectType = {
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT
})
public class JPAStuffImpl implements JPAModuleInfo, JPADataSourcePopulator,
        JPADataSourceProvider, ServerStatusProvider2 {
    
    private final Project project;
    private ChangeSupport support = new ChangeSupport(this);
    
    public JPAStuffImpl(Project project) {
        this.project = project;
    }
    
    @Override
    public ModuleType getType() {
        EjbModuleProviderImpl im = project.getLookup().lookup(EjbModuleProviderImpl.class);
        if (im != null) {
            return JPAModuleInfo.ModuleType.EJB;
        }
        WebModuleProviderImpl im2 = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (im2 != null) {
            return JPAModuleInfo.ModuleType.WEB;
        }
        throw new IllegalStateException("Wrong placement of JPAModuleInfo in maven project " + project.getProjectDirectory());
    }

    @Override
    public String getVersion() {
        EjbModuleProviderImpl im = project.getLookup().lookup(EjbModuleProviderImpl.class);
        if (im != null) {
            return im.getModuleImpl().getModuleVersion();
        }
        WebModuleProviderImpl im2 = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (im2 != null) {
            return im2.getModuleImpl().getModuleVersion();
        }
        throw new IllegalStateException("Wrong placement of JPAModuleInfo in maven project " + project.getProjectDirectory());
    }

    @Override
    public Boolean isJPAVersionSupported(String version) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eePlatform platform  = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
        
        if (platform == null) {
            return null;
        }
        JpaSupport support = JpaSupport.getInstance(platform);
        JpaProvider provider = support.getDefaultProvider();
        if (provider != null) {
            return (Persistence.VERSION_3_2.equals(version) && provider.isJpa32Supported())
                    || (Persistence.VERSION_3_1.equals(version) && provider.isJpa31Supported())
                    || (Persistence.VERSION_3_0.equals(version) && provider.isJpa30Supported())
                    || (Persistence.VERSION_2_2.equals(version) && provider.isJpa22Supported())
                    || (Persistence.VERSION_2_1.equals(version) && provider.isJpa21Supported())
                    || (Persistence.VERSION_2_0.equals(version) && provider.isJpa2Supported())
                    || (Persistence.VERSION_1_0.equals(version) && provider.isJpa1Supported());
        }
        return null;
    }

    @Override
    public void connect(JComboBox comboBox) {
        J2eeModuleProvider prvd = project.getLookup().lookup(J2eeModuleProvider.class);
        DatasourceUIHelper.connect(project, prvd, comboBox);
    }

    @Override
    public List<JPADataSource> getDataSources() {

        J2eeModuleProvider prvd = project.getLookup().lookup(J2eeModuleProvider.class);
        List<Datasource> datasources = new ArrayList<>();
        try {
            datasources.addAll(prvd.getModuleDatasources());
        } catch (ConfigurationException e) {
            // TODO: it would be reasonable to rethrow this exception, see #96791
        }
        try {
            datasources.addAll(prvd.getServerDatasources());
        } catch (ConfigurationException e) {
            // TODO: it would be reasonable to rethrow this exception, see #96791
        }

        List<JPADataSource> result = new ArrayList<>(datasources.size());
        for(Datasource each : datasources){
            result.add(new DatasourceWrapper(each));
        }
        return result;
    }

    @Override
    public JPADataSource toJPADataSource(Object dataSource) {
        if (dataSource instanceof JPADataSource){
            return (JPADataSource) dataSource;
        } else if (dataSource instanceof Datasource){
            return new DatasourceWrapper((Datasource) dataSource);
        }
        return null;
    }

    @Override
    public boolean validServerInstancePresent() {
        J2eeModuleProvider prvd = project.getLookup().lookup(J2eeModuleProvider.class);
        return prvd != null && ServerUtil.isValidServerInstance(prvd);
    }

    @Override
    public boolean selectServer() {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        boolean res = SelectAppServerPanel.showServerSelectionDialog(project, provider, null);
        if (res) {
            // notify other parties that a server was selected
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    support.fireChange();
                }
            });
        }
        return res;
    }


    @Override
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    /**
     * Provides <code>JPADataSource</code> interface for <code>Datasource</code>s.
     */
    private static class DatasourceWrapper implements Datasource, JPADataSource{
        private Datasource delegate;

        DatasourceWrapper(Datasource datasource){
            this.delegate = datasource;
        }

        @Override
        public String getJndiName() {
            return delegate.getJndiName();
        }

        @Override
        public String getUrl() {
            return delegate.getUrl();
        }

        @Override
        public String getUsername() {
            return delegate.getUsername();
        }

        @Override
        public String getPassword() {
            return delegate.getPassword();
        }

        @Override
        public String getDriverClassName() {
            return delegate.getDriverClassName();
        }

        @Override
        public String getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

}
