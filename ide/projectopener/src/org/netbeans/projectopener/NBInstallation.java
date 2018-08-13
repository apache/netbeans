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

package org.netbeans.projectopener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Milan Kubec
 */
public class NBInstallation {
    
    public static final Comparator VERSION_COMPARATOR = new VersionComparator();
    public static final Comparator LAST_USED_COMPARATOR = new LastUsedComparator();
    
    private static Logger LOGGER = WSProjectOpener.LOGGER;
    
    // there are two versions of log file
    private static final String searchStr1 = "Installation; User Dir.";
    private static final String searchStr2 = "Installation";
    
    // regexp for matching nb cluster, e.g. nb5.5
    private static final Pattern NB_CLUSTER_REGEX = Pattern.compile("[/\\\\]nb(\\d*(\\.\\d+)*)$");
    // regexp for matching start of windows path, e.g. e:\
    private static final Pattern WIN_ROOT_REGEX = Pattern.compile("^[a-zA-Z]:\\\\");
    
    private static final String[] NON_CLUSTER_DIRS = new String[] { "etc", "bin", "harness" };
    
    private File userDir;
    private File installDir;
    private String ideVersion;
    
    private File logFile;
    private File lockFile;
    
    private String versionParts[];
    
    /** Creates a new instance of NetBeansInstallation */
    public NBInstallation(File userDir) {
        this.userDir = userDir;
        logFile = new File(new File(new File(userDir, "var"), "log"), "messages.log");
        ideVersion = findVersion();
        try {
            installDir = findInstallDir();
        } catch (Exception ex) {
            LOGGER.info("Exception during searching for install dir: " + Utils.exc2String(ex));
        }
        versionParts = Utils.getVersionParts(ideVersion);
        lockFile = new File(userDir, "lock");
    }
    
    public boolean isLocked() {
        if (lockFile.exists()) {
            return true;
        }
        return false;
    }
    
    public boolean isValid() {
        if (isNBUserdir() && Utils.getVersionParts(ideVersion) != null &&
                installDir != null && installDir.exists() && installDir.isDirectory() && 
                new File(installDir, "bin").exists()) {
            return true;
        }
        return false;
    }
    
    public File getInstallDir() {
        return installDir;
    }
    
    public File getExecDir() {
        return new File(installDir, "bin");
    }
    
    /** 
     * Tries to find important files for each project type in any cluster in installDir,
     * XXX should be extended to look also in userDir!
     */
    public boolean canHandle(Collection c) {
        int toBeFound = 0;
        int foundFiles = 0;
        for (Iterator iter = c.iterator(); iter.hasNext(); ) {
            ProjectType pt = (ProjectType) iter.next();
            String impFiles[] = pt.getImportantFiles();
            toBeFound += impFiles.length;
            String [] clusterPaths = getClusterDirPaths();
            for (int i = 0; i < impFiles.length; i++) {
                String iPath = impFiles[i].replace('/', File.separatorChar);
                for (int j = 0; j < clusterPaths.length; j++) {
                    String searchPath = clusterPaths[j] + File.separator + iPath;
                    LOGGER.fine("Looking for: " + searchPath);
                    File f = new File(searchPath);
                    if (f.exists()) {
                        LOGGER.info("File: " + f.getAbsolutePath() + " exists.");
                        foundFiles++;
                    }
                }
            }
        }
        if (foundFiles == toBeFound) {
            return true;
        }
        return false;
    }
    
