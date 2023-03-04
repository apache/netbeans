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

package org.netbeans.api.queries;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;

// XXX perhaps should be in the Filesystems API instead of here?

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * Likely to be of use only to a VCS filesystem.
 * <p>
 * This query can be considered to obsolete {@link org.openide.filesystems.FileObject#setImportant}.
 * Unlike that method, the information is pulled by the VCS filesystem on
 * demand, which may be more reliable than ensuring that the information
 * is pushed by a project type (or other implementor) eagerly.
 * @see SharabilityQueryImplementation2
 * @author Jesse Glick
 */
@SuppressWarnings("deprecation")
public final class SharabilityQuery {
    
    private static final Lookup.Result<org.netbeans.spi.queries.SharabilityQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(org.netbeans.spi.queries.SharabilityQueryImplementation.class);

    private static final Lookup.Result<SharabilityQueryImplementation2> implementations2 =
        Lookup.getDefault().lookupResult(SharabilityQueryImplementation2.class);
    
    private static final Logger LOG = Logger.getLogger(SharabilityQuery.class.getName());

    /**
     * Constant indicating that nothing is known about whether a given
     * file should be considered sharable or not.
     * A client should therefore behave in the safest way it can.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#UNKNOWN} instead.
     */
    @Deprecated public static final int UNKNOWN = 0;
    
    /**
     * Constant indicating that the file or directory is sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * sharable.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#SHARABLE} instead.
     */
    @Deprecated public static final int SHARABLE = 1;
    
    /**
     * Constant indicating that the file or directory is not sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * not sharable.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#NOT_SHARABLE} instead.
     */
    @Deprecated public static final int NOT_SHARABLE = 2;
    
    /**
     * Constant indicating that a directory is sharable but files and
     * directories recursively contained in it may or may not be sharable.
     * A client interested in children of this directory should explicitly
     * ask about each in turn.
     * @deprecated Use {@link org.netbeans.api.queries.SharabilityQuery.Sharability#MIXED} instead.
     */
    @Deprecated public static final int MIXED = 3;

    /**
     * Sharability constants.
     * @since 1.27
     */
    public enum Sharability {
        /**
        * Constant indicating that nothing is known about whether a given
        * file should be considered sharable or not.
        * A client should therefore behave in the safest way it can.
        */
        UNKNOWN,

        /**
        * Constant indicating that the file or directory is sharable.
        * In the case of a directory, this means that all files and
        * directories recursively contained in this directory are also
        * sharable.
        */
        SHARABLE,

        /**
        * Constant indicating that the file or directory is not sharable.
        * In the case of a directory, this means that all files and
        * directories recursively contained in this directory are also
        * not sharable.
        */
        NOT_SHARABLE,

        /**
        * Constant indicating that a directory is sharable but files and
        * directories recursively contained in it may or may not be sharable.
        * A client interested in children of this directory should explicitly
        * ask about each in turn.
        */
        MIXED;
    }
    
    private SharabilityQuery() {}
    
    /**
     * Check whether an existing file is sharable.
     * @param file a file or directory (may or may not already exist); should be {@linkplain FileUtil#normalizeFile normalized}
     * @return an answer or {@code UNKNOWN}
     * @deprecated Use {@link #getSharability(java.net.URI)} instead.
     */
    @Deprecated public static int getSharability(File file) {
        Parameters.notNull("file", file);
        boolean asserts = false;
        assert asserts = true;
        if (asserts && !BaseUtilities.isMac()) {
            File normFile = FileUtil.normalizeFile(file);
            if (!file.equals(normFile)) {
                throw new IllegalArgumentException("Must pass a normalized file: " + file + " vs. " + normFile);
            }
        }
        URI uri = null;
        for (SharabilityQueryImplementation2 sqi : implementations2.allInstances()) {
            if (uri == null) {
                uri = BaseUtilities.toURI(file);
            }
            Sharability x = sqi.getSharability(uri);
            if (x != Sharability.UNKNOWN) {
                return x.ordinal();
            }
        }
        for (org.netbeans.spi.queries.SharabilityQueryImplementation sqi : implementations.allInstances()) {
            int x = sqi.getSharability(file);
            if (x != UNKNOWN) {
                return x;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Check whether an existing file is sharable.
     * @param uri a file or directory (may or may not already exist); should be normalized.
     * @return an answer or {@code UNKNOWN}
     * @since 1.27
     */
    public static Sharability getSharability(URI uri) {
        Parameters.notNull("uri", uri);
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            URI normUri = BaseUtilities.normalizeURI(uri);
            if (!uri.equals(normUri)) {
                throw new IllegalArgumentException("Must pass a normalized URI: " + uri + " vs. " + normUri);
            }
        }
        for (SharabilityQueryImplementation2 sqi : implementations2.allInstances()) {
            Sharability x = sqi.getSharability(uri);
            if (x != Sharability.UNKNOWN) {
                return x;
            }
        }
        if ("file".equals(uri.getScheme())) { // NOI18N
            File file = FileUtil.normalizeFile(BaseUtilities.toFile(uri));
            for (org.netbeans.spi.queries.SharabilityQueryImplementation sqi : implementations.allInstances()) {
                int x = sqi.getSharability(file);
                if (x != UNKNOWN) {
                    return Sharability.values()[x];
                }
            }
        }
        return Sharability.UNKNOWN;
    }
    
    /**
     * Check whether an existing file is sharable.
     * @param fo a file or directory; should be normalized.
     * @return an answer or {@code UNKNOWN}
     * @since 1.27
     */
    public static Sharability getSharability(FileObject fo) {
        return getSharability(fo.toURI());
    }
}
