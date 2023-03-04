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

package org.netbeans.libs.svnclientadapter.javahl;

import java.io.File;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;

/**
 *
 * @author Tomas Stupka
 */
@ServiceProviders({@ServiceProvider(service=SvnClientAdapterFactory.class)})
public class JavaHlClientAdapterFactory extends SvnClientAdapterFactory {

    private static final String SUBVERSION_NATIVE_LIBRARY = "subversion.native.library";
    private static final String[] COLLABNET_LIBRARIES = new String[] { 
        "msvcr100.dll", //NOI18N
        "msvcp100.dll", //NOI18N
        "libapr-1.dll", //NOI18N
        "libapriconv-1.dll", //NOI18N
        "libeay32.dll", //NOI18N
        "ssleay32.dll", //NOI18N
        "libaprutil-1.dll", //NOI18N
        "dbghelp.dll", //NOI18N
        "libsasl.dll", //NOI18N
        "libsvn_subr-1.dll", //NOI18N
        "libsvn_delta-1.dll", //NOI18N
        "libsvn_diff-1.dll", //NOI18N
        "libsvn_wc-1.dll", //NOI18N
        "libsvn_fs-1.dll", //NOI18N
        "libsvn_repos-1.dll", //NOI18N
        "libsvn_ra-1.dll", //NOI18N
        "libsvn_client-1.dll" //NOI18N
    };
    private static final String SLIKSVN_CLIENT_LIBRARY = "SlikSvn-svn_client-1.dll"; //NOI18N
    private static final String[] SLIKSVN_LIBRARIES = new String[] { 
        "SlikSvn-DB44-20-win32.dll", //NOI18N
        "SlikSvn-DB44-20-x64.dll", //NOI18N
        "SlikSvn-libapr-1.dll", //NOI18N
        "SlikSvn-libaprutil-1.dll", //NOI18N
        "SlikSvn-libeay32.dll", //NOI18N
        "SlikSvn-libintl-Win32.dll", //NOI18N
        "SlikSvn-libintl-x64.dll", //NOI18N
        "SlikSvn-ssleay32.dll", //NOI18N
        "SlikSvn-Sasl21-23-win32.dll", //NOI18N
        "SlikSvn-Sasl21-23-x64.dll", //NOI18N
        "SlikSvn-svn_subr-1.dll", //NOI18N
        "SlikSvn-svn_delta-1.dll", //NOI18N
        "SlikSvn-svn_diff-1.dll", //NOI18N
        "SlikSvn-svn_wc-1.dll", //NOI18N
        "SlikSvn-svn_fs-1.dll", //NOI18N
        "SlikSvn-svn_repos-1.dll", //NOI18N
        "SlikSvn-svn_ra-1.dll", //NOI18N
        SLIKSVN_CLIENT_LIBRARY
    };
    
    private boolean available = false;

    public JavaHlClientAdapterFactory() {
        super();
    }

    @Override
    public Client provides() {
        return Client.JAVAHL;
    }

    @Override
    public boolean isAvailable() {
        if(!available) {                    
            presetJavahl();
            try {
                JhlClientAdapterFactory.setup();
            } catch (Throwable t) {
                if (!"factory for type javahl already registered".equals(t.getMessage())) {
                    String jhlErorrs = JhlClientAdapterFactory.getLibraryLoadErrors();
                    LOG.log(Level.INFO, t.getMessage());
                    LOG.log(Level.WARNING, "{0}\n", jhlErorrs);                                             // NOI18N
                    return false;   
                }
            }
            String version = getVersion();
            if(!isSupportedJavahlVersion(version)) {
                LOG.log(Level.INFO, "Unsupported version {0} of subversion javahl bindings.", version); // NOI18N
                return false;
            }
            available = JhlClientAdapterFactory.isAvailable();
        }
        return available;
    }

    @Override
    public ISVNClientAdapter createClient() {
        return JhlClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
    }

    private boolean isSupportedJavahlVersion(String version) {
        boolean retval = false;
        if (version != null) {
            version = version.toLowerCase();
            if (version.startsWith("1.14") ||                                                        // NOI18N
                version.contains("version 1.14"))                                                    // NOI18N
            {
                retval = true;
            }
        }
        return retval;
    }

