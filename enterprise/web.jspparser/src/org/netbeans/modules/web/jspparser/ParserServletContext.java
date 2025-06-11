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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.dd.api.web.Taglib;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 * Simple <code>ServletContext</code> implementation without
 * HTTP-specific methods.
 *
 * @author Peter Rossbach (pr@webapp.de)
 */

public class ParserServletContext implements ServletContext {
    
    public static final String JSP_TAGLIBRARY_CACHE = "com.sun.jsp.taglibraryCache";
    public static final String JSP_TAGFILE_JAR_URLS_CACHE = "com.sun.jsp.tagFileJarUrlsCache";
    
    private static final Logger LOGGER = Logger.getLogger(ParserServletContext.class.getName());
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Servlet context attributes.
     */
    protected Hashtable<String, Object> myAttributes;
    
    
    /**
     * The base FileObject (document root) for this context.
     */
    protected FileObject wmRoot;
    
    private JspConfigDescriptor jspConfigDesc;
    
    private final WebModuleProvider webModuleProvider;
    
    /** If true, takes the data from the editor; otherwise
     * from the disk.
     */
    protected boolean useEditorVersion;
    
    
    // ----------------------------------------------------------- Constructors
    
    
    /**
     * Create a new instance of this ServletContext implementation.
     *
     * @param wmRoot Resource base FileObject
     * @param wm JspParserAPI.WebModule in which we are parsing the file - this is used to
     *    find the editor for objects which are open in the editor
     */
    public ParserServletContext(FileObject wmRoot, WebModuleProvider WebModuleProvider, boolean useEditor) {
        LOGGER.log(Level.FINE, "ParserServletContext created");
        myAttributes = new Hashtable<String, Object>();
        this.wmRoot = wmRoot;
        this.webModuleProvider = WebModuleProvider;
        this.useEditorVersion = useEditor;
        this.jspConfigDesc = new JspConfigDescriptor() {
            
            @Override
            public Collection<TaglibDescriptor> getTaglibs() {
                // #197633:
                WebModule webModule = webModuleProvider.getWebModule();
                if (webModule != null) {
                    FileObject webxml = webModule.getDeploymentDescriptor();
                    if (webxml != null) {
                        try {
                            WebApp w = DDProvider.getDefault().getDDRoot(webxml);   
                            if (w != null) {
                                JspConfig jc = w.getSingleJspConfig();
                                if (jc != null) {
                                    Collection<TaglibDescriptor> result = new ArrayList<TaglibDescriptor>();
                                    for (Taglib tl : jc.getTaglib()) {
                                        result.add(new TaglibDescriptorImpl(
                                                tl.getTaglibUri(),
                                                tl.getTaglibLocation()
                                        ));
                                    }
                                    return result;
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.FINE, "getTaglibs for " + FileUtil.toFile(webxml), e);
                        } catch (VersionNotSupportedException e) {
                            LOGGER.log(Level.FINE, "getTaglibs for " + FileUtil.toFile(webxml), e);
                        }
                        
                    }
                }
                return Collections.<TaglibDescriptor>emptyList();
            }
            
            @Override
            public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
                WebModule webModule = webModuleProvider.getWebModule();
                if (webModule != null) {
                    FileObject webxml = webModule.getDeploymentDescriptor();
                    if (webxml != null) {
                        try {
                            WebApp w = DDProvider.getDefault().getDDRoot(webxml);   
                            if (w != null) {
                                JspConfig jc = w.getSingleJspConfig();
                                if (jc != null) {
                                    Collection<JspPropertyGroupDescriptor> result = new ArrayList<JspPropertyGroupDescriptor>();
                                    JspPropertyGroup[] jpgs = jc.getJspPropertyGroup();
                                    for (int i = 0; i < jpgs.length; i++) {
                                        JspPropertyGroup jpg = jpgs[i];
                                        result.add(new JspPropertyGroupDescriptorImpl(
                                                Arrays.asList(jpg.getUrlPattern()),
                                                Boolean.toString(jpg.isElIgnored()),
                                                jpg.getPageEncoding(),
                                                Boolean.toString(jpg.isScriptingInvalid()),
                                                Boolean.toString(jpg.isIsXml()),
                                                Arrays.asList(jpg.getIncludePrelude()),
                                                Arrays.asList(jpg.getIncludeCoda())
                                        ));
                                    }
                                    return result;
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.FINE, "getJspPropertyGroups for " + FileUtil.toFile(webxml), e);
                        } catch (VersionNotSupportedException e) {
                            LOGGER.log(Level.FINE, "getJspPropertyGroups for " + FileUtil.toFile(webxml), e);
                        }
                        
                    }
                }
                return Collections.<JspPropertyGroupDescriptor>emptyList();                
            }
            
        };
        
        setAttribute(JSP_TAGLIBRARY_CACHE, new ConcurrentHashMap<String, TagLibraryInfo>());
        setAttribute(JSP_TAGFILE_JAR_URLS_CACHE, new ConcurrentHashMap<String, URL>());
    }

    @Override
    public String getVirtualServerName() {
        return "";
    }
    
    
    private static final class TaglibDescriptorImpl implements TaglibDescriptor {

        private String uri;
        private String loc;

        public TaglibDescriptorImpl(String uri, String loc) {
            this.uri = uri;
            this.loc = loc;
        }
        
        @Override
        public String getTaglibURI() {
            return uri;
        }

        @Override
        public String getTaglibLocation() {
            return loc;
        }
    }
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Return the specified context attribute, if any.
     *
     * @param name Name of the requested attribute
     */
    public Object getAttribute(String name) {
        LOGGER.log(Level.FINE, "getAttribute({0}) = {1}", new Object[]{name, myAttributes.get(name)});
        return myAttributes.get(name);
    }
    
    
    /**
     * Return an enumeration of context attribute names.
     */
    public Enumeration<String> getAttributeNames() {
        
        return myAttributes.keys();
        
    }
    
    
    /**
     * Return the servlet context for the specified path.
     *
     * @param uripath Server-relative path starting with '/'
     */
    public ServletContext getContext(String uripath) {
        
        return null;
        
    }
    
    
    /**
     * Return the specified context initialization parameter.
     *
     * @param name Name of the requested parameter
     */
    public String getInitParameter(String name) {
        
        return null;
        
    }
    
    
    /**
     * Return an enumeration of the names of context initialization
     * parameters.
     */
    public Enumeration getInitParameterNames() {
        
        return new Vector().elements();
        
    }
    
    
    /**
     * Return the Servlet API major version number.
     */
    public int getMajorVersion() {
        
        return 4;
        
    }
    
    
    /**
     * Return the MIME type for the specified filename.
     *
     * @param file Filename whose MIME type is requested
     */
    public String getMimeType(String file) {
        
        return null;
        
    }
    
    
    /**
     * Return the Servlet API minor version number.
     */
    public int getMinorVersion() {
        
        return 0;
        
    }
    
    
    /**
     * Return a request dispatcher for the specified servlet name.
     *
     * @param name Name of the requested servlet
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        
        return null;
        
    }
    
    /** Returns a FileObject representation of the specified context-relative
     * virtual path.
     */
    protected FileObject getResourceAsObject(String path) {
        LOGGER.log(Level.FINE,  "getResourceAsObject({0})", path);
        FileObject fileObject = null;
        if (wmRoot != null) {
            fileObject = wmRoot.getFileObject(path);
        }
        WebModule webModule = webModuleProvider.getWebModule();
        if (fileObject == null && path != null && webModule != null && webModule.getWebInf() != null) {
            int index = path.toLowerCase().indexOf("web-inf");
            if (index > -1) {
                String newPath = path.substring(index + 7);
                fileObject = webModule.getWebInf().getFileObject(newPath);
            }
            else {
                fileObject = webModule.getWebInf().getFileObject(path);
            }
        }
        return fileObject;
    }
    
    
    /**
     * Return the real path for the specified context-relative
     * virtual path.
     *
     * @param path The context-relative virtual path to resolve
     */
    public String getRealPath(String path) {
        LOGGER.log(Level.FINE,  "getRealPath({0})", path);
        if (!path.startsWith("/")) {
            return null;
        }
        FileObject fo = getResourceAsObject(path);
        if (fo != null) {
            File ff = FileUtil.toFile(fo);
            if (ff != null) {
                return ff.getAbsolutePath();
            }
        }
        
        return null;
    }
    
    
    /**
     * Return a request dispatcher for the specified context-relative path.
     *
     * @param path Context-relative path for which to acquire a dispatcher
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        
        return null;
        
    }
    
    
    /**
     * Return a URL object of a resource that is mapped to the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     *
     * @exception MalformedURLException if the resource path is
     *  not properly formed
     */
    public URL getResource(String path) throws MalformedURLException {
        
        LOGGER.log(Level.FINE,  "getResource({0})", path);
        if (!path.startsWith("/"))
            throw new MalformedURLException(NbBundle.getMessage(ParserServletContext.class,
                    "EXC_PathMustStartWithSlash", path));
        
        FileObject fo = getResourceAsObject(path);
        if (fo == null) {
            return null;
        }
        return URLMapper.findURL(fo, URLMapper.EXTERNAL);
        
    }
    
    
    /**
     * Return an InputStream allowing access to the resource at the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     */
    public InputStream getResourceAsStream(String path) {
        LOGGER.log(Level.FINE,  "getResourceAsStream({0})", path);
        // first try from the opened editor - if fails read from file
        FileObject fo = getResourceAsObject(path);
        if ((fo != null) && (useEditorVersion)) {
            // reading from the editor
            InputStream result = getEditorInputStream(fo);
            if (result != null) {
                return result;
            }
        }
        
        // read from the file by default
        try {
            URL url = getResource(path);
            if (url == null) {
                return null;
            } else {
                return url.openStream();
            }
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, null, t);
            return null;
        }
        
    }
    
