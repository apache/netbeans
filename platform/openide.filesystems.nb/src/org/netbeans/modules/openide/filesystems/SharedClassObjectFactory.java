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

package org.netbeans.modules.openide.filesystems;

import org.openide.filesystems.spi.CustomInstanceFactory;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handles creation of SharedClassObjects for Filesystems API.
 * 
 * @author sdedic
 */
@ServiceProvider(service = CustomInstanceFactory.class)
public final class SharedClassObjectFactory implements CustomInstanceFactory {

    @Override
    public <T> T createInstance(Class<T> clazz) {
        if (SharedClassObject.class.isAssignableFrom(clazz)) {
            return (T)SharedClassObject.findObject(clazz.asSubclass(SharedClassObject.class), true);
        } else {
            return null;
        }
    }
    
}
