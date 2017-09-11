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
package org.netbeans.modules.html.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class HtmlDocumentation implements HelpResolver {

    static final String SECTIONS_PATTERN_CODE = "<h\\d\\s*?id=['\\\"]?([\\w\\d-_,:]*)['\\\"]?[^\\>]*>";//NOI18N
//    static final String SECTIONS_PATTERN_CODE ="<[\\w\\d]*.*?id=\\\"([\\w\\d-_]*)\\\"[^\\>]*>";//NOI18N
    static final Pattern SECTIONS_PATTERN = Pattern.compile(SECTIONS_PATTERN_CODE);
    private static final String DOC_ZIP_FILE_NAME = "docs/html5doc.zip"; //NOI18N
    private static URL DOC_ZIP_URL;
    private static final String HELP_PREFIX = "<html><head><title>help</title></head><body>"; //NOI18N
    private static final HtmlDocumentation SINGLETON = new HtmlDocumentation();
    //performance unit testing
    static long url_read_time, pattern_search_time;

    private static Map<String, String> HELP_FILES_CACHE = new WeakHashMap<String, String>();
    private static Map<URL, OffsetRange> HELP_LINKS_CACHE = new WeakHashMap<URL, OffsetRange>();

    public static void setupDocumentationForUnitTests() {
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final"));//NOI18N
    }

    public static HtmlDocumentation getDefault() {
        return SINGLETON;
    }

    static URL getZipURL() {
        if (DOC_ZIP_URL == null) {
            File file = InstalledFileLocator.getDefault().locate(DOC_ZIP_FILE_NAME, null, false);
            if (file != null) {
                try {
                    URL url = file.toURI().toURL();
                    DOC_ZIP_URL = FileUtil.getArchiveRoot(url);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(HtmlDocumentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getAnonymousLogger().warning(String.format("Cannot locate the %s documentation file.", DOC_ZIP_FILE_NAME)); //NOI18N
            }
        }
        return DOC_ZIP_URL;
    }

    public URL resolveLink(URL baseURL, String relativeLink) {
        String link = null;
        String base = baseURL.toExternalForm();

        if (relativeLink.startsWith("#")) {
            //link within the same file
            int hashIdx = base.indexOf('#');
            if (hashIdx != -1) {
                base = base.substring(0, hashIdx);
            }
            link = base + relativeLink;
        } else {
            //link contains a filename
            link = getZipURL() + relativeLink;
        }

        try {
            return new URI(link).toURL();
        } catch (URISyntaxException ex) {
            Logger.getLogger(HtmlDocumentation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HtmlDocumentation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public URL resolveLink(String relativeLink) {
        if (relativeLink == null) {
            return null;
        }
        URL zipURL = getZipURL();
        if(zipURL == null) {
            return null;
        }
        
        try {
            return new URI(zipURL.toExternalForm() + relativeLink).toURL();
        } catch (URISyntaxException ex) {
            Logger.getLogger(HtmlDocumentation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HtmlDocumentation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getHelpContent(URL url) {
        return getSectionContent(url, null);
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
        } catch (IOException ex) {
            Logger.getLogger(HtmlDocumentation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String getSectionContent(URL url, Charset charset) {
        long a = System.currentTimeMillis();
        String content = getContentAsString(url, charset);
        long b = System.currentTimeMillis();
        String surl = url.toExternalForm();
        int hashIndex = surl.indexOf('#');
        if (hashIndex == -1) {
            //no anchor, return whole content
            return content;
        }

        //tro to use cache
        OffsetRange range = HELP_LINKS_CACHE.get(url);
        if(range != null) {
            return buildHelpText(content, range);
        }

        //anchor
        String sectionName = surl.substring(hashIndex + 1);
        Matcher matcher = SECTIONS_PATTERN.matcher(content);

        int from = -1;
        int to = -1;
        List<Integer> groupIndexes = new LinkedList<Integer>();
        while (matcher.find()) {
            groupIndexes.add(matcher.start());

            if (matcher.group(1).equals(sectionName)) {
                from = matcher.start();
            } else if (from != -1) {
                //start of another section
                to = matcher.start();
                break;
            }
        }
        if (to == -1) {
            to = content.length();
        }
        long c = System.currentTimeMillis();
        url_read_time = (b - a);
        pattern_search_time = (c - b);

        if (from != -1) {
            return buildAndCacheHelpText(content, new OffsetRange(from, to), url);
        } else {
            //no heading found for the link, lets look into the heading groups and possibly
            //find a link in the <dfn id="..."/> form
            int lastgi = -1;
            for (int gi : groupIndexes) {
                if (lastgi != -1) {
                    //heading section <lastgi:gi>
                    //lets try to find the link inside
                    CharSequence sub = content.subSequence(lastgi, gi);
                    int index = CharSequences.indexOf(
                            sub,
                            String.format("<dfn id=%s", sectionName)); //NOI18N
                    if (index != -1) {
                        //use this section
                        return buildAndCacheHelpText(content, new OffsetRange(lastgi, gi), url);
                    }
                }
                lastgi = gi;
            }
        }

        return null;
    }

    private String buildAndCacheHelpText(String helpFileContent, OffsetRange strippedArea, URL url) {
        HELP_LINKS_CACHE.put(url, strippedArea);
        return buildHelpText(helpFileContent, strippedArea);
    }

    private String buildHelpText(String helpFileContent, OffsetRange strippedArea) {
        return new StringBuilder().append(HELP_PREFIX).append(helpFileContent.subSequence(strippedArea.from, strippedArea.to)).toString();
    }

    private static class OffsetRange {
        public int from, to;
        public OffsetRange(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
    
}
