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

import org.netbeans.api.annotations.common.NonNull;

/**
 * Descriptor which asynchronously translates itself into a new one(s) when it's fully computed.
 * @since 1.49
 * @author Tomas Zezula
 */
public interface AsyncDescriptor<T> {
    /**
     * Adds a {@link DescriptorChangeListener}.
     * @param listener the listener to be added
     */
    void addDescriptorChangeListener(@NonNull DescriptorChangeListener<T> listener);
    /**
     * Removes a {@link DescriptorChangeListener}.
     * @param listener the listener to be removed
     */
    void removeDescriptorChangeListener(@NonNull DescriptorChangeListener<T> listener);
    /**
     * Returns true if the transient (not fully computed) descriptor has correct name casing.
     * The transient descriptor may differ from the resolved descriptor in case. This happens when it's created
     * from the index in case insensitive query and the additional resolution is needed to compute correct name.
     * @return true if the descriptor's name has correct case.
     */
    boolean hasCorrectCase();
}
