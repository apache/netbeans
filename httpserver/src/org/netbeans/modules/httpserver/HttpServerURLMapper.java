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

package org.netbeans.modules.httpserver;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/** Implementation of a URLMapper which creates http URLs for fileobjects in the IDE.
 * Directs the requests for URLs to WrapperServlet.
 *
 * @author Petr Jiricka, David Konecny
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public class HttpServerURLMapper extends URLMapper {
    
    /** Creates a new instance of HttpServerURLMapper */
    public HttpServerURLMapper() {
    }
    
    /** Get an array of FileObjects for this url
     * @param url to wanted FileObjects
     * @return a suitable array of FileObjects, or null
     */
    public FileObject[] getFileObjects(URL url) {
        String path = url.getPath();

        // remove the wrapper servlet URI
        String wrapper = httpserverSettings().getWrapperBaseURL ();
        if (path == null || !path.startsWith(wrapper))
            return null;
        path = path.substring(wrapper.length());
        
        // resource name
        if (path.startsWith ("/")) path = path.substring (1); // NOI18N
        if (path.length() == 0) {
            return new FileObject[0];
        }
        // decode path to EXTERNAL/INTERNAL type of URL
        URL u = decodeURL(path);
        if (u == null) {
            return new FileObject[0];
        }
        return URLMapper.findFileObjects(u);
    }
    
    private URL decodeURL(String path) {
        StringTokenizer slashTok = new StringTokenizer(path, "/", true); // NOI18N
        StringBuffer newPath = new StringBuffer();
        while (slashTok.hasMoreTokens()) {
            String tok = slashTok.nextToken();
            if (tok.startsWith("/")) { // NOI18N
                newPath.append(tok);
            } else {
                try {
                    newPath.append(URLDecoder.decode(tok, "UTF-8")); // NOI18N
                } catch (UnsupportedEncodingException e) {
                    assert false : e;
                    return null;
                }
            }
        }
        
        try {
            return new URL(newPath.toString());
        } catch (MalformedURLException ex) {
            Exceptions.attachMessage(ex, "using: " + newPath);
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    /** Get a good URL for this file object which works according to type:
     * -inside this VM
     * - inside this machine
     * - from networked machines 
     * @return a suitable URL, or null
     */            
    public URL getURL(FileObject fileObject, int type) {
        
        // only do external and network URLs
        if (type != URLMapper.NETWORK)
            return null;
        
        // fileObject must not be null
        if (fileObject == null)
            return null;
        
        // It should be OK to call URLMapper here because we call
        // it with different then NETWORK type.
        URL u = URLMapper.findURL(fileObject, URLMapper.EXTERNAL);
        if (u == null) {
            // if EXTERNAL type is not available try the INTERNAL one
            u = URLMapper.findURL(fileObject, URLMapper.INTERNAL);
            if (u == null) {
                return null;
            }
        }
        String path = encodeURL(u);
        HttpServerSettings settings = httpserverSettings();
        settings.setRunning(true);
        try {
            URL newURL = new URL("http",   // NOI18N
                getLocalHost(),
                settings.getPort(),
                settings.getWrapperBaseURL() + path); // NOI18N
            return newURL;
        } catch (MalformedURLException e) {
            Logger.getLogger("global").log(Level.WARNING, null, e);
            return null;
        }
    }
    
    private String encodeURL(URL u) {
        String orig = u.toExternalForm();
        StringTokenizer slashTok = new StringTokenizer(orig, "/", true); // NOI18N
        StringBuffer path = new StringBuffer();
        while (slashTok.hasMoreTokens()) {
            String tok = slashTok.nextToken();
            if (tok.startsWith("/")) { // NOI18N
                path.append(tok);
            } else {
                try {
                    path.append(URLEncoder.encode(tok, "UTF-8")); // NOI18N
                } catch (UnsupportedEncodingException e) {
                    assert false : e;
                    return null;
                }
            }
        }
        return path.toString();
    }
    
    /** Returns string for localhost */
    private static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "127.0.0.1"; // NOI18N
        }
    }

    /** 
     * Obtains settings of this module
     */
    static HttpServerSettings httpserverSettings () {
        return HttpServerSettings.getDefault();
    }
    
}
