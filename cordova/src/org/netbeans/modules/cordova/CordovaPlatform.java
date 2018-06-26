/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

    private transient final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

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

