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
package org.netbeans.modules.subversion.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientFactory.ConnectionType;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.NetworkSettings;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * Handles the Subversions <b>servers</b> and <b>config</b> configuration files.</br>
 * Everytime the singleton instance is created are the values from the commandline clients
 * configuration directory merged into the Subversion modules configuration files. 
 * (registry on windows are ignored). 
 * Already present proxy setting values wan't be changed, 
 * the remaining values are always taken from the commandline clients configuration files. 
 * The only exception is the 'store-auth-creds' key, which is always set to 'no'.
 * 
 * @author Tomas Stupka
 */
public class SvnConfigFiles {

    /** the only SvnConfigFiles instance */
    private static SvnConfigFiles instance;

    /** the Ini instance holding the configuration values stored in the <b>servers</b>
     * file used by the Subversion module */    
    private Ini svnServers = null;
    
    /** the Ini instance holding the configuration values stored in the <b>config</b>
     * file used by the Subversion module */
    private Ini config = null;

    private static final String UNIX_CONFIG_DIR = ".subversion/";                                                               // NOI18N
    private static final String GROUPS_SECTION = "groups";                                                                      // NOI18N
    private static final String GLOBAL_SECTION = "global";                                                                      // NOI18N
    private static final String WINDOWS_USER_APPDATA = getAPPDATA();
    private static final String WINDOWS_CONFIG_DIR = WINDOWS_USER_APPDATA + "\\Subversion";                                     // NOI18N
    private static final String WINDOWS_GLOBAL_CONFIG_DIR = getGlobalAPPDATA() + "\\Subversion";                                // NOI18N
    private static final List<String> DEFAULT_GLOBAL_IGNORES = 
            parseGlobalIgnores("*.o *.lo *.la #*# .*.rej *.rej .*~ *~ .#* .DS_Store");                                          // NOI18N
    private static final boolean DO_NOT_SAVE_PASSPHRASE = Boolean.getBoolean("versioning.subversion.noPassphraseInConfig"); // NOI18N

    private String recentUrl;

    private interface IniFilePatcher {
        void patch(Ini file);
    }

    /**
     * The value for the 'store-auth-creds' key in the config configuration file is alway set to 'no'
     * so the commandline client wan't create a file holding the authentication credentials when
     * a svn command is called. The reason for this is that the Subverion module holds the credentials
     * in files with the same format as the commandline client but with a different name.
     *
     * Also sets password-stores to empty value. We currently handle password stores poorly and occasionally non-empty values cause a deadlock (see #178122).
     */
    private class ConfigIniFilePatcher implements IniFilePatcher {
        @Override
        public void patch(Ini file) {
            // patch store-auth-creds to "no"
            Ini.Section auth = (Ini.Section) file.get("auth");                  // NOI18N
            if(auth == null) {
                auth = file.add("auth");                                        // NOI18N
            }
            auth.put("store-auth-creds", "yes");                                // NOI18N
            auth.put("store-passwords", "no");                                  // NOI18N
            auth.put("password-stores", "");                                    // NOI18N
        }
    }

