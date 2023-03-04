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

package org.netbeans.modules.j2ee.metadata.model.spi;

import org.netbeans.modules.j2ee.metadata.model.MetadataModelAccessor;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.util.Parameters;

/**
 * Provides a way to create {@link MetadataModel} instances. They cannot be
 * created directly; instead, a model provider implements the
 * {@link MetadataModelImplementation} interface and uses this class.
 *
 * @author Andrei Badea
 * @since 1.2
 */
public final class MetadataModelFactory {

    private MetadataModelFactory() {
    }

    /**
     * Creates a metadata model.
     *
     * @param  impl the instance of {@link MetadataModelImplementation} which
     *         the model will delegate to; never null.
     * @return a {@link MetadataModel} delegating to <code>impl</code>; never null.
     * @throws NullPointerException if the <code>impl</code> parameter was null.
     */
    public static <T> MetadataModel<T> createMetadataModel(MetadataModelImplementation<T> impl) {
        Parameters.notNull("impl", impl); // NOI18N
        return MetadataModelAccessor.DEFAULT.createMetadataModel(impl);
    }
}
