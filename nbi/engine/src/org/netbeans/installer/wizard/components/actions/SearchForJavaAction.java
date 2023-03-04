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

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.*;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKCU;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;

/**
 *
 * @author Kirill Sorokin
 */
public class SearchForJavaAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private static List<File> javaLocations = new LinkedList<>();
    private static List<String> javaLabels  = new LinkedList<>();
    
    public SearchForJavaAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    @Override
    public void execute() {        
        execute(new Progress());
    }
    
    public void execute(Progress progress) {
        LogManager.logEntry("search for all java locations");
        getWizardUi().setProgress(progress);
        final List<File> locations = new LinkedList<>();
        progress.setTitle(SEARCH_INSTALLED_JAVAS);
        progress.setDetail(StringUtils.EMPTY_STRING);
        progress.setPercentage(Progress.START);
        
        SystemUtils.sleep(200);
        
        progress.setDetail(PREPARE_JAVA_LIST);
        if (SystemUtils.isWindows()) {
            fetchLocationsFromWindowsRegistry(locations);
        }
        fetchLocationsFromEnvironment(locations);
        fetchLocationsFromFilesystem(locations);
        fetchLocationsFromRegistry(locations);
        getJavaLocationsInfo(locations, progress);
        
        sortJavaLocations();
        
        progress.setDetail(StringUtils.EMPTY_STRING);
        progress.setPercentage(Progress.COMPLETE);
        
        SystemUtils.sleep(200);
        LogManager.logExit("... end of search for all java locations");
    }
    
    @Override
    public boolean canExecuteForward() {
        return javaLocations.isEmpty();
    }
    
     // private //////////////////////////////////////////////////////////////////////
    private static String getLabel(File javaHome) {
        JavaInfo javaInfo = JavaUtils.getInfo(javaHome);
        
        return getLabel(javaHome, javaInfo);
    }    
    
    private static String getLabel(File javaHome, JavaInfo javaInfo) {
        if (javaInfo.isNonFinal()) {
            return StringUtils.format(JAVA_ENTRY_LABEL_NON_FINAL,
                    javaHome,
                    javaInfo.getVersion().toJdkStyle(),
                    javaInfo.getArch(),
                    javaInfo.getVendor());
        } else {
            return StringUtils.format(JAVA_ENTRY_LABEL,
                    javaHome,
                    javaInfo.getVersion().toJdkStyle(),
                    javaInfo.getArch(),
                    javaInfo.getVendor());
        }
    }
    
    private static String getLabel(File javaHome, Version version, String vendor) {
        JavaInfo javaInfo = JavaUtils.getInfo(javaHome);
        if (javaInfo == null) {
            return StringUtils.format(JAVA_ENTRY_LABEL_NO_ARCH,
                    javaHome,
                    version.toJdkStyle(),
                    vendor);
        } else {
            return StringUtils.format(JAVA_ENTRY_LABEL,
                    javaHome,
                    version.toJdkStyle(),
                    javaInfo.getArch(),
                    vendor);
        }
    }
    
    private void getJavaLocationsInfo(List <File> locations, Progress progress) {
        for (int i = 0; i < locations.size(); i++) {
            File javaHome = locations.get(i).getAbsoluteFile();
            
            progress.setDetail(StringUtils.format(CHECKING, javaHome));
            
            if (isCanceled()) return; // check for cancel status
            
            LogManager.logIndent("investigating java home candidate: " + javaHome);
            
            // check whether it is a java installation - the result will be null if
            // it is not
            JavaInfo javaInfo = JavaUtils.getInfo(javaHome);

            if(javaInfo == null) {
                final File jreHome = new File(javaHome, "jre");
                if(FileUtils.exists(jreHome)) {
                    LogManager.log("investigating java home candidate: " + jreHome);
                    javaInfo = JavaUtils.getInfo(jreHome);
                    if(javaInfo!=null) {
                        javaHome = jreHome;
                    }
                }
            }
            
            if (javaInfo != null) {
                LogManager.logUnindent(
                        "... parsed java: " + javaInfo.getVersion() + " " + // NOI18N
                        "by " + javaInfo.getVendor() + "; " + // NOI18N
                        "final=" + !javaInfo.isNonFinal()); // NOI18N
                
                // filter out "private" jres
                if (javaHome.getName().equals("jre") &&
                        JavaUtils.isJdk(javaHome.getParentFile())) {
                    continue;
                }
                
                // add the location to the list if it's not already there
                addJavaLocation(javaHome);
            } else {
                LogManager.unindent();
            }
            SystemUtils.sleep(5);
            progress.setPercentage(Progress.COMPLETE * i / locations.size());            
        }
        
    }
    
    public static void sortJavaLocations(){
        // sort the found java installations:
        //   1) by final/non-final
        //   2) by version descending (6 has priority over 7u9 and older)
        //   3) by path acending
        //   4) by vendor descending (so Sun comes first, hehe)
        for (int i = 0; i < javaLocations.size(); i++) {
            for (int j = javaLocations.size() - 1; j > i ; j--) {
                File file1 = javaLocations.get(j);
                File file2 = javaLocations.get(j - 1);
                
                String label1 = javaLabels.get(j);
                String label2 = javaLabels.get(j - 1);
                
                JavaInfo info1 = JavaUtils.getInfo(javaLocations.get(j));
                JavaInfo info2 = JavaUtils.getInfo(javaLocations.get(j - 1));
                                               
                if (info1.isNonFinal() == info2.isNonFinal()) {
                    if (JavaUtils.isRecommended(info1.getVersion()) == JavaUtils.isRecommended(info2.getVersion())) {                        
                        if (info1.getVersion().equals(info2.getVersion())) {
                            // better than compare directly archs is to compare if archs are both 64bit
                            if (info1.getArch().endsWith("64") == info2.getArch().endsWith("64")) {                                                            
                                if (file1.getPath().compareTo(file2.getPath()) == 0) {
                                    if (info1.getVendor().compareTo(info2.getVendor()) == 0) {
                                        continue;
                                    } else if (info1.getVendor().compareTo(info2.getVendor()) < 0) {
                                        switchNeighbours(j, file2, file1, label2, label1);
                                    }
                                } else if (file1.getPath().length() < file2.getPath().length()) {
                                    switchNeighbours(j, file2, file1, label2, label1);
                                }
                            } else if (info1.getArch().endsWith("64") && !info2.getArch().endsWith("64")) {
                                switchNeighbours(j, file2, file1, label2, label1);
                            }
                        } else if (info1.getVersion().newerThan(info2.getVersion())) {
                            switchNeighbours(j, file2, file1, label2, label1);
                        }
                    } else if (JavaUtils.isRecommended(info1.getVersion()) && !JavaUtils.isRecommended(info2.getVersion())) {
                        switchNeighbours(j, file2, file1, label2, label1);
                    }                    
                } else if (!info1.isNonFinal() && info2.isNonFinal()) {
                    switchNeighbours(j, file2, file1, label2, label1);
                }
            }
        }        
    }        

    private static void switchNeighbours (int j, File file2, File file1, String label2, String label1) {
        javaLocations.set(j, file2);
        javaLocations.set(j - 1, file1);

        javaLabels.set(j, label2);
        javaLabels.set(j - 1, label1);
    }
    
    public static void addJavaLocation(File location, Version version, String vendor) {
        File javaHome = getCanonicalFile(location);
        if (!javaLocations.contains(javaHome)) {
            javaLocations.add(javaHome);
            javaLabels.add(getLabel(javaHome, version, vendor));
            JavaUtils.addJavaInfo(javaHome, new JavaInfo(version, vendor));
        }
    }

    public static void addJavaLocation(File location) {
        File javaHome = getCanonicalFile(location);
        if (!javaLocations.contains(javaHome)) {
            javaLocations.add(javaHome);
            javaLabels.add(getLabel(javaHome));
        }
    }
    
    public static List <File> getJavaLocations() {
        return javaLocations;
    }
    
    public static List <String> getJavaLabels() {
        return javaLabels;
    }
    
    private static File getCanonicalFile(final File file) {
        File location = file;
        if (SystemUtils.isWindows() && location.getAbsolutePath().matches(".*~[0-9]+.*")) {
            //Issue #166036
            try {
                // if C:\Program Files == C:\Progra~1, get canonical representation
                location = location.getCanonicalFile();
                if(location!=file) {
                    LogManager.log("... using " + location + " instead of " + file);
                }
            } catch (IOException e) {
            }
        }
        return location;
    }

    private void fetchLocationsFromFilesystem(final List<File> locations) {
        final List<String> candidateLocations = new ArrayList<String>();
        
        for(String location : JAVA_FILESYSTEM_LOCATIONS_COMMON) {
            candidateLocations.add(location);
        }
        
        if (SystemUtils.isWindows()) {
            candidateLocations.addAll(
                    Arrays.asList(JAVA_FILESYSTEM_LOCATIONS_WINDOWS));
        } else if (SystemUtils.isMacOS()) {
            candidateLocations.addAll(
                    Arrays.asList(JAVA_FILESYSTEM_LOCATIONS_MACOSX));
        } else {
            candidateLocations.addAll(
                    Arrays.asList(JAVA_FILESYSTEM_LOCATIONS_UNIX));
        }
        final File currentJava = SystemUtils.getCurrentJavaHome();
        final File currentJavaParentDir = currentJava.getParentFile();
        if(currentJavaParentDir!=null) {
            if(JavaUtils.isJavaHome(currentJava) && JavaUtils.isJdk(currentJavaParentDir)) {
                //installer runs on private JRE so perform search for
                //all children of the parent directory of the corresponding JDK
                String parentDir = currentJavaParentDir.getParent();
                if ( parentDir != null ) {
                    if(!candidateLocations.contains(parentDir)) {
                        candidateLocations.add(parentDir);
                    }
                }
            }
        }
        for (String location: candidateLocations) {
            final File parent = SystemUtils.resolvePath(location);
            
            if (parent.exists() && parent.isDirectory()) {
                locations.add(parent);
                final boolean isWindows = SystemUtils.isWindows();
                final boolean isSolaris = SystemUtils.isSolaris();
                final File[] children = parent.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File pathname) {
                        return pathname.isDirectory() && 
                                (!isSolaris || !pathname.equals(new File("/export/sybase"))) && //workaround for #143292
                                (isWindows || !pathname.getName().startsWith("."));
                    }
                });
                
                if (children != null) {
                    for (File child: children) {
                        locations.add(child);
                    }
                } else {
                    LogManager.log(ErrorLevel.DEBUG,
                            "Can`t get children of existing directory : " + parent.getPath());
                }
            }
        }
    }
    
    private void fetchLocationsFromEnvironment(final List<File> locations) {
        LogManager.logIndent("checking for possible java locations in environment");
        
        for (String name: JAVA_ENVIRONMENT_VARIABLES) {
            final String value = System.getenv(name);
            
            if (value != null) {
                LogManager.log("found: " + name + " = " + value); // NOI18N
                
                final File file = new File(value).getAbsoluteFile();
                if (!locations.contains(file)) {
                    locations.add(file);
                }
            }
        }
        
        LogManager.logUnindent("... finished");
    }
    
    private void fetchLocationsFromWindowsRegistry(final List<File> locations) {
        LogManager.logIndent("checking for possible java locations in windows registry");
        
        final WindowsNativeUtils nativeUtils =
                ((WindowsNativeUtils) SystemUtils.getNativeUtils());
        final WindowsRegistry registry =
                nativeUtils.getWindowsRegistry();
        
        final int currentMode = registry.getMode();
        List <Boolean> modes = new ArrayList <> ();
        modes.add(null); //default mode
        
        if(registry.isAlternativeModeSupported()) {
            LogManager.log("... alternative registry view is also supported");
            modes.add(true);//alternative mode
        }        
        try {
          for (Boolean mode : modes) {
            registry.setMode(mode);
            for (int section : new int[]{HKLM, HKCU}) {
                for (String path: JAVA_WINDOWS_REGISTRY_ENTRIES) {
                    // check whether current path exists in this section
                    if (!registry.keyExists(section, path)) {
                        continue;
                    }
                    
                    // get the names of all installed jdks
                    String[] keys = registry.getSubKeyNames(section, path);
                    
                    // iterate over the list of jdks, checking their versions
                    // and taking actions appropriate to the current search
                    // mode
                    for (int i = 0; i < keys.length; i++) {
                        // get the name of the current examined jdk
                        String key = keys[i];
                        
                        // get the java home of the current jdk, if it exists
                        if (!registry.valueExists(
                                section,
                                path + WindowsRegistry.SEPARATOR + key,
                                JavaUtils.JAVAHOME_VALUE)) {
                            continue;
                        }
                        
                        final String javaHome = registry.getStringValue(
                                section,
                                path + WindowsRegistry.SEPARATOR + key,
                                JavaUtils.JAVAHOME_VALUE,
                                false);
                        
                        LogManager.log("found: " + (section == HKLM ?
                            "HKEY_LOCAL_MACHINE" : "HKEY_CURRENT_USER") + // NOI18N
                                "\\" + path + "\\" + key + "\\" + // NOI18N
                                JavaUtils.JAVAHOME_VALUE + " = " + javaHome); // NOI18N
                        
                        
                        // add java home to the list if it's not there already
                        final File file = new File(javaHome);
                        if (file.exists() &&
                                file.isDirectory() &&
                                !locations.contains(file)) {
                            locations.add(file);
                        }
                    }
                }
            }
          }
        } catch (NativeException e) {
            ErrorManager.notify(ErrorLevel.DEBUG, "Failed to search in the windows registry", e);
        } finally {
            registry.setMode(currentMode);
        }
        
        LogManager.logUnindent("... finished");
    }
    
    private void fetchLocationsFromRegistry(final List<File> locations) {
        for (Product jdk: Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
            if (jdk.getStatus() == Status.INSTALLED) {
                if (!locations.contains(jdk.getInstallationLocation())) {
                    locations.add(jdk.getInstallationLocation());
                }
            }
        }
        
        for (Product product: Registry.getInstance().getProducts(Status.TO_BE_INSTALLED)) {
            final String jdkSysPropName = product.getUid() + StringUtils.DOT +
                    JdkLocationPanel.JDK_LOCATION_PROPERTY;
            final String jdkSysProp = System.getProperty(jdkSysPropName);
            if (jdkSysProp != null) {
                File sprop = new File(jdkSysProp);
                if (!locations.contains(sprop)) {
                    locations.add(sprop);
                }
            }
        }
    }
        
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.description"); // NOI18N
    public static final String PREPARE_JAVA_LIST =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.prepare.list"); //NOI18N
    public static final String CHECKING =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.checking");//NOI18N
    public static final String SEARCH_INSTALLED_JAVAS =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.search.java");//NOI18N
    public static final String JAVA_ENTRY_LABEL =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.entry.label");//NOI18N
    public static final String JAVA_ENTRY_LABEL_NO_ARCH =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.entry.label.noarch");//NOI18N
    public static final String JAVA_ENTRY_LABEL_NON_FINAL =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.entry.label.non.final");//NOI18N
    
    private static final String JDK_PRODUCT_UID = "jdk"; //NOI18N
    
    public static final String [] JAVA_WINDOWS_REGISTRY_ENTRIES = new String [] {
        "SOFTWARE\\JavaSoft\\Java Development Kit", // NOI18N
        "SOFTWARE\\JRockit\\Java Development Kit", // NOI18N
        "SOFTWARE\\IBM\\Java Development Kit", // NOI18N
        "SOFTWARE\\IBM\\Java2 Development Kit", // NOI18N
        
        "SOFTWARE\\JavaSoft\\Java Runtime Environment", // NOI18N
        "SOFTWARE\\JRockit\\Java Runtime Environment", // NOI18N
        "SOFTWARE\\IBM\\Java Runtime Environment", // NOI18N
        "SOFTWARE\\IBM\\Java2 Runtime Environment" // NOI18N
    };
    
    public static final String[] JAVA_ENVIRONMENT_VARIABLES = new String[] {
        "JAVA_HOME", // NOI18N
        "JAVAHOME", // NOI18N
        "JAVA_PATH", // NOI18N
        "JDK_HOME", // NOI18N
        "JDKHOME", // NOI18N
        "ANT_JAVA", // NOI18N
        "JAVA", // NOI18N
        "JDK" // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_COMMON = new String[] {
        "$S{java.home}", // NOI18N
        "$S{java.home}/.." // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_UNIX = new String[] {
        "$N{home}", // NOI18N
        "$N{home}/java", // NOI18N
        "$N{home}/jdk", // NOI18N
        "$N{home}/Java", // NOI18N
        
        "$N{install}", // NOI18N
        "$N{install}/Java", // NOI18N
        
        "/usr", // NOI18N
        "/usr/jdk", // NOI18N
        "/usr/jdk/instances", // NOI18N
        "/usr/java", // NOI18N
        "/usr/java/jdk", // NOI18N
        "/usr/j2se", // NOI18N
        "/usr/j2sdk", // NOI18N
               
        "/usr/local", // NOI18N
        "/usr/local/jdk", // NOI18N
        "/usr/local/jdk/instances", // NOI18N
        "/usr/local/java", // NOI18N
        "/usr/local/j2se", // NOI18N
        "/usr/local/j2sdk", // NOI18N
        
        "/export", // NOI18N
        "/export/jdk", // NOI18N
        "/export/jdk/instances", // NOI18N
        "/export/java", // NOI18N
        "/export/j2se",  // NOI18N
        "/export/j2sdk", // NOI18N
        
        "/opt", // NOI18N
        "/opt/jdk", // NOI18N
        "/opt/jdk/instances", // NOI18N
        "/opt/java", // NOI18N
        "/opt/j2se",  // NOI18N
        "/opt/j2sdk", // NOI18N
        
        "/usr/lib/jvm", // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_MACOSX = new String[] {
        "/Library/Java", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.7", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.7.0" // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_WINDOWS = new String[] {
        "$N{install}", // NOI18N
        "$N{install}/Java" // NOI18N
    };
}
