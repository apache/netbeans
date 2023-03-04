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
