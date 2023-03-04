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
package org.netbeans.spi.jumpto.support;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * A change of {@link AsyncDescriptor}.
 * The information that the {@link AsyncDescriptor} was resolved into a new one(s).
 * @since 1.49
 * @author Tomas Zezula
 */
public final class DescriptorChangeEvent<T> extends EventObject {

    private final Collection<? extends T> replacement;

    /**
     * Creates a new event.
     * @param source the originating descriptor
     * @param replacement the descriptor replacement(s). In case of an empty {@link Collection}
     * the originating descriptor is removed.
     */
    public DescriptorChangeEvent(
            @NonNull final T source,
            @NonNull final Collection<? extends T> replacement) {
        super(source);
        Parameters.notNull("descriptors", replacement); //NOI18N
        this.replacement = Collections.unmodifiableCollection(replacement);
    }

    /**
     * Returns the replacement.
     * @return the replacement
     */
    @NonNull
    public Collection<? extends T> getReplacement() {
        return replacement;
    }
}
