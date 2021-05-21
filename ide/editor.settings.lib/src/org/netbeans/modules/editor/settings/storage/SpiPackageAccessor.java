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

package org.netbeans.modules.editor.settings.storage;

import java.util.concurrent.Callable;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;

/**
 *
 * @author Vita Stejskal
 */
public abstract class SpiPackageAccessor {

    private static SpiPackageAccessor ACCESSOR = null;
    
    public static synchronized void register(SpiPackageAccessor accessor) {
        assert ACCESSOR == null : "Can't register two SPI package accessors!";
        ACCESSOR = accessor;
    }
    
    public static synchronized SpiPackageAccessor get() {
        // Trying to wake up HighlightsLayer ...
        try {
            Class<?> clazz = Class.forName(StorageReader.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        assert ACCESSOR != null : "There is no SPI package accessor available!";
        return ACCESSOR;
    }
    
    /** Creates a new instance of SpiPackageAccessor */
    protected SpiPackageAccessor() {
    }
    
    public abstract String storageFilterGetStorageDescriptionId(StorageFilter f);
    public abstract void storageFilterInitialize(StorageFilter f, Callable<Void> notificationCallback);
}
