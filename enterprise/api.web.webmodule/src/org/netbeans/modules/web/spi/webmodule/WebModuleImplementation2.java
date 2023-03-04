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
package org.netbeans.modules.web.spi.webmodule;

import java.beans.PropertyChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;

/**
 * SPI for {@link org.netbeans.modules.web.api.webmodule.WebModule}.
 *
 * @see WebModuleFactory
 */
public interface WebModuleImplementation2 {

    /**
     * @since org.netbeans.api.web.webmodule/1 1.16
     */
    static final String PROPERTY_DOCUMENT_BASE = "documentBase";
    
    /**
     * @since org.netbeans.api.web.webmodule/1 1.16
     */
    static final String PROPERTY_WEB_INF = "webInf";
    
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

    Profile getJ2eeProfile();

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

    /**
     * Add propertty change listener.
     *
     * @since org.netbeans.api.web.webmodule/1 1.16
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove propertty change listener.
     *
     * @since org.netbeans.api.web.webmodule/1 1.16
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
