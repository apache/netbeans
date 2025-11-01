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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.TldChangeListener;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

// PENDING - need to call reinitOptions when something changes (taglib, jar, web.xml)
// PENDING - separate to two classes, have a per-application instance of one of them

/**
 * @author Petr Jiricka
 */
public class JspParserImpl implements JspParserAPI {

    // @GuardedBy(this)
    final Map<WebModule, WebAppParseProxy> parseSupports = new WeakHashMap<>();
    private final TldChangeSupport tldChangeSupport;

    private static final Logger LOGGER = Logger.getLogger(JspParserImpl.class.getName());
    private static Method webAppParserImplFactoryMethod;
    private static Method webAppParserJakartaImplFactoryMethod;

    private static final JspParserAPI.JspOpenInfo DEFAULT_OPENINFO =
            new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"); // NOI18N

    /** Constructs a new Parser API implementation.
     */
    public JspParserImpl() {
        // PENDING - we are preventing the garbage collection of
        // Project-s and FileObject-s (wmRoots)
        tldChangeSupport = new TldChangeSupport(this);
    }

    private static URL[] urls;
    private static URL[] urlsJakarta;

    private static final String[] JAR_FILE_NAMES = new String[]{
        "ant/lib/ant.jar",  //NOI18N
        "modules/ext/glassfish-jspparser-5.1.0.jar", //NOI18N
        "modules/ext/jsp-parser-ext.jar", //NOI18N
        "modules/ext/jstl-api.jar", //NOI18N
        "modules/ext/jstl-impl.jar", //NOI18N
        "modules/ext/servlet4.0-jsp2.3-api.jar", //NOI18N
        "ant/lib/ant-launcher.jar" //Glassfish V2 //NOI18N
    };

    private static final String[] JAR_FILE_NAMES_JAKARTA = new String[]{
        "ant/lib/ant.jar",  //NOI18N
        "modules/ext/glassfish-jspparser-7.0.25.jar", //NOI18N
        "modules/ext/jsp-parser-ext-jakarta.jar", //NOI18N
        "modules/ext/jstl-jakarta-api.jar", //NOI18N
        "modules/ext/jstl-jakarta-impl.jar", //NOI18N
        "modules/ext/servlet-jsp-api-6.1_3.0.jar", //NOI18N
        "ant/lib/ant-launcher.jar" //Glassfish V2 //NOI18N
    };

    private static void initURLs() throws MalformedURLException, IOException {
        if (urls == null || urlsJakarta == null) {
            urls = buildUrlsFromRelativePaths(JAR_FILE_NAMES);
            urlsJakarta = buildUrlsFromRelativePaths(JAR_FILE_NAMES_JAKARTA);
        }
    }

