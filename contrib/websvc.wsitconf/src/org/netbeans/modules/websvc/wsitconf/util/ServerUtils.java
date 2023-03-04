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

package org.netbeans.modules.websvc.wsitconf.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Grebac
 */
public class ServerUtils {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    public static final String DEVNULL = "DEV-NULL"; //NOI18N

    public static J2eePlatform getJ2eePlatform(Project project) {
        String serverInstanceID = getServerInstanceID(project);
        if ((serverInstanceID != null) && (serverInstanceID.length() > 0) && (!DEVNULL.equals(serverInstanceID))) {
            try {
                return Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
            } catch (InstanceRemovedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public static String getServerInstanceID(Project p) {
        if (p != null) {
            J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (mp != null) {
                return mp.getServerInstanceID();
            }
        }
        return null;
    }

    public static String getServerName(Project p) {
        String sID = getServerInstanceID(p);
        if (sID != null) {
            J2eePlatform j2eePlatform;
            try {
                j2eePlatform = Deployment.getDefault().getServerInstance(sID).getJ2eePlatform();
                return j2eePlatform.getDisplayName();
            } catch (InstanceRemovedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public static final boolean isTomcat(Project project) {
        String sID = getServerInstanceID(project);
        if (sID != null) {
            if ((sID != null) && (sID.toLowerCase().contains("tomcat"))) {     //NOI18N
                return true;
            }
        }
        return false;
    }

    public static final boolean isGlassfish(Project project) {
        if (project != null) {
            J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
            if (mp == null) return false;
            return isGlassfish(mp.getServerInstanceID());
        }
        return false;
    }

    public static final boolean isGlassfish(String serverID) {
        if ((serverID != null) && ((serverID.toLowerCase().contains("appserv")) || (serverID.toLowerCase().contains("gfv3")))) {     //NOI18N
            return true;
        }
        return false;
    }

    public static J2eeModuleProvider getProvider(Project p) {
        if (p != null) {
            return p.getLookup().lookup(J2eeModuleProvider.class);
        }
        return null;
    }


    //
    public static final String BUNDLED_TOMCAT_SETTING = "J2EE/BundledTomcat/Setting"; // NOI18N
    
    public static FileObject getTomcatLocation(Project project) {

        String catalinaHome = null;
        String catalinaBase = null;

        File homeDir = null;
        File baseDir = null;

        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties ip = null;
        if (mp != null) {
            ip = mp.getInstanceProperties();
        }

        /* copied from TomcatProperties */
        /* START */
        String uri = ip.getProperty(InstanceProperties.URL_ATTR);
        final String home = "home=";    // NOI18N
        final String base = ":base=";   // NOI18N
        final String uriString = "http://";  // NOI18N
        int uriOffset = uri.indexOf (uriString);
        int homeOffset = uri.indexOf (home) + home.length ();
        int baseOffset = uri.indexOf (base, homeOffset);
        if (homeOffset >= home.length ()) {
            int homeEnd = baseOffset > 0 ? baseOffset : (uriOffset > 0 ? uriOffset - 1 : uri.length ());
            int baseEnd = uriOffset > 0 ? uriOffset - 1 : uri.length ();
            catalinaHome= uri.substring (homeOffset, homeEnd);
            if (baseOffset > 0) {
                catalinaBase = uri.substring (baseOffset + base.length (), baseEnd);
            }
            // Bundled Tomcat home and base dirs can be specified as attributes
            // specified in BUNDLED_TOMCAT_SETTING file. Tomcat manager URL can
            // then look like "tomcat:home=$bundled_home:base=$bundled_base" and
            // therefore remains valid even if Tomcat version changes. (issue# 40659)
            if (catalinaHome.length() > 0 && catalinaHome.charAt(0) == '$') {
                FileObject fo = FileUtil.getConfigFile(BUNDLED_TOMCAT_SETTING);
                if (fo != null) {
                    catalinaHome = fo.getAttribute(catalinaHome.substring(1)).toString();
                    if (catalinaBase != null && catalinaBase.length() > 0
                        && catalinaBase.charAt(0) == '$') {
                        catalinaBase = fo.getAttribute(catalinaBase.substring(1)).toString();
                    }
                }
            }
        }
        if (catalinaHome == null) {
            throw new IllegalArgumentException("CATALINA_HOME must not be null."); // NOI18N
        }
        homeDir = new File(catalinaHome);
        if (!homeDir.isAbsolute ()) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            homeDir = ifl.locate(catalinaHome, null, false);
        }
        if (!homeDir.exists()) {
            throw new IllegalArgumentException("CATALINA_HOME directory does not exist."); // NOI18N
        }
        if (catalinaBase != null) {
            baseDir = new File(catalinaBase);
            if (!baseDir.isAbsolute ()) {
                InstalledFileLocator ifl = InstalledFileLocator.getDefault();
                baseDir = ifl.locate(catalinaBase, null, false);
                if (baseDir == null) {
                    baseDir = new File(System.getProperty("netbeans.user"), catalinaBase);   // NOI18N
                }
            }
        }
        /* END */

        return ((baseDir == null) ?
            ((homeDir == null) ? null : FileUtil.toFileObject(homeDir)) : FileUtil.toFileObject(baseDir));
    }

    /**
     * Return server.xml file from the catalina base folder if the base folder is used
     * or from the catalina home folder otherwise.
     * <p>
     * <b>BEWARE</b>: If the catalina base folder is used but has not bee generated yet,
     * the server.xml file from the catalina home folder will be returned.
     * </p>
     */
    public static FileObject getServerXml(Project p) {
        String confServerXml = "conf/server.xml"; // NIO18N
        FileObject fO = getTomcatLocation(p);
        return (fO == null) ? null : fO.getFileObject(confServerXml);
    }

    public static FileObject getTomcatLocation(String serverID) {
        if (serverID == null) return null;
        FileObject folder = null;
        try {
            int begin = serverID.indexOf("home=") + 5;
            int end = serverID.indexOf(":", serverID.indexOf("home=") + 1);
            if (end <= begin) {
                end = serverID.length();
            }
            String location = serverID.substring(begin, end);
            File f = new File(location);
            if (f != null) {
                folder = FileUtil.toFileObject(f);
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, serverID, ex);
        }
        return folder;
    }

    public static String getStoreLocation(Project project, boolean trust, boolean client) {
        String storeLocation = null;
        if (project == null) {
            return storeLocation;
        }
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            String sID = mp.getServerInstanceID();
            InstanceProperties ip = mp.getInstanceProperties();
            if ((ip == null) || ("".equals(ip.getProperty("LOCATION")))) {
                return "";
            }
            storeLocation = getStoreLocation(sID, trust, client);
        }
        return storeLocation;
    }

    static String getStoreLocation(String serverInstanceID, boolean trust, boolean client) {
        String storeLocation = null;
        J2eePlatform j2eePlatform = null;
        File[] keyLocs = null;
        String store = null;
        try {
            j2eePlatform = Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (client) {
            store = trust ? J2eePlatform.TOOL_TRUSTSTORE_CLIENT : J2eePlatform.TOOL_KEYSTORE_CLIENT;
        } else {
            store = trust ? J2eePlatform.TOOL_TRUSTSTORE : J2eePlatform.TOOL_KEYSTORE;
        }
        keyLocs = j2eePlatform.getToolClasspathEntries(store);
        if ((keyLocs != null) && (keyLocs.length > 0)) {
            storeLocation = keyLocs[0].getAbsolutePath();
        }
        return storeLocation;
    }
}
