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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.openide.util.NbBundle;

/**
 * Represents a table and its references used and displayed in the wizard.
 *
 * @author Andrei Badea
 */
public abstract class Table implements Comparable<Table> {

    private final String catalog;
    private final String schema;
    private final String name;
    private final boolean join;
    private final DisabledReason disabledReason;
    private final boolean tableOrView; // true for table and false for view

    public Table(String catalog, String schema, String name, boolean join, DisabledReason disabledReason) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.join = join;
        this.disabledReason = disabledReason;
        tableOrView = true; // default to table
    }
    
    public Table(String catalog, String schema, String name, boolean join, DisabledReason disabledReason, boolean isTable) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.join = join;
        this.disabledReason = disabledReason;
        tableOrView = isTable;
    }
    
    public boolean isTable() {
        return tableOrView;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof Table) {
            return compareTo((Table)that) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Table that) {
        if (that == null) {
            return 1;
        }
        return this.getName().compareTo(that.getName());
    }
    
    public String getSchema() {
        return this.schema;
    }
    
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * Returns the name of the table.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if the table is a join table.
     */
    public boolean isJoin() {
        return join;
    }

    /**
     * Returns the reason why this table should be disabled when displayed
     * in the UI.
     */
    public DisabledReason getDisabledReason() {
        return disabledReason;
    }

    /**
     * Returns true if the table is disabled. In this case {@link #getDisabledReason}
     * will return a non-true value.
     */
    public boolean isDisabled() {
        return disabledReason != null;
    }

    @Override
    public String toString() {
        return "TableItem[name='" + name + "']"; // NOI18N
    }

    /**
     * Returns the tables this table references.
     */
    public abstract Set<Table> getReferencedTables();

    /**
     * Returns the table referenced by this table.
     */
    public abstract Set<Table> getReferencedByTables();

    /**
     * Returns the tables which this table joins.
     */
    public abstract Set<Table> getJoinTables();
    
    /**
     * Returns the unique constaints defined on this table
     */
    public abstract Set<List<String>> getUniqueConstraints();

    /**
     * A generic reason for a table to be disabled. If there is no need
     * to specify the exact reason why the table is disabled, this class can
     * be used, otherwise it can be subclassed, like {@link #ExistingDisabledReason}.
     */
    public static class DisabledReason {

        private final String displayName;
        private final String description;

        public DisabledReason(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * This implementation of DisabledReason specifies that a table is disabled
     * because an entity class already exists for it.
     */
    public static final class ExistingDisabledReason extends DisabledReason {

        private String fqClassName;

        public ExistingDisabledReason(String fqClassName) {
            super(NbBundle.getMessage(Table.class, "LBL_AlreadyMapped", JavaIdentifiers.unqualify(fqClassName)),
                    NbBundle.getMessage(Table.class, "LBL_AlreadyMappedDescription", fqClassName));
            this.fqClassName = fqClassName;
        }

        public String getFQClassName() {
            return fqClassName;
        }
    }
    /**
     * This implementation of DisabledReason specifies that a table is disabled
     * because an entity class already exists for it.
     */
    public static final class ExistingNotInSourceDisabledReason extends DisabledReason {

        private String fqClassName;

        public ExistingNotInSourceDisabledReason(String fqClassName) {
            super(NbBundle.getMessage(Table.class, "LBL_AlreadyMapped", JavaIdentifiers.unqualify(fqClassName)),
                    NbBundle.getMessage(Table.class, "LBL_AlreadyMappedNoSourceDescription", fqClassName));
            this.fqClassName = fqClassName;
        }

        public String getFQClassName() {
            return fqClassName;
        }
    }
    /**
     * This implementation of DisabledReason specifies that a table is disabled
     * because an entity class already exists for it and can't be updated.
     */
    public static final class ExistingReadOnlyDisabledReason extends DisabledReason {

        private String fqClassName;

        public ExistingReadOnlyDisabledReason(String fqClassName) {
            super(NbBundle.getMessage(Table.class, "LBL_AlreadyMapped", JavaIdentifiers.unqualify(fqClassName)),
                    NbBundle.getMessage(Table.class, "LBL_AlreadyMappedDescription", fqClassName));
            this.fqClassName = fqClassName;
        }

        public String getFQClassName() {
            return fqClassName;
        }
    }

    /**
     * This implementation of DisabledReason specifies that a table is disabled
     * because it doesn't have a primary key.
     */
    public static final class NoPrimaryKeyDisabledReason extends DisabledReason {

        public NoPrimaryKeyDisabledReason() {
            super(NbBundle.getMessage(Table.class, "LBL_NoPrimaryKey"), NbBundle.getMessage(Table.class, "LBL_NoPrimaryKeyDescription"));
        }
    }
}
