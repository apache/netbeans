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

package org.netbeans.modules.nativeexecution.test;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexander Simon
 */
@ServiceProvider(service=KeyringProvider.class, position=0)
public class DummyKeyringProvider implements KeyringProvider {
    private Map<String,char[]> cache = new HashMap<>();

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public char[] read(String key) {
        char[] copy =  cache.get(key);
        if (copy == null) {
            return null;
        }
        char[] password = new char[copy.length];
        System.arraycopy(copy, 0, password, 0, copy.length);
        return password;
    }

    @Override
    public void save(String key, char[] password, String description) {
        char[] copy = new char[password.length];
        System.arraycopy(password, 0, copy, 0, password.length);
        cache.put(key, copy);
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }
}

