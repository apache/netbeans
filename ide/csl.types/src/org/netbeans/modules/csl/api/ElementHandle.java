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

package org.netbeans.modules.csl.api;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 * Based on org.netbeans.modules.gsfpath.api.source by Tomas Zezula
 *
 * @author Tor Norbye
 */
public interface ElementHandle {
    /** 
     * Return the FileObject associated with this handle, or null
     * if the file is unknown or in a parse tree (in which case the
     * file object is the same as the file object in the CompilationInfo
     * for the root of the parse tree.
     */
    @CheckForNull
    FileObject getFileObject();
    
    /**
     * The mime type associated with this element. This is typically
     * used to identify the type of element in embedded scenarios.
     */
    @CheckForNull
    String getMimeType();

    @NonNull
    String getName();

    @CheckForNull
    String getIn();

    @NonNull
    ElementKind getKind();

    @NonNull
    Set<Modifier> getModifiers();
    
    /** 
     * Tests if the handle has the same signature as the parameter.
     * @param handle to be checked
     * @return true if the handles refer to elements with the same signature
     */
    boolean signatureEquals (@NonNull final ElementHandle handle);

    OffsetRange getOffsetRange(@NonNull ParserResult result);

    /** 
     * A special handle which holds URL. Can be used to handle documentation
     * requests etc.
     */
    public static class UrlHandle implements ElementHandle {
        private String url;

        public UrlHandle(@NonNull String url) {
            this.url = url;
        }

        public FileObject getFileObject() {
            return null;
        }
        
        public String getMimeType() {
            return null;
        }

        public boolean signatureEquals(ElementHandle handle) {
            if (handle instanceof UrlHandle) {
                return url.equals(((UrlHandle)handle).url);
            }
            
            return false;
        }

        public OffsetRange getOffsetRange(@NonNull ParserResult result) {
            return null;
        }

        @NonNull
        public String getUrl() {
            return url;
        }

        public String getName() {
            return url;
        }

        public String getIn() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
    }
}
