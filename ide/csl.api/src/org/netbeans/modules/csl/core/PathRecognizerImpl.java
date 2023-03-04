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

package org.netbeans.modules.csl.core;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;

/**
 *
 * @author vita
 */
public final class PathRecognizerImpl extends PathRecognizer {

    // ------------------------------------------------------------------------
    // PathRecognizer implementation
    // ------------------------------------------------------------------------

    @Override
    public Set<String> getSourcePathIds() {
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        return l != null ? l.getSourcePathIds() : null;
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        return l != null ? l.getBinaryLibraryPathIds() : null;
    }

    @Override
    public Set<String> getLibraryPathIds() {
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        return l != null ? l.getLibraryPathIds() : null;
    }

    @Override
    public Set<String> getMimeTypes() {
        return Collections.singleton(mimeType);
    }

    // ------------------------------------------------------------------------
    // Public implementation
    // ------------------------------------------------------------------------

    public static PathRecognizer createInstance(Map fileAttributes) {
        Object v = fileAttributes.get("mimeType"); //NOI18N
        if (v instanceof String) {
            return new PathRecognizerImpl((String) v);
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(PathRecognizerImpl.class.getName());

    private final String mimeType;

    /**
     * Use {@link #getInstance()} to get the cached instance of this class. This
     * constructor is public only for @ServiceProvider registration.
     */
    public PathRecognizerImpl(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return super.toString() + "[mimeType=" + mimeType; //NOI18N
    }

}
