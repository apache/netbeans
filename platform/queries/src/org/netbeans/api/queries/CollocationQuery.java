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
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.BaseUtilities;

/**
 * Find out whether some files logically belong in one directory tree,
 * for example as part of a VCS checkout.
 * @see CollocationQueryImplementation2
 * @author Jesse Glick
 */
@SuppressWarnings("deprecation")
public final class CollocationQuery {

    private static final Lookup.Result<org.netbeans.spi.queries.CollocationQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(org.netbeans.spi.queries.CollocationQueryImplementation.class);
    private static final Lookup.Result<CollocationQueryImplementation2> implementations2 =
        Lookup.getDefault().lookupResult(CollocationQueryImplementation2.class);
    
    private CollocationQuery() {}
    
    /**
     * Check whether two files are logically part of one directory tree.
     * For example, if both files are stored in CVS, with the same server
     * (<code>CVSROOT</code>) they might be considered collocated.
     * If nothing is known about them, return false.
     * @param file1 one file
     * @param file2 another file
     * @return true if they are probably part of one logical tree
     * @deprecated Use {@link #areCollocated(java.net.URI, java.net.URI)} instead.
     */
    @Deprecated public static boolean areCollocated(File file1, File file2) {
        if (!file1.equals(FileUtil.normalizeFile(file1))) {
            throw new IllegalArgumentException("Parameter file1 was not "+  // NOI18N
                "normalized. Was "+file1+" instead of "+FileUtil.normalizeFile(file1));  // NOI18N
        }
        if (!file2.equals(FileUtil.normalizeFile(file2))) {
            throw new IllegalArgumentException("Parameter file2 was not "+  // NOI18N
                "normalized. Was "+file2+" instead of "+FileUtil.normalizeFile(file2));  // NOI18N
        }
        URI uri1 = BaseUtilities.toURI(file1);
        URI uri2 = BaseUtilities.toURI(file2);
        for (CollocationQueryImplementation2 cqi : implementations2.allInstances()) {
            if (cqi.areCollocated(uri1, uri2)) {
                return true;
            }
        }
        for (org.netbeans.spi.queries.CollocationQueryImplementation cqi : implementations.allInstances()) {
            if (cqi.areCollocated(file1, file2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether two files are logically part of one directory tree.
     * For example, if both files are stored in CVS, with the same server
     * (<code>CVSROOT</code>) they might be considered collocated.
     * If nothing is known about them, return false.
     * @param file1 one file
     * @param file2 another file
     * @return true if they are probably part of one logical tree
     * @since 1.27
     */
    public static boolean areCollocated(URI file1, URI file2) {
        if (!file1.equals(BaseUtilities.normalizeURI(file1))) {
            throw new IllegalArgumentException("Parameter file1 was not "+  // NOI18N
                "normalized. Was "+file1+" instead of "+BaseUtilities.normalizeURI(file1));  // NOI18N
        }
        if (!file2.equals(BaseUtilities.normalizeURI(file2))) {
            throw new IllegalArgumentException("Parameter file2 was not "+  // NOI18N
                "normalized. Was "+file2+" instead of "+BaseUtilities.normalizeURI(file2));  // NOI18N
        }
        for (CollocationQueryImplementation2 cqi : implementations2.allInstances()) {
            if (cqi.areCollocated(file1, file2)) {
                return true;
            }
        }
        if ("file".equals(file1.getScheme()) && "file".equals(file2.getScheme())) { // NOI18N
            File f1 = FileUtil.normalizeFile(BaseUtilities.toFile(file1));
            File f2 = FileUtil.normalizeFile(BaseUtilities.toFile(file2));
            for (org.netbeans.spi.queries.CollocationQueryImplementation cqi : implementations.allInstances()) {
                if (cqi.areCollocated(f1, f2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Find a root of a logical tree containing this file, if any.
     * @param file a file on disk
     * @return an ancestor directory which is the root of a logical tree,
     *         if any (else null)
     * @deprecated Use {@link #findRoot(java.net.URI)} instead.
     */
    @Deprecated public static File findRoot(File file) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        URI uri = BaseUtilities.toURI(file);
        for (CollocationQueryImplementation2 cqi : implementations2.allInstances()) {
            URI root = cqi.findRoot(uri);
            if (root != null) {
                return BaseUtilities.toFile(root);
            }
        }
        for (org.netbeans.spi.queries.CollocationQueryImplementation cqi : implementations.allInstances()) {
            File root = cqi.findRoot(file);
            if (root != null) {
                return root;
            }
        }
        return null;
    }
    
    /**
     * Find a root of a logical tree containing this file, if any.
     * @param file a file on disk
     * @return an ancestor directory which is the root of a logical tree,
     *         if any (else null)
     * @since 1.27
     */
    public static URI findRoot(URI file) {
        if (!file.equals(BaseUtilities.normalizeURI(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+BaseUtilities.normalizeURI(file));  // NOI18N
        }
        for (CollocationQueryImplementation2 cqi : implementations2.allInstances()) {
            URI root = cqi.findRoot(file);
            if (root != null) {
                return root;
            }
        }
        if ("file".equals(file.getScheme())) { // NOI18N
            File f = FileUtil.normalizeFile(BaseUtilities.toFile(file));
            for (org.netbeans.spi.queries.CollocationQueryImplementation cqi : implementations.allInstances()) {
                File root = cqi.findRoot(f);
                if (root != null) {
                    return BaseUtilities.toURI(root);
                }
            }
        }
        return null;
    }
}
