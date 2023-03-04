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
package org.netbeans.modules.glassfish.tooling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.netbeans.modules.glassfish.tooling.admin.CommandRestoreDomain;
import org.netbeans.modules.glassfish.tooling.admin.ResultString;
import org.netbeans.modules.glassfish.tooling.admin.ServerAdmin;
import org.netbeans.modules.glassfish.tooling.data.*;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Common GlassFish IDE SDK test.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class CommonTest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommonTest.class);

    /** GlassFish test server name property. */
    public static final String GFPROP_NAME            = "name";

    /** GlassFish test server host property. */
    public static final String GFPROP_HOST            = "host";

    /** GlassFish test server port property. */
    public static final String GFPROP_PORT            = "port";

    /** GlassFish test server admin port property. */
    public static final String GFPROP_ADMIN_PORT      = "adminPort";

    /** GlassFish test server admin user property. */
    public static final String GFPROP_ADMIN_USER      = "adminUser";

    /** GlassFish test server admin password property. */
    public static final String GFPROP_ADMIN_PASSWORD  = "adminPassword";

    /** GlassFish test server domains folder property. */
    public static final String GFPROP_DOMAINS_FOLDER  = "domainsFolder";

    /** GlassFish test server domain name property. */
    public static final String GFPROP_DOMAIN_NAME     = "domainName";

    /** GlassFish test server url property. */
    public static final String GFPROP_URL             = "url";

    /** GlassFish test server version property. */
    public static final String GFPROP_VERSION         = "version";

    /** GlassFish test server administration interface property. */
    public static final String GFPROP_ADMIN_INTERFACE = "adminInterface";

    /** GlassFish test server server home property. */
    public static final String GFPROP_HOME            = "serverHome";

    /** GlassFish test server server JVM command line arguments. */
    public static final String GFPROP_JAVA_ARGS       = "javaArgs";

    /** GlassFish test server server bootstrap jar command line arguments. */
    public static final String GFPROP_GLASSFISH_ARGS  = "glassFishArgs";

    /** Test JDK Java home property. */
    public static final String JDKPROP_HOME           = "javaHome";

    /** Test JDK property file. */
    private static final String JDK_PROPERTES =
            "src/test/java/org/netbeans/modules/glassfish/tooling/Java.properties";
    
    /** Backup domain property. */
    public static final String BACKUP_DOMAIN         = "backupDomain";

    /** Test JDK properties. */
    private static volatile Properties jdkProperties;
   
    /** Name of static method to retrieve GlassFish test server properties.*/
    private static final String GLASSFISH_PROPERTES_METHOD
            = "getGlassFishProperty";

    /** GlassFish test server property file. */
    private static final String GLASSFISH_PROPERTES
            = "src/test/java/org/netbeans/modules/glassfish/tooling/GlassFish.properties";

    /** Regex expression to find GlassFish log message informing about
     *  basic startup.
     *  <p/>
     *  Example: GlassFish Server Open Source Edition 3.1.2 (23) startup time :
     *           Felix (2,201ms), startup services(4,351ms), total(6,552ms) */
    private static final String STARTED_MESSAGE_REGEX
            = ".*Server.*startup time.*";

    /** Regex expression to find GlassFish log message informing about
     *  server shutdown.
     *  <p/>
     *  Example: Server shutdown initiated */
    private static final String SHUTDOWN_MESSAGE_REGEX
            = ".*Server shutdown initiated.*";

    /** Pattern to find GlassFish log message informing about basic startup. */
    protected static final Pattern STARTED_MESSAGE_PATTERN
            = Pattern.compile(STARTED_MESSAGE_REGEX);

    /** Pattern to find GlassFish log message informing about server shutdown.
     */
    protected static final Pattern SHUTDOWN_MESSAGE_PATTERN
            = Pattern.compile(SHUTDOWN_MESSAGE_REGEX);

    /** GlassFish test server properties. */
    private static volatile Properties glassfishProperties;

    /** GlassFish test server object. */
    private static volatile GlassFishServer glassfishServer;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve integer value stored as property.
     * <p/>
     * @param properties    <code>Properties</code> container.
     * @param propertyName  Name of property to be retrieved.
     * @return <code>int</code> value of property or <code>-1</code> if value
     *         could not be converted.
     */
    private static int intProperty(Properties properties, String propertyName) {
        final String METHOD = "intProperty";
        String propertyString = properties.getProperty(propertyName);
        int propertyValue;
        if (propertyString == null) {
            propertyValue = -1;
            LOGGER.log(Level.WARNING, METHOD, "undefined", propertyName);
        }
        else {
            try {
                propertyValue = Integer.parseInt(propertyString);
            } catch (NumberFormatException nfe) {
                Logger.log(Level.WARNING,
                        "Cannot set " + propertyName + " property", nfe);
                propertyValue = -1;
            }
        }
        return propertyValue;
    }

    /**
     * Get test JDK properties.
     * <p>
     * @return Test JDK properties.
     */
    public static Properties jdkProperties() {
        if (jdkProperties != null) {
            return jdkProperties;
        }
        else {
            synchronized(CommonTest.class) {
                if (jdkProperties == null) {
                    jdkProperties = readProperties(JDK_PROPERTES);
                }
            }
            return jdkProperties;
        }
    }

    /**
     * Get JDK property for tests.
     * <p/>
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     * <p/>
     * @param key Property key.
     * @return Value in JDK property list with the specified key value.
     */
    public static String getJdkProperty(String key) {
        return jdkProperties().getProperty(key);
    }

    /**
     * Read properties from file. <p>
     *
     * @param propertiesFile Properties file.
     * @return GlassFish test server properties.
     */
    public static Properties readProperties(String propertiesFile) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException ioe) {
            Logger.log(Level.WARNING,
                    "Cannot read " + propertiesFile + " file", ioe);
        }
        return properties;
    }

    /**
     * Get GlassFish test server properties from provided super class invoking
     * it's <code>getGlassFishProperty(String key)</code> static method.
     * <p/>
     * @param pClass Super class containing
     *        <code>getGlassFishProperty(String key)</code> static method
     *        to be invoked.
     * @param pMeth <code>getGlassFishProperty(String key)</code> method already
     *        retrieved from super class object.
     * @param key   Property key.
     * @return Value returned by super class
     *         <code>getGlassFishProperty(String key)</code> method.
     */
    private static String getGlassFishProperty(
            Class <? extends CommonTest> pClass, Method pMeth, String key) {
        try {
            return (String)pMeth.invoke(pClass, key);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException iae) {
            Logger.log(Level.WARNING, "Getting GlassFish property failed: ",
                    iae);
            return null;
        }
    }

    /**
     * Get GlassFish test server properties from provided super class invoking
     * it's <code>getGlassFishProperty(String key)</code> static method
     * and convert it to positive integer value if possible.
     * <p/>
     * @param pClass Super class containing
     *        <code>getGlassFishProperty(String key)</code> static method
     *        to be invoked.
     * @param pMeth <code>getGlassFishProperty(String key)</code> method already
     *        retrieved from super class object.
     * @param key   Property key.
     * @return Value returned by super class
     *         <code>getGlassFishProperty(String key)</code> method converted 
     *         to positive integer value or <code>-1</code> if value
     *         could not be converted or retrieved.
     */
    private static int intGlassFishProperty(Class <? extends CommonTest> pClass,
            Method propertyMethod, String key) {
        final String METHOD = "intGlassFishProperty";
        String propertyString = getGlassFishProperty(pClass, propertyMethod,
                key);
        int propertyValue;
        if (propertyString == null) {
            propertyValue = -1;
            LOGGER.log(Level.WARNING, METHOD, "undefined", key);
        }
        else {
            try {
                propertyValue = Integer.parseInt(propertyString);
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.WARNING, METHOD, "cantSet", key);
                LOGGER.log(Level.WARNING, METHOD, "numberFormat", nfe);
                propertyValue = -1;
            }
        }
        return propertyValue;
    }

    /**
     * Constructs <code>GlassFishServer</code> object using GlassFish
     * test server properties
     * <p/>
     * @param properties
     * @return <code>GlassFishServer</code> object initialized with GlassFish
     *         test server properties values.
     */
    public static GlassFishServer createGlassfishServer(
            Class <? extends CommonTest> pClass) {
        final String METHOD = "createGlassfishServer";
        Method pMeth;
        try {
            pMeth = pClass.getMethod(GLASSFISH_PROPERTES_METHOD, String.class);
        } catch (NoSuchMethodException | SecurityException nme) {
            LOGGER.log(Level.WARNING, METHOD, "accessorFailed", nme);
            return null;
        }
        GlassFishServerEntity server = new GlassFishServerEntity();
        server.setName(getGlassFishProperty(pClass, pMeth, GFPROP_NAME));
        server.setHost(getGlassFishProperty(pClass, pMeth, GFPROP_HOST));
        server.setAdminUser(getGlassFishProperty(pClass, pMeth,
                GFPROP_ADMIN_USER));
        server.setAdminPassword(getGlassFishProperty(pClass, pMeth,
                GFPROP_ADMIN_PASSWORD));
        server.setDomainsFolder(getGlassFishProperty(pClass, pMeth,
                GFPROP_DOMAINS_FOLDER));
        server.setDomainName(getGlassFishProperty(pClass, pMeth,
                GFPROP_DOMAIN_NAME));
        server.setServerHome(getGlassFishProperty(pClass, pMeth, GFPROP_HOME));
        server.setUrl(getGlassFishProperty(pClass, pMeth, GFPROP_URL));
        server.setPort(intGlassFishProperty(pClass, pMeth, GFPROP_PORT));
        server.setAdminPort(intGlassFishProperty(pClass, pMeth,
                GFPROP_ADMIN_PORT));
        GlassFishVersion version = GlassFishVersion.toValue(
                getGlassFishProperty(pClass, pMeth, GFPROP_VERSION));
        if (version == null) {
            LOGGER.log(Level.WARNING, METHOD, "unknownVersion", GFPROP_ADMIN_PORT);
        }
        server.setVersion(version);
        GlassFishAdminInterface adminInterface =
                GlassFishAdminInterface.toValue(
                getGlassFishProperty(pClass, pMeth, GFPROP_ADMIN_INTERFACE));
        if (adminInterface == null) {
            LOGGER.log(Level.WARNING, METHOD,
                    "unknownAdminInterface", GFPROP_ADMIN_INTERFACE);
        }
        server.setAdminInterface(adminInterface);
        return server;
    }

    /**
     * Constructs <code>GlassFishServer</code> object using GlassFish
     * test server properties
     * <p/>
     * @param properties
     * @return <code>GlassFishServer</code> object initialized with GlassFish
     *         test server properties values.
     */
    public static GlassFishServer createGlassfishServer() {
        return CommonTest.createGlassfishServer(CommonTest.class);
    }

    /**
     * Get GlassFish test server object with common values.
     * <p>
     * @return GlassFish test server object with common values.
     */
    protected static GlassFishServer glassFishServer() {
        if (glassfishServer != null) {
            return glassfishServer;
        }
        else {
            synchronized(CommonTest.class) {
                if (glassfishServer == null) {
                    glassfishServer = createGlassfishServer();
                }
            }
            return glassfishServer;
        }
    }
    
    public static void restoreDomain(GlassFishServer server, String backupFile) {
        File domainBackupFile = new File(backupFile);
        if (!domainBackupFile.exists() || !domainBackupFile.isFile()) {
            throw new IllegalArgumentException("Wrong configuration of backup archive");
        }
        CommandRestoreDomain command = new CommandRestoreDomain(getJdkProperty(
                JDKPROP_HOME), domainBackupFile);
        
        try {
        Future<ResultString> future = ServerAdmin.<ResultString>exec(
                server, command);
        try {
            ResultString result = future.get(120, TimeUnit.SECONDS);
            assertEquals(result.getState(), TaskState.COMPLETED);
        } catch (InterruptedException ex) {
            fail("restore domain interrupted", ex);
        } catch (ExecutionException ex) {
            fail("restore domain failed", ex);
        } catch (TimeoutException ex) {
            fail("restore domain timeout", ex);
        }
        } catch (GlassFishIdeException e) {
            fail("restore domain failed", e);
        }
    }
}