    private static URL[] buildUrlsFromRelativePaths(String[] jarFileNames) throws IOException, MalformedURLException {
        File[] files = new File[jarFileNames.length];
        List<String> missing = new ArrayList<>();
        for(int i = 0; i < jarFileNames.length; i++) {
            files[i] = InstalledFileLocator.getDefault().locate(jarFileNames[i], null, false);
            if(files[i] == null) {
                missing.add(jarFileNames[i]);
            }
        }
        if(!missing.isEmpty()) {
            //something wasn't found, report error and cancel the initialization
            StringBuilder msg = new StringBuilder();
            msg.append("Cannot initialize JSP parser, following JAR files couldn't be localted: "); //NOI18N
            for(String fname : missing) {
                msg.append(fname);
                msg.append(','); //NOI18N
            }
            msg.setCharAt(msg.length() - 1, '.'); //replace last comma

            throw new IOException(msg.toString());
        }
        URL[] urls2 = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            urls2[i] = Utilities.toURI(files[i]).toURL();
        }
        return urls2;
    }

    /**
     * This method is designed to be called only from unit tests to initialize
     * parser JARs.
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public static void setParserJARs(URL[] urls, URL[] urlsJakarta) {
        JspParserImpl.urls = urls;
        JspParserImpl.urlsJakarta = urlsJakarta;
    }

    private static void initReflection(boolean jakarta) throws IOException {
        if (((! jakarta) && webAppParserImplFactoryMethod == null) || (jakarta && webAppParserJakartaImplFactoryMethod == null)) {
            try {
                initURLs();
                ExtClassLoader urlCL = new ExtClassLoader(jakarta ? urlsJakarta : urls, JspParserImpl.class.getClassLoader());
                Class<?> cl = urlCL.loadClass("org.netbeans.modules.web.jspparser_ext.WebAppParseSupport"); // NOI18N
                if(jakarta) {
                    webAppParserJakartaImplFactoryMethod = cl.getDeclaredMethod("create", JspParserImpl.class, WebModule.class); // NOI18N
                } else {
                    webAppParserImplFactoryMethod = cl.getDeclaredMethod("create", JspParserImpl.class, WebModule.class); // NOI18N
                }
            } catch (NoSuchMethodException | MalformedURLException | ClassNotFoundException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    @Override
    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, WebModule wm, boolean useEditor) {
        //try to fast create openinfo

        //detects encoding even if there is not webmodule (null) or deployment descriptor doesn't exist
        FastOpenInfoParser fastOIP = FastOpenInfoParser.get(wm);
        if (fastOIP != null) {
            JspParserAPI.JspOpenInfo jspOI = fastOIP.getJspOpenInfo(jspFile, useEditor);
            if (jspOI != null) {
                return jspOI;
            }
        }

        //no encoding found in the file or the deployment descriptor contains encoding declarations
        if (wm != null) {
            FileObject wmRoot = wm.getDocumentBase();
            if (wmRoot != null) {
                WebAppParseProxy pp = getParseProxy(wm);
                if (pp != null) {
                    return pp.getJspOpenInfo(jspFile, useEditor);
                }
            }
        }
        return DEFAULT_OPENINFO;
    }

    @Override
    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, WebModule wm, int errorReportingMode) {
        if (wm == null) {
            return getNoWebModuleResult(jspFile, null);
        }
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            return getNoWebModuleResult(jspFile, wm);
        }
        WebAppParseProxy pp = getParseProxy(wm);
        if (pp == null) {
            return getNoWebModuleResult(jspFile, wm);
        }
        return pp.analyzePage(jspFile, errorReportingMode);
    }

    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library.
     * @param wmRoot the web module for which to return the map
     * @return Map which maps global tag library URI to the location
     * (resource path) of its tld. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    @Override
    public Map<String, String[]> getTaglibMap(WebModule wm) throws IOException {
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            throw new IOException();
        }
        WebAppParseProxy pp = getParseProxy(wm);
        return pp.getTaglibMap(true);
    }

    private synchronized WebAppParseProxy getParseProxy(WebModule wm) {
        WebAppParseProxy pp = parseSupports.get(wm);
        if (pp == null || !pp.isValid(wm)) {
            pp = createParseProxy(wm);
            parseSupports.put(wm, pp);
        }
        return pp;
    }

    private WebAppParseProxy createParseProxy(WebModule wm) {
        // PENDING - do caching for individual JSPs
        try {
            if(wm.getJ2eeProfile().isAtLeast(Profile.JAKARTA_EE_9_WEB)) {
                initReflection(true); // Jakarta Variant
                return (WebAppParseProxy) webAppParserJakartaImplFactoryMethod.invoke(null, this, wm);
            } else {
                initReflection(false); // Javax Variant
                return (WebAppParseProxy) webAppParserImplFactoryMethod.invoke(null, this, wm);
            }
        } catch (IOException ise) {
            LOGGER.log(Level.WARNING, null, ise);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return null;
    }

    @Override
    public URLClassLoader getModuleClassLoader(WebModule wm) {
        WebAppParseProxy pp = getParseProxy(wm);
        return pp.getWAClassLoader();
    }

    private JspParserAPI.ParseResult getNoWebModuleResult(FileObject jspFile, @SuppressWarnings("unused") WebModule wm) {
        JspParserAPI.ErrorDescriptor error = new JspParserAPI.ErrorDescriptor(null, jspFile, -1, -1,
                NbBundle.getMessage(JspParserImpl.class, "MSG_webModuleNotFound", jspFile.getNameExt()), ""); // NOI18N
        return new JspParserAPI.ParseResult(new JspParserAPI.ErrorDescriptor[] {error});
    }

    @Override
    public void addTldChangeListener(TldChangeListener listener) {
        tldChangeSupport.addTldChangeListener(listener);
    }

    @Override
    public void removeTldChangeListener(TldChangeListener listener) {
        tldChangeSupport.removeTldChangeListener(listener);
    }

    public void fireChange(WebModule webModule) {
        tldChangeSupport.fireChange(webModule);
    }

    private static class ExtClassLoader extends URLClassLoader {

        private static final AllPermission ALL_PERM = new AllPermission();

        public ExtClassLoader(URL[] classLoadingURLs, ClassLoader parent) {
            super(classLoadingURLs, parent);
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(ALL_PERM);
            return perms;
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // this is because debugger adds ant on cp but this classloader needs ant as well
            //  - so let this class loader find the class
            if (name.startsWith("org.apache.tools.ant.")) { // NOI18N
                Class<?> clazz = findClass(name);
                return clazz;
            } else if (name.startsWith("org.apache.jasper.")) { // NOI18N
                Class<?> clazz = findClass(name);
                return clazz;
            }
            return super.loadClass(name, resolve);
        }
    }
}
