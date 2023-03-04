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
package org.netbeans.modules.jumpto.common;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.jumpto.support.Descriptor;

/**
 *
 * @author Tomas Zezula
 */
public abstract class DescriptorAccessor {

    private static volatile DescriptorAccessor instance;

    public static void setInstance(@NonNull final DescriptorAccessor anInstance) {
        instance = anInstance;
    }

    @NonNull
    public static synchronized DescriptorAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(Descriptor.class.getName(), true, DescriptorAccessor.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            assert instance != null;
        }
        return instance;
    }

    @CheckForNull
    public abstract Object getAttribute(@NonNull Descriptor descriptor, @NonNull final String attribute);

    public abstract void setAttribute(@NonNull Descriptor descriptor, @NonNull final String attribute, @NullAllowed Object value);
}
