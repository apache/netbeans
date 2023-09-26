/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.java.queries;

import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result2;
import org.netbeans.modules.java.classpath.QueriesAccessor;

/**
 * Extended query about the binaries.
 * In addition to
 * {@link BinaryForSourceQueryImplementation}, this query also allows one
 * to specify whether {@link #computePreferBinaries(java.lang.Object) binaries are preferred}
 * - e.g. copied instead of obtaining them by compiling the sources.
 * The typical implementation of the query looks like:
 * {@snippet file="org/netbeans/api/java/queries/BinaryForSourceQuery2Test.java" region="SampleQuery"}
 * 
 * @param <Result> any type this implementation wants to use as a result
 * @see BinaryForSourceQuery
 * @see org.netbeans.api.java.queries.SourceForBinaryQuery
 * @see BinaryForSourceQueryImplementation
 * @see SourceForBinaryQueryImplementation
 * @since 1.58
 */
public interface BinaryForSourceQueryImplementation2<Result> extends BinaryForSourceQueryImplementation {

    /** Default
     * implementation of {@link BinaryForSourceQueryImplementation#findBinaryRoots(java.net.URL)}.
     * Calls {@link #findBinaryRoots2(java.net.URL)} and if the method returns non-{@code null}
     * value, then it creates instances of {@link org.netbeans.api.java.queries.BinaryForSourceQuery.Result2} and returns
     * it. Otherwise it returns {@code null}.
     * <p>
     * Override {@link #findBinaryRoots2(java.net.URL)}, not this method!
     *
     * @param sourceRoot
     * @return non-{@code null} result if this query has an answer for the provided {@code sourceRoot}
     * @since 1.58
     */
    @Override
    public default BinaryForSourceQuery.Result2 findBinaryRoots(URL sourceRoot) {
        Result res = findBinaryRoots2(sourceRoot);
        if (res == null) {
            return null;
        }
        return QueriesAccessor.getInstance().create(this, res);
    }
    
    /**
     * Returns the binary root result for a given source root.
     * <p>
     * The returned {@code Result} must be consistent. It means that for
     * repeated calling of this method with the same recognized root the method has to
     * return the {@link Object#equals(java.lang.Object) equal} result with the
     * same {@link Object#hashCode()}. The implementation of the
     * {@link #findBinaryRoots(java.net.URL)} method makes sure the same
     * {@link org.netbeans.api.java.queries.BinaryForSourceQuery.Result2} instance is returned for two
     * equal {@code Result} objects.
     *
     * @param sourceRoot the source path root
     * @return {@code null} if the sourceRoot is not recognized, or any object
     *   to feed into {@link #computeRoots(java.lang.Object)} &amp; co. methods
     *   any time later
     * @since 1.58
     */
    public Result findBinaryRoots2(URL sourceRoot);

    /** Implementation of {@link Result2#getRoots()}.
     *
     * @param result object created by {@link #findBinaryRoots2(java.net.URL)}
     *   method
     * @return result to return from {@link Result2#getRoots()}
     * @since 1.58
     */
    public URL[] computeRoots(Result result);


    /** Implementation of {@link Result2#preferBinaries()}.
     *
     * @param result object created by {@link #findBinaryRoots2(java.net.URL)}
     *   method
     * @return result to return from {@link Result2#preferBinaries()}
     * @since 1.58
     */
    public boolean computePreferBinaries(Result result);


    /** Implementation of {@link Result2#addChangeListener(javax.swing.event.ChangeListener) add}
     * and {@link Result2#removeChangeListener(javax.swing.event.ChangeListener) remove} listener.
     *
     * @param result object created by {@link #findBinaryRoots2(java.net.URL)}
     *   method
     * @param l the listener to operate on
     * @param add add or remove the listener?
     * @since 1.58
     */
    public void computeChangeListener(Result result, boolean add, ChangeListener l);
    
}
