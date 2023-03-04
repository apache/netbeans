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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentDescriptorConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.weblogic9.dd.model.EarApplicationModel;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * EAR application deployment configuration handles weblogic-application.xml configuration 
 * file creation.
 *
 * @author sherold
 */
public class EarDeploymentConfiguration extends WLDeploymentConfiguration
        implements ModuleConfiguration, DeploymentPlanConfiguration, DeploymentDescriptorConfiguration {
    
    private final File file;
    private final J2eeModule j2eeModule;
    private final DataObject dataObject;
    
    private final Version serverVersion;
    
    private EarApplicationModel weblogicApplication;
     
    public EarDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null);
    }
    
    /**
     * Creates a new instance of EarDeploymentConfiguration 
     */
    public EarDeploymentConfiguration(J2eeModule j2eeModule, Version serverVersion) {

        super(j2eeModule, serverVersion);
        this.j2eeModule = j2eeModule;
        this.serverVersion = serverVersion;
        file = j2eeModule.getDeploymentConfigurationFile("META-INF/weblogic-application.xml"); // NOI18N
        getWeblogicApplication();
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(FileUtil.toFileObject(file));
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        this.dataObject = dataObject;
    }
       
    /**
     * Return weblogicApplication graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return weblogicApplication graph or null if the weblogic-application.xml file is not parseable.
     */
    public synchronized EarApplicationModel getWeblogicApplication() {
        if (weblogicApplication == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        weblogicApplication = EarApplicationModel.forFile(file);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // weblogic-application.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic-application.xml if it does not exist yet
                    weblogicApplication = genereateWeblogicApplication();
                    weblogicApplication.write(file);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return weblogicApplication;
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
    

    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public void dispose() {
    }

    @Override
    public boolean isDescriptorRequired() {
        return true;
    }

    // FIXME this is not a proper implementation - deployment PLAN should be saved
    // not a deployment descriptor    
    public void save(OutputStream os) throws ConfigurationException {
        EarApplicationModel weblogicApplication = getWeblogicApplication();
        if (weblogicApplication == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", file.getPath());
            throw new ConfigurationException(msg);
        }
        try {
            weblogicApplication.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", file.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate Context graph.
     */
    private EarApplicationModel genereateWeblogicApplication() {
        return EarApplicationModel.generate(serverVersion);
    }
}
