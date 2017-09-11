/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistence.api;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistenceapi.EntityClassScopeAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Describes an entity class scope, which is basically a bunch of related
 * entity classes on a classpath.
 *
 * @author Andrei Badea
 * @since 1.3
 */
public final class EntityClassScope {

    private static final Lookup.Result<EntityClassScopeProvider> providers =
            Lookup.getDefault().lookupResult(EntityClassScopeProvider.class);

    private final EntityClassScopeImplementation impl;

    static {
        EntityClassScopeAccessor.DEFAULT = new EntityClassScopeAccessor() {
            public EntityClassScope createEntityClassScope(EntityClassScopeImplementation impl) {
                return new EntityClassScope(impl);
            }
        };
    }

    /**
     * Returns the entity class scope for the given file (the entity classes
     * surrounding the given file).
     *
     * @param  fo the file for which to find the entity class scope; cannot be null.
     *
     * @return the entity class scope for the given file or null if there is no
     *         entity class scope.
     *
     * @throws NullPointerException if the fo parameter was null.
     */
    public static EntityClassScope getEntityClassScope(FileObject fo) {
        Parameters.notNull("fo", fo); // NOI18N
        for (EntityClassScopeProvider provider : providers.allInstances()) {
            EntityClassScope entityClassScope = provider.findEntityClassScope(fo);
            if (entityClassScope != null) {
                return entityClassScope;
            }
        }
        return null;
    }

    private EntityClassScope(EntityClassScopeImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the metadata model describing the entity classes in this 
     * entity class scope.
     * 
     * @param  withDeps <code>true</code> if the returned model needs to contain
     *         both the entity classes defined in Java sources and those defined
     *         on the compilation classpath of those sources, <code>false</code>
     *         if the model should only contain the entity classes defined
     *         in Java sources.
     * 
     * @return an entity class model; never null.
     */
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
        return impl.getEntityMappingsModel(withDeps);
    }
}
