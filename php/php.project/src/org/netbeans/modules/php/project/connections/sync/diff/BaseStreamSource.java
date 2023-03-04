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
package org.netbeans.modules.php.project.connections.sync.diff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Base class for stream source.
 */
abstract class BaseStreamSource extends StreamSource {

    protected static final Logger LOGGER = Logger.getLogger(BaseStreamSource.class.getName());

    private final String name;
    private final String mimeType;
    private final boolean remote;


    BaseStreamSource(String name, String mimeType, boolean remote) {
        this.name = name;
        this.mimeType = mimeType;
        this.remote = remote;
    }

    protected abstract Reader createReaderInternal() throws IOException;

    @Override
    public final String getName() {
        return name;
    }

    @NbBundle.Messages({
        "BaseStreamSource.title.local=Local Version",
        "BaseStreamSource.title.remote=Remote Version"
    })
    @Override
    public final String getTitle() {
        return remote ? Bundle.BaseStreamSource_title_remote() : Bundle.BaseStreamSource_title_local();
    }

    @Override
    public final String getMIMEType() {
        return mimeType;
    }

    @Override
    public final Reader createReader() throws IOException {
        if (!mimeType.startsWith("text/")) { // NOI18N
            LOGGER.log(Level.INFO, "No reader for non-text file; MIME type is {0}", mimeType);
            return null;
        }
        return createReaderInternal();
    }

    @Override
    public final Writer createWriter(Difference[] conflicts) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected static Reader createReader(FileObject fileObject) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(fileObject.getInputStream(), FileEncodingQuery.getEncoding(fileObject)));
    }

}
