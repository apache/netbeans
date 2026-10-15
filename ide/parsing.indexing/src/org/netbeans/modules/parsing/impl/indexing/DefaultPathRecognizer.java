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

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;

/**
 *
 * @author vita
 */
public final class DefaultPathRecognizer extends PathRecognizer {

    // ------------------------------------------------------------------------
    // PathRecognizer implementation
    // ------------------------------------------------------------------------

    @Override
    public Set<String> getSourcePathIds() {
        return sourcePathIds;
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return binaryLibraryPathIds;
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return libraryPathIds;
    }

    @Override
    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    // ------------------------------------------------------------------------
    // Public implementation
    // ------------------------------------------------------------------------

    public static PathRecognizer createInstance(Map fileAttributes) {
        
        // path ids
        Set<String> sourcePathIds = readIdsAttribute(fileAttributes, "sourcePathIds"); //NOI18N
        Set<String> libraryPathIds = readIdsAttribute(fileAttributes, "libraryPathIds"); //NOI18N
        Set<String> binaryLibraryPathIds = readIdsAttribute(fileAttributes, "binaryLibraryPathIds"); //NOI18N

        // mime types
        Set<String> mimeTypes = new HashSet<String>();
        Object mts = fileAttributes.get("mimeTypes"); //NOI18N
        if (mts instanceof String) {
            String [] arr = ((String) mts).split(","); //NOI18N
            for(String mt : arr) {
                mt = mt.trim();
                if (mt.length() > 0 && MimePath.validate(mt)) {
                    mimeTypes.add(mt);
                } else {
                    LOG.log(Level.WARNING, "Invalid mimetype {0}, ignoring.", mt); //NOI18N
                }
            }
        }

        return new DefaultPathRecognizer(sourcePathIds, libraryPathIds, binaryLibraryPathIds, Collections.unmodifiableSet(mimeTypes));
    }

    @Override
    public String toString() {
        return super.toString()
                + "[sourcePathIds=" + sourcePathIds //NOI18N
                + ", libraryPathIds=" + libraryPathIds //NOI18N
                + ", binaryLibraryPathIds=" + binaryLibraryPathIds //NOI18N
                + ", mimeTypes=" + mimeTypes; //NOI18N
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultPathRecognizer.class.getName());

    private final Set<String> sourcePathIds;
    private final Set<String> libraryPathIds;
    private final Set<String> binaryLibraryPathIds;
    private final Set<String> mimeTypes;

    private DefaultPathRecognizer(Set<String> sourcePathIds, Set<String> libraryPathIds, Set<String> binaryLibraryPathIds, Set<String> mimeTypes) {
        this.sourcePathIds = sourcePathIds;
        this.libraryPathIds = libraryPathIds;
        this.binaryLibraryPathIds = binaryLibraryPathIds;
        this.mimeTypes = mimeTypes;
    }

    private static Set<String> readIdsAttribute(Map<String, ?> fileAttributes, String attributeName) {
        Set<String> ids = new HashSet<>();
        
        Object attributeValue = fileAttributes.get(attributeName); //NOI18N
        if (attributeValue instanceof String) {
            String [] varr = ((String) attributeValue).split(","); //NOI18N
            for(String v : varr) {
                v = v.trim();
                if (v.equals("ANY")) { //NOI18N
                    ids = null;
                    break;
                } else if (v.length() > 0) {
                    ids.add(v);
                } else {
                    LOG.log(Level.WARNING, "Empty IDs are not alowed in {0} attribute, ignoring.", attributeName); //NOI18N
                }
            }
        } else {
            if (attributeValue != null) {
                LOG.log(Level.WARNING, "Invalid {0} attribute value, expecting java.lang.String, but got {1}, {2}", //NOI18N
                        new Object [] { attributeName, attributeValue, attributeValue == null ? null : attributeValue.getClass()});
            }
            
            ids = Collections.<String>emptySet();
        }

        return ids == null ? null : Collections.unmodifiableSet(ids);
    }
}