    /**
     * Creates a new instance
     */
    private SvnConfigFiles() {      
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Subversion.class.getClassLoader());
        try {
            Config.getGlobal().setEscape(false); // do not escape characters
            // copy config file
            config = copyConfigFileToIDEConfigDir("config", new ConfigIniFilePatcher());    // NOI18N
            // get the system servers file
            svnServers = loadSystemIniFile("servers");
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
//        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }
    
    /**
     * Returns a singleton instance.
     *
     * @return the SvnConfigFiles instance
     */
    public static synchronized SvnConfigFiles getInstance() {
        //T9Y - singleton is not required - always create new instance of this class
        String t9yUserConfigPath = System.getProperty("netbeans.t9y.svn.user.config.path");
        if (t9yUserConfigPath != null && t9yUserConfigPath.length() > 0) {
            //make sure that new instance will be created
            instance = null;
        }
        
        if(instance == null) {
            instance = new SvnConfigFiles();                    
        }
        return instance;
    }

    public void reset() {
        recentUrl = null; // force rewrite
    }

    /**
     * Stores the cert file and password, proxy host, port, username and password for the given
     * {@link SVNUrl} in the
     * <b>servers</b> file used by the Subversion module.  
     * 
     * It returns an instance of config file that should be deleted as soon as possible
     * because it contains sensitive private data such as passwords or passphrases.
     *     
     * @param host the host
     */
    public File storeSvnServersSettings(SVNUrl url, ConnectionType connType) {
                        
        assert url != null : "can\'t do anything for a null host";     // NOI18N
        File sensitiveConfigFile = null;
                         
        if(!(url.getProtocol().startsWith("http") ||                    //NOI18N
             url.getProtocol().startsWith("https") ||                   //NOI18N
             url.getProtocol().startsWith("svn+")) )                    //NOI18N
        {            
            // we need the settings only for remote http and https repositories
            return sensitiveConfigFile;
        }

        boolean changes = false;
        Ini nbServers = new Ini();   
        Ini.Section nbGlobalSection = nbServers.add(GLOBAL_SECTION);

        String repositoryUrl = url.toString();
        changes = !repositoryUrl.equals(recentUrl);

        if(changes) {
            RepositoryConnection rc = SvnModuleConfig.getDefault().getRepositoryConnection(repositoryUrl);
            if (rc != null && url.getProtocol().startsWith("svn+")) {   //NOI18N
                // must set tunnel info for the repository url
                if (connType == ConnectionType.svnkit) {
                    // hack for svnkit and ssh
                    // ssh port is read only from ssh tunnel info and considered valid only when usernam and password are not empty
                    // see implementation in SvnKit: org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager.getDefaultSSHAuthentication()
                    // weird and ugly
                    setExternalCommand("ssh", rc.getSshPortNumber() > 0 ? "ssh -p " + rc.getSshPortNumber() + " -P " + rc.getSshPortNumber() + " -l user -pw password" : "");
                    nbGlobalSection.put("store-auth-creds", "yes");                                // NOI18N
                    nbGlobalSection.put("store-passwords", "no");                                  // NOI18N
                } else {
                    setExternalCommand(SvnUtils.getTunnelName(url.getProtocol()), rc.getExternalCommand());
                }
            }
            boolean hasPassphrase = false;
            if(url.getProtocol().startsWith("https")) {
                hasPassphrase = setSSLCert(rc, nbGlobalSection);
            }
            hasPassphrase = setProxy(url, nbGlobalSection) | hasPassphrase;
            File configFile = storeIni(nbServers, "servers"); //NOI18N
            recentUrl = url.toString();
            if (hasPassphrase) {
                sensitiveConfigFile = configFile;
                recentUrl = null; //must be regenerated on next run
            }
        }
        return sensitiveConfigFile;
    }

    private boolean setSSLCert(RepositoryConnection rc, Ini.Section nbGlobalSection) {
        if(rc == null) {
            return false;
        }
        String certFile = rc.getCertFile();
        if(certFile == null || certFile.equals("")) {
            return false;
        }
        char[] certPasswordChars = rc.getCertPassword();
        String certPassword = certPasswordChars == null ? "" : new String(certPasswordChars); //NOI18N
        if(certPassword.equals("")) { // NOI18N
            return false;
        }
        nbGlobalSection.put("ssl-client-cert-file", certFile);
        if (!DO_NOT_SAVE_PASSPHRASE) {
            nbGlobalSection.put("ssl-client-cert-password", certPassword);
            return true;
        }
        return false;
    }

    private boolean setProxy(SVNUrl url, Ini.Section nbGlobalSection) {
        String host =  SvnUtils.ripUserFromHost(url.getHost());        
        Ini.Section svnGlobalSection = svnServers.get(GLOBAL_SECTION);
        URI uri = null;
        boolean passwordAdded = false;
        try {
            uri = new URI(url.toString());
        } catch (URISyntaxException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
            return passwordAdded;
        }
        String proxyHost = NetworkSettings.getProxyHost(uri);
        // check DIRECT connection
        if(proxyHost != null && proxyHost.length() > 0) {
            String proxyPort = NetworkSettings.getProxyPort(uri);
            assert proxyPort != null;
            nbGlobalSection.put("http-proxy-host", proxyHost);                     // NOI18N
            nbGlobalSection.put("http-proxy-port", proxyPort);                     // NOI18N

            // and the authentication
            String username = NetworkSettings.getAuthenticationUsername(uri);
            if(username != null) {
                String password = getProxyPassword(NetworkSettings.getKeyForAuthenticationPassword(uri));

                nbGlobalSection.put("http-proxy-username", username);                               // NOI18N
                nbGlobalSection.put("http-proxy-password", password);                               // NOI18N
                passwordAdded = true;
            }
        }
        // check if there are also some no proxy settings
        // we should get from the original svn servers file
        mergeNonProxyKeys(host, svnGlobalSection, nbGlobalSection);
        return passwordAdded;
    }

    private void mergeNonProxyKeys(String host, Ini.Section svnGlobalSection, Ini.Section nbGlobalSection) {                             
        if(svnGlobalSection != null) {
            // if there is a global section, than get the no proxy settings                                                                 
            mergeNonProxyKeys(svnGlobalSection, nbGlobalSection);
        }
        Ini.Section svnHostGroup = getServerGroup(host);
        if(svnHostGroup != null) {
            // if there is a section for the given host, than get the no proxy settings                                                                 
            mergeNonProxyKeys(svnHostGroup, nbGlobalSection);                
        }                                
    }
    
    private void mergeNonProxyKeys(Ini.Section source, Ini.Section target) {
        for (String key : source.keySet()) {
            if(!isProxyConfigurationKey(key)) {
                target.put(key, source.get(key));                                                
            }                    
        }
    }
    
    public void setExternalCommand(String tunnelName, String command) {
        if (command == null) {
            return;
        }
        if (Utilities.isWindows()) {
            // tunnel command should contain forward slashes even on windows
            command = command.replace("\\", "/");                       //NOI18N
        }
        Ini.Section tunnels = getSection(config, "tunnels", true);
        tunnels.put(tunnelName, command);
        storeIni(config, "config");                                                     // NOI18N
    }

    public String getExternalCommand(String tunnelName) {
        Ini.Section tunnels = getSection(config, "tunnels", true);
        String cmd = tunnels.get(tunnelName);
        return cmd != null ? cmd : "";        
    }
    
    private Ini.Section getSection(Ini ini, String key, boolean create) {
        Ini.Section section = ini.get(key);
        if(section == null) {
            return ini.add(key);
        }
        return section;
    }
    
    private File storeIni (Ini ini, String iniFile) {
        BufferedOutputStream bos = null;
        File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/" + iniFile));   // NOI18N
        try {
            file.getParentFile().mkdirs();
            ini.store(bos = FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);            
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ex) {
                    Subversion.LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return file;
    }    

    /**
     * Returns the miscellany/global-ignores setting from the config file.
     *
     * @return a list with the inore patterns
     *
     */
    public List<String> getGlobalIgnores() {
        Ini.Section miscellany = config.get("miscellany");                      // NOI18N
        if (miscellany != null) {
            String ignores = miscellany.get("global-ignores");                  // NOI18N
            if (ignores != null && ignores.trim().length() > 0) {
                return parseGlobalIgnores(ignores);
            }
        }
        return DEFAULT_GLOBAL_IGNORES;
    }

    public String getClientCertFile(String host) {
        return getMergeValue("ssl-client-cert-file", host);                     // NOI18N
    }

    public String getClientCertPassword(String host) {
        return getMergeValue("ssl-client-cert-password", host);                 // NOI18N    
    }
    
    private String getMergeValue(String key, String host) {
        Ini.Section group = getServerGroup(host);
        if(group != null) {
            return group.get(key);
        }
        group = svnServers.get(GLOBAL_SECTION);
        if(group != null) {
            return group.get(key);
        }
        return null;
    }
    
    private static List<String> parseGlobalIgnores(String ignores) {
        StringTokenizer st = new StringTokenizer(ignores, " ");                 // NOI18N
        List<String> ret = new ArrayList<String>(10);
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!entry.equals(""))                                              // NOI18N
                ret.add(entry);
        }
        return ret;
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the systems Subversion commandline client.
     *
     * @return the path
     *
     */ 
    public static String getUserConfigPath() {        
        
        //T9Y - user svn config files should be changable
        String t9yUserConfigPath = System.getProperty("netbeans.t9y.svn.user.config.path");
        if (t9yUserConfigPath != null && t9yUserConfigPath.length() > 0) {
            return t9yUserConfigPath;
        }
        
        if(Utilities.isUnix()) {
            String path = System.getProperty("user.home") ;                     // NOI18N
            return path + "/" + UNIX_CONFIG_DIR;                                // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_CONFIG_DIR;
        } 
        return "";                                                              // NOI18N
    }

    /**
     * Returns the path for the Sunbversion configuration directory used 
     * by the Netbeans Subversion module.
     *
     * @return the path
     *
     */ 
    public static String getNBConfigPath() {
        
        //T9Y - nb svn confing should be changable
        String t9yNbConfigPath = System.getProperty("netbeans.t9y.svn.nb.config.path");
        if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
            return t9yNbConfigPath;
        }
        
        String nbHome = Places.getUserDirectory().getAbsolutePath();
        return nbHome + "/config/svn/config/";                                  // NOI18N
    }
    
