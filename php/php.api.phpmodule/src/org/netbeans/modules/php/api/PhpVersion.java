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
package org.netbeans.modules.php.api;

import java.time.LocalDate;
import org.openide.util.NbBundle;

/**
 * Class representing a PHP version.
 * @since 2.28
 */
@NbBundle.Messages({
    "PhpVersion.PHP_5=PHP 5.2/5.1",
    "PhpVersion.PHP_53=PHP 5.3",
    "PhpVersion.PHP_54=PHP 5.4",
    "PhpVersion.PHP_55=PHP 5.5",
    "PhpVersion.PHP_56=PHP 5.6",
    "PhpVersion.PHP_70=PHP 7.0",
    "PhpVersion.PHP_71=PHP 7.1",
    "PhpVersion.PHP_72=PHP 7.2",
    "PhpVersion.PHP_73=PHP 7.3",
    "PhpVersion.PHP_74=PHP 7.4",
    "PhpVersion.PHP_80=PHP 8.0",
})
public enum PhpVersion {

    // order is important! from oldest to newest, see #getDefault()
    /**
     * PHP 5.2/5.1.
     */
    PHP_5(Bundle.PhpVersion_PHP_5(), false),
    /**
     * PHP 5.3.
     */
    PHP_53(Bundle.PhpVersion_PHP_53()),
    /**
     * PHP 5.4.
     */
    PHP_54(Bundle.PhpVersion_PHP_54()),
    /**
     * PHP 5.5.
     */
    PHP_55(Bundle.PhpVersion_PHP_55()),
    /**
     * PHP 5.6.
     */
    PHP_56(Bundle.PhpVersion_PHP_56()),
    /**
     * PHP 7.0.
     * @since 2.58
     */
    PHP_70(Bundle.PhpVersion_PHP_70()),
    /**
     * PHP 7.1.
     * @since 2.60
     */
    PHP_71(Bundle.PhpVersion_PHP_71()),
    /**
     * PHP 7.2.
     * @since 2.61
     */
    PHP_72(Bundle.PhpVersion_PHP_72()),
    /**
     * PHP 7.3.
     * @since 2.62
     */
    PHP_73(Bundle.PhpVersion_PHP_73()),
    /**
     * PHP 7.4.
     * @since 2.65
     */
    PHP_74(Bundle.PhpVersion_PHP_74()),
    /**
     * PHP 8.0.
     * @since 2.74
     */
    PHP_80(Bundle.PhpVersion_PHP_80());

    private final String displayName;
    private final boolean namespaces;


    private PhpVersion(String displayName) {
        this(displayName, true);
    }

    private PhpVersion(String displayName, boolean namespaces) {
        assert displayName != null;
        this.displayName = displayName;
        this.namespaces = namespaces;
    }

    /**
     * Always return the latest PHP version.
     * @return the latest PHP version
     */
    public static PhpVersion getDefault() {
        PhpVersion[] phpVersions = PhpVersion.values();
        return phpVersions[phpVersions.length - 1];
    }

    /**
     * Return legacy PHP version, the one before the latest one.
     * <p>
     * This usually means the oldest yet supported PHP version.
     * @return the legacy PHP version
     * @since 2.41
     */
    public static PhpVersion getLegacy() {
        PhpVersion[] phpVersions = PhpVersion.values();
        for (PhpVersion phpVersion : phpVersions) {
            if (phpVersion.isSupportedVersion()) {
                return phpVersion;
            }
        }
        return phpVersions[phpVersions.length - 2];
    }

    /**
     * Get display name of this version.
     * @return display name of this version
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Doec this version support namespaces?
     * @return {@code true} if this version supports namespaces, {@code false} otherwise
     */
    public boolean hasNamespaces() {
        return namespaces;
    }

    /**
     * Check whether this version supports scalar and return type declarations.
     *
     * @return {@code true} if this version scalar and return type declarations,
     * {@code false} otherwise
     * @since 2.67
     */
    public boolean hasScalarAndReturnTypes() {
        return this.compareTo(PhpVersion.PHP_70) >= 0;
    }

