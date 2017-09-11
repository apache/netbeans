/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.GlobalPathRegistryImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe
@ServiceProvider(service = GlobalPathRegistryImplementation.class, position = 10_000)
public final class DefaultGlobalPathRegistryImplementation extends GlobalPathRegistryImplementation {

    private final Map<String,List<ClassPath>> paths = new HashMap<>();

    @Override
    @NonNull
    protected Set<ClassPath> getPaths(@NonNull final String id) {
        List<ClassPath> l = paths.get(id);
        if (l != null && !l.isEmpty()) {
            return Collections.unmodifiableSet(new HashSet<ClassPath>(l));
        } else {
            return Collections.<ClassPath>emptySet();
        }
    }

    @Override
    @NonNull
    protected Set<ClassPath> register(
            @NonNull final String id,
            @NonNull final ClassPath[] paths) {
        final Set<ClassPath> added = new HashSet<>();
        List<ClassPath> l = this.paths.get(id);
        if (l == null) {
            l = new ArrayList<>();
            this.paths.put(id, l);
        }
        for (ClassPath path : paths) {
            if (path == null) {
                throw new NullPointerException("Null path encountered in " + Arrays.asList(paths) + " of type " + id); // NOI18N
            }
            if (!added.contains(path) && !l.contains(path)) {
                added.add(path);
            }
            l.add(path);
        }
        return added;
    }

    @Override
    @NonNull
    protected Set<ClassPath> unregister(
            @NonNull final String id,
            @NonNull final ClassPath[] paths) throws IllegalArgumentException {
        final Set<ClassPath> removed = new HashSet<>();
        List<ClassPath> l = this.paths.get(id);
        if (l == null) {
            l = new ArrayList<>();
        }
        List<ClassPath> l2 = new ArrayList<>(l); // in case IAE thrown below
        for (ClassPath path : paths) {
            if (path == null) {
                throw new NullPointerException();
            }
            if (!l2.remove(path)) {
                throw new IllegalArgumentException("Attempt to remove nonexistent path [" + path +
                        "] from list of registered paths ["+l2+"] for id "+id+". All paths: "+this.paths); // NOI18N
            }
            if (!removed.contains(path) && !l2.contains(path)) {
                removed.add(path);
            }
        }
        this.paths.put(id, l2);
        return removed;
    }

    @Override
    @NonNull
    protected Set<ClassPath> clear() {
        final Set<ClassPath> removed = new HashSet<>();
        for (Iterator<Map.Entry<String,List<ClassPath>>> it = paths.entrySet().iterator();
            it.hasNext();) {
            final Map.Entry<String, List<ClassPath>> e = it.next();
            removed.addAll(e.getValue());
            it.remove();
        }
        assert paths.isEmpty();
        return removed;
    }
}
