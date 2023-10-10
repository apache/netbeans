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
package org.netbeans.modules.php.phpunit;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

@NbBundle.Messages({
    "PhpUnitVersion.PHP_UNIT_9=PHPUnit 9 or earlier",
    "PhpUnitVersion.PHP_UNIT_10=PHPUnit 10 or later",
})
public enum PhpUnitVersion {
    // please order from oldest to newest
    PHP_UNIT_9(Bundle.PhpUnitVersion_PHP_UNIT_9()),
    PHP_UNIT_10(Bundle.PhpUnitVersion_PHP_UNIT_10()),
    ;

    private static final Logger LOGGER = Logger.getLogger(PhpUnitVersion.class.getName());

    private final String displayName;

    private PhpUnitVersion(String displayName) {
        this.displayName = displayName;
    }

    public static PhpUnitVersion getDefault() {
        PhpUnitVersion[] phpUnitVersions = PhpUnitVersion.values();
        return phpUnitVersions[0];
    }

    /**
     * Get PhpUnitVersion from string.
     * If the version is unexpected pattern, the default version is returned ({@link #getDefault()}).
     *
     * @param version the version (expected pattern: <major>.<minor>.<patch> e.g. 10.0.1)
     * @return PhpUnitVersion
     */
    public static PhpUnitVersion fromString(@NonNull String version) {
        Parameters.notNull("version", version); // NOI18N
        String[] versions = version.split("\\."); // NOI18N
        try {
            int majorVersion = Integer.parseInt(versions[0]);
            if (majorVersion >= 10) {
                return PHP_UNIT_10;
            } else if (majorVersion <= 9) {
                return PHP_UNIT_9;
            } else {
                LOGGER.log(Level.WARNING, "Unexpected PHPUnit major version:{0} ({1})", new Object[]{majorVersion, version}); // NOI18N
            }
        } catch (NumberFormatException ex) {
            // no-op
        }
        return getDefault();
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean useNetBeansSuite() {
        return this == PHP_UNIT_9;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
