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

package org.netbeans.modules.java.source.parsing;

import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author Tomas Zezula
 */
public final class NewComilerTask extends ClasspathInfoTask {

    private CompilationController result;
    private long timestamp;
    private final int position;

    public NewComilerTask (final ClasspathInfo cpInfo, final int position, final CompilationController last, long timestamp) {
        super (cpInfo);
        this.position = position;
        this.result = last;
        this.timestamp = timestamp;
    }

    @Override
    public void run(@NonNull ResultIterator resultIterator) throws Exception {
        final Snapshot snapshot = resultIterator.getSnapshot();
        if (!JavacParser.MIME_TYPE.equals(snapshot.getMimeType())) {
            resultIterator = findEmbeddedJava(resultIterator);
        }
        if (resultIterator != null) {
            resultIterator.getParserResult();   //getParserResult calls setCompilationController
        }
    }

    @CheckForNull
    private ResultIterator findEmbeddedJava (@NonNull final ResultIterator theMess) throws ParseException {
        final Collection<Embedding> todo = new LinkedList<>();
        //BFS should perform better than DFS in this dark.
        for (Embedding embedding : theMess.getEmbeddings()) {
            if (position != -1 && !embedding.containsOriginalOffset(position)) {
                continue;
            }
            if (JavacParser.MIME_TYPE.equals(embedding.getMimeType())) {
                return theMess.getResultIterator(embedding);
            } else {
                todo.add(embedding);
            }
        }
        for (Embedding embedding : todo) {
            final ResultIterator res  = findEmbeddedJava(theMess.getResultIterator(embedding));
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void setCompilationController (
            @NonNull final CompilationController result,
            final long timestamp) {
        assert result != null;
        this.result = result;
        this.timestamp = timestamp;
    }

    public CompilationController getCompilationController () {
        return result;
    }

    public long getTimeStamp () {
        return this.timestamp;
    }

}
