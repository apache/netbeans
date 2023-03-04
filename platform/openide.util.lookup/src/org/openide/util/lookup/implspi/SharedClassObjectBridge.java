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

package org.openide.util.lookup.implspi;

public abstract class SharedClassObjectBridge {

    private static SharedClassObjectBridge INSTANCE;

    public static synchronized void setInstance(SharedClassObjectBridge bridge) {
        assert INSTANCE == null;
        INSTANCE = bridge;
    }

    public static <T> T newInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException{
        SharedClassObjectBridge bridge;
        synchronized (SharedClassObjectBridge.class) {
            bridge = INSTANCE;
        }
        T o = null;
        if (bridge != null) {
            o = bridge.findObject(clazz);
        }
        if (o == null) {
            o = clazz.newInstance();
        }
        return o;
    }

    protected SharedClassObjectBridge() {}

    protected abstract <T> T findObject(Class<T> c) throws InstantiationException, IllegalAccessException;

}