    /**
     * Check whether this version supports nullable types.
     *
     * @return {@code true} if this version supports nullable types,
     * {@code false} otherwise
     * @since 2.67
     */
    public boolean hasNullableTypes() {
        return this.compareTo(PhpVersion.PHP_71) >= 0;
    }

    /**
     * Check whether this version supports a void return type.
     *
     * @return {@code true} if this version supports a void return type,
     * {@code false} otherwise
     * @since 2.68
     */
    public boolean hasVoidReturnType() {
        return this.compareTo(PhpVersion.PHP_71) >= 0;
    }

    /**
     * Check whether this version supports typed properties.
     *
     * @return {@code true} if this version supports typed properties,
     * {@code false} otherwise
     * @since 2.67
     */
    public boolean hasPropertyTypes() {
        return this.compareTo(PhpVersion.PHP_74) >= 0;
    }

    /**
     * Check whether this version supports the mixed type.
     *
     * @return {@code true} if this version supports mixed type, {@code false}
     * otherwise
     * @since 2.74
     */
    public boolean hasMixedType() {
        return this.compareTo(PhpVersion.PHP_80) >= 0;
    }

    /**
     * Check whether this is supported version yet by PHP official.
     *
     * @return {@code true} if this is supported version, {@code false}
     * otherwise
     * @since 2.72
     */
    public boolean isSupportedVersion() {
        return Period.valueOf(name()).isSupportedVersion();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Valid period for each php version.
     * https://www.php.net/supported-versions.php
     */
    private enum Period {
        // Use the same name as PhpVersion
        PHP_5(LocalDate.of(2005, 11, 24), LocalDate.of(2011, 1, 6), LocalDate.of(2011, 1, 6)),
        PHP_53(LocalDate.of(2009, 6, 30), LocalDate.of(2014, 8, 14), LocalDate.of(2014, 8, 14)),
        PHP_54(LocalDate.of(2012, 3, 1), LocalDate.of(2014, 9, 14), LocalDate.of(2014, 9, 14)),
        PHP_55(LocalDate.of(2013, 6, 20), LocalDate.of(2015, 7, 10), LocalDate.of(2015, 7, 10)),
        PHP_56(LocalDate.of(2014, 8, 28), LocalDate.of(2017, 1, 19), LocalDate.of(2018, 12, 31)),
        PHP_70(LocalDate.of(2015, 12, 3), LocalDate.of(2017, 12, 3), LocalDate.of(2018, 12, 3)),
        PHP_71(LocalDate.of(2016, 12, 1), LocalDate.of(2018, 12, 1), LocalDate.of(2019, 12, 1)),
        PHP_72(LocalDate.of(2017, 11, 30), LocalDate.of(2019, 11, 30), LocalDate.of(2020, 11, 30)),
        PHP_73(LocalDate.of(2018, 12, 6), LocalDate.of(2020, 12, 6), LocalDate.of(2021, 12, 6)),
        PHP_74(LocalDate.of(2019, 11, 28), LocalDate.of(2021, 11, 28), LocalDate.of(2022, 11, 28)),
        PHP_80(LocalDate.of(2020, 11, 26), LocalDate.of(2022, 11, 26), LocalDate.of(2023, 11, 26)),
        ;

        private final LocalDate initialRelease;
        private final LocalDate activeSupport;
        private final LocalDate securitySupport;

        private Period(LocalDate initialRelease, LocalDate activeSupport, LocalDate securitySupport) {
            this.initialRelease = initialRelease;
            this.activeSupport = activeSupport;
            this.securitySupport = securitySupport;
        }

        public LocalDate getInitialRelease() {
            return initialRelease;
        }

        public LocalDate getActiveSupport() {
            return activeSupport;
        }

        public LocalDate getSecuritySupport() {
            return securitySupport;
        }

        public boolean isSupportedVersion() {
            return LocalDate.now().isBefore(securitySupport);
        }

    }

};
