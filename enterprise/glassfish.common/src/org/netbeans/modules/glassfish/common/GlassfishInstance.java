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

package org.netbeans.modules.glassfish.common;

import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.data.GlassFishAdminInterface;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.nodes.Hk2InstanceNode;
import org.netbeans.modules.glassfish.common.parser.DomainXMLChangeListener;
import org.netbeans.modules.glassfish.common.ui.GlassFishPropertiesCustomizer;
import org.netbeans.modules.glassfish.common.ui.WarnPanel;
import org.netbeans.modules.glassfish.common.utils.Util;
import org.netbeans.modules.glassfish.spi.*;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.InputOutput;

/**
 * GlassFish server instance.
 * <p/>
 * Represents any GlassFish server registered in NetBeans.
 * <p/>
 * @author Peter Williams, Vince Kraemer, Tomas Kraus
 */
public class GlassfishInstance implements ServerInstanceImplementation,
        Lookup.Provider, LookupListener, GlassFishServer {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Properties map used to store GlassFish server properties in GlassFish
     * server instance.
     */
    public class Props implements Map<String, String> {

        private final Map<String, String> delegate;

        /**
         * Constructs a new properties map with the same mappings as the
         * specified <code>Map</code>.
         * <p/>
         * The <code>Props</code> is created with default load factor (0.75)
         * and an initial capacity sufficient to hold the mappings in the
         * specified <code>Map</code>.
         * <p/>
         * @param map Properties <code>Map</code> whose mappings are to be
         *            placed in this map.
         * @throws NullPointerException if the specified map is null.
         */
        public Props(Map<String, String> map) {
            if (map == null) {
                throw new NullPointerException("Source Map shall not be null.");
            }
            this.delegate = map;
        }

        @Override
        public Collection<String> values() {
            synchronized(delegate) {
               return Collections.<String>unmodifiableCollection(
                       delegate.values());
            }
        }

        @Override
        public Set<String> keySet() {
            synchronized(delegate) {
                return Collections.<String>unmodifiableSet(delegate.keySet());
            }
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            synchronized(delegate) {
                return Collections.<Entry<String, String>>unmodifiableSet(
                        delegate.entrySet());
            }
        }

        @Override
        public int size() {
            synchronized(delegate) {return delegate.size();}
        }

        @Override
        public String remove(Object key) {
            synchronized(delegate) {return delegate.remove(key);}
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            synchronized(delegate) {delegate.putAll(m);}
        }

        @Override
        public String put(String key, String value) {
            if (GlassfishModule.PASSWORD_ATTR.equals(key)) {
                String serverName = get(GlassfishModule.DISPLAY_NAME_ATTR);
                String userName = get(GlassfishModule.USERNAME_ATTR);
                if (serverName != null && userName != null) {
                    Keyring.save(GlassfishInstance.passwordKey(
                            serverName, userName), value.toCharArray(),
                            "GlassFish administrator user password");
                }
            }
            synchronized(delegate) {return delegate.put(key, value);}
        }


        @Override
        public boolean isEmpty() {
            synchronized(delegate) {return delegate.isEmpty();}
        }

        @Override
        public int hashCode() {
            synchronized(delegate) {return delegate.hashCode();}
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
	        return true;
            if (o instanceof Map)
                synchronized(delegate) {return delegate.equals(o);}
            else
                return false;
        }

        @Override
        public boolean containsValue(Object value) {
            synchronized(delegate) {return delegate.containsValue(value);}
        }

        @Override
        public boolean containsKey(Object key) {
            synchronized(delegate) {return delegate.containsKey(key);}
        }

        @Override
        public void clear() {
            synchronized(delegate) {delegate.clear();}
        }
        
        /**
         * Returns the property value to which the specified property key
         * is mapped, or <code>null</code> if this map contains no mapping for
         * the key.
         * <p/>
         * @param key Property key used to search for mapped property value.
         * @return Property value mapped to specified property key
         *         or <code>null</code> if no such property value exists.
         */
        @Override
        public String get(Object key) {
            if (GlassfishModule.PASSWORD_ATTR.equals(key)) {
                String value;
                synchronized(delegate) {
                    value = delegate.get(key);
                }
                if (value == null) {
                    value = getPasswordFromKeyring(
                            delegate.get(GlassfishModule.DISPLAY_NAME_ATTR),
                            delegate.get(GlassfishModule.USERNAME_ATTR));
                    synchronized(delegate) {
                        delegate.put((String)key, value);
                    }
                }
                return value;
            } else {
                synchronized(delegate) {return delegate.get(key);}
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassfishInstance.class);

    // Reasonable default values for various server parameters.  Note, don't use
    // these unless the server's actual setting cannot be determined in any way.
    public static final String DEFAULT_HOST_NAME = "localhost"; // NOI18N
    public static final String DEFAULT_ADMIN_NAME = "admin"; // NOI18N
    public static final String DEFAULT_ADMIN_PASSWORD = ""; // NOI18N
    /** Administrator's password being used in old NetBeans. */
    public static final String OLD_DEFAULT_ADMIN_PASSWORD = "adminadmin"; // NOI18N
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_HTTPS_PORT = 8181;
    public static final int DEFAULT_ADMIN_PORT = 4848;
    public static final int DEFAULT_DEBUG_PORT = 9009;
    static final int LOWEST_USER_PORT
            = org.openide.util.Utilities.isWindows() ? 1 : 1025;
    public static final String DEFAULT_DOMAINS_FOLDER = "domains"; //NOI18N
    public static final String DEFAULT_DOMAIN_NAME = "domain1"; // NOI18N

    static final String INSTANCE_FO_ATTR = "InstanceFOPath"; // NOI18N

    /** GlassFish user account instance key ring name space. */
    static final String KEYRING_NAME_SPACE="GlassFish.admin";
    
    /**
     * GlassFish user account instance key ring field separator.
     * <p/>
     * Key ring name is constructed in following form:
     * <field>{'.'<field>}':'<identifier>
     * e.g. "GlassFish.cloud.userAccount.userPassword:someUser".
     */
    static final String KEYRING_NAME_SEPARATOR=".";

    /**
     * GlassFish user account instance key ring identifier separator.
     * <p/>
     * Key ring name is constructed in following form:
     * <field>{'.'<field>}':'<identifier>
     * e.g. "GlassFish.cloud.userAccount.userPassword:someUser".
     */
    static final String KEYRING_IDENT_SEPARATOR=":";
    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build key ring identifier for password related to given user name.
     * <p/>
     * @param serverName Name of server to add into password key.
     * @param userName User name of account user who's password will be stored.
     * @return Key ring identifier for password related to given user name
     */
    public static String passwordKey(String serverName, String userName) {
        StringBuilder pwKey = new StringBuilder(
                KEYRING_NAME_SPACE.length() + KEYRING_NAME_SEPARATOR.length()
                + GlassfishModule.PASSWORD_ATTR.length()
                + KEYRING_IDENT_SEPARATOR.length()
                + (serverName != null ? serverName.length() : 0)
                + KEYRING_IDENT_SEPARATOR.length()
                + (userName != null ? userName.length() : 0));
        pwKey.append(KEYRING_NAME_SPACE);
        pwKey.append(KEYRING_NAME_SEPARATOR);
        pwKey.append(GlassfishModule.PASSWORD_ATTR);
        pwKey.append(KEYRING_IDENT_SEPARATOR);
        pwKey.append(serverName != null ? serverName : "");
        pwKey.append(KEYRING_IDENT_SEPARATOR);
        pwKey.append(userName != null ? userName : "");
        return pwKey.toString();
    }

    /**
     * Retrieve password stored in {@see Keyring}.
     * <p/>
     * @param serverName Name of server to add into password key.
     * @param userName User name of account user who's password will be read.
     * @return 
     */
    public static String getPasswordFromKeyring(final String serverName,
            final String userName) {
        char[] passwordChars = Keyring.read(
                GlassfishInstance.passwordKey(serverName, userName));
        String value = passwordChars != null
                ? new String(passwordChars)
                : DEFAULT_ADMIN_PASSWORD;
        return value;
    }

    /**
     * Find all modules that have NetBeans support, add them to
     * <code>GlassfishInstance<code> local lookup if server supports them.
     * <p/>
     * @param instance <code>GlassfishInstance<code> local lookup to be updated.
     */
    static void updateModuleSupport(GlassfishInstance instance) {
        instance.updateFactories();
        instance.lookupResult.addLookupListener(instance);
    }

    /**
     * Mark this GlassFish server URL as under construction.
     * <p/>
     * Will do nothing if provided <code>url</code> argument
     * is <code>null</code>.
     * <p/>
     * @param url GlassFish server URL
     */
    private static void tagUnderConstruction(String url) {
        if (url != null) {
            GlassfishInstanceProvider.activeRegistrationSet.add(url);
        }
    }
    
    /**
     * Remove under construction URL mark from this GlassFish server.
     * <p/>
     * Will do nothing if provided <code>url</code> argument
     * is <code>null</code>.
     * <p/>
     * @param url GlassFish server URL
     */
    private static void untagUnderConstruction(String url) {
        if (url != null) {
            GlassfishInstanceProvider.activeRegistrationSet.remove(url);
        }
    }

    /** 
     * Creates a GlassfishInstance object for a server installation.  This
     * instance should be added to the the provider registry if the caller wants
     * it to be persisted for future sessions or searchable.
     * <p/>
     * @param displayName Display name for this server instance.
     * @param installRoot GlassFish installation root directory.
     * @param glassfishRoot GlassFish server home directory.
     * @param domainsDir GlassFish server domains directory.
     * @param domainName GlassFish server registered domain name.
     * @param httpPort GlassFish server HTTP port for applications.
     * @param adminPort GlassFish server HTTP port for administration.
     * @param userName GlassFish server administrator's user name.
     * @param password GlassFish server administrator's password.
     * @param url GlassFish server URL (Java EE SPI unique identifier).
     * @param gip GlassFish instance provider.
     * @return GlassfishInstance object for this server instance.
     */
    public static GlassfishInstance create(String displayName,
            String installRoot, String glassfishRoot, String domainsDir,
            String domainName, int httpPort, int adminPort,
            String userName, String password, String target, String url,
            GlassfishInstanceProvider gip) {
        Map<String, String> ip = new HashMap<>();
        ip.put(GlassfishModule.DISPLAY_NAME_ATTR, displayName);
        ip.put(GlassfishModule.INSTALL_FOLDER_ATTR, installRoot);
        ip.put(GlassfishModule.GLASSFISH_FOLDER_ATTR, glassfishRoot);
        ip.put(GlassfishModule.DOMAINS_FOLDER_ATTR, domainsDir);
        ip.put(GlassfishModule.DOMAIN_NAME_ATTR, domainName);
        ip.put(GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
        ip.put(GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
        ip.put(GlassfishModule.TARGET_ATTR, target);
        ip.put(GlassfishModule.USERNAME_ATTR,
                userName != null
                ? userName : DEFAULT_ADMIN_NAME);
        if (password != null) {
            ip.put(GlassfishModule.PASSWORD_ATTR, password);
        }
        ip.put(GlassfishModule.URL_ATTR, url);
        // extract the host from the URL
        String[] bigUrlParts = url.split("]");
        if (null != bigUrlParts && bigUrlParts.length > 1) {
            String[] urlParts = bigUrlParts[1].split(":"); // NOI18N
            if (null != urlParts && urlParts.length > 2) {
                ip.put(GlassfishModule.HOSTNAME_ATTR, urlParts[2]);
                ip.put(GlassfishModule.HTTPHOST_ATTR, urlParts[2]);
            }
        }
        return create(ip, gip, true);
    }

    /**
     * Constructs an instance of GlassFish instance data object and initializes
     * it.
     * <p/>
     * @param ip GlassFish instance properties.
     * @param gip GlassFish instance provider.
     * @param updateNow Trigger lookup update.
     * @return Initialized GlassFish instance data object.
     */
    public static GlassfishInstance create(Map<String, String> ip,
            GlassfishInstanceProvider gip, boolean updateNow) {
        String deployerUri = ip.get(GlassfishModule.URL_ATTR);
        GlassfishInstance instance = null;
        GlassFishVersion version = ServerUtils.getServerVersion(
                ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR));
        try {
            instance = new GlassfishInstance(ip, version, gip, updateNow);
            tagUnderConstruction(deployerUri);
            if (!instance.isPublicAccess()) {
                instance.ic.add(instance.commonSupport);
                instance.allowPublicAccess();
            }
            if (updateNow) {
                updateModuleSupport(instance);
            }
        } finally {
            untagUnderConstruction(deployerUri);
        }
        LOGGER.log(Level.INFO,
                "Created GlassFish Server {0} instance with name {1}",
                new String[] {version != null ? version.toString() : "null",
                    instance != null ? instance.getName() : "null"});
        return instance;
    }

    /**
     * Constructs an instance of GlassFish instance data object and initializes
     * it.
     * <p/>
     * Lookup update will be done by default.
     * <p/>
     * @param ip GlassFish instance properties.
     * @param gip GlassFish instance provider.
     * @param updateNow Trigger lookup update.
     * @return Initialized GlassFish instance data object.
     */
    public static GlassfishInstance create(Map<String, String> ip,
            GlassfishInstanceProvider gip) {
        return create(ip, gip, true);
    }
    
    /**
     * Add new <code>String</code> storedValue into <code>Map</code> when storedValue
     * with specified key does not exist.
     * <p/>
     * @param map   Map to be checked and updated.
     * @param key   Key used to search for already existing storedValue.
     * @param value Value to be added when nothing is found.
     * @return Value stored in <code>Map</code> or <code>value</code> argument
     *         when no value was stored int the <code>Map</code>
     */
    private static String updateString(Map<String, String> map, String key,
            String value) {
        String result = map.get(key);
        if(result == null) {
            map.put(key, value);
            result = value;
        }
        return result;
    }

    /**
     * Add new <code>Integer</code> storedValue into <code>Map</code> when storedValue
     * with specified key does not exist.
     * <p/>
     * @param map   Map to be checked and updated.
     * @param key   Key used to search for already existing storedValue.
     * @param value Value to be added when nothing is found.
     * @return Value stored in <code>Map</code> or <code>value</code> argument
     *         when no value was stored int the <code>Map</code>
     */
    private static int updateInt(Map<String, String> map, String key, int value) {
        int result;
        String storedValue = map.get(key);
        try {
            // Throws NumberFormatException also when storedValue is null.
            result = Integer.parseInt(storedValue);
        } catch(NumberFormatException ex) {
            map.put(key, Integer.toString(value));
            result = value;
        }
        return result;
    }

    /**
     * Check if given GlassFish instance properties keys should be filtered
     * when persisting GlassFish instance.
     * <p/>
     * @param key GlassFish instance properties key to be checked.
     * @return Value of <code>true</code> when property with given key shall not
     *         be persisted or <code>false</code> otherwise.
     */

    private static boolean filterKey(String key) {
        return INSTANCE_FO_ATTR.equals(key);
    }

    /**
     * Fix attributes being imported from old NetBeans.
     * <p/>
     * Password for local server is changed from <code>"adminadmin"</code>
     * to <code>""</code>.
     * Fixed attributes are marked with new property to avoid multiple fixes
     * in the future.
     * <p/>
     * Argument <code>ip</code> shall not be <code>null</code>.
     * <p/>
     * @param ip Instance properties <code>Map</code>.
     * @param fo Instance file object.
     */
    private static void fixImportedAttributes(Map<String, String> ip,
            FileObject fo) {
        if (!ip.containsKey(GlassfishModule.NB73_IMPORT_FIXED)) {
            String password = ip.get(GlassfishModule.PASSWORD_ATTR);
            if (password != null) {
                boolean local
                        = ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) != null;
                if (local && GlassfishInstance.OLD_DEFAULT_ADMIN_PASSWORD
                        .equals(password)) {
                    if (GlassfishInstance.DEFAULT_ADMIN_PASSWORD == null
                            || GlassfishInstance
                            .DEFAULT_ADMIN_PASSWORD.length() == 0) {
                        ip.remove(GlassfishModule.PASSWORD_ATTR);
                    } else {
                        ip.put(GlassfishModule.PASSWORD_ATTR,
                                GlassfishInstance.DEFAULT_ADMIN_PASSWORD);
                    }
                    org.netbeans.modules.glassfish.common.utils.ServerUtils
                            .setStringAttribute(fo,
                            GlassfishModule.PASSWORD_ATTR,
                            GlassfishInstance.DEFAULT_ADMIN_PASSWORD);
                }
            }
            ip.put(GlassfishModule.NB73_IMPORT_FIXED, Boolean.toString(true));
        }
    }

    // Password from keyring (GlassfishModule.PASSWORD_ATTR) is read on demand
    // using code in GlassfishInstance.Props class.
    /**
     * Read GlassFish server instance data from persistent storage.
     * <p/>
     * @param instanceFO  Persistent storage file.
     * @param uriFragment GlassFish server URI fragment.
     * @return GlassFish server instance reconstructed from persistent storage.
     * @throws IOException 
     */
    public static GlassfishInstance readInstanceFromFile(
            FileObject instanceFO, boolean autoregistered) throws IOException {
        GlassfishInstance instance = null;

        String installRoot
                = org.netbeans.modules.glassfish.common.utils.ServerUtils
                .getStringAttribute(instanceFO,
                GlassfishModule.INSTALL_FOLDER_ATTR);
        String glassfishRoot
                = org.netbeans.modules.glassfish.common.utils.ServerUtils
                .getStringAttribute(instanceFO,
                GlassfishModule.GLASSFISH_FOLDER_ATTR);
        
        // Existing installs may lack "installRoot", but glassfishRoot and 
        // installRoot are the same in that case.
        if(installRoot == null) {
            installRoot = glassfishRoot;
        }
        // TODO: Implement better folders content validation.
        if(org.netbeans.modules.glassfish.common.utils.ServerUtils
                .isValidFolder(installRoot)
                && org.netbeans.modules.glassfish.common.utils.ServerUtils
                .isValidFolder(glassfishRoot)) {
            // collect attributes and pass to create()
            Map<String, String> ip = new HashMap<>();
            Enumeration<String> iter = instanceFO.getAttributes();
            while(iter.hasMoreElements()) {
                String name = iter.nextElement();
                String value
                        = org.netbeans.modules.glassfish.common.utils
                        .ServerUtils.getStringAttribute(instanceFO, name);
                ip.put(name, value);
            }
            ip.put(INSTANCE_FO_ATTR, instanceFO.getName());
            fixImportedAttributes(ip, instanceFO);
            instance = create(ip,GlassfishInstanceProvider.getProvider(), autoregistered);
        } else {
            LOGGER.log(Level.FINER,
                    "GlassFish folder {0} is not a valid install.",
                    instanceFO.getPath()); // NOI18N
            instanceFO.delete();
        }
        return instance;
    }

    /**
     * Write GlassFish server instance data into persistent storage.
     * <p/>
     * @param instance GlassFish server instance to be written.
     * @throws IOException 
     */
    public static void writeInstanceToFile(
            GlassfishInstance instance) throws IOException {
        String glassfishRoot = instance.getGlassfishRoot();
        if(glassfishRoot == null) {
            LOGGER.log(Level.SEVERE,
                    NbBundle.getMessage(GlassfishInstanceProvider.class,
                    "MSG_NullServerFolder")); // NOI18N
            return;
        }

        String url = instance.getDeployerUri();

        // For GFV3 managed instance files
        {
            FileObject dir
                    = org.netbeans.modules.glassfish.common.utils.ServerUtils
                    .getRepositoryDir(GlassfishInstanceProvider.getProvider()
                    .getInstancesDirFirstName(), true);
            FileObject[] instanceFOs = dir.getChildren();
            FileObject instanceFO = null;

            for (int i = 0; i < instanceFOs.length; i++) {
                if (url.equals(instanceFOs[i].getAttribute(GlassfishModule.URL_ATTR))
                        && !instanceFOs[i].getName().startsWith(GlassfishInstanceProvider.GLASSFISH_AUTOREGISTERED_INSTANCE)) {
                    instanceFO = instanceFOs[i];
                }
            }

            if(instanceFO == null) {
                String name = FileUtil.findFreeFileName(dir, "instance", null); // NOI18N
                instanceFO = dir.createData(name);
            }

            Map<String, String> attrMap = instance.getProperties();
            for(Map.Entry<String, String> entry: attrMap.entrySet()) {
                String key = entry.getKey();
                if(!filterKey(key)) {
                    Object currentValue = instanceFO.getAttribute(key);
                    if (null != currentValue && currentValue.equals(entry.getValue())) {
                        // do nothing
                    } else {
                        if (key.equals(GlassfishModule.PASSWORD_ATTR)) {
                            String serverName = attrMap.get(GlassfishModule.DISPLAY_NAME_ATTR);
                            String userName = attrMap.get(GlassfishModule.USERNAME_ATTR);
                            Keyring.save(GlassfishInstance.passwordKey(
                                    serverName, userName),
                                    entry.getValue().toCharArray(),
                                    "GlassFish administrator user password");
                            LOGGER.log(Level.FINEST,
                                    "{0} attribute stored in keyring: {1}",
                                    new String[] {instance.getDisplayName(),
                                        key});
                        } else {
                            instanceFO.setAttribute(key, entry.getValue());
                            LOGGER.log(Level.FINEST,
                                    "{0} attribute stored: {1} = {2}",
                                    new String[] {instance.getDisplayName(),
                                        key, entry.getValue()});
                        }
                    }
                }
            }
            // Remove FO attributes that are no more stored in server instance.
            for (Enumeration<String> foAttrs = instanceFO.getAttributes(); foAttrs.hasMoreElements(); ) {
                String foAttr = foAttrs.nextElement();
                if (!attrMap.containsKey(foAttr)) {
                    instanceFO.setAttribute(foAttr, null);
                    LOGGER.log(Level.FINEST,
                            "{0} attribute deleted: {1}",
                            new String[]{instance.getDisplayName(),
                        foAttr});
                }
            }
            
            instance.putProperty(INSTANCE_FO_ATTR, instanceFO.getName());
            instance.getCommonSupport().setFileObject(instanceFO);
        }
    }   

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    // Server properties
    private boolean removable = true;
    
    /** GlassFish server properties. */
    private transient Map<String, String> properties;

    /** GlassFish server version.
      * <p/>
      * This is always version of local GlassFish related to JavaEE platform,
      * even when registered domain is remote. */
    private final GlassFishVersion version;

    /** Process information of local running server started from IDE.
     *  <p/>
     *  This value shall be <code>null</code> when there is no server started
     *  from IDE running. But <code>null</code> <i>does not mean</i> that server
     *  is not running at all. */
    private transient volatile Process process;

    /** Configuration changes listener watching <code>domain.xml</code> file. */
    private final transient DomainXMLChangeListener domainXMLListener;

    private transient InstanceContent ic;
    private transient Lookup localLookup;
    private transient Lookup full;
    private final transient Lookup.Result<GlassfishModuleFactory>
            lookupResult = Lookups.forPath(Util.GF_LOOKUP_PATH).lookupResult(
            GlassfishModuleFactory.class);
    private transient Collection<? extends GlassfishModuleFactory>
            currentFactories = Collections.emptyList();
    
    /** GlassFish server support API for this instance. */
    private final transient CommonServerSupport commonSupport;
    // API instance
    private ServerInstance commonInstance;
    private GlassfishInstanceProvider instanceProvider;

    // GuardedBy("lookupResult")
    private Node fullNode;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("LeakingThisInConstructor")
    private GlassfishInstance(Map<String, String> ip, GlassFishVersion version,
            GlassfishInstanceProvider instanceProvider, boolean prepareProperties) {
        this.version = version;
        this.process = null;
        ic = new InstanceContent();
        localLookup = new AbstractLookup(ic);
        full = localLookup;
        this.instanceProvider = instanceProvider;
        String domainDirPath = ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        String domainName = ip.get(GlassfishModule.DOMAIN_NAME_ATTR);
        if (null != domainDirPath && null != domainName) {
            File domainDir = new File(domainDirPath,domainName);
            PortCollection pc = new PortCollection();
            if (Util.readServerConfiguration(domainDir, pc)) {
                ip.put(GlassfishModule.ADMINPORT_ATTR,
                        Integer.toString(pc.getAdminPort()));
                ip.put(GlassfishModule.HTTPPORT_ATTR,
                        Integer.toString(pc.getHttpPort()));
            }
            domainXMLListener = new DomainXMLChangeListener(this, 
                    ServerUtils.getDomainConfigFile(domainDirPath, domainName));
        } else {
            domainXMLListener = null;
        }
        if (prepareProperties) {
            this.properties = prepareProperties(ip);
        } else {
            this.properties = new Props(ip);
        }
        if (!isPublicAccess()) {
            // Add this instance into local lookup (to find instance from
            // node lookup).
            ic.add(this); 
            commonInstance = ServerInstanceFactory.createServerInstance(this);
        }
        // Warn when creating instance of unknown version.
        if (this.version == null) {
            String installroot = ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
            String displayName = ip.get(GlassfishModule.DISPLAY_NAME_ATTR);
            WarnPanel.gfUnknownVersionWarning(displayName, installroot);
            LOGGER.log(Level.INFO, NbBundle.getMessage(
                    GlassfishInstance.class,
                    "GlassfishInstance.init.versionNull",
                    new String[] {displayName, installroot}));
        }
        this.commonSupport = new CommonServerSupport(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish properties.
     * <p/>
     * @return GlassFish properties.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Set GlassFish properties.
     * <p/>
     * @param properties GlassFish properties to set
     */
    public void setProperties(final Props properties) {
        this.properties = properties;
    }

    /**
     * Get local <code>Lookup</code> object.
     * <P/>
     * Allow to access local lookup object in server support objects (e.g. 
     * <code>CommonServerSupport</code>).
     * <p/>
     * @return Local <code>Lookup</code> object.
     */
    Lookup localLookup() {
        return localLookup;
    }

    /**
     * Get local instance provider object.
     * <p/>
     * Allow to access local instance provider object in server support objects
     * (e.g. <code>CommonServerSupport</code>).
     * <p/>
     * @return Local instance provider object.
     */
    public GlassfishInstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    /**
     * Get process information of local running server started from IDE.
     * <p/>
     * @return Process information of local running server started from IDE.
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get GlassFish configuration file <code>domain.xml</code> changes
     * listener.
     * <p/>
     * @return GlassFish configuration file <code>domain.xml</code> changes
     *         listener. 
     */
    public DomainXMLChangeListener getDomainXMLChangeListener() {
        return domainXMLListener;
    }

    /**
     * Set process information of local running server started from IDE.
     * <p/>
     * @param process Process information of local running server started
     *                from IDE.
     */
    public void setProcess(final Process process) {
        this.process = process;
    }

    /**
     * Reset process information of local running server started from IDE.
     * <p/>
     * Value of process information of local running server started from IDE
     * is set to <code>null</code>.
     */
    public void resetProcess() {
        this.process = null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Fake Getters from GlassFishServer interface                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish display name stored properties.
     * <p/>
     * @return GlassFish display name.
     */
    @Override
    public String getName() {
        return properties.get(GlassfishModule.DISPLAY_NAME_ATTR);
    }

    /**
     * Get GlassFish server host from stored properties.
     * <p/>
     * @return lassFish server host.
     */
    @Override
    public String getHost() {
        return properties.get(GlassfishModule.HOSTNAME_ATTR);
    }

    /**
     * Set GlassFish server host from stored properties.
     * <p/>
     * @param host GlassFish server host  to be stored.
     */
    public void setHost(final String host) {
        properties.put(GlassfishModule.HOSTNAME_ATTR, host);
    }

    /**
     * Get GlassFish server HTTP port from stored properties.
     * <p/>
     * @return GlassFish server HTTP port.
     */
    @Override
    public int getPort() {
        return intProperty(GlassfishModule.HTTPPORT_ATTR);
    }

    /**
     * Set GlassFish server HTTP port in stored properties.
     * <p/>
     * @param httpPort GlassFish server HTTP port to be stored.
     * @throws NumberFormatException if <code>httpPort</code> does not contain
     *                               valid integer value.
     */
    public void setHttpPort(String httpPort) {
        Integer.parseInt(httpPort);
        properties.put(GlassfishModule.HTTPPORT_ATTR, httpPort);
    }

    /**
     * Set GlassFish server HTTP port in stored properties.
     * <p/>
     * @param httpPort GlassFish server HTTP port to be stored.
     */
    public void setHttpPort(int httpPort) {
        properties.put(
                GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
    }

    /**
     * Get GlassFish server administration port from stored properties.
     * <p/>
     * @return GlassFish server administration port.
     */
    @Override
    public int getAdminPort() {
        return intProperty(GlassfishModule.ADMINPORT_ATTR);
    }

    /**
     * Set GlassFish server administration port in stored properties.
     * <p/>
     * @param adminPort GlassFish server administration port to be stored.
     * @throws NumberFormatException if <code>httadminPortpPort</code> does not contain
     *                               valid integer value.
     */
    public void setAdminPort(String adminPort) {
        properties.put(GlassfishModule.ADMINPORT_ATTR, adminPort);
    }

    /**
     * Set GlassFish server administration port in stored properties.
     * <p/>
     * @param adminPort GlassFish server administration port to be stored.
     */
    public void setAdminPort(int adminPort) {
        properties.put(
                GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
    }

    /**
     * Get GlassFish server administration user name from stored properties.
     * <p/>
     * @return GlassFish server administration user name.
     */
    @Override
    public String getAdminUser() {
        return properties.get(GlassfishModule.USERNAME_ATTR);
    }

    /**
     * Get GlassFish server administration user's password from
     * stored properties.
     * <p/>
     * @return GlassFish server administration user's password.
     */
    @Override
    public String getAdminPassword() {
        return properties.get(GlassfishModule.PASSWORD_ATTR);
    }

    /**
     * Get GlassFish server domains folder from stored properties.
     * <p/>
     * @return GlassFish server domains folder.
     */
    @Override
    public String getDomainsFolder() {
        return properties.get(GlassfishModule.DOMAINS_FOLDER_ATTR);
    }

    /**
     * Set GlassFish server domains folder into stored properties.
     * <p/>
     * @param domainsFolder GlassFish server domains folder.
     * @return Previous value of domains folder or <code>null</code> if there
     *         was no value of domains folder stored.
     */
    public String setDomainsFolder(String domainsFolder) {
        return properties.put(GlassfishModule.DOMAINS_FOLDER_ATTR,
                domainsFolder);
    }

    /**
     * Set GlassFish server domain name from stored properties.
     * <p/>
     * @param domainsFolder GlassFish server domain name.
     */
    @Override
    public String getDomainName() {
        return properties.get(GlassfishModule.DOMAIN_NAME_ATTR);
    }

    /**
     * Get GlassFish server target in domain (cluster or standalone
     * server name).
     * <p/>
     * @return  GlassFish server target in domain (cluster or standalone
     *          server name).
     */
    public String getTarget() {
        return properties.get(GlassfishModule.TARGET_ATTR);
    }

    /**
     * Set GlassFish server target in domain (cluster or standalone
     * server name).
     * <p/>
     * @param target GlassFish server target in domain (cluster or standalone
     *               server name).
     * @return Previous value of target or <code>null</code> if there
     *         was no value of domains folder stored.
     */
    public String setTarget(final String target) {
        return properties.put(GlassfishModule.TARGET_ATTR, target);
    }

    /**
     * Get GlassFish server URL from stored properties.
     * <p/>
     * @return GlassFish server URL.
     */
    @Override
    public String getUrl() {
        return properties.get(GlassfishModule.URL_ATTR);
    }

    /**
     * Get GlassFish server home.
     * <p/>
     * @return Server home.
     */
    @Override
    public String getServerHome() {
        return properties.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
    }

    /**
     * Get GlassFish server installation root.
     * <p/>
     * @return Server installation root.
     */
    @Override
    public String getServerRoot() {
        return properties.get(GlassfishModule.INSTALL_FOLDER_ATTR);
    }

    /**
     * Get GlassFish server version.
     * <p/>
     * This is always version of local GlassFish related to JavaEE platform,
     * even when registered domain is remote.
     * <p/>
     * @return GlassFish server version or <code>null</code> when version is
     *         not known.
     */
    @Override
    public GlassFishVersion getVersion() {
        return version;
    }

    /**
     * Get GlassFish server administration interface type.
     * <p/>
     * @return GlassFish server administration interface type.
     */
    @Override
    public GlassFishAdminInterface getAdminInterface() {
//        if (version.ordinal() < GlassFishVersion.GF_4.ordinal())
            return GlassFishAdminInterface.HTTP;
//        else
//            return GlassFishAdminInterface.REST;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Fake Getters and Setters                                               //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get information if this GlassFish server instance is local or remote.
     * <p/>
     * Local GlassFish server instance has domains folder attribute set while
     * remote does not.
     * <p/>
     * @return Value of <code>true</code> when this GlassFish server instance
     *         is remote or <code>false</code> otherwise.
     */
    @Override
    public boolean isRemote() {
        return properties.get(GlassfishModule.DOMAINS_FOLDER_ATTR) == null;
    }

    /**
     * Returns property value to which the specified <code>key</code> is mapped,
     * or <code>null</code> if this map contains no mapping for the
     * <code>key</code>.
     * <p/>
     * @param key GlassFish property <code>key</code>.
     * @return GlassFish property or <code>null</code> if no value with
     *         given <code>key</code> is stored.
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Associates the specified <code>value</code> with the specified
     * <code>key</code> in this map.
     * <p/>
     * If the map previously contained a mapping for the key, the old value
     * is replaced by the specified value.
     * <p/>
     * @param key   GlassFish property <code>key</code>.
     * @param value GlassFish property <code>value</code>.
     * @return Previous value associated with <code>key</code>, or
     *         <code>null</code> if there was no mapping for <code>key</code>.
     */
    public String putProperty(String key, String value) {
        return properties.put(key, value);
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     * <p/>
     * @param key GlassFish property <code>key</code>.
     * @return Previous value associated with <code>key</code>, or
     *         <code>null</code> if there was no mapping for <code>key</code>.
     */
    public String removeProperty(String key) {
        return properties.remove(key);
    }

    /**
     * Get GlassFish server port from stored properties.
     * <p/>
     * @return GlassFish server port as <code>String</code>.
     */
    public String getHttpPort() {
        return properties.get(GlassfishModule.HTTPPORT_ATTR);
    }

    /**
     * Get GlassFish server administration port from stored properties.
     * <p/>
     * @return GlassFish server administration port as <code>String</code>.
     */
    public String getHttpAdminPort() {
        return properties.get(GlassfishModule.ADMINPORT_ATTR);
    }

    /**
     * Set GlassFish server administration user name from stored properties.
     * <p/>
     * Method {@see #writeInstanceToFile(GlassfishInstance)} must be called
     * to persist value.
     * <p/>
     * @param usern GlassFish server administration user name.
     */
    public void setAdminUser(final String user) {
        properties.put(GlassfishModule.USERNAME_ATTR, user);
    }

    /**
     * Store password attribute into GlassFish instance properties.
     * <p/>
     * Password is not stored in {@see Keyring}. Method
     * {@see #writeInstanceToFile(GlassfishInstance)} must be called to persist
     * value.
     * <p/>
     * @param password Password attribute to store.
     */
    public void setAdminPassword(String password) {
        properties.put(GlassfishModule.PASSWORD_ATTR, password);
    }

    /**
     * Retrieve password attribute from stored properties and NetBeans
     * key store.
     * <p/>
     * @return Retrieved password attribute.
     */
    public String getPassword() {
        return properties.get(GlassfishModule.PASSWORD_ATTR);
    }

    public String getInstallRoot() {
        return properties.get(GlassfishModule.INSTALL_FOLDER_ATTR);
    }

    public String getGlassfishRoot() {
        return properties.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
    }

    @Override
    public String getDisplayName() {
        return properties.get(GlassfishModule.DISPLAY_NAME_ATTR);
    }

    public String getDeployerUri() {
        return properties.get(GlassfishModule.URL_ATTR);
    }

    public String getUserName() {
        return properties.get(GlassfishModule.USERNAME_ATTR);
    }

    /**
     * Returns Java SE platform home configured for GlassFfish server.
     * <p/>
     * @return Java SE platform configured for GlassFfish server or null
     *         if no such platform was configured.
     */
    public String getJavaHome() {
        return properties.get(GlassfishModule.JAVA_PLATFORM_ATTR);
    }

    /**
     * Sets Java SE platform home configured for GlassFfish server.
     * <p/>
     * Java SE platform home value is cleared when <code>javahome</code>
     * is <code>null</code>.
     * <p/>
     * @param javahome Java SE platform home to be set for GlassFfish server.
     */
    public void setJavaHome(String javahome) {
        if (javahome != null)
            properties.put(GlassfishModule.JAVA_PLATFORM_ATTR, javahome);
        else
            properties.remove(GlassfishModule.JAVA_PLATFORM_ATTR);
    }

    /**
     * Return server JVM mode as <code>String</code> value.
     * <p/>
     * @return Server JVM mode.
     */
    public String getJvmModeAsString() {
        return properties.get(GlassfishModule.JVM_MODE);
    }

    /**
     * Return server JVM mode.
     * <p/>
     * @return Server JVM mode.
     */
    public GlassFishJvmMode getJvmMode() {
        return GlassFishJvmMode.toValue(
                properties.get(GlassfishModule.JVM_MODE));
    }

    /**
     * Return server debug port to be used to attach debugger.
     * <p/>
     * Value of <code>GlassfishModule.USE_SHARED_MEM_ATTR</code> is changed
     * to false.
     * <p/>
     * @return Server debug port.
     */
    public int getDebugPort() {
        int debugPort;
        try {
            debugPort = Integer.parseInt(
                    getProperty(GlassfishModule.DEBUG_PORT));
            if (debugPort < LOWEST_USER_PORT || debugPort > 65535) {
                putProperty(GlassfishModule.DEBUG_PORT,
                        Integer.toString(DEFAULT_DEBUG_PORT));
                debugPort = DEFAULT_DEBUG_PORT;
                LOGGER.log(Level.INFO, "Converted debug port to {0} for {1}",
                        new String[] {Integer.toString(DEFAULT_DEBUG_PORT),
                            getDisplayName()});
            }
        } catch (NumberFormatException nfe) {
            putProperty(GlassfishModule.DEBUG_PORT,
                    Integer.toString(DEFAULT_DEBUG_PORT));
            debugPort = DEFAULT_DEBUG_PORT;
            LOGGER.log(Level.INFO, "Converted debug port to {0} for {1}",
                    new String[]{Integer.toString(DEFAULT_DEBUG_PORT),
                getDisplayName()});
        } finally {
            putProperty(GlassfishModule.USE_SHARED_MEM_ATTR,
                    Boolean.toString(false));
        }
        return debugPort;
    }

    /**
     * Check if local running server started from IDE is still running.
     * <p/>
     * @returns Value of <code>true</code> when process information is stored
     *          and process is still running or <code>false</code> otherwise.
     */
    public boolean isProcessRunning() {
        if (process == null) {
            return false;
        }
        try {
            process.exitValue();
        } catch (IllegalThreadStateException itse) {
            return true;
        }
        return false;
    }

    /**
     * Returns Java SE platform {@see JavaPlatform} object configured
     * for GlassFfish server.
     * <p/>
     * Current code is not optimal. It does full scan of installed platforms
     * to search for platform installation folder matching java home folder
     * from GlassFfish server instance object.
     * <p/>
     * @return Returns Java SE platform {@see JavaPlatform} object configured
     *         for GlassFfish server or null if no such platform was configured.
     */
    public JavaPlatform getJavaPlatform() {
        String javaHome = getJavaHome();
        if (javaHome == null || javaHome.length() == 0) {
            return null;
        }
        JavaPlatform[] platforms
                = JavaPlatformManager.getDefault().getInstalledPlatforms();
        File javaHomeFile = new File(javaHome);
        JavaPlatform javaPlatform = null;
        for (JavaPlatform platform : platforms) {
            for (FileObject fo : platform.getInstallFolders()) {
                if (javaHomeFile.equals(FileUtil.toFile(fo))) {
                    javaPlatform = platform;
                    break;
                }
            }
            if (javaPlatform != null) {
                break;
            }
        }
        return javaPlatform;
    }

    /**
     * Get domains root folder with write access.
     * <p/>
     * @return Domains root folder with write access.
     */
    public synchronized String getDomainsRoot() {
        String retVal = getDomainsFolder();
        if (null == retVal) {
            return null;
        }
        File candidate = new File(retVal);
        if (candidate.exists() && !Utils.canWrite(candidate)) {
            // we need to do some surgury here...
            String domainsFolder = org.netbeans.modules.glassfish.common.utils
                    .ServerUtils.getDomainsFolder(this);
            String foldername = FileUtil.findFreeFolderName(
                    FileUtil.getConfigRoot(), domainsFolder);
            FileObject destdir = null;
            try {
                destdir = FileUtil.createFolder(FileUtil.getConfigRoot(),foldername);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO,"could not create a writable domain dir",ex); // NOI18N
            }
            if (null != destdir) {
                candidate = new File(candidate, getDomainName());
                FileObject source = FileUtil.toFileObject(FileUtil.normalizeFile(candidate));
                try {
                    Utils.doCopy(source, destdir);

                    retVal = FileUtil.toFile(destdir).getAbsolutePath();
                    setDomainsFolder(retVal);
                } catch (IOException ex) {
                    // need to try again... since the domain is probably unreadable.
                    foldername = FileUtil.findFreeFolderName(
                            FileUtil.getConfigRoot(), domainsFolder); // NOI18N
                    try {
                        destdir = FileUtil.createFolder(FileUtil.getConfigRoot(), foldername);
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO,"could not create a writable second domain dir",ioe); // NOI18N
                        return retVal;
                    }
                    File destdirFile = FileUtil.toFile(destdir);
                    setDomainsFolder(destdirFile.getAbsolutePath());
                    retVal = destdirFile.getAbsolutePath();
                    // getProvider() eventually creates a call to getDomainsRoot()... which can lead to a deadlock
                    //  forcing the call to happen after getDomainsRoot returns will 
                    // prevent the deadlock.
                    RequestProcessor.getDefault().post(new Runnable() {

                        @Override
                        public void run() {
                            CreateDomain cd = new CreateDomain("anonymous", "", // NOI18N
                                    new File(getServerHome()),
                                    properties, GlassfishInstanceProvider.getProvider(),
                                    false, true, "INSTALL_ROOT_KEY"); // NOI18N
                            cd.start();
                        }
                    }, 100);
                }
            }
        }
        return retVal;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods dependent on linked CommonServerSupport instance               //
    ////////////////////////////////////////////////////////////////////////////
    // It was too complicated to remove this dependency completely. All       //
    // methods that are dependent on CommonServerSupport instance were marked //
    // as deprecated.                                                         //
    // All of them should be moved to CommonServerSupport class itself and    //
    // used in context of this class in the future.                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get <code>CommonServerSupport</code> instance associated with
     * this object.
     * <p/>
     * @return <code>CommonServerSupport</code> instance associated with
     * this object.
     */
    public final CommonServerSupport getCommonSupport() {
        return commonSupport;
    }

    /**
     * Get GlassFish server state.
     * <p/>
     * Server state is refreshed if actual state is unknown.
     * <p/>
     * @return GlassFish server state.
     * @deprecated GlassfishInstance class should not be dependent
     *             on CommonServerSupport.
     */
    @Deprecated
    public final ServerState getServerState() {
        return getCommonSupport().getServerState();
    }

    /**
     * Stop GlassFish instance if it was started by IDE.
     * <p/>
     * @param timeout Time to wait for successful completion.
     * @deprecated GlassfishInstance class should not be dependent
     *             on CommonServerSupport.
     */
    @Deprecated
    final void stopIfStartedByIde(long timeout) {
        if(commonSupport.isStartedByIde()) {
            ServerState state = commonSupport.getServerState();
            if(state == ServerState.STARTING ||
                    (state == ServerState.RUNNING
                    && GlassFishState.isOnline(this))) {
                try {
                    Future<TaskState> stopServerTask = commonSupport.stopServer(null);
                    if(timeout > 0) {
                        TaskState opState = stopServerTask.get(timeout, TimeUnit.MILLISECONDS);
                        if(opState != TaskState.COMPLETED) {
                            Logger.getLogger("glassfish").info("Stop server failed..."); // NOI18N
                        }
                    }
                } catch(TimeoutException ex) {
                    LOGGER.log(Level.FINE, "Server {0} timed out sending stop-domain command.", getDeployerUri()); // NOI18N
                } catch(Exception ex) {
                    LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
                }
            }
        } else {
            // prevent j2eeserver from stoping an authenticated server that
            // it did not start.
            commonSupport.disableStop();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build and update copy of GlassFish properties to be stored
     * in <code>this</code> object.
     * <p/>
     * Constructor helper method.
     * <p/>
     * @param properties Source GlassFish properties.
     * @return Updated copy of GlassFish properties to be stored.
     */
    private Map<String, String> prepareProperties(
            Map<String, String> properties) {
        boolean isRemote
                = properties.get(GlassfishModule.DOMAINS_FOLDER_ATTR) == null;
        String deployerUri = properties.get(GlassfishModule.URL_ATTR);
        updateString(properties, GlassfishModule.HOSTNAME_ATTR,
                DEFAULT_HOST_NAME);
        updateString(properties, GlassfishModule.GLASSFISH_FOLDER_ATTR, "");
        updateInt(properties, GlassfishModule.HTTPPORT_ATTR,
                DEFAULT_HTTP_PORT);
        updateString(properties, GlassfishModule.DISPLAY_NAME_ATTR,
                "Bogus display name");
        updateInt(properties, GlassfishModule.ADMINPORT_ATTR,
                DEFAULT_ADMIN_PORT);
        updateString(properties, GlassfishModule.SESSION_PRESERVATION_FLAG,
                "true");
        updateString(properties, GlassfishModule.START_DERBY_FLAG,
                isRemote ? "false" : "true");
        updateString(properties, GlassfishModule.USE_IDE_PROXY_FLAG, "true");
        updateString(properties, GlassfishModule.DRIVER_DEPLOY_FLAG, "true");
        updateString(properties, GlassfishModule.HTTPHOST_ATTR, "localhost");
        properties.put(GlassfishModule.JVM_MODE,
                isRemote && !deployerUri.contains("deployer:gfv3ee6wc")
                ? GlassfishModule.DEBUG_MODE : GlassfishModule.NORMAL_MODE);
        updateString(properties, GlassfishModule.USERNAME_ATTR,
                DEFAULT_ADMIN_NAME);
        updateString(properties, GlassfishModule.NB73_IMPORT_FIXED,
                Boolean.toString(true));
        Map<String, String> newProperties = new Props(properties);
        // Asume a local instance is in NORMAL_MODE
        // Assume remote Prelude and 3.0 instances are in DEBUG (we cannot change them)
        // Assume a remote 3.1 instance is in NORMAL_MODE... we can restart it into debug mode
        // XXX username/password handling at some point.
        return newProperties;
    }

    /**
     * Check if this instance is publicly accessible.
     * <p/>
     * @return <code>true</code> if this instance is publicly accessible
     *         or <code>false</code> otherwise.
     */
    private boolean isPublicAccess() {
        return instanceProvider.getInternalInstance(getUrl()) != null;
    }

    /**
     * Make this instance publicly accessible if it was not done yet.
     * <p/>
     * Used during initialization phase to register this object into
     * <code>GlassfishInstanceProvider</code>.
     * <code>CommonServerSupport</code> instance related to this object must be
     * also initialized.
     */
    private void allowPublicAccess() {
        if (!isPublicAccess()) {
            instanceProvider.addServerInstance(this);
        }
    }

    /**
     * Get property storedValue with given <code>name</code> as <code>int</code>
     * storedValue.
     * <p/>
     * Works for positive values only because <code>-1</code> storedValue is reserved
     * for error conditions.
     * <p/>
     * @param name Name of property to be retrieved.
     * @return Property storedValue as <code>int</code> or <code>-1</code>
     *         if property cannot be converted to integer storedValue.
     */
    private int intProperty(String name) {
        String property = properties.get(name);
        if (property == null) {
            LOGGER.log(Level.WARNING,
                    "Cannot convert null value to a number");
            return -1;
        }
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.WARNING, "Cannot convert "+
                    property +" to a number: ", nfe);
            return -1;
        }
    }

    private void updateFactories() {
        // !PW FIXME should read asenv.bat on windows.
        Properties asenvProps = new Properties();
        String homeFolder = getGlassfishRoot();
        File asenvConf = new File(homeFolder, "config/asenv.conf"); // NOI18N
        if(asenvConf.exists()) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(asenvConf));
                asenvProps.load(is);
            } catch(FileNotFoundException ex) {
                LOGGER.log(Level.WARNING, null, ex); // NOI18N
            } catch(IOException ex) {
                LOGGER.log(Level.WARNING, null, ex); // NOI18N
                asenvProps.clear();
            } finally {
                if(is != null) {
                    try { is.close(); } catch (IOException ex) { }
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "{0} does not exist", asenvConf.getAbsolutePath()); // NOI18N
        }
        Set<GlassfishModuleFactory> added = new HashSet<>();
        //Set<GlassfishModuleFactory> removed = new HashSet<GlassfishModuleFactory>();
        synchronized (lookupResult) {
            Collection<? extends GlassfishModuleFactory> factories = lookupResult.allInstances();
            added.addAll(factories);
            added.removeAll(currentFactories);
            currentFactories = factories;

            List<Lookup> proxies = new ArrayList<>();
            proxies.add(localLookup);
            for (GlassfishModuleFactory moduleFactory : added) {
                if(moduleFactory.isModuleSupported(homeFolder, asenvProps)) {
                    Object t = moduleFactory.createModule(localLookup);
                    if (null == t) {
                        LOGGER.log(Level.WARNING, "{0} created a null module", moduleFactory); // NOI18N
                    } else {
                        ic.add(t);
                        if (t instanceof Lookup.Provider) {
                            proxies.add(Lookups.proxy((Lookup.Provider) t));
                        }
                    }
                }
            }

            if (!proxies.isEmpty()) {
                full = new ProxyLookup(proxies.toArray(Lookup[]::new));
            }
        }

    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        updateFactories();
    }

    public ServerInstance getCommonInstance() {
        return commonInstance;
    }
           
    @Override
    public Lookup getLookup() {
        synchronized (lookupResult) {
            return full;
        }
    }
    
    // ------------------------------------------------------------------------
    // ServerInstance interface implementation
    // ------------------------------------------------------------------------

    // TODO -- this should be done differently
    @Override
    public String getServerDisplayName() {
        return NbBundle.getMessage(GlassfishInstance.class, "STR_SERVER_NAME",
                new Object[] {version != null ? version.toString() : ""});
    }

    @Override
    public Node getFullNode() {
        Logger.getLogger("glassfish").finer("Creating GF Instance node [FULL]"); // NOI18N
        synchronized (lookupResult) {
            if (fullNode == null) {
                fullNode = new Hk2InstanceNode(this, true);
            }
            return fullNode;
        }
    }

    @Override
    public Node getBasicNode() {
        Logger.getLogger("glassfish").finer("Creating GF Instance node [BASIC]"); // NOI18N
        return new Hk2InstanceNode(this, false);
    }

    @Override
    public JComponent getCustomizer() {
        return new GlassFishPropertiesCustomizer(this, localLookup);
    }

    @Override
    public boolean isRemovable() {
        return removable;
    }

    @Override
    public void remove() {
        // Just in case...
        if(!removable) {
            return;
        }
        
        // !PW FIXME Remove debugger hooks, if any
//        DebuggerManager.getDebuggerManager().removeDebuggerListener(debuggerStateListener);

        stopIfStartedByIde(3000L);
        
        // close the server io window
        String uri = getDeployerUri();
        InputOutput io = LogViewMgr.getServerIO(uri);
        if(io != null && !io.isClosed()) {
            io.closeInputOutput();
        }

        Collection<? extends RemoveCookie> lookupAll = localLookup.lookupAll(RemoveCookie.class);
        for(RemoveCookie cookie: lookupAll) {
            cookie.removeInstance(getDeployerUri());
        }

        instanceProvider.removeServerInstance(this);
        ic.remove(this);
    }

    //
    // watch out for the localhost alias.
    //
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlassfishInstance)) {
            return false;
        }
        GlassfishInstance other = (GlassfishInstance) obj;
        if (null == getDeployerUri()) {
            return false;
        }
        if (null == other.getDeployerUri()) {
            return false;
        }
        // Domain name can be null so we shall avoid NPE.
        boolean domainName = Objects.equals(getDomainName(), other.getDomainName());
        // Domains root can be null so we shall avoid NPE.
        boolean domainsRoot = Objects.equals(getDomainsRoot(), other.getDomainsRoot());
        return domainName && domainsRoot
                && getDeployerUri().replace("127.0.0.1", "localhost")
                        .equals(other.getDeployerUri().replace("127.0.0.1", "localhost"))
                && getHttpPort().equals(other.getHttpPort());
    }

    /**
     * Generate hash code for GlassFish instance data object.
     * <p/>
     * Hash code is based on name attribute
     * (<code>GlassfishModule.DISPLAY_NAME_ATTR</code> property) which
     * is unique.
     * <p/>
     * @return Hash code for GlassFish instance data object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
