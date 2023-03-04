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

import java.util.EventListener;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Listener for changes of {@link AsyncDescriptor}.
 * @since 1.49
 * @author Tomas Zezula
 */
public interface DescriptorChangeListener<T> extends EventListener {
    /**
     * Called when the {@link AsyncDescriptor} was fully resolved.
     * @param event the information about new descriptor(s)
     */
    void descriptorChanged(@NonNull DescriptorChangeEvent<T> event);
}
