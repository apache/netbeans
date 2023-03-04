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
package org.netbeans.modules.cordova;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class CordovaPlatform {
    
    private static CordovaPlatform instance;
    
    private static String CORDOVA_SDK_ROOT_PREF = "cordova.home";//NOI18N

    private final Map<String, Version> versions = Collections.synchronizedMap(new HashMap<>());
    private boolean isGitReady;

    private final transient java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    private CordovaPlatform() {
    }

    public static synchronized CordovaPlatform getDefault() {
        if (instance == null) {
            instance = new CordovaPlatform();
        }
        return instance;
    }
    
    private static Pattern versionPattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+.*");
    
    public Version getVersion(String path) {
        if(path.length() > 0 ) { 
            if( !(new File(path).isFile() && path.endsWith("cordova")) ) {
                if(File.separatorChar != path.charAt(path.length() -1)) {
                    path += File.separatorChar;
                } 
                path = path + "cordova";
            }
        } else {
            path = path + "cordova";
        }
        
        Version version = versions.get(path);
        if (version == null) {
            version = loadVersionIntern(path);
            if(version != null) {
                versions.put(path, version);
            }
        }
        return version;
    }
    
    public Version getVersion() {
        return getVersion("");
    }

    protected Version loadVersionIntern(String path) {     
        
        Version.LOG.log(Level.INFO, "loading cordova version for path {0}", path);
        
        try {
            String v;
            if (Utilities.isWindows()) {
                v = ProcessUtilities.callProcess("cmd", true, 60*1000, "/C " + path + " -v");
            } else if (Utilities.isMac()) {
                v = ProcessUtilities.callProcess("/bin/bash", true, 60*1000, "-lc", path + " -v");
            } else {
                v = ProcessUtilities.callProcess(path, true, 60*1000, "-v");
            }
            if (versionPattern.matcher(v.trim()).matches()) {
                return new Version(v.trim());
            }
        } catch (IOException ex) {
            Version.LOG.log(Level.INFO, "Could not find cordova on PATH."); //NOI18N
        }
        return null;
    }
    

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }
    
    public boolean isReady() {
        return isGitReady() && getVersion() != null;
    }
    
    public static boolean isCordovaProject(Project project) {
        final FileObject root = project.getProjectDirectory();
        root.refresh();
        return root.getFileObject("hooks") != null || root.getFileObject(".cordova") != null; // NOI18N
    }

    private boolean isGitReady() {
        if (!isGitReady) {
            try {
                String v;
                if (Utilities.isWindows()) {
                    v = ProcessUtilities.callProcess("git.exe", true, 60*1000, "--version");
                } else if (Utilities.isMac()) {
                    v = ProcessUtilities.callProcess("/bin/bash", true, 60*1000, "-lc", "git --version");
                } else {
                    v = ProcessUtilities.callProcess("git", true, 60*1000, "--version");
                }
                
                if (v.contains("version")) {
                    isGitReady = true;
                }
            } catch (IOException ex) {
                Version.LOG.log(Level.INFO, "Could not find git on PATH."); //NOI18N
            }
        } 
        return isGitReady;
    }

    public static class Version {
        
        private SubVersion api;
        private SubVersion cli;
        private static Logger LOG = Logger.getLogger(Version.class.getName());

        public Version(String version) {
            LOG.fine("Cordova version " + version);

            if (version.contains("-")) {// prior 3.7.0, e.g. 3.5.0-0.2.7
                api = new SubVersion(version.substring(0, version.indexOf("-")));
                cli = new SubVersion(version.substring(version.indexOf("-") + 1));
            } else { // after 3.7.0
                api = new SubVersion(version);
                cli = new SubVersion("0.0.0");
            }
        }


        @Override
        public String toString() {
            return api.toString() + "-" + cli.toString();
        }
        
        public boolean isSupported() {
            return api.compareTo(new SubVersion(("3.0")))>0; // NOI18N
        }

        public final SubVersion getApiVersion() {
            return api;
        }

        public final SubVersion getCliVersion() {
            return cli;
        }
        
        public static class SubVersion implements Comparable<SubVersion> {

            private final String version;

            public SubVersion(String version) {
                this.version = version;
            }

            @Override
            public int compareTo(SubVersion o) {
                return version.compareTo(o.version);
            }

            @Override
            public String toString() {
                return version;
            }
        }
    }
}

