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

package org.netbeans.modules.parsing.spi.indexing.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.impl.indexing.DeletedIndexable;
import org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.Util;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class IndexResult {

    private static final Logger LOG = Logger.getLogger(IndexResult.class.getName());
    
    private final org.netbeans.modules.parsing.lucene.support.IndexDocument spi;
    private final URL root;

    private volatile URL cachedUrl;
    private volatile FileObject cachedFile;

    IndexResult (final org.netbeans.modules.parsing.lucene.support.IndexDocument spi, final URL root) {
        assert spi != null;
        assert root != null;
        this.spi = spi;
        this.root = root;
    }

    public String getValue (final String key) {
        Parameters.notEmpty("key", key); //NOI18N
        return this.spi.getValue (key);
    }

    public String[] getValues (final String key) {
        Parameters.notEmpty("key", key); //NOI18N
        return this.spi.getValues (key);
    }

    public URL getUrl() {
        if (cachedUrl == null) {
            URL url = null;
            try {
                url = Util.resolveUrl(root, spi.getPrimaryKey(), false);
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, null, ex);
            }

            synchronized(this) {
                if (cachedUrl == null) {
                    cachedUrl = url;
                }
            }
        }
        return cachedUrl;
    }

    public FileObject getFile () {
        if (cachedFile == null) {
//            final String path = spi.getSourceName();
//            final FileObject rootFo = URLMapper.findFileObject(root);
//            FileObject resource = null;
//            if (rootFo != null) {
//                resource = rootFo.getFileObject(path);
//            }
            FileObject resource = null;
            final URL url = getUrl();
            if (url != null) {
                resource = URLMapper.findFileObject(url);
            }
            synchronized (this) {
                if (cachedFile == null) {
                    cachedFile = resource;
                }
            }
        }
        return cachedFile;
    }

    /**
     * @since 1.9
     */
    public String getRelativePath() {
        return spi.getPrimaryKey();
    }

    /**
     * @since 1.9
     */
    public URL getRoot() {
        return root;
    }

    /**
     * Gets <code>Indexable</code> for this result. The indexable returned is giong
     * to represent the file that was used for creating {@link IndexDocument} and indexed.
     *
     * <p class="nonnormative">
     * Please note that this file may no longer exist on the disk in which case
     * the returned <code>Indexable</code> is going to have limited capabilities.
     * For example you want's be able to retrieve its mimetype.
     *
     * @return The <code>Indexable</code> representing the file that was used for
     *   creating the index data represented by this <code>IndexResult</code>.
     *
     * @since 1.22
     */
    public Indexable getIndexable() {
        FileObject file = getFile();
        if (file != null) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                return SPIAccessor.getInstance().create(new FileObjectIndexable(rootFo, file));
            }
        }
        
        return SPIAccessor.getInstance().create(new DeletedIndexable(root, getRelativePath()));
    }
}
