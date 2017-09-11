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

package org.netbeans.modules.extbrowser;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.execution.NbProcessDescriptor;

/** Class that implements browsing.
 *  It starts new process whenever it is asked to display URL.
 */
public class MacBrowserImpl extends ExtBrowserImpl {

    public MacBrowserImpl(ExtWebBrowser extBrowserFactory) {
        super();
        this.extBrowserFactory = extBrowserFactory;
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "MacBrowserImpl created from factory: " + extBrowserFactory);    // NOI18N
        }
    }

    /** Given URL is displayed. 
      *  Configured process is started to satisfy this request. 
      */
    @Override
    protected void loadURLInBrowserInternal(URL url) {
        assert !EventQueue.isDispatchThread();
        if (url == null) {
            return;
        }
        
        NbProcessDescriptor np = extBrowserFactory.getBrowserExecutable();
        try {
            url = URLUtil.createExternalURL(url, false);
            URI uri = url.toURI();
            
            if (np != null) {
                np.exec(new SimpleExtBrowser.BrowserFormat((uri == null)? "": uri.toASCIIString())); // NOI18N
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            logInfo(ex);
            BrowserUtils.notifyMissingBrowser(np.getProcessName());
        }
    }

    private static void logInfo(Exception ex) {
        Logger logger = Logger.getLogger(MacBrowserImpl.class.getName());
        logger.log(Level.INFO, null, ex);
    }
    
    @Override
    protected PrivateBrowserFamilyId detectPrivateBrowserFamilyId( ) {
        PrivateBrowserFamilyId pluginId = super.detectPrivateBrowserFamilyId();
        if (pluginId != PrivateBrowserFamilyId.UNKNOWN){
            return pluginId;
        }
        String defaultApps = getDefaultApps();
        /*if (url != null) {
            String protocol = url.getProtocol();
            if ( protocol != null ){
                pluginId = parseDefaultApps( defaultApps , "LSHandlerURLScheme",    // NOI18N
                        protocol );
            }
            if ( pluginId != null && pluginId != PrivateBrowserFamilyId.UNKNOWN){
                return pluginId;
            }
            String file = url.getFile();
            if ( file!= null ){
                int index = file.lastIndexOf('.');
                if ( index != -1 && file.length() > index +1 ){
                    String ext = file.substring( index +1);
                    pluginId = parseDefaultApps( defaultApps , "LSHandlerContentType",
                            "public."+ext );                                        // NOI18N
                }
            }
        }*/
        if ( pluginId == null || pluginId == PrivateBrowserFamilyId.UNKNOWN){
            pluginId = getPrivateBrowserFamilyIdFromDefaultApps(defaultApps);
            if (pluginId == null) {
                pluginId = PrivateBrowserFamilyId.UNKNOWN;
            }
            return pluginId;
        } else {
            return pluginId;
        }
    }
    
    // package private for tests
    static PrivateBrowserFamilyId getPrivateBrowserFamilyIdFromDefaultApps(String defaultApps) {
        return parseDefaultApps( defaultApps , "LSHandlerContentType", "public.html" );  
    }
    
    
    private static PrivateBrowserFamilyId parseDefaultApps( String defaultApps, String key,
            String value )
    {
        if ( defaultApps == null ){
            return null;
        }
        int index =0;
        while( true ){
            index = defaultApps.indexOf(value, index + 1);
            if ( index == -1 ){
                return null;
            }
            int lBrace = defaultApps.substring(0, index).lastIndexOf('{');
            int rBrace = defaultApps.indexOf('}', index );
            if ( lBrace == -1 || rBrace == -1 ){
                return null;
            }
            int valueIndex = defaultApps.indexOf( key , lBrace );
            if ( valueIndex != -1 && valueIndex <index ){
                // need to check Chrome first, as it inserts itself only to LSHandlerRoleViewer,
                // and may leave another browser in LSHandlerRoleAll
                int chromeIndex = defaultApps.indexOf("chrome", lBrace);        // NOI18N
                if ( chromeIndex <rBrace ){
                    return PrivateBrowserFamilyId.CHROME;
                }
                int firefoxIndex = defaultApps.indexOf("firefox", lBrace);      // NOI18N
                if ( firefoxIndex <rBrace ){
                    return PrivateBrowserFamilyId.FIREFOX;
                }
                int safariIndex = defaultApps.indexOf("safari", lBrace);      // NOI18N
                if ( safariIndex <rBrace ){
                    return PrivateBrowserFamilyId.SAFARI;
                }
                int operaIndex = defaultApps.indexOf("opera", lBrace);      // NOI18N
                if ( operaIndex <rBrace ){
                    return PrivateBrowserFamilyId.OPERA;
                }
            }
            else {
                continue;
            }
        }
    }
    
    private String getDefaultApps(){
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec(
                    "defaults read com.apple.LaunchServices");          // NOI18N
            process.waitFor();

            InputStream inputStream = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine())!= null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                builder.append(line);
            }
            return builder.toString();
        }
        catch (Exception ex) {
            Logger.getLogger(MacBrowserImpl.class.getCanonicalName()).
                log(Level.INFO, "Unable to run process: " +
                		"'defaults read com.apple.LaunchServices'", ex ); // NOI18N
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    Logger.getLogger(MacBrowserImpl.class.getCanonicalName()).
                        log(Level.INFO, 
                                "Unable close process input stream reader " ,       // NOI18N 
                                    ex );      
                }
            }
        }
        return null;
    }
}
