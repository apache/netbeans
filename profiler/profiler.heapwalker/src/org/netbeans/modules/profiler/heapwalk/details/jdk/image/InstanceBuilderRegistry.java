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
package org.netbeans.modules.profiler.heapwalk.details.jdk.image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.lib.profiler.heap.Instance;

/**
 * Support for automatic selection of {@link InstanceBuilder} for an instance.
 *
 * @author Jan Taus
 */
class InstanceBuilderRegistry {

    private static class RegisteredBuilder {

        private final String mask;
        private final InstanceBuilder<?> builder;

        public RegisteredBuilder(String mask, InstanceBuilder<?> builder) {
            this.mask = mask;
            this.builder = builder;
        }
    }
    private final List<RegisteredBuilder> builders;

    public InstanceBuilderRegistry() {
        this.builders = new ArrayList<RegisteredBuilder>();
    }

    public void register(String mask, InstanceBuilder<?> builder) {
        builders.add(new RegisteredBuilder(mask, builder));
    }

    public void register(Class<?> type, boolean subtypes, InstanceBuilder<?> builder) {
        register(FieldAccessor.getClassMask(type, subtypes), builder);
    }

    /**
     * Returns builder which creates object of given
     * <code>type</code>. First registered builder matching given type and registered for given instance is returned. No
     * <em>best match</em> is performed.
     *
     * @return builder or <code>null</code>.
     */
    public <T> InstanceBuilder<? extends T> getBuilder(Instance instance, Class<T> type) {
        for (RegisteredBuilder builder : builders) {
            if (FieldAccessor.matchClassMask(instance, builder.mask)) {
                if (type.isAssignableFrom(builder.builder.getType())) {
                    return (InstanceBuilder<? extends T>) builder.builder;
                }
            }
        }
        return null;
    }

    public String[] getMasks(Class<?>... types) {
        Set<String> masks = new HashSet<String>();
        for (RegisteredBuilder builder : builders) {
            for (Class<?> type : types) {
                if (type.isAssignableFrom(builder.builder.getType())) {
                    masks.add(builder.mask);
                }
            }
        }
        return masks.toArray(new String[0]);
    }
}
