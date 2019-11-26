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
package org.netbeans.modules.javaee.wildfly;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class WildflyDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "wildfly-deployer:"; // NOI18N

    private static final String DISCONNECTED_URI = URI_PREFIX + "http://localhost:8080&"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WildflyDeploymentFactory.class.getName());

    /**
     * Mapping of a instance properties to a deployment factory.
     * <i>GuardedBy(WildflyDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, DeploymentFactory> factoryCache =
            new WeakHashMap<InstanceProperties, DeploymentFactory>();

    /**
     * Mapping of a instance properties to a deployment manager.
     * <i>GuardedBy(WildflyDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, WildflyDeploymentManager> managerCache =
            new WeakHashMap<InstanceProperties, WildflyDeploymentManager>();

    private final Map<InstanceProperties, WildflyDeploymentFactory.WildFlyClassLoader> classLoaderCache =
            new WeakHashMap<InstanceProperties, WildflyDeploymentFactory.WildFlyClassLoader>();

    private static WildflyDeploymentFactory instance;

    private WildflyDeploymentFactory() {
        super();
    }

    public static synchronized WildflyDeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WildflyDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }

    public static class WildFlyClassLoader extends URLClassLoader {

        /**
         * Patching the xnio code to avoid bug #249135
         * @see https://netbeans.org/bugzilla/show_bug.cgi?id=249135
         */
        private final boolean patchXnio;

        public WildFlyClassLoader(URL[] urls, ClassLoader parent, boolean patchXnio) throws MalformedURLException, RuntimeException {
            super(urls, parent);
            this.patchXnio = patchXnio;
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            // get rid of annoying warnings
            if (name.contains("jndi.properties")) { // NOI18N
                return Collections.enumeration(Collections.<URL>emptyList());
            }

            return super.getResources(name);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // see issue #249135
            if (patchXnio && "org.xnio.nio.WorkerThread".equals(name)) { // NOI18N
                try {
                    LOGGER.log(Level.INFO, "Patching the issue #249135");
                    String path = name.replace('.', '/').concat(".class"); // NOI18N
                    try (InputStream is = super.getResourceAsStream(path)) {
                        ClassReader cr = new ClassReader(is);
                        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES) {

                            @Override
                            protected String getCommonSuperClass(String string, String string1) {
                                if ("org/xnio/nio/NioHandle".equals(string) // NOI18N
                                        || "org/xnio/nio/NioHandle".equals(string1)) { // NOI18N
                                    return "java/lang/Object"; // NOI18N
                                }
                                return super.getCommonSuperClass(string, string1);
                            }
                        };
                        ClassNode node = new ClassNode(Opcodes.ASM7);
                        cr.accept(node, 0);

                        for (MethodNode m : (Collection<MethodNode>) node.methods) {
                            if ("execute".equals(m.name) // NOI18N
                                    && "(Ljava/lang/Runnable;)V".equals(m.desc)) { // NOI18N
                                InsnList list = m.instructions;
                                for (ListIterator it = list.iterator(); it.hasNext(); ) {
                                    AbstractInsnNode n = (AbstractInsnNode) it.next();
                                    if (n instanceof MethodInsnNode) {
                                        MethodInsnNode mn = (MethodInsnNode) n;
                                        if ("org/xnio/nio/Log".equals(mn.owner) // NOI18N
                                                && "threadExiting".equals(mn.name) // NOI18N
                                                && "()Ljava/util/concurrent/RejectedExecutionException;".equals(mn.desc)) { // NOI18N
                                            if (it.hasNext()) {
                                                AbstractInsnNode possibleThrow = (AbstractInsnNode) it.next();
                                                if (possibleThrow.getOpcode() == Opcodes.ATHROW) {
                                                    it.set(new InsnNode(Opcodes.POP));
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        node.accept(cw);
                        byte[] newBytecode = cw.toByteArray();
                        return super.defineClass(name, newBytecode, 0, newBytecode.length);
                    }
                } catch (Exception ex) {
                    // just fallback to original behavior
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return super.findClass(name);
        }
    }

    public synchronized WildFlyClassLoader getWildFlyClassLoader(InstanceProperties ip) {
        WildFlyClassLoader cl = classLoaderCache.get(ip);
        if (cl == null) {
            DeploymentFactory factory = factoryCache.get(ip);
            if (factory != null && factory.getClass().getClassLoader() instanceof WildFlyClassLoader) {
                cl = (WildFlyClassLoader) factory.getClass().getClassLoader();
            }
            if (cl == null) {
                cl = createWildFlyClassLoader(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
            }
            classLoaderCache.put(ip, cl);
        }
        return cl;
    }

    public static WildFlyClassLoader createWildFlyClassLoader(String serverRoot) {
        try {
            String sep = File.separator;
            List<URL> urlList = new ArrayList<>(20);
            File org = new File(serverRoot, WildflyPluginUtils.getModulesBase(serverRoot) + "org");
            addUrl(urlList, org, "dom4j" + sep + "main", Pattern.compile("dom4j-.*.jar"));
            if (urlList.isEmpty()) {
                LOGGER.log(Level.INFO, "No dom4j.jar availabale on classpath"); // NOI18N
            }
            File jboss = new File(org, "jboss");
            File wildfly = new File(org, "wildfly");
            File as = new File(jboss, "as");

            final File jbossModules = new File(serverRoot, "jboss-modules.jar");
            if(jbossModules.exists()) {
                urlList.add(Utilities.toURI(jbossModules).toURL());
            }
            final File jbossClient = new File(serverRoot, "bin" + sep + "client" + sep + "jboss-client.jar");
            if(jbossClient.exists()) {
                urlList.add(Utilities.toURI(jbossClient).toURL());
            }
            addUrl(urlList, jboss, "dmr" + sep + "main", Pattern.compile("jboss-dmr-.*.jar"));
            addUrl(urlList, jboss, "logging" + sep + "main", Pattern.compile("jboss-logging-.*.jar"));
            addUrl(urlList, jboss, "marshalling" + sep + "main", Pattern.compile("jboss-marshalling-.*.jar"));
            addUrl(urlList, jboss, "marshalling" + sep + "river" + sep + "main", Pattern.compile("jboss-marshalling-river-.*.jar"));
            addUrl(urlList, jboss, "remoting" + sep + "main", Pattern.compile("jboss-remoting-.*.jar"));
            addUrl(urlList, jboss, "sasl" + sep + "main", Pattern.compile("jboss-sasl-.*.jar"));
            addUrl(urlList, jboss, "threads" + sep + "main", Pattern.compile("jboss-threads-.*.jar"));
            addUrl(urlList, jboss, "xnio" + sep + "main", Pattern.compile("xnio-api-.*.jar"));
            addUrl(urlList, jboss, "xnio" + sep + "nio" + sep + "main", Pattern.compile("xnio-nio-.*.jar"));
            addUrl(urlList, as, "controller" + sep + "main", Pattern.compile("wildfly-controller-.*.jar"));
            addUrl(urlList, as, "controller" + sep + "main", Pattern.compile("jboss-as-controller-.*.jar"));
            addUrl(urlList, as, "controller-client" + sep + "main", Pattern.compile("jboss-as-controller-client-.*.jar"));
            addUrl(urlList, as, "controller-client" + sep + "main", Pattern.compile("wildfly-controller-client-.*.jar"));
            addUrl(urlList, as, "protocol" + sep + "main", Pattern.compile("wildfly-protocol-.*.jar"));
            addUrl(urlList, as, "protocol" + sep + "main", Pattern.compile("jboss-as-protocol-.*.jar"));
            //CLI GUI
//            addUrl(urlList, jboss, "aesh" + sep + "main", Pattern.compile("aesh-.*.jar"));
//            addUrl(urlList, jboss, "staxmapper" + sep + "main", Pattern.compile("staxmapper-.*.jar"));
//            addUrl(urlList, wildfly, "security" + sep + "manager" + sep + "main", Pattern.compile("wildfly-security-manager-.*.jar"));
//            addUrl(urlList, jboss, "remoting-jmx" + sep + "main", Pattern.compile("remoting-jmx-.*.jar"));
//            addUrl(urlList, jboss, "vfs" + sep + "main", Pattern.compile("jboss-vfs-.*.jar"));
//            addUrl(urlList, org, "picketbox" + sep + "main", Pattern.compile("picketbox-.*.jar"));
//            addUrl(urlList, as, "cli" + sep + "main", Pattern.compile("wildfly-cli-.*.jar"));
            File serverPath = new File(serverRoot);
            Version version = WildflyPluginUtils.getServerVersion(serverPath);
            if (WildflyPluginUtils.WILDFLY_10_0_0.compareToIgnoreUpdate(version) >= 0) {
                addUrl(urlList, wildfly, "common" + sep + "main", Pattern.compile("wildfly-common-.*.jar"));
            }
            boolean shouldPatchXnio = WildflyPluginUtils.WILDFLY_8_0_0.compareToIgnoreUpdate(version) <= 0;
            WildFlyClassLoader loader = new WildFlyClassLoader(urlList.toArray(new URL[] {}),
                    WildflyDeploymentFactory.class.getClassLoader(), shouldPatchXnio);
            return loader;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return null;
    }

    private static void addUrl(List<URL> result, File root, String path, final Pattern pattern) {
        File folder = new File(root, path);
        if(folder.exists() && folder.isDirectory()) {
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
                        LOGGER.log(Level.INFO, "Adding {0} to the classpath", child.getAbsolutePath());
                    } catch (MalformedURLException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
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
            throw new DeploymentManagerCreationException(NbBundle.getMessage(WildflyDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        synchronized (WildflyDeploymentFactory.class) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip != null) {
                WildflyDeploymentManager dm = managerCache.get(ip);
                if (dm != null) {
                    return dm;
                }
            }

            try {
                DeploymentFactory df = getFactory(uri);
                if (df == null) {
                    throw new DeploymentManagerCreationException(NbBundle.getMessage(WildflyDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N
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
                            + (ip != null ? ip.getProperty(WildflyPluginProperties.PROPERTY_HOST) : "localhost"); // NOI18N
                }
                WildflyDeploymentManager dm = new WildflyDeploymentManager(df, uri, jbURI, uname, passwd);
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
            throw new DeploymentManagerCreationException(NbBundle.getMessage(WildflyDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
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
                String root = ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR);
                if (root == null || !new File(root).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent server root " + root); // NOI18N
                }
                String server = ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR);
                if (server == null || !new File(server).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent domain root " + server); // NOI18N
                }
            }

            return new WildflyDeploymentManager(null, uri, null, null, null);
        } catch (NoClassDefFoundError e) {
            DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
            dmce.initCause(e);
            throw dmce;
        }
    }

    @Override
    public String getProductVersion() {
        return NbBundle.getMessage (WildflyDeploymentFactory.class, "LBL_JBossFactoryVersion");
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WildflyDeploymentFactory.class, "WILDFLY_SERVER_NAME"); // NOI18N
    }

    private DeploymentFactory getFactory(String instanceURL) {
        DeploymentFactory jbossFactory = null;
        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(instanceURL);
            synchronized (WildflyDeploymentFactory.class) {
                if (ip != null) {
                    jbossFactory = (DeploymentFactory) factoryCache.get(ip);
                }
               if (jbossFactory == null) {
                    jbossFactory = this;
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

}

