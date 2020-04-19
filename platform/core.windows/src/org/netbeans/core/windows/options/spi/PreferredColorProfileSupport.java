/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.core.windows.options.spi;

/**
 * Whenever a component supports color profiles it can implement this interface
 * in order to be updated when LaF change requested with a color profile change.
 *
 * @author lkishalmi
 * @since 2.92
 */
public interface PreferredColorProfileSupport {
    /**
     * This method shall try to set the current color profile of the supported
     * component according to the provided profile name. The default profile
     * name is usually called {@code "NetBeans"}.
     *
     * @param profileName the color profile name to be set.
     * @since 2.92
     */
    void setPreferredProfile(String profileName);

    /**
     * This method shall return the name of the current color profile used by
     * the implementation. It can return <code>null</code> if this information
     * cannot be determined (e.g. some user customized profile is in use). In
     * that case the color profile change option will always be presented to
     * the user.
     *
     * @return the current color profile name or <code>null</code>.
     * @since 2.92
     */
    String getCurrentProfileName();
}
