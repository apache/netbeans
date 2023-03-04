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

package org.netbeans.modules.tomcat5;

import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class AntDeploymentProviderImpl implements AntDeploymentProvider {
    
    private final TomcatManager tm;
    
    private static final Logger LOGGER = Logger.getLogger(AntDeploymentProviderImpl.class.getName()); // NOI18N
    
    public AntDeploymentProviderImpl(DeploymentManager dm) {
        tm = (TomcatManager)dm;
    }

    @Override
    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        String name = null;
        switch (tm.getTomcatVersion()) {
            case TOMCAT_110:
            case TOMCAT_101:
            case TOMCAT_100:
            case TOMCAT_90:
            case TOMCAT_80:
            case TOMCAT_70:
                name = "resources/tomcat-ant-deploy70.xml";
                break;
            case TOMCAT_60:
                name = "resources/tomcat-ant-deploy60.xml";
                break;
            case TOMCAT_55:
            case TOMCAT_50:
            default:
                name = "resources/tomcat-ant-deploy.xml";
        }
        
        InputStream is = AntDeploymentProviderImpl.class.getResourceAsStream(name); // NOI18N
        if (is == null) {
            // this should never happen, but better make sure
            LOGGER.log(Level.SEVERE, "Missing resource {0}.", name); // NOI18N
            return;
        }
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
    }

    @Override
    public File getDeploymentPropertiesFile() {
        TomcatProperties tp = tm.getTomcatProperties();
        File file = tp.getAntDeploymentPropertiesFile();
        if (!file.exists()) {
            // generate the deployment properties file only if it does not exist
            try {
                tp.storeAntDeploymentProperties(file, true);
            } catch (IOException ioe) {
                Logger.getLogger(AntDeploymentProviderImpl.class.getName()).log(Level.INFO, null, ioe);
            }
        }
        return file;
    }
}
