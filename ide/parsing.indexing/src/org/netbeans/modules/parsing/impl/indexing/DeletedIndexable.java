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

package org.netbeans.modules.parsing.impl.indexing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public final class DeletedIndexable implements IndexableImpl, FileObjectProvider {

    private static final Logger LOG = Logger.getLogger(DeletedIndexable.class.getName());

    private final URL root;
    private final String relativePath;

    public DeletedIndexable (final URL root, final String relativePath) {
        assert root != null : "root must not be null"; //NOI18N
        assert relativePath != null : "relativePath must not be null"; //NOI18N
        assert relativePath.length() == 0 || relativePath.charAt(0) != '/'; //NOI18N
        this.root = root;
        this.relativePath = relativePath;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public URL getURL() {
        try {
            
            return Util.resolveUrl(
                    root,
                    relativePath,
                    relativePath.isEmpty() || relativePath.charAt(relativePath.length()-1) == '/'?
                        null : Boolean.FALSE);
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    @Override
    public String getMimeType() {
        throw new UnsupportedOperationException("Mimetype related operations are not supported by DeletedIndexable"); //NOI18N
    }

    @Override
    public boolean isTypeOf(String mimeType) {
        throw new UnsupportedOperationException("Mimetype related operations are not supported by DeletedIndexable"); //NOI18N
    }

    @Override
    public FileObject getFileObject() {
        return null;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_BLOCKING_METHODS_ON_URL",
    justification="URLs have never host part")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeletedIndexable other = (DeletedIndexable) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        if (this.relativePath != other.relativePath && (this.relativePath == null || !this.relativePath.equals(other.relativePath))) {
            return false;
        }
        return true;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_BLOCKING_METHODS_ON_URL",
    justification="URLs have never host part")
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.root != null ? this.root.hashCode() : 0);
        hash = 83 * hash + (this.relativePath != null ? this.relativePath.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "DeletedIndexable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + getURL() + "]"; //NOI18N
    }

}
