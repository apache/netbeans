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

package org.netbeans.spi.keyring;

/**
 * Provider for a keyring.
 * Should be registered in global lookup.
 * Providers will be searched in the order in which they are encountered
 * until one which is {@link #enabled} is found.
 * There is a default platform-independent implementation at position 1000
 * which should always be enabled.
 * <p>All SPI calls are made from one thread at a time, so providers need not be synchronized.
 */
public interface KeyringProvider {

    /**
     * Check whether this provider can be used in the current JVM session.
     * If integrating a native keyring, this should attempt to load related
     * libraries and check whether they can be found.
     * This method will be called at most once per JVM session,
     * prior to any other methods in this interface being called.
     * @return true if this provider should be used, false if not
     */
    boolean enabled();

    /**
     * Read a key from the ring.
     * @param key the identifier of the key
     * @return its value if found (elements may be later nulled out), else null if not present
     */
    char[] read(String key);

    /**
     * Save a key to the ring.
     * If it could not be saved, do nothing.
     * If the key already existed, overwrite the password.
     * @param key a key identifier
     * @param password the password or other sensitive information associated with the key
     *                 (elements will be later nulled out)
     * @param description a user-visible description of the key (may be null)
     */
    void save(String key, char[] password, String description);

    /**
     * Delete a key from the ring.
     * If the key was not in the ring to begin with, do nothing.
     * @param key a key identifier
     */
    void delete(String key);

}