    private String getVersion() {
        ISVNClientAdapter adapter = createClient();
        if (adapter != null) {
            JhlClientAdapter jhlAdapter = (JhlClientAdapter) adapter;
            String version = jhlAdapter.getNativeLibraryVersionString();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "getVersion: version {0}", version);                            // NOI18N
            }
            return version;
        }
        return null;
    }

    private void presetJavahl() {
        if(Utilities.isUnix() && !Utilities.isMac() ) { // javahl for mac is already bundled
            presetJavahlUnix();
        } else if(Utilities.isWindows()) {
            presetJavahlWindows();
        }
    }

    private void presetJavahlUnix() {
        LOG.log(Level.FINE, "looking for svn native library...");
        String libPath = System.getProperty(SUBVERSION_NATIVE_LIBRARY);
        if (libPath != null && !libPath.trim().equals("")) {
            LOG.log(Level.FINE, "won't preset javahl due to subversion.native.library={0}", new Object[] { libPath });
            return;
        }
        String name = "libsvnjavahl-1.so";
        String[] locations = new String[] {"/usr/lib/", "/usr/lib/jni/", "/usr/local/lib/",
            "/usr/lib64/", "/usr/lib64/jni/", "/usr/local/lib64/", "/opt/csw/bin/svn/", 
            "/usr/lib/x86_64-linux-gnu/jni/", "/usr/lib/i386-linux-gnu/jni/"};
        File location = null;
        for (String loc : locations) {
            File file = new File(loc, name);
            LOG.log(Level.FINE, " checking existence of {0}", new Object[] { file.getAbsolutePath() });
            if (file.exists()) {
                location = file;
                break;
            }
        }
        if(location == null) {
            location = getJavahlFromExecutablePath(name);
        }
        if(location != null) {
            System.setProperty("subversion.native.library", location.getAbsolutePath());
            LOG.log(Level.FINE, "   found javahl library. Setting subversion.native.library={0}", new Object[] { location.getAbsolutePath() });
        }
    }

    private void presetJavahlWindows() {
        String libPath = System.getProperty(SUBVERSION_NATIVE_LIBRARY);
        if (libPath != null && !libPath.trim().equals("")) {
            // the path is already set -> lets ensure we load all dependencies
            // from the same folder and let then svnClientAdapter take care for the rest
            LOG.log(Level.FINE, "preset subversion.native.library={0}", new Object[] { libPath } );
            int idx = libPath.lastIndexOf(File.separator);
            if(idx > -1) {
                libPath = libPath.substring(0, idx);
                LOG.log(Level.FINE, "loading dependencies from ", new Object[] { libPath } );
                loadJavahlDependencies(libPath);
            }
            return;
        }
                
        File location = InstalledFileLocator.getDefault().locate("modules/lib/libsvnjavahl-1.dll", JAVAHL_WIN32_MODULE_CODE_NAME, false);
        if(location == null) {
            LOG.fine("could not find location for bundled javahl library");
            location = getJavahlFromExecutablePath("libsvnjavahl-1.dll");
            if(location == null) {
                return;
            }
        }
        // the library seems to be available in the netbeans install/user dir
        // => set it up so that it will used by the svnClientAdapter
        LOG.fine("libsvnjavahl-1.dll located : " + location.getAbsolutePath());
        String locationPath = location.getParentFile().getAbsolutePath();
        // svnClientAdapter workaround - we have to explicitly load the
        // libsvnjavahl-1 dependencies as svnClientAdapter  tryies to get them via loadLibrary.
        // That won't work i they aren't on java.library.path
        loadJavahlDependencies(locationPath);

        // libsvnjavahl-1 must be loaded by the svnClientAdapter to get the factory initialized
        locationPath = location.getAbsolutePath();
        LOG.log(Level.FINE, "setting subversion.native.library={0}", new Object[] { locationPath });
        System.setProperty("subversion.native.library", locationPath);
    }

    private void loadJavahlDependencies(String locationPath) {
        for (String filename : COLLABNET_LIBRARIES) {
            try { System.load(locationPath + "/" + filename); } catch (Throwable t) { LOG.log(Level.FINE, "cannot load library {0}", filename); } //NOI18N
        }
        // SlikSVN??
        if (new File(locationPath + "/" + SLIKSVN_CLIENT_LIBRARY).exists()) { //NOI18N
            for (String filename : SLIKSVN_LIBRARIES) {
                try { System.load(locationPath + "/" + filename); } catch (Throwable t) { LOG.log(Level.FINE, "cannot load library {0}", filename); } //NOI18N
            }
        }
    }    
    
    private File getJavahlFromExecutablePath(String libName) {
        Preferences prefs = org.openide.util.NbPreferences.root ().node ("org/netbeans/modules/subversion");
        
        String executablePath = prefs.get("svnExecBinary", "");
        if(executablePath == null || executablePath.trim().equals("")) {
            return null;
        }
        LOG.log(Level.FINE, "looking for svn native library in executable path={0}", new Object[] { executablePath });
        File location = new File(executablePath);
        if (location.isFile()) {
            location = location.getParentFile();
        }
        if (location != null) {
            location = new File(location.getAbsolutePath() + File.separatorChar + libName);
            if(location.exists()) {
                LOG.log(Level.FINE, "found svn native library in executable path={0}", new Object[] { location.getAbsolutePath() });
                return location;
            }
        }
        return null;
    }    
}