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
package org.netbeans.modules.j2ee.dd.impl.common;

import org.openide.filesystems.FileLock;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;

import java.io.IOException;
import java.io.Reader;

/**
 * @author pfiala
 */
public interface DDProviderDataObject {
    /**
     * Provides Reader to save binary data.
     * @return the Reader
     * @throws IOException
     */
    Reader createReader() throws IOException;

    /**
     * Locks binary data if possible.
     * @return the data lock
     */
    FileLock getDataLock();

    /**
     * Writes data from model to the cache and saves the data if needed.
     * @param model
     * @param dataLock
     */
    void writeModel(RootInterface model, FileLock dataLock);

    /**
     * Obtains data lock, writes data from model to the cache and saves the data if needed.
     * Finally releases the lock.
     * @param model
     */
    void writeModel(RootInterface model) throws IOException;
}
