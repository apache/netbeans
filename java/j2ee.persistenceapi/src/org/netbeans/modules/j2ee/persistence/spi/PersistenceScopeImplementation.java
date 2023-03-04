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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.openide.filesystems.FileObject;

/**
 * The SPI for {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScope}.
 *
 * @author Andrei Badea
 *
 * @see org.netbeans.modules.j2ee.persistence.api.PersistenceScope
 * @see PersistenceScopeFactory
 */
public interface PersistenceScopeImplementation {

    /**
     * Returns the persistence.xml file of this persistence scope.
     *
     * @return the persistence.xml file or null if it the persistence.xml file does
     * not exist.
     */
    FileObject getPersistenceXml();

    /**
     * Provides the classpath of this persistence scope, which covers the sources
     * of the entity classes referenced by the persistence.xml file, as well
     * as the referenced JAR files.
     *
     * @return the persistence scope classpath; never null.
     */
    ClassPath getClassPath();

    MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName);
}
