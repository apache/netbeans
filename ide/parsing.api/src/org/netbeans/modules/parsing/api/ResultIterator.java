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

package org.netbeans.modules.parsing.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import org.netbeans.modules.parsing.impl.ParserAccessor;
import org.netbeans.modules.parsing.impl.ResultIteratorAccessor;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceCache;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;


/**
 * ResultIterator allows to iterate tree of embedded blocks of sources, and 
 * request parse results on different levels. You can force parsing of all 
 * blocks of embedded {@link Source}s, or you can find your favourite one and
 * skip parsing of rest of them. In some situations you can even parse embedded 
 * language without parsing of surrounding block.
 * 
 * @author Jan Jancura
 */
public final class ResultIterator {
    
    private SourceCache     sourceCache;
    private UserTask        task;
    private Parser.Result   result;
    private Parser          parser;
    
    static {
        ResultIteratorAccessor.setINSTANCE(new MyAccessor());
    }

    ResultIterator (
        Result              result
    ) {
        this.result = result;
    }

    ResultIterator (
        SourceCache         sourceCache,
        UserTask            task
    ) {
        this.sourceCache = sourceCache;
        this.task = task;
    }

    ResultIterator (
        SourceCache         sourceCache,
        Parser              parser,
        UserTask            task
    ) {
        this.sourceCache = sourceCache;
        this.parser = parser;
        this.task = task;
    }
    
    public Snapshot getSnapshot () {
        if (sourceCache != null)
            return sourceCache.getSnapshot ();
        return result.getSnapshot ();
    }
    
    private void invalidate () {
        if (result != null) {
            ParserAccessor.getINSTANCE ().invalidate (result);
            result = null;
            parser = null;
        }
        for (Iterator<ResultIterator> it = children.iterator(); it.hasNext();) {
            final ResultIterator child = it.next();
            it.remove();
            child.invalidate();            
        }
    }
    
    /**
     * Returns parse {@link Result} for current source or <code>null</code>.
     * 
     * @return              parse {@link Result} for current source or <code>null</code>.
     */
    public Result getParserResult () throws ParseException {
        if (result == null) {
            if (parser != null) {
                SourceModificationEvent event = SourceAccessor.getINSTANCE ().getSourceModificationEvent (getSnapshot ().getSource ());
                TaskProcessor.callParse(parser, getSnapshot (), task, event);
                result = TaskProcessor.callGetResult(parser, task);
            } else
                result = sourceCache.getResult (task);
        }
        return result;
    }
    
    /**
     * Returns parse {@link Result} for deepest embedding on given offset or <code>null</code>.
     * 
     * @return              parse {@link Result} for current source or <code>null</code>.
     */
    public Result getParserResult (int offset) throws ParseException {
        for (Embedding embedding : getEmbeddings ())
            if (embedding.containsOriginalOffset (offset))
                return getResultIterator (embedding).getParserResult (offset);
        return getParserResult ();
    }
    
    /**
     * Allows iterate all embedded sources.
     * 
     * @return              {@link Iterator} of all embeddings.
     */
    public Iterable<Embedding> getEmbeddings () {
        if (sourceCache == null)
            return Collections.<Embedding> emptyList ();
        return sourceCache.getAllEmbeddings ();
    }
    
    //@NotThreadSafe    //accessed under parser lock
    private final List<ResultIterator> children = new LinkedList<ResultIterator>();
    //@NotThreadSafe    //accessed under parser lock
    private Map<Embedding,ResultIterator>
                            embeddingToResultIterator = new HashMap<Embedding,ResultIterator> ();
    
    /**
     * Returns {@link ResultIterator} for one {@link Embedding}.
     * 
     * @param embedding     A embedding.
     * @return              {@link ResultIterator} for one {@link Embedding}.
     */
    public ResultIterator getResultIterator (Embedding embedding) {
        if (sourceCache == null)
            return null;
        ResultIterator resultIterator = embeddingToResultIterator.get (embedding);
        if (resultIterator == null) {
            SourceCache cache = sourceCache.getCache (embedding);
            resultIterator = new ResultIterator (
                cache,
                task
            );
            embeddingToResultIterator.put(embedding, resultIterator);
            children.add (resultIterator);
        }
        return resultIterator;
    }
    
    private static class MyAccessor extends ResultIteratorAccessor {

        @Override
        public void invalidate(final ResultIterator resultIterator) {
            assert resultIterator != null;
            resultIterator.invalidate();
        }
        
    }
    
}
