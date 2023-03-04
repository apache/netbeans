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

package org.netbeans.modules.mercurial.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;
import java.util.logging.Level;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * Handles the Mercurial <b>hgrc</b> configuration file.</br>
 *
 * @author Padraig O'Briain
 */
public class HgConfigFiles {    
    public static final String HG_EXTENSIONS = "extensions";  // NOI18N
    public static final String HG_EXTENSIONS_HGK = "hgext.hgk";  // NOI18N
    public static final String HG_EXTENSIONS_FETCH = "fetch";  // NOI18N
    public static final String HG_UI_SECTION = "ui";  // NOI18N
    public static final String HG_USERNAME = "username";  // NOI18N
    public static final String HG_PATHS_SECTION = "paths";  // NOI18N
    public static final String HG_DEFAULT_PUSH = "default-push";  // NOI18N
    public static final String HG_DEFAULT_PUSH_VALUE = "default-push";  // NOI18N
    public static final String HG_DEFAULT_PULL = "default-pull";  // NOI18N
    public static final String HG_DEFAULT_PULL_VALUE = "default";  // NOI18N

    /** The HgConfigFiles instance for user and system defaults */
    private static HgConfigFiles instance;

    /** the Ini instance holding the configuration values stored in the <b>hgrc</b>
     * file used by the Mercurial module */    
    private Ini hgrc = null;
    
    /** The repository directory if this instance is for a repository */
    private File dir;
    public static final String HG_RC_FILE = "hgrc";                                                                       // NOI18N
    public static final String HG_REPO_DIR = ".hg";                                                                       // NOI18N

    private static final String WINDOWS_HG_RC_FILE = "Mercurial.ini";                                 // NOI18N
    private static final String WINDOWS_DEFAULT_MECURIAL_INI_PATH = "C:\\Mercurial\\Mercurial.ini";                                 // NOI18N
    private boolean bIsProjectConfig;
    private IOException initException;
    /**
     * fileName of the configuration file
     */
    private String configFileName;
    /**
     * Creates a new instance
     */
    private HgConfigFiles() {
        bIsProjectConfig = false;
        // get the system hgrc file
        Config.getGlobal().setEscape(false); // escaping characters disabled
        if(Utilities.isWindows()) {
            // on windows both Mercurial.ini and .hgrc are allowed
            hgrc = loadSystemAndGlobalFile(new String[] {WINDOWS_HG_RC_FILE, "." + HG_RC_FILE}); //NOI18N
        } else {
            hgrc = loadSystemAndGlobalFile(new String[] {HG_RC_FILE});
        }
    }
    
    /**
     * Returns a singleton instance
     *
     * @return the HgConfiles instance
     */
    public static HgConfigFiles getSysInstance() {
        if (instance==null) {
            instance = new HgConfigFiles();
        }
        return instance;
    }

    public HgConfigFiles(File file) {
        Config.getGlobal().setEscape(false); // escaping characters disabled
        bIsProjectConfig = true;
        dir = file;        
        // <repository>/.hg/hgrc on all platforms
        hgrc = loadRepoHgrcFile(file);
    }

    public IOException getException () {
        return initException;
    }
 
    public void setProperty(String name, String value) {
        if (name.equals(HG_USERNAME)) { 
            setProperty(HG_UI_SECTION, HG_USERNAME, value);
        } else if (name.equals(HG_DEFAULT_PUSH)) { 
            setProperty(HG_PATHS_SECTION, HG_DEFAULT_PUSH_VALUE, removeTrailingBackslahes(value));
            HgRepositoryContextCache.getInstance().reset();
        } else if (name.equals(HG_DEFAULT_PULL)) { 
            setProperty(HG_PATHS_SECTION, HG_DEFAULT_PULL_VALUE, removeTrailingBackslahes(value));
            HgRepositoryContextCache.getInstance().reset();
        } else if (name.equals(HG_EXTENSIONS_HGK)) { 
            // Allow hgext.hgk to be set to some other user defined value if required
            if(getProperty(HG_EXTENSIONS, HG_EXTENSIONS_HGK).equals("")){
                setProperty(HG_EXTENSIONS, HG_EXTENSIONS_HGK, value, true);
            }
        } else if (name.equals(HG_EXTENSIONS_FETCH)) { 
            // Allow fetch to be set to some other user defined value if required
            if(getProperty(HG_EXTENSIONS, HG_EXTENSIONS_FETCH).equals("")){ 
                setProperty(HG_EXTENSIONS, HG_EXTENSIONS_FETCH, value, true);
            }
        }

    }
 
