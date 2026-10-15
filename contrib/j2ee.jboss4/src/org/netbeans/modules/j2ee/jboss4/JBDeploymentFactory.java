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
package org.netbeans.modules.j2ee.jboss4;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "jboss-deployer:"; // NOI18N
    
    private static final String DISCONNECTED_URI = "jboss-deployer:http://localhost:8080&"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(JBDeploymentFactory.class.getName());

    /**
     * Mapping of a instance properties to a deployment factory.
     * <i>GuardedBy(JBDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, DeploymentFactory> factoryCache =
            new WeakHashMap<InstanceProperties, DeploymentFactory>();

    /**
     * Mapping of a instance properties to a deployment manager.
     * <i>GuardedBy(JBDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, JBDeploymentManager> managerCache =
            new WeakHashMap<InstanceProperties, JBDeploymentManager>();

    private final Map<InstanceProperties, JBDeploymentFactory.JBClassLoader> classLoaderCache =
            new WeakHashMap<InstanceProperties, JBDeploymentFactory.JBClassLoader>();

    private static JBDeploymentFactory instance;

    private JBDeploymentFactory() {
        super();
    }

    public static synchronized JBDeploymentFactory getInstance() {
        if (instance == null) {
            instance = new JBDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);

            registerDefaultServerInstance();
        }

        return instance;
    }

    public static class JBClassLoader extends URLClassLoader {

        public JBClassLoader(URL[] urls, ClassLoader parent) throws MalformedURLException, RuntimeException {
            super(urls, parent);
        }

        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

       public Enumeration<URL> getResources(String name) throws IOException {
           // get rid of annoying warnings
           if (name.indexOf("jndi.properties") != -1) {// || name.indexOf("i18n_user.properties") != -1) { // NOI18N
               return Collections.enumeration(Collections.<URL>emptyList());
           }

           return super.getResources(name);
       }
    }

    public synchronized JBClassLoader getJBClassLoader(InstanceProperties ip) {
        JBClassLoader cl = classLoaderCache.get(ip);
        if (cl == null) {
            DeploymentFactory factory = factoryCache.get(ip);
            if (factory != null) {
                cl = (JBClassLoader) factory.getClass().getClassLoader();
            }
            if (cl == null) {
                cl = createJBClassLoader(ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR),
                            ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR));
            }
            classLoaderCache.put(ip, cl);
        }
        return cl;
    }

    public static JBClassLoader createJBClassLoader(String serverRoot, String domainRoot) {
        try {

            Version jbossVersion = JBPluginUtils.getServerVersion(new File (serverRoot));
            boolean version5Above = (jbossVersion != null && jbossVersion.compareToIgnoreUpdate(JBPluginUtils.JBOSS_5_0_0) >= 0);

            // dom4j.jar library for JBoss Application Server 4.0.4 and lower and JBoss Application Server 5.0
            File domFile = new File(serverRoot , JBPluginUtils.LIB + "dom4j.jar"); // NOI18N
            if (!domFile.exists()) {
                // dom4j.jar library for JBoss Application Server 4.0.5
                domFile = new File(domainRoot, JBPluginUtils.LIB + "dom4j.jar"); // NOI18N
            }

            String sep = File.separator;
            if (!domFile.exists() && jbossVersion != null && "7".equals(jbossVersion.getMajorNumber())) {
                domFile = new File(serverRoot, JBPluginUtils.getModulesBase(serverRoot)
                        + "org" + sep + "dom4j" + sep + "main" + sep + "dom4j-1.6.1.jar"); // NOI18N
            }
            if (!domFile.exists()) {
                domFile = null;
                LOGGER.log(Level.INFO, "No dom4j.jar availabale on classpath"); // NOI18N
            }

            // jbosssx-client.jar JBoss Application Server 5.0
//            File sxClient50 = new File(serverRoot + "/client/jbosssx-client.jar"); // NOI18N
//
//            // jboss-client.jar JBoss Application Server 5.0
//            File client50 = new File(serverRoot + "/client/jboss-client.jar"); // NOI18N
//
//            // jboss-common-core.jar for JBoss Application Server 5.0
//            File core50 = new File(serverRoot + "/client/jboss-common-core.jar"); // NOI18N
//
//            // jboss-logging-spi.jar for JBoss Application Server 5.0
//            File logging50 = new File(serverRoot + "/client/jboss-logging-spi.jar"); // NOI18N

            List<URL> urlList = new ArrayList<URL>();

            if (domFile != null) {
                urlList.add(domFile.toURI().toURL());
            }

            if (jbossVersion != null && "7".equals(jbossVersion.getMajorNumber())) {
                File org = new File(serverRoot, JBPluginUtils.getModulesBase(serverRoot) + "org");
                File jboss = new File(org, "jboss");
                File as = new File(jboss, "as");
                
                if (domFile != null && domFile.exists()) {
                    urlList.add(domFile.toURI().toURL());
                }
                
                urlList.add(new File(serverRoot, "jboss-modules.jar").toURI().toURL());
                //urlList.add(new File(serverRoot, "bin"+sep+"client"+sep+"jboss-client.jar").toURI().toURL());
                addUrl(urlList, new File(serverRoot), "bin" + sep + "client", Pattern.compile("jboss-client.*.jar"));

                addUrl(urlList, jboss, "logging" + sep + "main", Pattern.compile("jboss-logging-.*.jar"));
                addUrl(urlList, jboss, "threads" + sep + "main", Pattern.compile("jboss-threads-.*.jar"));
                addUrl(urlList, jboss, "remoting3" + sep + "main", Pattern.compile("jboss-remoting-.*.jar"));
                addUrl(urlList, jboss, "xnio" + sep + "main", Pattern.compile("xnio-api-.*.jar"));
                addUrl(urlList, jboss, "xnio" + sep + "nio" + sep + "main", Pattern.compile("xnio-nio-.*.jar"));
                addUrl(urlList, jboss, "dmr" + sep + "main", Pattern.compile("jboss-dmr-.*.jar"));
                addUrl(urlList, jboss, "msc" + sep + "main", Pattern.compile("jboss-msc-.*.jar"));
                addUrl(urlList, jboss, "common-core" + sep + "main", Pattern.compile("jboss-common-core-.*.jar"));
                addUrl(urlList, as, "ee" + sep + "deployment" + sep + "main", Pattern.compile("jboss-as-ee-deployment-.*.jar"));
                addUrl(urlList, as, "naming" + sep + "main", Pattern.compile("jboss-as-naming-.*.jar"));
                addUrl(urlList, as, "controller-client" + sep + "main", Pattern.compile("jboss-as-controller-client-.*.jar"));
                addUrl(urlList, as, "protocol" + sep + "main", Pattern.compile("jboss-as-protocol-.*.jar"));

            } else if (version5Above) {
                // get lient class path for Jboss 5.0
                List<URL> clientClassUrls = JBPluginUtils.getJB5ClientClasspath(
                        serverRoot);
                urlList.addAll(clientClassUrls);

                File runFile = new File(serverRoot, "bin" + File.separator + "run.jar"); // NOI18N
                if ( runFile.exists()) {
                    urlList.add(runFile.toURI().toURL());
                }

                File securityFile = new File(serverRoot, "common" + File.separator // NOI18N
                        + "lib" + File.separator + "jboss-security-aspects.jar"); // NOI18N
                if (securityFile.exists()) {
                    urlList.add(securityFile.toURI().toURL());
                }
                File profileFile = new File(serverRoot, "common" + File.separator // NOI18N
                        + "lib" + File.separator + "jboss-profileservice.jar"); // NOI18N
                if (profileFile.exists()) {
                    urlList.add(profileFile.toURI().toURL());
                }                
                File managedFile = new File(serverRoot, "lib" + File.separator + "jboss-managed.jar"); // NOI18N
                if (managedFile.exists()) {
                    urlList.add(managedFile.toURI().toURL());
                }
                File metaFile = new File(serverRoot, "lib" + File.separator + "jboss-metatype.jar"); // NOI18N
                if (metaFile.exists()) {
                    urlList.add(metaFile.toURI().toURL());
                }                 
            } else {  // version < 5.0
                urlList.add(
                        new File(serverRoot , JBPluginUtils.CLIENT + "jbossall-client.jar").toURI().toURL());      //NOI18N
                urlList.add(
                        new File(serverRoot , JBPluginUtils.CLIENT + "jboss-deployment.jar").toURI().toURL());     //NOI18N
                urlList.add(
                        new File(serverRoot, JBPluginUtils.CLIENT + "jnp-client.jar").toURI().toURL());           //NOI18N

                // jboss-common-client.jar JBoss Application Server 4.x
                File client40 = new File(serverRoot , JBPluginUtils.CLIENT + "jboss-common-client.jar"); // NOI18N
                if (client40.exists()) {
                    urlList.add(client40.toURI().toURL());
                }
            }

            JBClassLoader loader = new JBClassLoader(urlList.toArray(new URL[] {}), JBDeploymentFactory.class.getClassLoader());
            return loader;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return null;
    }

    private static void addUrl(List<URL> result, File root, String path, final Pattern pattern) {
        File folder = new File(root, path);
        File[] children = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name).matches();
            }
        });
        if (children != null) {
            for (File child : children) {
                try {
                    result.add(child.toURI().toURL());
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
    }

    @Override
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }

        return false;
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        synchronized (JBDeploymentFactory.class) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip != null) {
                JBDeploymentManager dm = managerCache.get(ip);
                if (dm != null) {
                    return dm;
                }
            }

            try {
                DeploymentFactory df = getFactory(uri);
                if (df == null) {
                    throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N
                }

                String jbURI = uri;
                try {
                    int index1 = uri.indexOf('#'); // NOI18N
                    int index2 = uri.indexOf('&'); // NOI18N
                    int index = Math.min(index1, index2);
                    jbURI = uri.substring(0, index); // NOI18N
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, null, e);
                }

                // see #228619
                // The default host where the DM is connecting is based on
                // serverHost parameter if it is null it uses InetAddress.getLocalHost()
                // which is however based on hostname. If hostname is not mapped
                // to localhost (the interface where the JB is running) we get
                // an excpetion
                if (jbURI.endsWith("as7")) { // NOI18N
                    jbURI = jbURI + "&serverHost=" // NOI18N
                            + (ip != null ? ip.getProperty(JBPluginProperties.PROPERTY_HOST) : "localhost"); // NOI18N
                }
                JBDeploymentManager dm = new JBDeploymentManager(df, uri, jbURI, uname, passwd);
                if (ip != null) {
                    managerCache.put(ip, dm);
                }
                return dm;
            } catch (NoClassDefFoundError e) {
                DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
                dmce.initCause(e);
                throw dmce;
            }
        }
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(JBDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                // null ip either means that the instance is not registered, or that this is the disconnected URL
                if (!DISCONNECTED_URI.equals(uri)) {
                    throw new DeploymentManagerCreationException("JBoss instance " + uri + " is not registered in the IDE."); // NOI18N
                }
            }

            if (ip != null) {
                String root = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
                if (root == null || !new File(root).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent server root " + root); // NOI18N
                }
                String server = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                if (server == null || !new File(server).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent domain root " + server); // NOI18N
                }
            }
            
            return new JBDeploymentManager(null, uri, null, null, null);
        } catch (NoClassDefFoundError e) {
            DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
            dmce.initCause(e);
            throw dmce;
        }
    }

    public String getProductVersion() {
        return NbBundle.getMessage (JBDeploymentFactory.class, "LBL_JBossFactoryVersion");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(JBDeploymentFactory.class, "SERVER_NAME"); // NOI18N
    }

    private DeploymentFactory getFactory(String instanceURL) {
        DeploymentFactory jbossFactory = null;
        try {
            String jbossRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);

            String domainRoot = InstanceProperties.getInstanceProperties(instanceURL).
                                    getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);

            // if jbossRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains
            // install location of the instance being registered
            if (jbossRoot == null) {
                jbossRoot = JBPluginProperties.getInstance().getInstallLocation();
            }

            // if domainRoot is null, then we are in a server instance registration process, thus this call
            // is made from InstanceProperties creation -> JBPluginProperties singleton contains
            // install location of the instance being registered
            if (domainRoot == null) {
                domainRoot = JBPluginProperties.getInstance().getDomainLocation();
            }

            InstanceProperties ip = InstanceProperties.getInstanceProperties(instanceURL);
            synchronized (JBDeploymentFactory.class) {
                if (ip != null) {
                    jbossFactory = (DeploymentFactory) factoryCache.get(ip);
                }
                if (jbossFactory == null) {
                    Version version = JBPluginUtils.getServerVersion(new File(jbossRoot));
                    URLClassLoader loader = (ip != null) ? getJBClassLoader(ip) : createJBClassLoader(jbossRoot, domainRoot);
                    if(version!= null && "7".equals(version.getMajorNumber())) {
                        Class<?> c = loader.loadClass("org.jboss.as.ee.deployment.spi.factories.DeploymentFactoryImpl");
                        c.getMethod("register").invoke(null);
                        jbossFactory = (DeploymentFactory) c.getDeclaredConstructor().newInstance();//NOI18N
                    } else {
                        jbossFactory = (DeploymentFactory) loader.loadClass("org.jboss.deployment.spi.factories.DeploymentFactoryImpl").getDeclaredConstructor().newInstance();//NOI18N
                    }


                    if (ip != null) {
                        factoryCache.put(ip, jbossFactory);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return jbossFactory;
    }

    private static final String INSTALL_ROOT_PROP_NAME = "org.netbeans.modules.j2ee.jboss4.installRoot"; // NOI18N

    private static void registerDefaultServerInstance() {
        try {
            FileObject serverInstanceDir = getServerInstanceDir();
            String serverLocation = getDefaultInstallLocation();
            String domainLocation = serverLocation + File.separator + "server" + File.separator + "default"; // NOI18N
            setRemovability(serverInstanceDir, domainLocation);
            File serverDirectory = new File(serverLocation);
            if (JBPluginUtils.isGoodJBLocation(serverDirectory, new File(domainLocation)))
            {
                if (!isAlreadyRegistered(serverInstanceDir, domainLocation)) {
                    String host = "localhost"; // NOI18N
                    String port = JBPluginUtils.getHTTPConnectorPort(domainLocation); // NOI18N
                    register(serverInstanceDir, serverLocation, domainLocation, host, port);
                }
            }
        }
        catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.getMessage());
        }
    }

    private static String getDefaultInstallLocation() {
        String installRoot = System.getProperty(INSTALL_ROOT_PROP_NAME);
        if (installRoot != null && new File(installRoot).exists()) {
            return installRoot;
        }

        return "";
    }

    private static boolean isAlreadyRegistered(FileObject serverInstanceDir, String domainLocation) throws IOException {
        String domainLocationCan = new File(domainLocation).getCanonicalPath();
        for (FileObject instanceFO : serverInstanceDir.getChildren()) {
            String installedLocation = (String)instanceFO.getAttribute(JBPluginProperties.PROPERTY_SERVER_DIR);
            if (installedLocation != null) {
                String installedLocationCan = new File(installedLocation).getCanonicalPath();
                if (domainLocationCan.equals(installedLocationCan)) {
                    return true; // do not overwrite registered instance
                }
            }
        }

        return false;
    }

    private static void setRemovability(FileObject serverInstanceDir, String domainLocation) throws IOException {
        String domainLocationCan = new File(domainLocation).getCanonicalPath();
        for (FileObject instanceFO : serverInstanceDir.getChildren()) {
            String url = (String)instanceFO.getAttribute(InstanceProperties.URL_ATTR);
            if (url == null) { // can occur if some unxpected file is in the directory
                LOGGER.log(Level.INFO, "No server URL in " + FileUtil.getFileDisplayName(instanceFO));
            } else if (url.startsWith(URI_PREFIX)) { // it's JBoss instance
                String installedLocation = (String)instanceFO.getAttribute(JBPluginProperties.PROPERTY_SERVER_DIR);
                String installedLocationCan = new File(installedLocation).getCanonicalPath();
                if (domainLocationCan.equals(installedLocationCan)) {
                    instanceFO.setAttribute(InstanceProperties.REMOVE_FORBIDDEN, Boolean.TRUE);
                }
                else {
                    if (instanceFO.getAttribute(InstanceProperties.REMOVE_FORBIDDEN) != null) {
                        instanceFO.setAttribute(InstanceProperties.REMOVE_FORBIDDEN, Boolean.FALSE);
                    }
                }
            }
        }
    }

    private static void register(FileObject serverInstanceDir, String serverLocation, String domainLocation, String host, String port) throws IOException {
        String displayName = generateDisplayName(serverInstanceDir);

        String url = URI_PREFIX + host + ":" + port + "#default&" + serverLocation;    // NOI18N

        String name = FileUtil.findFreeFileName(serverInstanceDir, "instance", null); // NOI18N
        FileObject instanceFO = serverInstanceDir.createData(name);

        instanceFO.setAttribute(InstanceProperties.URL_ATTR, url);
        instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, "");
        instanceFO.setAttribute(InstanceProperties.PASSWORD_ATTR, "");
        instanceFO.setAttribute(InstanceProperties.DISPLAY_NAME_ATTR, displayName);
        instanceFO.setAttribute(InstanceProperties.REMOVE_FORBIDDEN, "true");

        instanceFO.setAttribute(JBPluginProperties.PROPERTY_SERVER, "default"); // NOI18N
        String deployDir = JBPluginUtils.getDeployDir(domainLocation);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_DEPLOY_DIR, deployDir);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_SERVER_DIR, domainLocation);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_ROOT_DIR, serverLocation);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_HOST, host);
        instanceFO.setAttribute(JBPluginProperties.PROPERTY_PORT, port);
    }

    private static FileObject getServerInstanceDir() {
        FileObject dir = FileUtil.getConfigFile("J2EE/InstalledServers"); // NOI18N
        return dir;
    }

    private static String generateDisplayName(FileObject serverInstanceDir) {
        final String serverName = NbBundle.getMessage(JBDeploymentFactory.class, "SERVER_NAME"); // NOI18N

        String instanceName = serverName;
        int counter = 1;
        Set<String> registeredInstances = getServerInstancesNames(serverInstanceDir);

        while (registeredInstances.contains(instanceName.toUpperCase())) {
            instanceName = serverName  + " (" + String.valueOf(counter++) + ")";
        }

        return instanceName;
    }

    private static Set<String> getServerInstancesNames(FileObject serverInstanceDir) {
        Set<String> names = new HashSet<String>();
        for (FileObject instanceFO : serverInstanceDir.getChildren()) {
            String instanceName = (String)instanceFO.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR);
            names.add(instanceName.toUpperCase());
        }

        return names;
    }

}

