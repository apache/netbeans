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
package org.netbeans.modules.turbo;

/**
 * Default NOP implementation. It's dangerous because it
 * understands all requests so it's explictly managed by
 * the Turbo query and used as a fallback.
 *
 * @author Petr Kuzel
 */
final class DefaultTurboProvider implements TurboProvider {

    private static final TurboProvider DEFAULT_INSTANCE = new DefaultTurboProvider();

    public static TurboProvider getDefault() {
        return DEFAULT_INSTANCE;
    }

    private DefaultTurboProvider() {
    }

    public boolean recognizesAttribute(String name) {
        return true;
    }

    public boolean recognizesEntity(Object key) {
        return true;
    }

    public Object readEntry(Object key, String name, MemoryCache memoryCache) {
        return null;
    }

    public boolean writeEntry(Object key, String name, Object value) {
        return true;
    }

    public String toString() {
        return "DefaultTurboProvider a NOP implementation";  // NOI18N
    }
}
