/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        J2eePlatform platform  = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
        
        if (platform == null) {
            return null;
        }
        JpaSupport support = JpaSupport.getInstance(platform);
        JpaProvider provider = support.getDefaultProvider();
        if (provider != null) {
            return (Persistence.VERSION_2_1.equals(version) && provider.isJpa21Supported())
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
