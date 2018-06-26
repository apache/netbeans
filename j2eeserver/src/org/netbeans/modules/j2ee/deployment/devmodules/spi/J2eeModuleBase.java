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
