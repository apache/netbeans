/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
