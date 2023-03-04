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

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 *
 * @author Petr Hejl
 */
public class TestLookup extends Lookup {

    private final Map<Class<?>, Object> lookups;

    public TestLookup(Map<Class<?>, Object> lookups) {
        this.lookups = new HashMap<Class<?>, Object>(lookups);
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        return (T) lookups.get(clazz);
    }

    @Override
    public <T> Result<T> lookup(Template<T> template) {
        T instance = (T) lookups.get(template.getType());
        if (instance == null) {
            return new TestLookup.DummyResult<T>();
        }
        return new TestLookup.DummyResult<T>(instance);
    }

    private class DummyResult<T> extends Result<T> {

        private final Collection<T> instances = new ArrayList<T>();

        public DummyResult(T... instances) {
            if (instances != null) {
                for (T instance : instances) {
                    this.instances.add(instance);
                }
            }
        }

        @Override
        public void addLookupListener(LookupListener l) {
        }

        @Override
        public void removeLookupListener(LookupListener l) {
        }

        @Override
        public Collection<? extends T> allInstances() {
            return instances;
        }

    }
}
