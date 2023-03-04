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

package org.netbeans.modules.j2ee.persistence.api;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.openide.filesystems.FileObject;

/**
 * This class allows retrieving the default persistence location in a project
 * or creating this location if it doesn't exist.
 *
 * @author Andrei Badea
 */
public final class PersistenceLocation {

    private PersistenceLocation() {
    }

    /**
     * Returns the default persistence location in the given project.
     *
     * @param  project the project.
     * @return the persistence location or null if the project does not have
     *         a persistence location or the location does not exist.
     */
    public static FileObject getLocation(Project project) {
        PersistenceLocationProvider provider = project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.getLocation();
        }
        return null;
    }

    /**
     * Returns the FileObject's persistence location in the given project.
     *
     * @param  project the project.
     * @param  fo the FileObject
     * @return the persistence location or null if the project does not have
     *         a persistence location or the location does not exist for
     *         the given FileObject.
     * @since 1.37
     */
    public static FileObject getLocation(Project project, FileObject fo) {
        PersistenceLocationProvider provider = project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.getLocation(fo);
        }
        return null;
    }

    /**
     * Creates the default persistence location in the given project.
     *
     * @param  project the project.
     * @return the persistence location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     * @throws IOException if the persistence location could not be created
     *         or the project did not have an implementation of
     *         PersistenceLocationProvider in its lookup.
     */
    public static FileObject createLocation(Project project) throws IOException {
        PersistenceLocationProvider provider = project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.createLocation();
        }
        throw new IOException("The project " + project + " does not have an implementation of PersistenceLocationProvider in its lookup"); // NOI18N
    }

    /**
     * Creates the FileObject's persistence location in the given project.
     *
     * @param  project the project.
     * @param fo the FileObject
     * @return the persistence location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     * @throws IOException if the persistence location could not be created
     *         or the project did not have an implementation of
     *         PersistenceLocationProvider in its lookup.
     * @since 1.37
     */
    public static FileObject createLocation(Project project, FileObject fo) throws IOException {
        PersistenceLocationProvider provider = project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.createLocation(fo);
        }
        throw new IOException("The project " + project + " does not have an implementation of PersistenceLocationProvider in its lookup"); // NOI18N
    }
}
