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

package org.netbeans.modules.derby;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.BeanNode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea
 */
public class DerbyOptions {

    private static final Logger LOGGER = Logger.getLogger(DerbyOptions.class.getName());

    private static final DerbyOptions INSTANCE = new DerbyOptions();

    /**
     * This system property allows setting a default value for the Derby system home directory.
     * Its value will be returned by the {@link getSystemHome} method if the
     * systemHome property is null. See issue 76908.
     */
    public static final String NETBEANS_DERBY_SYSTEM_HOME = "netbeans.derby.system.home"; // NOI18N

    static final String PROP_DERBY_LOCATION = "location"; // NOI18N
    static final String PROP_DERBY_SYSTEM_HOME = "systemHome"; // NOI18N

    static final String INST_DIR = "db-derby-10.14.2.0"; // NOI18N

    public static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    public static final String DRIVER_CLASS_EMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver"; // NOI18N

    private static final String DRIVER_PATH_NET = "lib/derbyclient.jar"; // NOI18N
    private static final String DRIVER_PATH_EMBEDDED = "lib/derby.jar"; // NOI18N

    // XXX these should actually be localized, but we'd have to localize
    // DriverListUtil in the db module first
    public static final String DRIVER_DISP_NAME_NET = "Java DB (Network)"; // NOI18N
    public static final String DRIVER_DISP_NAME_EMBEDDED = "Java DB (Embedded)"; // NOI18N

    private static final String DRIVER_NAME_NET = "apache_derby_net"; // NOI18N
    private static final String DRIVER_NAME_EMBEDDED = "apache_derby_embedded"; // NOI18N

    public static DerbyOptions getDefault() {
        return INSTANCE;
    }

