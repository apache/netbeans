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

package org.netbeans.modules.payara.jakartaee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

class AntDeploymentProviderImpl implements AntDeploymentProvider {

    /** Property files location in Payara configuration directory. */
    private static String PROPERTIES_PATH = "/PayaraEE6/Properties";

    /**
     * Returns property files location in Payara configuration directory.
     * <p/>
     * New property configuration directory is created when not exists under
     * NetBeans configuration directory.
     * <p/>
     * @return Property files location in Payara configuration directory.
     */
    private static File getPropertiesDir() {
        FileObject dir = FileUtil.getConfigFile(PROPERTIES_PATH);
        if (dir == null) {
            try {
                dir = FileUtil.createFolder(
                        FileUtil.getConfigRoot(), PROPERTIES_PATH);
            } catch(IOException ex) {
                Logger.getLogger("payara").log(Level.INFO, null, ex);
            }
        }
        return FileUtil.toFile(dir);
    }

    private final File propFile;
    private final Properties props;

    AntDeploymentProviderImpl(Hk2DeploymentManager dm, Hk2OptionalFactory aThis) {        
        PayaraModule commonSupport = dm.getCommonServerSupport();
        // compute the properties file path
        propFile = computeFile(commonSupport);
        // compute the property values.
        props = computeProps(commonSupport);
    }

    @Override
    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        InputStream is = AntDeploymentProviderImpl.class.getResourceAsStream("ant-deploy.xml"); // NOI18N            
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
    }

    @Override
    public File getDeploymentPropertiesFile() {
        if (!propFile.exists()) {
            // generate the deployment properties file only if it does not exist
            try {
                FileObject fo = FileUtil.createData(propFile);
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    OutputStream os = fo.getOutputStream(lock);
                    try {
                        props.store(os, ""); // NOI18N
                    } finally {
                        if (null != os) {
                            os.close();
                        }
                    }
                } finally {
                    if (null != lock) {
                        lock.releaseLock();
                    }
                }
            } catch (IOException ioe) {
                Logger.getLogger("payara-jakartaee").log(Level.INFO, null, ioe);      //NOI18N
            }
        }
        return propFile;
    }

    private File computeFile(PayaraModule commonSupport) {
        String url = commonSupport.getInstanceProperties().get(PayaraModule.URL_ATTR);
        String domainDir = commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR);
        String domain = commonSupport.getInstanceProperties().get(PayaraModule.DOMAIN_NAME_ATTR);
        String user = commonSupport.getInstanceProperties().get(PayaraModule.USERNAME_ATTR);
        String name = "pfv3" + (url+domainDir+domain+user).hashCode() + "";  // NOI18N
        return new File(getPropertiesDir(), name + ".properties"); // NOI18N
    }

    private Properties computeProps(PayaraModule commonSupport) {
        //PayaraModule commonSupport = dm.getCommonServerSupport();
        Properties retVal = new Properties();
        retVal.setProperty("pfv3.root", commonSupport.getInstanceProperties().get(PayaraModule.PAYARA_FOLDER_ATTR)); //getPlatformRoot().getAbsolutePath()); // NOI18N
        String webUrl = "http://" + commonSupport.getInstanceProperties().get(PayaraModule.HOSTNAME_ATTR) + 
                ":" + commonSupport.getInstanceProperties().get(PayaraModule.HTTPPORT_ATTR);
        retVal.setProperty("pfv3.url", webUrl);                // NOI18N
        webUrl = "http://" + commonSupport.getInstanceProperties().get(PayaraModule.HOSTNAME_ATTR) +
                ":" + commonSupport.getInstanceProperties().get(PayaraModule.ADMINPORT_ATTR);
        retVal.setProperty("pfv3.admin.url", webUrl);                // NOI18N
        retVal.setProperty("pfv3.username", commonSupport.getInstanceProperties().get(PayaraModule.USERNAME_ATTR));
        retVal.setProperty("pfv3.host",commonSupport.getInstanceProperties().get(PayaraModule.HOSTNAME_ATTR));
        retVal.setProperty("pfv3.port",commonSupport.getInstanceProperties().get(PayaraModule.ADMINPORT_ATTR));
        return retVal;
    }
}
