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

package org.netbeans.spi.project.libraries;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;

/**
 * Base SPI interface for library. This SPI class is used as a model by the libraries framework.
 * The {@link LibraryTypeProvider} implementor should rather use
 * {@link org.netbeans.spi.project.libraries.support.LibrariesSupport#createLibraryImplementation}
 * factory method to create default LibraryImplementation than to implement this interface.
 */
public interface LibraryImplementation {

    String PROP_NAME = "name";                  //NOI18N
    String PROP_DESCRIPTION = "description";    //NOI18N
    String PROP_CONTENT = "content";            //NOI18N

    /**
     * Returns type of library, the LibraryTypeProvider creates libraries
     * of given unique type.
     * @return String unique identifier, never returns null.
     */
    String getType();

    /**
     * Returns name of the library
     * @return String unique name of the library, never returns null.
     */
    String getName();

    /**
     * Get a description of the library.
     * The description provides more detailed information about the library.
     * @return String the description or null if the description is not available
     */
    String getDescription();


    /**
     * Returns the resource name of the bundle which is used for localizing
     * the name and description. The bundle is located using the system ClassLoader.
     * @return String, the resource name of the bundle. If null in case when the
     * name and description is not localized.
     */
    String getLocalizingBundle();

    /**
     * Returns List of resources contained in the given volume.
     * The returned list is unmodifiable. To change the content of
     * the given volume use setContent method.
     * @param volumeType the type of volume for which the content should be returned.
     * @return list of resource URLs (never null)
     * @throws IllegalArgumentException if the library does not support given type of volume
     */
    List<URL> getContent(String volumeType) throws IllegalArgumentException;

    /**
     * Sets the name of the library, called by LibrariesStorage while reading the library
     * @param name -  the unique name of the library, can't be null.
     */
    void setName(String name);

    /**
     * Sets the description of the library, called by LibrariesStorage while reading the library
     * The description is more detailed information about the library.
     * @param text - the description of the library, may be null.
     */
    void setDescription(String text);

    /**
     * Sets the localizing bundle. The bundle is used for localizing the name and description.
     * The bundle is located using the system ClassLoader.
     * Called by LibrariesStorage while reading the library.
     * @param resourceName of the bundle without extension, may be null.
     */
    void setLocalizingBundle(String resourceName);

    /**
     * Adds PropertyChangeListener
     * @param l - the PropertyChangeListener
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes PropertyChangeListener
     * @param l - - the PropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Sets content of given volume
     * @param volumeType the type of volume for which the content should be set
     * @param path the list of resource URLs
     * @throws IllegalArgumentException if the library does not support given volumeType
     */
    void setContent(String volumeType, List<URL> path) throws IllegalArgumentException;

}
