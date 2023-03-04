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
package org.netbeans.spi.java.classpath;

import java.util.List;
import java.beans.PropertyChangeListener;

/**
 * SPI interface for ClassPath.
 * @see ClassPathFactory
 * @since org.netbeans.api.java/1 1.4
 */
public interface ClassPathImplementation {

    public static final String PROP_RESOURCES = "resources";    //NOI18N

    /**
     * Returns list of entries, the list is unmodifiable.
     * @return List of PathResourceImplementation, never returns null
     * it may return an empty List
     */
    public List<? extends PathResourceImplementation> getResources();

    /**
     * Adds property change listener.
     * The listener is notified when the set of entries has changed.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes property change listener
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
