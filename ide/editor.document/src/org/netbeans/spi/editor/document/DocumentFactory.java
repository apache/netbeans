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

package org.netbeans.spi.editor.document;

import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Interface to create documents. Implementations should be registered in the
 * {@link MimeLookup} for an appropriate MIME type. The system may provide a default
 * implementation for all MIME types.
 * 
 * @author sdedic
 */
public interface DocumentFactory {
    /**
     * Creates a document for the given mime type.
     * @param mimeType the MIME type
     * @return document instance or {@code null}
     */
    @CheckForNull
    public Document createDocument(@NonNull String mimeType);

    /**
     * Returns a {@link Document} for the given {@link FileObject}.
     * @param file the {@link FileObject} to create {@link Document} for
     * @return the document instance or {@code null}
     */
    @CheckForNull
    public Document getDocument(@NonNull FileObject file);

    /**
     * Returns a {@link FileObject} for given {@link Document}.
     * @param document the {@link Document} to find {@link FileObject} for
     * @return the {@link FileObject} or {@code null}
     */
    @CheckForNull
    public FileObject getFileObject(@NonNull Document document);

}
