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

package org.netbeans.modules.db.metadata.model;

import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Function;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Parameter;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Value;
import org.netbeans.modules.db.metadata.model.api.View;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;
import org.netbeans.modules.db.metadata.model.spi.ColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.ForeignKeyImplementation;
import org.netbeans.modules.db.metadata.model.spi.FunctionImplementation;
import org.netbeans.modules.db.metadata.model.spi.IndexColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.IndexImplementation;
import org.netbeans.modules.db.metadata.model.spi.MetadataImplementation;
import org.netbeans.modules.db.metadata.model.spi.ParameterImplementation;
import org.netbeans.modules.db.metadata.model.spi.PrimaryKeyImplementation;
import org.netbeans.modules.db.metadata.model.spi.ProcedureImplementation;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;
import org.netbeans.modules.db.metadata.model.spi.ValueImplementation;
import org.netbeans.modules.db.metadata.model.spi.ViewImplementation;

/**
 *
 * @author Andrei Badea
 */
public abstract class MetadataAccessor {

    private static volatile MetadataAccessor accessor;

    public static void setDefault(MetadataAccessor accessor) {
        if (MetadataAccessor.accessor != null) {
            throw new IllegalStateException();
        }
        MetadataAccessor.accessor = accessor;
    }

    public static MetadataAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        Class<Metadata> c = Metadata.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return accessor;
    }

    public abstract ForeignKeyColumn createForeignKeyColumn(ForeignKeyColumnImplementation impl);

    public abstract ForeignKey createForeignKey(ForeignKeyImplementation impl);

    public abstract Index createIndex(IndexImplementation impl);

    public abstract IndexColumn createIndexColumn(IndexColumnImplementation impl);

    public abstract MetadataModel createMetadataModel(MetadataModelImplementation impl);

    public abstract Metadata createMetadata(MetadataImplementation impl);

    public abstract Catalog createCatalog(CatalogImplementation impl);

    public abstract Parameter createParameter(ParameterImplementation impl);

    public abstract PrimaryKey createPrimaryKey(PrimaryKeyImplementation impl);

    public abstract Procedure createProcedure(ProcedureImplementation impl);

    public abstract Function createFunction(FunctionImplementation impl);

    public abstract Schema createSchema(SchemaImplementation impl);

    public abstract Table createTable(TableImplementation impl);

    public abstract Column createColumn(ColumnImplementation impl);

    public abstract Value createValue(ValueImplementation impl);

    public abstract View createView(ViewImplementation impl);

    public abstract CatalogImplementation getCatalogImpl(Catalog catalog);
}
