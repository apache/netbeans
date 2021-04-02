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

import io.foojay.api.discoclient.pkg.ArchiveType;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Latest;
import io.foojay.api.discoclient.pkg.PackageType;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.VersionNumber;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

class QuickPkgSelection implements PkgSelection {

    private final VersionNumber version;
    private final Predicate<Pkg> filter;

    private @MonotonicNonNull Pkg pkg = null;

    public QuickPkgSelection(QuickPanel.QuickSelection quick) {
        this.version = new VersionNumber(quick.version);
        this.filter = quick.zip
                ? (p) -> p.getArchiveType() == ArchiveType.ZIP
                : (p) -> {
                    switch (OS.getOperatingSystem()) {
                        case WINDOWS:
                            return ArchiveType.MSI == p.getArchiveType() || ArchiveType.EXE == p.getArchiveType();
                        case MACOS:
                            return ArchiveType.DMG == p.getArchiveType() || ArchiveType.PKG == p.getArchiveType();
                        default:
                            //anything goes?
                            return true;
                    }
                };
    }

    @Override
    public String getFileName() {
        if (pkg == null)
            return "Java " + version.getFeature().getAsInt(); //NOI18N
        else
            return pkg.getFileName();
    }

    @Override
    public String getJavaPlatformDisplayName() {
        if (pkg != null)
            return pkg.getDistribution().getUiString() + " " + pkg.getJavaVersion().toString();
        else
            return "Zulu " + version.getFeature().getAsInt(); //NOI18N
    }

    @Override
    public @Nullable Pkg get(@Nullable Client d) {
        if (pkg != null || d == null)
            return pkg;

        List<Pkg> pkgs;
            pkgs = d.getPkgs(Distribution.ZULU, version, Latest.OVERALL, OS.getOperatingSystem(),
                    OS.getArchitecture(),
                    ArchiveType.NONE, PackageType.JDK, false);
        Optional<Pkg> found = pkgs.stream().filter(filter).findAny();

        if (found.isPresent()) {
            pkg = found.get();
            return pkg;
        } else {
            //what now?
            throw new IllegalStateException("Could not find package for " + version);
        }
    }

}
