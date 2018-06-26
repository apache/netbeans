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
