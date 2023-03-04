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

package org.netbeans.spi.queries;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.netbeans.modules.queries.UnknownEncoding;
import org.openide.filesystems.FileObject;

/**
 * Information about encoding of a file.
 * <p>
 * A default implementations are registered by the
 * <code>org.netbeans.modules.projectapi</code> module which firstly looks up the
 * implementation of this interface in the <code>DataObject</code> lookup. When
 * available it delegates to it. When the implementation isn't available in the
 * <code>DataObject</code> lookup or it returns null it tries to find a
 * project corresponding to the file and checks whether that project has an
 * implementation of this interface in its lookup. If so, it delegates to
 * that implementation. Therefore it is not generally necessary
 * for a project type provider nor data loader to register its own global implementation of
 * this query.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Typical implementation returns a {@link Charset} for recognized file. The
 * implementation which needs to analyze the content of the file (XML, HTML) 
 * should implement a subclass of the {@link Charset} and provide own {@link CharsetEncoder}
 * end {@link CharsetDecoder} which buffer the input up to 4 KB and either delegate
 * to the correct {@link Charset} when the encoding is found in the buffer or signal 
 * that they are not able to process the file and other {@link Charset} should be used
 * by calling the {@link FileEncodingQueryImplementation#throwUnknownEncoding} method.
 * </p>
 * </div>
 * @since org.netbeans.modules.queries/1 1.9
 * @author Tomas Zezula
 */
public abstract class FileEncodingQueryImplementation {
    
    /**
     * Returns encoding of a given file or folder. For folders the default encoding for the whole subtree
     * shall be returned (if known).
     * @param file to find an encoding for
     * @return encoding which should be used for given file
     * or null when nothing is known about the file encoding.
     */
    public abstract Charset getEncoding (FileObject file);
    
    /**
     * By calling this method the {@link CharsetEncoder} and 
     * {@link CharsetDecoder} signal that they are not able to handle
     * the document and the other {@link Charset} should be used. This
     * method may be called during processing the first 4 KB of data and
     * before any output has been written.
     */
    protected static void throwUnknownEncoding() {
        throw new UnknownEncoding ();
    }
    
}