    // copied from web.core because it's the only place with this method implemented
    // (can be easily improved using customization interface if needed)
    /**
     * Returns InputStream for the file open in editor or null
     * if the file is not open.
     */
    private InputStream getEditorInputStream(FileObject fo) {
        InputStream result = null;
        EditorCookie ec = null;
        try {
            ec = DataObject.find(fo).getCookie(EditorCookie.class);
        } catch (DataObjectNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        if ((ec instanceof CloneableEditorSupport)) {
            try {
                result = ((CloneableEditorSupport) ec).getInputStream();
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
        return result;
    }
    
    /**
     * Return the set of resource paths for the "directory" at the
     * specified context path.
     *
     * @param path Context-relative base path
     */
    public Set<String> getResourcePaths(String path) {
        
        LOGGER.log(Level.FINE,  "getResourcePaths({0})", path);
        Set<String> thePaths = new HashSet<String>();
        if (!path.endsWith("/"))
            path += "/";
        String basePath = getRealPath(path);
        if (basePath == null)
            return thePaths;
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory())
            return thePaths;
        String theFiles[] = theBaseDir.list();
        for (int i = 0; i < theFiles.length; i++) {
            File testFile = new File(basePath + File.separator + theFiles[i]);
            if (testFile.isFile())
                thePaths.add(path + theFiles[i]);
            else if (testFile.isDirectory())
                thePaths.add(path + theFiles[i] + "/");
        }
        return thePaths;
        
    }
    
    
    /**
     * Return descriptive information about this server.
     */
    public String getServerInfo() {
        
        return "NB.ParserServletContext/1.0";
        
    }
    
    
    /**
     * Return a null reference for the specified servlet name.
     *
     * @param name Name of the requested servlet
     *
     * @deprecated This method has been deprecated with no replacement
     */
    @Deprecated
    public Servlet getServlet(String name) throws ServletException {
        
        return null;
        
    }
    
    
    /**
     * Return the name of this servlet context.
     */
    public String getServletContextName() {
        
        return getServerInfo();
        
    }
    
    
    /**
     * Return an empty enumeration of servlet names.
     *
     * @deprecated This method has been deprecated with no replacement
     */
    @Deprecated
    public Enumeration getServletNames() {
        
        return new Vector().elements();
        
    }
    
    
    /**
     * Return an empty enumeration of servlets.
     *
     * @deprecated This method has been deprecated with no replacement
     */
    @Deprecated
    public Enumeration getServlets() {
        
        return new Vector().elements();
        
    }
    
    
    /**
     * Log the specified message.
     *
     * @param message The message to be logged
     */
    public void log(String message) {
        LOGGER.log(Level.INFO, message);
    }
    
    
    /**
     * Log the specified message and exception.
     *
     * @param exception The exception to be logged
     * @param message The message to be logged
     *
     * @deprecated Use log(String,Throwable) instead
     */
    @Deprecated
    public void log(Exception exception, String message) {
        
        log(message, exception);
        
    }
    
    
    /**
     * Log the specified message and exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    public void log(String message, Throwable exception) {
        LOGGER.log(Level.INFO, message);
        LOGGER.log(Level.INFO, null, exception);
    }
    
    
    /**
     * Remove the specified context attribute.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        myAttributes.remove(name);
        
    }
    
    
    /**
     * Set or replace the specified context attribute.
     *
     * @param name Name of the context attribute to set
     * @param value Corresponding attribute value
     */
    public void setAttribute(String name, Object value) {
        myAttributes.put(name, value);
        
    }
    
    
    public String getContextPath(){
        return "";
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 4;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public boolean setInitParameter(String string, String string1) {
        return false;
    }

    @Override
    public Dynamic addServlet(String string, String string1) {
        return null;
    }

    @Override
    public Dynamic addServlet(String string, Servlet srvlt) {
        return null;
    }

    @Override
    public Dynamic addServlet(String string, Class<? extends Servlet> type) {
        return null;
    }
    
    @Override
    public Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> type) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String string) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String string, String string1) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String string, Filter filter) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String string, Class<? extends Filter> type) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> type) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String string) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
        
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String string) {
        
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        
    }

    @Override
    public void addListener(Class<? extends EventListener> type) {
        
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> type) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return jspConfigDesc;
    }

    @Override
    public ClassLoader getClassLoader() {
        // !!!!!
        return null;
    }

    @Override
    public void declareRoles(String... strings) {
        
    }
    
    @Override
    public int getSessionTimeout() {
        return 0;
    }
      
    @Override
    public void setSessionTimeout(int sessionTimeout) {
        
    }
    
    @Override
    public String getRequestCharacterEncoding() {
        return "";
    }
    
    @Override
    public void setRequestCharacterEncoding(String encoding) {
        
    }
    
    @Override
    public String getResponseCharacterEncoding() {
        return "";
    }
    
    @Override
    public void setResponseCharacterEncoding(String encoding) {
        
    }
    
    /**
     * This interface delegates lifecycle of {@link WebModule} to the caller.
     * See issue #85817 for more information.
     */
    public interface WebModuleProvider {
        /**
         * Get {@link WebModule} instance.
         * @return {@link WebModule} instance or <code>null</code> if WebModule has already been garbage collected.
         */
        WebModule getWebModule();
    }
    
    private static class JspPropertyGroupDescriptorImpl implements JspPropertyGroupDescriptor {

        private Collection<String> urlPatterns;
        private String isElIgnored;
        private String pageEncoding;
        private String scriptingInvalid;
        private String isXml;
        private Collection<String> includePreludes;
        private Collection<String> includeCodas;

        public JspPropertyGroupDescriptorImpl(Collection<String> urlPatterns, String isElIgnored, 
                String pageEncoding, String scriptingInvalid, String isXml, 
                Collection<String> includePreludes, Collection<String> includeCodas) {
            this.urlPatterns = urlPatterns;
            this.isElIgnored = isElIgnored;
            this.pageEncoding = pageEncoding;
            this.scriptingInvalid = scriptingInvalid;
            this.isXml = isXml;
            this.includePreludes = includePreludes;
            this.includeCodas = includeCodas;
        }
       

        
        @Override
        public Collection<String> getUrlPatterns() {
            return urlPatterns;
        }

        @Override
        public String getElIgnored() {
            return isElIgnored;
        }

        @Override
        public String getPageEncoding() {
            return pageEncoding;
        }

        @Override
        public String getScriptingInvalid() {
            return scriptingInvalid;
        }

        @Override
        public String getIsXml() {
            return isXml;
        }

        @Override
        public Collection<String> getIncludePreludes() {
            return includePreludes;
        }

        @Override
        public Collection<String> getIncludeCodas() {
            return includeCodas;
        }

        @Override
        public String getDeferredSyntaxAllowedAsLiteral() {
            return "IGNORE ME - THIS VALUE IS NOT SUPPOSED TO BE USED"; // NOI18N
        }

        @Override
        public String getTrimDirectiveWhitespaces() {
            return "IGNORE ME - THIS VALUE IS NOT SUPPOSED TO BE USED"; // NOI18N
        }

        @Override
        public String getDefaultContentType() {
            return "IGNORE ME - THIS VALUE IS NOT SUPPOSED TO BE USED"; // NOI18N
        }

        @Override
        public String getBuffer() {
            return "IGNORE ME - THIS VALUE IS NOT SUPPOSED TO BE USED"; // NOI18N
        }

        @Override
        public String getErrorOnUndeclaredNamespace() {
            return "IGNORE ME - THIS VALUE IS NOT SUPPOSED TO BE USED"; // NOI18N
        }
        
    }
}
