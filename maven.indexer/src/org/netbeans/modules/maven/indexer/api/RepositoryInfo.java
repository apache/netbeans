/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author mkleint
 */
public final class RepositoryInfo {

    public static final String PROP_INDEX_CHANGE = "index.change"; // NOI18N
    public static final String PROP_NO_REMOTE_INDEX = "no.remote.index"; // NOI18N
    
    /** @see org.sonatype.nexus.index.context.DefaultIndexingContext#INDEX_DIRECTORY */
    static final String DEFAULT_INDEX_SUFFIX = ".index/"; // NOI18N

    private final String id;
    private final @NonNull String name;
    private final String repositoryPath;
    private final String repositoryUrl;
    private final String indexUpdateUrl;
    private final List<RepositoryInfo> mirrorOf = new ArrayList<RepositoryInfo>();
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    private MirrorStrategy mirrorStrategy = MirrorStrategy.NON_WILDCARD;

    public RepositoryInfo(String id, @NullAllowed String name, String repositoryPath, String repositoryUrl) throws URISyntaxException {
        this(id, name, repositoryPath, repositoryUrl, null);
    }
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    RepositoryInfo(String id, @NullAllowed String name, String repositoryPath,
            String repositoryUrl, String indexUpdateUrl) throws URISyntaxException {
        this.id = id;
        this.name = name != null ? name : id;
        this.repositoryPath = repositoryPath;
        if (repositoryUrl != null && !repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }
        if (repositoryUrl != null) {
            new URI(repositoryUrl);
        }
        this.repositoryUrl = repositoryUrl;
        this.indexUpdateUrl = indexUpdateUrl != null ? indexUpdateUrl : repositoryUrl != null ? repositoryUrl + DEFAULT_INDEX_SUFFIX : null;
        if (!isLocal() ^ isRemoteDownloadable()) {
            throw new IllegalArgumentException("Cannot have both local and remote index fields filled in. Repository: " + id + " Path=" + repositoryPath + " Remote URL:" + indexUpdateUrl);
        }
    }

    public @NonNull String getId() {
        return id;
    }

    public @NonNull String getName() {
        return name;
    }

    public @CheckForNull String getRepositoryPath() {
        return repositoryPath;
    }

    public @CheckForNull String getRepositoryUrl() {
        return repositoryUrl;
    }

    public @CheckForNull String getIndexUpdateUrl() {
        return indexUpdateUrl;
    }

    public boolean isRemoteDownloadable() {
        return indexUpdateUrl != null;
    }
    
    public boolean isLocal() {
        return repositoryPath != null;
    }
        
    /**
     * Notifies listeners that the index content has changed.
     * to be called from RepositoryIndexerImplementation only.
     */
    public void fireIndexChange() {
        support.firePropertyChange(PROP_INDEX_CHANGE, null, null);
    }
    
    /**
     * Notifies listeners that there was no remote index available.
     */
    public void fireNoIndex() {
        support.firePropertyChange(PROP_NO_REMOTE_INDEX, null, null);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    /**
     * denotes if the current instance if mirroring one or more other urls.
     * @return 
     * @since 2.10
     */
    public boolean isMirror() {
        synchronized (mirrorOf) {
            return !mirrorOf.isEmpty();
        }
    }
    
    /**
     * list of repositories mirrored by this instance.
     * @return 
     * @since 2.10
     */
    public List<RepositoryInfo> getMirroredRepositories() {
        synchronized (mirrorOf) {
            return new ArrayList<RepositoryInfo>(mirrorOf);
        }
    }
    
    /**
     * 
     * @return 
     * @since 2.10
     */
    void addMirrorOfRepository(RepositoryInfo info) {
        assert isRemoteDownloadable();
        synchronized (mirrorOf) {
            mirrorOf.add(info);
        }
    }

    
    /**
     * @since 2.11
     */
    public MirrorStrategy getMirrorStrategy() {
        return mirrorStrategy;
    }

    /**
     * @since 2.11
     */
    public void setMirrorStrategy(MirrorStrategy mirrorStrategy) {
        this.mirrorStrategy = mirrorStrategy;
    }
    

    /**
     * strategy for resolving the repositoryUrl property
     * @since 2.11
     */
    public enum MirrorStrategy {
        /**
         * no processing happens, repositoryUrl is used as is.
         */
        NONE, 
        /**
         * id and repositoryUrl properties are processed through the mirrors settings in ~/.m2/repository
         */
        ALL, 
        /**
         * only explicit mirrors matching the id are used, wildcard mirrors are ignored.
         */
        NON_WILDCARD
    }
    
     public @Override String toString() {
        return id;
    }   

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepositoryInfo other = (RepositoryInfo) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.repositoryPath == null) ? (other.repositoryPath != null) : !this.repositoryPath.equals(other.repositoryPath)) {
            return false;
        }
        if ((this.repositoryUrl == null) ? (other.repositoryUrl != null) : !this.repositoryUrl.equals(other.repositoryUrl)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.repositoryPath != null ? this.repositoryPath.hashCode() : 0);
        hash = 53 * hash + (this.repositoryUrl != null ? this.repositoryUrl.hashCode() : 0);
        return hash;
    }
    
    
}
