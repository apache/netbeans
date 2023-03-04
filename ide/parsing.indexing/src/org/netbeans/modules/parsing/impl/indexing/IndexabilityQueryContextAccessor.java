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
package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation;
import org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation.IndexabilityQueryContext;
import org.openide.util.Exceptions;

public abstract class IndexabilityQueryContextAccessor {

    private static volatile IndexabilityQueryContextAccessor instance;

    public static synchronized IndexabilityQueryContextAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(
                        IndexabilityQueryImplementation.IndexabilityQueryContext.class.getName(),
                        true,
                        IndexabilityQueryContextAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return instance;
    }

    public static void setInstance(@NonNull final IndexabilityQueryContextAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }

    public abstract IndexabilityQueryContext createContext(
            URL indexable,
            String indexerName,
            URL root
    );
}
