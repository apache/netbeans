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
package org.netbeans.modules.java.source.indexing;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePrefetcher implements Iterator<CompileTuple>, /*Auto*/Closeable {
       
    private final Iterator<? extends CompileTuple> iterator;
    //@NotThreadSafe
    private boolean active;

    private SourcePrefetcher(
            @NonNull final Iterator<? extends CompileTuple> iterator) {
        assert iterator != null;
        this.iterator = iterator;
    }
    
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    @Override
    @CheckForNull
    public CompileTuple next() {
        if (active) {
            throw new IllegalStateException("Call remove to free resources");   //NOI18N
        }
        final CompileTuple res = iterator.next();
        active = true;
        return res;
    }

    @Override
    public void remove() {
        if (!active) {
            throw new IllegalStateException("Call next before remove");   //NOI18N
        }
        try {
            iterator.remove();
        } finally {
            active = false;
        }
    }

    @Override
    public void close() throws IOException {
        if (iterator instanceof Closeable) {
            ((Closeable)iterator).close();
        }
    }
    
    
    public static SourcePrefetcher create(
            @NonNull final Collection<? extends CompileTuple> files,
            @NonNull final SuspendStatus suspendStatus) {
        return new SourcePrefetcher(JavaIndexerWorker.getCompileTupleIterator(files, suspendStatus));
    }    
}
