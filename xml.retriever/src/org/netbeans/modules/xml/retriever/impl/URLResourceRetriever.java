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

/*
 * URLResourceRetriever.java
 *
 * Created on January 9, 2006, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.retriever.*;

/**
 *
 * @author girix
 */
public class URLResourceRetriever implements ResourceRetriever{
    
    /**
     * Hack: this is a URL rewriter for well-known XML namespaces whose resources
     * do not follow the convention - most prominent is the XSLT schema
     */
    private static final Map<String, String>    rewriteURIMap = new HashMap<String, String>();
    
    /**
     * Well known XML schemas from specs
     */
    static {
        rewriteURIMap.put("http://www.w3.org/2001/XMLSchema", "http://www.w3.org/2001/XMLSchema.xsd");
        rewriteURIMap.put("http://www.w3.org/1999/XSL/Transform", "http://www.w3.org/2007/schema-for-xslt20.xsd");
    }

    /**
     * HTTP Location header name
     */
    private static final String HTTP_REDIRECT_LOCATION = "Location";
    
    private static final String URI_SCHEME = "http"; //NOI18N
    /** Creates a new instance of FileResourceRetriever */
    public URLResourceRetriever() {
    }
    
    public boolean accept(String baseAddr, String currentAddr) throws URISyntaxException {
        URI currURI = new URI(currentAddr);
        if( (currURI.isAbsolute()) && (currURI.getScheme().equalsIgnoreCase(URI_SCHEME)))
            return true;
        if(baseAddr != null){
            if(!currURI.isAbsolute()){
                URI baseURI = new URI(baseAddr);
                if(baseURI.getScheme().equalsIgnoreCase(URI_SCHEME))
                    return true;
            }
        }
        return false;
    }
    
    public HashMap<String, InputStream> retrieveDocument(String baseAddress,
            String documentAddress) throws IOException,URISyntaxException{
        
        String effAddr = getEffectiveAddress(baseAddress, documentAddress);
        if(effAddr == null)
            return null;
        URI currURI = new URI(effAddr);
        HashMap<String, InputStream> result = null;
        
        InputStream is = getInputStreamOfURL(currURI.toURL(), ProxySelector.
                getDefault().select(currURI).get(0));
        result = new HashMap<String, InputStream>();
        result.put(effectiveURL.toString(), is);
        return result;
        
    }
    
    /**
     * Sets up newly opened connection. E.g. for HTTPS, certificate / host
     * verification can be disabled.
     * <p/>
     * The default implementation does nothing.
     * 
     * @param c new connection instance
     */
    protected void configureURLConnection(URLConnection ucn) {}
    
    private HttpURLConnection doConfigureURLConnection(URLConnection ucn) {
        assert ucn instanceof HttpURLConnection;
        HttpURLConnection hucn = ((HttpURLConnection)ucn);
        hucn.setFollowRedirects(false);
        configureURLConnection(ucn);
        return hucn;
    }
    
    long streamLength = 0;
    URL effectiveURL = null;
    public InputStream getInputStreamOfURL(URL downloadURL, Proxy proxy) throws IOException{
        
        URLConnection ucn = null;
        
        // loop until no more redirections are 
        for (;;) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            if(proxy != null) {
                ucn = downloadURL.openConnection(proxy);
            } else {
                ucn = downloadURL.openConnection();
            }
            HttpURLConnection hucn = doConfigureURLConnection(ucn);

            if(Thread.currentThread().isInterrupted())
                return null;
        
            ucn.connect();

            int rc = hucn.getResponseCode();
            boolean isRedirect = 
                    rc == HttpURLConnection.HTTP_MOVED_TEMP ||
                    rc == HttpURLConnection.HTTP_MOVED_PERM;
            if (!isRedirect) {
                break;
            }

            String addr = hucn.getHeaderField(HTTP_REDIRECT_LOCATION);
            URL newURL = new URL(addr);
            if (!downloadURL.getProtocol().equalsIgnoreCase(newURL.getProtocol())) {
                throw new ResourceRedirectException(newURL);
            }
            downloadURL = newURL;
        }

        ucn.setReadTimeout(10000);
        InputStream is = ucn.getInputStream();
        streamLength = ucn.getContentLength();
        effectiveURL = ucn.getURL();
        return is;
        
    }
    
    public long getStreamLength() {
        return streamLength;
    }
    
    public String getEffectiveAddress(String baseAddress, String documentAddress) throws IOException, URISyntaxException {
        return resolveURL(baseAddress, documentAddress, true);
    }
    
    public static String resolveURL(String baseAddress, String documentAddress) throws URISyntaxException{
        return resolveURL(baseAddress, documentAddress, false);
    }
    
    /**
     * Hack: in the orignal form the method was called with rewrite = false only. It should resolve
     * URIs relative to base address. But when actually obtaining the resource stream, rewrite = true
     * is used. This way the effective URL will differ from the resolved URL, which makes the rest
     * of code think that the resource URI was redirected and remembers the appropriate original URI
     * in the catalog.
     * 
     * @param baseAddress base address for the resolution
     * @param documentAddress relative or absolute resource URI to resolve
     * @param rewrite if true, rewrites URIs of well-known schemas to the proper locations
     * @return resolved (or rewritten) URL
     * @throws URISyntaxException 
     */
    static String resolveURL(String baseAddress, String documentAddress, boolean rewrite) throws URISyntaxException{
        URI currURI = new URI(documentAddress);
        String result = null;
        if(currURI.isAbsolute()){
            String rewritten = rewriteURIMap.get(documentAddress);
            if (rewrite && rewritten != null) {
                result = rewritten;
            } else {
                result = currURI.toString();
            }
            return result;
        }else{
            //relative URI
            if(baseAddress != null){
                URI baseURI = new URI(baseAddress);
                URI finalURI = baseURI.resolve(currURI);
                result = finalURI.toString();
                return result;
            }else{
                //neither the current URI nor the base URI are absoulte. So, can not resolve this
                //path
                return null;
            }
        }
    }
}
