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
package org.netbeans.modules.nativeexecution.support.filesearch;

import org.netbeans.modules.nativeexecution.support.*;
import java.util.Collection;
import java.util.logging.Level;
import org.openide.util.Lookup;

public final class FileSearchSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final TasksCachedProcessor<FileSearchParams, String> processor =
            new TasksCachedProcessor<>(
            new Searcher(), false);

    public final String searchFile(final FileSearchParams fileSearchParams) throws InterruptedException {
        // TODO: should we check for file presence if result is taken from the cache?
        return processor.compute(fileSearchParams);
    }

    private static final class Searcher implements Computable<FileSearchParams, String> {

        public String compute(FileSearchParams fileSearchParams) throws InterruptedException {
            final Collection<? extends FileSearcher> searchers = Lookup.getDefault().lookupAll(FileSearcher.class);
            String result = null;

            for (FileSearcher searcher : searchers) {
                result = searcher.searchFile(fileSearchParams);
                if (result != null) {
                    return result;
                }
            }

            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "File '" + fileSearchParams.getFilename() + "' not found. {" + fileSearchParams.toString() + "}"); // NOI18N
            }

            return null;
        }
    }
}
