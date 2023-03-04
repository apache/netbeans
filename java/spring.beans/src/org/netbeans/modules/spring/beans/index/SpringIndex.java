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
package org.netbeans.modules.spring.beans.index;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author alex
 */
public class SpringIndex {

    private final FileObject[] binaryRoots;

    public SpringIndex(FileObject fo) {
        this.binaryRoots = (ClassPath.getClassPath(fo, ClassPath.EXECUTE).getRoots());
    }

    public SpringIndex(ClassPath cp) {
        this.binaryRoots = cp.getRoots();
    }
    
    private QuerySupport createBinaryIndex() throws IOException {
        return QuerySupport.forRoots(SpringBinaryIndexer.INDEXER_NAME, SpringBinaryIndexer.INDEX_VERSION, binaryRoots);
    }

    public Map<String, FileObject> getAllSpringLibraryDescriptors() {
        Map<String, FileObject> map = new HashMap<String, FileObject>();
        try {
            Collection<? extends IndexResult> results = createBinaryIndex().query(
                    SpringBinaryIndexer.LIBRARY_MARK_KEY,
                    "true", //NOI18N
                    QuerySupport.Kind.EXACT,
                    SpringBinaryIndexer.LIBRARY_MARK_KEY, SpringBinaryIndexer.NAMESPACE_MARK_KEY);
            for (IndexResult result : results) {
                FileObject file = result.getFile(); //expensive? use result.getRelativePath?
                if (file != null) {
                    map.put(result.getValue(SpringBinaryIndexer.NAMESPACE_MARK_KEY), file);
                }

            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;

    }
}
