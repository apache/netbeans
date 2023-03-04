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

import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;

/**
 * The SPI for {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScopes}.
 *
 * @author Andrei Badea
 *
 * @see org.netbeans.modules.j2ee.persistence.api.PersistenceScopes
 * @see PersistenceScopesFactory
 */
public interface PersistenceScopesImplementation {

    /**
     * Returns the persistence scopes contained in this instance.
     *
     * @return an array of <code>PersistenceScope</code> instances; never null.
     */
    PersistenceScope[] getPersistenceScopes();

    /**
     * Adds a property change listener, allowing to listen on properties, e.g.
     * {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScopes#PROP_PERSISTENCE_SCOPES}.
     *
     * @param  listener the listener to add; can be null.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener.
     *
     * @param  listener the listener to remove; can be null.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
