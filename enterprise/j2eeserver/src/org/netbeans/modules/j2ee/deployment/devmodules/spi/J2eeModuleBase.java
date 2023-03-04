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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sherold
 * @since 1.59
 */
public interface J2eeModuleBase {
    
    /** 
     * Returns a Java EE module specification version, version of a web application 
     * for example.
     * <p>
     * Do not confuse with the Java EE platform specification version.
     *
     * @return module specification version.
     */
    @NonNull
    String getModuleVersion();
    
    /** 
     * Returns the location of the module within the application archive. 
     * 
     * TODO: this does not belong here.. it has to be moved to J2eeApplication
     * If incremental deployment is supported, the value should
     * match the {@link IncrementalDeployment#getModuleUrl(javax.enterprise.deploy.spi.TargetModuleID)}.
     */
    abstract String getUrl ();
    
    /** Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet). 
     */
    FileObject getArchive () throws java.io.IOException;
    
    /** 
     * Returns the contents of the archive, in copyable form.
     * Used for incremental deployment.
     * Currently uses its own {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.RootedEntry}
     * interface. If the J2eeModule instance describes a j2ee application,
     * the result should not contain module archives.
     * 
     * @return entries
     */
    Iterator<J2eeModule.RootedEntry> getArchiveContents() throws java.io.IOException;

    /** This call is used in in-place deployment. 
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     *  @return FileObject for the content directory, return null if the 
     *     module doesn't have a build directory, like an binary archive project
     */
    FileObject getContentDirectory() throws java.io.IOException;
    
    /**
     * Returns a metadata model of a deployment descriptor specified by the 
     * <code>type</code> parameter.
     * 
     * <p>
     * As an example, passing <code>org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata.class</code>
     * as a type parameter will return a metadata model of the web module deployment 
     * descriptor - web.xml.
     * </p>
     * 
     * @param type metadata model type class for which a <code>MetadataModel</code>
     *        instance will be returned.
     * 
     * @return metadata model of a deployment descriptor specified by the <code>type</code>
     *         parameter.
     */
    <T> MetadataModel<T> getMetadataModel(Class<T> type);
    
    /**
     * Returns the module resource directory, or null if the module has no resource
     * directory.
     * 
     * @return the module resource directory, or null if the module has no resource
     *         directory.
     */
    File getResourceDirectory();
    
    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml
     *        for example.
     * 
     * @return absolute path to the deployment configuration file, or null if the
     *         specified file name is not known to this J2eeModule.
     */
    File getDeploymentConfigurationFile(String name);
    
    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
