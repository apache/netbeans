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
package org.netbeans.installer.utils.system;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.InternetShortcut;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.cleaner.ProcessOnExitCleanerHandler;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.cleaner.OnExitCleanerHandler;
import org.netbeans.installer.utils.system.shortcut.LocationType;
import org.netbeans.installer.utils.system.unix.shell.BourneShell;
import org.netbeans.installer.utils.system.unix.shell.CShell;
import org.netbeans.installer.utils.system.unix.shell.KornShell;
import org.netbeans.installer.utils.system.unix.shell.Shell;
import org.netbeans.installer.utils.system.unix.shell.TCShell;

/**
 *
 * @author Dmitry Lipin
 */
public class UnixNativeUtils extends NativeUtils {

    private boolean isUserAdminSet;
    private boolean isUserAdmin;
    private boolean checkQuota = true;
    private File quotaExecutable = null;
    private File browserExecutable = null;
    private boolean browserExecutableSet = false;
    private Boolean isSystem64Bit = null;
    
    private static final String[] FORBIDDEN_DELETING_FILES_UNIX = {
        System.getProperty("user.home"),
        System.getProperty("java.home"),
        "/",
        "/bin",
        "/boot",
        "/dev",
        "/etc",
        "/home",
        "/lib",
        "/mnt",
        "/opt",
        "/sbin",
        "/share",
        "/usr",
        "/usr/bin",
        "/usr/include",
        "/usr/lib",
        "/usr/man",
        "/usr/sbin",
        "/var",
    };
    
    private static final String CLEANER_RESOURCE =
            NATIVE_CLEANER_RESOURCE_SUFFIX + "unix/cleaner.sh"; // NOI18N

    private static final String CLEANER_FILENAME =
            "nbi-cleaner.sh"; // NOI18N

    public static final String XDG_DATA_HOME_ENV_VARIABLE =
            "XDG_DATA_HOME"; // NOI18N

    public static final String XDG_DATA_DIRS_ENV_VARIABLE =
            "XDG_DATA_DIRS"; // NOI18N
    
    public static final String XDG_DESKTOP_DIR_ENV_VARIABLE =
            "XDG_DESKTOP_DIR";
    public static final String XDG_USERDIRS_DIRS = 
            ".config/user-dirs.dirs";//NOI18N
    public static final String XDG_USERDIRS_CONF = 
            ".config/user-dirs.conf";//NOI18N    
    public static final String XDG_USERDIRS_GLOBAL_CONF = 
            "/etc/xdg/user-dirs.conf";//NOI18N
    public static final String DESKTOP_EXT = 
            ".desktop";//NOI18N

    public static final String XDG_CONFIG_HOME_ENV_VARIABLE =
            "XDG_CONFIG_HOME";//NOI18N
    public static final String XDG_CONFIG_HOME_DEFAULT =
            ".config";//NOI18N
    public static final String XDG_APPLICATION_MENU_FILE =
            "menus/applications.menu";//NOI18N
    
    public static final String DEFAULT_XDG_DATA_HOME =
            ".local/share"; // NOI18N

    public static final String DEFAULT_XDG_DATA_DIRS =
            "/usr/share"; // NOI18N

    public UnixNativeUtils() {
        initializeForbiddenFiles();
    }


    protected void loadLibrary(String path) {
        try {
            loadNativeLibrary(path);
        } catch (NativeException e) {
            //ok, not loaded, we`ll use Java`s implementation
            LogManager.log("Could not load native library due to some reasons, " + //NOI18N
                    "falling back to the Java implementation", e); //NOI18N
        }
    }
    
    @Override
    protected Platform getPlatform() {
        final String osName = System.getProperty("os.name");

        if (osName.equals("FreeBSD")) {
            return getPlatformFreeBSD();
        } else if (osName.equals("OpenBSD")) {
            return getPlatformOpenBSD();
        } else if (osName.endsWith("BSD")) {
            return getPlatformBSD();
        } else if (osName.equals("AIX")) {
            return getPlatformAIX();
        } else if (osName.toLowerCase(Locale.ENGLISH).startsWith("hp-ux")) {
            return getPlatformHPUX();
        } else {
            return Platform.UNIX;
        }
    }
    
    private Platform getPlatformOpenBSD() {
        final String osArch = System.getProperty("os.arch");
        if (osArch.contains("ppc") || osArch.contains("PowerPC")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.OPENBSD_PPC64 : Platform.OPENBSD_PPC;
        } else if (osArch.contains("sparc")) {
            return Platform.OPENBSD_SPARC;
        } else if (osArch.matches("i[3-6]86|x86|amd64|x86_64")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.OPENBSD_X64 : Platform.OPENBSD_X86;
        } else {
            return Platform.OPENBSD;
        }
    }
    private Platform getPlatformFreeBSD() {
        final String osArch = System.getProperty("os.arch");
        if (osArch.contains("ppc") || osArch.contains("PowerPC")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.FREEBSD_PPC64 : Platform.FREEBSD_PPC;
        } else if (osArch.contains("sparc")) {
            return Platform.FREEBSD_SPARC;
        } else if (osArch.matches("i[3-6]86|x86|amd64|x86_64")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.FREEBSD_X64 : Platform.FREEBSD_X86;
        } else {
            return Platform.FREEBSD;
        }
    }
    private Platform getPlatformBSD() {
        final String osArch = System.getProperty("os.arch");
        if (osArch.contains("ppc") || osArch.contains("PowerPC")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.BSD_PPC64 : Platform.BSD_PPC;
        } else if (osArch.contains("sparc")) {
            return Platform.BSD_SPARC;
        } else if (osArch.matches("i[3-6]86|x86|amd64|x86_64")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.BSD_X64 : Platform.BSD_X86;
        } else {
            return Platform.BSD;
        }
    }
    private Platform getPlatformAIX() {
        final String osArch = System.getProperty("os.arch");
        if (osArch.contains("ppc") || osArch.contains("PowerPC")) {
            return SystemUtils.isCurrentJava64Bit() ? Platform.AIX_PPC64 : Platform.AIX_PPC;
        } else {
            return Platform.AIX;
        }
    }
    
