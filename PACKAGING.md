<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# Apache NetBeans packaging guidelines

Apache NetBeans is distributed by a number of downstream entities. We
recognize the importance of these distributors, and are grateful for their
support. This document outlines some key requirements that must be
followed to distribute packages of Apache NetBeans under that name. It
complements the ASF's [Downstream Distribution Branding Policy][asf-downstream].

Packages of Apache NetBeans MUST be built from the officially released
source code zip, or from the associated binary zip release. These are
listed on the download pages of the [Apache NetBeans website][nb-download].
Packages MUST NOT be built from staged artefacts or before the release
vote has completed successfully.

Packages MUST NOT be built from a git checkout or the automatic zip on
release tags. The repository contains non-released sources and is
missing certain required metadata for an accurate release.

Testing packages of Apache NetBeans release candidates MAY be produced
using files from the [nightlies server][nb-nightlies]. If made available to
download they MUST be clearly marked as not built from an official
release.

Packages MAY patch the release to remove OS-specific binaries and
other files not required by the target OS. Any other patching must be
in line with the [sources requirements][asf-downstream-sources] of the ASF
distribution policy. We prefer Apache NetBeans is packaged as released.

Packages MAY include an open-source JDK alongside Apache NetBeans
within a single self-contained bundle. The JDK MUST be either the
latest release (recommended), or the latest LTS, compatible with the
Apache NetBeans version. The JDK MUST NOT include JavaFX. Distributors
SHOULD ensure that the licensing information for Apache NetBeans and
for the bundled JDK are clear and distinct.

We provide the [NBPackage tool][nbpackage] for building packages from the
binary zip. It supports bundling a JDK. Distributors MAY choose to use
this tool to build packages.

The Apache NetBeans build is not reproducible. NBPackage is designed
to keep as many files as possible binary comparable with the release
artefact. Distributors using another tool to produce packages from the
binary zip SHOULD ensure that the contents of the package can be
similarly verified. Distributors packaging from a source release
SHOULD ensure that the build process is transparent and the source
link verifiable.

Distributors MUST provide contact details for reporting bugs and
security vulnerabilities related to their packages. They SHOULD
consider suitable practices for security and user experience - build
verification, file hashes, code signing, notarization, immutable
releases, etc.

Any queries about how to apply these requirements and guidelines
should be raised on the [Dev mailing list][nb-dev-list].

[asf-downstream]: https://www.apache.org/foundation/marks/downstream
[nb-download]: https://netbeans.apache.org/download/
[nb-nightlies]: https://nightlies.apache.org/netbeans/candidate/netbeans/
[asf-downstream-sources]: https://www.apache.org/foundation/marks/downstream#source
[nbpackage]: https://github.com/apache/netbeans-nbpackage/
[nb-dev-list]: https://netbeans.apache.org/community/mailing-lists/
