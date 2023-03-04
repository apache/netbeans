/**
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

package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.*;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.wizard.components.panels.netbeans.NbWelcomePanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbWelcomePanel.BundleType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class NetBeansUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static void addCluster(File nbLocation, String clusterName) throws IOException {
        addCluster(nbLocation, clusterName, null);
    }

    public static void addCluster(File nbLocation, String clusterName, String afterCluster) throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Modifying netbeans.conf for NetBeans installed at " + nbLocation + " by adding cluster \""+ clusterName + "\"" + (afterCluster==null ? "" : " after cluster \"" + afterCluster + "\""));
        final File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        touchLastModified(nbLocation, clusterName);
        List<String> list = FileUtils.readStringList(netbeansclusters);
        LogManager.log(ErrorLevel.DEBUG, "... initial list of clusters : ");
        LogManager.indent();
        LogManager.log(ErrorLevel.DEBUG, StringUtils.asString(list, SystemUtils.getLineSeparator()));
        LogManager.unindent();
        int length  = list.size();
        for (int i=0;i<length;i++) {
            String string = list.get(i);
            if (string.equals(clusterName)) {
                if(afterCluster!=null) {
                    LogManager.log(ErrorLevel.DEBUG, "... after-cluster exist, removing from the list");
                    list.remove(i);
                    break;
                } else {
                    LogManager.log(ErrorLevel.DEBUG, "... requested cluster already exist, do nothing");
                    return;
                }
            }
        }

        int index = 0;
        for (String string : list) {
            index++;
            if (afterCluster != null && string.equals(afterCluster)) {
                break;
            }
        }

        list.add(index, clusterName);
        LogManager.log(ErrorLevel.DEBUG, "... final list of clusters : ");
        LogManager.indent();
        LogManager.log(ErrorLevel.DEBUG, StringUtils.asString(list, SystemUtils.getLineSeparator()));
        LogManager.unindent();
        FileUtils.writeStringList(netbeansclusters, list);
    }
    
    private static void touchLastModified(File nbLocation, String clusterName) {
        final File clusterFile = new File(nbLocation, clusterName);
        final File lastModified = new File(clusterFile, LAST_MODIFIED_MARKER);
        // Workaround to Issue #129288 (http://www.netbeans.org/issues/show_bug.cgi?id=129288)
        // Enabling clusters has no effect without removal userdir
        // Touching of .lastModified file is done everytime when user requests to add the cluster -
        // even though it is already in the netbeans.clusters
        if(FileUtils.exists(clusterFile)) {
            if(!FileUtils.exists(lastModified)) {
                try {
                    lastModified.createNewFile();
                    lastModified.setLastModified(new Date().getTime());
                } catch (IOException e) {
                    LogManager.log(e);
                }
            } else {
                lastModified.setLastModified(new Date().getTime());
            }
        }
    }
    
    public static void removeCluster(File nbLocation, String clusterName) throws IOException {
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        
        List<String> list = FileUtils.readStringList(netbeansclusters);
        list.remove(clusterName);
        
        FileUtils.writeStringList(netbeansclusters, list);
    }
    
    public static FilesList createProductId(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        return FileUtils.writeFile(productid, getNetBeansId());
    }
    
    public static FilesList addPackId(File nbLocation, String packId) throws IOException {
        final File nbCluster = getNbCluster(nbLocation);
        
        final File productid = new File(nbCluster, PRODUCT_ID);
        
        final String id;
        if (!productid.exists()) {
            id = getNetBeansId();
        } else {
            id = FileUtils.readFile(productid).trim();
        }
        
        final List<String> ids =
                new LinkedList<>(Arrays.asList(id.split(PACK_ID_SEPARATOR)));
        
        boolean packAdded = false;
        for (int i = 1; i < ids.size(); i++) {
            if (packId.equals(ids.get(i))) {
                return new FilesList();
            }
            
            if (packId.compareTo(ids.get(i)) < 0) {
                ids.add(i, packId);
                packAdded = true;
                break;
            }
        }
        
        if (!packAdded) {
            ids.add(packId);
        }
        
        return FileUtils.writeFile(
                productid,
                StringUtils.asString(ids, PACK_ID_SEPARATOR));
    }

    public static void updateTrackingFilesInfo(File nbLocation, String clusterName) throws IOException {

        File clusterDir = new File(nbLocation, clusterName);
        if (!FileUtils.exists(clusterDir)) {
            return;
        }
        LogManager.log("Update update_tracking files for cluster directory " + clusterDir);
        File[] files = new File(clusterDir,UPDATE_TRACKING_DIR).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".xml");
            }
        });
        
        if (files != null) {
            for (File f : files) {
                updateTrackingFilesCRC(f, clusterDir);
            }
        }
    }

    private static void updateTrackingFilesCRC(File f, File clusterDir) throws IOException {
        try {
            LogManager.log("... check if correct CRC sums are used in update_tracking file " + f);
            Element root = XMLUtils.getDocumentElement(f);
            boolean needSave = false;
            for (Element el : XMLUtils.getChildren(root, "module_version")) {
                if ("true".equals(el.getAttribute("last"))) {
                    for (Element fileEl : XMLUtils.getChildren(el)) {
                        String crc = fileEl.getAttribute("crc");
                        String name = fileEl.getAttribute("name");
                        if (name != null && crc != null) {
                            File utFile = new File(clusterDir, name);
                            if (FileUtils.exists(utFile)) {
                                long crcValue = Long.parseLong(crc);
                                long realCRC = FileUtils.getCrc32(utFile);
                                if (realCRC != crcValue) {
                                    fileEl.setAttribute("crc", new Long(realCRC).toString());
                                    needSave = true;
                                }
                            }
                        }
                    }
                    break;
                }
            }
            if (needSave) {
                XMLUtils.saveXMLDocument(root.getOwnerDocument(), f);
            }
        } catch (XMLException e) {
            LogManager.log(e);
            throw new IOException("Can`t update CRC in update_tracking files", e);
        } catch (NumberFormatException e) {
            LogManager.log(e);
            throw new IOException("Can`t update CRC in update_tracking files", e);
        } catch (IOException e) {
            LogManager.log(e);
            throw new IOException("Can`t update CRC in update_tracking files", e);
        }
    }

    public static void removePackId(File nbLocation, String packId) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        String id;
        if (!productid.exists()) {
            id = getNetBeansId();
        } else {
            id = FileUtils.readFile(productid).trim();
        }
        
        String[] components = id.split(PACK_ID_SEPARATOR);
        
        StringBuilder builder = new StringBuilder(components[0]);
        for (int i = 1; i < components.length; i++) {
            if (!components[i].equals(packId)) {
                builder.append(PACK_ID_SEPARATOR).append(components[i]);
            }
        }
        
        FileUtils.writeFile(productid, builder);
    }
    
    public static void removeProductId(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        File productid = new File(nbCluster, PRODUCT_ID);
        
        FileUtils.deleteFile(productid);
    }
    
    public static FilesList createLicenseAcceptedMarker(File nbLocation, String text) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        File license_accepted = new File(nbCluster, LICENSE_ACCEPTED);
        
        //if (!license_accepted.exists()) {
            return FileUtils.writeFile(license_accepted, text, false);
        //} else {
        //    return new FilesList();
        //}
    }
    
    public static void removeLicenseAcceptedMarker(File nbLocation) throws IOException {
        File nbCluster = getNbCluster(nbLocation);
        
        File license_accepted = new File(nbCluster, LICENSE_ACCEPTED);
        
        if (license_accepted.exists()) {
            FileUtils.deleteFile(license_accepted);
        }
    }
    
    public static FilesList setUsageStatistics(File nbLocation, boolean enabled) throws IOException {
        File file = new File(getNbCluster(nbLocation), CORE_PROPERTIES);
        String prop = USAGE_STATISTICS_ENABLED_PROPERTY + "=" + enabled;
        if (!file.exists()) {
            return FileUtils.writeFile(file,
                    prop);
        } else {
            List<String> list = FileUtils.readStringList(file);
            boolean exist = false;
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                if (s.startsWith(USAGE_STATISTICS_ENABLED_PROPERTY)) {
                    exist = true;
                    if (!s.endsWith("" + enabled)) {
                        list.remove(i);
                        list.add(prop);
                    }
                    break;
                }
            }
            if (!exist) {
                list.add(prop);
            }
            FileUtils.writeStringList(file, list);
            return new FilesList();
        }
    }
    
    public static void setJavaHome(File nbLocation, File javaHome) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctJavaHome = StringUtils.escapeRegExp(javaHome.getAbsolutePath());
        
        contents = contents.replaceAll(
                "#?" + NETBEANS_JDKHOME + "\".*?\"",
                NETBEANS_JDKHOME + "\"" + correctJavaHome + "\"");
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void setUserDir(File nbLocation, File userDir) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        String correctUserDir = StringUtils.escapeRegExp(userDir.getAbsolutePath());
        
        contents = contents.replaceAll(
                NETBEANS_USERDIR +
                "\".*?\"",
                NETBEANS_USERDIR +
                "\"" + correctUserDir + "\"");
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static String getJvmOption(File nbLocation, String name) throws IOException {
        return getJvmOption(nbLocation, name, "=");
    }
    
    public static String getJvmOption(File nbLocation, String name, String separator) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        final String pattern = StringUtils.format(
                NETBEANS_OPTIONS_PATTERN,
                StringUtils.escapeRegExp(name),
                StringUtils.escapeRegExp(separator));
        
        final Matcher matcher =
                Pattern.compile(pattern).matcher(FileUtils.readFile(netbeansconf));
        
        if (matcher.find()) {
            String value = matcher.group(4);
            if (value == null) {
                value = matcher.group(5);
            }
            if (value == null) {
                value = matcher.group(6);
            }
            
            return value;
        } else {
            return null;
        }
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name) throws IOException {
        setJvmOption(nbLocation, name, null);
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name, String value) throws IOException {
        setJvmOption(nbLocation, name, value);
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name, String value, boolean quote) throws IOException {
        setJvmOption(nbLocation, name, value, quote);
    }
    
    @Deprecated
    public static void addJvmOption(File nbLocation, String name, String value, boolean quote, String separator) throws IOException {
        setJvmOption(nbLocation, name, value, quote, separator);
    }
    
    public static void setJvmOption(File nbLocation, String name) throws IOException {
        setJvmOption(nbLocation, name, null, false);
    }
    
    public static void setJvmOption(File nbLocation, String name, String value) throws IOException {
        setJvmOption(nbLocation, name, value, false);
    }
    
    public static void setJvmOption(File nbLocation, String name, String value, boolean quote) throws IOException {
        setJvmOption(nbLocation, name, value, quote, "=");
    }
    
    public static void setJvmOption(File nbLocation, String name, String value, boolean quote, String separator) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        final String option = "-J" + name + (value != null ?
            separator + (quote ? "\\\"" : "") + value + (quote ? "\\\"" : "") : "");
        
        final String pattern = StringUtils.format(
                NETBEANS_OPTIONS_PATTERN,
                StringUtils.escapeRegExp(name),
                StringUtils.escapeRegExp(separator));
        
        String contents = FileUtils.readFile(netbeansconf);
        final Matcher matcher =
                Pattern.compile(pattern).matcher(contents);
        
        if (matcher.find()) {
            contents = contents.replace(matcher.group(3), option);
        } else {
            contents = contents.replace(
                    NETBEANS_OPTIONS + "\"",
                    NETBEANS_OPTIONS + "\"" + option + " ");
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    public static void removeJvmOption(File nbLocation, String name) throws IOException {
        removeJvmOption(nbLocation, name, "=");
    }
    
    public static void removeJvmOption(File nbLocation, String name, String separator) throws IOException {
        final File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        
        String contents = FileUtils.readFile(netbeansconf);
        
        final String pattern = StringUtils.format(
                NETBEANS_OPTIONS_PATTERN,
                StringUtils.escapeRegExp(name),
                StringUtils.escapeRegExp(separator));
        
        final Matcher matcher =
                Pattern.compile(pattern).matcher(contents);
        
        if (matcher.find()) {
            contents = contents.replace(" " + matcher.group(3), "");
            contents = contents.replace(matcher.group(3) + " ", "");
            contents = contents.replace(matcher.group(3), "");
        }
        
        FileUtils.writeFile(netbeansconf, contents);
    }
    
    /**
     * Get JVM memory value.
     *
     * @param nbLocation NetBeans home directory
     * @param memoryType Memory type that can be one of the following values
     *          <ul><li> <code>MEMORY_XMX</code></li>
     *              <li> <code>MEMORY_XMS</code></li>
     *              <li> <code>MEMORY_XSS</code></li>
     *          </ul>
     * @return The size of memory in bytes. <br>
     *         If there is no such option then return 0;
     */
    public static long getJvmMemorySize(File nbLocation, String memoryType) throws IOException {
        final String size = getJvmOption(nbLocation, memoryType, "");
        
        if (size != null) {
            return getJavaMemorySize(size);
        } else {
            return 0;
        }
    }
    
    /**
     * Get JVM memory value. <br>
     * If value is <i>zero</i> then remove the jvm option from netbeans options<br><br>
     * @param nbLocation NetBeans home directory
     * @param memoryType Memory type that can be one of the following values
     *           <ul><li> <code>MEMORY_XMX</code></li>
     *              <li> <code>MEMORY_XMS</code></li>
     *              <li> <code>MEMORY_XSS</code></li>
     *          </ul>
     * @param value Size of memory to be set
     */
    public static void setJvmMemorySize(File nbLocation, String memoryType, long size) throws IOException {
        setJvmOption(nbLocation, memoryType, formatJavaMemoryString(size), false, "");
    }
    /**
     * Get NetBeans branding cluster directory
     * @throws IOException if the no cluster directory exists
     */
    public static File getNbCluster(File nbLocation) throws IOException {
        File location = new File(nbLocation, "netbeans");//NOI18N
        for (File child: location.listFiles()) {
            LogManager.log("    child = " + child.getName());
            if (child.isDirectory() && child.getName().matches(NB_CLUSTER_PATTERN)) {
                return child;
            }
        }
        throw new IOException(ERROR_BRANDING_CLUSTER_NOT_EXIS_STRING);
    }
    
    /**
     * Get resolved netbeans user directory
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    public static File getNetBeansUserDirFile(File nbLocation) throws IOException {
        String dir = getNetBeansUserDir(nbLocation);
        LogManager.log(ErrorLevel.DEBUG, "System.getProperty(netbeans.default_userdir_root): " + System.getProperty("netbeans.default_userdir_root"));
        if (dir.contains(DEFAULT_USERDIR_ROOT) && System.getProperty("netbeans.default_userdir_root", null) != null) {
            dir = dir.replace(DEFAULT_USERDIR_ROOT, System.getProperty("netbeans.default_userdir_root"));
        }
        if (dir.contains(USER_HOME_TOKEN)) {
            dir = dir.replace(USER_HOME_TOKEN, System.getProperty("user.home"));
        }
        return new File(dir);
    }
    
    /**
     * Get netbeans user directory as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    private static String getNetBeansUserDir(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_USERDIR +
                "\"(.*?)\"").matcher(contents);
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException(StringUtils.format(
                    ERROR_CANNOT_GET_USERDIR_STRING,netbeansconf));
        }
    }
    
    /**
     * Get resolved netbeans cache directory
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default cachedir
     */
    public static File getNetBeansCacheDirFile(File nbLocation) throws IOException {
        String dir = getNetBeansCacheDir(nbLocation);
        LogManager.log(ErrorLevel.DEBUG, "System.getProperty(netbeans.default_cachedir_root): " + System.getProperty("netbeans.default_cachedir_root"));
        if (dir.contains(DEFAULT_CACHEDIR_ROOT) && System.getProperty("netbeans.default_cachedir_root", null) != null) {
            dir = dir.replace(DEFAULT_CACHEDIR_ROOT, System.getProperty("netbeans.default_cachedir_root"));
        }
        if (dir.contains(USER_HOME_TOKEN)) {
            dir = dir.replace(USER_HOME_TOKEN, System.getProperty("user.home"));
        }
        return new File(dir);
    }
    
    /**
     * Get netbeans user directory as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @throws IOException if can`t get netbeans default userdir
     */
    private static String getNetBeansCacheDir(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_CACHEDIR +
                "\"(.*?)\"").matcher(contents);
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException(StringUtils.format(
                    ERROR_CANNOT_GET_USERDIR_STRING,netbeansconf));
        }
    }
    
    /**
     * Get jdkhome as it is written in netbeans.conf
     * @param nbLocation NetBeans home directory
     * @return JDK location
     * @throws IOException if can`t get netbeans_jdkhome value of netbeans.conf
     */
    public static String getJavaHome(File nbLocation) throws IOException {
        File netbeansconf = new File(nbLocation, NETBEANS_CONF);
        String contents = FileUtils.readFile(netbeansconf);
        
        Matcher matcher = Pattern.compile(
                NEW_LINE_PATTERN + SPACES_PATTERN +
                NETBEANS_JDKHOME +
                "\"(.*?)\"").matcher(contents);
        
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            throw new IOException(StringUtils.format(
                    ERROR_CANNOT_GET_JAVAHOME_STRING, netbeansconf));
        }
    }
    
    /**
     * Check if NetBeans is running
     * @param nbLocation NetBeans home directory
     * @return True if NetBeans is running
     * @throws IOException if can`t say for sure whether it is running or not
     */
    public static boolean isNbRunning(File nbLocation) throws IOException {
        return FileUtils.exists(getLockFile(nbLocation));
    }
    
    public static File getLockFile(File nbLocation) throws IOException {
        return new File(getNetBeansUserDirFile(nbLocation), "lock");
    }
    
    /**
     *  Test for running NetBeans IDE.<br>
     *  If the lock file exist - issue a warning but do not throw an exception
     */
    public static boolean warnNetbeansRunning(File nbLocation) {
        try {
            boolean isRunning = isNbRunning(nbLocation);
            if (isRunning) {
                if(!checkedAndRunning.contains(nbLocation)) {
                    checkedAndRunning.add(nbLocation);
                    final String message = ResourceUtils.getString(
                            NetBeansUtils.class,
                            "NU.warning.running"); // NOI18N
                    final String warning = StringUtils.format(
                            message,
                            nbLocation,
                            NetBeansUtils.getLockFile(nbLocation));
                    
                    // uninstallation shouls not run when trying to uninstall
                    // running instance of NB
                    ErrorManager.notifyCritical(warning);
                }
            } else {
                checkedAndRunning.remove(nbLocation);
            }
            
            return isRunning;
        } catch (IOException e) {
            ErrorManager.notifyDebug(
                    "Can`t say for sure if NetBeans is running or not",
                    e);
        }
        
        return false;
    }
    
    public static void updateNetBeansHome(final File nbLocation) throws IOException {
        FileUtils.modifyFile(
                new File(nbLocation, NETBEANS_CONF),
                NETBEANS_HOME_TOKEN,
                nbLocation.getAbsolutePath());
    }
    
    public static void runUpdater(File nbLocation) throws IOException {
        File jdkLocation = new File(getJavaHome(nbLocation));
        LogManager.log("running the NetBeans updater : ");
        LogManager.log("    nbLocation = " + nbLocation);
        LogManager.log("    jdkLocation = " + jdkLocation);
        
        List <File> classes = new ArrayList <File> ();
        List <File> nbDirs = new ArrayList <File> ();
        
        
        File netbeansclusters = new File(nbLocation, NETBEANS_CLUSTERS);
        List<String> list = FileUtils.readStringList(netbeansclusters);
                
        File platformCluster = null;
        
        for(String s : list) {
            String cluster = s.trim();
            if(!cluster.startsWith("#") &&
                    !cluster.equals("etc")) {
                nbDirs.add(new File(nbLocation, cluster));
                if(cluster.startsWith("platform")) {
                    platformCluster= new File(nbLocation, cluster);
                }
            }
        }
        
        String nbDirsString = StringUtils.asString(nbDirs, File.pathSeparator);
        
        LogManager.log("    adding classes to classpath");
        classes.add(new File(platformCluster,
                "lib" + File.separator + "boot.jar"));
        if(!SystemUtils.isMacOS()) {
            classes.add(new File(jdkLocation,
                    "lib" + File.separator + "tools.jar" ));
        }
        classes.add(new File(jdkLocation,
                "lib" + File.separator + "dt.jar"));
        classes.add(new File(platformCluster,
                "modules" + File.separator +
                "ext" + File.separator +
                "updater.jar"));
        String classpath = StringUtils.asString(classes, File.pathSeparator);
        
        String importClassProp = "netbeans.importclass";
        String nbHomeProp = "netbeans.home";
        String nbHome = platformCluster.getPath();
        String nbUserdir = getNetBeansUserDirFile(nbLocation).getPath();
        String nbUserdirProp = "netbeans.user";
        String nbDirsProp = "netbeans.dirs";
        String sysProp ="-D";
        String eq = "=";
        String java = JavaUtils.getExecutable(jdkLocation).getPath();
        LogManager.log("    executing updater...");
        SystemUtils.executeCommand(nbLocation, new String [] {
            java,
            sysProp + importClassProp + eq + UPDATER_CLASSNAME,
            sysProp + nbHomeProp    + eq + nbHome,
            sysProp + nbUserdirProp + eq + nbUserdir,
            sysProp + nbDirsProp    + eq + nbDirsString,
            "-Xms32m", "-Xverify:none", "-Xmx128m",
            "-cp", classpath,
            UPDATER_FRAMENAME, "--nosplash"});
    }
    public static boolean setModuleStatus(File nbLocation, String clusterName, String moduleName, boolean enable) {        
        LogManager.log(ErrorLevel.DEBUG,
                ((enable) ? "... enabling" : "disabling") +
                " module " + moduleName + 
                " in cluster " + clusterName + 
                " at " + nbLocation);
        final File configFile = getConfigFile(nbLocation, clusterName, moduleName);
        if (FileUtils.exists(configFile)) {
            Document doc = null;
            try {
                doc = XMLUtils.loadXMLDocument(configFile);
            } catch (XMLException e) {
                LogManager.log("Cannot load config file", e);
            }
            if (doc != null) {
                for (Element element : XMLUtils.getChildren(doc.getDocumentElement(), "param")) {
                    if (element.getAttribute("name").equals("enabled")) {
                        if (element.getTextContent().equals(Boolean.toString(!enable))) {
                            element.setTextContent(Boolean.toString(enable));
                            try {
                                XMLUtils.saveXMLDocument(doc, configFile);
                                LogManager.log(ErrorLevel.MESSAGE, "... module status changed");
                                return true;
                            } catch (XMLException e) {
                                LogManager.log("... Cannot save config file", e);
                            }
                        } else {
                            LogManager.log(ErrorLevel.MESSAGE, "... module is already set to requested status");
                            return true;
                        }
                        break;
                    }
                }
            }
            LogManager.log(ErrorLevel.MESSAGE,"... module status did not changed");
        } else {
            LogManager.log(ErrorLevel.MESSAGE,"... module config file does not exist at " + configFile);
        }
        return false;
    }
    
    public static Boolean getModuleStatus(File nbLocation, String clusterName, String moduleName) {        
        LogManager.log(ErrorLevel.DEBUG, 
                "... getting status of module " + moduleName + 
                " in cluster " + clusterName + 
                " at " + nbLocation);
        final File configFile = getConfigFile(nbLocation, clusterName, moduleName);
        
        if (FileUtils.exists(configFile)) {
            Document doc = null;
            try {
                doc = XMLUtils.loadXMLDocument(configFile);
            } catch (XMLException e) {
                LogManager.log("Cannot load config file", e);
            }
            if (doc != null) {
                for (Element element : XMLUtils.getChildren(doc.getDocumentElement(), "param")) {
                    if (element.getAttribute("name").equals("enabled")) {
                         return Boolean.valueOf(element.getTextContent());
                }
                }
            }
            LogManager.log(ErrorLevel.MESSAGE,"... cannot get module status");
        } else {
            LogManager.log(ErrorLevel.MESSAGE,"... module config file does not exist");
        }
        return null;
    }
    
    private static File getConfigFile(File nbLocation, String clusterName, String moduleName) {
        File cluster = new File (nbLocation, clusterName);
        return new File(cluster, "config" + File.separator + "Modules" + File.separator + moduleName + ".xml");
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static long getJavaMemorySize(String string) {
        String suffix = string.substring(string.length() - 1);
        
        if(!suffix.matches(DIGITS_PATTERN)) {
            long value = Long.parseLong(string.substring(0, string.length() - 1));
            if(suffix.equalsIgnoreCase("k")) {
                value *= K;
            } else if(suffix.equalsIgnoreCase("m")) {
                value *= M;
            } else if(suffix.equalsIgnoreCase("g")) {
                value *= G;
            } else if(suffix.equalsIgnoreCase("t")) {
                value *= T;
            }
            return value;
        } else {
            return new Long(string).longValue() * M; // default - megabytes
        }
    }
    
    private static String formatJavaMemoryString(long size) {
        if((size > T) && (size % T == 0)) {
            return StringUtils.EMPTY_STRING + (size/T) + "t";
        } else if((size > G) && (size % G == 0)) {
            return StringUtils.EMPTY_STRING + (size/G) + "g";
        } else if((size > M) && (size % M == 0)) {
            return StringUtils.EMPTY_STRING + (size/M) + "m";
        }  else if((size > K) && (size % K == 0)) {
            return StringUtils.EMPTY_STRING + (size/K) + "k";
        } else {
            if(size > (10 * M)) {
                // round up to the nearest M value
                return StringUtils.EMPTY_STRING + (size/M + 1) + "m";
            } else {
                // round up to the nearest K value
                return StringUtils.EMPTY_STRING + (size/K + 1) + "k";
            }
        }
    }
    public static String getNetBeansId() {
        return BundleType.getType(
                System.getProperty(NbWelcomePanel.WELCOME_PAGE_TYPE_PROPERTY)).
                getNetBeansBundleId();
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private NetBeansUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String NETBEANS_CLUSTERS =
            "netbeans/etc/netbeans.clusters"; // NOI18N
    public static final String NETBEANS_CONF =
            "netbeans/etc/netbeans.conf"; // NOI18N
    public static final String PRODUCT_ID =
            "config/productid"; // NOI18N
    public static final String LICENSE_ACCEPTED =
            "var/license_accepted"; // NOI18N
    public static final String CORE_PROPERTIES =
            "config/Preferences/org/netbeans/core.properties";
    public static final String USAGE_STATISTICS_ENABLED_PROPERTY = 
            "usageStatisticsEnabled";//NOI18N
    public static final String DIGITS_PATTERN =
            "[0-9]+"; // NOI18N
    public static final String CLUSTER_NUMBER_PATTERN =
            "(" + DIGITS_PATTERN + "(\\." + DIGITS_PATTERN + ")?)?"; // NOI18N
    
    public static final String NB_CLUSTER_PATTERN =
            "nb" + CLUSTER_NUMBER_PATTERN + "$"; // NOI18N
    public static final String NEW_LINE_PATTERN =
            "[\r\n|\n|\r]"; // NOI18N
    public static final String SPACES_PATTERN =
            "\\ *"; // NOI18N
    
    public static final String NETBEANS_USERDIR =
            "netbeans_default_userdir="; // NOI18N
    public static final String NETBEANS_CACHEDIR =
            "netbeans_default_cachedir="; // NOI18N
    public static final String NETBEANS_JDKHOME =
            "netbeans_jdkhome="; // NOI18N
    public static final String NETBEANS_OPTIONS =
            "netbeans_default_options="; // NOI18N
    
    public static final String NETBEANS_OPTIONS_PATTERN =
            NETBEANS_OPTIONS + "\"(.*?)( ?)(-J{0}(?:{1}\\\\\\\"(.*?)\\\\\\\"|{1}(.*?)|())(?= |\"))( ?)(.*)?\"";

    public static final String LAST_MODIFIED_MARKER =
           ".lastModified";
    public static final String UPDATE_TRACKING_DIR =
            "update_tracking";
    public static final String NB_IDE_ID =
            "NB"; // NOI18N
    public static final String PACK_ID_SEPARATOR =
            "_"; // NOI18N
    public static final String MEMORY_XMX =
            "-Xmx"; // NOI18N
    public static final String MEMORY_XMS =
            "-Xms"; // NOI18N
    public static final String MEMORY_XSS =
            "-Xss"; // NOI18N
    public static final String USER_HOME_TOKEN =
            "${HOME}"; // NOI18N
    public static final String DEFAULT_USERDIR_ROOT =
            "${DEFAULT_USERDIR_ROOT}"; // NOI18N
    public static final String DEFAULT_CACHEDIR_ROOT =
            "${DEFAULT_CACHEDIR_ROOT}"; // NOI18N
    public static final String NETBEANS_HOME_TOKEN =
            "${NETBEANS_HOME}"; // NOI18N
    public static final String UPDATER_FRAMENAME = 
            "org.netbeans.updater.UpdaterFrame";
    public static final String UPDATER_CLASSNAME = 
            "org.netbeans.upgrade.AutoUpgrade";
    public static final String ERROR_BRANDING_CLUSTER_NOT_EXIS_STRING =
            ResourceUtils.getString(NetBeansUtils.class,
            "NU.error.branding.cluster.not.exists");//NOI18N
    public static final String ERROR_CANNOT_GET_USERDIR_STRING =
            ResourceUtils.getString(NetBeansUtils.class,
            "NU.error.cannot.get.userdir");//NOI18N
    public static final String ERROR_CANNOT_GET_JAVAHOME_STRING =
            ResourceUtils.getString(NetBeansUtils.class,
            "NU.error.cannot.get.javahome");//NOI18N
    
    public static final long K =
            1024; // NOMAGI
    public static final long M =
            K * K;
    public static final long G =
            M * K;
    public static final long T =
            G * K;
    // one set for the whole installer
    private static Set <File> checkedAndRunning = new HashSet <File> ();
    
    private static final String MEMORY_SUFFIX_PATTERN =
            "[kKmMgGtT]?"; // NOI18N
}
