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

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.Bitness;
import eu.hansolo.jdktools.Latest;
import eu.hansolo.jdktools.LibCType;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.ReleaseStatus;
import eu.hansolo.jdktools.TermOfSupport;
import eu.hansolo.jdktools.versioning.Semver;
import eu.hansolo.jdktools.versioning.VersionNumber;
import io.foojay.api.discoclient.DiscoClient;
import io.foojay.api.discoclient.event.Evt;
import io.foojay.api.discoclient.event.EvtObserver;
import io.foojay.api.discoclient.event.EvtType;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.MajorVersion;
import io.foojay.api.discoclient.pkg.Pkg;
import io.foojay.api.discoclient.pkg.Scope;
import io.foojay.api.discoclient.util.PkgInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Client {

    private static final Client INSTANCE = new Client();

    private DiscoClient client = null;
    private List<MajorVersion> majorVersions;
    private List<Distribution> distributions;

    private Client() {}

    public static Client getInstance() {
        return INSTANCE;
    }

    private synchronized DiscoClient getDisco() {
        if (client == null) {
            client = new DiscoClient(DiscoPlatformInstall.clientName());
        }
        return client;
    }

    public synchronized List<MajorVersion> getAllMajorVersions() {
        if (majorVersions == null) {
            majorVersions = Collections.unmodifiableList(new ArrayList<>(getDisco().getAllMajorVersions(true)));
        }
        return majorVersions;
    }

    public synchronized List<Distribution> getDistributions() {
        if (distributions == null) {
            distributions = Collections.unmodifiableList(
                getDisco().getDistributions().stream()
                    .filter(distribution -> distribution.getScopes().contains(Scope.BUILD_OF_OPEN_JDK))
                    .filter(distribution -> distribution.getScopes().contains(Scope.PUBLIC))
                    .collect(Collectors.toList())
            );
        }
        return distributions;
    }

    /**
     * Returns all major versions which are still maintained (excludes EA releases).
     */
    public Stream<MajorVersion> getAllMaintainedMajorVersions() {
        return getAllMajorVersions().stream()
                .filter(v -> v.isEarlyAccessOnly() != null && !v.isEarlyAccessOnly())
                .filter(MajorVersion::isMaintained);
    }

    public MajorVersion getLatestGAVersion() {
        return getAllMaintainedMajorVersions()
                .findFirst()
                .orElse(new MajorVersion(21));
    }

    public MajorVersion getLatestEAVersion() {
        return getAllMajorVersions().stream()
                .filter(v -> v.isEarlyAccessOnly() != null && v.isEarlyAccessOnly())
                .findFirst()
                .orElse(new MajorVersion(21));
    }

    public synchronized List<Pkg> getPkgs(final Distribution distribution, final VersionNumber versionNumber, final Latest latest, final OperatingSystem operatingSystem,
            final Architecture architecture, final ArchiveType archiveType, final PackageType packageType,
            final boolean ea, final boolean javafxBundled) {
        return getDisco().getPkgs(List.of(distribution),
                versionNumber,
                latest,
                operatingSystem,
                LibCType.NONE,
                architecture,
                Bitness.NONE,
                archiveType,
                packageType,
                javafxBundled,
                /*directlyDownloadable*/ true,
                ea ? List.of(ReleaseStatus.GA, ReleaseStatus.EA) : List.of(ReleaseStatus.GA),
                TermOfSupport.NONE,
                List.of(Scope.PUBLIC),
                null
        );
    }

    public Optional<Distribution> getDistribution(String text) {
        return getDistributions().stream()
                .filter(d -> d.getSynonyms().contains(text))
                .findFirst();
    }

    public synchronized PkgInfo getPkgInfo(String ephemeralId, Semver javaVersion) {
        return getDisco().getPkgInfoByEphemeralId(ephemeralId, javaVersion);
    }

    public synchronized Future<?> downloadPkg(PkgInfo pkgInfo, String absolutePath) throws InterruptedException {
        return getDisco().downloadPkg(pkgInfo, absolutePath);
    }

    public synchronized void setOnEvt(final EvtType<? extends Evt> type, final EvtObserver observer) {
        //XXX: in theory this could be delayed until disco is instantiated...
        getDisco().setOnEvt(type, observer);
    }

    public synchronized void removeAllObservers() {
        getDisco().removeAllObservers();
    }

}
