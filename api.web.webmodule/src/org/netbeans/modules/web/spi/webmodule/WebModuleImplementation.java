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
package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;

/**
 * SPI for {@link org.netbeans.modules.web.api.webmodule.WebModule}.
 *
 * @see WebModuleFactory
 * @deprecated implement {@link WebModuleImplementation2}
 */
public interface WebModuleImplementation {

    /**
     * Returns the folder that contains sources of the static documents for
     * the web module (html, JSPs, etc.).
     *
     * @return the static documents folder; can be null.
     */
    FileObject getDocumentBase ();

    /**
     * Returns the context path of the web module.
     *
     * @return the context path; can be null.
     */
    String getContextPath ();

    /**
     * Returns the J2EE platform version of this module. The returned value is
     * one of the constants {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_13_LEVEL},
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_14_LEVEL} or
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#JAVA_EE_5_LEVEL}.
     *
     * @return J2EE platform version; never null.
     */
    String getJ2eePlatformVersion ();

    /**
     * WEB-INF folder for the web module.
     * <div class="nonnormative">
     * The WEB-INF folder would typically be a child of the folder returned
     * by {@link #getDocumentBase} but does not need to be.
     * </div>
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getWebInf ();

    /**
     * Returns the deployment descriptor (<code>web.xml</code> file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned
     * by {@link #getWebInf} but does not need to be.
     * </div>
     *
     * @return the <code>web.xml</code> file; can be null.
     */
    FileObject getDeploymentDescriptor ();

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
    FileObject[] getJavaSources();

    /**
     * Returns a model describing the metadata of this web module (servlets,
     * resources, etc.).
     *
     * @return this web module's metadata model; never null.
     */
    MetadataModel<WebAppMetadata> getMetadataModel();
}
