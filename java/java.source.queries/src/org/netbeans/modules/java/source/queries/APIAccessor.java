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
package org.netbeans.modules.java.source.queries;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.spi.ModelOperations;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class APIAccessor {
    private static volatile APIAccessor instance;

    public static void setInstance(@NonNull final APIAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }

    public static synchronized APIAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(
                        Queries.class.getName(),
                        true,
                        APIAccessor.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return instance;
    }

    public abstract void attach(@NonNull Queries q, @NonNull ModelOperations ops);
    public abstract void detach(@NonNull Queries q);
}
