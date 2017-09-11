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
package org.netbeans.spi.java.classpath;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.java.classpath.SPIAccessor;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * The SPI interface for the {@link GlobalPathRegistry}.
 * Allows different implementations of the {@link GlobalPathRegistry}.
 * The first SPI instance registered in the global {@link Lookup} is used
 * by the {@link GlobalPathRegistry}.
 * Threading: The implementations don't need to be thread safe,
 * synchronization is done by the {@link GlobalPathRegistry}.
 * @author Tomas Zezula
 * @since 1.48
 */
public abstract class GlobalPathRegistryImplementation {

    static {
        SPIAccessor.setInstance(new AccessorImpl());
    }

    private volatile GlobalPathRegistry owner;

    /**
     * Find all paths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @return an immutable set of all registered {@link ClassPath}s of that type (may be empty but not null)
     */
    @NonNull
    protected abstract Set<ClassPath> getPaths(@NonNull String id);

    /**
     * Register some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to add to the registry
     * @return the added classpaths
     */
    @NonNull
    protected abstract Set<ClassPath> register(@NonNull String id, @NonNull ClassPath[] paths);

    /**
     * Unregister some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to remove from the registry
     * @return the removed classpaths
     * @throws IllegalArgumentException if they had not been registered before
     */
    @NonNull
    protected abstract Set<ClassPath> unregister(@NonNull String id, @NonNull ClassPath[] paths) throws IllegalArgumentException;

    /**
     * Removes all known classpaths.
     * @return a set of removed classpaths
     */
    @NonNull
    protected abstract Set<ClassPath> clear();

    private static final class AccessorImpl extends SPIAccessor {

        @Override
        @NonNull
        public Set<ClassPath> getPaths(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final String id) {
            return impl.getPaths(id);
        }

        @Override
        @NonNull
        public Set<ClassPath> register(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final String id,
                @NonNull final ClassPath[] paths) {
            return impl.register(id, paths);
        }

        @Override
        @NonNull
        public Set<ClassPath> unregister(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final String id,
                @NonNull final ClassPath[] paths) throws IllegalArgumentException {
            return impl.unregister(id, paths);
        }

        @Override
        @NonNull
        public Set<ClassPath> clear(@NonNull final GlobalPathRegistryImplementation impl) {
            return impl.clear();
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public void attachAPI(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final GlobalPathRegistry api) {
            Parameters.notNull("api", api); //NOI18N
            impl.owner = api;
        }
    }
}
