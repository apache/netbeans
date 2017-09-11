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

package org.netbeans.modules.db.metadata.model.api;

import java.util.Collection;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;

/**
 *
 * @author Andrei Badea
 */
public class Catalog extends MetadataElement {

    final CatalogImplementation impl;

    Catalog(CatalogImplementation impl) {
        this.impl = impl;
    }

    public MetadataElement getParent() {
        return null;
    }

    /**
     * Returns the name of this catalog or {@code null} if the name is not known.
     *
     * @return the name or {@code null}.
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Returns {@code true} if this catalog is the default one in this metadata model.
     *
     * @return {@code true} if this is the default catalog, {@false} otherwise.
     */
    public boolean isDefault() {
        return impl.isDefault();
    }

    /**
     * Returns the synthetic schema in this catalog. The catalog has
     * a synthetic schema if the database doesn't support schemas. Synthetic
     * schemas have {@code null} names.
     *
     * <p>If this method returns {@code null}, then the database supports schemas,
     * and they can be retrieved by the {@link #getSchemas} method.</p>
     *
     * @return the synthetic schema or {@code null}.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Schema getSyntheticSchema() {
        return impl.getSyntheticSchema();
    }

    /**
     * Returns the schemas in this catalog.
     *
     * <p>If the database does not supports schemas, this method always returns
     * an empty collection. In that case, a synthetic schema available
     * through {@link #getSyntheticSchema} provides access to the tables and
     * other metadata elements in the catalog.</p>
     *
     * @return the schemas.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Collection<Schema> getSchemas() {
        return impl.getSchemas();
    }

    /**
     * Returns the schema with the given name.
     *
     * @param name a schema name.
     * @return a schema named {@code name} or {@code null} if there is no such schema.
     * @throws MetadataException if an error occurs while retrieving the metadata.
     */
    public Schema getSchema(String name) {
        return impl.getSchema(name);
    }

    /**
     * Refresh the entire catalog
     *
     */
    public void refresh() {
        impl.refresh();
    }

    @Override
    public String toString() {
        return "Catalog[name='" + impl.getName() + "',default=" + isDefault() + "]"; // NOI18N
    }
}
