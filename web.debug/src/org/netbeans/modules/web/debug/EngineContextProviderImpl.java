/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                path = path.replaceAll("/_", "/"); // NOI8N
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
