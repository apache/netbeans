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
import org.netbeans.modules.java.classpath.QueriesAccessor;

/**
 * Extended information about where binaries (classfiles) corresponding to
 * Java sources can be found, this is intended to be the inverse of 
 * the {@link SourceForBinaryQueryImplementation}. In addition to
 * {@link BinaryForSourceQueryImplementation}, this one also allows
 * to specify whether {@link #computePreferBinaries(java.lang.Object) binaries are preferred}
 * - e.g. copied instead of always compiling the sources.
 * 
 * @param <Result> any type this implementation wants to represent a result
 * @see BinaryForSourceQuery
 * @see SourceForBinaryQuery
 * @see SourceForBinaryQueryImplementation
 * @since 1.58
 */
public interface BinaryForSourceQueryImplementation2<Result> extends BinaryForSourceQueryImplementation {

    /**
     * Implementation of {@link BinaryForSourceQueryImplementation#findBinaryRoots(java.net.URL)}.
     * Calls {@link #findBinaryRoots2(java.net.URL)} and if the method returns non-nu
     * @param sourceRoot
     * @return
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
     * Returns the binary root(s) for a given source root.
     * <p>
     * The returned BinaryForSourceQuery.Result must be a singleton. It means that for
     * repeated calling of this method with the same recognized root the method has to
     * return the same instance of BinaryForSourceQuery.Result.<br>
     * The typical implemantation of the findBinaryRoots contains 3 steps:
     * <ol>
     * <li>Look into the cache if there is already a result for the root, if so return it</li>
     * <li>Check if the sourceRoot is recognized, if not return null</li>
     * <li>Create a new BinaryForSourceQuery.Result for the sourceRoot, put it into the cache
     * and return it.</li>
     * </ol>
     * </p>
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param sourceRoot the source path root
     * @return a result object encapsulating the answer or null if the sourceRoot is not recognized
     */
    public Result findBinaryRoots2(URL sourceRoot);

    public URL[] computeRoots(Result result);
    public boolean computePreferBinaries(Result result);
    public void computeChangeListener(Result result, boolean add, ChangeListener l);
    
}
