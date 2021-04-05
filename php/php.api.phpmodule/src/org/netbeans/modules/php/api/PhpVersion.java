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
    "PhpVersion.PHP_74=PHP 7.4"
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
    PHP_74(Bundle.PhpVersion_PHP_74());

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

    @Override
    public String toString() {
        return getDisplayName();
    }

};
