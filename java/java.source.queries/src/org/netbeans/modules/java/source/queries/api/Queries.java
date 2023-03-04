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
package org.netbeans.modules.java.source.queries.api;

import java.net.URL;
import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.queries.APIAccessor;
import org.netbeans.modules.java.source.queries.SPIAccessor;
import org.netbeans.modules.java.source.queries.spi.ModelOperations;
import org.netbeans.modules.java.source.queries.spi.QueriesController;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Support for queries
 * @author Tomas Zezula
 */
public class Queries {

    static {
        APIAccessor.setInstance(new Accessor());
    }

    static final QueriesController ctl = Lookup.getDefault().lookup(QueriesController.class);
    private final URL forURL;
    ModelOperations impl;

    Queries(@NonNull final URL forURL) {
        assert forURL != null;
        this.forURL = forURL;
    }

    /**
     * Returns an {@link URL} of the file for which the {@link Queries} was created.
     * @return an {@link URL}
     */
    @NonNull
    public final URL getURL() {
        return forURL;
    }

    /**
     * Returns the fully qualified names of top level classes in the
     * compilation unit represented by {@link Queries#getURL()}
     * @return a collection of fully qualified names
     * @throws QueryException in case of failure
     */
    @NonNull
    public final Collection<? extends String> getTopLevelClasses() throws QueryException {
        return impl.getTopLevelClasses();
    }

    /**
     * Returns a binary name as specified by JLS §13.1 for given
     * fully qualified name
     * @param cls the fully qualified name of the class
     * @return the binary name or null if cls cannot be resolved
     * @throws QueryException in case of failure
     */
    @CheckForNull
    public final String getClassBinaryName(
            @NonNull final String cls) throws QueryException {
        Parameters.notNull("cls", cls);     //NOI18N
        return impl.getClassBinaryName(cls);
    }

    /**
     * Returns the fully qualified name of the super class for
     * for given class.
     * @param cls the class fully qualified name
     * @return the fully qualified name of the super class or null
     * when the class has no super type, eg j.l.Object, error type
     * @throws QueryException in case of failure
     */
    @CheckForNull
    public final String getSuperClass(final @NonNull String cls) throws QueryException {
        Parameters.notNull("cls", cls);     //NOI18N
        return impl.getSuperClass(cls);
    }

    /**
     * Returns the fully qualified names of the implemented interfaces for
     * for given class.
     * @param cls the class fully qualified name
     * @return the fully qualified names of the implemented interfaces
     * @throws QueryException in case of failure
     */
    @NonNull
    public final Collection<? extends String> getInterfaces(final @NonNull String cls) throws QueryException {
        Parameters.notNull("cls", cls);     //NOI18N
        return impl.getInterfaces(cls);
    }

    /**
     * Returns names of the methods complying to given types.
     * @param cls the fully qualified name of the class to look up methods in
     * @param useRawTypes if true the erasure is done before comparison
     * @param returnType the return type of the method specified by
     * the class canonical name, @see Class#getCanonicalName. If useRawTypes is
     * false parameterized type is allowed.
     * @param parameterTypes the types of the method parameters specified by
     * the class canonical name, @see Class#getCanonicalName
     * (null represent any type). If useRawTypes is false parameterized types
     * are allowed.
     * @return a collection of method names
     * @throws QueryException in case of failure
     */
    @NonNull
    public final Collection<? extends String> getMethodNames(
            @NonNull final String cls,
            boolean useRawTypes,
            @NullAllowed final String returnType,
            @NullAllowed final String[] parameterTypes) throws QueryException {
        Parameters.notNull("cls", cls); //NOI18N
        return impl.getMethodNames(cls, useRawTypes, returnType, parameterTypes);
    }

    /**
     * Returns names of the fields complying to type.
     * @param cls the fully qualified name of the class to look up methods in
     * @param useRawTypes if true the erasure is done before comparison
     * @param type the type of the field specified by the class canonical name,
     * @see Class#getCanonicalName (null represents any type).
     * If useRawTypes is false parameterized type is allowed.
     * @return a collection of field names
     * @throws QueryException in case of failure
     */
    @NonNull
    public final Collection<? extends String> getFieldNames(
            @NonNull final String cls,
            final boolean useRawTypes,
            @NullAllowed final String type) throws QueryException {
        Parameters.notNull("cls", cls); //NOI18N
        return impl.getFieldNames(cls, useRawTypes, type);
    }

    /**
     * Returns a method start and end position.
     * @param cls the fully qualified name of the class to look up methods in
     * @param methodName name of the method
     * @param useRawTypes if true the erasure is done before comparison
     * @param returnType the return type of the method specified by
     * the class canonical name, @see Class#getCanonicalName. If useRawTypes is
     * false parameterized type is allowed.
     * @param parameterTypes the types of the method parameters specified by
     * the class canonical name, @see Class#getCanonicalName. If useRawTypes is
     * false parameterized types are allowed.
     * @return a two elements int array containing method start and end position,
     * or null if cannot be found
     * @throws QueryException in case of failure
     */
    @CheckForNull
    public final int[] getMethodSpan(
            @NonNull final String cls,
            @NonNull final String methodName,
            final boolean useRawTypes,
            @NonNull final String returnType,
            @NonNull final String... parameterTypes) throws QueryException {
        Parameters.notNull("cls", cls); //NOI18N
        Parameters.notNull("methodName", methodName); //NOI18N
        Parameters.notNull("returnType", returnType);     //NOI18N
        Parameters.notNull("parameterTypes", parameterTypes); //NOI18N
        return impl.getMethodSpan(
                cls,
                methodName,
                useRawTypes,
                returnType,
                parameterTypes);
    }

    /**
     * Performs the query
     * @param forURL the URL of the file to perform query on
     * @param queryFnc the query function
     * @return the result of query function
     * @throws QueryException in case of exception
     */
    @CheckForNull
    public static <T> T query (
        @NonNull final URL forURL,
        @NonNull final Function<Queries,T> queryFnc) throws QueryException {
        Parameters.notNull("forURL", forURL);     //NOI18N
        Parameters.notNull("queryFnc", queryFnc);   //NOI18N
        if (ctl == null) {
            throw new IllegalStateException("No QueriesController found in the Lookup");    //NOI18N
        }
        final Queries q = new Queries(forURL);
        final QueriesController.Context<T> ctx = SPIAccessor.getInstance().createContext(queryFnc, q);
        return ctl.runQuery(ctx);
    }


    private void attach(@NonNull final ModelOperations impl) {
        assert impl != null;
        this.impl = impl;
    }

    private void detach() {
        this.impl = null;
    }

    private static class Accessor extends APIAccessor {

        @Override
        public void attach(Queries q, ModelOperations ops) {
            q.attach(ops);
        }

        @Override
        public void detach(Queries q) {
            q.detach();
        }
    }
}