    private Platform getPlatformHPUX() {
        final String osArch = System.getProperty("os.arch");
        if (osArch.toLowerCase(Locale.ENGLISH).replace("-", "_").startsWith("pa_risc")) {
            return osArch.startsWith("PA_RISC2.0") ? Platform.HPUX_PA_RISC20 : Platform.HPUX_PA_RISC;
        } else if (osArch.toLowerCase(Locale.ENGLISH).startsWith("ia64")) {
            return Platform.HPUX_IA64;
        } else {
            return Platform.HPUX;
        }
    }
    
    @Override
    public boolean isSystem64Bit() {
        if (isSystem64Bit == null) {
            isSystem64Bit = false;
            
            try {
                Process p = Runtime.getRuntime().exec("uname -m");
                
                // Try if command exited. Wait max 1s.
                for (int i = 0; i < 5; i++) {
                    try {
                        p.exitValue();
                        break;
                    } catch (IllegalThreadStateException ex) {
                        // Exception should be ignored - do nothing.
                    }
                    
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        // Exception should be ignored - do nothing.
                    }
                }
             
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));                                
                if (br.ready()) {
                    String line = br.readLine();
                    isSystem64Bit = line.endsWith("64");
                }
            } catch (IOException ex) {
                LogManager.log(ErrorLevel.WARNING, ex);
            }
        }
        
        return isSystem64Bit;
    }
    
    @Override
    public boolean isCurrentUserAdmin() throws NativeException{
        if(isUserAdminSet) {
            return isUserAdmin;
        }
        boolean result = isCurrentUserAdminNative();
        isUserAdmin = result;
        isUserAdminSet = true;
        return result;
    }

    @Override
    protected OnExitCleanerHandler newDeleteOnExitCleanerHandler() {
        return new UnixProcessOnExitCleanerHandler(CLEANER_FILENAME);
    }

    public void updateApplicationsMenu() {
        try {
            SystemUtils.executeCommand(null,new String [] {
                "pkill", "-u", SystemUtils.getUserName(), "panel"});
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }

    public File getShortcutLocation(
            final Shortcut shortcut,
            final LocationType locationType) throws NativeException {
        LogManager.logIndent(
                "devising the shortcut location by type: " + locationType); // NOI18N

        if(shortcut.getPath()!=null) {
            return new File(shortcut.getPath());
        }
        final File shortcutFile;
        
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
            case ALL_USERS_DESKTOP:
                final File desktopLocation = getDesktopFolder();
                LogManager.log("... desktop folder : " + desktopLocation);
                shortcutFile = new File(desktopLocation, getShortcutFileName(shortcut));
                break;
            case CURRENT_USER_START_MENU:
                final File currentUserAppFolder = 
                        getApplicationsLocation(
                        getCurrentUserLocation());
                LogManager.log("... current user app folder : " + currentUserAppFolder);
                shortcutFile =  new File(currentUserAppFolder, 
                        getShortcutFileName(shortcut));
                break;
            case ALL_USERS_START_MENU:
                final File allUsersAppFolder = 
                        getApplicationsLocation(
                        getAllUsersLocation());
                LogManager.log("... all users app folder : " + allUsersAppFolder);
                shortcutFile = new File(allUsersAppFolder,
                        getShortcutFileName(shortcut));
                break;
            case CUSTOM:
                final String folder = shortcut.getRelativePath();

                LogManager.log("... custom folder : " + folder);
                shortcutFile = new File(folder,
                        getShortcutFileName(shortcut));
                break;
            default:
                shortcutFile = null;

        }
        if(shortcutFile!=null) {
            shortcut.setPath(shortcutFile.getAbsolutePath());
        }
        LogManager.logUnindent(
                "shortcut file: " + shortcutFile); // NOI18N

        return shortcutFile;
    }
    
    private String getShortcutFileName(Shortcut shortcut) {
        String fileName = shortcut.getFileName();
        if (fileName == null) {
            if (shortcut instanceof FileShortcut) {
                final File target = ((FileShortcut) shortcut).getTarget();

                fileName = target.getName();
                if(!target.isDirectory()) {
                    fileName += DESKTOP_EXT;
                }
            } else if(shortcut instanceof InternetShortcut) {
                fileName = ((InternetShortcut) shortcut).getURL().getFile() +
                        DESKTOP_EXT;
            }
        }
        return fileName;
    }
    
    private File getApplicationsLocation(File location) {
        return new File(location, "applications");
    }
    
    private File getAllUsersLocation() {
        final String XDG_DATA_DIRS = System.getenv(XDG_DATA_DIRS_ENV_VARIABLE);                
        final File allUsersLocation;
        if (XDG_DATA_DIRS == null) {
            allUsersLocation = new File(DEFAULT_XDG_DATA_DIRS);
        } else {
            // Workaround for Issue 131194 : 
            // Cannot install netbeans using xfce4 session (incorrect XDG_DATA_DIRS set)
            // http://www.netbeans.org/issues/show_bug.cgi?id=131194
            String firstPath = XDG_DATA_DIRS.split(SystemUtils.getPathSeparator())[0].trim();
            if(firstPath.contains(File.separator) && !firstPath.startsWith(File.separator)) {            
                firstPath = File.separator + firstPath;
            }
            allUsersLocation = new File(firstPath);
        }
        
        LogManager.log(XDG_DATA_DIRS_ENV_VARIABLE + " = " + allUsersLocation); // NOI18N
        
        return allUsersLocation;
    }
    
    private File getCurrentUserLocation() {
        final File currentUserLocation;
        final String XDG_DATA_HOME = System.getenv(XDG_DATA_HOME_ENV_VARIABLE);

        if (XDG_DATA_HOME == null) {
            currentUserLocation = new File(
                    SystemUtils.getUserHomeDirectory(),
                    DEFAULT_XDG_DATA_HOME);
        } else {
            currentUserLocation = new File(XDG_DATA_HOME);
        }
        
        LogManager.log(XDG_DATA_DIRS_ENV_VARIABLE + " = " + currentUserLocation); // NOI18N

        return currentUserLocation;
    }
    
    private File getDesktopFolder() {
        // TODO
        // If using XDG, desktop folder can be obtained simpler using '/usr/bin/xdg-user-dir DESKTOP' command
        // See also http://www.netbeans.org/issues/show_bug.cgi?id=144646
        final String desktopDir = System.getenv(XDG_DESKTOP_DIR_ENV_VARIABLE);
        final File globalConfigFile = new File(XDG_USERDIRS_GLOBAL_CONF);
        final File userHome       = SystemUtils.getUserHomeDirectory();
        final File userDirsFile   = new File(userHome, XDG_USERDIRS_DIRS);
        final File userConfigFile = new File(userHome, XDG_USERDIRS_CONF);
        LogManager.log("... getting desktop folder");
        if (desktopDir != null && !desktopDir.equals("")) {
            LogManager.log(XDG_DESKTOP_DIR_ENV_VARIABLE + " = " + desktopDir);
            File f = new File(desktopDir);
            if (f.exists()) {
                LogManager.log("... desktop dir : " + f);
                return f;
            } else {
                LogManager.log("... " + XDG_DESKTOP_DIR_ENV_VARIABLE + " is defined but does not exist:" + desktopDir);
            }
        } else if (System.getenv("XDG_SESSION_COOKIE") == null && System.getenv(XDG_DATA_DIRS_ENV_VARIABLE) == null) {
            LogManager.log("... neither XDG_SESSION_COOKIE nor " + XDG_DATA_DIRS_ENV_VARIABLE + " environment variable is defined");
        } else if (!FileUtils.exists(globalConfigFile)) {
            LogManager.log("... global XDG config file does not exist");
        } else if (!FileUtils.exists(userDirsFile)) {
            LogManager.log("... user XDG config file does not exist");
        } else if (!FileUtils.canRead(userDirsFile)) {
            LogManager.log("... cannot read user XDG config file");
        } else {
            try {
                boolean useXdgDirs = false;
                for (File configFile : new File[]{userConfigFile, globalConfigFile}) {
                    if (!FileUtils.exists(configFile)) {
                        continue;
                    }
                    for (String s : FileUtils.readStringList(configFile)) {
                        final Matcher matcher = Pattern.compile("enabled=(.*)").matcher(s);
                        if (matcher.find()) {
                            if (!Boolean.parseBoolean(matcher.group(1).toLowerCase(Locale.ENGLISH))) {
                                LogManager.log("... XDG dirs are disabled");
                                useXdgDirs = false;
                                break;
                            } else {
                                LogManager.log("... XDG dirs are enabled");
                                useXdgDirs = true;
                            }
                        }
                    }
                }
                
                if (useXdgDirs) {
                    String encoding = StringUtils.EMPTY_STRING;// by default
                    for (File configFile : new File[]{userConfigFile, globalConfigFile}) {
                        if (!FileUtils.exists(configFile)) {
                            continue;
                        }
                        for (String s : FileUtils.readStringList(configFile)) {
                            final Matcher matcher = Pattern.compile("filename_encoding=(.*)").matcher(s);
                            if (matcher.find()) {
                                encoding = matcher.group(1);
                                if (encoding.equals("locale")) {
                                    // http://src.opensolaris.org/source/xref/jds/spec-files/trunk/SUNWxdg-user-dirs.spec
                                    encoding = null;
                                }
                                break;
                            }
                        }
                        if (encoding == null || !encoding.equals(StringUtils.EMPTY_STRING)) {
                            break;
                        } else {
                            encoding = StringUtils.ENCODING_UTF8;
                        }
                    }
                    LogManager.log("... using encoding for config file reading : " + encoding );
                    List<String> content = (encoding == null) ? FileUtils.readStringList(userDirsFile) : FileUtils.readStringList(userDirsFile, encoding);

                    for (String s : content) {
                        LogManager.log("...... evaluating string : " + s);
                        Matcher matcher = Pattern.compile("^" + XDG_DESKTOP_DIR_ENV_VARIABLE + "=\"(.*)\"").matcher(s);
                        if (matcher.find()) {
                            LogManager.log("...... matches expected pattern");
                            final String value = matcher.group(1).replace("$HOME", userHome.getAbsolutePath());
                            File f = new File(value);
                            if (FileUtils.exists(f)) {
                                return f;
                            } else {
                                LogManager.log("... custom desktop directory defined but does not exist: " + f);
                                LogManager.log("... probably wrong encoding used, fallback to system utils");
                                throw new IOException("File " + f + " is defined as desktop folder but does not exist");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogManager.log(e);
                //fallback
                try {
                    final File bin = new File("/usr/bin/xdg-user-dir");
                    if (FileUtils.exists(bin)) {
                        String stdout = SystemUtils.executeCommand(bin.getAbsolutePath(), "DESKTOP").getStdOut();
                        if (stdout.length() > 0) {
                            File dsk = new File(stdout);
                            if (FileUtils.exists(dsk)) {
                                return dsk;
                            } else {
                                LogManager.log("... custom desktop directory defined (system util) but does not exist: " + dsk);
                                LogManager.log("... probably wrong encoding, fallback to default");                                
                            }
                        }
                    }
                } catch (Exception ex) {
                    LogManager.log(ex);
                }
            }
        }
        return new File(userHome, "Desktop");
    }
    
    private void addLocalizedMapEntry(List<String> list, String entryName, Map<Locale, String> entryMap) {
        if (!entryMap.isEmpty()) {
            String name = entryMap.get(new Locale(StringUtils.EMPTY_STRING));
            if(name==null) {
                name = entryMap.get(Locale.getDefault());
            }
            list.add(entryName + "=" + name);

            for (Map.Entry<Locale, String> entry : entryMap.entrySet()) {
                if (!name.equals(entry.getValue())) {
                    list.add(entryName + "[" + entry.getKey() + "]=" + StringUtils.getLocalizedString(entryMap, entry.getKey()));
                }
            }
        }
    }

    private List <String> getDesktopEntry(FileShortcut shortcut) {
        final List <String> list = new ArrayList<String> ();

        list.add("[Desktop Entry]");
        list.add("Encoding=UTF-8");
        addLocalizedMapEntry(list, "Name",    shortcut.getNames());
        addLocalizedMapEntry(list, "Comment", shortcut.getDescriptions());
        list.add("Exec=/bin/sh \"" + shortcut.getTarget() + "\"" + 
                ((shortcut.getArguments()!=null && shortcut.getArguments().size()!=0) ? 
                    StringUtils.SPACE + shortcut.getArgumentsString() : StringUtils.EMPTY_STRING)
                    );

        if(shortcut.getIcon()!=null) {
            list.add("Icon=" + shortcut.getIconPath());
        }
        if(shortcut.getCategories().length != 0) {
            list.add("Categories=" +
                    StringUtils.asString(shortcut.getCategories(),";"));
        }

        list.add("Version=1.0");
        list.add("Type=Application");
        list.add("Terminal=0");
        Properties props = shortcut.getAdditionalProperties();
        props.forEach((k, v) -> list.add(k.toString() + "=" + v));
        list.add(SystemUtils.getLineSeparator());
        return list;
    }

    protected List <String> getDesktopEntry(InternetShortcut shortcut) {
        final List <String> list = new ArrayList<String> ();
        list.add("[Desktop Entry]");
        list.add("Encoding=UTF-8");
        addLocalizedMapEntry(list, "Name",    shortcut.getNames());
        addLocalizedMapEntry(list, "Comment", shortcut.getDescriptions());
        
        list.add("URL=" + shortcut.getURL());
        if(shortcut.getIcon()!=null) {
            list.add("Icon=" + shortcut.getIconPath());
        }
        if(shortcut.getCategories().length != 0) {
            list.add("Categories=" +
                    StringUtils.asString(shortcut.getCategories(),";"));
        }
        list.add("Version=1.0");
        list.add("Type=Link");
        Properties props = shortcut.getAdditionalProperties();
        props.forEach((k, v) -> list.add(k.toString() + "=" + v));
        list.add(SystemUtils.getLineSeparator());
        return list;
    }

    private void addExecutablePermissions(File file) throws IOException {
        // add x permission bit to r bits
        int permissions = SystemUtils.getPermissions(file);
        int newPermissions = (permissions +
                ((permissions & FileAccessMode.RU) != 0 && (permissions & FileAccessMode.EU)==0 ? FileAccessMode.EU : 0) +
                ((permissions & FileAccessMode.RG) != 0 && (permissions & FileAccessMode.EG)==0 ? FileAccessMode.EG : 0) +
                ((permissions & FileAccessMode.RO) != 0 && (permissions & FileAccessMode.EO)==0 ? FileAccessMode.EO : 0));
        SystemUtils.setPermissions(file, newPermissions, FA_MODE_SET);
    }

    public File createShortcut(Shortcut shortcut, LocationType locationType) throws NativeException {
        final File          file     = getShortcutLocation(shortcut, locationType);
        try {
            if(shortcut instanceof FileShortcut) {
                File target = ((FileShortcut)shortcut).getTarget();
                if(target.isDirectory()) {
                    createSymLink(file, target);
                } else {
                    FileUtils.writeStringList(file,
                            getDesktopEntry((FileShortcut)shortcut));
                    addExecutablePermissions(file);
                    postShortcutCreate(shortcut, locationType);
                }
            } else if(shortcut instanceof InternetShortcut) {
                FileUtils.writeStringList(file,
                        getDesktopEntry((InternetShortcut)shortcut));
                addExecutablePermissions(file);
                postShortcutCreate(shortcut, locationType);
            }
        } catch (IOException e) {
            throw new NativeException("Cannot create shortcut", e);
        }

        return file;
    }

    private void postShortcutCreate(Shortcut shortcut, LocationType locationType) {
        if(locationType.equals(LocationType.CURRENT_USER_START_MENU)) {
            // #165320
            final String XDG_CONFIG_HOME = System.getenv(XDG_CONFIG_HOME_ENV_VARIABLE);
            final File configHome = XDG_CONFIG_HOME != null ?
                new File(XDG_CONFIG_HOME) :
                new File (SystemUtils.getUserHomeDirectory(), XDG_CONFIG_HOME_DEFAULT);
            
            final File appsMenu = new File(configHome, XDG_APPLICATION_MENU_FILE);
            if(!FileUtils.exists(appsMenu)) {
                try {
                    FileUtils.mkdirs(appsMenu.getParentFile());
                    boolean created = appsMenu.createNewFile();
                    if(created) {
                        SystemUtils.sleep(50);
                        FileUtils.deleteFile(appsMenu);
                    }
                } catch (IOException e) {
                    LogManager.log(e);
                }
            }
        }
    }

    public void removeShortcut(Shortcut shortcut, LocationType locationType, boolean cleanupParents) throws NativeException {
        try {
            File shortcutFile = getShortcutLocation(shortcut, locationType);

            FileUtils.deleteFile(shortcutFile);

            if(cleanupParents &&
                    (locationType == LocationType.ALL_USERS_START_MENU ||
                    locationType == LocationType.CURRENT_USER_START_MENU)) {
                FileUtils.deleteEmptyParents(shortcutFile);
            }
        } catch (IOException e) {
            throw new NativeException("Cannot remove shortcut", e);
        }
    }

    @Override
    public boolean isBrowseSupported() {
        initBrowser();
        return browserExecutable!=null;
    }

    public boolean openBrowser(URI uri) {
        initBrowser();
        if (browserExecutable != null) {
            LogManager.log("... using browser: " + browserExecutable);
            try {
                Runtime.getRuntime().exec(new String[]{browserExecutable.getAbsolutePath(), uri.toString()});
                return true;
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
        return false;
    }

    protected String[] getPossibleBrowserLocations() {
        return new String [] {};
    }

    private void initBrowser() {
        if(browserExecutableSet) return;
        final String[] possibleBrowsers = getPossibleBrowserLocations();
        for (String s : possibleBrowsers) {
            File f = new File(s);
            if (f.exists()) {
                browserExecutable = f;
                break;
            }
        }
        browserExecutableSet = true;
    }
    
    public List<File> findExecutableFiles(File parent) throws IOException {
        List<File> files = new ArrayList<File>();

        if (parent.exists()) {
            if(parent.isDirectory()) {
                File [] children = parent.listFiles();
                for(File child : children) {
                    files.addAll(findExecutableFiles(child));
                }
            } else {
                // name based analysis
                File child = parent;
                String name = child.getName();
                String [] scriptExtensions = { ".sh", ".pl", ".py"};  //shell, perl, python
                for(String ext : scriptExtensions) {
                    if (name.endsWith(ext)) {
                        files.add(child);
                        return files;
                    }
                }
                // contents based analysis
                String line = FileUtils.readFirstLine(child);
                if (line != null) {
                    if (line.startsWith("#!")) { // a script of some sort
                        files.add(child);
                        return files;
                    }
                }
                // is it an ELF file?
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(child));
                byte[] buf = new byte[4];
                bis.read(buf);
                bis.close();
                if (Arrays.equals(buf, ELF_BYTES)){
                    files.add(child);
                    return files;
                }
            }
        }
        return files;
    }

    public List<File> findIrrelevantFiles(File parent) throws IOException {
        List<File> files = new ArrayList<File>();

        if (parent.exists()) {
            if(parent.isDirectory()) {
                File [] children = parent.listFiles();
                for(File child : children) {
                    files.addAll(findIrrelevantFiles(child));
                }
            } else {
                // contents based analysis - none at this point

                // name based analysis
                File child = parent;
                String name = child.getName();
                String [] windowsExtensions = {
                    ".bat", ".cmd", ".dll", ".exe", ".com",
                    ".vbs", ".vbe", ".wsf", ".wsh"} ;
                for(String ext : windowsExtensions) {
                    if(name.endsWith(ext)) {
                        files.add(child);
                        break;
                    }
                }
            }
        }
        return files;
    }

    public void chmod(File file, String mode) throws IOException {
        chmod(Arrays.asList(file), mode);
    }

    public void chmod(File file, int mode) throws IOException {
        chmod(file, Integer.toString(mode,8));
    }

    public void chmod(List<File> files, String mode) throws IOException {
        for(File file : files) {
            File   directory = file.getParentFile();
            String name      = file.getName();

            SystemUtils.executeCommand(directory, "chmod", mode, name);
        }
    }

    public void setPermissions(File file, int mode, int change) throws IOException {
        LogManager.log("setting permissions " + Integer.toString(mode, 8) + " on " + file);

        setPermissionsNative(file.getAbsolutePath(), mode, change);
    }

    public int getPermissions(File file) throws IOException {
        return getPermissionsNative(file.getAbsolutePath());
    }

    public void removeIrrelevantFiles(File parent) throws IOException {
        FileUtils.deleteFiles(findIrrelevantFiles(parent));
    }

    public void correctFilesPermissions(File parent) throws IOException {
        chmod(findExecutableFiles(parent), "ugo+x");
    }

    public long getFreeSpace(File file) {
        if ((file == null) || file.getPath().equals("")) {
            return 0;
        } else {
            long freeSpace = getFreeSpaceNative(file.getPath());
            if (checkQuota) {
                // #123587 Disk space check should take into account user quota
                try {
                    LogManager.indent();
                    long freeSpaceQuota = getFreeSpaceUsingQuota(file);
                    if(freeSpaceQuota!=-1L) {
                        LogManager.log("... free space (due to the quote) is " + freeSpaceQuota + ", physical is : " + freeSpace);
                        freeSpace = freeSpaceQuota;
                    }
                } catch (IOException e) {
                    LogManager.log("... quota check is disabled");
                    checkQuota = false;
                } finally {
                    LogManager.unindent();
                }
            }
            return freeSpace;
        }
    }

    private long getFreeSpaceUsingQuota(File file) throws IOException {
        String path = file.getAbsolutePath();
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            LogManager.log(e);
        }

        LogManager.log("Checking free space with quota in " + path);
        try {
            setEnvironmentVariable("LANG", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_COLLATE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_CTYPE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_MESSAGES", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_MONETARY", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_NUMERIC", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_TIME", "C", EnvironmentScope.PROCESS, false);
        } catch (NativeException e) {
            LogManager.log(e);
        }
        if (quotaExecutable == null) {
            for (String q : QUOTA_LOCATIONS) {
                final File f = new File(q);
                if (FileUtils.exists(f)) {
                    quotaExecutable = f;
                    break;
                }
            }
            if (quotaExecutable == null) {
                LogManager.log("... no quota executable found");
                throw new IOException();
            }
        }
        final List<String> stdoutList = new ArrayList<String>();

        Thread quotaThread = null;
        try {
            quotaThread = new Thread() {
                @Override
                public void run() {
                    try {
                        LogManager.log("... running command : " + quotaExecutable.getPath() + " -v");
                        Process p = new ProcessBuilder(quotaExecutable.getPath(), "-v").start();
                        final InputStream is = p.getInputStream();
                        final InputStream err = p.getErrorStream();
                        p.waitFor();
                        final String output = StringUtils.readStream(is);
                        final String error = StringUtils.readStream(err);
                        LogManager.log("... stdout:");
                        LogManager.log(output);
                        LogManager.log("... stderr:");
                        LogManager.log(error);
                        LogManager.log("... return : " + p.exitValue());
                        stdoutList.add(output);
                        is.close();
                        err.close();
                    } catch (IOException e) {
                        LogManager.log("... error occured when running quota executable", e);
                    } catch (InterruptedException e) {
                        LogManager.log("... interrupted");
                    }
                }
            };

            quotaThread.start();
            quotaThread.join(QUOTA_TIMEOUT_MILLIS);
            if (quotaThread.isAlive()) {
                LogManager.log("... quota command is hanging more than 5 seconds so killing it");
                quotaThread.interrupt();
                LogManager.log("... killed");
            }
        } catch (InterruptedException ie) {
            LogManager.log("... interrupted", ie);
            quotaThread.interrupt();
        }
        if(stdoutList.size()==0) {
            LogManager.log("... quota produced no stdout for analysis");
            throw new IOException();
        }

        final String stdout = stdoutList.get(0);
        final String[] lines = StringUtils.splitByLines(stdout);
        if (lines.length <= 2) {
            LogManager.log("... no quota set for the user (number of lines in output less that 3)");
            throw new IOException();
        }
        // Usual format is the following
        // Disk quotas for <userid> (<uid>):
        // Filesystem  usage  quota  limit  timeleft  files  quota  limit   timeleft
        // /home/<userid> 943880  0 1577704           18992    0      0   
        // /home/<userid2> 943880  0 1577704      1    18992    0      0     1
        List<Pair<String, Long>> pathSpace = new ArrayList<Pair<String, Long>>();

        try {
            for (int i = 2; i < lines.length; i++) {
                String s = lines[i].trim();
                if (s.startsWith(File.separator) && s.indexOf(StringUtils.SPACE) != -1) {
                    String quotedPath = s.substring(0, s.indexOf(StringUtils.SPACE));
                    String[] numbers = s.substring(s.indexOf(StringUtils.SPACE) + 1).
                            trim().split("[ |\t]+");
                    if (numbers.length < 6) {
                        LogManager.log("...cannot parse the quota numbers [" + numbers.length + "]");
                        throw new IOException();
                    }

                    final long limit = Long.parseLong(numbers[2]);
                    final long usage = Long.parseLong(numbers[0]);
                    final long freespace = (limit - usage) * 1024;

                    if (limit > 0 && freespace >= 0) {
                        pathSpace.add(new Pair<>(quotedPath, freespace));
                    }
                }
            }
            if (pathSpace.size() == 0) {
                LogManager.log("... no quota set for the user (no paths in quota output)");
                throw new IOException();
            }
            String longestPath = StringUtils.EMPTY_STRING;
            long freespace = -1L;
            for (Pair<String, Long> p : pathSpace) {
                final String s = p.getFirst();
                if (s.length() > longestPath.length() && path.startsWith(s)) {
                    longestPath = s;
                    freespace = p.getSecond();
                }
            }
            return freespace;
        } catch (NumberFormatException e) {
            LogManager.log("...cannot parse the quota numbers", e);
            throw new IOException();
        } catch (PatternSyntaxException e) {
            LogManager.log("...cannot parse the quota numbers", e);
            throw new IOException();
        }
    }

    public boolean isUNCPath(String path) {
        // for Unix UNC is smth like servername:/folder...
        return path.matches("^.+:/.+");
    }

    // other ... //////////////////////////
    
    public String getEnvironmentVariable(String name, EnvironmentScope scope, boolean flag) {
        return System.getenv(name);
    }

    public void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean flag) throws NativeException {
        if (EnvironmentScope.PROCESS == scope) {
            SystemUtils.getEnvironment().put(name, value);
        } else {
            try {
                getCurrentShell().setVar(name, value, scope);
            } catch (IOException e) {
                throw new NativeException("Cannot set the environment variable value", e);
            }
        }
    }

    public Shell getCurrentShell() {
        LogManager.log(ErrorLevel.DEBUG,
                "Getting current shell..");
        LogManager.indent();
        Shell [] avaliableShells =  {
            new BourneShell(),
            new CShell() ,
            new TCShell(),
            new KornShell()
        };
        String shell = System.getenv("SHELL");
        Shell result = null;
        if(shell == null) {
            shell = System.getenv("shell");
        }
        LogManager.log(ErrorLevel.DEBUG,
                "... shell env variable = " + shell);

        if(shell != null) {
            if(shell.lastIndexOf(File.separator)!=-1) {
                shell = shell.substring(shell.lastIndexOf(File.separator) + 1);
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "... searching for the shell with name [" + shell +  "] " +
                    "among available shells names");
            for(Shell sh : avaliableShells) {
                if(sh.isCurrentShell(shell)) {
                    result = sh;
                    LogManager.log(ErrorLevel.DEBUG,
                            "... detected shell: " +
                            sh.getClass().getSimpleName());
                    break;
                }
            }

        }
        if(result == null) {
            LogManager.log(ErrorLevel.DEBUG,
                    "... no shell found");
        }
        LogManager.unindent();
        LogManager.log(ErrorLevel.DEBUG,
                "... finished detecting shell");
        return result;
    }

    public File getDefaultApplicationsLocation() {
        File opt = new File("/opt");

        if (opt.exists() && opt.isDirectory() && FileUtils.canWrite(opt)) {
            return opt;
        } else {
            return SystemUtils.getUserHomeDirectory();
        }
    }

    public boolean isPathValid(String path) {
        return true;
    }

    public FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        final FilesList list = new FilesList();

        if (descriptor.getModifyCommand() != null) {
            try {
                final Launcher launcher = createUninstaller(descriptor, false, new Progress());
                correctFilesPermissions(launcher.getOutputFile());
                list.add(launcher.getOutputFile());
            } catch (IOException e) {
                throw new NativeException("Can't create uninstaller", e);
            }
        }

        if (descriptor.getUninstallCommand() != null) {
            try {
                final Launcher launcher = createUninstaller(descriptor, true, new Progress());
                correctFilesPermissions(launcher.getOutputFile());
                list.add(launcher.getOutputFile());
            } catch (IOException e) {
                throw new NativeException("Can't create uninstaller", e);
            }
        }

        return list;
    }

    public void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) {
        // does nothing - no support for unix package managers yet
    }

    public FilesList createSymLink(File source, File target) throws IOException {
        return createSymLink(source, target, true);
    }

    public FilesList createSymLink(File source, File target, boolean useRelativePath) throws IOException {
        FilesList list = new FilesList();

        list.add(FileUtils.mkdirs(source.getParentFile()));
        list.add(source);

        String relativePath = null;
        if (useRelativePath) {
            relativePath = FileUtils.getRelativePath(source, target);
        }

        SystemUtils.executeCommand(
                "ln",
                "-s",
                relativePath == null ? target.getAbsolutePath() : relativePath,
                source.getAbsolutePath());

        return list;
    }
    
    private String[] getDfCommand(String... args) {
        List <String> command = new ArrayList <String> ();
        command.add("df");//NOI18N
        command.add(getCurrentPlatform().isCompatibleWith(Platform.HPUX) ? "-kP" : "-k"); //NOI18N
        // it is also possible to use bdf instead of df -kP but block size is 512 in that case        
        
        if (args != null && args.length > 0) {
            for(String arg:args) {
                command.add(getExistingParent(arg));
            }
        }
        return command.toArray(new String[0]);
    }
    
    /**
     * Checks if given filename belongs to existing file. 
     * If not, the closest existing parent filename is returned.
     * 
     * @param fileName 
     * @return Filename of existing parent or file itself.
     */
    private String getExistingParent(String fileName) {
        File file = new File(fileName);
        
        if (file.exists()) {
            return fileName;
        } else {
            return getExistingParent(file.getParent());
        }
    }
        
    @Override
    public List<File> getFileSystemRoots(String... files) throws IOException {
        try {
            setEnvironmentVariable(
                    "LANG", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_COLLATE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_CTYPE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_MESSAGES", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_MONETARY", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_NUMERIC", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_TIME", "C", EnvironmentScope.PROCESS, false);
            
            final String stdout = SystemUtils.executeCommand(getDfCommand(files)).getStdOut();
            final String[] lines = StringUtils.splitByLines(stdout);

            // a quick and dirty solution - we assume that % is present only once in
            // each line - in the part where the percentage is reported, hence we
            // look for the percentage sign and then for the first slash
            final List<File> roots = new LinkedList<>();
            for (int i = 1; i < lines.length; i++) {
                int index = lines[i].indexOf("%");
                if (index != -1) {
                    index = lines[i].indexOf("/", index);

                    if (index != -1) {
                        final String path = lines[i].substring(index);
                        final File file = new File(path);

                        if (!roots.contains(file)) {
                            roots.add(file);
                        }
                    }
                }
            }

            return roots;
        } catch (NativeException e) {
            final IOException ioException =
                    new IOException("Cannot define the environment");

            throw (IOException) ioException.initCause(e);
        }
    }

    // native declarations //////////////////////////////////////////////////////////
    private native long getFreeSpace0(String s);

    private native void setPermissions0(String path, int mode, int change);

    private native int getPermissions0(String path);

    private native boolean isCurrentUserAdmin0();
    
    private long getFreeSpaceNative(String s) {
        return nativeLibraryLoaded ? getFreeSpace0(s) : getFreeSpaceJ(s);
    }
    
    private long getFreeSpaceJ(String s) {
        try {
            setEnvironmentVariable(
                    "LANG", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_COLLATE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_CTYPE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_MESSAGES", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_MONETARY", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_NUMERIC", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_TIME", "C", EnvironmentScope.PROCESS, false);
        } catch (NativeException e) {
            LogManager.log(e);
        }
        
        try {
            final String stdout = SystemUtils.executeCommand(getDfCommand(s)).getStdOut().trim();
            final String[] lines = StringUtils.splitByLines(stdout);

            // a quick and dirty solution - we assume that % is present only once in
            // each line - in the part where the percentage is reported, hence we
            // look for the percentage sign and then for the first slash            
            for (int i = 1; i < lines.length; i++) {
                int index = lines[i].indexOf("%");
                if (index != -1) {                    
                    String parts[] = lines[i].substring(0, index).split("[ ]+");
                    if (parts.length > 1) {
                        return Long.parseLong(parts[parts.length - 2]) * 1024L;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            LogManager.log(e);
        }
        return 0L;
    }
    
    private void setPermissionsJ(String path, int mode, int change) throws IOException {
        switch(change) {
            case FA_MODE_SET: 
                chmod(new File(path), mode); 
                break;
                
            case FA_MODE_ADD: 
            case FA_MODE_REMOVE:                     
                if(mode==0) return;
                String fullmode = StringUtils.EMPTY_STRING;
                
                Integer [] rModes = new Integer [] {FileAccessMode.RU, FileAccessMode.RG, FileAccessMode.RO };
                Integer [] wModes = new Integer [] {FileAccessMode.WU, FileAccessMode.WG, FileAccessMode.WO };
                Integer [] xModes = new Integer [] {FileAccessMode.EU, FileAccessMode.EG, FileAccessMode.EO };
                
                List <Pair <List <Integer>, String >> modes = new ArrayList<>();
                
                modes.add(new Pair< >(new ArrayList<>(Arrays.asList(rModes)), "r"));
                modes.add(new Pair< >(new ArrayList<>(Arrays.asList(wModes)), "w"));
                modes.add(new Pair< >(new ArrayList<>(Arrays.asList(xModes)), "x"));
                
                for (int i = 0; i < modes.size(); i++) {
                    String m = StringUtils.EMPTY_STRING;
                    List<Integer> list = modes.get(i).getFirst();
                    for (int j = 0; j < list.size(); j++) {
                        if ((mode & list.get(j)) != 0) {
                            m += (j == 0 ? "u" : (j == 1 ? "g" : "o"));
                        }
                    }                    
                    if(!m.equals(StringUtils.EMPTY_STRING)) {
                        m += ((change == FA_MODE_ADD) ? "+" : "-") + modes.get(i).getSecond();
                        fullmode = fullmode.equals(StringUtils.EMPTY_STRING) ? m : fullmode + "," + m;
                        m = StringUtils.EMPTY_STRING;
                    }                
                }
                
                if(!fullmode.equals(StringUtils.EMPTY_STRING)) {
                    chmod(new File(path), fullmode); 
                }
                break;
            default: 
                break;
        }
    }
    
    private void setPermissionsNative(String path, int mode, int change) throws IOException {
        if(nativeLibraryLoaded) {
            setPermissions0(path,mode,change);
        } else  {
            setPermissionsJ(path,mode,change);
        }
    }
    
    private int getPermissionsNative(String path) {
        return nativeLibraryLoaded ? getPermissions0(path) : getPermissionsJ(path);
    }
    
    private int getPermissionsJ(String path) {
        try {
            final String output = SystemUtils.executeCommand("ls", "-ld", path).getStdOut().trim();

            int permissions = 0;
            for (int i = 0; i < 9; i++) {
                char character = output.charAt(i + 1);

                if (i % 3 == 0) {
                    permissions *= 10;
                }

                if (character == '-') {
                    continue;
                } else if ((i % 3 == 0) && (character == 'r')) {
                    permissions += 4;
                } else if ((i % 3 == 1) && (character == 'w')) {
                    permissions += 2;
                } else if ((i % 3 == 2) && (character == 'x')) {
                    permissions += 1;
                } else {
                    return -1;
                }
            }

            return permissions;
        } catch (IOException | IndexOutOfBoundsException e) {
            return -1;
        }
    }
    
    private boolean isCurrentUserAdminNative() {
        return (nativeLibraryLoaded) ? isCurrentUserAdmin0() : isCurrentUserAdminJ();
    }
            
    private boolean isCurrentUserAdminJ() {
        boolean adm = false;
        try {
            try {
                setEnvironmentVariable(
                        "LANG", "C", EnvironmentScope.PROCESS, false);
                setEnvironmentVariable(
                        "LC_COLLATE", "C", EnvironmentScope.PROCESS, false);
                setEnvironmentVariable(
                        "LC_CTYPE", "C", EnvironmentScope.PROCESS, false);
                setEnvironmentVariable(
                        "LC_MESSAGES", "C", EnvironmentScope.PROCESS, false);
                setEnvironmentVariable(
                        "LC_MONETARY", "C", EnvironmentScope.PROCESS, false);
                setEnvironmentVariable(
                        "LC_NUMERIC", "C", EnvironmentScope.PROCESS, false);
                setEnvironmentVariable(
                        "LC_TIME", "C", EnvironmentScope.PROCESS, false);
            } catch (NativeException e) {
                LogManager.log(e);
            }
            String stdout = SystemUtils.executeCommand("id").getStdOut();
            Matcher matcher = Pattern.compile("euid=([0-9]+)\\(").matcher(stdout);
            if (!matcher.find()) {
                matcher = Pattern.compile("uid=([0-9]+)\\(").matcher(stdout);
            }
            if (matcher.find()) {
                adm = Integer.parseInt(matcher.group(1)) == 0;
            }
        } catch (IOException | NumberFormatException e) {
            LogManager.log(e);
        }
        return adm;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class UnixProcessOnExitCleanerHandler extends ProcessOnExitCleanerHandler {
        public UnixProcessOnExitCleanerHandler(String cleanerFileName) {
            super(cleanerFileName);
        }
        protected void writeCleaner(File cleanerFile) throws IOException {
            InputStream is = ResourceUtils.getResource(CLEANER_RESOURCE);
            CharSequence cs = StreamUtils.readStream(is);
            is.close();
            String [] lines = StringUtils.splitByLines(cs);
            FileUtils.writeFile(cleanerFile, StringUtils.asString(lines, SystemUtils.getLineSeparator()));
        }

        protected void writeCleaningFileList(File listFile, List<String> files) throws IOException {
            // be sure that the list file contains end-of-line
            // otherwise the installer will run into Issue 104079
            List<String> newList = new LinkedList<> (files);
            newList.add(SystemUtils.getLineSeparator());
            FileUtils.writeStringList(listFile, newList);
        }
    }

    public static class FileAccessMode {
        /** Read by user */
        public static final int RU = 0400;
        /** Write by user */
        public static final int WU = 0200;
        /** Execute by user */
        public static final int EU = 0100;
        
        /** Read by group */
        public static final int RG = 040;
        /** Write by group */
        public static final int WG = 020;
        /** Execute by group */
        public static final int EG = 010;
        
        /** Read by others */
        public static final int RO = 04;
        /** Write by others */
        public static final int WO = 02;
        /** Execute by others */
        public static final int EO = 01;
    }

    @Override
    protected void initializeForbiddenFiles(String ... files) {
        super.initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_UNIX);
        super.initializeForbiddenFiles(files);
    }
    private static final String [] QUOTA_LOCATIONS = {
        "/usr/sbin/quota", //NOI18N
        "/usr/bin/quota",  //NOI18N    
        "/sbin/quota",     //NOI18N
        "/bin/quota",      //NOI18N
    };
    private static final byte [] ELF_BYTES = new byte[]{'\177','E','L','F'};
    private static final long QUOTA_TIMEOUT_MILLIS = 5000;//NOMAGI
}
