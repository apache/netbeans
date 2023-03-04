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

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.queries.VersioningQueryImplementation;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;

/**
 * Find out Versioning System relevant information about a file.
 * 
 * @author Tomas Stupka
 * @since 1.35
 */
public final class VersioningQuery {

    private static final Logger LOG = Logger.getLogger(FileEncodingQuery.class.getName());
    
    private static final Lookup.Result<org.netbeans.spi.queries.VersioningQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(org.netbeans.spi.queries.VersioningQueryImplementation.class);
        
    private VersioningQuery() { }
    
    /**
     * Determines whether the given local file or directory is managed by a Versioning System 
     * - e.g. located in a SVN checkout or Mercurial clone.
     * 
     * @param uri a {@link org.openide.filesystems.FileUtil#normalizeFile normalized} file to check if managed
     * @return <code>true</code> if the file is managed, otherwise <code>false</code>.
     * @since 1.35
     */
    public static boolean isManaged(URI uri) {
        java.util.Objects.requireNonNull(uri);
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            URI normUri = BaseUtilities.normalizeURI(uri);
            if (!uri.equals(normUri)) {
                throw new IllegalArgumentException("Must pass a normalized URI: " + uri + " vs. " + normUri);
            }
        }
        
        for (VersioningQueryImplementation vqi : implementations.allInstances()) {
            if(vqi.isManaged(uri)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0} is VCS managed", new Object[] {uri}); // NOI18N
                }
                return true;
            }
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0} isn't managed by any VCS", new Object[] {uri}); // NOI18N
        }
        return false;
    }

    /**
     * Provides the Versioning System specific information about a files remote repository or origin. 
     * This might be for example in case of Subversion the repository url, or in case of Mercurial the default pull url. 
     * Also note that only Versioning Systems available by a supported Team Server are expected to return a
     * valid value. 
     * 
     * @param uri a {@link org.openide.filesystems.FileUtil#normalizeFile normalized} file to check if managed
     * @return value describing the remote location or null if not available or not provided
     * @since 1.35
     */       
    public static String getRemoteLocation(URI uri) {
        java.util.Objects.requireNonNull(uri);
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            URI normUri = BaseUtilities.normalizeURI(uri);
            if (!uri.equals(normUri)) {
                throw new IllegalArgumentException("Must pass a normalized URI: " + uri + " vs. " + normUri);
            }
        }
        
        for (VersioningQueryImplementation vqi : implementations.allInstances()) {
            final String remoteLocation = vqi.getRemoteLocation(uri);
            if(remoteLocation != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: received remote location {1}", new Object[] {uri, remoteLocation}); // NOI18N
                }
                return remoteLocation;
            }
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0}: received no remote location", new Object[] {uri}); // NOI18N
        }
        return null;        
    }
}
