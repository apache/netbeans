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
package org.netbeans.modules.languages.antlr;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lkishalmi
 */
public abstract class AntlrParser extends org.netbeans.modules.parsing.spi.Parser {

    AntlrParserResult lastResult;

    /*
     * The NetBeans indexer support is more appropriate for large scale project
     * where parsing of all affected files would take a too long time. For ANTLR
     * imports are only possible from the current directory, so it can be
     * expected that the number of files that can be imported or need to be
     * scanned is low.
     */
    private static final WeakHashMap<FileObject, Reference<AntlrParserResult>> CACHE = new WeakHashMap<>();

    protected abstract AntlrParserResult<?> createParserResult(Snapshot snapshot);

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        AntlrParserResult<?> parserResult = createParserResult(snapshot);

        AntlrParserResult<?> parsed = parserResult.get();
        cacheResult(snapshot.getSource().getFileObject(), parsed);
        lastResult = parsed;
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        assert lastResult != null;
        return lastResult;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    private static void cacheResult(FileObject fo, AntlrParserResult<?> result) {
        synchronized (CACHE) {
            CACHE.put(fo, new WeakReference<>(result));
        }
    }

    public static AntlrParserResult<?> getParserResult(FileObject fo) {
        AntlrParserResult<?> result = null;
        java.lang.ref.Reference<AntlrParserResult> ceReference;
        synchronized (CACHE) {
            ceReference = CACHE.get(fo);
        }
        if (ceReference != null) {
            result = ceReference.get();
        }

        if (result == null) {
            try {
                AntlrParserResult<?>[] parserResult = new AntlrParserResult<?>[1];
                ParserManager.parse(Collections.singleton(Source.create(fo)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        org.netbeans.modules.parsing.spi.Parser.Result result = resultIterator.getParserResult();
                        if (result instanceof AntlrParserResult) {
                            parserResult[0] = (AntlrParserResult) result;
                        }
                    }
                });
                if (parserResult[0] != null) {
                    result = parserResult[0];
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return result;
    }

}
