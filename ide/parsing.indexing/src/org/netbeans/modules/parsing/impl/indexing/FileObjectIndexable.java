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
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class FileObjectIndexable implements IndexableImpl, FileObjectProvider {

    private static final Logger LOG = Logger.getLogger(FileObjectIndexable.class.getName());

    private final FileObject root;
    private final String relativePath;

    private Object url;
    private String mimeType;
    private FileObject file;

    public FileObjectIndexable (FileObject root, FileObject file) {
        this(root, FileUtil.getRelativePath(root, file));
        this.file = file;
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "File: {0}", FileUtil.getFileDisplayName(file));  //NOI18N
        }
    }

    public FileObjectIndexable (FileObject root, String relativePath) {
        Parameters.notNull("root", root); //NOI18N
        Parameters.notNull("relativePath", relativePath); //NOI18N
        this.root = root;
        this.relativePath = relativePath;
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Root: {0}", FileUtil.getFileDisplayName(root));  //NOI18N
            LOG.log(Level.FINEST, "Path: {0}", relativePath);                       //NOI8N
        }
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public URL getURL() {
        if (url == null) {
            try {
                FileObject f = getFileObject();
                if (f != null) {
                    url = f.toURL();
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(
                            Level.FINEST,
                            "URL from existing FileObject: {0} = {1}",  //NOI18N
                            new Object[] {
                                FileUtil.getFileDisplayName(f),
                                url
                            });
                    }
                } else {
                    url = Util.resolveUrl(root.toURL(), relativePath, false);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(
                            Level.FINEST,
                            "URL from non existing FileObject root: {0} ({1}), relative path: {2} = {3}",  //NOI18N
                            new Object[] {
                                FileUtil.getFileDisplayName(root),
                                root.toURL(),
                                relativePath,
                                url
                            });
                    }
                }
            } catch (MalformedURLException ex) {
                url = ex;
            }
        }

        return url instanceof URL ? (URL) url : null;
    }

    @Override
    public String getMimeType() {
        return mimeType == null ? "content/unknown" : mimeType;
    }

    @Override
    public boolean isTypeOf(String mimeType) {
        Parameters.notNull("mimeType", mimeType); //NOI18N
        if (this.mimeType == null) {
            FileObject f = getFileObject();
            if (f != null) {
                String mt = FileUtil.getMIMEType(f, mimeType);
                if (mt != null && !mt.equals("content/unknown")) {
                    this.mimeType = mt;
                }
            }
        }
        return this.mimeType == null ? false : this.mimeType.equals(mimeType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileObjectIndexable other = (FileObjectIndexable) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        if (this.relativePath != other.relativePath && (this.relativePath == null || !this.relativePath.equals(other.relativePath))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.relativePath != null ? this.relativePath.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FileObjectIndexable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + root.toURL() + "/" + getRelativePath() + "]"; //NOI18N
    }

    @Override
    public FileObject getFileObject() {
        if (file == null) {
            file = root.getFileObject(relativePath);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(
                    Level.FINEST,
                    "File: {0} in Root: {1}", //NOI18N
                    new Object[] {
                        FileUtil.getFileDisplayName(file),
                        FileUtil.getFileDisplayName(root)
                    });
            }
        }
        return file != null && file.isValid() ? file : null;
    }    
}