    public void setProperty(String section, String name, String value, boolean allowEmpty) {
        if (!allowEmpty) {
            if (value.length() == 0) {
                removeProperty(section, name);
            } else {
                Ini.Section inisection = getSection(hgrc, section, true);
                inisection.put(name, value);
            }
        } else {
            Ini.Section inisection = getSection(hgrc, section, true);
            inisection.put(name, value);
        }
        if (!bIsProjectConfig && Utilities.isWindows()) {
            storeIni(hgrc, configFileName);
        } else {
            storeIni(hgrc, configFileName);
        }
    }

    public void setProperty(String section, String name, String value) {
        setProperty(section, name,value, false);
    }

    public void setUserName(String value) {
        setProperty(HG_UI_SECTION, HG_USERNAME, value);
    }

    public String getSysUserName() {
        return getUserName(true);
    }

    public String getSysPushPath() {
        return getDefaultPush(true);
    }

    public String getSysPullPath() {
        return getDefaultPull(true);
    }

    public Properties getProperties(String section) {
        Ini.Section inisection = getSection(hgrc, section, false);
        Properties props = new Properties();
        if (inisection != null) {
            Set<String> keys = inisection.keySet();
            for (String key : keys) {
                props.setProperty(key, inisection.get(key));
            }
        }
        return props;
    }

    public void clearProperties(String section) {
        Ini.Section inisection = getSection(hgrc, section, false);
        if (inisection != null) {
             inisection.clear();
            if (!bIsProjectConfig && Utilities.isWindows()) {
                storeIni(hgrc, configFileName);
            } else {
                storeIni(hgrc, configFileName);
            }
         }
    }

