/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.disco;

import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.ReleaseStatus;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.Semver;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.function.Function;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;


public class BundleTableModel extends AbstractTableModel {

    private enum Column {

        VERSION("Version", Semver.class, bundle -> bundle.getJavaVersion()),
        DISTRIBUTION("Distribution", Distribution.class, bundle -> bundle.getDistribution().getUiString()),
        ARCHITECTURE("Architecture", String.class, bundle -> bundle.getArchitecture().getUiString()),
        LIBC("LibC", String.class, bundle -> bundle.getLibCType().getUiString()),
        BUNDLE("Bundle", PackageType.class, bundle -> bundle.getPackageType().getUiString()),
        SUPPORT("Support", String.class, bundle -> toString(bundle.getTermOfSupport())),
        STATUS("Release Status", ReleaseStatus.class, bundle -> bundle.getReleaseStatus().name()),
        EXTENSION("Extension", ArchiveType.class, bundle -> bundle.getArchiveType().getUiString());

        private static String toString(TermOfSupport tos) {
            switch (tos) {
                case LTS: return "LTS";
                case MTS: return "MTS";
                case STS: return "STS";
                default:  return "";
            }
        }

        private final String colname;
        private final Class<?> type;
        private final Function<Pkg, Object> valueMapping;

        private Column(String name, Class<?> type, Function<Pkg, Object> valueMapping) {
            this.colname = name;
            this.type = type;
            this.valueMapping = valueMapping;
        }

        private Object getValueFor(Pkg bundle) {
            return valueMapping.apply(bundle);
        }
    }

    private static final Column[] COLUMNS = Column.values();

    private List<Pkg> bundles;


    public BundleTableModel(final List<Pkg> bundles) {
        this.bundles = bundles;
    }


    public List<Pkg> getBundles() {
        return bundles;
    }

    public void setBundles(final List<Pkg> bundles) {
        this.bundles = bundles;
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return bundles == null ? 0 : bundles.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public @NonNull String getColumnName(final int col) {
        return COLUMNS[col].colname;
    }

    @Override
    public @NonNull Class<?> getColumnClass(final int col) {
        return COLUMNS[col].type;
    }

    @Override
    public @NonNull Object getValueAt(final @NonNegative int row, final @NonNegative int col) {
        return COLUMNS[col].getValueFor(bundles.get(row));
    }
}
