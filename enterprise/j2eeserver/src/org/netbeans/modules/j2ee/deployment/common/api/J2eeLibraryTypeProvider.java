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

package org.netbeans.modules.j2ee.deployment.common.api;

import java.beans.Customizer;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The J2eeLibraryTypeProvider defines a j2ee library type and is responsible for
 * creating new libraries of this type.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public final class J2eeLibraryTypeProvider implements LibraryTypeProvider {
    
    /** J2ee library type */
    public static final String LIBRARY_TYPE = "j2ee";                   //NOI18N
    
    /** Classpath volume type */
    public static final String VOLUME_TYPE_CLASSPATH = "classpath";     //NOI18N
    /** Source volume type */
    public static final String VOLUME_TYPE_SRC = "src";                 //NOI18N
    /** Javadoc volume type */
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";         //NOI18N
    
    static final String[] VOLUME_TYPES = new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC
    };

    
    /** Creates a new instance of J2eeLibraryTypeProvider */
    public J2eeLibraryTypeProvider() {
    }
    
    /**
     * Returns the UI name of the LibraryType.
     * This name is used in the UI while the libraryType is used as a system
     * identifier.
     * @return String the display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(J2eeLibraryTypeProvider.class,"TXT_J2eeLibraryType"); //NOI18N
    }

    /**
     * Get a j2ee library type identifier.
     *
     * @return the j2ee library type identifier.
     */
    public String getLibraryType() {
        return LIBRARY_TYPE;
    }

    /**
     * Return supported volume types: <code>classpath</code>, <code>javadoc</code>, 
     * <code>src</code>.
     *
     * @return support volume types.
     */
    public String[] getSupportedVolumeTypes() {
        return VOLUME_TYPES;
    }

    /**
     * Creates a new implementation of j2ee library type.
     *
     * @return the created library model, never null
     */
    public LibraryImplementation createLibrary() {
        return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE, VOLUME_TYPES);
    }

    /**
     * Does nothing now.
     */
    public void libraryDeleted(LibraryImplementation libraryImpl) {
    }

    /**
     * Does nothing now.
     */
    public void libraryCreated(LibraryImplementation libraryImpl) {
    }

    /**
     * Currently returns <code>null</code>.
     */
    public Customizer getCustomizer(String volumeType) {
        return null;
    }
    
    /**
     * Returns empty lookup.
     * @return empty lookup.
     */
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
}
