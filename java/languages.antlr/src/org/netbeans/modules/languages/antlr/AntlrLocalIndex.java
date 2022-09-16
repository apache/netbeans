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
package org.netbeans.modules.languages.antlr;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.WeakHashMap;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * The NetBeans indexer support is more appropriate for large scale project
 * where parsing of all affected files would take a too long time. For antlr
 * imports are only possible from the current directory, so it can be expected
 * that the number of files that can be imported or need to be scanned is low.
 */
public class AntlrLocalIndex {
    private static final WeakHashMap<FileObject, Reference<CacheEntry>> CACHE = new WeakHashMap<>();

    public static AntlrParserResult getParserResult(FileObject fo) {
        AntlrParserResult result = null;
        Reference<CacheEntry> ceReference;
        synchronized (CACHE) {
            ceReference = CACHE.get(fo);
        }
        if(ceReference != null) {
            CacheEntry ce = ceReference.get();
            if(ce != null) {
                if(! fo.lastModified().after(ce.lastModified)) {
                    result = ce.parserResult;
                }
            }
        }

        if(result == null) {
            try {
                Date lastModified = fo.lastModified();
                AntlrParserResult[] parserResult = new AntlrParserResult[1];
                ParserManager.parse(Collections.singleton(Source.create(fo)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Result result = resultIterator.getParserResult();
                        if(result instanceof AntlrParserResult) {
                            parserResult[0] = (AntlrParserResult) result;
                        }
                    }
                });
                if(parserResult[0] != null) {
                    result = parserResult[0];
                    synchronized (CACHE) {
                        CacheEntry ce = new CacheEntry(lastModified, parserResult[0]);
                        CACHE.put(fo, new WeakReference<>(ce));
                    }
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return result;
    }

    private static class CacheEntry {
        public final Date lastModified;
        public final AntlrParserResult parserResult;

        public CacheEntry(Date lastModified, AntlrParserResult parserResult) {
            this.lastModified = lastModified;
            this.parserResult = parserResult;
        }
    }

}
