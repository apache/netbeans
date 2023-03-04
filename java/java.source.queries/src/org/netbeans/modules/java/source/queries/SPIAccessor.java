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
import org.netbeans.modules.java.source.queries.api.Function;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.spi.QueriesController;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SPIAccessor {

    private static volatile SPIAccessor instance;

    public static synchronized SPIAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(
                        QueriesController.Context.class.getName(),
                        true,
                        SPIAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return instance;
    }

    public static void setInstance(@NonNull final SPIAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }

    public abstract <P extends Queries,R>  QueriesController.Context<R> createContext(
            final Function<P,R> fnc,
            final P param
            );

}
