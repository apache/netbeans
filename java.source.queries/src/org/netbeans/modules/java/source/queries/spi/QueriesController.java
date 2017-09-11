/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
