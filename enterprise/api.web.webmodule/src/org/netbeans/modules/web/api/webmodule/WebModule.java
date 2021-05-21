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

package org.netbeans.modules.web.api.webmodule;

import java.util.Iterator;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * This class encapsulates a web module.
 * 
 * <p>A client may obtain a <code>WebModule</code> instance using
 * method {@link #getWebModule}, for any
 * {@link org.openide.filesystems.FileObject} in the web module directory structure.</p>
 * <div class="nonnormative">
 * <p>Use the classpath API to obtain the classpath for the document base (this classpath
 * is used for code completion of JSPs). An example:</p>
 * <pre>
 *     WebModule wm = ...;
 *     FileObject docRoot = wm.getDocumentBase ();
 *     ClassPath cp = ClassPath.getClassPath(docRoot, ClassPath.EXECUTE);
 * </pre>
 * <p>Note that no particular directory structure for web module is guaranteed 
 * by this API.</p>
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class WebModule {

    @Deprecated
    public static final String J2EE_13_LEVEL = "1.3"; //NOI18N

    @Deprecated
    public static final String J2EE_14_LEVEL = "1.4"; //NOI18N

    @Deprecated
    public static final String JAVA_EE_5_LEVEL = "1.5"; //NOI18N

    @SuppressWarnings("deprecation")
    private final WebModuleImplementation impl;

    private final WebModuleImplementation2 impl2;

    private static final Lookup.Result implementations =
            Lookup.getDefault().lookupResult(WebModuleProvider.class);
    
    static  {
        WebModuleAccessor.setDefault(new WebModuleAccessor() {

            @Override
            public WebModule createWebModule(WebModuleImplementation spiWebmodule) {
                return new WebModule(spiWebmodule, null);
            }

            @Override
            public WebModule createWebModule(WebModuleImplementation2 spiWebmodule) {
                return new WebModule(null, spiWebmodule);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private WebModule (WebModuleImplementation impl, WebModuleImplementation2 impl2) {
        assert (impl != null && impl2 == null) || (impl == null && impl2 != null);
        this.impl = impl;
        this.impl2 = impl2;
    }
    
    /**
     * Finds the web module that a given file belongs to. The given file should
     * be one known to be owned by a web module (e.g., it can be a file in a
     * Java source group, such as a servlet, or it can be a file in the document
     * base, such as a JSP page).
     *
     * @param  file the file to find the web module for; never null.
     * @return the web module this file belongs to or null if the file does not belong
     *         to any web module.
     * @throws NullPointerException if the <code>file</code> parameter is null.
     */
    public static WebModule getWebModule (FileObject file) {
        Parameters.notNull("file", file); // NOI18N
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebModuleProvider impl = (WebModuleProvider)it.next();
            WebModule wm = impl.findWebModule (file);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /**
     * Returns the folder that contains sources of the static documents for
     * the web module (html, JSPs, etc.).
     *
     * @return the static documents folder; can be null.
     */
    public FileObject getDocumentBase () {
        if (impl2 != null) {
            return impl2.getDocumentBase ();
        }
        return impl.getDocumentBase ();
    }
    
    /**
     * Returns the WEB-INF folder for the web module.
     * It may return null for web module that does not have any WEB-INF folder.
     * <div class="nonnormative">
     * <p>The WEB-INF folder would typically be a child of the folder returned
     * by {@link #getDocumentBase} but does not need to be.</p>
     * </div>
     *
     * @return the WEB-INF folder; can be null.
     */
    public FileObject getWebInf () {
        if (impl2 != null) {
            return impl2.getWebInf();
        }
        return impl.getWebInf ();
    }

    /**
     * Returns the deployment descriptor (<code>web.xml</code> file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned
     * by {@link #getWebInf} but does not need to be.
     * </div>
     *
     * @return the <code>web.xml</code> file; can be null.
     */
    public FileObject getDeploymentDescriptor () {
        if (impl2 != null) {
            return impl2.getDeploymentDescriptor();
        }
        return impl.getDeploymentDescriptor ();
    }
    
    /**
     * Returns the context path of the web module.
     *
     * @return the context path; can be null.
     */
    public String getContextPath () {
        if (impl2 != null) {
            return impl2.getContextPath();
        }
        return impl.getContextPath ();
    }
    
    /**
     * Returns the J2EE platform version of this module. The returned value is
     * one of the properties string for constants defined in {@link Profile}.
     *
     * @return J2EE platform version; never null.
     * @deprecated use {@link #getJ2eeProfile()}
     */
    @Deprecated
    public String getJ2eePlatformVersion () {
        if (impl2 != null) {
            return impl2.getJ2eeProfile().toPropertiesString();
        }
        return impl.getJ2eePlatformVersion();
    }

    public Profile getJ2eeProfile() {
         if (impl2 != null) {
            return impl2.getJ2eeProfile();
        }
        return Profile.fromPropertiesString(impl.getJ2eePlatformVersion());
    }
    
    /**
     * Returns the Java source roots associated with the web module.
     * <div class="nonnormative">
     * <p>Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the web module.</p>
     * </div>
     *
     * @return this web module's Java source roots; never null.
     *
     * @deprecated This method is deprecated, because its return values does
     * not contain enough information about the source roots. Source roots
     * are usually implemented by a <code>org.netbeans.api.project.SourceGroup</code>,
     * which is more than just a container for a {@link org.openide.filesystems.FileObject}.
     */
    @Deprecated
    public FileObject[] getJavaSources() {
        if (impl2 != null) {
            return impl2.getJavaSources();
        }
        return impl.getJavaSources();
    }

    /**
     * Returns a model describing the metadata of this web module (servlets,
     * resources, etc.).
     *
     * @return this web module's metadata model; never null.
     */
    public MetadataModel<WebAppMetadata> getMetadataModel() {
        if (impl2 != null) {
            return impl2.getMetadataModel();
        }
        return impl.getMetadataModel();
    }
}
