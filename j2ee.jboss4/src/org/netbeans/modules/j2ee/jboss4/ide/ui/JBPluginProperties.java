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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public class JBPluginProperties {
    
    public static final String PROPERTY_DISPLAY_NAME ="displayName";//NOI18N
    public static final String PROPERTY_SERVER = "server";//NOI18N
    public static final String PROPERTY_DEPLOY_DIR = "deploy-dir";//NOI18N
    public static final String PROPERTY_SERVER_DIR = "server-dir";//NOI18N
    public static final String PROPERTY_ROOT_DIR = "root-dir";//NOI18N
    public static final String PROPERTY_HOST = "host";//NOI18N
    public static final String PROPERTY_PORT = "port";//NOI18N
    public static final String PROPERTY_JMX_PORT = "jmx-Port";//NOI18N
    public static final String PROPERTY_JAVA_OPTS = "java_opts"; // NOI18N
    
    private static JBPluginProperties pluginProperties = null;
    private String installLocation;
    private String domainLocation;
    
    
    public static JBPluginProperties getInstance(){
        if(pluginProperties==null){
            pluginProperties = new JBPluginProperties();
        }
        return pluginProperties;
    }
    
    
    
    /** Creates a new instance of */
    private JBPluginProperties() {
        java.io.InputStream inStream = null;
        try {
            try {
                propertiesFile = getPropertiesFile();
                if (null != propertiesFile)
                    inStream = propertiesFile.getInputStream();
            } catch (java.io.FileNotFoundException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } finally {
                loadPluginProperties(inStream);
                if (null != inStream)
                    inStream.close();
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
    }
    
    void loadPluginProperties(java.io.InputStream inStream) {
        Properties inProps = new Properties();
        if (null != inStream)
            try {
                inProps.load(inStream);
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        String loc = inProps.getProperty(INSTALL_ROOT_KEY);
        if (loc!=null){// try to get the default value
            setInstallLocation(loc);
        }
    }
    
    private static final String INSTALL_ROOT_KEY = "installRoot"; // NOI18N
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; //NOI18N
    
    
    private  FileObject propertiesFile = null;
    
    private FileObject getPropertiesFile() throws java.io.IOException {
        FileObject dir = FileUtil.getConfigFile("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("jb","properties"); // NOI18N
            if (null == retVal) {
                retVal = dir.createData("jb","properties"); //NOI18N
            }
        }
        return retVal;
    }
    
    
    public void saveProperties(){
        Properties outProp = new Properties();
        String installRoot = getInstallLocation();
        if (installRoot != null)
            outProp.setProperty(INSTALL_ROOT_KEY, installRoot);
        
        FileLock l = null;
        java.io.OutputStream outStream = null;
        try {
            if (null != propertiesFile) {
                try {
                    l = propertiesFile.lock();
                    outStream = propertiesFile.getOutputStream(l);
                    if (null != outStream)
                        outProp.store(outStream, "");
                } catch (java.io.IOException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                } finally {
                    if (null != outStream)
                        outStream.close();
                    if (null != l)
                        l.releaseLock();
                }
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
    }

    public boolean isCurrentServerLocationValid() {
        if (getInstallLocation() != null) {
            return JBPluginUtils.isGoodJBServerLocation(new File(getInstallLocation()), (Version) null);
        }

        return false;
    }

    public void setInstallLocation(String installLocation) {
        if (installLocation.endsWith(File.separator)) {
            installLocation = installLocation.substring(0, installLocation.length() - 1);
        }

        this.installLocation = installLocation;
    }

    public String getInstallLocation() {
        return this.installLocation;
    }

    public void setDomainLocation(String domainLocation) {
        if (domainLocation.endsWith(File.separator)) {
            domainLocation = domainLocation.substring(0, domainLocation.length() - 1);
        }

        this.domainLocation = domainLocation;
    }

    public String getDomainLocation() {
        return domainLocation;
    }
}
