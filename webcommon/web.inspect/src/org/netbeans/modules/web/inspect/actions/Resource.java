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
package org.netbeans.modules.web.inspect.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;

/**
 * Descriptor of a resource (basically a typed wrapper of a {@code String}
 * suitable for inclusion in a lookup).
 *
 * @author Jan Stola
 */
public final class Resource {
    /** Cached mappings from {@code Resource} to {@code FileObject}. */
    private static final Map<Ref,FileObject> cache = new HashMap<Ref, FileObject>();
    /** URI of the resource. */
    private final String name;
    /** Owning project of the resource. */
    private final Project project;

    /**
     * Creates a new {@code Resource}.
     *
     * @param project owning project of the resource.
     * @param name URI of the resource.
     */
    public Resource(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    /**
     * Returns the URI of the resource.
     *
     * @return URI of the resource.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the owning project of the resource.
     * 
     * @return the owning project of the resource.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns a {@code FileObject} that corresponds to this resource.
     *
     * @return {@code FileObject} that corresponds to this resource
     * or {@code null} if the resource doesn't correspond to a file
     * or if the corresponding {@code FileObject} cannot be found.
     */
    public FileObject toFileObject() {
        Ref ref = new Ref(this);
        synchronized (cache) {
            if (cache.containsKey(ref)) {
                FileObject cached = cache.get(ref);
                if ((cached == null) || cached.isValid()) {
                    return cached;
                }
            }
        }
        FileObject result = null;
        // Issue 227766 and 228154
        assert !Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess();
        if (project != null) {
            try {
                result = ServerURLMapping.fromServer(project, new URL(name));
                synchronized (cache) {
                    cache.put(ref, result);
                }
                return result;
            } catch (MalformedURLException ex) {
            }
        }
        if (name != null && name.startsWith("file://")) { // NOI18N
            try {
                URI uri = new URI(name);
                if ((uri.getAuthority() != null) || (uri.getFragment() != null) || (uri.getQuery() != null)) {
                    uri = new URI(uri.getScheme(), null, uri.getPath(), null, null);
                }
                File file = new File(uri);
                file = FileUtil.normalizeFile(file);
                result = FileUtil.toFileObject(file);
            } catch (URISyntaxException ex) {
                Logger.getLogger(Resource.class.getName()).log(Level.INFO, null, ex);
            }
        }
        synchronized (cache) {
            cache.put(ref, result);
        }
        return result;
    }

    /**
     * Clears the cached mappings.
     */
    public static void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * Wrapper of {@code Resource} that defines {@code equals()}
     * and {@code hashCode()} methods. These methods cannot be added
     * into {@code Resource} directly because they would break
     * {@code CSSStylesSelectionPanel.resourceCache} because
     * it is {@code WeakHashMap}.
     */
    static class Ref {
        /** Wrapped {@code Resource}. */
        private final Resource resource;

        /**
         * Creates a new {@code Ref}.
         * 
         * @param resource wrapped {@code Resource}.
         */
        Ref(Resource resource) {
            this.resource = resource;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (resource.name == null ? 0 : resource.name.hashCode());
            hash = 97 * hash + (resource.project == null ? 0 : resource.project.hashCode());
            return hash;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Ref)) {
                return false;
            }
            Ref other = (Ref)object;
            if ((resource.name == null)
                    ? (other.resource.name != null)
                    : !resource.name.equals(other.resource.name)) {
                return false;
            }
            return resource.project == other.resource.project
                    || (resource.project != null && resource.project.equals(other.resource.project));
        }

    }

}
