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

package org.netbeans.modules.javaee.wildfly.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.javaee.wildfly.config.gen.JbossApp;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * EAR application deployment configuration handles jboss-app.xml configuration
 * file creation.
 *
 * @author sherold
 */
public class EarDeploymentConfiguration extends WildflyDeploymentConfiguration
implements ModuleConfiguration, DeploymentPlanConfiguration {

    private File jbossAppFile;
    private JbossApp jbossApp;

    public EarDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null, true);
    }

    /**
     * Creates a new instance of EarDeploymentConfiguration
     */
    public EarDeploymentConfiguration(J2eeModule j2eeModule, WildflyPluginUtils.Version version, boolean isWildFly) {
        super(j2eeModule, version, isWildFly);
        jbossAppFile = j2eeModule.getDeploymentConfigurationFile("META-INF/jboss-app.xml"); // NOI18N
        getJbossApp();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossAppFile));
            } catch(DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
            }
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    /**
     * Return jbossApp graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return jbossApp graph or null if the jboss-app.xml file is not parseable.
     */
    public synchronized JbossApp getJbossApp() {
        if (jbossApp == null) {
            try {
                if (jbossAppFile.exists()) {
                    // load configuration if already exists
                    try {
                        jbossApp = jbossApp.createGraph(jbossAppFile);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // jboss-app.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-app.xml if it does not exist yet
                    jbossApp = genereatejbossApp();
                    ResourceConfigurationHelper.writeFile(jbossAppFile, jbossApp);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return jbossApp;
    }

    @Override
    public void save(OutputStream os) throws ConfigurationException {
        JbossApp jbossApp = getJbossApp();
        if (jbossApp == null) {
            String msg = NbBundle.getMessage(EarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", jbossAppFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        try {
            jbossApp.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(EarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossAppFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }

    // private helper methods -------------------------------------------------

    /**
     * Genereate Context graph.
     */
    private JbossApp genereatejbossApp() {
        return new JbossApp();
    }
}
