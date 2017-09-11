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

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistenceapi.PersistenceScopesAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Describes a list of persistence scopes and allows listening on this list.
 *
 * @author Andrei Badea
 */
public final class PersistenceScopes {

    /**
     * The property corresponding to {@link #getPersistenceScopes}.
     */
    public static final String PROP_PERSISTENCE_SCOPES = "persistenceScopes"; // NOI18N

    private final PersistenceScopesImplementation impl;

    static {
        PersistenceScopesAccessor.DEFAULT = new PersistenceScopesAccessor() {
            public PersistenceScopes createPersistenceScopes(PersistenceScopesImplementation impl) {
                return new PersistenceScopes(impl);
            }
        };
    }

    /**
     * Returns an instance of <code>PersistenceScopes</code> for the given
     * project.
     *
     * @return an instance of <code>PersistenceScopes</code> or null if the
     *         project doesn't provide a list of persistence scopes.
     * @throws NullPointerException if <code>project</code> was null.
     */
    public static PersistenceScopes getPersistenceScopes(Project project) {
        Parameters.notNull("project", project); // NOI18N
        PersistenceScopesProvider provider = (PersistenceScopesProvider)project.getLookup().lookup(PersistenceScopesProvider.class);
        if (provider != null) {
            return provider.getPersistenceScopes();
        }
        return null;
    }

    /**
     * Returns an instance of <code>PersistenceScopes</code> for the given
     * project's root.
     *
     * @return an instance of <code>PersistenceScopes</code> or null if the
     *         project doesn't provide a list of persistence scopes.
     * @throws NullPointerException if <code>project</code> was null.
     * @since 1.37
     */
    public static PersistenceScopes getPersistenceScopes(Project project, FileObject fo) {
        Parameters.notNull("project", project); // NOI18N
        PersistenceScopesProvider provider = (PersistenceScopesProvider)project.getLookup().lookup(PersistenceScopesProvider.class);
        if (provider != null) {
            return provider.getPersistenceScopes(fo);
        }
        return null;
    }

    private PersistenceScopes(PersistenceScopesImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the persistence scopes contained in this instance.
     *
     * @return an array of <code>PersistenceScope</code> instances; never null.
     */
    public PersistenceScope[] getPersistenceScopes() {
        return impl.getPersistenceScopes();
    }

    /**
     * Adds a property change listener, allowing to listen on properties, e.g.
     * {@link #PROP_PERSISTENCE_SCOPES}.
     *
     * @param  listener the listener to add; can be null.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param  listener the listener to remove; can be null.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }
}
