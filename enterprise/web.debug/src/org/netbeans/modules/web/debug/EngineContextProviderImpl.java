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

package org.netbeans.modules.web.debug;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 *
 * @author Jan Jancura
 */
public class EngineContextProviderImpl extends SourcePathProvider {

    private static final Logger LOGGER = Logger.getLogger(EngineContextProviderImpl.class.getName());
    
    private static boolean verbose =
        System.getProperty ("netbeans.debugger.enginecontextproviderimpl") != null;

    private static final Set virtualFolders = new HashSet (Arrays.asList (
        new String[] {
            "org", // NOI18N
            "org/apache", // NOI18N
            "org/apache/jsp", // NOI18N
            "jsp_servlet"
        }
    ));
    
    private final ContextProvider contextProvider;
    
    public EngineContextProviderImpl(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    
    /**
     * This method is workaround for {@link org.netbeans.modules.debugger.jpda.ui.SmartSteppingImpl#stopHere}
     * which is asking for relative paths representing folders. Based on this
     * queries non-existent packages are filtered out next time. However folders
     * representing org.apache.jsp usually does not exist and on the other hand
     * they can't be filtered out (we would filter out any jsp).
     * <p>
     * In such case method is returning valid url to nonexistent directory.
     * <p>
     * {@inheritDoc}
     */
    // FIXME this should be fixed in SmartSteppingImpl after 6.1
    public String getURL (String relativePath, boolean global) {
        if (verbose) System.out.println ("ECPI(JSP): getURL " + relativePath + " global " + global);
        if ((relativePath == null) || (relativePath.endsWith(".java"))) {
           return null; 
        }
        if (virtualFolders.contains (relativePath)
                || relativePath.startsWith("org/apache/jsp")) { // NOI18N
            if (verbose) System.out.println ("ECPI(JSP):  fo virtual folder");

            String userDir = System.getProperty("netbeans.user"); // NOI18N
            try {
                if (userDir != null) {
                    File virtual = new File(userDir, "var" + File.separator + "virtual" + System.currentTimeMillis()); // NOI18N                    
                    String url = virtual.toURI().toURL().toString();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Returning fake URL: " + url);
                    }
                    return url;
                }
            } catch (MalformedURLException ex) {
                // return default
            }
            String temp = System.getProperty("java.io.tmpdir"); // NOI18N
            try {
                if (temp != null) {
                    File virtual = new File(temp, "virtual" + System.currentTimeMillis()); // NOI18N
                    String url = virtual.toURI().toURL().toString();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Returning fake URL: " + url);
                    }
                    return url;
                }
            } catch (MalformedURLException ex) {
                // return default
            }

            LOGGER.log(Level.INFO, "Both netbeans.user and java.io.tmpdir properties are missing, returning null");
            return null;
        }
        // XXX terrible hack - in addition WL specific code
        if (relativePath.startsWith("jsp_servlet")) { // NOI18N
            SourcePathProvider provider = getDefaultContext();
            if (provider != null) {
                String path = relativePath.substring(11);
                path = path.replace("/_", "/"); // NOI8N
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                return provider.getURL(path, global);
            }
        }
        return null;
    }

    private SourcePathProvider getDefaultContext() {
        List<? extends SourcePathProvider> providers = contextProvider.lookup(null, SourcePathProvider.class);
        for (SourcePathProvider provider : providers) {
            // Hack - find our provider:
            if (provider != null &&
                provider.getClass().getName().equals("org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl")) {
                return (SourcePathProvider) provider;
            }
        }
        return null;
    }    
    
    /**
     * Returns relative path for given url.
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
    ) {
        return null;
    }
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public String[] getOriginalSourceRoots () {
        return new String [0];
    }
    
    /**
     * Returns array of source roots.
     *
     * @return array of source roots
     */
    public String[] getSourceRoots () {
        return new String [0];
    }
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public void setSourceRoots (String[] sourceRoots) {
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
    }
}
