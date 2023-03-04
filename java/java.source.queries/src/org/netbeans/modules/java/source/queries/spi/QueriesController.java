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
package org.netbeans.modules.java.source.queries.spi;

import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.queries.APIAccessor;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.netbeans.modules.java.source.queries.SPIAccessor;
import org.netbeans.modules.java.source.queries.api.Function;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.api.Updates;

/**
 * SPI for the {@link Queries} and {@link Updates}.
 * The implementation is registered in META-INF/services.
 * @author Tomas Zezula
 */
public interface QueriesController {
    /**
     * Performs the query.
     * @param ctx the query {@link Context} holding the
     * query function and query parameter
     * @return the result of the query function
     * @throws IOException in case of error
     */
    <R> R runQuery(@NonNull Context<R> ctx) throws QueryException;

    /**
     * Performs the modification.
     * @param ctx the modification {@link Context} holding the
     * modification function and modification parameter
     * @return the result of the query function, true means changes
     * were applied, false changes were rolled back.
     * @throws IOException in case of error
     */
    boolean runUpdate(@NonNull Context<Boolean> ctx) throws QueryException;

    /**
     * Context of the query or modification
     */
    public static final class Context<R> {

        static {
            SPIAccessor.setInstance(new Accessor());
        }

        private final SPIFnc<?, R> toRun;
        private final URL forURL;

        private Context (
            @NonNull final URL forURL,
            @NonNull final SPIFnc<?, R> toRun) {
            assert forURL != null;
            assert toRun != null;
            this.toRun = toRun;
            this.forURL = forURL;
        }

        /**
         * Return the {@link URL} of a file for which the {@link Queries} or
         * {@link Updates} instance was created.
         * @return the file
         */
        public URL getURL(){
            return forURL;
        }

        /**
         * Performs the query function on query parameter.
         * using the provided {@link ModelOperations}
         * @param op the {@link ModelOperations} SPI to used to
         * perform the operation.
         * @return the query function result
         */
        public R execute(ModelOperations op) throws QueryException {
            return toRun.apply(op);
        }

        private static class Accessor extends SPIAccessor {
            @Override
            public <P extends Queries,R>  QueriesController.Context<R> createContext(
                final Function<P,R> fnc,
                final P param) {
                return new Context<R>(param.getURL(), new SPIFnc<P, R>(fnc, param));
            }
        }

        private static class SPIFnc<P extends Queries, R> implements Function<ModelOperations, R>{

            private final Function<P,R> fnc;
            private final P param;

            private SPIFnc(
                @NonNull final Function<P,R> fnc,
                @NonNull final P param) {
                assert fnc != null;
                assert param != null;
                this.fnc = fnc;
                this.param = param;
            }

            @Override
            public R apply(@NonNull final ModelOperations spi) throws QueryException {
                APIAccessor.getInstance().attach(param, spi);
                try {
                    return fnc.apply(param);
                } finally {
                    APIAccessor.getInstance().detach(param);
                }
            }
        }
    }
}
