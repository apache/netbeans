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
package org.netbeans.modules.profiler.oql.engine.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.modules.profiler.oql.engine.api.impl.OQLEngineImpl;
import org.netbeans.modules.profiler.oql.engine.api.impl.Snapshot;

/**
 * This is Object Query Language Interpreter
 *
 * @author J. Bachorik
 */
public final class OQLEngine {
    private static final Logger LOGGER = Logger.getLogger(OQLEngine.class.getName());
    private final OQLEngineImpl delegate;
    private final Heap heap;
    
    /**
     * This represents a parsed OQL query
     *
     * @author A. Sundararajan
     */
    public abstract static class OQLQuery {

    }

    /**
     * This visitor is supplied to OQLEngine.executeQuery
     * to receive result set objects one by one.
     *
     * @author A. Sundararajan
     * @author J. Bachorik
     */
    public static interface ObjectVisitor {
        // return true to terminate the result set callback earlier
        public boolean visit(Object o);

        public static final ObjectVisitor DEFAULT = new ObjectVisitor() {

            public boolean visit(Object o) {
                if (o != null && LOGGER.isLoggable(Level.FINEST)) LOGGER.finest(o.toString());

                return true; // prevent calling "visit" for the rest of the result set
            }
        };
    }

    // check OQL is supported or not before creating OQLEngine 
    public static boolean isOQLSupported() {
        return OQLEngineImpl.isOQLSupported();
    }

    public OQLEngine(Heap heap) {
        delegate = new OQLEngineImpl(new Snapshot(heap, this));
        this.heap = heap;
    }

    public Heap getHeap() {
        return heap;
    }

    /**
    Query is of the form

    select &lt;java script code to select&gt;
    [ from [instanceof] &lt;class name&gt; [&lt;identifier&gt;]
    [ where &lt;java script boolean expression&gt; ]
    ]
     */
    public void executeQuery(String query, ObjectVisitor visitor)
            throws OQLException {
        delegate.executeQuery(query, visitor);
    }

    public OQLQuery parseQuery(String query) throws OQLException {
        return delegate.parseQuery(query);
    }

    public void cancelQuery() throws OQLException {
        delegate.cancelQuery();
    }

    public Object unwrapJavaObject(Object object) {
        return delegate.unwrapJavaObject(object);
    }

    public Object unwrapJavaObject(Object object, boolean tryAssociativeArray) {
        return delegate.unwrapJavaObject(object, tryAssociativeArray);
    }

    public boolean isCancelled() {
        return delegate.isCancelled();
    }
}
