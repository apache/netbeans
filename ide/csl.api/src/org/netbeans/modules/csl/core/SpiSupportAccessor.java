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
package org.netbeans.modules.csl.core;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SpiSupportAccessor {
    private static volatile SpiSupportAccessor instance;

    @NonNull
    public static synchronized SpiSupportAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(CancelSupport.class.getName(), true, SpiSupportAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public static void setInstance(@NonNull final SpiSupportAccessor inst) {
        Parameters.notNull("inst", inst);   //NOI18N
        instance = inst;
    }
    public abstract void setCancelSupport(@NonNull CancelSupportImplementation cancelSupport);
    public abstract void removeCancelSupport(@NonNull CancelSupportImplementation cancelSupport);
}