    public void removeProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, false);
        if (inisection != null) {
             inisection.remove(name);
            if (!bIsProjectConfig && Utilities.isWindows()) {
                storeIni(hgrc, configFileName);
            } else {
                storeIni(hgrc, configFileName);
            }
         }
    }

    public String getDefaultPull(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty(HG_PATHS_SECTION, HG_DEFAULT_PULL_VALUE); 
    }

    public String getDefaultPush(Boolean reload) {
        if (reload) {
            doReload();
        }
        String value = getProperty(HG_PATHS_SECTION, HG_DEFAULT_PUSH); 
        if (value.length() == 0) {
            value = getProperty(HG_PATHS_SECTION, HG_DEFAULT_PULL_VALUE); 
        }
        return value;
    }

    public String getUserName(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty(HG_UI_SECTION, HG_USERNAME);                                              
    }

    public String getProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, true);
        String value = inisection.get(name);
        return value != null ? value : "";        // NOI18N 
    }
    
    public boolean containsProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, true);
        return inisection.containsKey(name);
    }

    public void doReload () {
        if (dir == null) {
            if(!bIsProjectConfig && Utilities.isWindows()) {
                // on windows both Mercurial.ini and .hgrc are allowed
                hgrc = loadSystemAndGlobalFile(new String[] {WINDOWS_HG_RC_FILE, "." + HG_RC_FILE}); //NOI18N
            } else {
                hgrc = loadSystemAndGlobalFile(new String[] {HG_RC_FILE});
            }
        } else {
            hgrc = loadRepoHgrcFile(dir);                                      
        }
    }

    private Ini.Section getSection(Ini ini, String key, boolean create) {
        Ini.Section section = ini.get(key);
        if(section == null && create) {
            return ini.add(key);
        }
        return section;
    }
    
    private void storeIni(Ini ini, String iniFile) {
        assert initException == null;
        BufferedOutputStream bos = null;
        try {
            String filePath;
            if (dir != null) {
                filePath = dir.getAbsolutePath() + File.separator + HG_REPO_DIR + File.separator + iniFile; // NOI18N 
            } else {
                filePath =  getUserConfigPath() + iniFile;
            }
            File file = FileUtil.normalizeFile(new File(filePath));
            file.getParentFile().mkdirs();
            ini.store(bos = new BufferedOutputStream(new FileOutputStream(file)));
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                }
            }
        }
    }

    /**
     * Returns a default ini instance, with classloader issue workaround (#141364)
     * @return ini instance
     */
    private Ini createIni () {
        return createIni(null);
    }

    /**
     * Returns an ini instance for given file, with classloader issue workaround (#141364)
     * @param file ini file being parsed. If null then a default ini will be created.
     * @return ini instance for the given file
     */
    private Ini createIni (File file) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Mercurial.class.getClassLoader());
        try {
            if (file == null) {
                return new Ini();
            } else {
                return new Ini(new FileReader(file));
            }
        } catch (FileNotFoundException ex) {
            // ignore
        } catch (InvalidFileFormatException ex) {
            Mercurial.LOG.log(Level.INFO, "Cannot parse configuration file", ex); // NOI18N
            initException = ex;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        } catch (Exception ex) {
            Mercurial.LOG.log(Level.INFO, "Cannot parse configuration file", ex); // NOI18N
            initException = new IOException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return null;
    }

    /**
     * Loads Repository configuration file  <repo>/.hg/hgrc on all platforms
     * */
    private Ini loadRepoHgrcFile(File dir) {
        String filePath = dir.getAbsolutePath() + File.separator + HG_REPO_DIR + File.separator + HG_RC_FILE; // NOI18N
        configFileName = HG_RC_FILE;
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        system = createIni(file);
        
        if(system == null) {
            system = createIni();
            Mercurial.LOG.log(Level.FINE, "Could not load the file " + filePath + ". Falling back on hg defaults."); // NOI18N
        }
        return system;
    }
    /**
     * Loads user and system configuration files 
     * The settings are loaded and merged together in the folowing order:
     * <ol>
     *  <li> The per-user configuration file, Unix: ~/.hgrc, Windows: %USERPROFILE%\Mercurial.ini
     *  <li> The system-wide file, Unix: /etc/mercurial/hgrc, Windows: Mercurial_Install\Mercurial.ini
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the configuration file. 
     */       
    private Ini loadSystemAndGlobalFile(String[] fileNames) {
        // config files from userdir
        Ini system = null;
        for (String userConfigFileName : fileNames) {
            String filePath = getUserConfigPath() + userConfigFileName;
            File file = FileUtil.normalizeFile(new File(filePath));
            system = createIni(file);
            if (system != null) {
                configFileName = userConfigFileName;
                break;
            }
            Mercurial.LOG.log(Level.INFO, "Could not load the file {0}.", filePath); //NOI18N
        }
        
        if(system == null) {
            configFileName = fileNames[0];
            system = createIni();
            Mercurial.LOG.log(Level.INFO, "Could not load the user config file. Falling back on hg defaults."); //NOI18N
        }
        
        Ini global = null;
        File gFile = FileUtil.normalizeFile(new File(getGlobalConfigPath() + File.separator + fileNames[0]));
        global = createIni(gFile);   // NOI18N

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
     * Return the path for the user command lines configuration directory 
     */
    private static String getUserConfigPath() {        
        if(Utilities.isUnix()) {
            String path = System.getProperty("user.home") ;                     // NOI18N
            return path + "/.";                                // NOI18N
        } else if (Utilities.isWindows()){
            String userConfigPath = getUSERPROFILE();
            return userConfigPath.equals("")? "":  userConfigPath + File.separator; // NOI18N 
        } 
        return "";                                                              // NOI18N
    }


    /**
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/mercurial";               // NOI18N
        } else if (Utilities.isWindows()){
            // <Mercurial Install>\Mercurial.ini
            String mercurialPath = HgModuleConfig.getDefault().getExecutableBinaryPath ();
            if(mercurialPath != null && !mercurialPath.equals("")){
                File ini = new File(mercurialPath, WINDOWS_HG_RC_FILE);
                if(ini != null && ini.exists() && ini.canRead()){
                    return ini.getParentFile().getAbsolutePath();
                }
            }
            // C:\Mercurial\Mercurial.ini
            File ini = new File(WINDOWS_DEFAULT_MECURIAL_INI_PATH);// NOI18N
            if(ini != null && ini.exists() && ini.canRead()){
                    return ini.getParentFile().getAbsolutePath();                
            }        
        } 
        return "";                                  // NOI18N
    }

    private static String getUSERPROFILE() {
        if(!Utilities.isWindows()) return null;

        String userprofile = ""; // NOI18N
        userprofile = System.getenv("USERPROFILE");// NOI18N

        return userprofile!= null? userprofile: ""; // NOI18N
    }

    private static String removeTrailingBackslahes (String value) {
        while (value.endsWith("\\")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

}
