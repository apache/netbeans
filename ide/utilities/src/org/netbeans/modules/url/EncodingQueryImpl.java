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

package org.netbeans.modules.url;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Implementation of {@code FileEncodingQueryImplementation} that returns
 * encoding UTF-8 for every file.
 *
 * @author  Marian Petras
 */
final class EncodingQueryImpl extends FileEncodingQueryImplementation {

    private final Charset charsetUtf8;

    EncodingQueryImpl() {
        charsetUtf8 = StandardCharsets.UTF_8;
    }

    @Override
    public Charset getEncoding(FileObject file) {
        return charsetUtf8;
    }

}
