/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
    PHP_72(Bundle.PhpVersion_PHP_72());

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

    @Override
    public String toString() {
        return getDisplayName();
    }

};
