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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/** Utility class for various useful URL-related tasks.
 * <p>There is similar class in extbrowser/webclient module doing almost the same work.
 * @author Petr Jiricka
 */
public class URLUtil {

    /** results with URLMapper instances*/
    private static Lookup.Result result;            
    
    static {
        result = Lookup.getDefault().lookup(new Lookup.Template (URLMapper.class));
    }            
    
    /** Creates a URL that is suitable for using in a different process on the
     * same node, similarly to URLMapper.EXTERNAL. May just return the original
     * URL if that's good enough.
     * @param url URL that needs to be displayed in browser
     * @param allowJar is <CODE>jar:</CODE> acceptable protocol?
     * @return browsable URL or null
     */
    public static URL createExternalURL(URL url, boolean allowJar) {
        if (url == null)
            return null;
        
        URL compliantURL = getFullyRFC2396CompliantURL(url);

        // return if the protocol is fine
        if (isAcceptableProtocol(compliantURL, allowJar))
            return compliantURL;
        
        // remove the anchor
        String anchor = compliantURL.getRef();
        String urlString = compliantURL.toString ();
        int ind = urlString.indexOf('#');
        if (ind >= 0) {
            urlString = urlString.substring(0, ind);
        }
        
        // map to an external URL using the anchor-less URL
        try {
            FileObject fo = URLMapper.findFileObject(new URL(urlString));
            if (fo != null) {
                URL newUrl = getURLOfAppropriateType(fo, allowJar);
                if (newUrl != null) {
                    // re-add the anchor if exists
                    urlString = newUrl.toString();
                    if (ind >=0) {
                        urlString = urlString + "#" + anchor; // NOI18N
                    }
                    return new URL(urlString);
                }
            }
        }
        catch (MalformedURLException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
        return compliantURL;
    }

    private static URL getFullyRFC2396CompliantURL(URL url){
        String urlStr = url.toString();
        int ind = urlStr.indexOf('#');

        if (ind > -1) {
            String urlWithoutRef = urlStr.substring(0, ind);
            try {
                String asciiURL = url.toURI().toASCIIString();
                // TODO: why not to use just escapedURL = new URL(asciiURL) ?
                ind = asciiURL.indexOf('#');
                String anchorEscaped = asciiURL.substring(ind);
                URL escapedURL = new URL(urlWithoutRef + anchorEscaped);
                return escapedURL;
            } catch (URISyntaxException | MalformedURLException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return url;
    }

    /** Returns a URL for the given file object that can be correctly interpreted
     * by usual web browsers (including Netscape 4.71, IE and Mozilla).
     * First attempts to get an EXTERNAL URL, if that is a suitable URL, it is used;
     * otherwise a NETWORK URL is used.
     */
    private static URL getURLOfAppropriateType(FileObject fo, boolean allowJar) {
        // PENDING - there is still the problem that the HTTP server will be started 
        // (because the HttpServerURLMapper.getURL(...) method starts it), 
        // even when it is not needed
        URL retVal;
        URL suitable = null;
        
        Iterator instances = result.allInstances ().iterator();                
        while (instances.hasNext()) {
            URLMapper mapper = (URLMapper) instances.next();
            retVal = mapper.getURL (fo, URLMapper.EXTERNAL);
            if ((retVal != null) && isAcceptableProtocol(retVal, allowJar)) {
                // return if this is a 'file' or 'jar' URL
                String p = retVal.getProtocol().toLowerCase();
                if ("file".equals(p) || "jar".equals(p)) { // NOI18N
                    return retVal;
                }
                suitable = retVal;
            }
        }
        
        // if we found a suitable URL, return it
        if (suitable != null) {
            return suitable;
        }
        
        URL url = URLMapper.findURL(fo, URLMapper.NETWORK);
        
        if (url == null){
            Logger.getLogger("global").log(Level.SEVERE, "URLMapper.findURL() failed for " + fo); //NOI18N
            
            return null;
        }
        
        return makeURLLocal(url);
    }
    
    private static URL makeURLLocal(URL input) {
        String host = input.getHost();
        try {
            if (host.equals(InetAddress.getLocalHost().getHostName())) {
                host = "127.0.0.1"; // NOI18N
                return new URL(input.getProtocol(), host, input.getPort(), input.getFile());
            }
            else return input;
        } catch (UnknownHostException e) {
            return input;
        } catch (MalformedURLException e) {
            return input;
        }
    }
        
    /** Returns true if the protocol is acceptable for usual web browsers.
     * Specifically, returns true for file, http(s) and ftp protocols.
     */
    private static boolean isAcceptableProtocol(URL url, boolean allowJar) {
        String protocol = url.getProtocol().toLowerCase();
        if ("http".equals(protocol)          // NOI18N
        ||  "https".equals(protocol)         // NOI18N
        ||  "ftp".equals(protocol)           // NOI18N
        ||  "file".equals(protocol))         // NOI18N
            return true;
        if (allowJar && "jar".equals(protocol)) { // NOI18N
            String urlString = url.toString();
            if (!urlString.toLowerCase().startsWith("jar:nbinst:")) // NOI18N
                return true;
        }
        
        return false;
    }
    
    /** Determines whether a given browser is capable of displaying URLs with the jar: protocol.
     *  Currently, Mozilla and Firefox can do this.
     *  @param browser browser id - one of the constants defined in ExtWebBrowser
     *  @return true if the browser handles jar URLs
     */
    public static boolean browserHandlesJarURLs(String browser) {
        return (ExtWebBrowser.MOZILLA.equals(browser) ||
                ExtWebBrowser.FIREFOX.equals(browser));
    }

}