    /**
     * Returns the section from the <b>servers</b> config file used by the Subversion module which 
     * is holding the proxy settings for the given host
     *
     * @param host the host
     * @return the section holding the proxy settings for the given host
     */ 
    private Ini.Section getServerGroup(String host) {
        if(host == null || host.equals("")) {                                   // NOI18N
            return null;
        }
        Ini.Section groups = svnServers.get(GROUPS_SECTION);
        if(groups != null) {
            for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                String value = groups.get(key);
                if(value != null) {     
                    value = value.trim();                    
                    if(value != null && match(value, host)) {
                        return svnServers.get(key);
                    }      
                }
            }
        }
        return null;
    }
       
    /**
     * Evaluates if the given hostaname or IP address is in the given value String.
     *
     * @param value the value String. A list of host names or IP addresses delimited by ",". 
     *                          (e.g 192.168.0.1,*.168.0.1, some.domain.com, *.anything.com, ...)
     * @param host the hostname or IP address
     * @return true if the host name or IP address was found in the values String, otherwise false.
     */
    private boolean match(String value, String host) {                    
        String[] values = value.split(",");                                     // NOI18N
        for (int i = 0; i < values.length; i++) {
            value = values[i].trim();

            if(value.equals("*") || value.equals(host) ) {                      // NOI18N
                return true;
            }

            int idx = value.indexOf("*");                                       // NOI18N
            if(idx > -1 && matchSegments(value, host) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates if the given hostaname or IP address matches with the given value String representing 
     * a hostaname or IP adress with one or more "*" wildcards in it.
     *
     * @param value the value String. A host name or IP addresse with a "*" wildcard. (e.g *.168.0.1 or *.anything.com)
     * @param host the hostname or IP address
     * @return true if the host name or IP address matches with the values String, otherwise false.
     */
    private boolean matchSegments(String value, String host) {
        value = value.replace(".", "\\.");
        value = value.replace("*", ".*");
        Matcher m = Pattern.compile(value).matcher(host);
        return m.matches();
    }

    /**
     * Copies the given configuration file from the Subversion commandline client
     * configuration directory into the configuration directory used by the Netbeans Subversion module. </br>
     */
    private Ini copyConfigFileToIDEConfigDir(String fileName, IniFilePatcher patcher) {
        Ini systemIniFile = loadSystemIniFile(fileName);

        patcher.patch(systemIniFile);

        File file = FileUtil.normalizeFile(new File(getNBConfigPath() + File.separatorChar + fileName)); // NOI18N
        BufferedOutputStream bos = null;
        try {
            file.getParentFile().mkdirs();
            systemIniFile.store(bos = FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex)     ; // should not happen
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ex) {
                    Subversion.LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return systemIniFile;
    }

    /**
     * Loads the ini configuration file from the directory used by 
     * the Subversion commandline client. The settings are loaded and merged together in 
     * in the folowing order:
     * <ol>
     *  <li> The per-user INI files
     *  <li> The system-wide INI files
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the configuration file. 
     */       
    private Ini loadSystemIniFile(String fileName) {
        // config files from userdir
        String filePath = getUserConfigPath() + "/" + fileName;                         // NOI18N
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            // ignore
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex)     ;
        } catch (Exception ex) {
            Subversion.LOG.log(Level.INFO, "exception in Ini4j, system file not loaded: " + filePath, ex);
        }

        if(system == null) {
            system = new Ini();
            Subversion.LOG.warning("Could not load the file " + filePath + ". Falling back on svn defaults."); // NOI18N
        }
        
        Ini global = null;      
        try {
            global = new Ini(new FileReader(getGlobalConfigPath() + "/" + fileName));   // NOI18N
        } catch (FileNotFoundException ex) {
            // just doesn't exist - ignore
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex)     ;
        } catch (Exception ex) {
            Subversion.LOG.log(Level.INFO, "exception in Ini4j, global file not loaded: " + getGlobalConfigPath() + "/" + fileName, ex);
        }
         
        if(global != null) {
            merge(global, system);
        }                
        return system;
    }

    /**
     * Merges only sections/keys/values into target which are not already present in source
     * 
     * @param source the source ini file
     * @param target the target ini file in which the values from the source file are going to be merged
     */
    private void merge(Ini source, Ini target) {
        for (Iterator<String> itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = itSections.next();
            Ini.Section sourceSection = source.get( sectionName );
            Ini.Section targetSection = target.get( sectionName );

            if(targetSection == null) {
                targetSection = target.add(sectionName);
            }

            for (Iterator<String> itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = itVariables.next();

                if(!targetSection.containsKey(key)) {
                    targetSection.put(key, sourceSection.get(key));
                }
            }            
        }
    }
   
    /**
     * Evaluates if the value stored under the key is a proxy setting value.
     *
     * @param key the key
     * @return true if the value stored under the key is a proxy setting value. Otherwise false
     */
    private boolean isProxyConfigurationKey(String key) {
        return key.equals("http-proxy-host")     || // NOI18N
               key.equals("http-proxy-port")     || // NOI18N
               key.equals("http-proxy-username") || // NOI18N
               key.equals("http-proxy-password") || // NOI18N
               key.equals("http-proxy-exceptions"); // NOI18N        
    }
    
    /**
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/subversion";               // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_GLOBAL_CONFIG_DIR;
        } 
        return "";                                  // NOI18N
    }

    /**
     * Returns the value for the %APPDATA% env variable on windows
     *
     */
    private static String getAPPDATA() {
        String appdata = "";
        if(Utilities.isWindows()) {
            appdata = System.getenv("APPDATA");// NOI18N
        }
        return appdata!= null? appdata: "";
    }

    /**
     * Returns the value for the %ALLUSERSPROFILE% + the last foder segment from %APPDATA% env variables on windows
     *
     */
    private static String getGlobalAPPDATA() {
        if(Utilities.isWindows()) {
            String globalProfile = System.getenv("ALLUSERSPROFILE");                                // NOI18N
            if(globalProfile == null || globalProfile.trim().equals("")) {                          // NOI18N
                globalProfile = "";
            }
            String appdataPath = WINDOWS_USER_APPDATA;
            if(appdataPath == null || appdataPath.equals("")) {                                     // NOI18N
                return "";                                                                          // NOI18N
            }                                                                                       // NOI18N
            return getWinUserAppdata(appdataPath, globalProfile);                                          // NOI18N
        }
        return "";                                                                                  // NOI18N
    }

    private static String getWinUserAppdata(String appdataPath, String globalProfile) {
        appdataPath = trimBackslash(appdataPath);
        globalProfile = trimBackslash(globalProfile);
        String appdata = "";                                                                        // NOI18N
        int idx = appdataPath.lastIndexOf("\\");                                                    // NOI18N
        if (idx > -1) {
            appdata = appdataPath.substring(idx + 1);
            if (appdata.trim().equals("")) {                                                        // NOI18N
                int previdx = appdataPath.lastIndexOf("\\", idx);                                   // NOI18N
                if (idx > -1) {
                    appdata = appdataPath.substring(previdx + 1, idx);
                }
            }
        } else {
            return "";                                                                              // NOI18N
        }
        if(globalProfile.endsWith("\\")) {                                                          // NOI18N
            globalProfile = globalProfile.substring(0, globalProfile.length() - 1);
        }
        return globalProfile + "/" + appdata;                                                       // NOI18N
    }

    private static String trimBackslash(String appdataPath) {
        if (appdataPath.endsWith("\\")) {
            // NOI18N
            appdataPath = appdataPath.substring(0, appdataPath.length() - 1);
        }
        return appdataPath;
    }

    private String getProxyPassword(String key) {
        char[] pwd = KeyringSupport.read("", key);
        return pwd == null ? "" : new String(pwd); //NOI18N
    }
    
}
