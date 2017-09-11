/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup.base;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.openide.filesystems.FileSystem;

/**
 * SPI interface to separate layered filesystem infrastructure from the I/O and environment.
 * The purpose is to separate loading / caching of the layered filesystem contents and possible
 * configuration from the environment from the layering code itself.
 * 
 * @author sdedic
 * @since 1.60
 */
public interface LayerFactory {
    /**
     * Creates an empty FS used as the base for layered filesystem.
     * @return an instance of FileSystem
     * @throws IOException 
     */
    public FileSystem   createEmptyFileSystem() throws IOException;
    
    /**
     * Loads filesystem contents from the cache.
     * @return initialized FileSystem instance
     * @throws IOException 
     */
    public FileSystem   loadCache() throws IOException;
    
    /**
     * Called to store populate the FileSystem and store its contents.
     * The method will load the FileSystem from the supplied URLs.
     * @param cache the filesystem to initialize and store
     * @param urls list of URLs used to populate the filesystem
     * @return the instance to be used from now; possibly the same as 'cache'.
     * @throws IOException 
     */
    public FileSystem   store(FileSystem cache, List<URL> urls) throws IOException;
    
    /**
     * Retrives additional layers which should be included into FS configuration
     * @param urls additional URLs
     * @return 
     */
    public List<URL>    additionalLayers(List<URL> urls);

    /**
     * SPI to create a system-dependent {@link LayerFactory}
     */
    public interface Provider {
        /**
         * Creates the LayerFactory instance. Two modes are supported:
         * <ul>
         * <li>system - settings taken from NetBeans installation, or from a shared location only. 
         * The created FileSystem will be typically shared between multiple contexts in a multi-tenant environment.
         * <li>user - settings are taken from the user storage, but based on shared ones.
         * </ul>
         * 
         * @param system true, if the instance is shared
         * @return LayerFactory to create and populate the FileSystem
         */
        public LayerFactory     create(boolean system);
    }
}
