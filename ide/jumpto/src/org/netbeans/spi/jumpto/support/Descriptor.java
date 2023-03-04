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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.jumpto.common.DescriptorAccessor;
import org.openide.util.Parameters;

/**
 * A base for the descriptor classes.
 * @author Tomas Zezula
 * @since 1.55
 */
public class Descriptor {
    static {
        DescriptorAccessor.setInstance(new DescriptorAccessorImpl());
    }

    private final Map<String,Object> attrs = new HashMap<>();

    @CheckForNull
    final Object getAttribute(@NonNull final String attr) {
        Parameters.notNull("attr", attr);   //NOI18N
        return attrs.get(attr);
    }

    final void setAttribute(
            @NonNull final String attr,
            @NullAllowed final Object value) {
        Parameters.notNull("attr", attr);   //NOI18N
        this.attrs.put(attr, value);
    }

    private static final class DescriptorAccessorImpl extends DescriptorAccessor {

        @Override
        public Object getAttribute(
                @NonNull final Descriptor descriptor,
                @NonNull final String attribute) {
            return descriptor.getAttribute(attribute);
        }

        @Override
        public void setAttribute(
                @NonNull final Descriptor descriptor,
                @NonNull final String attribute,
                @NullAllowed final Object value) {
            descriptor.setAttribute(attribute, value);
        }
    }
}