    protected final String putProperty(String key, String value, boolean notify) {
        String retval = NbPreferences.forModule(DerbyOptions.class).get(key, null);
        if (value != null) {
            // LOGGER.log(Level.FINE, "Setting property {0} to {1}", key, value); // NOI18N
            NbPreferences.forModule(DerbyOptions.class).put(key, value);
        } else {
            // LOGGER.log(Level.FINE, "Removing property {0}", key); // NOI18N
            NbPreferences.forModule(DerbyOptions.class).remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return NbPreferences.forModule(DerbyOptions.class).get(key, null);
    }

    public String displayName() {
        return NbBundle.getMessage(DerbyOptions.class, "LBL_DerbyOptions");
    }

    /**
     * Returns the Derby location or an empty string if the Derby location
     * is not set. Never returns null.
     */
    public String getLocation() {
        DerbyActivator.activate();
        String location = getProperty(PROP_DERBY_LOCATION);
        if (location == null) {
            location = ""; // NOI18N
        }
        Logger.getLogger(DerbyOptions.class.getName()).finest("Derby location is " + location);
        return location;
    }

    private String getCurrentLocation() {
        String location = getProperty(PROP_DERBY_LOCATION);
        if (location == null) {
            location = ""; // NOI18N
        }
        return location;
    }

    /**
     * Returns true if the Derby location is null. This method is needed
     * since getLocation() will never return a null value.
     */
    public boolean isLocationNull() {
        return getProperty(PROP_DERBY_LOCATION) == null;
    }

    /**
     * Sets the Derby location.
     *
     * @param location the Derby location. A null value is valid and
     *        will be returned by getLocation() as an empty
     *        string (meaning "not set"). An empty string is valid
     *        and has the meaning "set to the default location".
     */
    public void setLocation(String location) {
        if (location !=  null && location.length() > 0) {
            File locationFile = new File(location).getAbsoluteFile();
            if (!locationFile.exists()) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", locationFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
            if (!Util.isDerbyInstallLocation(locationFile)) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_InvalidDerbyLocation", locationFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
        }

        synchronized (this) {
            stopDerbyServer();
            if (location != null && location.length() <= 0) {
                location = getDefaultInstallLocation();
            }
            registerDrivers(location);
            registerLibrary(location);
            LOGGER.log(Level.FINE, "Setting location to {0}", location); // NOI18N
            putProperty(PROP_DERBY_LOCATION, location, true);
        }
    }

    public synchronized boolean trySetLocation(String location) {
        LOGGER.log(Level.FINE, "trySetLocation: Trying to set location to {0}", location); // NOI18N
        String current = getCurrentLocation();
        if (current.length() == 0) {
            setLocation(location);
            LOGGER.fine("trysetLocation: Succeeded"); // NOI18N
            return true;
        }
        File currentFile = new File(current);
        if (!currentFile.exists() || currentFile.isFile()) {
             setLocation(location);
             LOGGER.fine("trysetLocation: correcting"); // NOI18N
             return true;                
        }
        LOGGER.fine("trySetLocation: Another location already set"); // NOI18N
        return false;
    }

    /**
     * Returns the Derby system home or an empty string if the system home
     * is not set. Never returns null.
     */
    public String getSystemHome() {
        String systemHome = getProperty(PROP_DERBY_SYSTEM_HOME);
        if (systemHome == null) {
            systemHome = System.getProperty(NETBEANS_DERBY_SYSTEM_HOME);
        }
        if (systemHome == null) {
            systemHome = ""; // NOI18N
        }
        return systemHome;
    }

    public void setSystemHome(String derbySystemHome) {
        if (derbySystemHome != null && derbySystemHome.length() > 0) {
            File derbySystemHomeFile = new File(derbySystemHome).getAbsoluteFile();
            if (!derbySystemHomeFile.exists() || !derbySystemHomeFile.isDirectory()) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", derbySystemHomeFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
            if (!derbySystemHomeFile.canWrite()) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryIsNotWritable", derbySystemHomeFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
        }

        synchronized (this) {
            stopDerbyServer();
            putProperty(PROP_DERBY_SYSTEM_HOME, derbySystemHome, true);
            DerbyDatabasesImpl.getDefault().notifyChange();
        }
    }

    static String getDefaultInstallLocation() {
        File location = InstalledFileLocator.getDefault().locate(INST_DIR, null, false);
        if (location == null) {
            return null;
        }
        if (!Util.isDerbyInstallLocation(location)) {
            return null;
        }
        return location.getAbsolutePath();
    }

    private static void stopDerbyServer() {
        RegisterDerby.getDefault().stop();
    }

    private static void registerDrivers(final String newLocation) {
        try {
            // registering the drivers in an atomic action so the Drivers node
            // is refreshed only once
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() {
                    registerDriver(DRIVER_NAME_NET, DRIVER_DISP_NAME_NET, DRIVER_CLASS_NET,
                            new String[]{DRIVER_PATH_NET, DRIVER_PATH_EMBEDDED}, newLocation);
                    registerDriver(DRIVER_NAME_EMBEDDED, DRIVER_DISP_NAME_EMBEDDED,
                            DRIVER_CLASS_EMBEDDED, new String[]{DRIVER_PATH_EMBEDDED}, newLocation);
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private static void registerDriver(String driverName, String driverDisplayName, String driverClass, String[] driverRelativeFile, String newLocation) {
        // try to remove the driver first if it exists was registered from the current location
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
        for (int i = 0; i < drivers.length; i++) {
            JDBCDriver driver = drivers[i];
            URL[] urls = driver.getURLs();
            String currentLocation = DerbyOptions.getDefault().getLocation();
            if (currentLocation == null) {
                continue;
            }

            boolean fromCurrentLocation = true;

            for (int j = 0; j < urls.length; j++) {
                File file = null;
                if ("file".equals(urls[j].getProtocol())) { // NOI18N
                    // FileObject's do not work nice if the file urls[j] points to doesn't exist
                    try {
                        file = new File(urls[j].toURI());
                    } catch (URISyntaxException e) {
                        LOGGER.log(Level.WARNING, null, e);
                    }
                } else {
                    FileObject fo = URLMapper.findFileObject(urls[j]);
                    if (fo != null) {
                        file = FileUtil.toFile(fo);
                    }
                }
                if (file != null) {
                    String driverFile = file.getAbsolutePath();
                    if (driverFile.startsWith(currentLocation)) {
                        continue;
                    }
                }
                fromCurrentLocation = false;
                break;
            }

            if (fromCurrentLocation) {
                try {
                    JDBCDriverManager.getDefault().removeDriver(driver);
                } catch (DatabaseException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    // better to return if the existing driver could not be registered
                    // otherwise we would register yet another one
                    return;
                }
            }
        }

        // register the new driver if it exists at the new location
        if (newLocation != null && newLocation.length() >= 0) {
            URL[] driverFileUrls = new URL[driverRelativeFile.length];
            try {
                for (int i = 0; i < driverRelativeFile.length; i++) {
                    File drvFile = new File(newLocation, driverRelativeFile[i]);
                    if (!drvFile.exists()) {
                        return;
                    }
                    driverFileUrls[i] = drvFile.toURI().toURL();
                }
                JDBCDriver newDriver = JDBCDriver.create(driverName, driverDisplayName, driverClass, driverFileUrls);
                JDBCDriverManager.getDefault().addDriver(newDriver);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, null, e);
            } catch (DatabaseException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
    }

    private void registerLibrary(final String newLocation) {
        final FileObject libsFolder = FileUtil.getConfigFile(
                "org-netbeans-api-project-libraries/Libraries");        //NOI18N
        if (libsFolder != null && newLocation != null) {
            try {
                File location = new File(newLocation);
                if (location.exists() && location.isDirectory()) {
                    location = FileUtil.normalizeFile(location);
                    libsFolder.getFileSystem().runAtomicAction(
                            new DerbyLibraryRegistrar(location, libsFolder));
                }
            } catch (FileStateInvalidException ex) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);   //NOI18N
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);   //NOI18N
            }
        }
    }

    private static BeanNode createViewNode() throws java.beans.IntrospectionException {
        return new BeanNode<DerbyOptions>(DerbyOptions.getDefault());
    }

    static class DerbyLibraryRegistrar implements FileSystem.AtomicAction {

        private File location;
        private FileObject libsFolder;

        DerbyLibraryRegistrar(File location, FileObject libsFolder) {
            this.location = location;
            this.libsFolder = libsFolder;
        }

        @Override
        public void run() throws IOException {

            FileLock ld = null;
            java.io.OutputStream outStreamd = null;
            Writer outd = null;
            OutputStreamWriter osw = null;
            try {
                // the derby lib driver:
                FileObject derbyLib = null;
                derbyLib = libsFolder.getFileObject("JavaDB", "xml");//NOI18N
                if (null == derbyLib) {
                    derbyLib = libsFolder.createData("JavaDB", "xml");//NOI18N
                    ld = derbyLib.lock();
                    outStreamd = derbyLib.getOutputStream(ld);
                    osw = new OutputStreamWriter(outStreamd);
                    outd = new BufferedWriter(osw);
                    outd.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");//NOI18N
                    outd.write("<library version=\"3.0\" xmlns=\"http://www.netbeans.org/ns/library-declaration/3\">\n");//NOI18N
                    outd.write("<name>JAVADB_DRIVER_LABEL</name>\n");//NOI18N
                    outd.write("<type>j2se</type>\n");//NOI18N
                    outd.write("<localizing-bundle>org.netbeans.modules.derby.Bundle</localizing-bundle>\n");//NOI18N
                    outd.write("<volume>\n<type>classpath</type>\n"); //NOI18N
                    outd.write("<resource>jar:" + new File(location.getAbsolutePath() + "/lib/derby.jar").toURI().toURL() + "!/</resource>\n"); //NOI18N
                    outd.write("<resource>jar:" + new File(location.getAbsolutePath() + "/lib/derbyclient.jar").toURI().toURL() + "!/</resource>\n"); //NOI18N
                    outd.write("<resource>jar:" + new File(location.getAbsolutePath() + "/lib/derbynet.jar").toURI().toURL() + "!/</resource>\n"); //NOI18N
                    outd.write("</volume>\n<volume>\n<type>src</type>\n</volume>\n"); //NOI18N
                    outd.write("<volume>\n<type>javadoc</type>\n");  //NOI18N
                    outd.write("</volume>\n"); //NOI18N
                    outd.write("<properties>\n<property>\n"); //NOI18N
                    outd.write("<name>maven-dependencies</name>\n"); //NOI18N
                    outd.write("<value>\n"); //NOI18N
                    outd.write("org.apache.derby:derby:10.14.2.0:jar\n"); //NOI18N
                    outd.write("org.apache.derby:derbyclient:10.14.2.0:jar\n"); //NOI18N
                    outd.write("org.apache.derby:derbynet:10.14.2.0:jar\n"); //NOI18N
                    outd.write("</value>\n"); //NOI18N
                    outd.write("</property>\n</properties>\n"); //NOI18N
                    outd.write("</library>\n"); //NOI18N
                }
            } finally {
                if (null != outd) {
                    try {
                        outd.close();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO, ioe.getLocalizedMessage(), ioe); //NOI18N
                    }
                }
                if (null != outStreamd) {
                    try {
                        outStreamd.close();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO, ioe.getLocalizedMessage(), ioe); //NOI18N
                    }
                }
                if (null != ld) {
                    ld.releaseLock();
                }
            }
        } //run
    } //DerbyLibraryRegistrar
}
