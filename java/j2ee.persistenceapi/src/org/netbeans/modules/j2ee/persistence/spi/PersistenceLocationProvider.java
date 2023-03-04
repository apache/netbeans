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

package org.netbeans.modules.j2ee.persistence.spi;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * This interface should be implemented in a context which supports
 * Java Persistence API, whether or not is contains a persistence.xml file. It
 * contains methods for creating/retrieving the default location for persistence.xml
 * files. For example it can be implemented by a project and can be used by 
 * a client which wants to create a persistence scope in that project.
 *
 * @author Andrei Badea
 */
public interface PersistenceLocationProvider {

    /**
     * Returns the default location for persistence.xml and related files.
     *
     * @return the default location or null if it does not exist.
     */
    FileObject getLocation();

    /**
     * Returns the location for persistence.xml and related files for the given
     * FileObject.
     *
     * @param fo the FileObject
     * @return the location or null if it does not exist.
     * @since 1.37
     */
    default FileObject getLocation(FileObject fo) {
        return getLocation();
    }

    /**
     * Creates (if it does not exist) and returns the default location for
     * persistence.xml and related files.
     *
     * @return the default location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     *
     * @throws IOException if an error occured while creating the location
     *         of persistence.xml
     */
    FileObject createLocation() throws IOException;

    /**
     * Creates (if it does not exist) and returns the location for
     * persistence.xml and related files for the given FileObject.
     *
     * @param fo the FileObject
     * @return the location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     *
     * @throws IOException if an error occured while creating the location
     *         of persistence.xml
     * @since 1.37
     */
    default FileObject createLocation(FileObject fo) throws IOException {
        return createLocation();
    }
}
