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

package org.netbeans.modules.payara.spi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.netbeans.modules.payara.common.ServerDetails;
import org.netbeans.modules.payara.common.wizards.PayaraWizardProvider;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * General helper methods for accessing Payara server objects.
 *
 * @author Peter Williams
 * @author Ludovic Champenois
 * @author Gaurav Gupta
 */
public final class ServerUtilities {

    public static final int ACTION_TIMEOUT = 15000;
    public static final TimeUnit ACTION_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    public static final String PF_MODULES_DIR_NAME = "modules"; // NOI18N
    public static final String PF_LIB_DIR_NAME = "lib"; // NOI18N
    public static final String VERSION_MATCHER = "(?:-[0-9bSNAPHOT]+(?:\\.[0-9]+(?:_[0-9]+|)|).*|).jar"; // NOI18N
    public static final String GF_JAR_MATCHER = "glassfish" + VERSION_MATCHER; // NOI18N
    public static final String PROP_FIRST_RUN = "first_run";
    private final PayaraInstanceProvider pip;
    private final PayaraWizardProvider pwp;
    
    
    private ServerUtilities(PayaraInstanceProvider pip, PayaraWizardProvider pwp) {
        assert null != pip;
        this.pip = pip;
        this.pwp = pwp;
    }

    public static ServerUtilities getEe6Utilities() {
        PayaraInstanceProvider pip = PayaraInstanceProvider.getProvider();
        return null == pip ? null : new ServerUtilities(pip,
                PayaraWizardProvider.createEe6());
    }
        
    public static ServerUtilities getEe7Utilities() {
        PayaraInstanceProvider pip = PayaraInstanceProvider.getProvider();
        return null == pip ? null : new ServerUtilities(pip,
                PayaraWizardProvider.createEe7());
    }
    
    public static ServerUtilities getEe8Utilities() {
        PayaraInstanceProvider pip = PayaraInstanceProvider.getProvider();
        return null == pip ? null : new ServerUtilities(pip,
                PayaraWizardProvider.createEe8());
    }

    /**
     * Returns the ServerInstance object for the server with the specified URI.
     * 
     * @param uri uri identifying the server instance.
     * 
     * @return ServerInstance object for this server instance.
     */
    public ServerInstance getServerInstance(String uri) {
        ServerInstance retVal = pip.getInstance(uri);
        return retVal;
    }

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported Payara
     * server versions.
     */
    public static WizardDescriptor.InstantiatingIterator
            getInstantiatingIterator() {
        return ServerDetails.getInstantiatingIterator();
    }

    /**
     * Returns true if this server is registered or is in the process of being
     * registered.
     *
     * @param uri uri identifying the server instance.
     *
     * @return True if this server is or is being registered, false otherwise.
     */
    public boolean isRegisteredUri(String uri) {
        return pip.getInstance(uri) != null ||
                PayaraInstanceProvider.activeRegistrationSet.contains(uri);
    }

    /**
     * Returns the lookup object for a server instance when the caller only has
     * the public handle available via common server API.
     *
     * @param instance object for this server instance.
     *
     * @return Lookup object maintained by backing instance implementation
     */
    public Lookup getLookupFor(ServerInstance instance) {
        return pip.getLookupFor(instance);
    }

    /**
     * Returns the facade implementation for the specified server, if that server
     * supports the facade class passed in.
     * 
     * @param uri uri identifying the server instance.
     * @param serverFacadeClass class definition of the server facade we're
     *   looking for.
     * 
     * @return facade implementation for specified server or null if either the
     *   server does not exist or the server does not implement this facade.
     */
    public <T> T getInstanceByCapability(String uri, Class <T> serverFacadeClass) {
        return pip.getInstanceByCapability(uri, serverFacadeClass);
    }
    
    /**
     * Returns a list of the server instances that support the facade class 
     * specified (e.g. all servers that support <code>RubyInstance</code>).
     * 
     * @param serverFacadeClass class definition of the server facade we're
     *   looking for.
     * 
     * @return list of servers that support the interface specified or an empty
     *   list if no servers support that interface.
     */
    public<T> List<T> getInstancesByCapability(Class<T> serverFacadeClass) {
        return pip.getInstancesByCapability(serverFacadeClass);
    }
    
    /**
     * Returns the ServerInstanceImplementation instance for the server with the
     * specified URI.  Use when you need to avoid calling through the ServerInstance
     * facade wrapper.  Otherwise, you should call <code>getServerInstance()</code> instead.
     * 
     * @param uri uri identifying the server instance.
     * 
     * @return ServerInstanceImplementation object for this server instance.
     */
//    public static ServerInstanceImplementation getInternalServerInstance(String uri) {
//        return PayaraInstanceProvider.getDefault().getInternalInstance(uri);
//    }
    
    /**
     * Returns the ServerInstanceProvider for this server plugin so we don't 
     * have to look it up via common server SPI.
     * 
     * @return the Payara impl for ServerInstanceProvider.
     */
    public ServerInstanceProvider getServerProvider() {
        return pip;
    }
    
