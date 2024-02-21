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
package org.netbeans.modules.php.editor.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public final class QuerySupportFactory {

    private QuerySupportFactory() {
    }

    public static QuerySupport getDependent(FileObject source) {
        return get(QuerySupport.findDependentRoots(source, false));
    }

    public static QuerySupport get(final FileObject source) {
        return get(QuerySupport.findRoots(source,
                Collections.singleton(PhpSourcePath.SOURCE_CP),
                Arrays.asList(PhpSourcePath.BOOT_CP, PhpSourcePath.PROJECT_BOOT_CP),
                Collections.<String>emptySet()));
    }

    public static QuerySupport get(final ParserResult info) {
        return get(info.getSnapshot().getSource().getFileObject());
    }

    public static QuerySupport get(final Collection<FileObject> roots) {
        try {
            return QuerySupport.forRoots(PHPIndexer.Factory.NAME,
                    PHPIndexer.Factory.VERSION,
                    roots.toArray(new FileObject[0]));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
