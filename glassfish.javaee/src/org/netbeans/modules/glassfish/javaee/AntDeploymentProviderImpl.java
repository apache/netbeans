/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

class AntDeploymentProviderImpl implements AntDeploymentProvider {

    /** Property files location in GlassFish configuration directory. */
    private static String PROPERTIES_PATH = "/GlassFishEE6/Properties";

    /**
     * Returns property files location in GlassFish configuration directory.
     * <p/>
     * New property configuration directory is created when not exists under
     * NetBeans configuration directory.
     * <p/>
     * @return Property files location in GlassFish configuration directory.
     */
    private static File getPropertiesDir() {
        FileObject dir = FileUtil.getConfigFile(PROPERTIES_PATH);
        if (dir == null) {
            try {
                dir = FileUtil.createFolder(
                        FileUtil.getConfigRoot(), PROPERTIES_PATH);
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, null, ex);
            }
        }
        return FileUtil.toFile(dir);
    }

    private final File propFile;
    private final Properties props;

    AntDeploymentProviderImpl(Hk2DeploymentManager dm, Hk2OptionalFactory aThis) {        
        GlassfishModule commonSupport = dm.getCommonServerSupport();
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
                Logger.getLogger("glassfish-javaee").log(Level.INFO, null, ioe);      //NOI18N
            }
        }
        return propFile;
    }

    private File computeFile(GlassfishModule commonSupport) {
        String url = commonSupport.getInstanceProperties().get(GlassfishModule.URL_ATTR);
        String domainDir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        String domain = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);
        String user = commonSupport.getInstanceProperties().get(GlassfishModule.USERNAME_ATTR);
        String name = "gfv3" + (url+domainDir+domain+user).hashCode() + "";  // NOI18N
        return new File(getPropertiesDir(), name + ".properties"); // NOI18N
    }

    private Properties computeProps(GlassfishModule commonSupport) {
        //GlassfishModule commonSupport = dm.getCommonServerSupport();
        Properties retVal = new Properties();
        retVal.setProperty("gfv3.root", commonSupport.getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR)); //getPlatformRoot().getAbsolutePath()); // NOI18N
        String webUrl = "http://" + commonSupport.getInstanceProperties().get(GlassfishModule.HOSTNAME_ATTR) + 
                ":" + commonSupport.getInstanceProperties().get(GlassfishModule.HTTPPORT_ATTR);
        retVal.setProperty("gfv3.url", webUrl);                // NOI18N
        webUrl = "http://" + commonSupport.getInstanceProperties().get(GlassfishModule.HOSTNAME_ATTR) +
                ":" + commonSupport.getInstanceProperties().get(GlassfishModule.ADMINPORT_ATTR);
        retVal.setProperty("gfv3.admin.url", webUrl);                // NOI18N
        retVal.setProperty("gfv3.username", commonSupport.getInstanceProperties().get(GlassfishModule.USERNAME_ATTR));
        retVal.setProperty("gfv3.host",commonSupport.getInstanceProperties().get(GlassfishModule.HOSTNAME_ATTR));
        retVal.setProperty("gfv3.port",commonSupport.getInstanceProperties().get(GlassfishModule.ADMINPORT_ATTR));
        return retVal;
    }
}