     /**
     * Returns the fqn jar name with the correct version 
     * 
     * @param payaraHome
     * @param jarNamePattern
     * @return the File with full path of the jar or null
     */
    public static File getJarName(String payaraHome, String jarNamePattern) {
        return getJarName(payaraHome, jarNamePattern, PF_MODULES_DIR_NAME);
    }

    public static File getJarName(String payaraHome, String jarNamePattern, String subdirectoryName) {
        File searchDirectory = new File(payaraHome + File.separatorChar + subdirectoryName);
        return Utils.getFileFromPattern(jarNamePattern, searchDirectory);
    }

     /**
     * Returns the fqn jar name with the correct version
     *
     * @param payaraHome
     * @param jarNamePattern
     * @return the File with full path of the jar or null
     */
    public static File getWsJarName(String payaraHome, String jarNamePattern) {
        File modulesDir = new File(payaraHome + File.separatorChar + PF_MODULES_DIR_NAME);
        File retVal = Utils.getFileFromPattern(jarNamePattern, modulesDir);
        if (null == retVal) {
            retVal = Utils.getFileFromPattern(jarNamePattern,
                    new File(modulesDir,"endorsed"));
        }
        return retVal;
    }
    
    /**
     * Get the url for a file, including proper protocol for archive files (jars).
     * 
     * @param file File to create URL from.
     * 
     * @return url URL for file with proper protocol specifier.
     * 
     * @throws java.net.MalformedURLException
     */
    public static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }    

    /**
     * Surround the submitted string with quotes if it contains any embedded
     * whitespace characters.
     *
     * Implementation note: Do not trim the submitted string.  Assume all
     * whitespace character are part of a file name or path that requires
     * quotes.
     *
     * !PW FIXME handles only spaces right now.  Should handle all whitespace.
     * !PW FIMME 4NT completion on Windows quotes paths if a folder has a comma
     *   in the name.  Might need that too, though I haven't proved it yet.      *
     * @param path
     * @return
     */
    public static final String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\"";
    }


    /**
     *  Determine if the named directory is a TP2 install.
     * 
     * @param gfRoot the name of the directory to check against.
     * @return true if the directory appears to be the root of a TP2 installation.
     */    
    static public boolean isTP2(String gfRoot) {
        return ServerUtilities.getJarName(gfRoot, ServerUtilities.GF_JAR_MATCHER).getName().indexOf("-tp-2-") > -1; // NOI18N
    }
  
    /**
     * create a list of jars that appear to be Java EE api jars that live in the 
     * modules directory.
     * 
     * @param jarList the list "so far"
     * @param parent the directory to look into. Should not be null.
     * @param depth depth of the server
     * @param escape pass true if backslashes in jar names should be escaped
     * @return the complete list of jars that match the selection criteria
     */
    public static List<String> filterByManifest(List<String> jarList, FileObject parent, int depth, boolean escape) {
        // be kind to clients that pass in null
        if(null != parent) {
            int parentLength = parent.getPath().length();
            /* modules/web/jsf-impl.jar was not seen (or added with wrong relative name).
             * need to calculate size relative to the modules/ dir and not the subdirs
             * notice: this works only for depth=0 or 1
             * not need to make it work deeper anyway
             * with this test, we now also return "web/jsf-impl.jar" which is correct
             */
            if (depth==1){
                parentLength = parent.getParent().getPath().length();
            }

            for(FileObject candidate: parent.getChildren()) {
                if(candidate.isFolder()) {
                    if(depth < 1) {
                        filterByManifest(jarList, candidate, depth+1, escape);
                    }
                    continue;
                } else if(!candidate.getNameExt().endsWith(".jar")) {
                    continue;
                }
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(FileUtil.toFile(candidate), false);
                    Manifest manifest = jarFile.getManifest();
                    if(manifest != null) {
                        Attributes attrs = manifest.getMainAttributes();
                        if(attrs != null) {
                            String bundleName = attrs.getValue("Bundle-SymbolicName");
                            //String bundleName = attrs.getValue("Extension-Name");
                            if(bundleName != null  && bundleName.contains("javax")) {
                                String val = candidate.getPath().substring(parentLength);
                                if(escape) {
                                    val = val.replace("\\", "\\\\");
                                }
                                jarList.add(val);
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerUtilities.class.getName()).log(Level.INFO, 
                            candidate.getPath(), ex);
                } finally {
                    if (null != jarFile) {
                        try {
                            jarFile.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ServerUtilities.class.getName()).log(Level.INFO,
                                    candidate.getPath(), ex);
                        }
                        jarFile = null;
                    }
                }

            }
        } else {
           Logger.getLogger(ServerUtilities.class.getName()).log(Level.FINER,
                            "Null FileObject passed in as the parent parameter. Returning the original list");
        }
        return jarList;
    }

}
