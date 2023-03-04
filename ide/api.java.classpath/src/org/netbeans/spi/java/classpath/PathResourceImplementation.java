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

import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * SPI interface for one classpath entry.
 * @see ClassPathImplementation
 * @since org.netbeans.api.java/1 1.4
 */
public interface PathResourceImplementation {

    public static final String PROP_ROOTS = "roots";    //NOI18N

    /** Roots of the class path entry.
     *  In the case of simple resource it returns array containing just one URL.
     *  In the case of composite resource it returns array containing one or more URL.
     * @return array of URL, never returns null.
     */
    public URL[] getRoots();

    /**
     * Returns ClassPathImplementation representing the content of the PathResourceImplementation.
     * If the PathResourceImplementation represents leaf resource, it returns null.
     * The ClassPathImplementation is live and can be used for path resource content
     * modification.
     * <p><strong>Semi-deprecated.</strong> There was never a real reason for this method to exist.
     * If implementing <code>PathResourceImplementation</code> you can simply return null;
     * it is unlikely anyone will call this method anyway.
     * @return classpath handle in case of composite resource; null for leaf resource
     */
    public ClassPathImplementation getContent();

    /**
     * Adds property change listener.
     * The listener is notified when the roots of the entry are changed.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes property change listener.
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
