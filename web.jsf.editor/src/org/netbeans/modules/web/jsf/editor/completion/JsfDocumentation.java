/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.completion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author marekfukala
 */
public class JsfDocumentation implements HelpResolver {

    private static final Logger LOGGER = Logger.getLogger(JsfDocumentation.class.getName());
    private static final JsfDocumentation SINGLETON = new JsfDocumentation();
    private static final String DOC_ZIP_FILE_NAME = "docs/jsf-api-docs.zip"; //NOI18N
    private static URL DOC_ZIP_URL;

    private static final String JAVADOC_FOLDER_NAME = "javadocs/"; //NOI18N

    private static Map<String, String> HELP_FILES_CACHE = new WeakHashMap<>();

    public static JsfDocumentation getDefault() {
        return SINGLETON;
    }

    static URL getZipURL() {
        if (DOC_ZIP_URL == null) {
            File file = InstalledFileLocator.getDefault().locate(DOC_ZIP_FILE_NAME, null, false);
            if (file != null) {
                try {
                    URL url = Utilities.toURI(file).toURL();
                    DOC_ZIP_URL = FileUtil.getArchiveRoot(url);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(JsfDocumentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getAnonymousLogger().warning(String.format("Cannot locate the %s documentation file.", DOC_ZIP_FILE_NAME)); //NOI18N
            }
        }
        return DOC_ZIP_URL;
    }

    @Override
    public URL resolveLink(URL baseURL, String relativeLink) {
        LOGGER.log(Level.FINE, "relativeLink = ''{0}''", relativeLink); //NOI18N
        LOGGER.log(Level.FINE, "baseURL = ''{0}''", baseURL); //NOI18N

        try {
            //test if the relative link isn't an absolute link (http://site.org/file)
            URI u = new URI(relativeLink);
            if (u.isAbsolute()) {
                LOGGER.log(Level.FINE, "resolved to = ''{0}''", u.toURL()); //NOI18N
                return u.toURL();
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }


        //make the links from taglib descriptors linkable to the jsf-api-docs content
        int javadocIndex = relativeLink.indexOf(JAVADOC_FOLDER_NAME);
        if(javadocIndex != -1) {
            relativeLink = relativeLink.substring(javadocIndex + JAVADOC_FOLDER_NAME.length());
        }
        String link;

        if (relativeLink.startsWith("#")) {
            assert baseURL != null : "Base URL must be provided for local relative links (anchors)."; //NOI18N
            String base = baseURL.toExternalForm();
            //link within the same file
            int hashIdx = base.indexOf('#');
            if (hashIdx != -1) {
                base = base.substring(0, hashIdx);
            }
            link = base + relativeLink;
        } else {
            //link contains a filename
            if(baseURL != null) {
                URL url = getRelativeURL(baseURL, relativeLink);
                LOGGER.log(Level.FINE, "resolved to = ''{0}''", url); //NOI18N
                return url;
            } else {
                link = getZipURL() + relativeLink;
            }
        }

        try {
            URL url = new URI(link).toURL();
            LOGGER.log(Level.FINE, "resolved to = ''{0}''", url); //NOI18N
            return url;
        } catch (URISyntaxException | MalformedURLException ex) {
            Logger.getLogger(JsfDocumentation.class.getName()).log(Level.INFO, null, ex);
        }
        LOGGER.fine("cannot be resolved!"); //NOI18N
        return null;
    }

    private URL getRelativeURL(URL baseurl, String link){
        if(link.startsWith("./")) {
            link = link.substring(2);
        }
        String url = baseurl.toString();
        int index;
        if (link.trim().charAt(0) == '#'){
            index = url.indexOf('#');
            if (index > -1)
                url = url.substring(0,url.indexOf('#'));
            url = url + link;
        } else {
            index = 0;
            url = url.substring(0, url.lastIndexOf('/'));
            while ((index = link.indexOf("../", index)) > -1){      //NOI18N
                url = url.substring(0, url.lastIndexOf('/'));
                link = link.substring(index+3);
            }
            url = url + "/" + link; // NOI18N
        }
        URL newURL;
        try{
            newURL = new URL(url);
        } catch (java.net.MalformedURLException e){
            Logger.getLogger(JsfDocumentation.class.getName()).log(Level.INFO, null, e);
            return null;
        }
        return newURL;
    }

    @Override
    public String getHelpContent(URL url) {
        return getContentAsString(url, null);
    }

    static String getContentAsString(URL url, Charset charset) {
        String filePath = url.getPath();
        String cachedContent = HELP_FILES_CACHE.get(filePath);
        if(cachedContent != null) {
            return cachedContent;
        }

        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        try {
            URLConnection con = url.openConnection();
            con.connect();
            Reader r = new InputStreamReader(new BufferedInputStream(con.getInputStream()), charset);
            char[] buf = new char[2048];
            int read;
            StringBuilder content = new StringBuilder();
            while ((read = r.read(buf)) != -1) {
                content.append(buf, 0, read);
            }
            r.close();
            String strContent = content.toString();
            HELP_FILES_CACHE.put(filePath, strContent);
            return strContent;
        } catch (FileNotFoundException fnfe) {
            LOGGER.log(Level.INFO, "Document at this link is not available: {0}", filePath);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        return null;
    }

    
}