    /** 
     * Returns all dirs under installDir that are not listed in 
     * NON_CLUSTER_DIRS, they are considered to be clusters
     */
    public File[] getClusterDirs() {
        File[] dirs = installDir.listFiles(new FileFilter(){
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    for (int i = 0; i < NON_CLUSTER_DIRS.length; i++) {
                        if (f.getName().equals(NON_CLUSTER_DIRS[i])) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        return dirs;
    }
    
    private String[] getClusterDirPaths() {
        File[] files = getClusterDirs();
        String dirPaths[] = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            dirPaths[i] = files[i].getAbsolutePath();
        }
        return dirPaths;
    }
    
    public long lastUsed() {
        if (logFile.exists()) {
            return logFile.lastModified();
        }
        return 0L;
    }
    
    // from 5.5.1beta2 => 5.5.1
    public String numVersion() {
        if (versionParts != null && !versionParts[0].equals("")) {
            return versionParts[0];
        }
        // fallback to avoid problems when dev version
        // userdir doesn't have a numeric version
        return "1.0";
    }
    
    // from 5.5.1beta2 => beta
    public String releaseType() {
        if (versionParts != null && !versionParts[1].equals("")) {
            return versionParts[1];
        }
        return "";
    }
    
    // from 5.5.1beta2 => 2
    public String releaseVersion() {
        if (versionParts != null && !versionParts[2].equals("")) {
            return versionParts[2];
        }
        return "";
    }
    
    public String toString() {
        return userDir.getAbsolutePath();
    }
    
    // ---
    
    public static class LastUsedComparator implements Comparator {
        
        public int compare(Object arg0, Object arg1) {
            return signum(((NBInstallation) arg0).lastUsed() - 
                    ((NBInstallation) arg1).lastUsed());
        }
        
        private int signum(long diff) {
            if (diff > 0) return 1;
            if (diff < 0) return -1;
            return 0;
        }
        
    }
    
    public static class VersionComparator implements Comparator {
        
        public int compare(Object arg0, Object arg1) {
            int retVal = 0;
            String v0 = ((NBInstallation) arg0).numVersion();
            String v1 = ((NBInstallation) arg1).numVersion();
            // this is because dev version doesn't have any numbers,
            // so 'dev' means lower version always
            if (v0.equals("")) {
                retVal = -1;
            } else if (v1.equals("")) {
                retVal = 1;
            }
            if (retVal == 0) {
                retVal = Utils.compareVersions(v0, v1);
            }
            if (retVal == 0) {
                v0 = ((NBInstallation) arg0).releaseType();
                v1 = ((NBInstallation) arg1).releaseType();
                retVal = Utils.compareReleaseTypes(v0, v1);
            }
            if (retVal == 0) {
                v0 = ((NBInstallation) arg0).releaseVersion();
                v1 = ((NBInstallation) arg1).releaseVersion();
                retVal = Utils.compareVersions(v0, v1);
            }
            return retVal;
        }
        
    }
    
    // ---
    
    private File findInstallDir() throws FileNotFoundException, IOException {
        String dirPath = null;
        LOGGER.fine("Parsing file: " + logFile.getAbsolutePath());
        BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));
        String line = logFileReader.readLine();
        boolean lineRead;
        
        while (line != null) {
            
            lineRead = false;
            if (line.indexOf(searchStr1) != -1) { // old version of log file
                LOGGER.fine("Found line: " + line);
                int index1 = line.indexOf('=') + 2;
                int index2 = line.indexOf("; ", index1);
                String subStr = line.substring(index1, index2);
                LOGGER.fine("Found substring: " + subStr);
                StringTokenizer tokenizer = new StringTokenizer(subStr, File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {
                    String instPart = tokenizer.nextToken();
                    LOGGER.fine("Testing token: " + instPart);
                    // regex matcher looking for nb cluster e.g. nb5.5
                    Matcher matcher = NB_CLUSTER_REGEX.matcher(instPart);
                    if (matcher.find()) {
                        File f = new File(instPart).getParentFile();
                        LOGGER.fine("Found file: " + f.getAbsolutePath());
                        if (f.exists()) {
                            dirPath = f.getAbsolutePath();
                        }
                    }
                }
            } else if (line.indexOf(searchStr2) != -1) { // new version of log file
                LOGGER.fine("Found line: " + line);
                int index = line.indexOf('=') + 2;
                String tLine = line.substring(index).trim();
                boolean matching;
                
                do {
                    matching = false;
                    // startsWith("/") OR startsWith("e:\")
                    if (tLine.startsWith("/") || matchWinRoot(tLine)) { // correct line matched
                        matching = true;
                        LOGGER.fine("Matching line: " + tLine);
                        Matcher matcher = NB_CLUSTER_REGEX.matcher(tLine);
                        if (matcher.find()) { // nb cluster matched
                            File f = new File(tLine).getParentFile();
                            LOGGER.fine("Found file: " + f.getAbsolutePath());
                            if (f.exists()) {
                                dirPath = f.getAbsolutePath();
                            }
                        }
                        line = logFileReader.readLine();
                        lineRead = true;
                        tLine = line.trim();
                    }
                } while (matching);
                
            }
            
            if (!lineRead) {
                line = logFileReader.readLine();
            }
            
        }
        
        if (dirPath != null) {
            return new File(dirPath);
        } else {
            return null;
        }
    }
    
    private boolean matchWinRoot(String line) {
        Matcher matcher = WIN_ROOT_REGEX.matcher(line);
        return matcher.find();
    }
    
    // XXX the version might be read from log file
    private String findVersion() {
        return userDir.getName();
    }
    
    // check if build.properties and var/log/messages.log files exist
    private boolean isNBUserdir() {
        File buildProps = new File(userDir, "build.properties");
        if (buildProps.exists() && logFile.exists()) {
            return true;
        }
        return false;
    }

    // ---
    
    // will be used to validate folder as NB install dir
    // after user selects some folder
    public static boolean isNBInstallation(File f) {
        if (new File(f, "bin").exists()) {
            return true;
        }
        return false;
    }
    
}
